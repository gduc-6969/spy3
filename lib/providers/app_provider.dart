import 'package:flutter/foundation.dart';
import '../models/models.dart';
import '../services/native_service.dart';

class AppProvider extends ChangeNotifier {
  List<SmsMessage> _smsMessages = [];
  List<CallLog> _callLogs = [];
  List<Contact> _contacts = [];
  List<App> _apps = [];
  List<String> _blockedNumbers = [];
  bool _isLoading = false;
  bool _serviceRunning = false;
  bool _permissionsGranted = false;

  // Getters
  List<SmsMessage> get smsMessages => _smsMessages;
  List<CallLog> get callLogs => _callLogs;
  List<Contact> get contacts => _contacts;
  List<App> get apps => _apps;
  List<String> get blockedNumbers => _blockedNumbers;
  bool get isLoading => _isLoading;
  bool get serviceRunning => _serviceRunning;
  bool get permissionsGranted => _permissionsGranted;

  // Request permissions
  Future<void> requestPermissions() async {
    _isLoading = true;
    notifyListeners();

    try {
      _permissionsGranted = await NativeService.requestPermissions();
    } catch (e) {
      print('Error requesting permissions: $e');
      _permissionsGranted = false;
    }

    _isLoading = false;
    notifyListeners();
  }

  // Load SMS messages
  Future<void> loadSmsMessages() async {
    if (!_permissionsGranted) return;

    _isLoading = true;
    notifyListeners();

    try {
      final List<Map<String, dynamic>> data =
          await NativeService.getSmsMessages();
      _smsMessages = data.map((e) => SmsMessage.fromMap(e)).toList();
    } catch (e) {
      print('Error loading SMS messages: $e');
    }

    _isLoading = false;
    notifyListeners();
  }

  // Load call logs
  Future<void> loadCallLogs() async {
    if (!_permissionsGranted) return;

    _isLoading = true;
    notifyListeners();

    try {
      final List<Map<String, dynamic>> data = await NativeService.getCallLogs();
      _callLogs = data.map((e) => CallLog.fromMap(e)).toList();
    } catch (e) {
      print('Error loading call logs: $e');
    }

    _isLoading = false;
    notifyListeners();
  }

  // Load contacts
  Future<void> loadContacts() async {
    if (!_permissionsGranted) return;

    _isLoading = true;
    notifyListeners();

    try {
      final List<Map<String, dynamic>> data = await NativeService.getContacts();
      _contacts = data.map((e) => Contact.fromMap(e)).toList();
    } catch (e) {
      print('Error loading contacts: $e');
    }

    _isLoading = false;
    notifyListeners();
  }

  // Load installed apps
  Future<void> loadApps() async {
    _isLoading = true;
    notifyListeners();

    try {
      final List<Map<String, dynamic>> data =
          await NativeService.getInstalledApps();
      _apps = data.map((e) => App.fromMap(e)).toList();
      // Sort apps alphabetically by name
      _apps.sort(
        (a, b) => a.appName.toLowerCase().compareTo(b.appName.toLowerCase()),
      );
    } catch (e) {
      print('Error loading apps: $e');
    }

    _isLoading = false;
    notifyListeners();
  }

  // Load blocked numbers
  Future<void> loadBlockedNumbers() async {
    _isLoading = true;
    notifyListeners();

    try {
      _blockedNumbers = await NativeService.getBlockedNumbers();
    } catch (e) {
      print('Error loading blocked numbers: $e');
    }

    _isLoading = false;
    notifyListeners();
  }

  // Block a number
  Future<bool> blockNumber(String number) async {
    try {
      final bool success = await NativeService.blockNumber(number);
      if (success) {
        _blockedNumbers.add(number);
        notifyListeners();
      }
      return success;
    } catch (e) {
      print('Error blocking number: $e');
      return false;
    }
  }

  // Unblock a number
  Future<bool> unblockNumber(String number) async {
    try {
      final bool success = await NativeService.unblockNumber(number);
      if (success) {
        _blockedNumbers.remove(number);
        notifyListeners();
      }
      return success;
    } catch (e) {
      print('Error unblocking number: $e');
      return false;
    }
  }

  // Start blocking service
  Future<bool> startBlockingService() async {
    try {
      final bool success = await NativeService.startBlockingService();
      if (success) {
        _serviceRunning = true;
        notifyListeners();
      }
      return success;
    } catch (e) {
      print('Error starting blocking service: $e');
      return false;
    }
  }

  // Stop blocking service
  Future<bool> stopBlockingService() async {
    try {
      final bool success = await NativeService.stopBlockingService();
      if (success) {
        _serviceRunning = false;
        notifyListeners();
      }
      return success;
    } catch (e) {
      print('Error stopping blocking service: $e');
      return false;
    }
  }

  // Check if a number is blocked
  bool isNumberBlocked(String number) {
    return _blockedNumbers.contains(number);
  }

  // Enable call screening (Android 10+)
  Future<bool> enableCallScreening() async {
    try {
      final bool success = await NativeService.enableCallScreening();
      return success;
    } catch (e) {
      print('Error enabling call screening: $e');
      return false;
    }
  }

  // Load all data
  Future<void> loadAllData() async {
    if (!_permissionsGranted) {
      await requestPermissions();
    }

    if (_permissionsGranted) {
      await Future.wait([
        loadSmsMessages(),
        loadCallLogs(),
        loadContacts(),
        loadBlockedNumbers(),
      ]);
    }

    // Load apps separately as it doesn't require special permissions
    await loadApps();
  }
}
