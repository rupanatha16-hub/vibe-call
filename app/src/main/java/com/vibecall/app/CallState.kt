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

    /** Service sets this. It is called whenever the call state, mute or speaker changes, so the notification can refresh. */
    var onChanged: (() -> Unit)? = null

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
        recording
