package com.thatonedev.notifly

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.PowerManager
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.thatonedev.notifly.activities.EditRuleActivity
import com.thatonedev.notifly.activities.PermissionActivity
import com.thatonedev.notifly.components.RuleComponent
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class MainActivity : AppCompatActivity(), RuleComponent.OnDataPass{
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
            createRule(this, "New Rule",true ,"[]", "", "", "All Notifications", "[]", "OR")

            val ruleId = loadRulesFromFile(this).length() - 1
            editRule(ruleId)
        }
    }

    private fun refreshRules(ruleArray: JSONArray){
        val recyclerView = findViewById<RecyclerView>(R.id.rule_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)
        val adapter = RuleComponent(this, ruleArray)
        recyclerView.adapter = adapter
    }

    private fun createRule(context: Context, name: String, active: Boolean, apps: String, vibration: String, sound: String, containsType: String, containsData: String, containsOperation: String) {
        val rulesArray = loadRulesFromFile(context)
        val newRule = JSONObject().apply {
            put("name", name)
            put("active", active)
            put("apps", apps)
            put("vibration", vibration)
            put("sound", sound)
            put("filterType", containsType) // ALL Notifications, Content, Title, Text
            put("keywordOperation", containsOperation) // AND, OR
            put("keywordInclusion", true)
            put("keywords", containsData)

        }
        rulesArray.put(newRule)

        saveRulesToFile(context, rulesArray)
        refreshRules(rulesArray)
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
        var newRules = loadRulesFromFile(this)
        newRules.remove(ruleId)
        saveRulesToFile(this, newRules)
        refreshRules(loadRulesFromFile(this))
    }

}