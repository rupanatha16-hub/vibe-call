package com.vibecall.app

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.speech.tts.TextToSpeech
import android.telecom.Call
import android.telecom.InCallService
import java.util.Locale

@Suppress("DEPRECATION")
class VibeInCallService : InCallService() {
    private var ringtone: Ringtone? = null
    private var tts: TextToSpeech? = null

    private val ringCb = object : Call.Callback() {
        override fun onStateChanged(call: Call, state: Int) {
            if (state != Call.STATE_RINGING) stopRinging()
        }
    }

    override fun onCreate() {
        super.onCreate()
        Prefs.init(this)
    }

    override fun onCallAdded(call: Call) {
        super.onCallAdded(call)
        CallState.service = this
        CallState.attach(this, call)
        if (call.state == Call.STATE_RINGING) {
            call.registerCallback(ringCb)
            startRinging()
            showIncomingNotification()
        }
        openUi()
    }

    override fun onCallRemoved(call: Call) {
        super.onCallRemoved(call)
        stopRinging()
        CallState.detach(call)
        cancelNotification(this)
        if (calls.isEmpty()) CallState.service = null
    }

    override fun onDestroy() {
        stopRinging()
        super.onDestroy()
    }

    private fun openUi() {
        val i = Intent(this, InCallActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        try {
            startActivity(i)
        } catch (e: Exception) {
            // The full-screen notification below is the fallback.
        }
    }

    private fun showIncomingNotification() {
        val nm = getSystemService(NotificationManager::class.java)
        val ch = NotificationChannel(CH, "Incoming calls", NotificationManager.IMPORTANCE_HIGH)
        ch.setSound(null, null)
        ch.enableVibration(false)
        nm.createNotificationChannel(ch)
        val pi = PendingIntent.getActivity(
            this, 0, Intent(this, InCallActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val who = CallState.callerName.ifBlank { CallState.callerNumber.ifBlank { "Unknown" } }
        val n = Notification.Builder(this, CH)
            .setSmallIcon(android.R.drawable.sym_call_incoming)
            .setContentTitle(who)
            .setContentText("Incoming call")
            .setCategory(Notification.CATEGORY_CALL)
            .setOngoing(true)
            .setContentIntent(pi)
            .setFullScreenIntent(pi, true)
            .build()
        nm.notify(ID, n)
    }

    // This service handles ringing itself (IN_CALL_SERVICE_RINGING = true in the manifest).
    private fun startRinging() {
        val am = getSystemService(AudioManager::class.java)
        val mode = am.ringerMode

        if (mode == AudioManager.RINGER_MODE_NORMAL) {
            val r = RingtoneManager.getRingtone(
                this, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            )
            r.audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
            if (Build.VERSION.SDK_INT >= 28) r.isLooping = true
            r.play()
            ringtone = r
        }

        if (Prefs.vibrate && mode != AudioManager.RINGER_MODE_SILENT) {
            val v = getSystemService(Vibrator::class.java)
            v.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 700, 500), 0))
        }

        if (Prefs.announce && mode != AudioManager.RINGER_MODE_SILENT) {
            val who = CallState.callerName.ifBlank { "An unknown number" }
            tts = TextToSpeech(applicationContext) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    tts?.language = Locale.getDefault()
                    tts?.speak("$who is calling", TextToSpeech.QUEUE_FLUSH, null, "vibe")
                }
            }
        }
    }

    private fun stopRinging() {
        try { ringtone?.stop() } catch (e: Exception) { }
        ringtone = null
        try { getSystemService(Vibrator::class.java).cancel() } catch (e: Exception) { }
        try { tts?.stop(); tts?.shutdown() } catch (e: Exception) { }
        tts = null
    }

    companion object {
        const val CH = "vibe_incoming"
        const val ID = 42
        fun cancelNotification(c: Context) {
            c.getSystemService(NotificationManager::class.java).cancel(ID)
        }
    }
}
