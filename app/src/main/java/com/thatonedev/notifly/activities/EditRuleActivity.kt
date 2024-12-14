package com.thatonedev.notifly.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.materialswitch.MaterialSwitch
import com.thatonedev.notifly.MainActivity
import com.thatonedev.notifly.R
import com.thatonedev.notifly.components.VibrationStepComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class EditRuleActivity : AppCompatActivity() {

    private lateinit var adapter: AppAdapter
    private lateinit var ruleSelectedAppsRecycler: RecyclerView
    private lateinit var rule: JSONObject


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_edit_rule)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val filterTypeSpinnerOptions = listOf("All Notifications", "Full Content", "Title", "Text")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, filterTypeSpinnerOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        val ruleId = intent.getIntExtra("RULE_ID", 0)
        rule = loadRulesFromFile(this).getJSONObject(ruleId)

        var vibrationStepMode = 1


        val ruleName = findViewById<TextView>(R.id.edit_rule_name)
        val ruleNameEditPopup = findViewById<CardView>(R.id.edit_rule_name_popup)
        val ruleNameInput = findViewById<EditText>(R.id.edit_rule_name_input)
        val ruleNameSaveButton = findViewById<Button>(R.id.edit_rule_name_save_button)
        val ruleActiveSwitch = findViewById<MaterialSwitch>(R.id.edit_rule_activate_switch)
        val ruleAddApps = findViewById<ImageButton>(R.id.edit_rule_add_apps)
        val ruleApplyToApps = findViewById<TextView>(R.id.edit_rule_apply_to_apps)
        val ruleKeywordOperationChip = findViewById<Chip>(R.id.edit_rule_keyword_operation_chip)
        val ruleKeywordInclusionChip = findViewById<Chip>(R.id.edit_rule_keyword_inclusion_chip)
        val ruleKeywordChipContainer = findViewById<ChipGroup>(R.id.edit_rule_keyword_chip_container)
        val ruleKeywordInputText = findViewById<EditText>(R.id.edit_rule_keyword_input_text)
        val ruleAddKeywordButton = findViewById<TextView>(R.id.edit_rule_add_keyword_button)
        val ruleSaveButton = findViewById<FloatingActionButton>(R.id.edit_rule_save_button)
        val ruleFilterTypeSpinner = findViewById<Spinner>(R.id.edit_rule_filter_type_spinner)
        val ruleAddKeywordsContainer = findViewById<ConstraintLayout>(R.id.edit_rule_add_keywords_container)
        ruleSelectedAppsRecycler = findViewById(R.id.edit_rule_selected_apps_recycler)


        val ruleIgnoreDNDSwitch = findViewById<MaterialSwitch>(R.id.edit_rule_ignore_dnd_switch)
        val ruleIgnoreRingerSwitch = findViewById<MaterialSwitch>(R.id.edit_rule_ignore_ringer_switch)

        ruleIgnoreDNDSwitch.isChecked = rule.getBoolean("ignoreDND")
        ruleIgnoreRingerSwitch.isChecked = rule.getBoolean("ignoreRinger")

        ruleIgnoreDNDSwitch.setOnCheckedChangeListener { _, isChecked ->
            rule.put("ignoreDND", isChecked)
        }

        ruleIgnoreRingerSwitch.setOnCheckedChangeListener { _, isChecked ->
            rule.put("ignoreRinger", isChecked)
        }


        val ruleAppDisplayContainer = findViewById<ConstraintLayout>(R.id.edit_rule_app_display_container)
        val ruleVibrationStepModeChip = findViewById<Chip>(R.id.edit_rule_vibration_step_mode_chip)
        val ruleVibrationStepContainer = findViewById<LinearLayout>(R.id.edit_rule_vibration_step_container)
        fun updateVibrationStepUI() {
            ruleVibrationStepContainer.removeAllViews()
            val pattern = JSONArray(rule.getString("vibrationPattern"))

            var maxWeight = 0
            for (i in 0 until pattern.length()) {
                maxWeight += pattern.getInt(i)
            }

            for (i in 0 until pattern.length()) {
                val vibrationStepComponent = VibrationStepComponent(this)
                vibrationStepComponent.setProps((pattern.getInt(i).toFloat()/1000).toString() + "s", i % 2 == 0, pattern.getInt(i).toFloat()/maxWeight)
                ruleVibrationStepContainer.addView(vibrationStepComponent)
            }
        }

        if (vibrationStepMode == 0){
            ruleVibrationStepModeChip.text = "Silence"
        } else {
            ruleVibrationStepModeChip.text = "Vibration"
        }

        val ruleToggleVibrationSwitch = findViewById<MaterialSwitch>(R.id.edit_rule_toggle_vibration_switch)
        if (rule.getBoolean("vibration")){
            findViewById<ConstraintLayout>(R.id.edit_rule_edit_vibration_container).visibility = View.VISIBLE
            updateVibrationStepUI()
        } else {
            findViewById<ConstraintLayout>(R.id.edit_rule_edit_vibration_container).visibility = View.GONE
        }
        ruleToggleVibrationSwitch.isChecked = rule.getBoolean("vibration")


        ruleToggleVibrationSwitch.setOnCheckedChangeListener { _, isChecked ->
            rule.put("vibration", isChecked)
            if (isChecked){
                findViewById<ConstraintLayout>(R.id.edit_rule_edit_vibration_container).visibility = View.VISIBLE
                updateVibrationStepUI()
            } else {
                findViewById<ConstraintLayout>(R.id.edit_rule_edit_vibration_container).visibility = View.GONE
            }
        }

        val ruleToggleSoundSwitch = findViewById<MaterialSwitch>(R.id.edit_rule_toggle_sound_switch)
        if (rule.getBoolean("sound")){
            findViewById<ConstraintLayout>(R.id.edit_rule_edit_sound_container).visibility = View.VISIBLE
        } else {
            findViewById<ConstraintLayout>(R.id.edit_rule_edit_sound_container).visibility = View.GONE
        }
        ruleToggleSoundSwitch.isChecked = rule.getBoolean("sound")

        ruleToggleSoundSwitch.setOnCheckedChangeListener { _, isChecked ->
            rule.put("sound", isChecked)
            if (isChecked){
                findViewById<ConstraintLayout>(R.id.edit_rule_edit_sound_container).visibility = View.VISIBLE
            } else {
                findViewById<ConstraintLayout>(R.id.edit_rule_edit_sound_container).visibility = View.GONE
            }
        }

        findViewById<TextView>(R.id.edit_rule_selected_sound).text = getSoundNameFromUriString(rule.getString("selectedSound"))


        findViewById<ConstraintLayout>(R.id.edit_rule_edit_sound_container).setOnClickListener {
            selectNotificationSound()
        }



        fun addVibrationStep(length: Int) {
            val pattern = JSONArray(rule.getString("vibrationPattern"))

            if (pattern.length() != 0 || vibrationStepMode != 0) {
                if (pattern.length() % 2 == vibrationStepMode) {
                    pattern.put(pattern.length() - 1, pattern.getInt(pattern.length() - 1) + length)
                } else {
                    pattern.put(length)
                }
            }

            rule.put("vibrationPattern", pattern.toString())
            updateVibrationStepUI()
        }

        updateVibrationStepUI()


        findViewById<Button>(R.id.edit_rule_vibration_undo_button).setOnClickListener {
            val pattern = JSONArray(rule.getString("vibrationPattern"))
            pattern.remove(pattern.length() - 1)
            
            rule.put("vibrationPattern", pattern.toString())

            updateVibrationStepUI()
        }

        findViewById<Button>(R.id.edit_rule_vibration_preview_button).setOnClickListener {
            if (rule.getString("vibrationPattern") != "[]"){
                val stringValues = rule.getString("vibrationPattern").trim('[', ']').split(",").map { it.trim() }
                val vibrationPattern = longArrayOf(0) + stringValues.map { it.toLong() }.toLongArray()

                val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                val vibrationEffect = VibrationEffect.createWaveform(vibrationPattern, -1)
                vibrator.vibrate(vibrationEffect)
            }
        }

        ruleVibrationStepModeChip.setOnClickListener {
            if (vibrationStepMode != 0){
                ruleVibrationStepModeChip.text = "Silence"
                vibrationStepMode = 0
            } else {
                ruleVibrationStepModeChip.text = "Vibration"
                vibrationStepMode = 1
            }
        }

        findViewById<Button>(R.id.edit_rule_vibration_XXS).setOnClickListener {
            addVibrationStep(10)
        }
        findViewById<Button>(R.id.edit_rule_vibration_XS).setOnClickListener {
            addVibrationStep(50)
        }
        findViewById<Button>(R.id.edit_rule_vibration_S).setOnClickListener {
            addVibrationStep(150)
        }
        findViewById<Button>(R.id.edit_rule_vibration_M).setOnClickListener {
            addVibrationStep(400)
        }
        findViewById<Button>(R.id.edit_rule_vibration_L).setOnClickListener {
            addVibrationStep(700)
        }
        findViewById<Button>(R.id.edit_rule_vibration_XL).setOnClickListener {
            addVibrationStep(1000)
        }
        findViewById<Button>(R.id.edit_rule_vibration_XXL).setOnClickListener {
            addVibrationStep(2000)
        }



        ruleFilterTypeSpinner.adapter = adapter

        ruleName.setOnClickListener {
            ruleNameInput.setText(rule.getString("name"))
            ruleNameEditPopup.visibility = View.VISIBLE
        }
        ruleNameEditPopup.setOnClickListener {
            ruleNameEditPopup.visibility = View.GONE
        }
        ruleNameSaveButton.setOnClickListener {
            rule.put("name", ruleNameInput.text.toString())
            ruleName.text = rule.getString("name")
            ruleNameEditPopup.visibility = View.GONE
        }


        ruleKeywordInclusionChip.setOnClickListener {
            rule.put("keywordInclusion", !rule.getBoolean("keywordInclusion"))
            if (rule.getBoolean("keywordInclusion")) {
                ruleKeywordInclusionChip.text = "INCL"
            } else {
                ruleKeywordInclusionChip.text = "EXCL"
            }
        }

        if (rule.getBoolean("keywordInclusion")) {
            ruleKeywordInclusionChip.text = "INCL"
        } else {
            ruleKeywordInclusionChip.text = "EXCL"
        }

        if (JSONArray(rule.getString("apps")).length() == 0) {
            ruleApplyToApps.text = "Apply rule to all apps"
            ruleAppDisplayContainer.visibility = View.GONE
        } else {
            ruleApplyToApps.text = "Apply rule to the following app(s)"
            ruleAppDisplayContainer.visibility = View.VISIBLE
            loadAppIcons(JSONArray(rule.getString("apps")))
        }

        ruleFilterTypeSpinner.setSelection(filterTypeSpinnerOptions.indexOf(rule.getString("filterType")))

        ruleAddKeywordButton.setOnClickListener {
            if (ruleKeywordInputText.text.toString() != "") {
                val chip = Chip(this).apply {
                    text = ruleKeywordInputText.text.toString()
                    isCloseIconVisible = true
                    setOnCloseIconClickListener {
                        ruleKeywordChipContainer.removeView(this)
                    }
                }
                ruleKeywordChipContainer.addView(chip)
                ruleKeywordInputText.text = null
            }

            var newKeywords = "["
            for (i in 0 until ruleKeywordChipContainer.childCount) {
                val chip = ruleKeywordChipContainer.getChildAt(i) as? Chip
                chip?.let {
                    newKeywords = newKeywords + "\"" + it.text.toString() + "\","
                }
            }
            newKeywords = newKeywords.substring(0, newKeywords.length - 1) + "]"
            if (newKeywords == "]") newKeywords = "[]"
            rule.put("keywords", newKeywords)
        }


        val keywords = JSONArray(rule.getString("keywords"))
        for (keyword in 0 until keywords.length()){
            val chip = Chip(this).apply {
                text = keywords.getString(keyword)
                isCloseIconVisible = true
                setOnCloseIconClickListener {
                    ruleKeywordChipContainer.removeView(this)
                    var newKeywords = "["
                    for (i in 0 until ruleKeywordChipContainer.childCount) {
                        val chip = ruleKeywordChipContainer.getChildAt(i) as? Chip
                        chip?.let {
                            newKeywords = newKeywords + "\"" + it.text.toString() + "\","
                        }
                    }

                    newKeywords = newKeywords.substring(0, newKeywords.length - 1) + "]"
                    if (newKeywords == "]") newKeywords = "[]"
                    rule.put("keywords", newKeywords)
                }
            }
            ruleKeywordChipContainer.addView(chip)
        }


        ruleAddApps.setOnClickListener {
            if (JSONArray(rule.getString("keywords")).length() == 0){
                rule.put("filterType", "All Notifications")
            }
            val newRules = loadRulesFromFile(this)
            newRules.put(ruleId, rule)
            saveRulesToFile(this, newRules)

            val intent = Intent(this, AppSelectActivity::class.java).apply {
                putExtra("RULE_ID", ruleId)
            }
            startActivity(intent)
        }

        ruleActiveSwitch.setOnCheckedChangeListener { _, isChecked ->
            rule.put("active", isChecked)
        }

        ruleSaveButton.setOnClickListener {
            if (JSONArray(rule.getString("keywords")).length() == 0)
                rule.put("filterType", "All Notifications")

            if (JSONArray(rule.getString("vibrationPattern")).length() == 0)
                rule.put("vibration", false)

            val newRules = loadRulesFromFile(this)
            newRules.put(ruleId, rule)
            saveRulesToFile(this, newRules)

            startActivity(Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            })
            finish()
        }

        ruleFilterTypeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedItem = parent.getItemAtPosition(position).toString()
                rule.put("filterType", selectedItem)
                if (rule.getString("filterType") == "All Notifications"){
                    ruleKeywordOperationChip.visibility = View.GONE
                    ruleAddKeywordsContainer.visibility = View.GONE
                    ruleKeywordChipContainer.visibility = View.GONE
                    ruleKeywordInclusionChip.visibility = View.GONE
                } else{
                    ruleKeywordOperationChip.visibility = View.VISIBLE
                    ruleAddKeywordsContainer.visibility = View.VISIBLE
                    ruleKeywordChipContainer.visibility = View.VISIBLE
                    ruleKeywordInclusionChip.visibility = View.VISIBLE
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {  }
        }

        ruleName.text = rule.getString("name")
        ruleActiveSwitch.isChecked = rule.getBoolean("active")


        ruleKeywordOperationChip.text = rule.getString("keywordOperation")
        if (rule.getString("filterType") == "All Notifications"){
            ruleKeywordOperationChip.visibility = View.GONE
        } else{
            ruleKeywordOperationChip.visibility = View.VISIBLE
        }

        ruleKeywordOperationChip.setOnClickListener {
            if (rule.getString("keywordOperation") == "AND")
                rule.put("keywordOperation", "OR")
            else rule.put("keywordOperation", "AND")
            ruleKeywordOperationChip.text = rule.getString("keywordOperation")
        }

    }

    private fun selectNotificationSound() {
        val ringtonePickerIntent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
            putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION)
            putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select a notification sound")
            putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false)
            putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
        }

        startActivityForResult(ringtonePickerIntent, 1001)
    }


    @Deprecated("This method has been deprecated in favor of using the Activity Result API\n      which brings increased type safety via an {@link ActivityResultContract} and the prebuilt\n      contracts for common intents available in\n      {@link androidx.activity.result.contract.ActivityResultContracts}, provides hooks for\n      testing, and allow receiving results in separate, testable classes independent from your\n      activity. Use\n      {@link #registerForActivityResult(ActivityResultContract, ActivityResultCallback)}\n      with the appropriate {@link ActivityResultContract} and handling the result in the\n      {@link ActivityResultCallback#onActivityResult(Object) callback}.")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1001 && resultCode == RESULT_OK) {

            val soundUriString: String? = data?.getParcelableExtra<Uri>(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)?.toString()

            soundUriString?.let {

                val soundName = getSoundNameFromUriString(it)

                rule.put("selectedSound", it)

                findViewById<TextView>(R.id.edit_rule_selected_sound).text = soundName
            }
        }
    }


    private fun getSoundNameFromUriString(uriString: String): String {
        var soundName = "Unknown Sound"

        if (uriString == RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION).toString()) {
            return "Default Notification Sound"
        }

        val uri = Uri.parse(uriString)

        val cursor = contentResolver.query(uri, null, null, null, null)

        cursor?.use {
            if (it.moveToFirst()) {
                val displayNameColumnIndex = it.getColumnIndex(android.provider.MediaStore.Audio.Media.DISPLAY_NAME)

                if (displayNameColumnIndex != -1) {
                    soundName = it.getString(displayNameColumnIndex)
                }
            }
        }

        return soundName
    }


    private fun loadRulesFromFile(context: Context): JSONArray {
        val file = File(context.filesDir, "rules.json")
        if (file.exists()) {
            val jsonString = file.readText()
            return JSONArray(jsonString)
        }
        return JSONArray()
    }

    private fun saveRulesToFile(context: Context, ruleArray: JSONArray) {
        val file = File(context.filesDir, "rules.json")
        file.writeText(ruleArray.toString())
    }

    private fun loadAppIcons(selectedApps: JSONArray) {
        val packageManager = packageManager
        val appList = mutableListOf<AppInfo>()

        CoroutineScope(Dispatchers.IO).launch {
            for (i in 0 until selectedApps.length()) {
                val packageName = selectedApps.getString(i)
                val appIcon = getAppIcon(packageManager, packageName)

                appIcon?.let {
                    appList.add(AppInfo(packageName, it))
                }
            }

            withContext(Dispatchers.Main) {
                adapter = AppAdapter(appList)
                ruleSelectedAppsRecycler.adapter = adapter
            }
        }
    }

    private fun getAppIcon(packageManager: PackageManager, packageName: String): Drawable? {
        return try {
            val applicationInfo = packageManager.getApplicationInfo(packageName, 0)
            packageManager.getApplicationIcon(applicationInfo)
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
    }

    class AppAdapter(private val appList: List<AppInfo>) : RecyclerView.Adapter<AppAdapter.AppViewHolder>() {

        inner class AppViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val appIcon: ImageView = itemView.findViewById(R.id.app_icon)
            private val appName: TextView = itemView.findViewById(R.id.app_name)

            fun bind(appInfo: AppInfo) {
                appIcon.setImageDrawable(appInfo.icon)
                appName.text = appInfo.packageName
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_app, parent, false)
            return AppViewHolder(view)
        }

        override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
            holder.bind(appList[position])
        }

        override fun getItemCount(): Int = appList.size
    }

    data class AppInfo(
        val packageName: String,
        val icon: Drawable?
    )


}
