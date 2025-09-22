# Call Blocking Test Guide for Spy3 App

## Important Notes for Android 16+ Call Blocking

### Why Call Blocking is Challenging

Call blocking on Android requires different approaches depending on the Android version:

1. **Android 4.1-9 (API 16-28)**: Uses reflection with TelephonyManager.endCall()
2. **Android 10+ (API 29+)**: Requires CallScreeningService with ROLE_CALL_SCREENING

### Testing Instructions

#### Step 1: Install and Setup
1. Install the APK on your Android device
2. Grant all requested permissions when prompted
3. Tap "Enable Advanced Call Blocking" button on the Dashboard

#### Step 2: Add Numbers to Block List
1. Go to SMS, Calls, or Contacts tab
2. Tap the block icon (🚫) next to any number
3. Confirm blocking in the dialog
4. Verify the number appears in the "Blocked" tab

#### Step 3: Start the Blocking Service
1. Tap the security icon in the top-right corner of the app
2. Verify it shows a green security icon when running
3. Check that the persistent notification appears

#### Step 4: Test Call Blocking

**For Android 10+ (Recommended)**:
1. Enable "Call Screening" when prompted
2. Set the app as your default call screening service
3. The CallScreeningService will automatically reject blocked calls

**For Older Android Versions**:
1. The CallReceiver will attempt to end calls using reflection
2. Note: This may not work on all devices due to security restrictions

### Expected Behavior

#### Successful Blocking:
- Blocked calls are automatically rejected
- Toast notification shows "Blocking call from: [number]"
- Call appears in logs as "BLOCKED"
- No ringtone or notification for blocked calls

#### Partial Blocking (Older Android):
- Call may ring briefly before being ended
- Toast notification appears
- Call is logged as "BLOCKED"

### Troubleshooting

#### If Calls Are Not Being Blocked:

1. **Check Permissions**:
   - Ensure all permissions are granted
   - Check phone app settings for call blocking permissions

2. **Enable Call Screening (Android 10+)**:
   - Go to Settings → Apps → Default apps → Call screening
   - Select "Spy3" as your call screening app

3. **Service Status**:
   - Verify the blocking service is running (green icon)
   - Check notification area for "Call & SMS Blocking Active"

4. **Test with Known Number**:
   - Add your own secondary phone number to blocked list
   - Call from that number to test

#### Alternative Testing Method:

If direct call blocking doesn't work, you can verify the detection:

1. Block a test number
2. Call from that number
3. Check the app logs (logcat) for:
   - "Blocking call from: [number]"
   - "Successfully blocked call from: [number]"

### Technical Limitations

#### Android Security Restrictions:
- Modern Android versions restrict call blocking for security
- Some manufacturers disable reflection-based call ending
- Call screening requires user consent and system role

#### Device-Specific Issues:
- Samsung, Huawei, Xiaomi may have additional restrictions
- Some custom ROMs block telephony modifications
- MIUI, ColorOS, OxygenOS may require additional permissions

### Advanced Configuration

#### For Developers/Root Users:
1. Grant system-level permissions via ADB:
   ```bash
   adb shell pm grant com.example.spy3 android.permission.MODIFY_PHONE_STATE
   adb shell pm grant com.example.spy3 android.permission.CALL_PRIVILEGED
   ```

2. Set as system app (requires root):
   ```bash
   adb shell su -c "cp /data/app/com.example.spy3-*/base.apk /system/app/Spy3/Spy3.apk"
   ```

### Expected Log Output

When call blocking works correctly, you should see in logcat:

```
D/CallReceiver: Incoming call from: +1234567890
D/CallReceiver: Blocking call from: +1234567890
D/CallReceiver: Successfully blocked call from: +1234567890
D/CallReceiver: Call logged: +1234567890|BLOCKED|1629876543210
```

### Alternative Solutions

If the app cannot block calls directly:

1. **Detection + Notification**: App detects blocked calls and notifies user
2. **Manual Rejection**: App provides quick "Reject" button overlay
3. **Integration**: Work with existing call blocking apps
4. **System Integration**: Use Android's built-in blocked numbers provider

### Testing Checklist

- [ ] App installs successfully
- [ ] All permissions granted
- [ ] Numbers can be added to block list
- [ ] Blocking service starts and shows notification
- [ ] Call screening enabled (Android 10+)
- [ ] Test call from blocked number
- [ ] Verify call is rejected/ended
- [ ] Check blocked call appears in logs
- [ ] Toast notification shows during blocking

### Success Criteria

**Full Success**: Blocked calls are automatically rejected with no user interaction
**Partial Success**: Blocked calls are detected and logged, may ring briefly
**Detection Only**: Blocked calls are identified but not automatically rejected

Remember: Even if automatic call blocking doesn't work due to system restrictions, the app still provides valuable call monitoring and manual blocking capabilities.