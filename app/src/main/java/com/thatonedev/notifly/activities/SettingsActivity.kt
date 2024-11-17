package com.thatonedev.notifly.activities

import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.icu.util.Calendar
import android.net.Uri
import android.os.Bundle
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
import com.thatonedev.notifly.MainActivity
import com.thatonedev.notifly.R
import org.json.JSONArray
import java.io.File


class SettingsActivity : AppCompatActivity() {
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

        findViewById<Switch>(R.id.enable_active_rule_hours_switch).isChecked = 
            sharedPreferences.getBoolean("activeHoursEnabled", false)


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
                    return i  // Return the index if the item is found
                }
            }
            return -1  // Return -1 if the item is not found
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


        if (findViewById<Switch>(R.id.enable_active_rule_hours_switch).isChecked)
            findViewById<ConstraintLayout>(R.id.edit_rule_active_hours).visibility = View.VISIBLE
        else
            findViewById<ConstraintLayout>(R.id.edit_rule_active_hours).visibility = View.GONE

        findViewById<Switch>(R.id.enable_active_rule_hours_switch).setOnClickListener {
            sharedPreferences.edit().putBoolean("activeHoursEnabled", findViewById<Switch>(R.id.enable_active_rule_hours_switch).isChecked).apply()
            if (findViewById<Switch>(R.id.enable_active_rule_hours_switch).isChecked)
                findViewById<ConstraintLayout>(R.id.edit_rule_active_hours).visibility = View.VISIBLE
            else
                findViewById<ConstraintLayout>(R.id.edit_rule_active_hours).visibility = View.GONE
        }

        findViewById<Chip>(R.id.edit_active_from_hour_chip).setOnClickListener {
            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)

            val timePickerDialog = TimePickerDialog(
                this,
                { _, selectedHour, selectedMinute ->

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

    fun onBackupButtonClick() {
        backupLauncher.launch("Notifly_Backup.json")
    }

    fun onRestoreButtonClick() {
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