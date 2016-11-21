package com.oubowu.refreshlayoutdemo;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {


    private DisscrollViewFragment mDisscrollViewFragment;
    private ListViewFragment mListViewFragment;
    private NestedScrollViewFragment mNestedScrollViewFragment;
    private RecyclerViewFragment mRecyclerViewFragment;
    private ScrollViewFragment mScrollViewFragment;
    private WebViewFragment mWebViewFragment;
    private ViewPagerFragment mViewPagerFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mDisscrollViewFragment = DisscrollViewFragment.newInstance("", "");
        mListViewFragment = ListViewFragment.newInstance("", "");
        mNestedScrollViewFragment = NestedScrollViewFragment.newInstance(R.id.appbar + "", "");
        mRecyclerViewFragment = RecyclerViewFragment.newInstance(R.id.appbar + "", "");
        mScrollViewFragment = ScrollViewFragment.newInstance("", "");
        mWebViewFragment = WebViewFragment.newInstance("", "");
        mViewPagerFragment = ViewPagerFragment.newInstance("", "");

        changeFragment(mDisscrollViewFragment, "不能滑动View");

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        String title = item.getTitle().toString();
        item.setChecked(true);
        switch (id) {
            case R.id.action_disscroll:
                changeFragment(mDisscrollViewFragment, title);
                break;
            case R.id.action_listview:
                changeFragment(mListViewFragment, title);
                break;
            case R.id.action_nested_scrollview:
                changeFragment(mNestedScrollViewFragment, title);
                break;
            case R.id.action_recyclerview:
                changeFragment(mRecyclerViewFragment, title);
                break;
            case R.id.action_scrollview:
                changeFragment(mScrollViewFragment, title);
                break;
            case R.id.action_webview:
                changeFragment(mWebViewFragment, title);
                break;
            case R.id.action_view_pager:
                changeFragment(mViewPagerFragment, title);
                break;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void changeFragment(Fragment fragment, String title) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.activity_refresh, fragment);
        transaction.commit();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }

    }
}
