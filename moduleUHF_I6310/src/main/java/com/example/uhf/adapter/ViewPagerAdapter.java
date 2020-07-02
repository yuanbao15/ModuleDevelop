package com.example.uhf.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;


import java.util.ArrayList;
import java.util.List;

import com.example.uhf.fragment.KeyDwonFragment;

/**
 * Created by Administrator on 2015-03-10.
 */
public class ViewPagerAdapter extends FragmentPagerAdapter {

    private List<KeyDwonFragment> lstFrg = new ArrayList<KeyDwonFragment>();
    private List<String> lstTitles = new ArrayList<String>();

    public ViewPagerAdapter(FragmentManager fm, List<KeyDwonFragment> fragments, List<String> titles) {
        super(fm);

        lstFrg = fragments;
        lstTitles = titles;
    }


    @Override
    public Fragment getItem(int position) {
        if (lstFrg.size() > 0) {
            return lstFrg.get(position);
        }
        throw new IllegalStateException("No fragment at position " + position);
    }

    @Override
    public int getCount() {
        return lstFrg.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        if (lstTitles.size() > 0) {
            return lstTitles.get(position);
        }
        return null;
    }
}
