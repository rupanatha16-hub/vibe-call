package com.vibecall.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat

class MainActivity : ComponentActivity() {
    private var refresh by mutableIntStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Prefs.init(this)
        enableEdgeToEdge()
        setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                VibeApp(refresh)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Lets the Settings screen re-check whether this app is the default phone app.
        refresh += 1
    }
}

private val NavDim = Color(0xFF9491AA)

@Composable
private fun VibeApp(refresh: Int) {
    val ctx = LocalContext.current
    val t = currentTheme()
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var settings by rememberSaveable { mutableStateOf(false) }

    val askPerms = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { Perm.tick = Perm.tick + 1 }

    LaunchedEffect(Unit) {
        val wanted = mutableListOf(
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.READ_PHONE_STATE
        )
        if (Build.VERSION.SDK_INT >= 33) wanted.add(Manifest.permission.POST_NOTIFICATIONS)
        val missing = wanted.filter {
            ContextCompat.checkSelfPermission(ctx, it) != PackageManager.PERMISSION_GRANTED
        }
        if (missing.isNotEmpty()) askPerms.launch(missing.toTypedArray())
    }

    BackHandler(enabled = settings) { settings = false }

    Column(Modifier.fillMaxSize().background(t.bg).statusBarsPadding()) {
        Box(Modifier.weight(1f).fillMaxWidth()) {
            if (settings) {
                SettingsScreen(onBack = { settings = false }, refresh = refresh)
            } else {
                when (tab) {
                    0 -> DialTab(onSettings = { settings = true })
                    1 -> ContactsTab()
                    else -> VibeTab()
                }
            }
        }
        if (!settings) {
            Row(
                Modifier.fillMaxWidth().background(t.surface).navigationBarsPadding()
                    .padding(vertical = 8.dp)
            ) {
                NavItem(VibeIcons.Keypad, "Dial", tab == 0) { tab = 0 }
                NavItem(VibeIcons.Contacts, "Contacts", tab == 1) { tab = 1 }
                NavItem(VibeIcons.Vibe, "Vibe", tab == 2) { tab = 2 }
            }
        }
    }
}

@Composable
private fun RowScope.NavItem(icon: ImageVector, label: String, selected: Boolean, onClick: () -> Unit) {
    val t = currentTheme()
    val c = if (selected) t.a else NavDim
    Column(
        Modifier.weight(1f).clip(RoundedCornerShape(16.dp)).clickable(onClick = onClick).padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, label, tint = c, modifier = Modifier.size(26.dp))
        Spacer(Modifier.height(3.dp))
        Text(label, color = c, fontSize = 11.sp)
        Spacer(Modifier.height(4.dp))
        Box(
            Modifier.size(4.dp).clip(CircleShape)
                .background(if (selected) t.a else Color.Transparent)
        )
    }
}
