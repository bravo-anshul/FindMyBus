package com.example.bravo.findmybus;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

public class splash_screen extends AppCompatActivity {

    private AlertDialog.Builder alertBuilder;
    FirebaseAuth auth;
    public static final String TAG = "FindMyBus";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        auth = FirebaseAuth.getInstance();
        alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setTitle("GPS Turned off");
        alertBuilder.setMessage("GPS is turned off. You would like to turn it on.");
        alertBuilder.setPositiveButton("yes sure!", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent =  new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        });
        alertBuilder.setNegativeButton("No.", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Toast.makeText(getApplicationContext(),"App won't work without GPS",Toast.LENGTH_LONG).show();
                finish();
            }
        });
        chechkGps();

    }


    private void chechkGps() {
        final Handler handler = new Handler();
        Intent intent;
        final LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );

        if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            alertBuilder.show();
        }
        else {
            if(auth.getCurrentUser()!=null){
                Toast.makeText(getApplicationContext(),"You are logged in as: "+auth.getCurrentUser().getEmail(), Toast.LENGTH_SHORT).show();
                intent = new Intent(getApplication(),main_menu.class);
            }
            else{
                intent = new Intent(getApplication(),login_activity.class);
            }
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            /*handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(getApplication(),login_activity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                }
            }, 0);
            */
        }
    }

    @Override
    protected void onResume() {
        chechkGps();
        super.onResume();
    }
}
