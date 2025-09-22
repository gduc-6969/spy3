package com.example.spy3.services;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.telecom.Call;
import android.telecom.CallScreeningService;
import android.util.Log;

@TargetApi(Build.VERSION_CODES.Q)
public class CallScreeningServiceImpl extends CallScreeningService {
    private static final String TAG = "CallScreeningService";

    @Override
    public void onScreenCall(Call.Details callDetails) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            String phoneNumber = null;
            
            if (callDetails.getHandle() != null) {
                phoneNumber = callDetails.getHandle().getSchemeSpecificPart();
            }
            
            Log.d(TAG, "Screening call from: " + phoneNumber);
            
            if (isNumberBlocked(phoneNumber)) {
                Log.d(TAG, "Blocking call from: " + phoneNumber);
                
                CallResponse.Builder responseBuilder = new CallResponse.Builder();
                responseBuilder.setDisallowCall(true);
                responseBuilder.setRejectCall(true);
                responseBuilder.setSkipCallLog(false);
                responseBuilder.setSkipNotification(false);
                
                respondToCall(callDetails, responseBuilder.build());
                
                // Log the blocked call
                logCallEvent(phoneNumber, "BLOCKED", System.currentTimeMillis());
            } else {
                // Allow the call
                CallResponse.Builder responseBuilder = new CallResponse.Builder();
                responseBuilder.setDisallowCall(false);
                responseBuilder.setRejectCall(false);
                
                respondToCall(callDetails, responseBuilder.build());
            }
        }
    }
    
    private boolean isNumberBlocked(String phoneNumber) {
        if (phoneNumber == null) return false;
        SharedPreferences prefs = getSharedPreferences("blocked_numbers", Context.MODE_PRIVATE);
        return prefs.getBoolean(phoneNumber, false);
    }
    
    private void logCallEvent(String phoneNumber, String type, long timestamp) {
        SharedPreferences prefs = getSharedPreferences("call_logs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        
        String logKey = timestamp + "_" + phoneNumber;
        String logValue = phoneNumber + "|" + type + "|" + timestamp;
        editor.putString(logKey, logValue);
        editor.apply();
        
        Log.d(TAG, "Call logged: " + logValue);
    }
}