package com.example.bravo.findmybus;

import android.app.NotificationManager;
import android.graphics.BitmapFactory;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class notification_class extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        NotificationCompat.Builder notification;

        notification = new NotificationCompat.Builder(this);
        notification.setAutoCancel(true);

        notification.setSmallIcon(R.drawable.black_noti);
        notification.setContentTitle("Location Request");
        notification.setContentText("Somebody has requested your location access.");
        notification.setWhen(System.currentTimeMillis());
        notification.setVibrate(new long[] {1000,1000});
        notification.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
        notification.setSound(Settings.System.DEFAULT_NOTIFICATION_URI);


        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        nm.notify(4123,notification.build());
    }
}
