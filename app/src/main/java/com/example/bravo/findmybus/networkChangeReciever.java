package com.example.bravo.findmybus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class networkChangeReciever extends BroadcastReceiver {

    DatabaseReference database;
    SharedPreferences sharedpref;

    @Override
    public void onReceive(final Context context, final Intent intent) {
        final ConnectivityManager connMgr = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        final android.net.NetworkInfo wifi = connMgr
                .getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        final android.net.NetworkInfo mobile = connMgr
                .getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        if (wifi.isConnected() || mobile.isAvailable()) {

            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            String uid = "null";
            sharedpref = context.getSharedPreferences("userInfo", Context.MODE_PRIVATE);
            boolean locationBoolean = sharedpref.getBoolean("locationBoolean",false);
            if(!locationBoolean) {
                if (currentUser != null) {
                    uid = currentUser.getUid();
                }

                if(!Objects.equals(uid, "null")) {
                    database = FirebaseDatabase.getInstance().getReference().child("users").child(uid).child("location");
                    database.setValue("0");
                }
            }

        }
    }
}
