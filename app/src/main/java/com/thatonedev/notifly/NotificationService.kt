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
import org.json.JSONArray
import org.json.JSONObject

class NotificationService : NotificationListenerService() {

    val task1 = JSONObject().apply {
        put("name", "Click on the checkbox to complete task.")
        put("active", false)
        put("apps", "[]")
        put("vibration", "V100,S100,V100")
        put("sound", "i have no idea")
        put("containsType", 0) // 0 -> Entire Notification, 1 -> Title, 2 -> Text
        put("containsData", "[]")
        put("containsOperation", "OR") // AND, OR
    }

    val task2 = JSONObject().apply {
        put("name", "Click and hold on a task to delete.")
        put("active", true)
        put("apps", "[]")
        put("vibration", "V1000,S100,V100")
        put("sound", "i have no idea")
        put("containsType", "Notification") // Notification, Title, Text
        put("containsData", "[]")
        put("containsOperation", "OR") // AND, OR
    }


    val ruleArray = JSONArray().apply {
        put(task1)
        put(task2)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val packageName = sbn.packageName
        val notificationTitle = sbn.notification.extras.getString(Notification.EXTRA_TITLE, "")
        val notificationText = sbn.notification.extras.getString(Notification.EXTRA_TEXT, "")

        // Check if notification matches criteria
        if (shouldCustomizeNotification(packageName, notificationTitle, notificationText) != -1) {
            triggerCustomVibrationAndSound()
        }
    }

    private fun shouldCustomizeNotification(packageName: String, title: String, text: String): Int {
        Log.d("notification",packageName + " - " + title + " - " + text)

        for (i in 0 until ruleArray.length()) {
            val rule = ruleArray.getJSONObject(i)
            val apps = JSONArray(rule.getString("apps"))
            val containsData = JSONArray(rule.getString("containsData"))
            val containsOperation = rule.getString("containsOperation")

            var notiData = ""
            when (rule.getString("containsType")) {
                "Notification" -> notiData = title + " " + text
                "Title" -> notiData = title
                "Text" -> notiData = text
            }

            if (rule.getBoolean("active")) {

                for (app in 0 until apps.length()) {
                    if (apps[app] == packageName || apps.length() == 0){

                        if (rule.getString("containsType") == "All Notifications"){
                            return i
                        } else {
                            if (containsOperation == "OR"){
                                for (contData in 0 until containsData.length()){
                                    if (notiData.contains(containsData[contData].toString())){
                                        return i
                                    }
                                }
                            } else if (containsOperation == "AND") {
                                var count = 0
                                for (contData in 0 until containsData.length()){
                                    if (notiData.contains(containsData[contData].toString())){
                                        count++
                                    }
                                }
                                if (count == containsData.length()){
                                    return i
                                }
                            }
                        }
                    }
                }
            }
        }
        return -1

        //return packageName == "com.whatsapp" && title.contains("specific keyword") && text.contains("important message")

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