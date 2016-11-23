package com.dalbers.dialer;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;

import com.dalbers.dialer.DataHandling.Contact;
import com.dalbers.dialer.DataHandling.Recent;

import java.util.ArrayList;
import java.util.List;

import static com.dalbers.dialer.DataHandling.GetData.getContacts;
import static com.dalbers.dialer.DataHandling.GetData.getRecents;


public class DialerActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private ArrayList<Contact> contacts;
    private ArrayList<Recent> recents;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialer);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        //load all the data here, pass it on to fragments later
        //load from a saved state, if there is one
        if(savedInstanceState != null) {
            Log.d("main","load saved");
            recents = savedInstanceState.getParcelableArrayList("recents");
            contacts = savedInstanceState.getParcelableArrayList("contacts");
        }
        else { //no saved state, get data from database
            contacts = getContacts(this);
            recents = getRecents(this, contacts, 7);
        }
        //bundle up data and create fragments with data
        Bundle recentsBundle = new Bundle();
        recentsBundle.putParcelableArrayList("recents",recents);
        RecentsFragment recentsFragment = new RecentsFragment();
        recentsFragment.setArguments(recentsBundle);

        Bundle contactsBundle = new Bundle();
        contactsBundle.putParcelableArrayList("contacts",contacts);
        contactsBundle.putParcelableArrayList("recents", recents);
        ContactsFragment contactsFragment = new ContactsFragment();
        contactsFragment.setArguments(contactsBundle);
        //add contacts and recents fragments to tabs
        adapter.addFragment(contactsFragment, "Contacts");
        adapter.addFragment(recentsFragment, "Recent");
        viewPager.setAdapter(adapter);
        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                //hide the keyboard
                InputMethodManager imm = (InputMethodManager)DialerActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(tabLayout.getWindowToken(), 0);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    /**
     * Handles displaying our fragments in the tab format
     */
    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putParcelableArrayList("contacts",contacts);
        savedInstanceState.putParcelableArrayList("recents", recents);

        super.onSaveInstanceState(savedInstanceState);
    }
}
