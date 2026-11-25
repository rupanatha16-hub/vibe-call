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
            st
