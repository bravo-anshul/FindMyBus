package com.example.bravo.findmybus;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;

public class login_activity extends AppCompatActivity {

    public static final String TAG = "FindMyBus";
    private EditText email_input;
    private EditText pass_input;
    private FirebaseAuth auth;
    private HashMap<String,String> userMap;
    private ProgressDialog progressDialog;
    private AlertDialog.Builder builder;
    private DatabaseReference userData;

    SignInButton googleBtn;
    GoogleApiClient mGoogleSignInClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_activity);

        auth = FirebaseAuth.getInstance();
        userData = FirebaseDatabase.getInstance().getReference();
        pass_input = (EditText) findViewById(R.id.login_password);
        email_input = (EditText) findViewById(R.id.login_email);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Checking..");
        progressDialog.setMessage("please wait this could take a while...");
        progressDialog.setCancelable(false);

        googleBtn = (SignInButton) findViewById(R.id.google_btn);
        googleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressDialog.show();
                signIn();
            }
        });


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(android.Manifest.permission.CALL_PHONE)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{
                        android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION,
                        android.Manifest.permission.INTERNET, Manifest.permission.CALL_PHONE
                }, 10);
                Toast.makeText(this, "Allow permission.", Toast.LENGTH_LONG).show();
            }
        }

        if (auth.getCurrentUser() != null) {
            Toast.makeText(getApplicationContext(),"You are logged in as: "+auth.getCurrentUser().getEmail(), Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getApplication(),main_menu.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        }


        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = new GoogleApiClient.Builder(getApplicationContext())
                .enableAutoManage(this , new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

                    }
                } /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        builder = new AlertDialog.Builder(this);
        builder.setTitle("Bus Number");
        builder.setMessage("Enter your bus number and you're good to go.");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_CLASS_NUMBER);
        input.setFilters(new InputFilter[] {new InputFilter.LengthFilter(2)});
        builder.setView(input);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String m_Text = input.getText().toString();
                try {
                    if(Objects.equals(m_Text, "")){
                        throw new IOException();
                    }
                    else {
                        userMap.put("bus",m_Text);
                        dialog.dismiss();
                        dataEntry();
                    }
                } catch (IOException e) {

                    AlertDialog thisDialog = (AlertDialog) dialog;
                    thisDialog.setMessage("This is required , you can always change it later.");
                    thisDialog.setIcon(android.R.drawable.ic_dialog_alert);
                    thisDialog.cancel();

                }
            }
        });

        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                ((AlertDialog) dialog).show();
            }
        });
    }

    public void login(View view){
        String email = email_input.getText().toString();
        String pass = pass_input.getText().toString();
        Log.i(TAG,email+pass);

        if(!Objects.equals(email, "") && !Objects.equals(pass, "")){
            progressDialog.show();
            auth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {

                        String token_id = FirebaseInstanceId.getInstance().getToken();
                        Log.i(TAG,"TOKEN ID"+token_id);
                        String curr_user = auth.getCurrentUser().getUid();
                        userData.child("users").child(curr_user).child("device_token").setValue(token_id);

                        progressDialog.dismiss();
                        Toast.makeText(getApplication(), "Login Successful", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(getApplication(),main_menu.class);
                        startActivity(intent);
                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    } else {
                        progressDialog.dismiss();
                        String exp = task.getException().toString();
                        Log.i(TAG, exp);
                        Toast.makeText(getApplication(), exp, Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
        else{
            Toast.makeText(getApplication(), "Fill all the details", Toast.LENGTH_SHORT).show();
        }
    }

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleSignInClient);
        startActivityForResult(signInIntent, 2);
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 2){
            GoogleSignInResult googleSignInResult = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (googleSignInResult.isSuccess()){
                GoogleSignInAccount account = googleSignInResult.getSignInAccount();
                firebaseAuthWithGoogle(account);
            }

        }
    }

    private void firebaseAuthWithGoogle(final GoogleSignInAccount acct) {

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = auth.getCurrentUser();
                            checkUser(acct,user);
                            Log.d(TAG, "signInWithCredential:success");
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());

                        }

                        // ...
                    }
                });
    }

    private void checkUser(final GoogleSignInAccount acct, final FirebaseUser user){

        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference().child("users");
        rootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.hasChild(user.getUid())) {
                    progressDialog.dismiss();
                    Intent intent = new Intent(getApplication(),login_activity.class);
                    startActivity(intent);
                }
                else{

                    Toast.makeText(getApplicationContext(),"New User Detected.",Toast.LENGTH_SHORT).show();
                    String token_id = FirebaseInstanceId.getInstance().getToken();

                    userMap = new HashMap<>();
                    userMap.put("username",acct.getDisplayName());
                    userMap.put("email",acct.getEmail());
                    userMap.put("avatar", String.valueOf(acct.getPhotoUrl()));
                    userMap.put("location","0");
                    userMap.put("device_token",token_id);
                    progressDialog.dismiss();
                    builder.show();

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void dataEntry(){
        FirebaseUser user = auth.getCurrentUser();
        DatabaseReference database = FirebaseDatabase.getInstance().getReference().child("users").child(user.getUid());
        database.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    progressDialog.dismiss();
                    Intent intent = new Intent(getApplication(),login_activity.class);
                    startActivity(intent);
                }
            }
        });
    }

    public void signUp_activity(View view){
        Log.i(TAG,"SIGN UP CALLED");
        Intent intent = new Intent();
        intent.setClass(getApplicationContext(), signUp_activity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAffinity();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case 10: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    return;
                } else {
                    Toast.makeText(this,"We need Location Access.",Toast.LENGTH_LONG).show();
                    finishAffinity();
                }
                return;
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

}
