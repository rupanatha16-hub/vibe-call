package com.vibecall.app

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

/** Real SVG path icons (24x24 viewBox). Tint them with Icon(tint = ...). */
object VibeIcons {
    private fun make(name: String, filled: Boolean, vararg paths: String): ImageVector {
        val b = ImageVector.Builder(name, 24.dp, 24.dp, 24f, 24f)
        for (d in paths) {
            val nodes = PathParser().parsePathString(d).toNodes()
            if (filled) {
                b.addPath(nodes, fill = SolidColor(Color.Black))
            } else {
                b.addPath(
                    nodes,
                    stroke = SolidColor(Color.Black),
                    strokeLineWidth = 1.8f,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round
                )
            }
        }
        return b.build()
    }

    private fun line(name: String, vararg p: String) = make(name, false, *p)

    val Phone by lazy { line("phone", "M5 4h4l2 5-2.5 1.5a11 11 0 0 0 5 5L15 13l5 2v4a2 2 0 0 1-2 2A16 16 0 0 1 3 6a2 2 0 0 1 2-2z") }
    val Mic by lazy { line("mic", "M12 3a3 3 0 0 1 3 3v5a3 3 0 0 1-6 0V6a3 3 0 0 1 3-3z", "M5 11a7 7 0 0 0 14 0", "M12 18v3") }
    val MicOff by lazy { line("micoff", "M12 3a3 3 0 0 1 3 3v5a3 3 0 0 1-6 0V6a3 3 0 0 1 3-3z", "M5 11a7 7 0 0 0 14 0", "M12 18v3", "M4 4l16 16") }
    val Speaker by lazy { line("speaker", "M4 9v6h4l5 4V5L8 9z", "M16 9a4 4 0 0 1 0 6", "M18.5 6.5a8 8 0 0 1 0 11") }
    val Keypad by lazy { line("keypad", *Array(9) { i -> "M${5 + (i % 3) * 6} ${5 + (i / 3) * 6}h2v2h-2z" }) }
    val Plus by lazy { line("plus", "M12 5v14M5 12h14") }
    val Bluetooth by lazy { line("bluetooth", "M7 7l10 10-5 4V3l5 4L7 17") }
    val Record by lazy { line("record", "M12 3a9 9 0 1 0 0 18a9 9 0 1 0 0-18z", "M12 9a3 3 0 1 0 0 6a3 3 0 1 0 0-6z") }
    val Pause by lazy { line("pause", "M8 5v14M16 5v14") }
    val Video by lazy { line("video", "M3 7h12v10H3z", "M15 11l6-4v10l-6-4") }
    val Message by lazy { line("message", "M4 5h16v11H9l-5 4z") }
    val Close by lazy { line("close", "M6 6l12 12M18 6L6 18") }
    val Contacts by lazy { line("contacts", "M9 4a3.5 3.5 0 1 0 0 7a3.5 3.5 0 1 0 0-7z", "M2.5 20c.5-4 3-6 6.5-6s6 2 6.5 6", "M16 5a3.5 3.5 0 0 1 0 7", "M18 14c2 .7 3.2 2.6 3.5 6") }
    val Vibe by lazy { line("vibe", "M12 3l1.8 5.2L19 10l-5.2 1.8L12 17l-1.8-5.2L5 10l5.2-1.8z", "M19 16l.8 2.2L22 19l-2.2.8L19 22l-.8-2.2L16 19l2.2-.8z") }
    val Gear by lazy { line("gear", "M12 9a3 3 0 1 0 0 6a3 3 0 1 0 0-6z", "M12 2v3M12 19v3M2 12h3M19 12h3M4.9 4.9L7 7M17 17l2.1 2.1M4.9 19.1L7 17M17 7l2.1-2.1") }
    val Back by lazy { line("back", "M15 6l-6 6 6 6") }
    val Backspace by lazy { line("backspace", "M9 5h11v14H9l-6-7z", "M12 9l5 6M17 9l-5 6") }
    val Menu by lazy { line("menu", "M4 8h16M4 12h16M4 16h16") }
    val Camera by lazy { line("camera", "M4 8h3l2-3h6l2 3h3v11H4z", "M12 10a3.5 3.5 0 1 0 0 7a3.5 3.5 0 1 0 0-7z") }
    val Star by lazy { line("star", "M12 3l2.7 5.6 6.1.9-4.4 4.3 1 6.1L12 17l-5.4 2.9 1-6.1L3.2 9.5l6.1-.9z") }
    val Check by lazy { line("check", "M5 12l5 5L20 7") }
    val Chevron by lazy { line("chevron", "M9 6l6 6-6 6") }
    val Wave by lazy { line("wave", "M3 12h2M7 8v8M11 4v16M15 8v8M19 10v4") }
    val Image by lazy { line("image", "M4 5h16v14H4z", "M4 16l5-5 4 4 3-3 4 4") }
    val Note by lazy { line("note", "M9 18V6l10-2v12", "M7 16a2 2 0 1 0 0 4a2 2 0 1 0 0-4z", "M17 14a2 2 0 1 0 0 4a2 2 0 1 0 0-4z") }
    val Search by lazy { line("search", "M11 4a7 7 0 1 0 0 14a7 7 0 1 0 0-14z", "M16.5 16.5L21 21") }
    val Play by lazy { make("play", true, "M8 5l12 7-12 7z") }
    val Stop by lazy { make("stop", true, "M6 6h12v12H6z") }
}
