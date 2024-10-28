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

        val recyclerView = findViewById<RecyclerView>(R.id.rule_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)
        val adapter = RuleComponent(this, ruleArray)
        recyclerView.adapter = adapter

        findViewById<FloatingActionButton>(R.id.add_rule_btn).setOnClickListener {

        }
    }


}