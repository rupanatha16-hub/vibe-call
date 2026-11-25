package com.vibecall.app

import android.os.Build
import android.os.Bundle
import android.telecom.Call
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import kotlinx.coroutines.delay

/** The screen shown on top of the lock screen for every incoming and outgoing call. */
class InCallActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Prefs.init(this)
        if (Build.VERSION.SDK_INT >= 27) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        enableEdgeToEdge()
        setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                CallScreen(
                    onFinish = { finishAndRemoveTask() },
                    onBackground = { moveTaskToBack(true) }
                )
            }
        }
    }
}

@Composable
fun CallScreen(onFinish: () -> Unit, onBackground: () -> Unit = {}) {
    val state = CallState.state
    val number = CallState.callerNumber
    val name = CallState.callerName.ifBlank { number.ifBlank { "Unknown" } }
    val photo = Prefs.photoFor(number)
    val ended = CallState.call == null

    LaunchedEffect(ended) {
        if (ended) {
            delay(700)
            onFinish()
        }
    }

    // Back: close the keypad first. Otherwise send the screen to the background; the call keeps running.
    BackHandler {
        if (CallState.showKeypad) CallState.showKeypad = false else onBackground()
    }

    Box(Modifier.fillMaxSize()) {
        if (state == Call.STATE_RINGING) {
            IncomingCallUi(
                style = Prefs.incomingFor(number),
                name = name,
                number = number,
                photo = photo,
                onAnswer = { CallState.answer() },
                onDecline = { CallState.reject() }
            )
        } else {
            OutgoingCallUi(
                style = Prefs.outgoing,
                name = name,
                number = number,
                photo = photo,
                status = CallState.statusLabel(rememberElapsed()),
                onEnd = { CallState.hangup() }
            )
        }
    }
}
