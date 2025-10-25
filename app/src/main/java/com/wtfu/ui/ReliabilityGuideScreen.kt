package com.wtfu.ui

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

/**
 * Screen with instructions to improve alarm reliability.
 * Includes battery optimization exemption and OEM-specific guidance.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReliabilityGuideScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Improve Reliability") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "For WTFU to work reliably, you need to:",
                style = MaterialTheme.typography.titleMedium
            )

            // Step 1: Exact Alarms
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("1. Allow Exact Alarms", style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = "On Android 12+, WTFU needs permission to schedule exact alarms.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        Button(
                            onClick = {
                                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                                    data = Uri.parse("package:${context.packageName}")
                                }
                                context.startActivity(intent)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Open Exact Alarm Settings")
                        }
                    }
                }
            }

            // Step 2: Battery Optimization
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("2. Disable Battery Optimization", style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = "Allow WTFU to run in the background without restrictions.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Button(
                        onClick = {
                            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                                data = Uri.parse("package:${context.packageName}")
                            }
                            context.startActivity(intent)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Open Battery Settings")
                    }
                }
            }

            // Step 3: OEM-Specific Settings
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("3. OEM Battery Killers", style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = "Some manufacturers have aggressive battery management. " +
                                "Please check if your device needs additional settings:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    Text("\n• Samsung: Settings → Apps → WTFU → Battery → Allow background activity", 
                        style = MaterialTheme.typography.bodySmall)
                    Text("• Xiaomi: Settings → Apps → Manage apps → WTFU → Battery saver → No restrictions", 
                        style = MaterialTheme.typography.bodySmall)
                    Text("• Huawei: Settings → Apps → WTFU → Battery → App launch → Manage manually (enable all)", 
                        style = MaterialTheme.typography.bodySmall)
                    Text("• OnePlus: Settings → Battery → Battery optimization → WTFU → Don't optimize", 
                        style = MaterialTheme.typography.bodySmall)
                    Text("• Oppo/Realme: Settings → Battery → Power saving mode → Turn off for WTFU", 
                        style = MaterialTheme.typography.bodySmall)
                    
                    Button(
                        onClick = {
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.parse("package:${context.packageName}")
                            }
                            context.startActivity(intent)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Open App Settings")
                    }
                }
            }

            // Step 4: Notifications
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("4. Allow Notifications", style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = "WTFU needs notification permission to show the alarm notification.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            // Additional Tips
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("💡 Additional Tips", style = MaterialTheme.typography.titleMedium)
                    Text("• Keep WTFU open in recent apps (don't swipe it away)", 
                        style = MaterialTheme.typography.bodyMedium)
                    Text("• Test the alarm during the day before relying on it", 
                        style = MaterialTheme.typography.bodyMedium)
                    Text("• Enable 'Re-schedule after reboot' if you restart your phone often", 
                        style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}
