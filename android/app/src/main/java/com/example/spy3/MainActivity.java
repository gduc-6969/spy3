package com.example.spy3;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageInfo;
import android.content.pm.ApplicationInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.provider.Telephony;
import android.telephony.SmsManager;
import android.widget.Toast;
import android.telecom.TelecomManager;
import android.app.role.RoleManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.flutter.embedding.android.FlutterActivity;
import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;

import com.example.spy3.services.BlockingService;

public class MainActivity extends FlutterActivity {
    private static final String CHANNEL = "com.example.spy3/native";
    private static final int PERMISSION_REQUEST_CODE = 1001;
    
    private MethodChannel.Result pendingResult;
    
    @Override
    public void configureFlutterEngine(@NonNull FlutterEngine flutterEngine) {
        super.configureFlutterEngine(flutterEngine);
        
        new MethodChannel(flutterEngine.getDartExecutor().getBinaryMessenger(), CHANNEL)
                .setMethodCallHandler(this::onMethodCall);
    }
    
    private void onMethodCall(MethodCall call, MethodChannel.Result result) {
        Log.d("MainActivity", "Method called: " + call.method);
        switch (call.method) {
            case "requestPermissions":
                requestPermissions(result);
                break;
            case "getSmsMessages":
                getSmsMessages(result);
                break;
            case "getCallLogs":
                getCallLogs(result);
                break;
            case "getContacts":
                getContacts(result);
                break;
            case "getInstalledApps":
                getInstalledApps(result);
                break;
            case "blockNumber":
                String numberToBlock = call.argument("number");
                blockNumber(numberToBlock, result);
                break;
            case "unblockNumber":
                String numberToUnblock = call.argument("number");
                unblockNumber(numberToUnblock, result);
                break;
            case "getBlockedNumbers":
                getBlockedNumbers(result);
                break;
            case "startBlockingService":
                startBlockingService(result);
                break;
            case "stopBlockingService":
                stopBlockingService(result);
                break;
            case "enableCallScreening":
                Log.d("MainActivity", "enableCallScreening method called");
                enableCallScreening(result);
                break;
            default:
                Log.w("MainActivity", "Method not implemented: " + call.method);
                result.notImplemented();
                break;
        }
    }
    
    private void requestPermissions(MethodChannel.Result result) {
        String[] permissions = {
            Manifest.permission.READ_SMS,
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.SEND_SMS,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.WRITE_CONTACTS,
            Manifest.permission.CALL_PHONE
        };
        
        List<String> permissionsToRequest = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) 
                != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(permission);
            }
        }
        
        if (permissionsToRequest.isEmpty()) {
            result.success(true);
        } else {
            pendingResult = result;
            ActivityCompat.requestPermissions(this, 
                permissionsToRequest.toArray(new String[0]), 
                PERMISSION_REQUEST_CODE);
        }
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, 
                                         @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == PERMISSION_REQUEST_CODE && pendingResult != null) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            pendingResult.success(allGranted);
            pendingResult = null;
        }
    }
    
    private void getSmsMessages(MethodChannel.Result result) {
        Log.d("MainActivity", "getSmsMessages: Starting SMS query");
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) 
            != PackageManager.PERMISSION_GRANTED) {
            Log.d("MainActivity", "getSmsMessages: SMS permission not granted");
            result.error("PERMISSION_DENIED", "SMS permission not granted", null);
            return;
        }
        
        Log.d("MainActivity", "getSmsMessages: SMS permission granted, querying...");
        List<Map<String, Object>> smsList = new ArrayList<>();
        ContentResolver contentResolver = getContentResolver();
        
        Cursor cursor = contentResolver.query(
            Telephony.Sms.CONTENT_URI,
            null, null, null,
            Telephony.Sms.DEFAULT_SORT_ORDER
        );
        
        if (cursor != null) {
            Log.d("MainActivity", "getSmsMessages: Cursor created, count: " + cursor.getCount());
            while (cursor.moveToNext()) {
                Map<String, Object> sms = new HashMap<>();
                sms.put("address", cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.ADDRESS)));
                sms.put("body", cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.BODY)));
                sms.put("date", cursor.getLong(cursor.getColumnIndexOrThrow(Telephony.Sms.DATE)));
                sms.put("type", cursor.getInt(cursor.getColumnIndexOrThrow(Telephony.Sms.TYPE)));
                smsList.add(sms);
            }
            cursor.close();
        } else {
            Log.d("MainActivity", "getSmsMessages: Cursor is null");
        }
        
        Log.d("MainActivity", "getSmsMessages: Returning " + smsList.size() + " SMS messages");
        result.success(smsList);
    }
    
    private void getCallLogs(MethodChannel.Result result) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) 
            != PackageManager.PERMISSION_GRANTED) {
            result.error("PERMISSION_DENIED", "Call log permission not granted", null);
            return;
        }
        
        List<Map<String, Object>> callList = new ArrayList<>();
        ContentResolver contentResolver = getContentResolver();
        
        Cursor cursor = contentResolver.query(
            CallLog.Calls.CONTENT_URI,
            null, null, null,
            CallLog.Calls.DEFAULT_SORT_ORDER
        );
        
        if (cursor != null) {
            while (cursor.moveToNext()) {
                Map<String, Object> call = new HashMap<>();
                call.put("number", cursor.getString(cursor.getColumnIndexOrThrow(CallLog.Calls.NUMBER)));
                call.put("date", cursor.getLong(cursor.getColumnIndexOrThrow(CallLog.Calls.DATE)));
                call.put("duration", cursor.getLong(cursor.getColumnIndexOrThrow(CallLog.Calls.DURATION)));
                call.put("type", cursor.getInt(cursor.getColumnIndexOrThrow(CallLog.Calls.TYPE)));
                callList.add(call);
            }
            cursor.close();
        }
        
        result.success(callList);
    }
    
    private void getContacts(MethodChannel.Result result) {
        Log.d("MainActivity", "getContacts: Starting contacts query");
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) 
            != PackageManager.PERMISSION_GRANTED) {
            Log.d("MainActivity", "getContacts: Contacts permission not granted");
            result.error("PERMISSION_DENIED", "Contacts permission not granted", null);
            return;
        }
        
        Log.d("MainActivity", "getContacts: Contacts permission granted, querying...");
        List<Map<String, Object>> contactsList = new ArrayList<>();
        ContentResolver contentResolver = getContentResolver();
        
        Cursor cursor = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null, null, null, null
        );
        
        if (cursor != null) {
            Log.d("MainActivity", "getContacts: Cursor created, count: " + cursor.getCount());
            while (cursor.moveToNext()) {
                Map<String, Object> contact = new HashMap<>();
                contact.put("name", cursor.getString(cursor.getColumnIndexOrThrow(
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)));
                contact.put("number", cursor.getString(cursor.getColumnIndexOrThrow(
                    ContactsContract.CommonDataKinds.Phone.NUMBER)));
                contactsList.add(contact);
            }
            cursor.close();
        } else {
            Log.d("MainActivity", "getContacts: Cursor is null");
        }
        
        Log.d("MainActivity", "getContacts: Returning " + contactsList.size() + " contacts");
        result.success(contactsList);
    }
    
    private void blockNumber(String number, MethodChannel.Result result) {
        // Implementation will be handled by the content provider
        // For now, we'll use a simple storage mechanism
        getSharedPreferences("blocked_numbers", Context.MODE_PRIVATE)
            .edit()
            .putBoolean(number, true)
            .apply();
        result.success(true);
    }
    
    private void unblockNumber(String number, MethodChannel.Result result) {
        getSharedPreferences("blocked_numbers", Context.MODE_PRIVATE)
            .edit()
            .remove(number)
            .apply();
        result.success(true);
    }
    
    private void getBlockedNumbers(MethodChannel.Result result) {
        Map<String, ?> blockedNumbers = getSharedPreferences("blocked_numbers", Context.MODE_PRIVATE)
            .getAll();
        List<String> numbers = new ArrayList<>(blockedNumbers.keySet());
        result.success(numbers);
    }
    
    private void startBlockingService(MethodChannel.Result result) {
        Intent serviceIntent = new Intent(this, BlockingService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
        result.success(true);
    }
    
    private void stopBlockingService(MethodChannel.Result result) {
        Intent serviceIntent = new Intent(this, BlockingService.class);
        stopService(serviceIntent);
        result.success(true);
    }
    
    private void enableCallScreening(MethodChannel.Result result) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                RoleManager roleManager = (RoleManager) getSystemService(Context.ROLE_SERVICE);
                if (roleManager != null && roleManager.isRoleAvailable(RoleManager.ROLE_CALL_SCREENING)) {
                    if (!roleManager.isRoleHeld(RoleManager.ROLE_CALL_SCREENING)) {
                        Intent intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING);
                        startActivityForResult(intent, 1001);
                        result.success(true);
                        return;
                    } else {
                        result.success(true);
                        return;
                    }
                }
            } catch (Exception e) {
                Log.e("MainActivity", "Call screening error: " + e.getMessage(), e);
                result.error("CALL_SCREENING_ERROR", "Failed to enable call screening: " + e.getMessage(), null);
                return;
            }
        }
        
        // For older Android versions or if call screening is not available
        // Still return success so the blocking service can work with reflection method
        result.success(true);
    }
    
    private void getInstalledApps(MethodChannel.Result result) {
        try {
            PackageManager packageManager = getPackageManager();
            List<PackageInfo> packages = packageManager.getInstalledPackages(PackageManager.GET_META_DATA);
            List<Map<String, Object>> appsList = new ArrayList<>();
            
            for (PackageInfo packageInfo : packages) {
                ApplicationInfo appInfo = packageInfo.applicationInfo;
                
                // Get app name
                String appName = packageManager.getApplicationLabel(appInfo).toString();
                
                // Get package name
                String packageName = packageInfo.packageName;
                
                // Get version
                String version = packageInfo.versionName != null ? packageInfo.versionName : "Unknown";
                
                // Check if it's a system app
                boolean isSystemApp = (appInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
                
                // Get install time
                long installTime = packageInfo.firstInstallTime;
                
                // Create app data map
                Map<String, Object> appData = new HashMap<>();
                appData.put("packageName", packageName);
                appData.put("appName", appName);
                appData.put("version", version);
                appData.put("isSystemApp", isSystemApp);
                appData.put("installTime", installTime);
                
                appsList.add(appData);
            }
            
            Log.d("MainActivity", "Found " + appsList.size() + " installed apps");
            result.success(appsList);
            
        } catch (Exception e) {
            Log.e("MainActivity", "Error getting installed apps: " + e.getMessage(), e);
            result.error("APPS_ERROR", "Failed to get installed apps: " + e.getMessage(), null);
        }
    }
}
