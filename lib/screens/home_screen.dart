import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../providers/app_provider.dart';
import '../models/models.dart';

class HomeScreen extends StatefulWidget {
  const HomeScreen({super.key});

  @override
  State<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> {
  int _currentIndex = 0;

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      context.read<AppProvider>().loadAllData();
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Spy3 - Call & SMS Manager'),
        backgroundColor: Colors.blue,
        foregroundColor: Colors.white,
        actions: [
          Consumer<AppProvider>(
            builder: (context, provider, child) {
              return IconButton(
                icon: Icon(
                  provider.serviceRunning
                      ? Icons.security
                      : Icons.security_outlined,
                  color: provider.serviceRunning ? Colors.green : Colors.red,
                ),
                onPressed: () async {
                  if (provider.serviceRunning) {
                    await provider.stopBlockingService();
                    ScaffoldMessenger.of(context).showSnackBar(
                      const SnackBar(content: Text('Blocking service stopped')),
                    );
                  } else {
                    if (provider.permissionsGranted) {
                      await provider.startBlockingService();
                      ScaffoldMessenger.of(context).showSnackBar(
                        const SnackBar(
                          content: Text('Blocking service started'),
                        ),
                      );
                    } else {
                      ScaffoldMessenger.of(context).showSnackBar(
                        const SnackBar(
                          content: Text('Please grant permissions first'),
                        ),
                      );
                    }
                  }
                },
              );
            },
          ),
        ],
      ),
      body: IndexedStack(
        index: _currentIndex,
        children: const [
          DashboardTab(),
          SmsTab(),
          CallsTab(),
          ContactsTab(),
          AppsTab(),
          BlockedTab(),
        ],
      ),
      bottomNavigationBar: BottomNavigationBar(
        type: BottomNavigationBarType.fixed,
        currentIndex: _currentIndex,
        onTap: (index) => setState(() => _currentIndex = index),
        items: const [
          BottomNavigationBarItem(
            icon: Icon(Icons.dashboard),
            label: 'Dashboard',
          ),
          BottomNavigationBarItem(icon: Icon(Icons.sms), label: 'SMS'),
          BottomNavigationBarItem(icon: Icon(Icons.call), label: 'Calls'),
          BottomNavigationBarItem(
            icon: Icon(Icons.contacts),
            label: 'Contacts',
          ),
          BottomNavigationBarItem(icon: Icon(Icons.apps), label: 'Apps'),
          BottomNavigationBarItem(icon: Icon(Icons.block), label: 'Blocked'),
        ],
      ),
    );
  }
}

class DashboardTab extends StatelessWidget {
  const DashboardTab({super.key});

  @override
  Widget build(BuildContext context) {
    return Consumer<AppProvider>(
      builder: (context, provider, child) {
        return Padding(
          padding: const EdgeInsets.all(16.0),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Card(
                child: Padding(
                  padding: const EdgeInsets.all(16.0),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        'Status',
                        style: Theme.of(context).textTheme.headlineSmall,
                      ),
                      const SizedBox(height: 8),
                      Row(
                        children: [
                          Icon(
                            provider.permissionsGranted
                                ? Icons.check_circle
                                : Icons.error,
                            color: provider.permissionsGranted
                                ? Colors.green
                                : Colors.red,
                          ),
                          const SizedBox(width: 8),
                          Text(
                            'Permissions: ${provider.permissionsGranted ? 'Granted' : 'Not Granted'}',
                          ),
                        ],
                      ),
                      const SizedBox(height: 8),
                      Row(
                        children: [
                          Icon(
                            provider.serviceRunning
                                ? Icons.check_circle
                                : Icons.error,
                            color: provider.serviceRunning
                                ? Colors.green
                                : Colors.red,
                          ),
                          const SizedBox(width: 8),
                          Text(
                            'Service: ${provider.serviceRunning ? 'Running' : 'Stopped'}',
                          ),
                        ],
                      ),
                    ],
                  ),
                ),
              ),
              const SizedBox(height: 16),
              if (!provider.permissionsGranted)
                SizedBox(
                  width: double.infinity,
                  child: ElevatedButton(
                    onPressed: provider.isLoading
                        ? null
                        : () => provider.requestPermissions(),
                    child: provider.isLoading
                        ? const CircularProgressIndicator()
                        : const Text('Request Permissions'),
                  ),
                ),
              if (provider.permissionsGranted)
                SizedBox(
                  width: double.infinity,
                  child: ElevatedButton(
                    onPressed: () async {
                      final success = await provider.enableCallScreening();
                      if (success) {
                        ScaffoldMessenger.of(context).showSnackBar(
                          const SnackBar(
                            content: Text(
                              'Call screening enabled successfully',
                            ),
                          ),
                        );
                      } else {
                        ScaffoldMessenger.of(context).showSnackBar(
                          const SnackBar(
                            content: Text(
                              'Call screening not available or already enabled',
                            ),
                          ),
                        );
                      }
                    },
                    style: ElevatedButton.styleFrom(
                      backgroundColor: Colors.orange,
                      foregroundColor: Colors.white,
                    ),
                    child: const Text('Enable Advanced Call Blocking'),
                  ),
                ),
              if (provider.permissionsGranted) const SizedBox(height: 8),
              if (provider.permissionsGranted)
                SizedBox(
                  width: double.infinity,
                  child: ElevatedButton(
                    onPressed: () async {
                      await provider.loadAllData();
                      ScaffoldMessenger.of(context).showSnackBar(
                        const SnackBar(content: Text('All data refreshed')),
                      );
                    },
                    style: ElevatedButton.styleFrom(
                      backgroundColor: Colors.green,
                      foregroundColor: Colors.white,
                    ),
                    child: const Text('Get All Data'),
                  ),
                ),
              const SizedBox(height: 16),
              Expanded(
                child: GridView.count(
                  crossAxisCount: 3,
                  crossAxisSpacing: 12,
                  mainAxisSpacing: 12,
                  childAspectRatio: 0.8,
                  children: [
                    _buildStatCard(
                      context,
                      'SMS',
                      provider.smsMessages.length.toString(),
                      Icons.sms,
                      Colors.blue,
                    ),
                    _buildStatCard(
                      context,
                      'Calls',
                      provider.callLogs.length.toString(),
                      Icons.call,
                      Colors.green,
                    ),
                    _buildStatCard(
                      context,
                      'Contacts',
                      provider.contacts.length.toString(),
                      Icons.contacts,
                      Colors.orange,
                    ),
                    _buildStatCard(
                      context,
                      'Apps',
                      provider.apps.length.toString(),
                      Icons.apps,
                      Colors.purple,
                    ),
                    _buildStatCard(
                      context,
                      'Blocked',
                      provider.blockedNumbers.length.toString(),
                      Icons.block,
                      Colors.red,
                    ),
                  ],
                ),
              ),
            ],
          ),
        );
      },
    );
  }

  Widget _buildStatCard(
    BuildContext context,
    String title,
    String count,
    IconData icon,
    Color color,
  ) {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(12.0),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          mainAxisSize: MainAxisSize.min,
          children: [
            Icon(icon, size: 36, color: color),
            const SizedBox(height: 6),
            Text(
              count,
              style: Theme.of(context).textTheme.headlineSmall?.copyWith(
                fontWeight: FontWeight.bold,
                color: color,
              ),
            ),
            const SizedBox(height: 2),
            Text(
              title,
              textAlign: TextAlign.center,
              maxLines: 2,
              overflow: TextOverflow.ellipsis,
              style: Theme.of(context).textTheme.bodySmall,
            ),
          ],
        ),
      ),
    );
  }
}

class SmsTab extends StatelessWidget {
  const SmsTab({super.key});

  @override
  Widget build(BuildContext context) {
    return Consumer<AppProvider>(
      builder: (context, provider, child) {
        return Scaffold(
          body: _buildBody(context, provider),
          floatingActionButton: FloatingActionButton(
            onPressed: () async {
              await provider.loadSmsMessages();
              ScaffoldMessenger.of(context).showSnackBar(
                const SnackBar(content: Text('SMS messages refreshed')),
              );
            },
            child: const Icon(Icons.refresh),
            tooltip: 'Get SMS Messages',
          ),
        );
      },
    );
  }

  Widget _buildBody(BuildContext context, AppProvider provider) {
    if (provider.isLoading) {
      return const Center(child: CircularProgressIndicator());
    }

    if (provider.smsMessages.isEmpty) {
      return const Center(child: Text('No SMS messages found'));
    }

    return ListView.builder(
      itemCount: provider.smsMessages.length,
      itemBuilder: (context, index) {
        final sms = provider.smsMessages[index];
        final isBlocked = provider.isNumberBlocked(sms.address);

        return ListTile(
          leading: CircleAvatar(
            backgroundColor: sms.type == 2 ? Colors.blue : Colors.green,
            child: Icon(
              sms.type == 2 ? Icons.send : Icons.inbox,
              color: Colors.white,
            ),
          ),
          title: Text(sms.address),
          subtitle: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(sms.body),
              Text(
                '${sms.typeString} • ${sms.formattedDate}',
                style: Theme.of(context).textTheme.bodySmall,
              ),
            ],
          ),
          trailing: isBlocked
              ? Icon(Icons.block, color: Colors.red)
              : IconButton(
                  icon: const Icon(Icons.block),
                  onPressed: () => _showBlockDialog(context, sms.address),
                ),
        );
      },
    );
  }

  void _showBlockDialog(BuildContext context, String number) {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Block Number'),
        content: Text('Do you want to block $number?'),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: const Text('Cancel'),
          ),
          TextButton(
            onPressed: () async {
              await context.read<AppProvider>().blockNumber(number);
              Navigator.pop(context);
              ScaffoldMessenger.of(
                context,
              ).showSnackBar(SnackBar(content: Text('Blocked $number')));
            },
            child: const Text('Block'),
          ),
        ],
      ),
    );
  }
}

class CallsTab extends StatelessWidget {
  const CallsTab({super.key});

  @override
  Widget build(BuildContext context) {
    return Consumer<AppProvider>(
      builder: (context, provider, child) {
        return Scaffold(
          body: _buildBody(context, provider),
          floatingActionButton: FloatingActionButton(
            onPressed: () async {
              await provider.loadCallLogs();
              ScaffoldMessenger.of(context).showSnackBar(
                const SnackBar(content: Text('Call logs refreshed')),
              );
            },
            child: const Icon(Icons.refresh),
            tooltip: 'Get Call Logs',
          ),
        );
      },
    );
  }

  Widget _buildBody(BuildContext context, AppProvider provider) {
    if (provider.isLoading) {
      return const Center(child: CircularProgressIndicator());
    }

    if (provider.callLogs.isEmpty) {
      return const Center(child: Text('No call logs found'));
    }

    return ListView.builder(
      itemCount: provider.callLogs.length,
      itemBuilder: (context, index) {
        final call = provider.callLogs[index];
        final isBlocked = provider.isNumberBlocked(call.number);

        return ListTile(
          leading: CircleAvatar(
            backgroundColor: _getCallTypeColor(call.type),
            child: Icon(_getCallTypeIcon(call.type), color: Colors.white),
          ),
          title: Text(call.number),
          subtitle: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text('${call.typeString} • ${call.formattedDuration}'),
              Text(
                call.formattedDate,
                style: Theme.of(context).textTheme.bodySmall,
              ),
            ],
          ),
          trailing: isBlocked
              ? Icon(Icons.block, color: Colors.red)
              : IconButton(
                  icon: const Icon(Icons.block),
                  onPressed: () => _showBlockDialog(context, call.number),
                ),
        );
      },
    );
  }

  Color _getCallTypeColor(int type) {
    switch (type) {
      case 2:
        return Colors.blue; // Outgoing
      case 3:
        return Colors.red; // Missed
      default:
        return Colors.green; // Incoming
    }
  }

  IconData _getCallTypeIcon(int type) {
    switch (type) {
      case 2:
        return Icons.call_made; // Outgoing
      case 3:
        return Icons.call_received; // Missed
      default:
        return Icons.call_received; // Incoming
    }
  }

  void _showBlockDialog(BuildContext context, String number) {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Block Number'),
        content: Text('Do you want to block $number?'),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: const Text('Cancel'),
          ),
          TextButton(
            onPressed: () async {
              await context.read<AppProvider>().blockNumber(number);
              Navigator.pop(context);
              ScaffoldMessenger.of(
                context,
              ).showSnackBar(SnackBar(content: Text('Blocked $number')));
            },
            child: const Text('Block'),
          ),
        ],
      ),
    );
  }
}

class ContactsTab extends StatelessWidget {
  const ContactsTab({super.key});

  @override
  Widget build(BuildContext context) {
    return Consumer<AppProvider>(
      builder: (context, provider, child) {
        return Scaffold(
          body: _buildBody(context, provider),
          floatingActionButton: FloatingActionButton(
            onPressed: () async {
              await provider.loadContacts();
              ScaffoldMessenger.of(context).showSnackBar(
                const SnackBar(content: Text('Contacts refreshed')),
              );
            },
            child: const Icon(Icons.refresh),
            tooltip: 'Get Contacts',
          ),
        );
      },
    );
  }

  Widget _buildBody(BuildContext context, AppProvider provider) {
    if (provider.isLoading) {
      return const Center(child: CircularProgressIndicator());
    }

    if (provider.contacts.isEmpty) {
      return const Center(child: Text('No contacts found'));
    }

    return ListView.builder(
      itemCount: provider.contacts.length,
      itemBuilder: (context, index) {
        final contact = provider.contacts[index];
        final isBlocked = provider.isNumberBlocked(contact.number);

        return ListTile(
          leading: CircleAvatar(
            child: Text(
              contact.name.isNotEmpty ? contact.name[0].toUpperCase() : '?',
            ),
          ),
          title: Text(contact.name),
          subtitle: Text(contact.number),
          trailing: isBlocked
              ? Icon(Icons.block, color: Colors.red)
              : IconButton(
                  icon: const Icon(Icons.block),
                  onPressed: () => _showBlockDialog(context, contact.number),
                ),
        );
      },
    );
  }

  void _showBlockDialog(BuildContext context, String number) {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Block Number'),
        content: Text('Do you want to block $number?'),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: const Text('Cancel'),
          ),
          TextButton(
            onPressed: () async {
              await context.read<AppProvider>().blockNumber(number);
              Navigator.pop(context);
              ScaffoldMessenger.of(
                context,
              ).showSnackBar(SnackBar(content: Text('Blocked $number')));
            },
            child: const Text('Block'),
          ),
        ],
      ),
    );
  }
}

class BlockedTab extends StatelessWidget {
  const BlockedTab({super.key});

  @override
  Widget build(BuildContext context) {
    return Consumer<AppProvider>(
      builder: (context, provider, child) {
        if (provider.isLoading) {
          return const Center(child: CircularProgressIndicator());
        }

        if (provider.blockedNumbers.isEmpty) {
          return const Center(child: Text('No blocked numbers'));
        }

        return ListView.builder(
          itemCount: provider.blockedNumbers.length,
          itemBuilder: (context, index) {
            final number = provider.blockedNumbers[index];

            return ListTile(
              leading: const CircleAvatar(
                backgroundColor: Colors.red,
                child: Icon(Icons.block, color: Colors.white),
              ),
              title: Text(number),
              subtitle: const Text('Blocked number'),
              trailing: IconButton(
                icon: const Icon(Icons.delete),
                onPressed: () => _showUnblockDialog(context, number),
              ),
            );
          },
        );
      },
    );
  }

  void _showUnblockDialog(BuildContext context, String number) {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Unblock Number'),
        content: Text('Do you want to unblock $number?'),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: const Text('Cancel'),
          ),
          TextButton(
            onPressed: () async {
              await context.read<AppProvider>().unblockNumber(number);
              Navigator.pop(context);
              ScaffoldMessenger.of(
                context,
              ).showSnackBar(SnackBar(content: Text('Unblocked $number')));
            },
            child: const Text('Unblock'),
          ),
        ],
      ),
    );
  }
}

class AppsTab extends StatelessWidget {
  const AppsTab({super.key});

  @override
  Widget build(BuildContext context) {
    return Consumer<AppProvider>(
      builder: (context, provider, child) {
        return Scaffold(
          body: _buildBody(context, provider),
          floatingActionButton: FloatingActionButton(
            onPressed: () async {
              await provider.loadApps();
              ScaffoldMessenger.of(context).showSnackBar(
                const SnackBar(content: Text('Apps list refreshed')),
              );
            },
            child: const Icon(Icons.refresh),
            tooltip: 'Get Installed Apps',
          ),
        );
      },
    );
  }

  Widget _buildBody(BuildContext context, AppProvider provider) {
    if (provider.isLoading) {
      return const Center(child: CircularProgressIndicator());
    }

    if (provider.apps.isEmpty) {
      return const Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Icon(Icons.apps, size: 64, color: Colors.grey),
            SizedBox(height: 16),
            Text(
              'No apps found',
              style: TextStyle(fontSize: 18, color: Colors.grey),
            ),
            SizedBox(height: 8),
            Text(
              'Tap the refresh button to load apps',
              style: TextStyle(color: Colors.grey),
            ),
          ],
        ),
      );
    }

    return Column(
      children: [
        Container(
          padding: const EdgeInsets.all(16),
          child: Row(
            children: [
              const Icon(Icons.apps, color: Colors.blue),
              const SizedBox(width: 8),
              Text(
                'Installed Apps (${provider.apps.length})',
                style: Theme.of(context).textTheme.titleLarge,
              ),
            ],
          ),
        ),
        Expanded(
          child: ListView.builder(
            itemCount: provider.apps.length,
            itemBuilder: (context, index) {
              final app = provider.apps[index];

              return Card(
                margin: const EdgeInsets.symmetric(horizontal: 8, vertical: 2),
                child: ListTile(
                  leading: CircleAvatar(
                    backgroundColor: app.isSystemApp
                        ? Colors.orange
                        : Colors.blue,
                    child: Icon(
                      app.isSystemApp ? Icons.settings : Icons.android,
                      color: Colors.white,
                    ),
                  ),
                  title: Text(
                    app.appName,
                    style: const TextStyle(fontWeight: FontWeight.w500),
                  ),
                  subtitle: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        app.packageName,
                        style: const TextStyle(
                          fontSize: 12,
                          color: Colors.grey,
                        ),
                      ),
                      const SizedBox(height: 2),
                      Row(
                        children: [
                          Container(
                            padding: const EdgeInsets.symmetric(
                              horizontal: 6,
                              vertical: 2,
                            ),
                            decoration: BoxDecoration(
                              color: app.isSystemApp
                                  ? Colors.orange.withOpacity(0.2)
                                  : Colors.blue.withOpacity(0.2),
                              borderRadius: BorderRadius.circular(4),
                            ),
                            child: Text(
                              app.appType,
                              style: TextStyle(
                                fontSize: 10,
                                color: app.isSystemApp
                                    ? Colors.orange[800]
                                    : Colors.blue[800],
                                fontWeight: FontWeight.w500,
                              ),
                            ),
                          ),
                          const SizedBox(width: 8),
                          Text(
                            'v${app.version}',
                            style: const TextStyle(
                              fontSize: 11,
                              color: Colors.grey,
                            ),
                          ),
                        ],
                      ),
                    ],
                  ),
                  trailing: Column(
                    mainAxisAlignment: MainAxisAlignment.center,
                    crossAxisAlignment: CrossAxisAlignment.end,
                    children: [
                      const Icon(
                        Icons.info_outline,
                        size: 16,
                        color: Colors.grey,
                      ),
                      const SizedBox(height: 2),
                      Text(
                        app.formattedInstallDate,
                        style: const TextStyle(
                          fontSize: 10,
                          color: Colors.grey,
                        ),
                      ),
                    ],
                  ),
                  onTap: () => _showAppDetails(context, app),
                ),
              );
            },
          ),
        ),
      ],
    );
  }

  void _showAppDetails(BuildContext context, App app) {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: Text(app.appName),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            _buildDetailRow('Package Name', app.packageName),
            _buildDetailRow('Version', app.version),
            _buildDetailRow('Type', app.appType),
            _buildDetailRow('Install Date', app.formattedInstallDate),
          ],
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: const Text('Close'),
          ),
        ],
      ),
    );
  }

  Widget _buildDetailRow(String label, String value) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 4),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          SizedBox(
            width: 100,
            child: Text(
              '$label:',
              style: const TextStyle(fontWeight: FontWeight.w500),
            ),
          ),
          Expanded(child: Text(value)),
        ],
      ),
    );
  }
}
