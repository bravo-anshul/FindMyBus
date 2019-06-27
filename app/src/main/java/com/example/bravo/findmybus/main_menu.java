package com.example.bravo.findmybus;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class main_menu extends AppCompatActivity {

    public static final String TAG = "FindMyBus";
    public ViewPager mViewPager;
    public TabLayout tabLayout;
    private AlertDialog.Builder alertBuilder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setTitle("Update Available");
        alertBuilder.setMessage("New update is available. Please update your app.");
        alertBuilder.setPositiveButton("ok fine!", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finishAffinity();
            }
        });

        Query version = FirebaseDatabase.getInstance().getReference().child("version");
        version.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String curr_version = String.valueOf(dataSnapshot.child("current").getValue());
                if(!Objects.equals(curr_version, "version_1")){
                    alertBuilder.show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(new CustomAdapter(getSupportFragmentManager(),getApplicationContext()));

        tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(mViewPager);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener(){

            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mViewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                mViewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                mViewPager.setCurrentItem(tab.getPosition());
            }
        });
        tabLayout.getTabAt(0).setIcon(R.drawable.search);
        tabLayout.getTabAt(1).setIcon(R.drawable.location);
        tabLayout.getTabAt(2).setIcon(R.drawable.map);
        tabLayout.getTabAt(3).setIcon(R.drawable.direction_small);
        mViewPager.setOffscreenPageLimit(4);
        mViewPager.setCurrentItem(1);

    }


    private class CustomAdapter extends FragmentPagerAdapter {

        public CustomAdapter(FragmentManager supportFragmentManager, Context applicationContext) {
            super(supportFragmentManager);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position){
                case 0:
                    return new user_fragment();
                case 1:
                    return new location_fragment();
                case 2:
                    return new search_fragment();
                case 3:
                    return new direction_fragment();
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return 4;
        }

    }

    public void setView(int i, String uid, String uname){

        mViewPager.setCurrentItem(2);
        search_fragment searchob = (search_fragment) getSupportFragmentManager().getFragments().get(2);

        if(searchob!=null)
            searchob.addMarker(1,uid,uname);
        else{
            Toast.makeText(getApplication(),"Try restarting the app.",Toast.LENGTH_SHORT).show();
        }
    }

    public void setView(String driverKey,String driverBusNo) {
        mViewPager.setCurrentItem(2);
        search_fragment searchob = (search_fragment) getSupportFragmentManager().getFragments().get(2);

        if(searchob!=null)
            searchob.addMarker(driverKey,driverBusNo);
        else{
            Toast.makeText(getApplication(),"Try restarting the app.",Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAffinity();
    }
}
