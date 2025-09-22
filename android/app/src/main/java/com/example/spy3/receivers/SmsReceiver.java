package com.example.spy3.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

public class SmsReceiver extends BroadcastReceiver {
    private static final String TAG = "SmsReceiver";
    
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null && intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                Object[] pdus = (Object[]) bundle.get("pdus");
                if (pdus != null) {
                    for (Object pdu : pdus) {
                        SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) pdu);
                        if (smsMessage != null) {
                            String phoneNumber = smsMessage.getOriginatingAddress();
                            String messageBody = smsMessage.getMessageBody();
                            
                            Log.d(TAG, "SMS received from: " + phoneNumber + ", Message: " + messageBody);
                            
                            // Check if the number is blocked
                            if (isNumberBlocked(context, phoneNumber)) {
                                Log.d(TAG, "Blocking SMS from: " + phoneNumber);
                                Toast.makeText(context, "Blocked SMS from: " + phoneNumber, Toast.LENGTH_SHORT).show();
                                
                                // Abort the broadcast to prevent the SMS from appearing in the inbox
                                abortBroadcast();
                                return;
                            }
                            
                            // Log the SMS for the app
                            logSmsMessage(context, phoneNumber, messageBody, System.currentTimeMillis());
                        }
                    }
                }
            }
        }
    }
    
    private boolean isNumberBlocked(Context context, String phoneNumber) {
        SharedPreferences prefs = context.getSharedPreferences("blocked_numbers", Context.MODE_PRIVATE);
        return prefs.getBoolean(phoneNumber, false);
    }
    
    private void logSmsMessage(Context context, String phoneNumber, String message, long timestamp) {
        SharedPreferences prefs = context.getSharedPreferences("sms_logs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        
        // Store SMS log with timestamp as key
        String logKey = timestamp + "_" + phoneNumber;
        String logValue = phoneNumber + "|" + message + "|" + timestamp;
        editor.putString(logKey, logValue);
        editor.apply();
        
        Log.d(TAG, "SMS logged: " + logValue);
    }
}