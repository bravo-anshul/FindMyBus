package com.example.bravo.findmybus;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class location_fragment extends Fragment {

    public static final String TAG = "FindMyBus";
    public boolean locationBoolean ;
    private SharedPreferences sharedpref;
    private SharedPreferences.Editor editor;
    private Intent intent;
    private Button b1;
    private Button b2;
    private ImageView maps_img;
    private ImageView pin_img;
    private ImageView timer_img;
    int mapres;
    int pinres;
    int mapres_error;
    int pinres_error;

    AlarmManager am;
    PendingIntent timer_pi;
    Intent timer_intent;

    DatabaseReference database;
    FirebaseAuth auth;
    View view1;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.location_fragment,container,false);
        view1 = view;
        b1 = view.findViewById(R.id.location);
        b2 = view.findViewById(R.id.userSetting);
        maps_img = view.findViewById(R.id.maps_img);
        pin_img = view.findViewById(R.id.pin_img);
        timer_img = view.findViewById(R.id.timer_img);

        am = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
        timer_intent = new Intent(getActivity(),timer_service.class);
        timer_pi = PendingIntent.getService(getContext(),1 , timer_intent,0);

        mapres = getContext().getResources().getIdentifier("google_maps_low", "drawable", getContext().getPackageName());
        pinres = getContext().getResources().getIdentifier("location_blue", "drawable", getContext().getPackageName());
        mapres_error = getContext().getResources().getIdentifier("maps_bnw_low", "drawable", getContext().getPackageName());
        pinres_error = getContext().getResources().getIdentifier("location_error", "drawable", getContext().getPackageName());
        sharedpref = this.getActivity().getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        locationBoolean = sharedpref.getBoolean("locationBoolean",false);
        if(locationBoolean) {
            maps_img.setImageResource(mapres);
            pin_img.setImageResource(pinres);
            timer_img.setVisibility(View.VISIBLE);
            b1.setText("STOP LOCATION");
        }
        else {
            maps_img.setImageResource(mapres_error);
            pin_img.setImageResource(pinres_error);
            timer_img.setVisibility(View.GONE);
            b1.setText("SHARE LOCATION");
        }

        intent = new Intent(getActivity(),location_service.class);

        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG,"button listener called");
                sendLocation();
            }
        });
        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                auth = FirebaseAuth.getInstance();
                Intent intent = new Intent(getActivity(),settings_activity.class);
                startActivity(intent);
            }
        });
        Log.i(TAG,"location FRAGMENT MADE");
        return view;
    }

    public void sendLocation(){
        Log.i(TAG,"GET LOCATION CALLED");
        locationBoolean = sharedpref.getBoolean("locationBoolean",false);
        editor = sharedpref.edit();
        if(!locationBoolean) {
            Log.i(TAG,"Location sharing");
            b1.setText("Stop Location");
            editor.putBoolean("locationBoolean",true);
            editor.apply();
            maps_img.setImageResource(mapres);
            pin_img.setImageResource(pinres);
            intent.putExtra("key",true);
            getActivity().startService(intent);
            startTimer();
            startLocation();
        }
        else{
            Log.i(TAG,"Location sharing stopped");
            Toast.makeText(getContext(), "Location sharing stopped.", Toast.LENGTH_SHORT).show();
            b1.setText("Share Location");
            editor.putBoolean("locationBoolean",false);
            editor.apply();
            maps_img.setImageResource(mapres_error);
            pin_img.setImageResource(pinres_error);
            intent.putExtra("key",false);
            getActivity().startService(intent);
            stopTimer();
            stopLocation();
        }
    }


    public void stopLocation(){
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String uid = currentUser.getUid();

        database = FirebaseDatabase.getInstance().getReference().child("users").child(uid).child("location");
        database.setValue("0");
    }
    public void startLocation(){
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String uid = currentUser.getUid();

        database = FirebaseDatabase.getInstance().getReference().child("users").child(uid).child("location");
        database.setValue("1");
    }

    public void startTimer(){
        int hour_time = sharedpref.getInt("hour_time",1);
        int minute_time = sharedpref.getInt("minute_time",30);
        Toast.makeText(getActivity(),"Timer set for "+hour_time+" hour and "+minute_time+" minutes.",Toast.LENGTH_SHORT).show();
        int total_time = (hour_time*3600+minute_time*60)*1000;

        timer_img.setVisibility(View.VISIBLE);
        am.set(AlarmManager.RTC_WAKEUP,System.currentTimeMillis()+(total_time) , timer_pi);
    }
    public void stopTimer(){
        timer_img.setVisibility(View.GONE);
        am.cancel(timer_pi);
    }

}
