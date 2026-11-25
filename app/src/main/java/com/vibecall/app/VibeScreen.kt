package com.vibecall.app

import android.app.Activity
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.telecom.TelecomManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Dim = Color(0xFF9491AA)

@Composable
internal fun ScreenHeader(sub: String, title: String, trailing: @Composable () -> Unit = {}) {
    val t = currentTheme()
    Row(
        Modifier.fillMaxWidth().padding(start = 22.dp, end = 18.dp, top = 22.dp, bottom = 8.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(7.dp).clip(CircleShape).background(Brush.linearGradient(listOf(t.a, t.b))))
                Spacer(Modifier.width(8.dp))
                Text(sub, color = Dim, fontSize = 11.sp, letterSpacing = 3.sp)
            }
            Text(title, color = Color.White, fontSize = 38.sp, fontWeight = FontWeight.SemiBold)
        }
        trailing()
    }
}

@Composable
internal fun RoundIcon(icon: ImageVector, desc: String, onClick: () -> Unit) {
    Box(
        Modifier.size(44.dp).clip(CircleShape).background(Color(0x14FFFFFF))
            .border(1.dp, Color(0x1AFFFFFF), CircleShape).clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) { Icon(icon, desc, tint = Color.White, modifier = Modifier.size(22.dp)) }
}

@Composable
internal fun PillBtn(label: String, filled: Boolean, enabled: Boolean = true, onClick: () -> Unit) {
    val t = currentTheme()
    val bg = if (filled && enabled) t.a else Color(0x22FFFFFF)
    val fg = if (filled && enabled) t.bg else if (enabled) Color.White else Dim
    Box(
        Modifier.height(44.dp).clip(RoundedCornerShape(22.dp)).background(bg)
            .clickable(enabled = enabled, onClick = onClick).padding(horizontal = 20.dp),
        contentAlignment = Alignment.Center
    ) { Text(label, color = fg, fontSize = 14.sp, fontWeight = FontWeight.SemiBold) }
}

/** A small live preview card. Tap it to select. */
@Composable
internal fun StyleCard(label: String, selected: Boolean, onClick: () -> Unit, content: @Composable () -> Unit) {
    val t = currentTheme()
    val shape = RoundedCornerShape(22.dp)
    Column(Modifier.width(112.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            Modifier.fillMaxWidth().clip(shape)
                .border(2.dp, if (selected) t.a else Color.Transparent, shape).padding(3.dp)
        ) {
            PhonePreview(Modifier.fillMaxWidth(), onClick, content)
        }
        Spacer(Modifier.height(6.dp))
        Text(
            label, color = if (selected) Color.White else Dim, fontSize = 11.sp,
            textAlign = TextAlign.Center, maxLines = 1
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text.uppercase(), color = Dim, fontSize = 11.sp, letterSpacing = 3.sp,
        modifier = Modifier.padding(start = 22.dp, top = 22.dp, bottom = 10.dp)
    )
}

@Composable
private fun StyleSection(
    title: String,
    names: List<String>,
    current: Int,
    onApply: (Int) -> Unit,
    preview: @Composable (Int) -> Unit
) {
    var pending by remember(current) { mutableIntStateOf(current) }
    SectionTitle(title)
    LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        items(names.size) { i ->
            StyleCard(names[i], pending == i, { pending = i }) { preview(i) }
        }
    }
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 22.dp).padding(top = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(names[pending.coerceIn(0, names.lastIndex)], color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Medium)
            Text(if (pending == current) "In use now" else "Preview only. Tap Apply to use it.", color = Dim, fontSize = 12.sp)
        }
        PillBtn("Apply", true, pending != current) { onApply(pending) }
    }
}

@Composable
fun VibeTab() {
    val ctx = LocalContext.current
    val t = currentTheme()
    val pick = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { u ->
        if (u != null) Prefs.setMyPhoto(ctx, u)
    }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(bottom = 24.dp)) {
        ScreenHeader("YOUR CALL. YOUR STYLE.", "Vibe")

        Row(
            Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(top = 6.dp)
                .clip(RoundedCornerShape(22.dp)).background(Color(0x0DFFFFFF)).padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Avatar("Me", Prefs.myPhoto, 60.dp, Modifier.clickable { pick.launch("image/*") })
            Column(Modifier.weight(1f).padding(horizontal = 14.dp)) {
                Text("Your profile photo", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                Text("Shown on your phone only", color = Dim, fontSize = 12.sp)
            }
            PillBtn("Change", false) { pick.launch("image/*") }
        }

        SectionTitle("Theme")
        LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(Themes.size) { i ->
                val th = Themes[i]
                val sel = Prefs.theme == i
                val shape = RoundedCornerShape(20.dp)
                Column(
                    Modifier.width(84.dp).clickable { Prefs.theme = i; Prefs.save() },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    var m = Modifier.fillMaxWidth().height(104.dp).clip(shape)
                        .background(Brush.linearGradient(listOf(th.a, th.bg)))
                    if (sel) m = m.border(2.dp, Color.White, shape)
                    Box(m, contentAlignment = Alignment.Center) {
                        Box(Modifier.size(30.dp).clip(CircleShape).background(Color(0xD9FFFFFF)))
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(th.name, color = if (sel) Color.White else Dim, fontSize = 12.sp, maxLines = 1)
                }
            }
        }

        StyleSection("Incoming call design", IncomingStyles, Prefs.incoming, { Prefs.incoming = it; Prefs.save() }) { i ->
            IncomingCallUi(i, "Rahul", "+91 98765 43210", "", {}, {})
        }

        StyleSection("Outgoing call design", OutgoingStyles, Prefs.outgoing, { Prefs.outgoing = it; Prefs.save() }) { i ->
            OutgoingCallUi(i, "Rahul", "+91 98765 43210", "", "00:42", {})
        }

        StyleSection("Call button icons", IconStyles, Prefs.iconStyle, { Prefs.iconStyle = it; Prefs.save() }) { i ->
            Box(Modifier.fillMaxSize().background(Color(0xFF0B0B10)), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(28.dp)) {
                    ControlBtn(VibeIcons.Speaker, "Speaker", true, i) {}
                    ControlBtn(VibeIcons.Mic, "Mute", false, i) {}
                    ControlBtn(VibeIcons.Record, "Record", false, i) {}
                }
            }
        }

        StyleSection("Name and number style", NumberStyles.map { it.name }, Prefs.numberStyle, { Prefs.numberStyle = it; Prefs.save() }) { i ->
            val n = NumberStyles[i]
            Box(Modifier.fillMaxSize().background(t.bg), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Rahul", color = Color.White, fontSize = 52.sp, fontFamily = n.family, fontWeight = n.weight, letterSpacing = n.spacing)
                    Spacer(Modifier.height(12.dp))
                    Text("+91 98765 43210", color = Color(0xB3FFFFFF), fontSize = 26.sp, fontFamily = n.family, fontWeight = n.weight, letterSpacing = n.spacing)
                }
            }
        }
    }
}

fun dialerIntent(ctx: Context): Intent? {
    return if (Build.VERSION.SDK_INT >= 29) {
        val rm = ctx.getSystemService(RoleManager::class.java)
        if (rm != null && rm.isRoleAvailable(RoleManager.ROLE_DIALER) && !rm.isRoleHeld(RoleManager.ROLE_DIALER)) {
            rm.createRequestRoleIntent(RoleManager.ROLE_DIALER)
        } else null
    } else {
        Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER)
            .putExtra(TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME, ctx.packageName)
    }
}

fun isDefaultDialer(ctx: Context): Boolean =
    ctx.getSystemService(TelecomManager::class.java)?.defaultDialerPackage == ctx.packageName

@Composable
private fun ToggleRow(title: String, sub: String, on: Boolean, onChange: (Boolean) -> Unit) {
    val t = currentTheme()
    Row(Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, color = Color.White, fontSize = 16.sp)
            Text(sub, color = Dim, fontSize = 12.sp)
        }
        Switch(
            checked = on, onCheckedChange = onChange,
            colors = SwitchDefaults.colors(checkedTrackColor = t.a, checkedThumbColor = t.bg)
        )
    }
}

@Composable
fun SettingsScreen(onBack: () -> Unit, refresh: Int = 0) {
    val ctx = LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { }
    val mine = remember(refresh) { isDefaultDialer(ctx) }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        ScreenHeader("VIBE CALL", "Settings") { RoundIcon(VibeIcons.Back, "Back", onBack) }
        ToggleRow("Announce caller name", "Says \"Rahul is calling\" when it rings", Prefs.announce) { Prefs.announce = it; Prefs.save() }
        ToggleRow("Vibrate when ringing", "Works with the normal ringer mode", Prefs.vibrate) { Prefs.vibrate = it; Prefs.save() }

        SectionTitle("Phone app")
        Column(Modifier.padding(horizontal = 22.dp)) {
            Text(
                if (mine) "VIBE CALL is your phone app. Your call designs are active."
                else "Make VIBE CALL your default phone app so your call designs show on real calls.",
                color = Color.White, fontSize = 14.sp
            )
            Spacer(Modifier.height(12.dp))
            if (!mine) {
                PillBtn("Set as default phone app", true) {
                    val i = dialerIntent(ctx)
                    if (i != null) launcher.launch(i)
                }
            }
        }

        SectionTitle("Permissions")
        Column(Modifier.padding(horizontal = 22.dp)) {
            Text("Contacts, call log and phone access are needed for names and recent calls.", color = Dim, fontSize = 13.sp)
            Spacer(Modifier.height(12.dp))
            PillBtn("Open app permissions", false) {
                ctx.startActivity(
                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", ctx.packageName, null))
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
            }
        }

        SectionTitle("About")
        Column(Modifier.padding(horizontal = 22.dp).padding(bottom = 24.dp)) {
            Text("VIBE CALL 1.0", color = Color.White, fontSize = 15.sp)
            Text("Call recording button is a design only in this version.", color = Dim, fontSize = 12.sp)
        }
    }
}
