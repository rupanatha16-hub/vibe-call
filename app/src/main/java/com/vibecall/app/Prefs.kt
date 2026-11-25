package com.vibecall.app

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.io.File

/** Saved choices. Everything here is Compose state, so screens update instantly. */
object Prefs {
    private lateinit var sp: SharedPreferences
    private var ready = false

    var theme by mutableIntStateOf(0)
    var incoming by mutableIntStateOf(0)
    var outgoing by mutableIntStateOf(0)
    var iconStyle by mutableIntStateOf(0)
    var numberStyle by mutableIntStateOf(0)
    var announce by mutableStateOf(true)
    var vibrate by mutableStateOf(true)
    var myPhoto by mutableStateOf("")

    val contactIncoming = mutableStateMapOf<String, Int>()
    val contactPhotos = mutableStateMapOf<String, String>()

    fun key(number: String): String = number.filter { it.isDigit() }.takeLast(10)

    fun init(c: Context) {
        if (ready) return
        sp = c.applicationContext.getSharedPreferences("vibe", Context.MODE_PRIVATE)
        theme = sp.getInt("theme", 0)
        incoming = sp.getInt("incoming", 0)
        outgoing = sp.getInt("outgoing", 0)
        iconStyle = sp.getInt("iconStyle", 0)
        numberStyle = sp.getInt("numberStyle", 0)
        announce = sp.getBoolean("announce", true)
        vibrate = sp.getBoolean("vibrate", true)
        myPhoto = sp.getString("myPhoto", "") ?: ""
        for ((k, v) in sp.all) {
            if (k.startsWith("ci_") && v is Int) contactIncoming[k.removePrefix("ci_")] = v
            if (k.startsWith("ph_") && v is String) contactPhotos[k.removePrefix("ph_")] = v
        }
        ready = true
    }

    /** Call after changing any of the simple values above. */
    fun save() {
        if (!ready) return
        sp.edit()
            .putInt("theme", theme)
            .putInt("incoming", incoming)
            .putInt("outgoing", outgoing)
            .putInt("iconStyle", iconStyle)
            .putInt("numberStyle", numberStyle)
            .putBoolean("announce", announce)
            .putBoolean("vibrate", vibrate)
            .putString("myPhoto", myPhoto)
            .apply()
    }

    fun incomingFor(number: String): Int = contactIncoming[key(number)] ?: incoming

    fun setContactIncoming(number: String, style: Int?) {
        val k = key(number)
        if (k.isEmpty()) return
        if (style == null) {
            contactIncoming.remove(k)
            sp.edit().remove("ci_$k").apply()
        } else {
            contactIncoming[k] = style
            sp.edit().putInt("ci_$k", style).apply()
        }
    }

    fun photoFor(number: String): String = contactPhotos[key(number)] ?: ""

    fun setContactPhoto(c: Context, number: String, uri: Uri) {
        val k = key(number)
        if (k.isEmpty()) return
        val path = copyPhoto(c, uri, k) ?: return
        contactPhotos[k] = path
        sp.edit().putString("ph_$k", path).apply()
    }

    fun setMyPhoto(c: Context, uri: Uri) {
        val path = copyPhoto(c, uri, "me") ?: return
        myPhoto = path
        save()
    }

    private fun copyPhoto(c: Context, uri: Uri, tag: String): String? = try {
        val f = File(c.filesDir, "photo_${tag}_${System.currentTimeMillis()}.jpg")
        c.contentResolver.openInputStream(uri)?.use { i -> f.outputStream().use { o -> i.copyTo(o) } }
        f.absolutePath
    } catch (e: Exception) {
        null
    }
}
