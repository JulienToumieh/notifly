package com.thatonedev.notifly.activities

import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.icu.util.Calendar
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.widget.Button
import android.widget.Switch
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.CreateDocument
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import com.google.android.material.chip.Chip
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.slider.Slider
import com.thatonedev.notifly.MainActivity
import com.thatonedev.notifly.R
import org.json.JSONArray
import java.io.File
import java.util.*


class SettingsActivity : AppCompatActivity() {

    private var sndDelay = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_settings)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val activeDayColor = TypedValue()
        val inactiveDayColor = TypedValue()
        this.theme.resolveAttribute(com.google.android.material.R.attr.colorPrimaryContainer, activeDayColor, true)
        this.theme.resolveAttribute(Color.TRANSPARENT, inactiveDayColor, true)


        val sharedPreferences = getSharedPreferences("Settings", MODE_PRIVATE)

        findViewById<MaterialSwitch>(R.id.enable_active_rule_hours_switch).isChecked =
            sharedPreferences.getBoolean("activeHoursEnabled", false)

        findViewById<MaterialSwitch>(R.id.enable_override_notification_volume_switch).isChecked =
            sharedPreferences.getBoolean("overrideNotificationVolumeEnabled", false)


        if (findViewById<MaterialSwitch>(R.id.enable_override_notification_volume_switch).isChecked)
            findViewById<ConstraintLayout>(R.id.edit_override_notification_volume).visibility = View.VISIBLE
        else
            findViewById<ConstraintLayout>(R.id.edit_override_notification_volume).visibility = View.GONE

        findViewById<MaterialSwitch>(R.id.enable_override_notification_volume_switch).setOnClickListener {
            sharedPreferences.edit().putBoolean("overrideNotificationVolumeEnabled", findViewById<MaterialSwitch>(R.id.enable_override_notification_volume_switch).isChecked).apply()
            if (findViewById<MaterialSwitch>(R.id.enable_override_notification_volume_switch).isChecked)
                findViewById<ConstraintLayout>(R.id.edit_override_notification_volume).visibility = View.VISIBLE
            else
                findViewById<ConstraintLayout>(R.id.edit_override_notification_volume).visibility = View.GONE
        }

        findViewById<Slider>(R.id.rule_notification_volume_slider).value = sharedPreferences.getInt("overrideNotificationVolume", 50).toFloat()

        findViewById<Slider>(R.id.rule_notification_volume_slider).addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {
            }

            override fun onStopTrackingTouch(slider: Slider) {
                sharedPreferences.edit().putInt("overrideNotificationVolume", slider.value.toInt()).apply()
                triggerNotificationSound(this@SettingsActivity)
            }
        })

        val activeDays = JSONArray(sharedPreferences.getString("activeDays", "[mon,tue,wed,thu,fri,sat,sun]"))


        val monDayCard = findViewById<CardView>(R.id.active_day_mon_card)
        val tueDayCard = findViewById<CardView>(R.id.active_day_tue_card)
        val wedDayCard = findViewById<CardView>(R.id.active_day_wed_card)
        val thuDayCard = findViewById<CardView>(R.id.active_day_thu_card)
        val friDayCard = findViewById<CardView>(R.id.active_day_fri_card)
        val satDayCard = findViewById<CardView>(R.id.active_day_sat_card)
        val sunDayCard = findViewById<CardView>(R.id.active_day_sun_card)

        fun refreshDays() {
            monDayCard.setCardBackgroundColor(inactiveDayColor.data)
            tueDayCard.setCardBackgroundColor(inactiveDayColor.data)
            wedDayCard.setCardBackgroundColor(inactiveDayColor.data)
            thuDayCard.setCardBackgroundColor(inactiveDayColor.data)
            friDayCard.setCardBackgroundColor(inactiveDayColor.data)
            satDayCard.setCardBackgroundColor(inactiveDayColor.data)
            sunDayCard.setCardBackgroundColor(inactiveDayColor.data)

            for (i in 0 until activeDays.length()){
                when (activeDays[i]) {
                    "mon" ->  monDayCard.setCardBackgroundColor(activeDayColor.data)
                    "tue" ->  tueDayCard.setCardBackgroundColor(activeDayColor.data)
                    "wed" ->  wedDayCard.setCardBackgroundColor(activeDayColor.data)
                    "thu" ->  thuDayCard.setCardBackgroundColor(activeDayColor.data)
                    "fri" ->  friDayCard.setCardBackgroundColor(activeDayColor.data)
                    "sat" ->  satDayCard.setCardBackgroundColor(activeDayColor.data)
                    "sun" ->  sunDayCard.setCardBackgroundColor(activeDayColor.data)
                }
            }
            sharedPreferences.edit().putString("activeDays",  activeDays.toString()).apply()
        }

        refreshDays()

        fun containsString(jsonArray: JSONArray, target: String): Int {
            for (i in 0 until jsonArray.length()) {
                if (jsonArray.getString(i) == target) {
                    return i
                }
            }
            return -1
        }


        monDayCard.setOnClickListener {
            val day = "mon"
            val dayIDX = containsString(activeDays, day)
            if (dayIDX == -1)
                activeDays.put(day)
            else
                activeDays.remove(dayIDX)

            refreshDays()
        }
        tueDayCard.setOnClickListener {
            val day = "tue"
            val dayIDX = containsString(activeDays, day)
            if (dayIDX == -1)
                activeDays.put(day)
            else
                activeDays.remove(dayIDX)

            refreshDays()
        }
        wedDayCard.setOnClickListener {
            val day = "wed"
            val dayIDX = containsString(activeDays, day)
            if (dayIDX == -1)
                activeDays.put(day)
            else
                activeDays.remove(dayIDX)

            refreshDays()
        }
        thuDayCard.setOnClickListener {
            val day = "thu"
            val dayIDX = containsString(activeDays, day)
            if (dayIDX == -1)
                activeDays.put(day)
            else
                activeDays.remove(dayIDX)

            refreshDays()
        }
        friDayCard.setOnClickListener {
            val day = "fri"
            val dayIDX = containsString(activeDays, day)
            if (dayIDX == -1)
                activeDays.put(day)
            else
                activeDays.remove(dayIDX)

            refreshDays()
        }
        satDayCard.setOnClickListener {
            val day = "sat"
            val dayIDX = containsString(activeDays, day)
            if (dayIDX == -1)
                activeDays.put(day)
            else
                activeDays.remove(dayIDX)

            refreshDays()
        }
        sunDayCard.setOnClickListener {
            val day = "sun"
            val dayIDX = containsString(activeDays, day)
            if (dayIDX == -1)
                activeDays.put(day)
            else
                activeDays.remove(dayIDX)

            refreshDays()
        }


        if (findViewById<MaterialSwitch>(R.id.enable_active_rule_hours_switch).isChecked)
            findViewById<ConstraintLayout>(R.id.edit_rule_active_hours).visibility = View.VISIBLE
        else
            findViewById<ConstraintLayout>(R.id.edit_rule_active_hours).visibility = View.GONE

        findViewById<MaterialSwitch>(R.id.enable_active_rule_hours_switch).setOnClickListener {
            sharedPreferences.edit().putBoolean("activeHoursEnabled", findViewById<MaterialSwitch>(R.id.enable_active_rule_hours_switch).isChecked).apply()
            if (findViewById<MaterialSwitch>(R.id.enable_active_rule_hours_switch).isChecked)
                findViewById<ConstraintLayout>(R.id.edit_rule_active_hours).visibility = View.VISIBLE
            else
                findViewById<ConstraintLayout>(R.id.edit_rule_active_hours).visibility = View.GONE
        }

        fun refreshActiveHours() {
            fun convertToAmPm(time24: String): String {
                val (hour, minute) = time24.split(":").map { it.toInt() }
                return "%02d:%02d %s".format((if (hour % 12 == 0) 12 else hour % 12), minute, if (hour < 12) "AM" else "PM")
            }
            val fromHour = sharedPreferences.getString("activeHoursFrom", "07:00")
            val toHour = sharedPreferences.getString("activeHoursTo", "18:00")

            findViewById<Chip>(R.id.edit_active_from_hour_chip).text = fromHour?.let { convertToAmPm(it) }
            findViewById<Chip>(R.id.edit_active_to_hour_chip).text = toHour?.let { convertToAmPm(it) }
        }
        refreshActiveHours()

        findViewById<Chip>(R.id.edit_active_from_hour_chip).setOnClickListener {
            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)

            val timePickerDialog = TimePickerDialog(
                this,
                { _, selectedHour, selectedMinute ->
                    val formattedTime = String.format(Locale.US, "%02d:%02d", selectedHour, selectedMinute)
                    sharedPreferences.edit().putString("activeHoursFrom", formattedTime).apply()
                    refreshActiveHours()
                },
                hour,
                minute,
                false
            )

            timePickerDialog.show()
        }

        findViewById<Chip>(R.id.edit_active_to_hour_chip).setOnClickListener {
            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)

            val timePickerDialog = TimePickerDialog(
                this,
                { _, selectedHour, selectedMinute ->
                    val formattedTime = String.format(Locale.US, "%02d:%02d", selectedHour, selectedMinute)
                    sharedPreferences.edit().putString("activeHoursTo", formattedTime).apply()
                    refreshActiveHours()
                },
                hour,
                minute,
                false
            )

            timePickerDialog.show()
        }

        findViewById<ConstraintLayout>(R.id.backup_rules_setting).setOnClickListener {
            findViewById<CardView>(R.id.backup_rules_popup).visibility = View.VISIBLE
        }

        findViewById<CardView>(R.id.backup_rules_popup).setOnClickListener {
            findViewById<CardView>(R.id.backup_rules_popup).visibility = View.GONE
        }

        findViewById<Button>(R.id.backup_rules_btn).setOnClickListener {
            onBackupButtonClick()
        }

        findViewById<Button>(R.id.restore_rules_btn).setOnClickListener {
            onRestoreButtonClick()
        }

    }


    private fun triggerNotificationSound(context: Context) {
        if (sndDelay) return

        val sharedPreferences = getSharedPreferences("Settings", MODE_PRIVATE)
        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val originalMediaVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)

        val overrideVolume = sharedPreferences.getInt("overrideNotificationVolume", 50)
        val maxMusicVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val notificationVolume = ((overrideVolume / 100f) * maxMusicVolume).toInt()


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


    private fun saveRulesToFile(context: Context, ruleArray: JSONArray) {
        val file = File(context.filesDir, "rules.json")
        file.writeText(ruleArray.toString())
    }

    private fun loadRulesFromFile(context: Context): JSONArray {
        val file = File(context.filesDir, "rules.json")
        if (file.exists()) {
            val jsonString = file.readText()
            return JSONArray(jsonString)
        }
        return JSONArray()
    }

    private val backupLauncher = registerForActivityResult(CreateDocument("todo/todo")) { uri: Uri? ->
        uri?.let {
            backupRulesToUri(it)
        }
    }

    private val restoreLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        uri?.let {
            restoreRulesFromUri(it)
        }
    }

    private fun backupRulesToUri(uri: Uri) {
        val ruleArray = loadRulesFromFile(this)
        val jsonString = ruleArray.toString()
        contentResolver.openOutputStream(uri)?.use { outputStream ->
            outputStream.write(jsonString.toByteArray())
        }
        findViewById<CardView>(R.id.backup_rules_popup).visibility = View.GONE
    }

    private fun restoreRulesFromUri(uri: Uri) {
        contentResolver.openInputStream(uri)?.use { inputStream ->
            val jsonString = inputStream.bufferedReader().use { it.readText() }
            val ruleArray = JSONArray(jsonString)
            saveRulesToFile(this, ruleArray)
            findViewById<CardView>(R.id.backup_rules_popup).visibility = View.GONE
        }
    }

    private fun onBackupButtonClick() {
        backupLauncher.launch("Notifly_Backup.json")
    }

    private fun onRestoreButtonClick() {
        restoreLauncher.launch(arrayOf("*/*"))
    }

    override fun onBackPressed() {
        if (findViewById<CardView>(R.id.backup_rules_popup).isVisible)
            findViewById<CardView>(R.id.backup_rules_popup).visibility = View.GONE
        else {
            super.onBackPressed()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

    }

}