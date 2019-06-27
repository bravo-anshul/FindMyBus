package com.example.bravo.findmybus;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class settings_activity extends AppCompatActivity {

    public static final String TAG = "FindMyBus";
    EditText uname_edit;
    EditText bus_edit;
    EditText hour_input;
    EditText minutes_input;
    ProgressDialog progress;
    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;

    FirebaseAuth auth;
    DatabaseReference database;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_activity);

        progress=new ProgressDialog(this);
        progress.setTitle("Please wait");
        progress.setMessage("Updating , this could take while...");
        progress.setCancelable(false);

        uname_edit = (EditText) findViewById(R.id.uname_edit);
        bus_edit = (EditText) findViewById(R.id.bus_edit);
        hour_input = (EditText) findViewById(R.id.hour_text);
        minutes_input = (EditText) findViewById(R.id.minute_text);

        sharedPref = getSharedPreferences("userInfo",Context.MODE_PRIVATE);
        editor = sharedPref.edit();

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance().getReference();

    }

    public void apply(View view){
        progress.show();
        final String uname = uname_edit.getText().toString();
        final String bus = bus_edit.getText().toString();
        int hour;
        int minute;
        try{
            hour = Integer.parseInt(hour_input.getText().toString()) ;
        }catch (Exception e){
             hour = 0;
        }
        try {
            minute = Integer.parseInt(minutes_input.getText().toString()) ;
        }catch (Exception e){
            minute = 0;
        }


        if(!Objects.equals(uname, "")||!Objects.equals(bus, "")) {
            Query uname_check = database.child("users").orderByChild("username").equalTo(uname);
            uname_check.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getChildrenCount() > 0) {
                        progress.dismiss();
                        Toast.makeText(getApplication(), "Username must be unique.", Toast.LENGTH_SHORT).show();
                    } else {
                        server(uname, bus);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        if(hour!=0 || minute!=0){
            if(hour>3 && minute>60){
                Toast.makeText(this,"Hours cannot be greater than 3",Toast.LENGTH_SHORT).show();
                if(minute>60){
                    Toast.makeText(this,"Minutes cannot be greater than 60",Toast.LENGTH_SHORT).show();
                }
            }
            else{
                if(hour!=0)
                    editor.putInt("hour_time",hour);
                else
                    editor.putInt("hour_time",0);
                if(minute!=0)
                    editor.putInt("minute_time",minute);
                else
                    editor.putInt("minute_time",0);

                editor.apply();
            }
        }
        progress.dismiss();
        hour_input.setText("");
        minutes_input.setText("");
        uname_edit.setText("");
        bus_edit.setText("");
        Toast.makeText(this,"Changes Applied.",Toast.LENGTH_SHORT).show();
    }

    private void server(String uname, String bus) {
        String curr_user = auth.getCurrentUser().getUid();

        if(!Objects.equals(uname, "")) {
            database = FirebaseDatabase.getInstance().getReference().child("users").child(curr_user).child("username");
            database.setValue(uname);
        }
        if(!Objects.equals(bus, "")) {
            database = FirebaseDatabase.getInstance().getReference().child("users").child(curr_user).child("bus");
            database.setValue(bus);
        }
        progress.dismiss();
    }

    public void logout(View view){
        Intent intent = new Intent(getApplication(),location_service.class);
        intent.putExtra("key",false);
        this.startService(intent);
        SharedPreferences sharedPref = getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("locationBoolean",false);
        editor.apply();

        stopLocation();
        auth.signOut();
        Intent i = new Intent(this,login_activity.class);
        startActivity(i);
    }

    public void stopLocation(){
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String uid = currentUser.getUid();

        DatabaseReference database = FirebaseDatabase.getInstance().getReference().child("users").child(uid).child("location");
        database.setValue("0");
    }


}
