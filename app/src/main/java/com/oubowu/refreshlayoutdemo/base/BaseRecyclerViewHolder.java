package com.oubowu.refreshlayoutdemo.base;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.text.util.Linkify;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.Checkable;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;


/**
 * ClassName: BaseRecyclerViewHolder<p>
 * Author:oubowu<p>
 * Fuction: RecyclerView通用适配器Holder<p>
 * CreateDate:2016/2/16 23:00<p>
 * UpdateUser:<p>
 * UpdateDate:<p>
 */
public class BaseRecyclerViewHolder extends RecyclerView.ViewHolder {

    //集合类，layout里包含的View,以view的id作为key，value是view对象
    protected SparseArray<View> mViews;
    //上下文对象
    protected Context mContext;

    public BaseRecyclerViewHolder(Context context, View itemView) {
        super(itemView);
        mContext = context;
        mViews = new SparseArray<View>();
    }

    @SuppressWarnings("unchecked")
    private <T extends View> T findViewById(int viewId) {
        View view = mViews.get(viewId);
        if (view == null) {
            view = itemView.findViewById(viewId);
            mViews.put(viewId, view);
        }
        return (T) view;
    }

    public View getView(int viewId) {
        return findViewById(viewId);
    }

    public TextView getTextView(int viewId) {
        return (TextView) getView(viewId);
    }

    public Button getButton(int viewId) {
        return (Button) getView(viewId);
    }

    public ImageView getImageView(int viewId) {
        return (ImageView) getView(viewId);
    }

    public ImageButton getImageButton(int viewId) {
        return (ImageButton) getView(viewId);
    }

    public EditText getEditText(int viewId) {
        return (EditText) getView(viewId);
    }


    public BaseRecyclerViewHolder setBackground(int viewId, int resId) {
        View view = findViewById(viewId);
        view.setBackgroundResource(resId);
        return this;
    }

    /****以下为辅助方法*****/

    public BaseRecyclerViewHolder setText(int viewId, String text) {
        TextView tv = findViewById(viewId);
        tv.setText(text);
        return this;
    }

    public BaseRecyclerViewHolder setText(int viewId, CharSequence text) {
        TextView tv = findViewById(viewId);
        tv.setText(text);
        return this;
    }

    public BaseRecyclerViewHolder setImageResource(int viewId, int resId) {
        ImageView view = findViewById(viewId);
        view.setImageResource(resId);
        return this;
    }

    public BaseRecyclerViewHolder setImageBitmap(int viewId, Bitmap bitmap) {
        ImageView view = findViewById(viewId);
        view.setImageBitmap(bitmap);
        return this;
    }

    public BaseRecyclerViewHolder setImageDrawable(int viewId, Drawable drawable) {
        ImageView view = findViewById(viewId);
        view.setImageDrawable(drawable);
        return this;
    }

    public BaseRecyclerViewHolder setBackgroundColor(int viewId, int color) {
        View view = findViewById(viewId);
        view.setBackgroundColor(color);
        return this;
    }

    public BaseRecyclerViewHolder setBackgroundRes(int viewId, int backgroundRes) {
        View view = findViewById(viewId);
        view.setBackgroundResource(backgroundRes);
        return this;
    }

    public BaseRecyclerViewHolder setTextColor(int viewId, int textColor) {
        TextView view = findViewById(viewId);
        view.setTextColor(textColor);
        return this;
    }

    public BaseRecyclerViewHolder setTextColorRes(int viewId, int textColorRes) {
        TextView view = findViewById(viewId);
        view.setTextColor(ContextCompat.getColor(mContext, textColorRes));
        return this;
    }

    public BaseRecyclerViewHolder setAlpha(int viewId, float value) {
        ViewCompat.setAlpha(findViewById(viewId), value);
        return this;
    }

    public BaseRecyclerViewHolder setViewGone(int viewId, boolean gone) {
        View view = findViewById(viewId);
        view.setVisibility(gone ? View.GONE : View.VISIBLE);
        return this;
    }

    public BaseRecyclerViewHolder setViewInvisible(int viewId, boolean invisible) {
        View view = findViewById(viewId);
        view.setVisibility(invisible ? View.INVISIBLE : View.VISIBLE);
        return this;
    }

    public BaseRecyclerViewHolder linkify(int viewId) {
        TextView view = findViewById(viewId);
        Linkify.addLinks(view, Linkify.ALL);
        return this;
    }

    public BaseRecyclerViewHolder setTypeface(Typeface typeface, int... viewIds) {
        for (int viewId : viewIds) {
            TextView view = findViewById(viewId);
            view.setTypeface(typeface);
            view.setPaintFlags(view.getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG);
        }
        return this;
    }

    public BaseRecyclerViewHolder setProgress(int viewId, int progress) {
        ProgressBar view = findViewById(viewId);
        view.setProgress(progress);
        return this;
    }

    public BaseRecyclerViewHolder setProgress(int viewId, int progress, int max) {
        ProgressBar view = findViewById(viewId);
        view.setMax(max);
        view.setProgress(progress);
        return this;
    }

    public BaseRecyclerViewHolder setMax(int viewId, int max) {
        ProgressBar view = findViewById(viewId);
        view.setMax(max);
        return this;
    }

    public BaseRecyclerViewHolder setRating(int viewId, float rating) {
        RatingBar view = findViewById(viewId);
        view.setRating(rating);
        return this;
    }

    public BaseRecyclerViewHolder setRating(int viewId, float rating, int max) {
        RatingBar view = findViewById(viewId);
        view.setMax(max);
        view.setRating(rating);
        return this;
    }

    public BaseRecyclerViewHolder setTag(int viewId, Object tag) {
        View view = findViewById(viewId);
        view.setTag(tag);
        return this;
    }

    public BaseRecyclerViewHolder setTag(int viewId, int key, Object tag) {
        View view = findViewById(viewId);
        view.setTag(key, tag);
        return this;
    }

    public BaseRecyclerViewHolder setChecked(int viewId, boolean checked) {
        Checkable view = (Checkable) findViewById(viewId);
        view.setChecked(checked);
        return this;
    }

    /**
     * 关于事件的
     */
    public BaseRecyclerViewHolder setOnClickListener(int viewId, View.OnClickListener listener) {
        View view = findViewById(viewId);
        view.setOnClickListener(listener);
        return this;
    }

    public BaseRecyclerViewHolder setOnTouchListener(int viewId, View.OnTouchListener listener) {
        View view = findViewById(viewId);
        view.setOnTouchListener(listener);
        return this;
    }

    public BaseRecyclerViewHolder setOnLongClickListener(int viewId, View.OnLongClickListener listener) {
        View view = findViewById(viewId);
        view.setOnLongClickListener(listener);
        return this;
    }

}
