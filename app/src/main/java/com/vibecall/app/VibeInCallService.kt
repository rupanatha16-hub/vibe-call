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
import android.os.SystemClock
import android.os.VibrationEffect
import android.os.Vibrator
import android.speech.tts.TextToSpeech
import android.telecom.Call
import android.telecom.CallAudioState
import android.telecom.InCallService
import android.view.View
import android.widget.RemoteViews
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
        val nm = getSystemService(NotificationManager::class.java)
        val inc = NotificationChannel(CH, "Incoming calls", NotificationManager.IMPORTANCE_HIGH)
        inc.setSound(null, null)
        inc.enableVibration(false)
        nm.createNotificationChannel(inc)
        val ong = NotificationChannel(CH_ONGOING, "Ongoing call", NotificationManager.IMPORTANCE_LOW)
        ong.setSound(null, null)
        ong.enableVibration(false)
        nm.createNotificationChannel(ong)
    }

    override fun onCallAdded(call: Call) {
        super.onCallAdded(call)
        CallState.service = this
        CallState.onChanged = { refreshNotification() }
        CallState.attach(this, call)
        if (call.state == Call.STATE_RINGING) {
            call.registerCallback(ringCb)
            startRinging()
        }
        refreshNotification()
        openUi()
    }

    override fun onCallRemoved(call: Call) {
        super.onCallRemoved(call)
        stopRinging()
        CallState.detach(call)
        cancelNotification(this)
        if (calls.isEmpty()) {
            CallState.service = null
            CallState.onChanged = null
        }
    }

    override fun onCallAudioStateChanged(audioState: CallAudioState?) {
        super.onCallAudioStateChanged(audioState)
        CallState.syncAudio(audioState)
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
            // The full-screen notification is the fallback.
        }
    }

    private fun actionPi(action: String, code: Int): PendingIntent {
        val i = Intent(this, CallActionReceiver::class.java).setAction(action)
        return PendingIntent.getBroadcast(
            this, code, i, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun setBtn(
        rv: RemoteViews, id: Int, iconId: Int, labelId: Int,
        icon: Int, label: String, bg: Int, pi: PendingIntent
    ) {
        rv.setViewVisibility(id, View.VISIBLE)
        rv.setImageViewResource(iconId, icon)
        rv.setTextViewText(labelId, label)
        rv.setInt(id, "setBackgroundResource", bg)
        rv.setOnClickPendingIntent(id, pi)
    }

    private fun refreshNotification() {
        if (CallState.call == null) return
        val nm = getSystemService(NotificationManager::class.java)
        val ringing = CallState.state == Call.STATE_RINGING
        val active = CallState.state == Call.STATE_ACTIVE

        val number = CallState.callerNumber
        val name = CallState.callerName
        val who = name.ifBlank { number.ifBlank { "Unknown" } }
        val first = who.firstOrNull { it.isLetterOrDigit() }?.uppercase() ?: "#"

        val rv = RemoteViews(packageName, R.layout.notif_call_0)
        rv.setTextViewText(R.id.n_avatar, first)
        rv.setTextViewText(R.id.n_name, who)

        val parts = ArrayList<String>()
        if (name.isNotBlank() && number.isNotBlank()) parts.add(number)
        if (active) {
            val text = if (parts.isEmpty()) "" else parts.joinToString(" · ") + " · "
            rv.setTextViewText(R.id.n_status, text)
            rv.setViewVisibility(R.id.n_timer, View.VISIBLE)
            val base = SystemClock.elapsedRealtime() -
                (System.currentTimeMillis() - CallState.connectedAt).coerceAtLeast(0L)
            rv.setChronometer(R.id.n_timer, base, null, true)
        } else {
            parts.add(CallState.statusLabel(""))
            rv.setTextViewText(R.id.n_status, parts.joinToString(" · "))
            rv.setViewVisibility(R.id.n_timer, View.GONE)
        }

        if (ringing) {
            setBtn(rv, R.id.n_b1, R.id.n_b1_icon, R.id.n_b1_text,
                R.drawable.ic_n_hangup, "Decline", R.drawable.bg_n_red,
                actionPi(CallActionReceiver.ACTION_REJECT, 1))
            setBtn(rv, R.id.n_b2, R.id.n_b2_icon, R.id.n_b2_text,
                R.drawable.ic_n_phone, "Answer", R.drawable.bg_n_green,
                actionPi(CallActionReceiver.ACTION_ANSWER, 2))
            rv.setViewVisibility(R.id.n_b3, View.GONE)
        } else {
            setBtn(rv, R.id.n_b1, R.id.n_b1_icon, R.id.n_b1_text,
                R.drawable.ic_n_speaker, "Speaker",
                if (CallState.speaker) R.drawable.bg_n_on else R.drawable.bg_n_btn,
                actionPi(CallActionReceiver.ACTION_SPEAKER, 3))
            setBtn(rv, R.id.n_b2, R.id.n_b2_icon, R.id.n_b2_text,
                R.drawable.ic_n_mic, if (CallState.muted) "Unmute" else "Mute",
                if (CallState.muted) R.drawable.bg_n_on else R.drawable.bg_n_btn,
                actionPi(CallActionReceiver.ACTION_MUTE, 4))
            setBtn(rv, R.id.n_b3, R.id.n_b3_icon, R.id.n_b3_text,
                R.drawable.ic_n_hangup, "Hang up", R.drawable.bg_n_red,
                actionPi(CallActionReceiver.ACTION_HANGUP, 5))
        }

        val open = PendingIntent.getActivity(
            this, 0,
            Intent(this, InCallActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val b = Notification.Builder(this, if (ringing) CH else CH_ONGOING)
            .setSmallIcon(R.drawable.ic_n_phone)
            .setContentTitle(who)
            .setCategory(Notification.CATEGORY_CALL)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setStyle(Notification.DecoratedCustomViewStyle())
            .setCustomContentView(rv)
            .setCustomBigContentView(rv)
            .setContentIntent(open)
        if (ringing) b.setFullScreenIntent(open, true)
        nm.notify(ID, b.build())
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
        const val CH_ONGOING = "vibe_ongoing"
        const val ID = 42
        fun cancelNotification(c: Context) {
            c.getSystemService(NotificationManager::class.java).cancel(ID)
        }
    }
}
