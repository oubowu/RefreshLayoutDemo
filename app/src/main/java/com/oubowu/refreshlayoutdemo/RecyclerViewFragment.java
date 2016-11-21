package com.oubowu.refreshlayoutdemo;


import android.graphics.Rect;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.oubowu.refreshlayoutdemo.base.BaseRecyclerAdapter;
import com.oubowu.refreshlayoutdemo.base.BaseRecyclerViewHolder;
import com.oubowu.refreshlayoutdemo.callback.OnItemClickAdapter;
import com.oubowu.refreshlayoutdemo.refresh.RefreshLayout;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RecyclerViewFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RecyclerViewFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private RefreshLayout mRefreshLayout;
    private RecyclerView mRecyclerView;

    private List<String> list;


    public RecyclerViewFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment RecyclerViewFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static RecyclerViewFragment newInstance(String param1, String param2) {
        RecyclerViewFragment fragment = new RecyclerViewFragment();
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
        View view = inflater.inflate(R.layout.fragment_recycler_view, container, false);

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
                        if (getActivity() != null) {
                            Toast.makeText(getActivity(), "刷新完毕", Toast.LENGTH_SHORT).show();
                        }
                        mRefreshLayout.setRefreshing(false);
                    }
                }, 2000);
            }
        });

        mRecyclerView = (RecyclerView) view.findViewById(R.id.view);

        list = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            list.add("位置" + i);
        }

        BaseRecyclerAdapter<String> adapter = new BaseRecyclerAdapter<String>(getActivity(), list) {
            @Override
            public int getItemLayoutId(int viewType) {
                return R.layout.item_msg;
            }

            @Override
            public void bindData(BaseRecyclerViewHolder holder, int position, String item) {
                holder.setText(R.id.tv_failed, item);
            }
        };

        adapter.setOnItemClickListener(new OnItemClickAdapter() {
            @Override
            public void onItemClick(View view, int position) {
                Toast.makeText(getActivity(), "点击了：" + list.get(mRecyclerView.getChildLayoutPosition(view)), Toast.LENGTH_SHORT).show();
            }
        });

        mRecyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                if (parent.getChildLayoutPosition(view) != 0) {
                    outRect.top = 3;
                }
            }
        });
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        mRecyclerView.setAdapter(adapter);

        //        mRecyclerView.addOnScrollListener(mRefreshLayout.new RecyclerViewOnScrollListener());
        // 不需要处理其他的滑动事件的话
        mRefreshLayout.handleTargetOffset(mRecyclerView);

        AppBarLayout appBarLayout = (AppBarLayout) getActivity().findViewById(Integer.parseInt(mParam1));
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                mRefreshLayout.setRefreshable(verticalOffset == 0);
            }
        });

        return view;
    }

}
