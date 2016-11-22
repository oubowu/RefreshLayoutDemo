package com.oubowu.refreshlayoutdemo.refresh;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ScrollingView;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewTreeObserver;
import android.webkit.WebView;
import android.widget.AbsListView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import static android.support.v4.widget.ViewDragHelper.INVALID_POINTER;

/**
 * Created by Oubowu on 2016/11/20 0:40.
 */
public class RefreshLayout extends LinearLayout {

    // 隐藏的状态
    private static final int HIDE = 0;
    // 下拉刷新的状态
    private static final int PULL_TO_REFRESH = 1;
    // 松开刷新的状态
    private static final int RELEASE_TO_REFRESH = 2;
    // 正在刷新的状态
    private static final int REFRESHING = 3;
    // 正在隐藏的状态
    private static final int HIDING = 4;
    // 当前状态
    private int mCurrentState = HIDE;

    // 头部动画的默认时间（单位：毫秒）
    public static final int DEFAULT_DURATION = 200;

    // 头部高度
    private int mHeaderHeight;
    // 内容控件的滑动距离
    private int mContentViewOffset;
    // 最小滑动响应距离
    private int mScaledTouchSlop;
    // 记录上次的Y坐标
    private float mLastMotionY;
    // 记录一开始的Y坐标
    private float mInitDownY;
    // 响应的手指
    private int mActivePointerId;

    // 是否在处理头部
    private boolean mIsHeaderHandling;
    // 是否可以下拉刷新
    private boolean mIsRefreshable = true;

    // 内容控件是否可以滑动，不能滑动的控件会做触摸事件的优化
    private boolean mContentViewScrollable = true;
    // 头部，为了方便演示选取了TextView
    private TextView mHeader;

    // 容器要承载的内容控件，在XML里面要放置好
    private View mContentView;

    // 值动画，由于头部显示隐藏
    private ValueAnimator mHeaderAnimator;

    // 刷新的监听器
    private OnRefreshListener mOnRefreshListener;

    public RefreshLayout(Context context) {
        this(context, null);
    }

    public RefreshLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RefreshLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
        addHeader(context);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mHeaderAnimator != null && mHeaderAnimator.isRunning()) {
            mHeaderAnimator.removeAllUpdateListeners();
            mHeaderAnimator.removeAllListeners();
            mHeaderAnimator.cancel();
        }
    }

    private void init() {

        mScaledTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();

        mHeaderAnimator = ValueAnimator.ofInt(0).setDuration(DEFAULT_DURATION);
        mHeaderAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                if (getContext() == null) {
                    // 若是退出Activity了，动画结束不必执行头部动作
                    return;
                }
                // 通过设置paddingTop实现显示或者隐藏头部
                int offset = (Integer) valueAnimator.getAnimatedValue();
                mHeader.setPadding(0, offset, 0, 0);
            }
        });
        mHeaderAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (getContext() == null) {
                    // 若是退出Activity了，动画结束不必执行头部动作
                    return;
                }
                if (mCurrentState == RELEASE_TO_REFRESH) {
                    // 释放刷新状态执行的动画结束，意味接下来就是刷新了，改状态并且调用刷新的监听
                    mHeader.setText("正在刷新...");
                    mCurrentState = REFRESHING;
                    if (mOnRefreshListener != null) {
                        mOnRefreshListener.onRefresh();
                    }
                } else if (mCurrentState == HIDING) {
                    // 下拉状态执行的动画结束，隐藏头部，改状态
                    mHeader.setText("我是头部");
                    mCurrentState = HIDE;
                }
            }
        });
    }

    /**
     * 创建头部
     *
     * @param context 上下文
     */
    private void addHeader(Context context) {

        // 强制垂直方法
        setOrientation(LinearLayout.VERTICAL);

        mHeader = new TextView(context);
        mHeader.setBackgroundColor(Color.GRAY);
        mHeader.setTextColor(Color.WHITE);
        mHeader.setText("我是头部");
        mHeader.setTextSize(TypedValue.COMPLEX_UNIT_SP, 25);
        mHeader.setGravity(Gravity.CENTER);
        addView(mHeader, LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);

        mHeader.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // 算出头部高度
                mHeaderHeight = mHeader.getMeasuredHeight();
                // 移除监听
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    mHeader.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                } else {
                    mHeader.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
                // 设置paddingTop为-mHeaderHeight,刚好把头部隐藏掉了
                mHeader.setPadding(0, -mHeaderHeight, 0, 0);
            }
        });

    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        // 设置长点击或者短点击都能消耗事件，要不这样做，若孩子都不消耗，最终点击事件会被它的上级消耗掉，后面一系列的事件都只给它的上级处理了
        setLongClickable(true);

        // 获取内容控件
        mContentView = getChildAt(1);
        if (mContentView == null) {
            // 为空抛异常，强制要求在XML设置内容控件
            throw new IllegalArgumentException("You must add a content view!");
        }
        if (!(ViewCompat.isNestedScrollingEnabled(
                mContentView) && mContentView instanceof ScrollingView || mContentView instanceof WebView || mContentView instanceof ScrollView || mContentView instanceof AbsListView)) {
            // 不是具有滚动的控件，这里设置标志位
            mContentViewScrollable = false;
        }

    }


    @Override
    public boolean dispatchTouchEvent(final MotionEvent event) {

        if (!mIsRefreshable) {
            // 禁止下拉刷新，直接把事件分发
            return super.dispatchTouchEvent(event);
        }

        if ((mCurrentState == REFRESHING || mCurrentState == RELEASE_TO_REFRESH || mCurrentState == HIDING) && mHeaderAnimator.isRunning()) {
            // 正在刷新，正在释放，正在隐藏头部都不处理事件，并且不分发下去
            return true;
        }

        // 支持多指触控
        int actionMasked = MotionEventCompat.getActionMasked(event);

        switch (actionMasked) {

            case MotionEvent.ACTION_DOWN: {
                // 记录响应的手指
                mActivePointerId = event.getPointerId(0);
                // 记录初始Y坐标
                mInitDownY = mLastMotionY = event.getY(0);
            }
            break;

            case MotionEvent.ACTION_POINTER_DOWN: {
                // 另外一根手指按下，切换到这个手指响应
                int pointerDownIndex = MotionEventCompat.getActionIndex(event);
                if (pointerDownIndex < 0) {
                    Log.e("RefreshLayout", "296行-dispatchTouchEvent(): " + "Got ACTION_POINTER_DOWN event but have an invalid action index.");
                    return dispatchTouchEvent(event);
                }
                mActivePointerId = event.getPointerId(pointerDownIndex);
                mLastMotionY = event.getY(pointerDownIndex);
            }
            break;

            case MotionEvent.ACTION_POINTER_UP: {
                // 另外一根手指抬起，切换回其他手指响应
                final int pointerUpIndex = MotionEventCompat.getActionIndex(event);
                final int pointerId = event.getPointerId(pointerUpIndex);
                if (pointerId == mActivePointerId) {
                    // 抬起手指就是之前控制滑动手指，切换其他手指响应
                    final int newPointerIndex = pointerUpIndex == 0 ? 1 : 0;
                    mActivePointerId = event.getPointerId(newPointerIndex);
                }
                mLastMotionY = event.getY(event.findPointerIndex(mActivePointerId));
            }
            break;

            case MotionEvent.ACTION_MOVE: {
                // 移动事件
                if (mActivePointerId == INVALID_POINTER) {
                    Log.e("RefreshLayout", "235行-dispatchTouchEvent(): " + "Got ACTION_MOVE event but don't have an active pointer id.");
                    return dispatchTouchEvent(event);
                }

                float y = event.getY(event.findPointerIndex(mActivePointerId));
                // 移动的偏移量
                float yDiff = y - mLastMotionY;
                mLastMotionY = y;

                if (mContentViewOffset == 0 && (yDiff > 0 || (yDiff < 0 && isHeaderShowing()))) {
                    // 内容控件还没滚动时，下拉或者在头部还在显示的时候上滑时，交由自己处理滑动事件

                    // 滑动的总距离
                    float totalDistanceY = mLastMotionY - mInitDownY;
                    if (totalDistanceY > 0 && totalDistanceY <= mScaledTouchSlop && yDiff > 0) {
                        // 下拉时，优化滑动逻辑，不要稍微一点位移就响应
                        return super.dispatchTouchEvent(event);
                    }

                    // 正在处理事件
                    mIsHeaderHandling = true;

                    if (mCurrentState == REFRESHING) {
                        // 正在刷新，不让contentView响应滑动
                        event.setAction(MotionEvent.ACTION_CANCEL);
                    }

                    // 处理下拉头部
                    scrollHeader(yDiff);

                    break;

                } else if (mIsHeaderHandling) {
                    // 在头部隐藏的那一瞬间的事件特殊处理
                    if (mContentViewScrollable) {
                        // 1.可滑动的View，由于之前处理头部，之前的MOVE事件没有传递到内容页，这里需要要ACTION_DOWN来重新告知滑动的起点，不然会瞬间滑动一段距离
                        // 2.对于不滑动的View设置了点击事件，若这里给它一个ACTION_DOWN事件，在手指抬起时ACTION_UP事件会触发点击，因此这里做了处理
                        event.setAction(MotionEvent.ACTION_DOWN);
                    }
                    mIsHeaderHandling = false;
                }
            }
            break;

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP: {
                // 处理手指抬起或取消事件
                mActivePointerId = INVALID_POINTER;
                if (isHeaderShowing()) {
                    // 头部显示情况下
                    if (actionMasked == MotionEvent.ACTION_CANCEL) {
                        // 取消的话强制不能刷新，状态改为下拉刷新，接下来autoScrollHeader就会隐藏头部
                        mCurrentState = PULL_TO_REFRESH;
                    }
                    autoScrollHeader();
                }
            }
            break;

            default:
                break;
        }

        if (mCurrentState != REFRESHING && isHeaderShowing() && actionMasked != MotionEvent.ACTION_UP && actionMasked != MotionEvent.ACTION_POINTER_UP) {
            // 不是在刷新的时候，并且头部在显示， 某些情况下不让contentView响应事件
            event.setAction(MotionEvent.ACTION_CANCEL);
        }

        return super.dispatchTouchEvent(event);
    }

    /**
     * 拉动头部
     *
     * @param diff 拉动距离
     */
    private void scrollHeader(float diff) {
        // 除以3相当于阻尼值
        diff /= 3;
        // 计算出移动后的头部位置
        int top = (int) (diff + mHeader.getPaddingTop());
        // 控制头部位置最小不超过-mHeaderHeight，最大不超过mHeaderHeight * 4
        mHeader.setPadding(0, Math.min(Math.max(top, -mHeaderHeight), mHeaderHeight * 3), 0, 0);
        if (mCurrentState == REFRESHING) {
            // 之前还在刷新状态，继续维持刷新状态
            mHeader.setText("正在刷新...");
            return;
        }
        if (mHeader.getPaddingTop() > mHeaderHeight / 2) {
            // 大于mHeaderHeight / 2时可以刷新了
            mHeader.setText("可以释放刷新...");
            mCurrentState = RELEASE_TO_REFRESH;
        } else {
            // 下拉状态
            mHeader.setText("正在下拉...");
            mCurrentState = PULL_TO_REFRESH;
        }
    }

    /**
     * 执行头部显示或隐藏滑动
     */
    private void autoScrollHeader() {
        // 处理抬起事件
        if (mCurrentState == RELEASE_TO_REFRESH) {
            // 释放刷新状态，手指抬起，通过动画实现头部回到（0，0）位置
            mHeaderAnimator.setIntValues(mHeader.getPaddingTop(), 0);
            mHeaderAnimator.setDuration(DEFAULT_DURATION);
            mHeaderAnimator.start();
            mHeader.setText("正在释放...");
        } else if (mCurrentState == PULL_TO_REFRESH || mCurrentState == REFRESHING) {
            // 下拉状态或者正在刷新状态，通过动画隐藏头部
            mHeaderAnimator.setIntValues(mHeader.getPaddingTop(), -mHeaderHeight);
            if (mHeader.getPaddingTop() <= 0) {
                mHeaderAnimator.setDuration((long) (DEFAULT_DURATION * 1.0 / mHeaderHeight * (mHeader.getPaddingTop() + mHeaderHeight)));
            } else {
                mHeaderAnimator.setDuration(DEFAULT_DURATION);
            }
            mHeaderAnimator.start();
            if (mCurrentState == PULL_TO_REFRESH) {
                // 下拉状态的话，把状态改为正在隐藏头部状态
                mCurrentState = HIDING;
                mHeader.setText("收回头部...");
            }
        }
    }


    // 头部是否在显示，通过PaddingTop即可知道
    private boolean isHeaderShowing() {
        return mHeader.getPaddingTop() > -mHeaderHeight;
    }

    public interface OnRefreshListener {

        void onRefresh();

    }

    public void setOnRefreshListener(OnRefreshListener onRefreshListener) {
        mOnRefreshListener = onRefreshListener;
    }

    public void setContentViewOffset(int offset) {
        mContentViewOffset = offset;
    }

    public void setRefreshable(boolean refreshable) {
        mIsRefreshable = refreshable;
    }

    public boolean isRefreshing() {
        return mCurrentState == REFRESHING;
    }

    public void setRefreshing(boolean refreshing) {
        if (refreshing && mCurrentState != REFRESHING) {
            // 强开刷新头部
            openHeader();
        } else if (!refreshing) {
            closeHeader();
        }
    }

    private void openHeader() {
        post(new Runnable() {
            @Override
            public void run() {
                mCurrentState = RELEASE_TO_REFRESH;
                mHeaderAnimator.setDuration((long) (DEFAULT_DURATION * 2.5));
                mHeaderAnimator.setIntValues(mHeader.getPaddingTop(), 0);
                mHeaderAnimator.start();
            }
        });
    }

    private void closeHeader() {
        mHeader.setText("刷新完毕，收回头部...");
        mCurrentState = HIDING;
        mHeaderAnimator.setIntValues(mHeader.getPaddingTop(), -mHeaderHeight);
        // 0~-mHeaderHeight用时DEFAULT_DURATION
        mHeaderAnimator.setDuration(DEFAULT_DURATION);
        mHeaderAnimator.start();
    }

    /**
     * 根据不同类型的View采取不同类型策略去计算滑动距离
     *
     * @param view 内容View
     */
    public void handleTargetOffset(View view) {
        if (view instanceof RecyclerView) {
            ((RecyclerView) view).addOnScrollListener(new RecyclerViewOnScrollListener());
        } else if (view instanceof NestedScrollView) {
            ((NestedScrollView) view).setOnScrollChangeListener(new NestedScrollViewOnScrollChangeListener());
        } else if (view instanceof WebView) {
            view.setOnTouchListener(new WebViewOnTouchListener());
        } else if (view instanceof ScrollView) {
            view.setOnTouchListener(new ScrollViewOnTouchListener());
        } else if (view instanceof ListView) {
            ((ListView) view).setOnScrollListener(new ListViewOnScrollListener());
        }
    }

    /**
     * 适用于RecyclerView的滑动距离监听
     */
    public class RecyclerViewOnScrollListener extends RecyclerView.OnScrollListener {

        int offset = 0;

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            offset += dy;
            setContentViewOffset(offset);
        }

    }

    /**
     * 适用于NestedScrollView的滑动距离监听
     */
    public class NestedScrollViewOnScrollChangeListener implements NestedScrollView.OnScrollChangeListener {

        @Override
        public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
            setContentViewOffset(scrollY);
        }
    }

    /**
     * 适用于WebView的滑动距离监听
     */
    public class WebViewOnTouchListener implements View.OnTouchListener {

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            setContentViewOffset(view.getScrollY());
            return false;
        }

    }

    /**
     * 适用于ScrollView的滑动距离监听
     */
    public class ScrollViewOnTouchListener extends WebViewOnTouchListener {

    }

    /**
     * 适用于ListView的滑动距离监听
     */
    public class ListViewOnScrollListener implements AbsListView.OnScrollListener {

        @Override
        public void onScrollStateChanged(AbsListView absListView, int i) {

        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            if (firstVisibleItem == 0) {
                View c = view.getChildAt(0);
                if (c == null) {
                    return;
                }
                int firstVisiblePosition = view.getFirstVisiblePosition();
                int top = c.getTop();
                int scrolledY = -top + firstVisiblePosition * c.getHeight();
                setContentViewOffset(scrolledY);
            } else {
                setContentViewOffset(1);
            }
        }

    }

    /**
     * 告知View是否可滑动，这个View对view instanceof ScrollingView || view instanceof WebView || view instanceof ScrollView || view instanceof AbsListView做了判断，
     * 如果是其他可滚动的比如自定义的滚动，需要调这个方法告知
     *
     * @param contentViewScrollable true为可滑动
     */
    public void notifyContentViewScrollable(boolean contentViewScrollable) {
        mContentViewScrollable = contentViewScrollable;
    }

}
