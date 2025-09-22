class SmsMessage {
  final String address;
  final String body;
  final int date;
  final int type; // 1 = received, 2 = sent

  SmsMessage({
    required this.address,
    required this.body,
    required this.date,
    required this.type,
  });

  factory SmsMessage.fromMap(Map<String, dynamic> map) {
    return SmsMessage(
      address: map['address'] ?? '',
      body: map['body'] ?? '',
      date: map['date'] ?? 0,
      type: map['type'] ?? 1,
    );
  }

  String get formattedDate {
    final DateTime dateTime = DateTime.fromMillisecondsSinceEpoch(date);
    return '${dateTime.day}/${dateTime.month}/${dateTime.year} ${dateTime.hour}:${dateTime.minute.toString().padLeft(2, '0')}';
  }

  String get typeString {
    return type == 2 ? 'Sent' : 'Received';
  }
}

class CallLog {
  final String number;
  final int date;
  final int duration;
  final int type; // 1 = incoming, 2 = outgoing, 3 = missed

  CallLog({
    required this.number,
    required this.date,
    required this.duration,
    required this.type,
  });

  factory CallLog.fromMap(Map<String, dynamic> map) {
    return CallLog(
      number: map['number'] ?? '',
      date: map['date'] ?? 0,
      duration: map['duration'] ?? 0,
      type: map['type'] ?? 1,
    );
  }

  String get formattedDate {
    final DateTime dateTime = DateTime.fromMillisecondsSinceEpoch(date);
    return '${dateTime.day}/${dateTime.month}/${dateTime.year} ${dateTime.hour}:${dateTime.minute.toString().padLeft(2, '0')}';
  }

  String get typeString {
    switch (type) {
      case 2:
        return 'Outgoing';
      case 3:
        return 'Missed';
      default:
        return 'Incoming';
    }
  }

  String get formattedDuration {
    final int minutes = duration ~/ 60;
    final int seconds = duration % 60;
    return '${minutes}m ${seconds}s';
  }
}

class Contact {
  final String name;
  final String number;

  Contact({required this.name, required this.number});

  factory Contact.fromMap(Map<String, dynamic> map) {
    return Contact(name: map['name'] ?? '', number: map['number'] ?? '');
  }
}
