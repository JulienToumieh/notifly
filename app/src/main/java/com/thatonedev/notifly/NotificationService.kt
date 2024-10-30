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
import java.io.File

class NotificationService : NotificationListenerService() {

    lateinit var ruleArray: JSONArray

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        ruleArray = loadRulesFromFile(this)
        val packageName = sbn.packageName
        val notificationTitle = sbn.notification.extras.getString(Notification.EXTRA_TITLE, "")
        val notificationText = sbn.notification.extras.getString(Notification.EXTRA_TEXT, "")

        // Check if notification matches criteria
        val ruleId = shouldCustomizeNotification(packageName, notificationTitle, notificationText)
        if (ruleId != -1) {
            triggerCustomVibrationAndSound()
        }
    }

    private fun loadRulesFromFile(context: Context): JSONArray {
        val file = File(context.filesDir, "rules.json")
        if (file.exists()) {
            val jsonString = file.readText()
            return JSONArray(jsonString)
        }
        return JSONArray()
    }

    private fun shouldCustomizeNotification(packageName: String, title: String, text: String): Int {
        Log.d("notification",packageName + " - " + title + " - " + text)

        for (i in 0 until ruleArray.length()) {
            val rule = ruleArray.getJSONObject(i)
            val apps = JSONArray(rule.getString("apps"))
            val keywords = JSONArray(rule.getString("keywords"))
            val keywordOperation = rule.getString("keywordOperation")

            var notiData = ""
            when (rule.getString("filterType")) {
                "Notification" -> notiData = title + " " + text
                "Title" -> notiData = title
                "Text" -> notiData = text
            }

            if (rule.getBoolean("active")) {

                if (apps.length() == 0){
                    if (rule.getString("filterType") == "All Notifications"){
                        return i
                    } else {
                        if (keywordOperation == "OR"){
                            for (contData in 0 until keywords.length()){
                                if (notiData.lowercase().contains(keywords[contData].toString().lowercase())){
                                    return i
                                }
                            }
                        } else if (keywordOperation == "AND") {
                            var count = 0
                            for (contData in 0 until keywords.length()){
                                if (notiData.lowercase().contains(keywords[contData].toString().lowercase())){
                                    count++
                                }
                            }
                            if (count == keywords.length()){
                                return i
                            }
                        }
                    }
                } else {
                    for (app in 0 until apps.length()) {
                        if (apps[app] == packageName) {

                            if (rule.getString("filterType") == "All Notifications") {
                                return i
                            } else {
                                if (keywordOperation == "OR") {
                                    for (contData in 0 until keywords.length()) {
                                        if (notiData.lowercase().contains(keywords[contData].toString().lowercase())) {
                                            return i
                                        }
                                    }
                                } else if (keywordOperation == "AND") {
                                    var count = 0
                                    for (contData in 0 until keywords.length()) {
                                        if (notiData.lowercase().contains(keywords[contData].toString().lowercase())) {
                                            count++
                                        }
                                    }
                                    if (count == keywords.length()) {
                                        return i
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return -1
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
        /*
        val soundUri: Uri = Uri.parse("android.resource://${packageName}/raw/custom_sound")
        val mediaPlayer = MediaPlayer.create(this, soundUri)
        mediaPlayer.setOnCompletionListener { it.release() }
        mediaPlayer.start()
        */
    }
}