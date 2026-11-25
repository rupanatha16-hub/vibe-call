package com.vibecall.app

import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.ContactsContract
import android.telecom.Call
import android.telecom.CallAudioState
import android.telecom.InCallService
import android.telecom.VideoProfile
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/** The one live call the UI is showing. Compose reads these values directly. */
@Suppress("DEPRECATION")
object CallState {
    var service: InCallService? = null
    var call by mutableStateOf<Call?>(null)
    var state by mutableIntStateOf(Call.STATE_NEW)
    var callerNumber by mutableStateOf("")
    var callerName by mutableStateOf("")
    var muted by mutableStateOf(false)
    var speaker by mutableStateOf(false)
    var recording by mutableStateOf(false)
    var held by mutableStateOf(false)
    var showKeypad by mutableStateOf(false)
    var connectedAt by mutableLongStateOf(0L)

    private val cb = object : Call.Callback() {
        override fun onStateChanged(call: Call, state: Int) {
            update(call)
        }
    }

    fun attach(ctx: Context, c: Call) {
        call?.unregisterCallback(cb)
        call = c
        c.registerCallback(cb)
        val num = c.details.handle?.schemeSpecificPart ?: ""
        callerNumber = num
        callerName = lookupName(ctx, num) ?: ""
        muted = false
        speaker = false
        recording = false
        held = false
        showKeypad = false
        connectedAt = 0L
        update(c)
    }

    fun detach(c: Call) {
        if (call == c) {
            c.unregisterCallback(cb)
            call = null
            state = Call.STATE_DISCONNECTED
        }
    }

    private fun update(c: Call) {
        state = c.state
        if (state == Call.STATE_ACTIVE && connectedAt == 0L) connectedAt = System.currentTimeMillis()
        held = state == Call.STATE_HOLDING
    }

    fun answer() { call?.answer(VideoProfile.STATE_AUDIO_ONLY) }
    fun reject() { call?.reject(false, null) }
    fun hangup() { call?.disconnect() }

    fun toggleMute() {
        muted = !muted
        service?.setMuted(muted)
    }

    fun toggleSpeaker() {
        speaker = !speaker
        service?.setAudioRoute(
            if (speaker) CallAudioState.ROUTE_SPEAKER else CallAudioState.ROUTE_WIRED_OR_EARPIECE
        )
    }

    fun toggleHold() {
        val c = call ?: return
        if (held) c.unhold() else c.hold()
    }

    /** Only the button state changes. Real call recording is not implemented in this version. */
    fun toggleRecord() { recording = !recording }

    fun dtmf(ch: Char) {
        call?.playDtmfTone(ch)
        Handler(Looper.getMainLooper()).postDelayed({ call?.stopDtmfTone() }, 150)
    }

    fun statusLabel(elapsed: String): String = when (state) {
        Call.STATE_RINGING -> "Incoming call"
        Call.STATE_DIALING, Call.STATE_CONNECTING, Call.STATE_PULLING_CALL -> "Calling…"
        Call.STATE_ACTIVE -> elapsed.ifBlank { "Connected" }
        Call.STATE_HOLDING -> "On hold"
        Call.STATE_DISCONNECTING, Call.STATE_DISCONNECTED -> "Call ended"
        else -> "Connecting…"
    }

    fun lookupName(ctx: Context, number: String): String? {
        if (number.isBlank()) return null
        return try {
            val uri = Uri.withAppendedPath(
                ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number)
            )
            ctx.contentResolver.query(
                uri, arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME), null, null, null
            )?.use { if (it.moveToFirst()) it.getString(0) else null }
        } catch (e: Exception) {
            null
        }
    }
}
