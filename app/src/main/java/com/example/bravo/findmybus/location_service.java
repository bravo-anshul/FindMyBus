package com.example.bravo.findmybus;


import android.app.Notification;
        import android.app.NotificationManager;
        import android.app.PendingIntent;
        import android.app.Service;
        import android.content.Context;
        import android.content.Intent;
        import android.content.pm.PackageManager;
        import android.graphics.BitmapFactory;
        import android.location.Location;
        import android.location.LocationManager;
        import android.os.Bundle;
        import android.os.IBinder;
        import android.provider.Settings;
        import android.support.annotation.NonNull;
        import android.support.annotation.Nullable;
        import android.support.v4.app.ActivityCompat;
        import android.support.v4.app.NotificationCompat;
        import android.util.Log;
        import android.widget.Toast;

        import com.google.android.gms.common.ConnectionResult;
        import com.google.android.gms.common.api.GoogleApiClient;
        import com.google.android.gms.location.LocationRequest;
        import com.google.android.gms.location.LocationServices;
        import com.google.android.gms.tasks.OnCompleteListener;
        import com.google.android.gms.tasks.Task;
        import com.google.firebase.auth.FirebaseAuth;
        import com.google.firebase.auth.FirebaseUser;
        import com.google.firebase.database.DatabaseReference;
        import com.google.firebase.database.FirebaseDatabase;

        import java.text.SimpleDateFormat;
        import java.util.Date;
        import java.util.HashMap;

public class location_service extends Service implements GoogleApiClient.ConnectionCallbacks ,
        GoogleApiClient.OnConnectionFailedListener , com.google.android.gms.location.LocationListener{

    public static final String TAG = "FindMyBus";
    DatabaseReference database;

    Location mLastLocation;
    GoogleApiClient client;
    private LocationRequest mLocationRequest;
    private NotificationCompat.Builder notification;
    public static final int notiId = 4123;

    String uid;
    Boolean key;


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.i(TAG,"SERVICE WORKING");

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        try {
            uid = currentUser.getUid();
        }
        catch (Exception e){
            uid = null;
        }
        key = intent.getBooleanExtra("key",false);

        if(key){
            if(client==null)
                startNotification();

        }
        else{
            stopLocationUpdates();
            stopForeground(true);
            stopSelf();
        }


        return START_NOT_STICKY;
    }


    public void startNotification(){
        Intent notificationIntent = new Intent(this, main_menu.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);

        Notification notification = new android.support.v7.app.NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.black_noti)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                .setContentTitle("Location sharing")
                .setContentText("Location sharing is enabled.")
                .setContentIntent(pendingIntent)
                .setColor(getApplicationContext().getResources().getColor(android.R.color.white)).build();

        startForeground(1337, notification);

        if(checkGps()){
            buildGoogleClient();
        }
        else{
            notification();
        }

    }

    private boolean checkGps() {
        final LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );

        if( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            notification();
            return false;
        }
        else {
            return true;
        }
    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(10000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(0);

    }


    private void notification() {
        notification = new NotificationCompat.Builder(this);
        notification.setAutoCancel(true);

        notification.setSmallIcon(R.mipmap.ic_launcher);
        notification.setContentTitle("GPS disabled");
        notification.setContentText("Location still sharing turn on gps.");
        notification.setWhen(System.currentTimeMillis());

        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_UPDATE_CURRENT);
        notification.setContentIntent(pendingIntent);

        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        nm.notify(notiId,notification.build());

    }

    private void startLocationUpdates() {

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(client, mLocationRequest, this);
    }

    private void stopLocationUpdates() {
        if(client!=null)
            try {
                LocationServices.FusedLocationApi.removeLocationUpdates(client, this);
            }
            catch (Exception e){
                Log.i(TAG,"App crashed : Google client not yet connected");
            }
    }

    private synchronized void buildGoogleClient() {
        Log.i(TAG,"BUILD CLIENT CALLED");
        client = new GoogleApiClient.Builder(getApplicationContext())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        //Fix first time run app if permission doesn't grant yet so can't get anything
        client.connect();
    }

    public void server(){
        final String latitude = String.valueOf(mLastLocation.getLatitude());
        final String longitude = String.valueOf(mLastLocation.getLongitude());
        String accu = String.valueOf(mLastLocation.getAccuracy());
        Date d=new Date();
        SimpleDateFormat sdf=new SimpleDateFormat("hh:mm a");
        final String curr_time = sdf.format(d);

        database = FirebaseDatabase.getInstance().getReference().child("location").child(uid);
        HashMap<String,String> userMap = new HashMap<>();
        userMap.put("latitude",latitude);
        userMap.put("longitude",longitude);
        userMap.put("accuracy",accu);
        userMap.put("time", String.valueOf(curr_time));

        database.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Log.i(TAG,latitude+longitude);
                    Log.i(TAG,"Time is :" + String.valueOf(curr_time));
                }
                else{
                    Log.i(TAG,"Failed update");
                    Toast.makeText(getApplication(),"Failed updated",Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        server();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        createLocationRequest();
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i(TAG,"Google connection failed");
        Toast.makeText(getApplication(),"Google client connection failed",Toast.LENGTH_SHORT).show();
    }
}
