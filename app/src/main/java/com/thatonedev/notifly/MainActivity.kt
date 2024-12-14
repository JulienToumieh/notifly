package com.thatonedev.notifly

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Bundle
import android.os.PowerManager
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.thatonedev.notifly.activities.EditRuleActivity
import com.thatonedev.notifly.activities.PermissionActivity
import com.thatonedev.notifly.activities.SettingsActivity
import com.thatonedev.notifly.components.RuleComponent
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class MainActivity : AppCompatActivity(), RuleComponent.OnDataPass {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        val isIgnoringBatteryOptimizations = powerManager.isIgnoringBatteryOptimizations(packageName)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val isNotificationListenerEnabled = notificationManager.isNotificationPolicyAccessGranted

        if (!isIgnoringBatteryOptimizations || !isNotificationListenerEnabled) {
            startActivity(Intent(Intent(this, PermissionActivity::class.java)))
        }

        if (getSharedPreferences("app", MODE_PRIVATE).getBoolean("firstRun", true)) {
            getSharedPreferences("app", MODE_PRIVATE).edit().putBoolean("firstRun", false).apply()
            val tasksArray = JSONArray()
            saveRulesToFile(this, tasksArray)
        }

        refreshRules(loadRulesFromFile(this))

        findViewById<FloatingActionButton>(R.id.add_rule_btn).setOnClickListener {
            createRule(this, "New Rule",true ,"[]", false, "[]", false, getDefaultNotificationSound(), "All Notifications", "[]", "OR", false, false)

            val ruleId = loadRulesFromFile(this).length() - 1
            editRule(ruleId)
        }

        findViewById<ImageButton>(R.id.open_settings_btn).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
            finish()
        }

        updateRuleFeatures()
    }

    private fun refreshRules(ruleArray: JSONArray){
        val recyclerView = findViewById<RecyclerView>(R.id.rule_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)
        val adapter = RuleComponent(this, ruleArray)
        recyclerView.adapter = adapter
    }

    private fun createRule(context: Context, name: String, active: Boolean, apps: String, vibration: Boolean, vibrationPattern: String, sound: Boolean, selectedSound: String,containsType: String, containsData: String, containsOperation: String, ignoreDND: Boolean, ignoreRinger: Boolean) {
        val rulesArray = loadRulesFromFile(context)
        val newRule = JSONObject().apply {
            put("name", name)
            put("active", active)
            put("apps", apps)
            put("vibration", vibration)
            put("vibrationPattern", vibrationPattern) // [1000, 300, 200]
            put("sound", sound)
            put("selectedSound", selectedSound)
            put("filterType", containsType) // ALL Notifications, Content, Title, Text
            put("keywordOperation", containsOperation) // AND, OR
            put("keywordInclusion", true)
            put("keywords", containsData)
            put("ignoreDND", ignoreDND)
            put("ignoreRinger", ignoreRinger)
        }
        rulesArray.put(newRule)

        saveRulesToFile(context, rulesArray)
        refreshRules(rulesArray)
    }

    private fun updateRuleFeatures(){
        var rules = loadRulesFromFile(this)

        for (i in 0 until rules.length()){
            var rule = rules.getJSONObject(i)
            if (!rule.has("name")) rule.put("name", "New Rule")
            if (!rule.has("active")) rule.put("active", true)
            if (!rule.has("apps")) rule.put("apps", "[]")
            if (!rule.has("vibration")) rule.put("vibration", false)
            if (!rule.has("vibrationPattern")) rule.put("vibrationPattern", "[]")
            if (!rule.has("sound")) rule.put("sound", false)
            if (!rule.has("selectedSound")) rule.put("selectedSound", getDefaultNotificationSound())
            if (!rule.has("filterType")) rule.put("filterType", "All Notifications")
            if (!rule.has("keywords")) rule.put("keywords", "[]")
            if (!rule.has("keywordOperation")) rule.put("keywordOperation", "OR")
            if (!rule.has("ignoreDND")) rule.put("ignoreDND", false)
            if (!rule.has("ignoreRinger")) rule.put("ignoreRinger", false)
        }

        saveRulesToFile(this, rules)
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

    override fun editRule(ruleId: Int) {
        val intent = Intent(this, EditRuleActivity::class.java).apply {
            putExtra("RULE_ID", ruleId)
        }
        startActivity(intent)
    }

    override fun activateRule(ruleId: Int, active: Boolean) {
        val newRules = loadRulesFromFile(this)
        val rule = loadRulesFromFile(this).getJSONObject(ruleId)
        rule.put("active", active)
        newRules.put(ruleId, rule)
        saveRulesToFile(this, newRules)
    }

    override fun deleteRule(ruleId: Int) {
        val newRules = loadRulesFromFile(this)
        newRules.remove(ruleId)
        saveRulesToFile(this, newRules)
        refreshRules(loadRulesFromFile(this))
    }


    private fun getDefaultNotificationSound(): String {
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        return defaultSoundUri.toString()
    }

}