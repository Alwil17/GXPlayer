package com.grey.gxplayer.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.tabs.TabLayout;
import com.grey.gxplayer.R;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class MainFragment extends Fragment {


    private Toolbar toolBar;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        toolBar = rootView.findViewById(R.id.toolbar);
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolBar);
        tabLayout = rootView.findViewById(R.id.tabs);
        viewPager = rootView.findViewById(R.id.viewPager);
        setUpViewPager(viewPager);
        tabLayout.setupWithViewPager(viewPager);

        return rootView;
    }

    private void setUpViewPager (ViewPager viewPager){
        FragmentAdapter adapter = new FragmentAdapter(getActivity().getSupportFragmentManager());
        adapter.AddFragment(new SongsFragment(), "Songs");
        adapter.AddFragment(new AlbumsFragment(), "Albums");
        adapter.AddFragment(new ArtistsFragment(), "Artists");
        viewPager.setAdapter(adapter);

    }

    private class FragmentAdapter extends FragmentPagerAdapter {

        private List<Fragment> fragmentList = new ArrayList<>();
        private List<String> titleList = new ArrayList<>();

        public FragmentAdapter(@NonNull FragmentManager fm) {
            super(fm);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return fragmentList.get(position);
        }

        @Override
        public int getCount() {
            return fragmentList.size();
        }

        public void AddFragment(Fragment fragment, String title) {
            fragmentList.add(fragment);
            titleList.add(title);
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return titleList.get(position);
        }
    }
}
