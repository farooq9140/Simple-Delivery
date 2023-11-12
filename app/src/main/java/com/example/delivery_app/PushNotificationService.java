package com.example.delivery_app;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.jetbrains.annotations.NotNull;

public class PushNotificationService extends FirebaseMessagingService {
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onMessageReceived(@NonNull @NotNull RemoteMessage remoteMessage) {
        String title = remoteMessage.getNotification().getTitle();
        String text = remoteMessage.getNotification().getBody();
        final String CHANNEL_ID = "Parcel_Notif";
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID,"Parcel Notif", NotificationManager.IMPORTANCE_HIGH);
        getSystemService(NotificationManager.class).createNotificationChannel(channel);

        Notification.Builder notification = new Notification.Builder(this,CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(text)
                .setSmallIcon(R.drawable.ic_noti)
                .setAutoCancel(true)
                .setStyle(new Notification.BigTextStyle()
                        .bigText(text));

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this,Homepage.class), PendingIntent.FLAG_UPDATE_CURRENT);

        notification.setContentIntent(contentIntent);

        NotificationManagerCompat.from(this).notify(1,notification.build());

        super.onMessageReceived(remoteMessage);
    }

    @Override
    public void onNewToken(String token) {
        SharedPreferences sharedPref = getSharedPreferences("Settings", Context.MODE_PRIVATE);
        if(sharedPref.getBoolean("switch",true)){
        Log.d(" TAG", "Refreshed token: " + token);

        FirebaseDatabase.getInstance().getReference("test")
                .child("test")
                .setValue(token).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull @org.jetbrains.annotations.NotNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(PushNotificationService.this, "Saved the code", Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(PushNotificationService.this, "No code saved", Toast.LENGTH_SHORT).show();
                }
            }});
        }
    }
}
