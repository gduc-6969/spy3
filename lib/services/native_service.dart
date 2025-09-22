import 'package:flutter/services.dart';

class NativeService {
  static const MethodChannel _channel = MethodChannel(
    'com.example.spy3/native',
  );

  // Request all necessary permissions
  static Future<bool> requestPermissions() async {
    try {
      final bool result = await _channel.invokeMethod('requestPermissions');
      return result;
    } catch (e) {
      print('Error requesting permissions: $e');
      return false;
    }
  }

  // Get SMS messages
  static Future<List<Map<String, dynamic>>> getSmsMessages() async {
    try {
      final List<dynamic> result = await _channel.invokeMethod(
        'getSmsMessages',
      );
      return result.map((e) => Map<String, dynamic>.from(e)).toList();
    } catch (e) {
      print('Error getting SMS messages: $e');
      return [];
    }
  }

  // Get call logs
  static Future<List<Map<String, dynamic>>> getCallLogs() async {
    try {
      final List<dynamic> result = await _channel.invokeMethod('getCallLogs');
      return result.map((e) => Map<String, dynamic>.from(e)).toList();
    } catch (e) {
      print('Error getting call logs: $e');
      return [];
    }
  }

  // Get contacts
  static Future<List<Map<String, dynamic>>> getContacts() async {
    try {
      final List<dynamic> result = await _channel.invokeMethod('getContacts');
      return result.map((e) => Map<String, dynamic>.from(e)).toList();
    } catch (e) {
      print('Error getting contacts: $e');
      return [];
    }
  }

  // Block a number
  static Future<bool> blockNumber(String number) async {
    try {
      final bool result = await _channel.invokeMethod('blockNumber', {
        'number': number,
      });
      return result;
    } catch (e) {
      print('Error blocking number: $e');
      return false;
    }
  }

  // Unblock a number
  static Future<bool> unblockNumber(String number) async {
    try {
      final bool result = await _channel.invokeMethod('unblockNumber', {
        'number': number,
      });
      return result;
    } catch (e) {
      print('Error unblocking number: $e');
      return false;
    }
  }

  // Get blocked numbers
  static Future<List<String>> getBlockedNumbers() async {
    try {
      final List<dynamic> result = await _channel.invokeMethod(
        'getBlockedNumbers',
      );
      return result.map((e) => e.toString()).toList();
    } catch (e) {
      print('Error getting blocked numbers: $e');
      return [];
    }
  }

  // Start blocking service
  static Future<bool> startBlockingService() async {
    try {
      final bool result = await _channel.invokeMethod('startBlockingService');
      return result;
    } catch (e) {
      print('Error starting blocking service: $e');
      return false;
    }
  }

  // Stop blocking service
  static Future<bool> stopBlockingService() async {
    try {
      final bool result = await _channel.invokeMethod('stopBlockingService');
      return result;
    } catch (e) {
      print('Error stopping blocking service: $e');
      return false;
    }
  }

  // Enable call screening (Android 10+)
  static Future<bool> enableCallScreening() async {
    try {
      final bool result = await _channel.invokeMethod('enableCallScreening');
      return result;
    } catch (e) {
      print('Error enabling call screening: $e');
      return false;
    }
  }
}
