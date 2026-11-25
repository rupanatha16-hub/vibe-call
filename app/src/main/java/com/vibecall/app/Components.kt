package com.vibecall.app

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlinx.coroutines.delay
import java.io.File

fun initial(s: String): String =
    s.trim().firstOrNull { it.isLetterOrDigit() }?.uppercase() ?: "#"

fun mmss(ms: Long): String {
    val s = (ms / 1000).coerceAtLeast(0)
    return "%02d:%02d".format(s / 60, s % 60)
}

/** Ticking call timer text, empty until the call is connected. */
@Composable
fun rememberElapsed(): String {
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(500)
            now = System.currentTimeMillis()
        }
    }
    val start = CallState.connectedAt
    return if (start == 0L) "" else mmss(now - start)
}

/** Round profile picture. Shows the saved photo, or the first letter on a theme gradient. */
@Composable
fun Avatar(name: String, photo: String, size: Dp, modifier: Modifier = Modifier) {
    val t = currentTheme()
    if (photo.isNotBlank() && File(photo).exists()) {
        AsyncImage(
            model = File(photo),
            contentDescription = name,
            contentScale = ContentScale.Crop,
            modifier = modifier.size(size).clip(CircleShape)
        )
    } else {
        Box(
            modifier
                .size(size)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(t.a, t.b))),
            contentAlignment = Alignment.Center
        ) {
            Text(
                initial(name),
                color = t.bg,
                fontWeight = FontWeight.Bold,
                fontSize = (size.value * 0.4f).sp
            )
        }
    }
}

/** Name or number text, drawn in the number style chosen in Vibe. */
@Composable
fun StyledText(
    text: String,
    size: TextUnit,
    color: Color = Color.White,
    modifier: Modifier = Modifier,
    align: TextAlign = TextAlign.Center
) {
    val n = NumberStyles[Prefs.numberStyle.coerceIn(0, NumberStyles.lastIndex)]
    Text(
        text,
        modifier = modifier,
        color = color,
        fontSize = size,
        fontFamily = n.family,
        fontWeight = n.weight,
        letterSpacing = n.spacing,
        textAlign = align,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

/** Three expanding rings, used by several call designs. */
@Composable
fun PulseRings(color: Color, base: Dp, modifier: Modifier = Modifier) {
    val tr = rememberInfiniteTransition(label = "pulse")
    Box(modifier.size(base * 2.4f), contentAlignment = Alignment.Center) {
        for (i in 0..2) {
            val p by tr.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2600, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart,
                    initialStartOffset = StartOffset(i * 850)
                ),
                label = "ring$i"
            )
            Box(
                Modifier
                    .size(base * (1f + p * 1.4f))
                    .border(1.5.dp, color.copy(alpha = 1f - p), CircleShape)
            )
        }
    }
}

/** One call button (speaker, mute, keypad, record...). 12 looks, picked by [style]. */
@Composable
fun ControlBtn(
    icon: ImageVector,
    label: String,
    active: Boolean,
    style: Int,
    onClick: () -> Unit
) {
    val t = currentTheme()
    val s = style.coerceIn(0, IconStyles.lastIndex)
    val white = Color.White

    if (s == 8) {
        Row(
            Modifier
                .height(48.dp)
                .clip(CircleShape)
                .background(if (active) t.a else Color(0x26FFFFFF))
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(icon, contentDescription = label, tint = if (active) t.bg else white, modifier = Modifier.size(22.dp))
            Text(label, color = if (active) t.bg else white, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
        return
    }

    val shape: Shape = when (s) {
        4 -> RoundedCornerShape(16.dp)
        5 -> RoundedCornerShape(30.dp)
        10 -> CutCornerShape(16.dp)
        else -> CircleShape
    }
    val base = Modifier.size(62.dp)
    val (mod, tint) = when (s) {
        0 -> base.clip(shape).background(if (active) t.a else Color(0x33FFFFFF)) to (if (active) t.bg else white)
        1 -> base.clip(shape)
            .background(if (active) t.a.copy(alpha = .25f) else Color.Transparent)
            .border(2.dp, if (active) t.a else Color(0x80FFFFFF), shape) to (if (active) t.a else white)
        2 -> base.clip(shape)
            .background(Brush.radialGradient(listOf(Color(0x66FFFFFF), Color(0x12FFFFFF))))
            .border(1.dp, Color(0x55FFFFFF), shape) to (if (active) t.b else white)
        3 -> base.shadow(if (active) 22.dp else 8.dp, shape, ambientColor = t.a, spotColor = t.a)
            .background(Color(0xFF0B0B12), shape)
            .border(1.5.dp, t.a, shape) to (if (active) t.b else t.a)
        4 -> base.clip(shape).background(if (active) t.b else Color(0x24FFFFFF)) to (if (active) t.bg else white)
        5 -> base.clip(shape)
            .background(if (active) t.a else Color(0x1FFFFFFF))
            .border(1.dp, Color(0x33FFFFFF), shape) to (if (active) t.bg else white)
        6 -> base.clip(shape).background(
            Brush.linearGradient(
                if (active) listOf(t.a, t.b) else listOf(t.a.copy(alpha = .35f), t.b.copy(alpha = .35f))
            )
        ) to (if (active) t.bg else white)
        7 -> base.clip(shape).background(Color.Transparent) to (if (active) t.a else white)
        9 -> base.border(1.dp, Color(0x55FFFFFF), shape)
            .padding(5.dp)
            .clip(shape)
            .background(if (active) t.a else Color(0x22FFFFFF)) to (if (active) t.bg else white)
        10 -> base.clip(shape).background(if (active) t.a else Color(0x2AFFFFFF)) to (if (active) t.bg else white)
        else -> base.shadow(14.dp, shape)
            .clip(shape)
            .background(if (active) t.a else Color(0xFFF4F4F8)) to (if (active) t.bg else Color(0xFF15151F))
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(mod.clickable(onClick = onClick), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = label, tint = tint, modifier = Modifier.size(26.dp))
        }
        Text(label, color = Color(0xCCFFFFFF), fontSize = 12.sp)
    }
}

/**
 * Shows a full 360x640 call design shrunk into a small card, so a style can be previewed
 * before choosing it. Tapping the card calls [onClick].
 */
@Composable
fun PhonePreview(modifier: Modifier = Modifier, onClick: () -> Unit, content: @Composable () -> Unit) {
    val shape = RoundedCornerShape(18.dp)
    Box(
        modifier
            .aspectRatio(9f / 16f)
            .clip(shape)
            .border(1.dp, Color(0x33FFFFFF), shape)
    ) {
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val k = maxWidth.value / 360f
            Box(
                Modifier
                    .fillMaxSize()
                    .wrapContentSize(Alignment.TopStart, unbounded = true)
                    .size(360.dp, 640.dp)
                    .graphicsLayer(scaleX = k, scaleY = k, transformOrigin = TransformOrigin(0f, 0f))
            ) { content() }
        }
        Box(Modifier.fillMaxSize().clickable(onClick = onClick))
    }
}
