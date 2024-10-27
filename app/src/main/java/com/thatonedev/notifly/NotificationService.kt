package com.thatonedev.notifly

import android.app.Notification
import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log

class NotificationService : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val packageName = sbn.packageName
        val notificationTitle = sbn.notification.extras.getString(Notification.EXTRA_TITLE, "")
        val notificationText = sbn.notification.extras.getString(Notification.EXTRA_TEXT, "")

        // Check if notification matches criteria
        if (shouldCustomizeNotification(packageName, notificationTitle, notificationText)) {
            triggerCustomVibrationAndSound()
        }
    }

    private fun shouldCustomizeNotification(packageName: String, title: String, text: String): Boolean {
        Log.d("ayy",packageName + " - " + title + " - " + text)
        // Define which apps and notification content you want to customize.
        return packageName == "com.whatsapp" && title.contains("specific keyword") && text.contains("important message")

    }

    private fun triggerCustomVibrationAndSound() {
        // Trigger custom vibration pattern
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        val vibrationPattern = longArrayOf(0, 200, 100, 300) // Custom pattern
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val vibrationEffect = VibrationEffect.createWaveform(vibrationPattern, -1)
            vibrator.vibrate(vibrationEffect)
        } else {
            vibrator.vibrate(vibrationPattern, -1)
        }

        // Play custom sound
        val soundUri: Uri = Uri.parse("android.resource://${packageName}/raw/custom_sound")
        val mediaPlayer = MediaPlayer.create(this, soundUri)
        mediaPlayer.setOnCompletionListener { it.release() }
        mediaPlayer.start()
    }
}