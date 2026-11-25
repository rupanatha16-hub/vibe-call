package com.vibecall.app

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.CallLog
import android.provider.ContactsContract
import android.telecom.TelecomManager
import android.text.format.DateUtils
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat

/** Bump [tick] after permissions are granted so lists reload. */
object Perm {
    var tick by mutableIntStateOf(0)
}

private fun has(c: Context, p: String) =
    ContextCompat.checkSelfPermission(c, p) == PackageManager.PERMISSION_GRANTED

data class Recent(val name: String, val number: String, val whenText: String, val type: Int = 0)
data class Person(val name: String, val number: String)

fun loadRecents(c: Context): List<Recent> {
    if (!has(c, Manifest.permission.READ_CALL_LOG)) return emptyList()
    val out = mutableListOf<Recent>()
    try {
        c.contentResolver.query(
            CallLog.Calls.CONTENT_URI,
            arrayOf(
                CallLog.Calls.CACHED_NAME, CallLog.Calls.NUMBER,
                CallLog.Calls.DATE, CallLog.Calls.TYPE
            ),
            null, null, "${CallLog.Calls.DATE} DESC"
        )?.use { cur ->
            while (cur.moveToNext() && out.size < 80) {
                out.add(
                    Recent(
                        cur.getString(0) ?: "",
                        cur.getString(1) ?: "",
                        DateUtils.getRelativeTimeSpanString(cur.getLong(2)).toString(),
                        cur.getInt(3)
                    )
                )
            }
        }
    } catch (e: Exception) {
    }
    // One row per person/number: the newest call wins.
    return out.distinctBy { Prefs.key(it.number) }.take(40)
}

fun loadContacts(c: Context): List<Person> {
    if (!has(c, Manifest.permission.READ_CONTACTS)) return emptyList()
    val out = mutableListOf<Person>()
    try {
        c.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER
            ),
            null, null,
            "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} COLLATE NOCASE ASC"
        )?.use { cur ->
            while (cur.moveToNext()) {
                out.add(Person(cur.getString(0) ?: "", cur.getString(1) ?: ""))
            }
        }
    } catch (e: Exception) {
    }
    return out.distinctBy { Prefs.key(it.number) }
}

fun placeCall(c: Context, number: String) {
    if (number.isBlank()) return
    try {
        val tm = c.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
        tm.placeCall(Uri.fromParts("tel", number, null), Bundle())
    } catch (e: SecurityException) {
        Toast.makeText(c, "Allow the phone permission first (Settings)", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        Toast.makeText(c, "Could not place the call", Toast.LENGTH_SHORT).show()
    }
}

private fun digitsOf(s: String) = s.filter { it.isDigit() }

private fun personMatches(p: Person, q: String): Boolean {
    if (q.isBlank()) return true
    if (p.name.contains(q, ignoreCase = true)) return true
    val qd = digitsOf(q)
    return qd.isNotEmpty() && digitsOf(p.number).contains(qd)
}

private val Grey = Color(0xFF9491AA)
private val MissedRed = Color(0xFFFF6B6B)

@Composable
private fun Key(d: String, l: String, onClick: () -> Unit) {
    Box(
        Modifier.size(66.dp).clip(CircleShape).background(Color(0x14FFFFFF))
            .border(1.dp, Color(0x1AFFFFFF), CircleShape).clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(d, color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Light)
            if (l.isNotEmpty()) Text(l, color = Grey, fontSize = 9.sp, letterSpacing = 1.sp)
        }
    }
}

@Composable
private fun SearchBar(value: String, hint: String, onChange: (String) -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp).height(48.dp)
            .clip(RoundedCornerShape(24.dp)).background(Color(0x14FFFFFF))
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(VibeIcons.Search, "Search", tint = Grey, modifier = Modifier.size(20.dp))
        Box(Modifier.weight(1f).padding(horizontal = 10.dp), contentAlignment = Alignment.CenterStart) {
            if (value.isEmpty()) Text(hint, color = Grey, fontSize = 15.sp)
            BasicTextField(
                value = value,
                onValueChange = onChange,
                singleLine = true,
                textStyle = TextStyle(color = Color.White, fontSize = 15.sp),
                cursorBrush = SolidColor(Color.White),
                modifier = Modifier.fillMaxWidth()
            )
        }
        if (value.isNotEmpty()) {
            Box(
                Modifier.size(28.dp).clip(CircleShape).clickable { onChange("") },
                contentAlignment = Alignment.Center
            ) { Icon(VibeIcons.Close, "Clear", tint = Grey, modifier = Modifier.size(18.dp)) }
        }
    }
}

@Composable
private fun ListRow(
    title: String,
    sub: String,
    right: String,
    number: String,
    titleColor: Color = Color.White,
    onClick: () -> Unit
) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).clickable(onClick = onClick).padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Avatar(title, Prefs.photoFor(number), 44.dp)
        Column(Modifier.weight(1f).padding(horizontal = 14.dp)) {
            Text(title, color = titleColor, fontSize = 16.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(sub, color = Grey, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        if (right.isNotEmpty()) Text(right, color = Grey, fontSize = 12.sp)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DialTab(onSettings: () -> Unit) {
    val ctx = LocalContext.current
    val t = currentTheme()
    var num by remember { mutableStateOf("") }
    var query by remember { mutableStateOf("") }
    var showPad by remember { mutableStateOf(false) }
    val recents = remember(Perm.tick) { loadRecents(ctx) }
    val people = remember(Perm.tick) { loadContacts(ctx) }
    val rows = listOf(
        listOf("1" to "", "2" to "ABC", "3" to "DEF"),
        listOf("4" to "GHI", "5" to "JKL", "6" to "MNO"),
        listOf("7" to "PQRS", "8" to "TUV", "9" to "WXYZ"),
        listOf("*" to "", "0" to "+", "#" to "")
    )

    // Back closes the keypad first. The app closes only when the keypad is already closed.
    BackHandler(enabled = showPad) { showPad = false }

    val filter = if (showPad) num else query
    val results: List<Person> = if (filter.isBlank()) emptyList() else {
        val fromContacts = people.filter { personMatches(it, filter) }
        val fromRecents = recents
            .filter { personMatches(Person(it.name, it.number), filter) }
            .map { Person(it.name, it.number) }
        (fromContacts + fromRecents).distinctBy { Prefs.key(it.number) }.take(40)
    }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            ScreenHeader("VIBE CALL", "Dial") { RoundIcon(VibeIcons.Gear, "Settings", onSettings) }
            SearchBar(query, "Search name or number") { query = it }

            if (filter.isNotBlank()) {
                if (results.isEmpty()) {
                    Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("No match", color = Grey, fontSize = 14.sp)
                    }
                } else {
                    LazyColumn(
                        Modifier.weight(1f).padding(horizontal = 12.dp),
                        contentPadding = PaddingValues(bottom = 90.dp)
                    ) {
                        items(results) { p ->
                            ListRow(p.name.ifBlank { p.number }, p.number, "", p.number) {
                                num = p.number
                                query = ""
                                showPad = true
                            }
                        }
                    }
                }
            } else if (recents.isEmpty()) {
                Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("No recent calls", color = Grey, fontSize = 14.sp)
                }
            } else {
                LazyColumn(
                    Modifier.weight(1f).padding(horizontal = 12.dp),
                    contentPadding = PaddingValues(bottom = 90.dp)
                ) {
                    items(recents) { r ->
                        val title = r.name.ifBlank { r.number }
                        val missed = r.type == CallLog.Calls.MISSED_TYPE
                        val label = when (r.type) {
                            CallLog.Calls.OUTGOING_TYPE -> "Outgoing"
                            CallLog.Calls.MISSED_TYPE -> "Missed"
                            CallLog.Calls.REJECTED_TYPE -> "Declined"
                            else -> "Incoming"
                        }
                        ListRow(
                            title, "$label · ${r.number}", r.whenText, r.number,
                            if (missed) MissedRed else Color.White
                        ) {
                            num = r.number
                            showPad = true
                        }
                    }
                }
            }
        }

        // Round dial button (bottom right)
        if (!showPad) {
            Box(
                Modifier.align(Alignment.BottomEnd).padding(18.dp).size(62.dp).clip(CircleShape)
                    .background(Brush.linearGradient(listOf(Color(0xFF34D399), Color(0xFF22C1A6))))
                    .clickable { showPad = true },
                contentAlignment = Alignment.Center
            ) { Icon(VibeIcons.Keypad, "Dial", tint = Color(0xFF04261A), modifier = Modifier.size(28.dp)) }
        }

        // Number pad slides up from the bottom
        AnimatedVisibility(
            visible = showPad,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = slideInVertically { it },
            exit = slideOutVertically { it }
        ) {
            Column(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp))
                    .background(t.surface).padding(top = 10.dp, bottom = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    num, color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Light,
                    letterSpacing = 1.sp, maxLines = 1, textAlign = TextAlign.Center,
                    modifier = Modifier.height(38.dp).padding(horizontal = 20.dp)
                )
                for (r in rows) {
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 28.dp).padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) { for (k in r) Key(k.first, k.second) { num += k.first } }
                }
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 28.dp).padding(top = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        Modifier.size(44.dp).clip(CircleShape).clickable { showPad = false },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            VibeIcons.Chevron, "Hide keypad", tint = Color.White,
                            modifier = Modifier.size(26.dp).rotate(90f)
                        )
                    }
                    Box(
                        Modifier.size(68.dp).clip(CircleShape)
                            .background(Brush.linearGradient(listOf(Color(0xFF34D399), Color(0xFF22C1A6))))
                            .clickable { placeCall(ctx, num) },
                        contentAlignment = Alignment.Center
                    ) { Icon(VibeIcons.Phone, "Call", tint = Color(0xFF04261A), modifier = Modifier.size(30.dp)) }
                    Box(
                        Modifier.size(44.dp).clip(CircleShape)
                            .combinedClickable(
                                onClick = { num = num.dropLast(1) },
                                onLongClick = { num = "" }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(VibeIcons.Backspace, "Delete", tint = Color.White, modifier = Modifier.size(24.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun ContactsTab() {
    val ctx = LocalContext.current
    val people = remember(Perm.tick) { loadContacts(ctx) }
    var sel by remember { mutableStateOf<Person?>(null) }
    var query by remember { mutableStateOf("") }
    val shown = remember(people, query) { people.filter { personMatches(it, query) } }
    Column(Modifier.fillMaxSize()) {
        ScreenHeader("VIBE CALL", "Contacts")
        SearchBar(query, "Search name or number") { query = it }
        if (people.isEmpty()) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("No contacts (allow the contacts permission)", color = Grey, fontSize = 14.sp)
            }
        } else if (shown.isEmpty()) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("No match", color = Grey, fontSize = 14.sp)
            }
        } else {
            LazyColumn(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                items(shown) { p ->
                    Row(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).clickable { sel = p }.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Avatar(p.name, Prefs.photoFor(p.number), 44.dp)
                        Column(Modifier.weight(1f).padding(horizontal = 14.dp)) {
                            Text(p.name, color = Color.White, fontSize = 16.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(p.number, color = Grey, fontSize = 12.sp)
                        }
                        if (Prefs.contactIncoming.containsKey(Prefs.key(p.number))) {
                            Icon(VibeIcons.Vibe, "Has custom style", tint = currentTheme().a, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        }
    }
    sel?.let { p -> ContactSheet(p) { sel = null } }
}

@Composable
private fun ContactSheet(p: Person, onClose: () -> Unit) {
    val ctx = LocalContext.current
    val t = currentTheme()
    val pick = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { u ->
        if (u != null) Prefs.setContactPhoto(ctx, p.number, u)
    }
    val k = Prefs.key(p.number)
    Dialog(onDismissRequest = onClose, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Column(
            Modifier.fillMaxWidth().padding(16.dp).clip(RoundedCornerShape(28.dp)).background(t.surface)
                .verticalScroll(rememberScrollState()).padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Avatar(p.name, Prefs.photoFor(p.number), 96.dp, Modifier.clickable { pick.launch("image/*") })
            Spacer(Modifier.height(10.dp))
            Text(p.name, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
            Text(p.number, color = Grey, fontSize = 14.sp)
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                PillBtn("Change photo", false) { pick.launch("image/*") }
                PillBtn("Call", true) { onClose(); placeCall(ctx, p.number) }
            }
            Spacer(Modifier.height(20.dp))
            Text(
                "Incoming call design for ${p.name}", color = Color.White, fontSize = 14.sp,
                fontWeight = FontWeight.Medium, textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(10.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                item {
                    StyleCard("Default", !Prefs.contactIncoming.containsKey(k), { Prefs.setContactIncoming(p.number, null) }) {
                        Box(Modifier.fillMaxSize().background(t.bg), contentAlignment = Alignment.Center) {
                            Text("Use my default", color = Color.White, fontSize = 30.sp, textAlign = TextAlign.Center)
                        }
                    }
                }
                items(IncomingStyles.size) { i ->
                    StyleCard(IncomingStyles[i], Prefs.contactIncoming[k] == i, { Prefs.setContactIncoming(p.number, i) }) {
                        IncomingCallUi(i, p.name, p.number, Prefs.photoFor(p.number), {}, {})
                    }
                }
            }
            Spacer(Modifier.height(14.dp))
            Text(
                "This photo and design show on your phone only.", color = Grey,
                fontSize = 12.sp, textAlign = TextAlign.Center
            )
        }
    }
}
