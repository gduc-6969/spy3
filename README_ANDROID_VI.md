# Spy3 - Hướng Dẫn Triển Khai Android/Java

Hướng dẫn toàn diện về triển khai Android native của ứng dụng Spy3 Call & SMS Manager, tập trung vào Java code, quyền và tích hợp nền tảng.

## 📱 Tổng Quan

Tài liệu này bao gồm triển khai Android native bao gồm platform channels, xử lý quyền, content providers và tích hợp cấp hệ thống.

## 🏗️ Cấu Trúc Dự Án Android

```
android/
├── app/
│   ├── src/main/
│   │   ├── java/com/example/spy3/
│   │   │   └── MainActivity.java          # Trình xử lý platform channel chính
│   │   ├── AndroidManifest.xml            # Cấu hình ứng dụng và quyền
│   │   └── res/                          # Tài nguyên Android
│   └── build.gradle.kts                  # Cấu hình build
├── gradle/
│   └── wrapper/                          # Tệp Gradle wrapper
└── settings.gradle.kts                   # Cài đặt dự án
```

---

## 🤖 Triển Khai Android Native

### `MainActivity.java` - Trình Xử Lý Platform Channel Chính

```java
package com.example.spy3;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageInfo;
import android.content.pm.ApplicationInfo;
```
**Giải thích:**
- Khai báo package cho namespace ứng dụng
- Imports Android thiết yếu cho quyền, truy cập nội dung và quản lý package
- `Manifest`: Hằng số quyền Android
- `ContentResolver`: Truy cập đến system content providers
- `PackageManager/PackageInfo`: Thông tin package ứng dụng

```java
public class MainActivity extends FlutterActivity {
    private static final String CHANNEL = "com.example.spy3/native";
    private static final int PERMISSION_REQUEST_CODE = 1001;
    
    private MethodChannel.Result pendingResult;
```
**Giải thích:**
- Extends `FlutterActivity`: Cung cấp tích hợp Flutter framework
- `CHANNEL`: Phải khớp chính xác với tên channel Flutter side
- `PERMISSION_REQUEST_CODE`: Định danh duy nhất cho yêu cầu quyền
- `pendingResult`: Lưu trữ callback cho kết quả quyền async

```java
    @Override
    public void configureFlutterEngine(@NonNull FlutterEngine flutterEngine) {
        super.configureFlutterEngine(flutterEngine);
        
        new MethodChannel(flutterEngine.getDartExecutor().getBinaryMessenger(), CHANNEL)
                .setMethodCallHandler(this::onMethodCall);
    }
```
**Giải thích:**
- Được gọi khi Flutter engine khởi tạo
- Tạo method channel cho giao tiếp Dart-Java
- `setMethodCallHandler()`: Đăng ký callback cho Flutter method calls
- `this::onMethodCall`: Syntax method reference (Java 8)

```java
    private void onMethodCall(MethodCall call, MethodChannel.Result result) {
        Log.d("MainActivity", "Phương thức được gọi: " + call.method);
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
            default:
                Log.w("MainActivity", "Phương thức chưa triển khai: " + call.method);
                result.notImplemented();
                break;
        }
    }
```
**Giải thích:**
- Dispatcher trung tâm cho tất cả Flutter method calls
- `Log.d()`: Debug logging để troubleshooting
- Switch statement định tuyến calls đến handlers phù hợp
- `call.argument()`: Trích xuất tham số từ Flutter call
- `result.notImplemented()`: Báo Flutter phương thức không có sẵn

---

### Hệ Thống Quản Lý Quyền

#### Trình Xử Lý Yêu Cầu Quyền

```java
    private void requestPermissions(MethodChannel.Result result) {
        String[] permissions = {
            Manifest.permission.READ_SMS,
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.SEND_SMS,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.ANSWER_PHONE_CALLS,
            Manifest.permission.MODIFY_PHONE_STATE
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
            ActivityCompat.requestPermissions(
                this,
                permissionsToRequest.toArray(new String[0]),
                PERMISSION_REQUEST_CODE
            );
        }
    }
```
**Giải thích:**
- Định nghĩa tất cả quyền yêu cầu cho chức năng ứng dụng
- `checkSelfPermission()`: Kiểm tra nếu quyền đã được cấp
- Chỉ yêu cầu quyền chưa được cấp
- `pendingResult`: Lưu trữ callback để phản hồi sau tương tác người dùng
- `requestPermissions()`: Hiển thị dialog quyền Android

#### Callback Kết Quả Quyền

```java
    @Override
    public void onRequestPermissionsResult(
        int requestCode, 
        @NonNull String[] permissions, 
        @NonNull int[] grantResults
    ) {
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
```
**Giải thích:**
- Được gọi khi người dùng phản hồi dialog quyền
- Kiểm tra nếu tất cả quyền được yêu cầu đã được cấp
- Trả về kết quả cho Flutter qua callback đã lưu
- Dọn dẹp pending result để ngăn memory leaks

---

### Triển Khai Truy Cập Dữ Liệu

#### Truy Xuất Tin Nhắn SMS

```java
    private void getSmsMessages(MethodChannel.Result result) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS)
            != PackageManager.PERMISSION_GRANTED) {
            result.error("PERMISSION_DENIED", "Quyền SMS chưa được cấp", null);
            return;
        }

        try {
            ContentResolver contentResolver = getContentResolver();
            Uri smsUri = Telephony.Sms.CONTENT_URI;
            String[] projection = {"address", "body", "date", "type"};
            
            Cursor cursor = contentResolver.query(
                smsUri, 
                projection, 
                null, 
                null, 
                "date DESC LIMIT 100"
            );

            List<Map<String, Object>> smsList = new ArrayList<>();
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    Map<String, Object> sms = new HashMap<>();
                    sms.put("address", cursor.getString(0));
                    sms.put("body", cursor.getString(1));
                    sms.put("date", cursor.getLong(2));
                    sms.put("type", cursor.getInt(3));
                    smsList.add(sms);
                }
                cursor.close();
            }

            Log.d("MainActivity", "Tìm thấy " + smsList.size() + " tin nhắn SMS");
            result.success(smsList);

        } catch (Exception e) {
            Log.e("MainActivity", "Lỗi khi lấy tin nhắn SMS: " + e.getMessage(), e);
            result.error("SMS_ERROR", "Không thể lấy tin nhắn SMS: " + e.getMessage(), null);
        }
    }
```
**Giải thích:**
- Kiểm tra quyền trước khi truy cập dữ liệu nhạy cảm
- `ContentResolver`: Cách của Android để truy cập dữ liệu hệ thống
- `Telephony.Sms.CONTENT_URI`: Vị trí cơ sở dữ liệu SMS hệ thống
- Truy vấn giống SQL với projection (chọn cột)
- `"date DESC LIMIT 100"`: 100 tin nhắn mới nhất, mới nhất trước
- `Cursor`: Iterator tập kết quả cho database queries
- Iteration thủ công để xây dựng result list
- Dọn dẹp tài nguyên đúng cách với `cursor.close()`

#### Truy Xuất Call Logs

```java
    private void getCallLogs(MethodChannel.Result result) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG)
            != PackageManager.PERMISSION_GRANTED) {
            result.error("PERMISSION_DENIED", "Quyền call log chưa được cấp", null);
            return;
        }

        try {
            ContentResolver contentResolver = getContentResolver();
            Uri callLogUri = CallLog.Calls.CONTENT_URI;
            String[] projection = {
                CallLog.Calls.NUMBER,
                CallLog.Calls.DATE,
                CallLog.Calls.DURATION,
                CallLog.Calls.TYPE
            };
            
            Cursor cursor = contentResolver.query(
                callLogUri,
                projection,
                null,
                null,
                CallLog.Calls.DATE + " DESC LIMIT 100"
            );

            List<Map<String, Object>> callsList = new ArrayList<>();
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    Map<String, Object> call = new HashMap<>();
                    call.put("number", cursor.getString(0));
                    call.put("date", cursor.getLong(1));
                    call.put("duration", cursor.getInt(2));
                    call.put("type", cursor.getInt(3));
                    callsList.add(call);
                }
                cursor.close();
            }

            Log.d("MainActivity", "Tìm thấy " + callsList.size() + " call logs");
            result.success(callsList);

        } catch (Exception e) {
            Log.e("MainActivity", "Lỗi khi lấy call logs: " + e.getMessage(), e);
            result.error("CALL_LOG_ERROR", "Không thể lấy call logs: " + e.getMessage(), null);
        }
    }
```
**Giải thích:**
- Mẫu tương tự SMS nhưng cho lịch sử cuộc gọi
- `CallLog.Calls.CONTENT_URI`: Cơ sở dữ liệu call log hệ thống
- Sử dụng hằng số CallLog cho tên cột
- Bao gồm thời lượng cuộc gọi và thông tin loại
- Xử lý lỗi toàn diện với báo cáo lỗi Flutter

#### Truy Xuất Contacts

```java
    private void getContacts(MethodChannel.Result result) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
            != PackageManager.PERMISSION_GRANTED) {
            result.error("PERMISSION_DENIED", "Quyền contacts chưa được cấp", null);
            return;
        }

        try {
            ContentResolver contentResolver = getContentResolver();
            Uri contactsUri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
            String[] projection = {
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER
            };
            
            Cursor cursor = contentResolver.query(
                contactsUri,
                projection,
                null,
                null,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
            );

            List<Map<String, Object>> contactsList = new ArrayList<>();
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    Map<String, Object> contact = new HashMap<>();
                    contact.put("name", cursor.getString(0));
                    contact.put("number", cursor.getString(1));
                    contactsList.add(contact);
                }
                cursor.close();
            }

            Log.d("MainActivity", "Tìm thấy " + contactsList.size() + " contacts");
            result.success(contactsList);

        } catch (Exception e) {
            Log.e("MainActivity", "Lỗi khi lấy contacts: " + e.getMessage(), e);
            result.error("CONTACTS_ERROR", "Không thể lấy contacts: " + e.getMessage(), null);
        }
    }
```
**Giải thích:**
- Truy cập cơ sở dữ liệu contacts Android
- `ContactsContract`: API contacts của Android
- Sắp xếp contacts theo alphabet theo tên hiển thị
- Trích xuất cặp tên và số điện thoại

---

### Truy Xuất Ứng Dụng Đã Cài Đặt (Triển Khai Nổi Bật)

```java
    private void getInstalledApps(MethodChannel.Result result) {
        try {
            PackageManager packageManager = getPackageManager();
            List<PackageInfo> packages = packageManager.getInstalledPackages(PackageManager.GET_META_DATA);
            List<Map<String, Object>> appsList = new ArrayList<>();
            
            for (PackageInfo packageInfo : packages) {
                ApplicationInfo appInfo = packageInfo.applicationInfo;
                
                // Lấy tên ứng dụng
                String appName = packageManager.getApplicationLabel(appInfo).toString();
                
                // Lấy tên package
                String packageName = packageInfo.packageName;
                
                // Lấy phiên bản
                String version = packageInfo.versionName != null ? packageInfo.versionName : "Không xác định";
                
                // Kiểm tra nếu là ứng dụng hệ thống
                boolean isSystemApp = (appInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
                
                // Lấy thời gian cài đặt
                long installTime = packageInfo.firstInstallTime;
                
                // Tạo map dữ liệu ứng dụng
                Map<String, Object> appData = new HashMap<>();
                appData.put("packageName", packageName);
                appData.put("appName", appName);
                appData.put("version", version);
                appData.put("isSystemApp", isSystemApp);
                appData.put("installTime", installTime);
                
                appsList.add(appData);
            }
            
            Log.d("MainActivity", "Tìm thấy " + appsList.size() + " ứng dụng đã cài đặt");
            result.success(appsList);
            
        } catch (Exception e) {
            Log.e("MainActivity", "Lỗi khi lấy ứng dụng đã cài đặt: " + e.getMessage(), e);
            result.error("APPS_ERROR", "Không thể lấy ứng dụng đã cài đặt: " + e.getMessage(), null);
        }
    }
```
**Giải thích:**
- `getPackageManager()`: Truy cập đến hệ thống quản lý package Android
- `getInstalledPackages()`: Trả về tất cả package ứng dụng đã cài đặt
- `GET_META_DATA`: Bao gồm metadata trong thông tin package
- `getApplicationLabel()`: Lấy tên ứng dụng có thể đọc được
- `FLAG_SYSTEM`: Cờ bitwise để nhận dạng ứng dụng hệ thống
- `firstInstallTime`: Timestamp khi ứng dụng được cài đặt lần đầu
- Xây dựng map với tất cả thông tin ứng dụng liên quan
- Không yêu cầu quyền đặc biệt (sử dụng quyền QUERY_ALL_PACKAGES manifest)

---

### Triển Khai Chặn Số

#### Phương Thức Block Number

```java
    private void blockNumber(String number, MethodChannel.Result result) {
        if (number == null || number.trim().isEmpty()) {
            result.error("INVALID_NUMBER", "Số không thể để trống", null);
            return;
        }

        try {
            // Lưu trữ số đã chặn trong SharedPreferences
            SharedPreferences prefs = getSharedPreferences("blocked_numbers", Context.MODE_PRIVATE);
            Set<String> blockedNumbers = prefs.getStringSet("numbers", new HashSet<>());
            
            // Tạo bản sao mutable
            Set<String> mutableBlockedNumbers = new HashSet<>(blockedNumbers);
            mutableBlockedNumbers.add(number.trim());
            
            // Lưu trở lại preferences
            SharedPreferences.Editor editor = prefs.edit();
            editor.putStringSet("numbers", mutableBlockedNumbers);
            boolean success = editor.commit();
            
            if (success) {
                Log.d("MainActivity", "Chặn số thành công: " + number);
                result.success(true);
            } else {
                Log.e("MainActivity", "Không thể lưu số đã chặn: " + number);
                result.success(false);
            }
            
        } catch (Exception e) {
            Log.e("MainActivity", "Lỗi khi chặn số: " + e.getMessage(), e);
            result.error("BLOCK_ERROR", "Không thể chặn số: " + e.getMessage(), null);
        }
    }
```
**Giải thích:**
- Validation đầu vào cho số rỗng hoặc null
- `SharedPreferences`: Hệ thống lưu trữ key-value của Android
- `getStringSet()`: Truy xuất số đã chặn hiện tại
- Tạo bản sao mutable để tránh modification exceptions
- `commit()`: Lưu dữ liệu đồng bộ lên disk
- Xử lý lỗi và logging toàn diện

#### Phương Thức Unblock Number

```java
    private void unblockNumber(String number, MethodChannel.Result result) {
        if (number == null || number.trim().isEmpty()) {
            result.error("INVALID_NUMBER", "Số không thể để trống", null);
            return;
        }

        try {
            SharedPreferences prefs = getSharedPreferences("blocked_numbers", Context.MODE_PRIVATE);
            Set<String> blockedNumbers = prefs.getStringSet("numbers", new HashSet<>());
            
            Set<String> mutableBlockedNumbers = new HashSet<>(blockedNumbers);
            boolean wasRemoved = mutableBlockedNumbers.remove(number.trim());
            
            if (wasRemoved) {
                SharedPreferences.Editor editor = prefs.edit();
                editor.putStringSet("numbers", mutableBlockedNumbers);
                boolean success = editor.commit();
                
                Log.d("MainActivity", "Bỏ chặn số thành công: " + number);
                result.success(success);
            } else {
                Log.w("MainActivity", "Số không có trong danh sách chặn: " + number);
                result.success(true); // Coi như thành công nếu không bị chặn
            }
            
        } catch (Exception e) {
            Log.e("MainActivity", "Lỗi khi bỏ chặn số: " + e.getMessage(), e);
            result.error("UNBLOCK_ERROR", "Không thể bỏ chặn số: " + e.getMessage(), null);
        }
    }
```
**Giải thích:**
- Mẫu tương tự blocking nhưng xóa số
- `remove()`: Trả về boolean cho biết item có được xóa không
- Xử lý graceful khi số không bị chặn
- Báo cáo lỗi nhất quán cho Flutter

---

### Triển Khai Background Service

#### Quản Lý Blocking Service

```java
    private void startBlockingService(MethodChannel.Result result) {
        try {
            Intent serviceIntent = new Intent(this, BlockingService.class);
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent);
            } else {
                startService(serviceIntent);
            }
            
            Log.d("MainActivity", "Blocking service đã khởi động");
            result.success(true);
            
        } catch (Exception e) {
            Log.e("MainActivity", "Lỗi khi khởi động blocking service: " + e.getMessage(), e);
            result.error("SERVICE_ERROR", "Không thể khởi động blocking service: " + e.getMessage(), null);
        }
    }

    private void stopBlockingService(MethodChannel.Result result) {
        try {
            Intent serviceIntent = new Intent(this, BlockingService.class);
            stopService(serviceIntent);
            
            Log.d("MainActivity", "Blocking service đã dừng");
            result.success(true);
            
        } catch (Exception e) {
            Log.e("MainActivity", "Lỗi khi dừng blocking service: " + e.getMessage(), e);
            result.error("SERVICE_ERROR", "Không thể dừng blocking service: " + e.getMessage(), null);
        }
    }
```
**Giải thích:**
- `Intent`: Cơ chế giao tiếp component của Android
- `startForegroundService()`: Yêu cầu cho background services Android 8.0+
- Kiểm tra phiên bản cho tương thích API
- Quản lý vòng đời service

---

## 📋 Tệp Cấu Hình Android

### `AndroidManifest.xml` - Cấu Hình Ứng Dụng

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <!-- Quyền cho hoạt động SMS -->
    <uses-permission android:name="android.permission.RECEIVE_SMS" />
    <uses-permission android:name="android.permission.READ_SMS" />
    <uses-permission android:name="android.permission.SEND_SMS" />
    <uses-permission android:name="android.permission.WRITE_SMS" />
```
**Giải thích:**
- Manifest khai báo khả năng và yêu cầu ứng dụng
- Quyền SMS để đọc, nhận, gửi và sửa đổi tin nhắn
- Yêu cầu cho chức năng SMS đầy đủ

```xml
    <!-- Quyền cho hoạt động cuộc gọi -->
    <uses-permission android:name="android.permission.READ_PHONE_STATE" />
    <uses-permission android:name="android.permission.CALL_PHONE" />
    <uses-permission android:name="android.permission.ANSWER_PHONE_CALLS" />
    <uses-permission android:name="android.permission.READ_CALL_LOG" />
    <uses-permission android:name="android.permission.WRITE_CALL_LOG" />
    <uses-permission android:name="android.permission.PROCESS_OUTGOING_CALLS" />
```
**Giải thích:**
- Quyền liên quan cuộc gọi để giám sát và quản lý cuộc gọi điện thoại
- `READ_PHONE_STATE`: Truy cập trạng thái và danh tính điện thoại
- `ANSWER_PHONE_CALLS`: Trả lời cuộc gọi đến theo chương trình
- Quyền call log để đọc và viết lịch sử cuộc gọi

```xml
    <!-- Yêu cầu cho chặn cuộc gọi -->
    <uses-permission android:name="android.permission.MODIFY_PHONE_STATE" />
    <uses-permission android:name="android.permission.CALL_PRIVILEGED" />
    
    <!-- Yêu cầu cho sàng lọc cuộc gọi Android 10+ -->
    <uses-permission android:name="android.permission.BIND_SCREENING_SERVICE" />
    
    <!-- Yêu cầu cho quản lý cuộc gọi Android 10+ -->
    <uses-permission android:name="android.permission.MANAGE_OWN_CALLS" />
```
**Giải thích:**
- Quyền nâng cao cho kiểm soát và chặn cuộc gọi
- `MODIFY_PHONE_STATE`: Thao tác trạng thái điện thoại cấp thấp
- `CALL_PRIVILEGED`: Hoạt động cuộc gọi cấp hệ thống
- Quyền cụ thể Android 10+ cho APIs sàng lọc cuộc gọi hiện đại

```xml
    <!-- Yêu cầu để truy vấn tất cả packages (Android 11+) -->
    <uses-permission android:name="android.permission.QUERY_ALL_PACKAGES" />
```
**Giải thích:**
- Android 11+ yêu cầu quyền rõ ràng để xem tất cả ứng dụng đã cài đặt
- Không có điều này, ứng dụng chỉ có thể thấy tập con hạn chế của packages đã cài đặt
- Yêu cầu cho chức năng liệt kê ứng dụng mới

```xml
    <!-- Quyền background service -->
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
    <uses-permission android:name="android.permission.WAKE_LOCK" />
    <uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
```
**Giải thích:**
- `FOREGROUND_SERVICE`: Yêu cầu cho background services chạy lâu
- `WAKE_LOCK`: Ngăn thiết bị ngủ trong quá trình giám sát
- `RECEIVE_BOOT_COMPLETED`: Cho phép service khởi động khi device boot

```xml
    <application
        android:label="spy3"
        android:name="${applicationName}"
        android:icon="@mipmap/ic_launcher"
        android:allowBackup="true"
        android:theme="@style/LaunchTheme">
        
        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:launchMode="singleTop"
            android:taskAffinity=""
            android:theme="@style/LaunchTheme"
            android:configChanges="orientation|keyboardHidden|keyboard|screenSize|smallestScreenSize|locale|layoutDirection|fontScale|screenLayout|density|uiMode"
            android:hardwareAccelerated="true"
            android:windowSoftInputMode="adjustResize">
            
            <meta-data
                android:name="io.flutter.embedding.android.NormalTheme"
                android:resource="@style/NormalTheme" />
                
            <intent-filter android:autoVerify="true">
                <action android:name="android.intent.action.MAIN"/>
                <category android:name="android.intent.category.LAUNCHER"/>
            </intent-filter>
        </activity>
        
        <!-- Metadata Flutter framework -->
        <meta-data
            android:name="flutterEmbedding"
            android:value="2" />
    </application>
```
**Giải thích:**
- Khối cấu hình application
- `android:exported="true"`: Cho phép ứng dụng ngoài khởi động activity này
- `android:launchMode="singleTop"`: Ngăn nhiều instances
- `configChanges`: Xử lý thay đổi cấu hình mà không restart
- Flutter embedding phiên bản 2 cho ứng dụng Flutter hiện đại

---

### `build.gradle.kts` - Cấu Hình Build

```kotlin
android {
    namespace = "com.example.spy3"
    compileSdk = flutter.compileSdkVersion
    ndkVersion = flutter.ndkVersion

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    defaultConfig {
        applicationId = "com.example.spy3"
        minSdk = flutter.minSdkVersion
        targetSdk = flutter.targetSdkVersion
        versionCode = flutter.versionCode
        versionName = flutter.versionName
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
        }
    }
}

dependencies {
    implementation("androidx.annotation:annotation:1.6.0")
    implementation("androidx.core:core-ktx:1.10.1")
}
```
**Giải thích:**
- Cấu hình Gradle build cho Android app
- `namespace`: Định danh ứng dụng duy nhất cho hệ thống package Android
- Phiên bản SDK được kiểm soát bởi Flutter framework
- Tương thích Java 8 cho tính năng ngôn ngữ hiện đại
- Thông tin phiên bản kế thừa từ cấu hình Flutter
- Tối ưu release build với ProGuard
- Dependencies AndroidX cho phát triển Android hiện đại

---

## 🔧 Tính Năng Android Nâng Cao

### Tích Hợp Content Providers
- **SMS Provider**: Truy cập cơ sở dữ liệu SMS hệ thống qua ContentResolver
- **Call Log Provider**: Đọc lịch sử cuộc gọi với quyền đúng cách
- **Contacts Provider**: Truy cập cơ sở dữ liệu contacts thiết bị
- **Custom Queries**: Truy vấn giống SQL với projections và sorting

### Quản Lý Quyền
- **Runtime Permissions**: Yêu cầu quyền động Android 6.0+
- **Permission Groups**: Quyền liên quan được yêu cầu cùng nhau
- **Graceful Degradation**: Chức năng ứng dụng với quyền một phần
- **User Education**: Giải thích rõ ràng cho yêu cầu quyền

### Platform Channel Communication
- **Method Channels**: Giao tiếp hai chiều giữa Flutter và Android
- **Type Safety**: Trao đổi dữ liệu có cấu trúc qua Maps và Lists
- **Error Handling**: Propagation lỗi đúng cách đến Flutter layer
- **Async Operations**: Hoạt động không chặn với callback results

### Background Processing
- **Foreground Services**: Hoạt động background chạy lâu
- **Broadcast Receivers**: Giám sát sự kiện hệ thống
- **Service Lifecycle**: Quản lý và dọn dẹp service đúng cách
- **Battery Optimization**: Xử lý background hiệu quả

---

## 🛡️ Cân Nhắc Bảo Mật

### Bảo Mật Quyền
- **Least Privilege**: Chỉ yêu cầu quyền cần thiết
- **Permission Validation**: Kiểm tra quyền trước hoạt động nhạy cảm
- **User Consent**: Giải thích quyền rõ ràng và lý do
- **Graceful Handling**: Hành vi đúng cách khi quyền bị từ chối

### Bảo Vệ Dữ Liệu
- **Local Storage**: Lưu trữ an toàn dữ liệu nhạy cảm
- **Input Validation**: Sanitize tất cả user inputs
- **Error Information**: Tránh expose dữ liệu nhạy cảm trong thông báo lỗi
- **Access Control**: Hạn chế truy cập đến thành phần ứng dụng nhạy cảm

### Tuân Thủ Riêng Tư
- **Data Minimization**: Chỉ truy cập dữ liệu người dùng cần thiết
- **Transparency**: Giải thích sử dụng dữ liệu rõ ràng
- **User Control**: Cho phép người dùng quản lý dữ liệu của họ
- **Secure Communication**: Truyền dữ liệu được bảo vệ

---

## 🧪 Kiểm Thử Triển Khai Android

### Unit Tests
```java
@Test
public void testPermissionCheck() {
    // Mock permission check
    when(mockContext.checkSelfPermission(Manifest.permission.READ_SMS))
        .thenReturn(PackageManager.PERMISSION_GRANTED);
    
    // Test permission logic
    assertTrue(hasRequiredPermissions());
}
```

### Integration Tests
```java
@Test
public void testSmsRetrieval() {
    // Test truy xuất dữ liệu SMS thực tế
    // Xác minh định dạng dữ liệu và xử lý lỗi
}
```

### Instrumentation Tests
- Test giao tiếp platform channel
- Xác minh luồng yêu cầu quyền
- Test chức năng background service

---

## 🚀 Tối Ưu Hiệu Suất

### Truy Cập Database
- **Efficient Queries**: Sử dụng projections để hạn chế truy xuất dữ liệu
- **Pagination**: Giới hạn kết quả để ngăn vấn đề bộ nhớ
- **Cursor Management**: Dọn dẹp cursor đúng cách để ngăn leaks
- **Background Threading**: Hoạt động database non-UI thread

### Quản Lý Bộ Nhớ
- **Object Lifecycle**: Dọn dẹp đúng cách tài nguyên native
- **Collection Management**: Hoạt động List và Map hiệu quả
- **Reference Management**: Tránh memory leaks trong callbacks
- **Garbage Collection**: Tối thiểu tạo object trong vòng lặp

### Hiệu Quả Platform Channel
- **Batch Operations**: Nhóm hoạt động liên quan để giảm channel calls
- **Data Serialization**: Serialization cấu trúc dữ liệu hiệu quả
- **Error Handling**: Tối thiểu overhead exception
- **Callback Management**: Quản lý vòng đời callback đúng cách

Triển khai Android này cung cấp nền tảng native mạnh mẽ, an toàn và hiệu quả cho ứng dụng Spy3, tuân theo thực hành phát triển Android tốt nhất và hướng dẫn bảo mật.