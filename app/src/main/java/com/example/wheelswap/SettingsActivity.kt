package com.example.wheelswap

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth

class SettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LanguageHelper.applyLanguage(this)
        setContent {
            SettingsScreen(onBack = { finish() })
        }
    }
    
    fun restartActivity() {
        recreate()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val auth = FirebaseAuth.getInstance()
    val context = LocalContext.current
    val activity = context as? SettingsActivity
    val prefs = context.getSharedPreferences("Settings", Context.MODE_PRIVATE)
    
    var notificationsEnabled by rememberSaveable { mutableStateOf(prefs.getBoolean("Notifications", true)) }
    var locationSharing by rememberSaveable { mutableStateOf(prefs.getBoolean("LocationSharing", true)) }
    var showLanguageDialog by remember { mutableStateOf(false) }

    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text(stringResource(R.string.select_language_dialog), color = Gold) },
            containerColor = CardBg,
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text(stringResource(R.string.back), color = Gold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    LanguageOption("English", "en") {
                        changeLanguage(context, "en")
                        showLanguageDialog = false
                        activity?.restartActivity()
                    }
                    LanguageOption("اردو", "ur") {
                        changeLanguage(context, "ur")
                        showLanguageDialog = false
                        activity?.restartActivity()
                    }
                }
            }
        )
    }

    Scaffold(
        containerColor = DarkBg,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title), color = Gold, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back), tint = Gold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBg)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── PROFILE SECTION ──
            SettingsHeader(stringResource(R.string.account_profile))
            SettingsItem(
                icon = Icons.Default.Person,
                title = stringResource(R.string.edit_profile),
                subtitle = auth.currentUser?.email ?: "User",
                onClick = { context.startActivity(Intent(context, EditProfileActivity::class.java)) }
            )
            SettingsItem(
                icon = Icons.Default.Lock,
                title = stringResource(R.string.change_password),
                onClick = { /* Logic for password reset email */ }
            )

            // ── PREFERENCES ──
            SettingsHeader(stringResource(R.string.app_preferences))
            SettingsToggleItem(
                icon = Icons.Default.Notifications,
                title = stringResource(R.string.push_notifications),
                subtitle = stringResource(R.string.notifications_subtitle),
                checked = notificationsEnabled,
                onCheckedChange = { 
                    notificationsEnabled = it
                    prefs.edit().putBoolean("Notifications", it).commit()
                }
            )
            SettingsToggleItem(
                icon = Icons.Default.LocationOn,
                title = stringResource(R.string.location_services),
                subtitle = stringResource(R.string.location_subtitle),
                checked = locationSharing,
                onCheckedChange = { 
                    locationSharing = it
                    prefs.edit().putBoolean("LocationSharing", it).commit()
                }
            )
            SettingsItem(
                icon = Icons.Default.Language,
                title = stringResource(R.string.language),
                subtitle = stringResource(R.string.language_subtitle),
                onClick = { showLanguageDialog = true }
            )

            // ── MARKETPLACE SETTINGS ──
            SettingsHeader(stringResource(R.string.marketplace))
            SettingsItem(
                icon = Icons.Default.List,
                title = stringResource(R.string.manage_listings),
                onClick = { onBack() }
            )
            SettingsItem(
                icon = Icons.Default.History,
                title = stringResource(R.string.transaction_history),
                onClick = { /* Navigate to History */ }
            )

            // ── DANGER ZONE ──
            SettingsHeader(stringResource(R.string.danger_zone))
            SettingsItem(
                icon = Icons.Default.Delete,
                title = stringResource(R.string.deactivate_account),
                titleColor = Color.Red,
                onClick = { /* Account deactivation logic */ }
            )

            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = stringResource(R.string.app_version),
                color = Color.Gray,
                fontSize = 12.sp,
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
fun LanguageOption(label: String, code: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(containerColor = CardBg),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Gold.copy(alpha = 0.3f))
    ) {
        Text(text = label, color = Color.White, fontWeight = FontWeight.Bold)
    }
}

fun changeLanguage(context: Context, lang: String) {
    val prefs = context.getSharedPreferences("Settings", Context.MODE_PRIVATE)
    prefs.edit().putString("Language", lang).apply()
    LanguageHelper.applyLanguage(context)
}

@Composable
fun SettingsHeader(title: String) {
    Text(
        text = title,
        color = Gold,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    titleColor: Color = Color.White,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = CardBg),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = Gold, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = title, color = titleColor, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                if (subtitle != null) {
                    Text(text = subtitle, color = Color.Gray, fontSize = 12.sp)
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = Color.Gray)
        }
    }
}

@Composable
fun SettingsToggleItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = Gold, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                Text(text = subtitle, color = Color.Gray, fontSize = 12.sp)
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(checkedThumbColor = Gold, checkedTrackColor = Gold.copy(alpha = 0.5f))
            )
        }
    }
}
