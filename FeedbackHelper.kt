package com.brainrush.app.presentation.components

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.*

class FeedbackHelper(context: Context) {
    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    fun playCorrect(soundEnabled: Boolean, vibrationEnabled: Boolean) {
        if (soundEnabled) runCatching { ToneGenerator(AudioManager.STREAM_MUSIC, 65).startTone(ToneGenerator.TONE_PROP_ACK, 90) }
        if (vibrationEnabled) runCatching { vibrator?.vibrate(VibrationEffect.createOneShot(25L, VibrationEffect.DEFAULT_AMPLITUDE)) }
    }

    fun playWrong(soundEnabled: Boolean, vibrationEnabled: Boolean) {
        if (soundEnabled) runCatching { ToneGenerator(AudioManager.STREAM_MUSIC, 70).startTone(ToneGenerator.TONE_PROP_NACK, 150) }
        if (vibrationEnabled) runCatching { vibrator?.vibrate(VibrationEffect.createOneShot(80L, VibrationEffect.DEFAULT_AMPLITUDE)) }
    }
}
