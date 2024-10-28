package com.thatonedev.notifly

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.thatonedev.notifly.activities.PermissionActivity
import com.thatonedev.notifly.components.RuleComponent
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class MainActivity : AppCompatActivity() {
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
            createRule(this, "Mhmm", "[\"com.whatsapp\"]", "", "", "All Notifications", "[]", "")
        }



        refreshRules(loadRulesFromFile(this))

        findViewById<FloatingActionButton>(R.id.add_rule_btn).setOnClickListener {

        }
    }

    private fun refreshRules(ruleArray: JSONArray){
        val recyclerView = findViewById<RecyclerView>(R.id.rule_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)
        val adapter = RuleComponent(this, ruleArray)
        recyclerView.adapter = adapter
    }

    private fun createRule(context: Context, name: String, apps: String, vibration: String, sound: String, containsType: String, containsData: String, containsOperation: String) {
        val rulesArray = loadRulesFromFile(context) ?: JSONArray()
        val newRule = JSONObject().apply {
            put("name", name)
            put("active", true)
            put("apps", apps)
            put("vibration", vibration)
            put("sound", sound)
            put("containsType", containsType) // Notification, Title, Text
            put("containsData", containsData)
            put("containsOperation", containsOperation) // AND, OR
        }
        rulesArray.put(newRule)

        saveRulesToFile(context, rulesArray)
        refreshRules(rulesArray)
    }

    private fun saveRulesToFile(context: Context, tasksArray: JSONArray) {
        val file = File(context.filesDir, "rules.json")
        file.writeText(tasksArray.toString())
    }

    private fun loadRulesFromFile(context: Context): JSONArray {
        val file = File(context.filesDir, "rules.json")
        if (file.exists()) {
            val jsonString = file.readText()
            return JSONArray(jsonString)
        }
        return JSONArray()
    }

}