package com.example.spy3.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;
import android.os.Build;

import java.lang.reflect.Method;

public class CallReceiver extends BroadcastReceiver {
    private static final String TAG = "CallReceiver";
    private static boolean incomingCall = false;
    private static String incomingNumber = null;
    
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        
        if (action != null) {
            if (action.equals(TelephonyManager.ACTION_PHONE_STATE_CHANGED)) {
                handlePhoneStateChange(context, intent);
            } else if (action.equals(Intent.ACTION_NEW_OUTGOING_CALL)) {
                handleOutgoingCall(context, intent);
            }
        }
    }
    
    private void handlePhoneStateChange(Context context, Intent intent) {
        String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
        String phoneNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
        
        if (state != null) {
            if (TelephonyManager.EXTRA_STATE_RINGING.equals(state)) {
                incomingCall = true;
                incomingNumber = phoneNumber;
                Log.d(TAG, "Incoming call from: " + phoneNumber);
                
                if (isNumberBlocked(context, phoneNumber)) {
                    Log.d(TAG, "Blocking call from: " + phoneNumber);
                    Toast.makeText(context, "Blocking call from: " + phoneNumber, Toast.LENGTH_SHORT).show();
                    
                    // Attempt to end the call
                    boolean callEnded = endCall(context);
                    if (callEnded) {
                        Log.d(TAG, "Successfully blocked call from: " + phoneNumber);
                    } else {
                        Log.w(TAG, "Failed to block call from: " + phoneNumber);
                    }
                    
                    // Log the blocked call
                    logCallEvent(context, phoneNumber, "BLOCKED", System.currentTimeMillis());
                }
            } else if (TelephonyManager.EXTRA_STATE_OFFHOOK.equals(state)) {
                if (incomingCall) {
                    Log.d(TAG, "Call answered from: " + incomingNumber);
                    logCallEvent(context, incomingNumber, "ANSWERED", System.currentTimeMillis());
                }
            } else if (TelephonyManager.EXTRA_STATE_IDLE.equals(state)) {
                if (incomingCall) {
                    Log.d(TAG, "Call ended from: " + incomingNumber);
                    logCallEvent(context, incomingNumber, "MISSED", System.currentTimeMillis());
                    incomingCall = false;
                    incomingNumber = null;
                }
            }
        }
    }
    
    private void handleOutgoingCall(Context context, Intent intent) {
        String phoneNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
        Log.d(TAG, "Outgoing call to: " + phoneNumber);
        
        if (isNumberBlocked(context, phoneNumber)) {
            Log.d(TAG, "Blocking outgoing call to: " + phoneNumber);
            Toast.makeText(context, "Blocking outgoing call to: " + phoneNumber, Toast.LENGTH_SHORT).show();
            // Cancel the outgoing call by setting the result data to null
            setResultData(null);
        } else {
            logCallEvent(context, phoneNumber, "OUTGOING", System.currentTimeMillis());
        }
    }
    
    private boolean isNumberBlocked(Context context, String phoneNumber) {
        if (phoneNumber == null) return false;
        SharedPreferences prefs = context.getSharedPreferences("blocked_numbers", Context.MODE_PRIVATE);
        return prefs.getBoolean(phoneNumber, false);
    }
    
    private void logCallEvent(Context context, String phoneNumber, String type, long timestamp) {
        SharedPreferences prefs = context.getSharedPreferences("call_logs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        
        String logKey = timestamp + "_" + phoneNumber;
        String logValue = phoneNumber + "|" + type + "|" + timestamp;
        editor.putString(logKey, logValue);
        editor.apply();
        
        Log.d(TAG, "Call logged: " + logValue);
    }
    
    /**
     * Attempts to end an incoming call using reflection to access TelephonyManager's endCall method
     * This method works on older Android versions but may not work on newer versions due to security restrictions
     */
    private boolean endCall(Context context) {
        try {
            // Method 1: Try using TelephonyManager with reflection
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            
            if (telephonyManager != null) {
                // Try to get the ITelephony interface using reflection
                Class<?> telephonyClass = Class.forName(telephonyManager.getClass().getName());
                Method getITelephonyMethod = telephonyClass.getDeclaredMethod("getITelephony");
                getITelephonyMethod.setAccessible(true);
                Object iTelephonyObject = getITelephonyMethod.invoke(telephonyManager);
                
                if (iTelephonyObject != null) {
                    Class<?> iTelephonyClass = Class.forName(iTelephonyObject.getClass().getName());
                    Method endCallMethod = iTelephonyClass.getDeclaredMethod("endCall");
                    endCallMethod.setAccessible(true);
                    boolean result = (Boolean) endCallMethod.invoke(iTelephonyObject);
                    Log.d(TAG, "endCall() method result: " + result);
                    return result;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Method 1 failed - trying alternative approach", e);
        }
        
        try {
            // Method 2: Try using direct reflection on TelephonyManager
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            Method endCallMethod = telephonyManager.getClass().getDeclaredMethod("endCall");
            endCallMethod.setAccessible(true);
            boolean result = (Boolean) endCallMethod.invoke(telephonyManager);
            Log.d(TAG, "Direct endCall() method result: " + result);
            return result;
        } catch (Exception e) {
            Log.e(TAG, "Method 2 failed - trying final approach", e);
        }
        
        try {
            // Method 3: Try using system service with reflection
            Object service = context.getSystemService("phone");
            if (service != null) {
                Method endCallMethod = service.getClass().getDeclaredMethod("endCall");
                endCallMethod.setAccessible(true);
                boolean result = (Boolean) endCallMethod.invoke(service);
                Log.d(TAG, "System service endCall() method result: " + result);
                return result;
            }
        } catch (Exception e) {
            Log.e(TAG, "All methods failed to end call", e);
        }
        
        return false;
    }
}