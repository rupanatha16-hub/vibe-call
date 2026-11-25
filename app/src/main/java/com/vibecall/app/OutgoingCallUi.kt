package com.vibecall.app

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val EndRed = Color(0xFFFF4D6A)

private class Out(
    val name: String,
    val number: String,
    val photo: String,
    val status: String,
    val end: () -> Unit
)

private class Ctl(val icon: ImageVector, val label: String, val on: Boolean, val click: () -> Unit)

@Composable
private fun DtmfPad() {
    val keys = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "*", "0", "#")
    Column(verticalArrangement = Arrangement.spacedBy(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        for (r in 0..3) {
            Row(horizontalArrangement = Arrangement.spacedBy(22.dp)) {
                for (c in 0..2) {
                    val k = keys[r * 3 + c]
                    Box(
                        Modifier.size(58.dp).clip(CircleShape).background(Color(0x26FFFFFF))
                            .clickable { CallState.dtmf(k[0]) },
                        contentAlignment = Alignment.Center
                    ) { Text(k, color = Color.White, fontSize = 24.sp) }
                }
            }
        }
        Text(
            "Hide", color = Color(0xCCFFFFFF), fontSize = 14.sp,
            modifier = Modifier.clickable { CallState.showKeypad = false }.padding(12.dp)
        )
    }
}

/** Speaker, mute, keypad, record, hold, bluetooth. Look is chosen by Prefs.iconStyle. */
@Composable
private fun Controls(modifier: Modifier = Modifier) {
    if (CallState.showKeypad) {
        Box(modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { DtmfPad() }
        return
    }
    val st = Prefs.iconStyle
    val items = listOf(
        Ctl(VibeIcons.Speaker, "Speaker", CallState.speaker) { CallState.toggleSpeaker() },
        Ctl(if (CallState.muted) VibeIcons.MicOff else VibeIcons.Mic, "Mute", CallState.muted) { CallState.toggleMute() },
        Ctl(VibeIcons.Keypad, "Keypad", false) { CallState.showKeypad = true },
        Ctl(VibeIcons.Record, "Record", CallState.recording) { CallState.toggleRecord() },
        Ctl(VibeIcons.Pause, "Hold", CallState.held) { CallState.toggleHold() },
        Ctl(VibeIcons.Bluetooth, "Bluetooth", false) {
            CallState.service?.setAudioRoute(android.telecom.CallAudioState.ROUTE_BLUETOOTH)
        }
    )
    val per = if (st == 8) 2 else 3
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(20.dp)) {
        for (row in items.chunked(per)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                for (c in row) ControlBtn(c.icon, c.label, c.on, st, c.click)
            }
        }
    }
}

@Composable
private fun EndBtn(o: Out, modifier: Modifier = Modifier, size: Dp = 72.dp) {
    Box(
        modifier.size(size).clip(CircleShape).background(EndRed).clickable(onClick = o.end),
        contentAlignment = Alignment.Center
    ) {
        Icon(VibeIcons.Phone, "End call", tint = Color.White, modifier = Modifier.size(size * 0.42f).rotate(135f))
    }
}

@Composable
private fun Head(
    o: Out,
    av: Dp = 100.dp,
    nameSize: TextUnit = 30.sp,
    color: Color = Color.White,
    modifier: Modifier = Modifier
) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Avatar(o.name, o.photo, av)
        Spacer(Modifier.height(14.dp))
        StyledText(o.name, nameSize, color, Modifier.padding(horizontal = 16.dp))
        Text(o.status, color = color.copy(alpha = .7f), fontSize = 15.sp, modifier = Modifier.padding(top = 6.dp))
    }
}

@Composable
private fun Page(bg: Modifier, content: @Composable () -> Unit) {
    Box(bg.fillMaxSize()) { content() }
}

/** 12 outgoing call designs (also used while a call is connected). */
@Composable
fun OutgoingCallUi(style: Int, name: String, number: String, photo: String, status: String, onEnd: () -> Unit) {
    val o = Out(name, number, photo, status, onEnd)
    when (style.coerceIn(0, 11)) {
        0 -> O0(o)
        1 -> O1(o)
        2 -> O2(o)
        3 -> O3(o)
        4 -> O4(o)
        5 -> O5(o)
        6 -> O6(o)
        7 -> O7(o)
        8 -> O8(o)
        9 -> O9(o)
        10 -> O10(o)
        else -> O11(o)
    }
}

private val Pad = Modifier.fillMaxSize()

// 0 Calm Dark
@Composable
private fun O0(o: Out) {
    Page(Modifier.background(Color(0xFF0B0B10))) {
        Column(
            Pad.padding(horizontal = 24.dp).padding(top = 80.dp, bottom = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Head(o)
            Spacer(Modifier.weight(1f))
            Controls()
            Spacer(Modifier.height(28.dp))
            EndBtn(o)
        }
    }
}

// 1 Orbit
@Composable
private fun O1(o: Out) {
    val t = currentTheme()
    val tr = rememberInfiniteTransition(label = "orbit")
    val ang by tr.animateFloat(0f, 360f, infiniteRepeatable(tween(4000, easing = LinearEasing)), label = "a")
    Page(Modifier.background(t.bg)) {
        Column(
            Pad.padding(horizontal = 24.dp).padding(top = 70.dp, bottom = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(Modifier.size(170.dp), contentAlignment = Alignment.Center) {
                Box(Modifier.fillMaxSize().rotate(ang).border(1.dp, t.a.copy(alpha = .35f), CircleShape)) {
                    Box(Modifier.align(Alignment.TopCenter).size(14.dp).clip(CircleShape).background(t.a))
                }
                Avatar(o.name, o.photo, 120.dp)
            }
            Spacer(Modifier.height(16.dp))
            StyledText(o.name, 30.sp)
            Text(o.status, color = Color(0xB3FFFFFF), fontSize = 15.sp, modifier = Modifier.padding(top = 6.dp))
            Spacer(Modifier.weight(1f))
            Controls()
            Spacer(Modifier.height(28.dp))
            EndBtn(o)
        }
    }
}

// 2 Glass Panel
@Composable
private fun O2(o: Out) {
    val t = currentTheme()
    Page(Modifier.background(Brush.verticalGradient(listOf(t.a, t.bg)))) {
        Column(Pad, horizontalAlignment = Alignment.CenterHorizontally) {
            Head(o, 110.dp, 30.sp, Color.White, Modifier.padding(top = 80.dp))
            Spacer(Modifier.weight(1f))
            Column(
                Modifier.fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                    .background(Color(0x40000000)).padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Controls()
                Spacer(Modifier.height(24.dp))
                EndBtn(o)
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

// 3 Gradient Fade
@Composable
private fun O3(o: Out) {
    val t = currentTheme()
    Page(Modifier.background(Brush.verticalGradient(listOf(t.a, t.b.copy(alpha = .6f), t.bg)))) {
        Column(
            Pad.padding(horizontal = 24.dp).padding(top = 70.dp, bottom = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Head(o, 140.dp, 32.sp)
            Spacer(Modifier.weight(1f))
            Controls()
            Spacer(Modifier.height(28.dp))
            EndBtn(o)
        }
    }
}

// 4 Big Name
@Composable
private fun O4(o: Out) {
    Page(Modifier.background(Color(0xFF0A0A0A))) {
        Column(Pad.padding(horizontal = 28.dp).padding(top = 70.dp, bottom = 36.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f)) {
                    Text(o.status.uppercase(), color = Color(0xB3FFFFFF), fontSize = 12.sp, letterSpacing = 3.sp)
                    Spacer(Modifier.height(8.dp))
                    StyledText(o.name, 44.sp, Color.White, Modifier.fillMaxWidth(), TextAlign.Start)
                }
                Avatar(o.name, o.photo, 64.dp)
            }
            Spacer(Modifier.weight(1f))
            Controls()
            Spacer(Modifier.height(28.dp))
            Row(
                Modifier.fillMaxWidth().height(60.dp).clip(RoundedCornerShape(30.dp))
                    .background(EndRed).clickable(onClick = o.end),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(VibeIcons.Phone, "End call", tint = Color.White, modifier = Modifier.size(24.dp).rotate(135f))
                Text("  End call", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

// 5 Compact Bar
@Composable
private fun O5(o: Out) {
    val t = currentTheme()
    Page(Modifier.background(t.bg)) {
        Column(Pad.padding(horizontal = 20.dp).padding(top = 56.dp, bottom = 36.dp)) {
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(t.surface).padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Avatar(o.name, o.photo, 48.dp)
                Column(Modifier.weight(1f).padding(start = 12.dp)) {
                    StyledText(o.name, 18.sp, Color.White, Modifier.fillMaxWidth(), TextAlign.Start)
                    Text(o.status, color = Color(0xB3FFFFFF), fontSize = 13.sp)
                }
            }
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                StyledText(o.status, 48.sp, t.a)
            }
            Controls()
            Spacer(Modifier.height(24.dp))
            EndBtn(o, Modifier.align(Alignment.CenterHorizontally), 64.dp)
        }
    }
}

// 6 Ripple
@Composable
private fun O6(o: Out) {
    val t = currentTheme()
    Page(Modifier.background(Color(0xFF07070C))) {
        Column(
            Pad.padding(horizontal = 24.dp).padding(top = 40.dp, bottom = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(contentAlignment = Alignment.Center) {
                PulseRings(t.a, 100.dp)
                Avatar(o.name, o.photo, 100.dp)
            }
            StyledText(o.name, 30.sp)
            Text(o.status, color = Color(0xB3FFFFFF), fontSize = 15.sp, modifier = Modifier.padding(top = 6.dp))
            Spacer(Modifier.weight(1f))
            Controls()
            Spacer(Modifier.height(28.dp))
            EndBtn(o)
        }
    }
}

// 7 Duo Tone
@Composable
private fun O7(o: Out) {
    val t = currentTheme()
    Column(Pad.background(t.bg)) {
        Box(Modifier.fillMaxWidth().weight(.42f).background(t.a), contentAlignment = Alignment.Center) {
            Head(o, 90.dp, 28.sp, t.bg, Modifier.padding(top = 40.dp))
        }
        Column(
            Modifier.fillMaxWidth().weight(.58f).padding(horizontal = 24.dp).padding(top = 28.dp, bottom = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Controls()
            Spacer(Modifier.weight(1f))
            EndBtn(o)
        }
    }
}

// 8 Neon Frame
@Composable
private fun O8(o: Out) {
    val t = currentTheme()
    Page(Modifier.background(Color.Black)) {
        Box(
            Pad.padding(14.dp).border(2.dp, Brush.linearGradient(listOf(t.a, t.b)), RoundedCornerShape(32.dp))
        ) {
            Column(
                Pad.padding(horizontal = 20.dp).padding(top = 60.dp, bottom = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Head(o, 100.dp, 28.sp, t.a)
                Spacer(Modifier.weight(1f))
                Controls()
                Spacer(Modifier.height(24.dp))
                EndBtn(o)
            }
        }
    }
}

// 9 Paper Card
@Composable
private fun O9(o: Out) {
    val dark = Color(0xFF15151F)
    Page(Modifier.background(Color(0xFFEDEBF5))) {
        Column(
            Pad.padding(horizontal = 20.dp).padding(top = 70.dp, bottom = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Head(o, 90.dp, 28.sp, dark)
            Spacer(Modifier.weight(1f))
            Column(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(28.dp)).background(dark).padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Controls()
                Spacer(Modifier.height(20.dp))
                EndBtn(o)
            }
        }
    }
}

// 10 Radar
@Composable
private fun O10(o: Out) {
    val t = currentTheme()
    val tr = rememberInfiniteTransition(label = "radar")
    val ang by tr.animateFloat(0f, 360f, infiniteRepeatable(tween(3000, easing = LinearEasing)), label = "s")
    Page(Modifier.background(Color.Black)) {
        Column(
            Pad.padding(horizontal = 24.dp).padding(top = 50.dp, bottom = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(Modifier.size(260.dp), contentAlignment = Alignment.Center) {
                Canvas(Modifier.fillMaxSize()) {
                    for (i in 1..3) {
                        drawCircle(t.a.copy(alpha = .3f), radius = size.minDimension / 2f * i / 3f, style = Stroke(2f))
                    }
                    rotate(ang) {
                        drawLine(t.b, center, Offset(center.x, 0f), strokeWidth = 4f)
                    }
                }
                Avatar(o.name, o.photo, 96.dp)
            }
            Spacer(Modifier.height(10.dp))
            StyledText(o.name, 28.sp)
            Text(o.status, color = Color(0xB3FFFFFF), fontSize = 15.sp, modifier = Modifier.padding(top = 6.dp))
            Spacer(Modifier.weight(1f))
            Controls()
            Spacer(Modifier.height(24.dp))
            EndBtn(o)
        }
    }
}

// 11 Night Sky
@Composable
private fun O11(o: Out) {
    Page(Modifier.background(Brush.verticalGradient(listOf(Color(0xFF05060F), Color(0xFF101935))))) {
        Stars(Modifier.fillMaxSize())
        Column(
            Pad.padding(horizontal = 24.dp).padding(top = 80.dp, bottom = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Head(o, 100.dp, 30.sp)
            Spacer(Modifier.weight(1f))
            Controls()
            Spacer(Modifier.height(28.dp))
            EndBtn(o)
        }
    }
}
