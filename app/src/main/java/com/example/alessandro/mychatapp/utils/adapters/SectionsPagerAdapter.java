package com.example.alessandro.mychatapp.utils.adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.example.alessandro.mychatapp.R;
import com.example.alessandro.mychatapp.fragments.ChatsFragment;
import com.example.alessandro.mychatapp.fragments.FriendsFragment;
import com.example.alessandro.mychatapp.fragments.RequestsFragment;

public class SectionsPagerAdapter extends FragmentPagerAdapter {

    private Context context;

    public SectionsPagerAdapter(FragmentManager fm, Context ctx) {
        super(fm);
        context = ctx;
    }

    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case 0:
                return new RequestsFragment();

            case 1:
                return new ChatsFragment();

            case 2:
                return new FriendsFragment();

            default:
                return null;
        }

    }

    @Override
    public int getCount() {
        return 3;
    }

    public CharSequence getPageTitle(int position) {

        switch (position) {
            case 0:
                return context.getString(R.string.requests_tab_title);

            case 1:
                return context.getString(R.string.chats_tab_title);

            case 2:
                return context.getString(R.string.friends_tab_title);

            default:
                return null;
        }

    }

}