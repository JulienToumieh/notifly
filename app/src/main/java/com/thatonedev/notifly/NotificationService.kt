package com.thatonedev.notifly

import android.app.Notification
import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import org.json.JSONArray
import java.io.File
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class NotificationService : NotificationListenerService() {

    private lateinit var ruleArray: JSONArray
    private var vibDelay = false
    private var sndDelay = false

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        ruleArray = loadRulesFromFile(this)
        val packageName = sbn.packageName
        val notificationTitle = sbn.notification.extras.getString(Notification.EXTRA_TITLE, "")
        val notificationText = sbn.notification.extras.getString(Notification.EXTRA_TEXT, "")


        val sharedPreferences = getSharedPreferences("Settings", MODE_PRIVATE)

        if (sharedPreferences.getBoolean("activeHoursEnabled", false)){
            val now = LocalTime.now()
            val from = LocalTime.parse(sharedPreferences.getString("activeHoursFrom", "07:00"))
            val to = LocalTime.parse(sharedPreferences.getString("activeHoursTo", "18:00"))

            if (from.isBefore(to)) if (!(now.isAfter(from) && now.isBefore(to))) return
            else if (!(now.isAfter(from) || now.isBefore(to))) return

            val activeDays = JSONArray(sharedPreferences.getString("activeDays", "[mon,tue,wed,thu,fri,sat,sun]"))
            val currentDay = LocalDate.now().format(DateTimeFormatter.ofPattern("EEE")).lowercase()
            var found = false
            for (i in 0 until activeDays.length())
                if (currentDay == activeDays.get(i)){
                    found = true
                    break
                }
            if (!found)
                return
        }


        if (!vibDelay && !sndDelay) {
            val ruleId = shouldCustomizeNotification(packageName, notificationTitle, notificationText)
            if (ruleId != -1) {
                val rule = ruleArray.getJSONObject(ruleId)
                val ringerMode = checkRingerMode(this)

                if (rule.getBoolean("vibration") && ringerMode != 0){
                    triggerCustomVibration(rule.getString("vibrationPattern"))
                }
                if (rule.getBoolean("sound") && (ringerMode == 2 || ringerMode == -1)){
                    triggerCustomSound(this, rule.getString("selectedSound"))
                }
            }
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
        Log.d("notification", "$packageName - $title - $text")

        for (i in 0 until ruleArray.length()) {
            val rule = ruleArray.getJSONObject(i)
            val apps = JSONArray(rule.getString("apps"))
            val keywords = JSONArray(rule.getString("keywords"))
            var keywordOperation = rule.getString("keywordOperation")
            val keywordInclusion = rule.getBoolean("keywordInclusion")

            if (!keywordInclusion) {
                keywordOperation = if (keywordOperation == "AND") "OR"
                else "AND"
            }


            var notiData = ""
            when (rule.getString("filterType")) {
                "Full Content" -> notiData = "$title $text"
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
                                if (notiData.lowercase().contains(keywords[contData].toString().lowercase()) == keywordInclusion){
                                    return i
                                }
                            }
                        } else if (keywordOperation == "AND") {
                            var count = 0
                            for (contData in 0 until keywords.length()){
                                if (notiData.lowercase().contains(keywords[contData].toString().lowercase()) == keywordInclusion){
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
                                        if (notiData.lowercase().contains(keywords[contData].toString().lowercase()) == keywordInclusion) {
                                            return i
                                        }
                                    }
                                } else if (keywordOperation == "AND") {
                                    var count = 0
                                    for (contData in 0 until keywords.length()) {
                                        if (notiData.lowercase().contains(keywords[contData].toString().lowercase()) == keywordInclusion) {
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


    /*private fun triggerCustomVibration(vibration: String) {
        val stringValues = vibration.trim('[', ']').split(",").map { it.trim() }
        val vibrationPattern = longArrayOf(0) + stringValues.map { it.toLong() }.toLongArray()

        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        val vibrationEffect = VibrationEffect.createWaveform(vibrationPattern, -1)
        vibrator.vibrate(vibrationEffect)
    }

    private fun triggerCustomSound(context: Context, soundUriString: String) {
        val soundUri = Uri.parse(soundUriString)

        val mediaPlayer = MediaPlayer().apply {
            setDataSource(context, soundUri)
            prepare()
            start()
        }

        mediaPlayer.setOnCompletionListener {
            it.release()
        }
    }*/


    private fun checkRingerMode(context: Context): Int { // 0 -> Silent, 1 -> Vibrate, 2 -> Normal
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        when (audioManager.ringerMode) {
            AudioManager.RINGER_MODE_SILENT ->
                return 0
            AudioManager.RINGER_MODE_VIBRATE ->
                return 1
            AudioManager.RINGER_MODE_NORMAL ->
                return 2
        }
        return -1
    }


    private fun triggerCustomVibration(vibration: String) {
        val stringValues = vibration.trim('[', ']').split(",").map { it.trim() }
        val vibrationPattern = longArrayOf(0) + stringValues.map { it.toLong() }.toLongArray()

        vibDelay = true

        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        val vibrationEffect = VibrationEffect.createWaveform(vibrationPattern, -1)
        vibrator.vibrate(vibrationEffect)

        val totalVibrationDuration = vibrationPattern.sum()

        Handler(Looper.getMainLooper()).postDelayed({
            vibDelay = false
        }, totalVibrationDuration)
    }

    private fun triggerCustomSound(context: Context, soundUriString: String) {
        val soundUri = Uri.parse(soundUriString)

        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

        val originalMediaVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)

        val notificationVolume = audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION)
        val maxNotificationVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION)

        val mediaPlayerVolume = notificationVolume.toFloat() / maxNotificationVolume.toFloat()

        val wasPlaying = audioManager.isMusicActive
        if (wasPlaying) {
            audioManager.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
        }

        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, notificationVolume, 0)

        sndDelay = true

        val mediaPlayer = MediaPlayer().apply {
            setDataSource(context, soundUri)
            prepare()
            start()
            setVolume(mediaPlayerVolume, mediaPlayerVolume)
        }

        mediaPlayer.setOnCompletionListener {
            sndDelay = false
            it.release()

            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, originalMediaVolume, 0)

            if (wasPlaying) {
                audioManager.abandonAudioFocus(null)
            }
        }
    }

}