package com.example.bravo.findmybus;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class timer_service extends Service {
    public timer_service() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(getApplication(),"Location sharing stopped.",Toast.LENGTH_LONG).show();
        Intent i = new Intent(getApplication(),location_service.class);
        intent.putExtra("key",false);
        this.startService(i);
        SharedPreferences sharedPref = getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("locationBoolean",false);
        editor.apply();
        stopLocation();

        return START_NOT_STICKY;
    }

    public void stopLocation(){
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String uid = currentUser.getUid();

        DatabaseReference database = FirebaseDatabase.getInstance().getReference().child("users").child(uid).child("location");
        database.setValue("0");
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
