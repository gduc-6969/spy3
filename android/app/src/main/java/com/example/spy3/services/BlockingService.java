package com.example.spy3.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import android.os.Build;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.example.spy3.MainActivity;
import com.example.spy3.receivers.CallReceiver;
import com.example.spy3.receivers.SmsReceiver;

public class BlockingService extends Service {
    private static final String TAG = "BlockingService";
    private static final String CHANNEL_ID = "BlockingServiceChannel";
    private static final int NOTIFICATION_ID = 1;
    
    private SmsReceiver smsReceiver;
    private CallReceiver callReceiver;
    
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "BlockingService created");
        
        createNotificationChannel();
        
        // Register receivers dynamically
        smsReceiver = new SmsReceiver();
        callReceiver = new CallReceiver();
        
        IntentFilter smsFilter = new IntentFilter();
        smsFilter.addAction("android.provider.Telephony.SMS_RECEIVED");
        smsFilter.setPriority(1000);
        registerReceiver(smsReceiver, smsFilter);
        
        IntentFilter callFilter = new IntentFilter();
        callFilter.addAction(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
        callFilter.addAction(Intent.ACTION_NEW_OUTGOING_CALL);
        registerReceiver(callReceiver, callFilter);
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "BlockingService started");
        
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent, 
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? 
                PendingIntent.FLAG_IMMUTABLE : PendingIntent.FLAG_UPDATE_CURRENT
        );
        
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Call & SMS Blocking Active")
                .setContentText("Monitoring calls and SMS for blocked numbers")
                .setSmallIcon(android.R.drawable.ic_menu_call)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .build();
        
        startForeground(NOTIFICATION_ID, notification);
        
        return START_STICKY; // Service will be restarted if killed
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "BlockingService destroyed");
        
        if (smsReceiver != null) {
            try {
                unregisterReceiver(smsReceiver);
            } catch (IllegalArgumentException e) {
                Log.w(TAG, "SMS receiver not registered");
            }
        }
        
        if (callReceiver != null) {
            try {
                unregisterReceiver(callReceiver);
            } catch (IllegalArgumentException e) {
                Log.w(TAG, "Call receiver not registered");
            }
        }
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        return null; // Not a bound service
    }
    
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                CHANNEL_ID,
                "Blocking Service Channel",
                NotificationManager.IMPORTANCE_LOW
            );
            serviceChannel.setDescription("Channel for call and SMS blocking service");
            
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
            }
        }
    }
    

}