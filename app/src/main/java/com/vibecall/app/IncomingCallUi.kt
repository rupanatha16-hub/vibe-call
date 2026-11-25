package com.vibecall.app

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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.sin

private val Green = Color(0xFF34D399)
private val Red = Color(0xFFFF5C7A)
private val Ink = Color(0xFF04121A)

private class Who(
    val name: String,
    val number: String,
    val photo: String,
    val ans: () -> Unit,
    val dec: () -> Unit
) {
    val sub: String get() = if (number.isNotBlank() && number != name) number else ""
}

private fun Modifier.glow(c: Color, fx: Float, fy: Float, r: Float): Modifier = drawBehind {
    val center = Offset(size.width * fx, size.height * fy)
    val rad = size.width * r
    drawCircle(
        Brush.radialGradient(listOf(c, Color.Transparent), center = center, radius = rad),
        radius = rad,
        center = center
    )
}

@Composable
private fun Label(text: String, color: Color = Color(0xB3FFFFFF)) {
    Text(text, color = color, fontSize = 12.sp, letterSpacing = 3.sp)
}

@Composable
private fun Sub(text: String, color: Color = Color(0xB3FFFFFF)) {
    if (text.isNotBlank()) Text(text, color = color, fontSize = 15.sp, modifier = Modifier.padding(top = 4.dp))
}

@Composable
private fun Orb(accept: Boolean, onClick: () -> Unit, size: Dp = 72.dp, outline: Boolean = false, shape: Shape = CircleShape) {
    val c = if (accept) Green else Red
    var m = Modifier.size(size).clip(shape)
    m = if (outline) m.border(2.dp, c, shape) else m.background(c)
    Box(m.clickable(onClick = onClick), contentAlignment = Alignment.Center) {
        Icon(
            VibeIcons.Phone,
            if (accept) "Answer" else "Decline",
            tint = if (outline) c else Ink,
            modifier = Modifier.size(size * 0.42f).rotate(if (accept) 0f else 135f)
        )
    }
}

@Composable
private fun Actions(w: Who, size: Dp = 72.dp, outline: Boolean = false) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 36.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Orb(false, w.dec, size, outline)
            Spacer(Modifier.height(8.dp))
            Text("Decline", color = Color(0xCCFFFFFF), fontSize = 12.sp)
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Orb(true, w.ans, size, outline)
            Spacer(Modifier.height(8.dp))
            Text("Answer", color = Color(0xCCFFFFFF), fontSize = 12.sp)
        }
    }
}

@Composable
private fun TextBtn(
    label: String,
    bg: Color,
    fg: Color,
    shape: Shape,
    modifier: Modifier = Modifier,
    border: Color? = null,
    onClick: () -> Unit
) {
    var m = modifier.height(56.dp).clip(shape).background(bg)
    if (border != null) m = m.border(1.5.dp, border, shape)
    Box(m.clickable(onClick = onClick), contentAlignment = Alignment.Center) {
        Text(label, color = fg, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
internal fun Stars(modifier: Modifier = Modifier) {
    Canvas(modifier) {
        for (i in 0 until 70) {
            val x = ((i * 73) % 101) / 100f * size.width
            val y = ((i * 47 + 13) % 103) / 103f * size.height
            val r = (1 + i % 3) * 0.9f * density
            drawCircle(Color.White.copy(alpha = 0.25f + (i % 5) * 0.14f), r, Offset(x, y))
        }
    }
}

/** 12 incoming call designs. Used for real calls and for the small previews in the Vibe tab. */
@Composable
fun IncomingCallUi(
    style: Int,
    name: String,
    number: String,
    photo: String,
    onAnswer: () -> Unit,
    onDecline: () -> Unit
) {
    val w = Who(name, number, photo, onAnswer, onDecline)
    when (style.coerceIn(0, 11)) {
        0 -> In0(w)
        1 -> In1(w)
        2 -> In2(w)
        3 -> In3(w)
        4 -> In4(w)
        5 -> In5(w)
        6 -> In6(w)
        7 -> In7(w)
        8 -> In8(w)
        9 -> In9(w)
        10 -> In10(w)
        else -> In11(w)
    }
}

// 0 Aurora Glass
@Composable
private fun In0(w: Who) {
    val t = currentTheme()
    Box(
        Modifier.fillMaxSize().background(t.bg)
            .glow(t.a.copy(alpha = .7f), .1f, .08f, 1.1f)
            .glow(t.b.copy(alpha = .55f), .95f, .95f, 1.2f)
    ) {
        Column(
            Modifier.fillMaxSize().padding(horizontal = 24.dp).padding(top = 80.dp, bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val sh = RoundedCornerShape(32.dp)
            Column(
                Modifier.fillMaxWidth().clip(sh).background(Color(0x22FFFFFF))
                    .border(1.dp, Color(0x33FFFFFF), sh).padding(vertical = 32.dp, horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Avatar(w.name, w.photo, 110.dp)
                Spacer(Modifier.height(16.dp))
                StyledText(w.name, 30.sp)
                Sub(w.sub)
                Spacer(Modifier.height(12.dp))
                Label("INCOMING CALL")
            }
            Spacer(Modifier.weight(1f))
            Actions(w)
        }
    }
}

// 1 Neon Ring
@Composable
private fun In1(w: Who) {
    val t = currentTheme()
    Box(Modifier.fillMaxSize().background(Color.Black).glow(t.a.copy(alpha = .3f), .5f, .32f, .9f)) {
        Column(
            Modifier.fillMaxSize().padding(horizontal = 24.dp).padding(top = 50.dp, bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(contentAlignment = Alignment.Center) {
                PulseRings(t.a, 110.dp)
                Box(
                    Modifier.size(128.dp)
                        .border(3.dp, Brush.sweepGradient(listOf(t.a, t.b, t.a)), CircleShape)
                        .padding(8.dp)
                ) { Avatar(w.name, w.photo, 112.dp) }
            }
            StyledText(w.name, 32.sp, t.a)
            Sub(w.sub)
            Spacer(Modifier.height(10.dp))
            Label("INCOMING", t.b)
            Spacer(Modifier.weight(1f))
            Actions(w, outline = true)
        }
    }
}

// 2 Pulse Halo
@Composable
private fun In2(w: Who) {
    val t = currentTheme()
    Box(Modifier.fillMaxSize().background(t.bg).glow(t.a.copy(alpha = .45f), .5f, .3f, .9f)) {
        Column(
            Modifier.fillMaxSize().padding(horizontal = 24.dp).padding(top = 60.dp, bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(contentAlignment = Alignment.Center) {
                PulseRings(t.b, 110.dp)
                Avatar(w.name, w.photo, 110.dp)
            }
            StyledText(w.name, 30.sp)
            Sub(w.sub)
            Spacer(Modifier.height(10.dp))
            Label("INCOMING CALL")
            Spacer(Modifier.weight(1f))
            Actions(w, 80.dp)
        }
    }
}

// 3 Liquid Orb
@Composable
private fun In3(w: Who) {
    val t = currentTheme()
    Box(Modifier.fillMaxSize().background(t.bg)) {
        Box(
            Modifier.align(Alignment.TopCenter).offset(y = (-140).dp).requiredSize(480.dp)
                .clip(CircleShape).background(Brush.linearGradient(listOf(t.a, t.b)))
        )
        Column(
            Modifier.fillMaxSize().padding(horizontal = 24.dp).padding(top = 90.dp, bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Avatar(w.name, w.photo, 120.dp, Modifier.border(4.dp, Color(0x66FFFFFF), CircleShape))
            Spacer(Modifier.height(16.dp))
            StyledText(w.name, 30.sp, t.bg)
            Sub(w.sub, t.bg.copy(alpha = .7f))
            Spacer(Modifier.weight(1f))
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Orb(false, w.dec, 58.dp, true)
                Orb(true, w.ans, 92.dp)
            }
        }
    }
}

// 4 Minimal Mono
@Composable
private fun In4(w: Who) {
    Column(
        Modifier.fillMaxSize().background(Color(0xFF0A0A0A)).padding(horizontal = 28.dp)
            .padding(top = 90.dp, bottom = 40.dp)
    ) {
        Label("INCOMING")
        Spacer(Modifier.height(10.dp))
        StyledText(w.name, 42.sp, Color.White, Modifier.fillMaxWidth(), TextAlign.Start)
        Sub(w.sub)
        Spacer(Modifier.height(24.dp))
        Box(Modifier.fillMaxWidth().height(1.dp).background(Color(0x33FFFFFF)))
        Spacer(Modifier.height(24.dp))
        Avatar(w.name, w.photo, 64.dp)
        Spacer(Modifier.weight(1f))
        TextBtn("Accept", Color.Transparent, Color.White, RoundedCornerShape(4.dp), Modifier.fillMaxWidth(), Color.White) { w.ans() }
        Spacer(Modifier.height(12.dp))
        TextBtn("Decline", Color.Transparent, Color(0xFF8E8E93), RoundedCornerShape(4.dp), Modifier.fillMaxWidth()) { w.dec() }
    }
}

// 5 Royal Card
@Composable
private fun In5(w: Who) {
    val t = currentTheme()
    Box(Modifier.fillMaxSize().background(t.bg).glow(t.a.copy(alpha = .25f), .5f, 0f, 1f)) {
        Column(
            Modifier.fillMaxSize().padding(horizontal = 24.dp).padding(top = 80.dp, bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val sh = RoundedCornerShape(28.dp)
            Column(
                Modifier.fillMaxWidth().clip(sh).background(t.surface).border(1.5.dp, t.a, sh).padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Label("INCOMING CALL", t.a)
                Spacer(Modifier.height(20.dp))
                Avatar(w.name, w.photo, 100.dp, Modifier.border(2.dp, t.a, CircleShape))
                Spacer(Modifier.height(16.dp))
                StyledText(w.name, 30.sp)
                Sub(w.sub)
            }
            Spacer(Modifier.weight(1f))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TextBtn("Decline", t.surface, Red, RoundedCornerShape(16.dp), Modifier.weight(1f), Red) { w.dec() }
                TextBtn("Accept", t.a, t.bg, RoundedCornerShape(16.dp), Modifier.weight(1f)) { w.ans() }
            }
        }
    }
}

// 6 Cyber Grid
@Composable
private fun In6(w: Who) {
    val t = currentTheme()
    Box(Modifier.fillMaxSize().background(Color(0xFF030A05))) {
        Canvas(Modifier.fillMaxSize()) {
            val step = 36.dp.toPx()
            var x = 0f
            while (x < size.width) {
                drawLine(t.a.copy(alpha = .14f), Offset(x, 0f), Offset(x, size.height))
                x += step
            }
            var y = 0f
            while (y < size.height) {
                drawLine(t.a.copy(alpha = .14f), Offset(0f, y), Offset(size.width, y))
                y += step
            }
        }
        Column(
            Modifier.fillMaxSize().padding(horizontal = 24.dp).padding(top = 80.dp, bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("> INCOMING_CALL //", color = t.a, fontSize = 13.sp, fontFamily = FontFamily.Monospace)
            Spacer(Modifier.height(28.dp))
            Box(Modifier.border(2.dp, t.a, CutCornerShape(18.dp)).padding(8.dp)) {
                Avatar(w.name, w.photo, 100.dp)
            }
            Spacer(Modifier.height(20.dp))
            StyledText(w.name, 30.sp, t.a)
            Sub(w.sub)
            Spacer(Modifier.weight(1f))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TextBtn("[ DECLINE ]", Color.Transparent, Red, CutCornerShape(14.dp), Modifier.weight(1f), Red) { w.dec() }
                TextBtn("[ ACCEPT ]", Color.Transparent, Green, CutCornerShape(14.dp), Modifier.weight(1f), Green) { w.ans() }
            }
        }
    }
}

// 7 Starfield
@Composable
private fun In7(w: Who) {
    val t = currentTheme()
    Box(Modifier.fillMaxSize().background(Color.Black).glow(t.a.copy(alpha = .25f), .5f, .3f, .8f)) {
        Stars(Modifier.fillMaxSize())
        Column(
            Modifier.fillMaxSize().padding(horizontal = 24.dp).padding(top = 90.dp, bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Avatar(w.name, w.photo, 120.dp, Modifier.border(2.dp, Color(0x66FFFFFF), CircleShape))
            Spacer(Modifier.height(18.dp))
            StyledText(w.name, 30.sp)
            Sub(w.sub)
            Spacer(Modifier.height(10.dp))
            Label("INCOMING CALL")
            Spacer(Modifier.weight(1f))
            Actions(w, outline = true)
        }
    }
}

// 8 Soft Wave
@Composable
private fun In8(w: Who) {
    val t = currentTheme()
    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(t.bg, t.a.copy(alpha = .55f), t.b.copy(alpha = .85f))))) {
        Canvas(Modifier.fillMaxSize()) {
            for (k in 0..1) {
                val path = Path()
                val base = size.height * (0.70f + k * 0.05f)
                path.moveTo(0f, size.height)
                path.lineTo(0f, base)
                var x = 0f
                while (x <= size.width) {
                    path.lineTo(x, base + sin((x / size.width) * 2f * PI.toFloat() * 1.5f + k) * size.height * 0.025f)
                    x += 8f
                }
                path.lineTo(size.width, size.height)
                path.close()
                drawPath(path, Color.White.copy(alpha = 0.10f + k * 0.08f))
            }
        }
        Column(
            Modifier.fillMaxSize().padding(horizontal = 24.dp).padding(top = 90.dp, bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Avatar(w.name, w.photo, 110.dp)
            Spacer(Modifier.height(16.dp))
            StyledText(w.name, 30.sp)
            Sub(w.sub)
            Spacer(Modifier.weight(1f))
            Actions(w)
        }
    }
}

// 9 Split Light
@Composable
private fun In9(w: Who) {
    val t = currentTheme()
    Column(Modifier.fillMaxSize().background(t.bg)) {
        Box(
            Modifier.fillMaxWidth().weight(.42f).background(Brush.linearGradient(listOf(t.a, t.b))),
            contentAlignment = Alignment.TopCenter
        ) { Box(Modifier.padding(top = 60.dp)) { Label("INCOMING CALL", t.bg) } }
        Column(
            Modifier.fillMaxWidth().weight(.58f).padding(horizontal = 24.dp).padding(bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(Modifier.height(64.dp).fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
                Avatar(w.name, w.photo, 128.dp, Modifier.requiredSize(128.dp).offset(y = (-64).dp).border(4.dp, t.bg, CircleShape))
            }
            Spacer(Modifier.height(12.dp))
            StyledText(w.name, 30.sp)
            Sub(w.sub)
            Spacer(Modifier.weight(1f))
            Actions(w)
        }
    }
}

// 10 Ticket Pass
@Composable
private fun In10(w: Who) {
    val t = currentTheme()
    val dark = Color(0xFF15151F)
    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(t.a, t.b)))) {
        Column(
            Modifier.fillMaxSize().padding(horizontal = 24.dp).padding(top = 80.dp, bottom = 40.dp)
                .clip(RoundedCornerShape(28.dp)).background(Color(0xFFF7F7FB)).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Label("INCOMING CALL PASS", Color(0xFF6B6780))
            Spacer(Modifier.height(20.dp))
            Avatar(w.name, w.photo, 96.dp)
            Spacer(Modifier.height(14.dp))
            StyledText(w.name, 28.sp, dark)
            Sub(w.sub, Color(0xFF6B6780))
            Spacer(Modifier.weight(1f))
            Canvas(Modifier.fillMaxWidth().height(2.dp)) {
                drawLine(
                    Color(0xFFBDBBD0), Offset(0f, 0f), Offset(size.width, 0f),
                    strokeWidth = 3f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(16f, 12f))
                )
            }
            Spacer(Modifier.height(20.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TextBtn("Decline", Color(0x22FF5C7A), Color(0xFFD6334F), RoundedCornerShape(16.dp), Modifier.weight(1f)) { w.dec() }
                TextBtn("Accept", Green, Ink, RoundedCornerShape(16.dp), Modifier.weight(1f)) { w.ans() }
            }
        }
    }
}

// 11 Spotlight
@Composable
private fun In11(w: Who) {
    Box(Modifier.fillMaxSize().background(Color.Black).glow(Color.White.copy(alpha = .28f), .5f, .18f, 1f)) {
        Column(
            Modifier.fillMaxSize().padding(horizontal = 24.dp).padding(top = 70.dp, bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Avatar(w.name, w.photo, 150.dp)
            Spacer(Modifier.height(20.dp))
            StyledText(w.name, 32.sp)
            Sub(w.sub)
            Spacer(Modifier.height(10.dp))
            Label("INCOMING CALL")
            Spacer(Modifier.weight(1f))
            Row(
                Modifier.fillMaxWidth().height(76.dp).clip(CircleShape).background(Color(0x22FFFFFF)).padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Orb(false, w.dec, 60.dp, true)
                Text(
                    "›  ›  ›", Modifier.weight(1f), color = Color(0x99FFFFFF),
                    fontSize = 20.sp, textAlign = TextAlign.Center
                )
                TextBtn("Answer", Green, Ink, CircleShape, Modifier.width(120.dp)) { w.ans() }
            }
        }
    }
}
