package com.naughtybitch.usthweather;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class HomeFragmentPagerAdapter extends FragmentPagerAdapter {
    private final int PAGE_COUNT = 3;
    private String[] titles = new String[]{"Hanoi", "Paris", "Toulouse"};

    protected HomeFragmentPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public int getCount() {
        return PAGE_COUNT; // number of pages for a ViewPager
    }

    @Override
    public Fragment getItem(int page) {
        // returns an instance of Fragment corresponding to the specified page
        switch (page) {
            case 0: // Fragment # 0 - This will show first fragment
                return WeatherAndForecastFragment.newInstance(0, "Page #1");
            case 1: // Fragment # 0 - This will show second fragment
                return WeatherAndForecastFragment.newInstance(1, "Page #2");
            case 2: // Fragment # 0 - This will show third fragment
                return WeatherAndForecastFragment.newInstance(2, "Page #3");
            default:
                return null; // Fail-safe
        }
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return titles[position];
    }
}
