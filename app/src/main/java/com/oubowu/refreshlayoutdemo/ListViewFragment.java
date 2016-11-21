package com.oubowu.refreshlayoutdemo;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.oubowu.refreshlayoutdemo.refresh.RefreshLayout;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ListViewFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ListViewFragment extends Fragment implements View.OnClickListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private RefreshLayout mRefreshLayout;
    private ListView mListView;
    private LayoutInflater mInflater;


    public ListViewFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ListViewFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ListViewFragment newInstance(String param1, String param2) {
        ListViewFragment fragment = new ListViewFragment();
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
        View view = inflater.inflate(R.layout.fragment_list_view, container, false);

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

        final List<String> list = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            list.add("位置" + i);
        }

        mListView = (ListView) view.findViewById(R.id.view);
        mInflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mListView.setAdapter(new BaseAdapter() {

            @Override
            public int getCount() {
                return list.size();
            }

            @Override
            public Object getItem(int i) {
                return list.get(i);
            }

            @Override
            public long getItemId(int i) {
                return i;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                ViewHolder holder;
                if (convertView == null) {
                    convertView = mInflater.inflate(R.layout.item_msg, parent, false);
                    holder = new ViewHolder();
                    holder.textView = (TextView) convertView.findViewById(R.id.tv_failed);
                    convertView.setTag(holder);
                } else {
                    holder = (ViewHolder) convertView.getTag();
                }
                convertView.setOnClickListener(ListViewFragment.this);
                convertView.setTag(R.id.id, list.get(position));
                holder.textView.setText(list.get(position));
                return convertView;
            }

            class ViewHolder {
                TextView textView;
            }
        });

        // mListView.setOnScrollListener(mRefreshLayout.new ListViewOnScrollListener());
        // 不需要处理其他的滑动事件的话
        mRefreshLayout.handleTargetOffset(mListView);

        return view;
    }

    @Override
    public void onClick(View view) {
        if (getActivity() != null) {
            Toast.makeText(getActivity(), "点击了：" + view.getTag(R.id.id), Toast.LENGTH_SHORT).show();
        }
    }
}
