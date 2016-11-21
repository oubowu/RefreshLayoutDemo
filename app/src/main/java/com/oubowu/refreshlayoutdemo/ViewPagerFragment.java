package com.oubowu.refreshlayoutdemo;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.oubowu.refreshlayoutdemo.refresh.RefreshLayout;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ViewPagerFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ViewPagerFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private ViewPager mView;
    private RefreshLayout mRefreshLayout;


    public ViewPagerFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ViewPagerFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ViewPagerFragment newInstance(String param1, String param2) {
        ViewPagerFragment fragment = new ViewPagerFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_view_pager, container, false);

        mRefreshLayout = (RefreshLayout) view.findViewById(R.id.refresh_layout);
        mRefreshLayout.setRefreshing(true);
        mRefreshLayout.setOnRefreshListener(new RefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.e("MainActivity", "70行-onRefresh(): " + "正在刷新");
                mRefreshLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Log.e("MainActivity", "63行-run(): " + "刷新完毕");
                        Toast.makeText(getActivity(), "刷新完毕", Toast.LENGTH_SHORT).show();
                        mRefreshLayout.setRefreshing(false);
                    }
                }, 2000);
            }
        });

        mView = (ViewPager) view.findViewById(R.id.view);

        ArrayList<Integer> list = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            list.add(i);
        }

        mView.setAdapter(new VpAdapter<>(list));

        // 处理与RefreshLayout的事件冲突
        mView.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                Log.e("ViewPagerFragment","106行-onPageScrolled(): "+" ");
                mRefreshLayout.setRefreshable(positionOffset==0);
            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if (state==ViewPager.SCROLL_STATE_IDLE){
                    mRefreshLayout.setRefreshable(true);
                }else {
                    mRefreshLayout.setRefreshable(false);
                }
            }
        });

        return view;
    }

    class VpAdapter<T> extends PagerAdapter {

        private List<T> mData;

        public VpAdapter(List<T> data) {
            this.mData = data;
        }

        @Override
        public int getCount() {
            return mData == null ? 0 : mData.size();
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {

            final View view = LayoutInflater.from(container.getContext()).inflate(R.layout.item_vp, container, false);

            TextView tvPage = (TextView) view.findViewById(R.id.tv_page);

            tvPage.setText("页面" + (position + 1));

            container.addView(view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

    }

}
