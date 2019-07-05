package me.wowtao.pottery.activity;
import me.wowtao.pottery.R;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import com.astuetz.PagerSlidingTabStrip;
import me.wowtao.pottery.fragment.CollectFragment;
import me.wowtao.pottery.fragment.OrderFragment;

import android.os.Bundle;

public class CollectAndOrderActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collect_or_order);
        // Initialize the ViewPager and set an adapter
        ViewPager pager = (ViewPager) findViewById(R.id.pager);
        pager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {
            CollectFragment cf = new CollectFragment();
            OrderFragment of = new OrderFragment();

            @Override
            public Fragment getItem(int position) {
                if (position == 0) {
                    return cf;
                } else {
                    return of;
                }
            }

            @Override
            public int getCount() {
                return 2;
            }

            @Override
            public CharSequence getPageTitle(int position) {
                if (position == 0) {
                    return "我的收藏";
                } else {
                    return "我的订单";
                }
            }
        });

        // Bind the tabs to the ViewPager
        PagerSlidingTabStrip tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
        tabs.setViewPager(pager);
    }


}