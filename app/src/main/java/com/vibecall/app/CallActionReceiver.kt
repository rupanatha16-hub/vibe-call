package com.vibecall.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class CallActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_ANSWER -> CallState.answer()
            ACTION_REJECT -> CallState.reject()
            ACTION_HANGUP -> CallState.hangup()
            ACTION_SPEAKER -> CallState.toggleSpeaker()
            ACTION_MUTE -> CallState.toggleMute()
        }
    }

    companion object {
        const val ACTION_ANSWER = "com.vibecall.app.ANSWER"
        const val ACTION_REJECT = "com.vibecall.app.REJECT"
        const val ACTION_HANGUP = "com.vibecall.app.HANGUP"
        const val ACTION_SPEAKER = "com.vibecall.app.SPEAKER"
        const val ACTION_MUTE = "com.vibecall.app.MUTE"
    }
}
