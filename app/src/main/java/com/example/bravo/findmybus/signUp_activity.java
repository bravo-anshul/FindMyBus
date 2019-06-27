package com.example.bravo.findmybus;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;
import java.util.Objects;

public class signUp_activity extends AppCompatActivity {

    public static final String TAG = "FindMyBus";
    private DatabaseReference database;
    private FirebaseAuth auth;
    private ImageButton [] buttons = new ImageButton[6];
    private EditText email_input;
    private EditText uname_input;
    private EditText pass_input;
    private EditText bus_input;
    private int avatar=0;
    private ProgressDialog progress;

    Boolean emailBool=false;
    Boolean unameBool=false;
    private int flag=0 ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_activity);

        Log.i(TAG,"on create");

        database = FirebaseDatabase.getInstance().getReference();
        auth = FirebaseAuth.getInstance();

        email_input = (EditText) findViewById(R.id.login_email);
        uname_input = (EditText) findViewById(R.id.uname_input);
        pass_input = (EditText) findViewById(R.id.pass_input);
        bus_input = (EditText) findViewById(R.id.bus_input);

        final int ID = R.id.imageButton1;
        for(int x=0;x<6;x++){
            buttons[x] = (ImageButton) findViewById(ID+x);
        }

        progress=new ProgressDialog(this);
        progress.setTitle("Please wait");
        progress.setMessage("Registering , this could take while...");
        progress.setCancelable(false);
    }

    public void image_click(View view){
        switch (view.getId()){
            case R.id.imageButton1:
                clear_button();
                avatar=1;
                buttons[0].setBackground(getDrawable(R.drawable.square_border));
                break;
            case R.id.imageButton2:
                clear_button();
                avatar=2;
                buttons[1].setBackground(getDrawable(R.drawable.square_border));
                break;
            case R.id.imageButton3:
                clear_button();
                avatar=3;
                buttons[2].setBackground(getDrawable(R.drawable.square_border));
                break;
            case R.id.imageButton4:
                clear_button();
                avatar=4;
                buttons[3].setBackground(getDrawable(R.drawable.square_border));
                break;
            case R.id.imageButton5:
                clear_button();
                avatar=5;
                buttons[4].setBackground(getDrawable(R.drawable.square_border));
                break;
            case R.id.imageButton6:
                clear_button();
                avatar=6;
                buttons[5].setBackground(getDrawable(R.drawable.square_border));
                break;
        }
    }

    public void clear_button(){
        avatar=0;
        for(int x=0;x<6;x++){
            buttons[x].setBackgroundColor(Color.WHITE);
        }
    }

    public void create(View view){
        Log.i(TAG,"INSIDE CREATE");
        final String email = email_input.getText().toString();
        final String uname = uname_input.getText().toString();
        final String pass = pass_input.getText().toString();
        final String bus_no = bus_input.getText().toString();

        if(avatar==0|| Objects.equals(email, "") || Objects.equals(pass, "") || Objects.equals(uname, "") || Objects.equals(bus_no, "")){
            Toast.makeText(getApplication(),"Please Fill out all the details",Toast.LENGTH_SHORT).show();
        }
        else {
            progress.show();
            Query email_check = database.child("users").orderByChild("email").equalTo(email);
            email_check.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Log.i(TAG, "Cheacking email");
                    if (dataSnapshot.getChildrenCount() > 0) {
                        progress.dismiss();
                        Toast.makeText(getApplication(), "Email alerady registered", Toast.LENGTH_SHORT).show();
                    } else {
                        emailBool = true;
                        flag += 1;
                        ok(uname, email, pass,bus_no);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            Query uname_check = database.child("users").orderByChild("username").equalTo(uname);
            uname_check.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Log.i(TAG, "cheacking unmae");
                    if (dataSnapshot.getChildrenCount() > 0) {
                        progress.dismiss();
                        Toast.makeText(getApplication(), "Username must be unique.", Toast.LENGTH_SHORT).show();
                    } else {
                        unameBool = true;
                        flag += 1;
                        ok(uname, email, pass,bus_no);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

    }

    private void ok(String uname,String email,String pass,String bus_no) {
        if(flag==2) {
            flag=0;
            if (emailBool && unameBool) {
                emailBool = false;
                unameBool = false;
                Log.i(TAG, "Bollean true");
                createUser(uname, email, pass,bus_no);
            } else {
                Log.i(TAG, "Bollean false");
                emailBool = false;
                unameBool = false;
                progress.dismiss();
            }
        }
    }

    public void createUser(final String uname, final String email, final String pass,final String bus_no){
        Log.i(TAG,"user create");
        auth.createUserWithEmailAndPassword(email,pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    Log.i(TAG,"REgister SCucessfull");
                    FirebaseUser current_user = FirebaseAuth.getInstance().getCurrentUser();
                    String uid = current_user.getUid();
                    String curr_email = current_user.getEmail();
                    String token_id = FirebaseInstanceId.getInstance().getToken();

                    database = FirebaseDatabase.getInstance().getReference().child("users").child(uid);
                    HashMap<String,String> userMap = new HashMap<>();
                    userMap.put("username",uname);
                    userMap.put("email",curr_email);
                    userMap.put("password",pass);
                    userMap.put("avatar", String.valueOf(avatar));
                    userMap.put("bus",bus_no);
                    userMap.put("location","0");
                    userMap.put("device_token",token_id);

                    database.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Intent intent = new Intent(getApplication(),login_activity.class);
                                startActivity(intent);
                            }
                        }
                    });
                    progress.dismiss();
                    Toast.makeText(getApplicationContext(),"USER SUCCESSFULLY REGISTERED",Toast.LENGTH_SHORT).show();
                }
                else{
                    String exp = task.getException().toString();
                    Log.i(TAG,exp);
                    progress.dismiss();
                    Toast.makeText(getApplicationContext(),exp,Toast.LENGTH_LONG).show();
                }
            }
        });
    }


}
