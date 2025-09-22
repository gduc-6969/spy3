# Spy3 - Call & SMS Manager

A Flutter application with Java native Android code for reading SMS messages, managing call logs, and blocking/unblocking phone numbers.

## Features

- **SMS Management**: Read and display SMS messages with sender details and timestamps
- **Call Log Management**: View incoming, outgoing, and missed calls with duration information
- **Contact Management**: Access and display device contacts
- **Number Blocking**: Block and unblock phone numbers to prevent calls and SMS
- **Background Service**: Runs a foreground service to monitor and block calls/SMS in real-time
- **Permissions Management**: Handles all necessary Android permissions automatically

## Architecture

### Android Java Components

1. **MainActivity.java**: Main activity with method channels for Flutter-Java communication
2. **SmsReceiver.java**: BroadcastReceiver to intercept incoming SMS messages
3. **CallReceiver.java**: BroadcastReceiver to monitor phone state changes and calls
4. **BlockingService.java**: Foreground service for background call/SMS monitoring
5. **BlockedNumbersProvider.java**: ContentProvider for managing blocked numbers database

### Flutter Components

1. **NativeService**: Service class for communicating with native Android code
2. **AppProvider**: State management using Provider pattern
3. **Models**: Data models for SMS, CallLog, and Contact
4. **HomeScreen**: Main UI with 5 tabs (Dashboard, SMS, Calls, Contacts, Blocked)

## Permissions Required

The app requests the following Android permissions:

- `RECEIVE_SMS` - Receive SMS messages
- `READ_SMS` - Read SMS messages
- `SEND_SMS` - Send SMS messages (future feature)
- `READ_PHONE_STATE` - Monitor phone state
- `READ_CALL_LOG` - Access call logs
- `READ_CONTACTS` - Access contacts
- `WRITE_CONTACTS` - Modify contacts
- `FOREGROUND_SERVICE` - Run background service
- `WAKE_LOCK` - Keep device awake for monitoring

## Installation

1. Clone the repository
2. Navigate to the project directory
3. Run `flutter pub get` to install dependencies
4. Connect an Android device or start an emulator
5. Run `flutter run` or `flutter build apk` to install

## Usage

### First Launch
1. Grant all requested permissions when prompted
2. The app will automatically load SMS messages, call logs, and contacts
3. Use the service toggle in the app bar to start/stop the blocking service

### Dashboard Tab
- View summary statistics (SMS count, call logs, contacts, blocked numbers)
- Monitor service status and permissions
- Request permissions if not granted

### SMS Tab
- View all SMS messages with sender and content
- See message type (sent/received) and timestamp
- Block numbers directly from SMS list

### Calls Tab
- View call history with type indicators (incoming/outgoing/missed)
- See call duration and timestamps
- Block numbers from call history

### Contacts Tab
- Browse device contacts
- Block/unblock contacts easily

### Blocked Tab
- View all blocked numbers
- Unblock numbers as needed

## Technical Details

### Method Channels
The app uses Flutter's MethodChannel for communication between Dart and Java:
- Channel name: `com.example.spy3/native`
- Supported methods: `requestPermissions`, `getSmsMessages`, `getCallLogs`, `getContacts`, `blockNumber`, `unblockNumber`, `getBlockedNumbers`, `startBlockingService`, `stopBlockingService`

### Database
Blocked numbers are stored using:
- SharedPreferences for simple storage
- SQLite database via ContentProvider for advanced features

### Background Monitoring
The BlockingService runs as a foreground service and:
- Registers broadcast receivers dynamically
- Shows persistent notification while active
- Monitors incoming calls and SMS
- Blocks numbers based on the blocked list

## Limitations

1. **Call Blocking**: True call rejection requires system-level permissions or root access. The current implementation detects blocked calls but cannot automatically reject them.

2. **SMS Blocking**: SMS blocking works by intercepting messages before they reach the inbox, but requires the app to be set as the default SMS app on Android 4.4+.

3. **Permissions**: Some permissions may require manual enabling in device settings for full functionality.

## Development Notes

- The app targets Android API level with support for modern Android versions
- Uses Material Design 3 for modern UI
- Implements proper error handling and user feedback
- Follows Android development best practices for security and performance

## Future Enhancements

- Implement actual call rejection (requires system privileges)
- Add SMS filtering and auto-reply features
- Include call recording capabilities
- Add backup/restore for blocked numbers
- Implement whitelist functionality
- Add scheduled blocking (time-based rules)

## Building

To build a release APK:
```bash
flutter build apk --release
```

To build for specific architecture:
```bash
flutter build apk --target-platform android-arm64
```

## Testing

The application has been tested for:
- ✅ Permissions handling
- ✅ UI navigation and functionality
- ✅ Method channel communication
- ✅ Service lifecycle management
- ✅ Database operations

## Support

For issues or questions, please refer to the Flutter and Android documentation for method channels and native development.
