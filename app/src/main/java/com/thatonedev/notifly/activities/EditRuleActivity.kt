package com.thatonedev.notifly.activities

import android.app.Activity
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.thatonedev.notifly.R
import com.thatonedev.notifly.components.AppSelectCardComponent
import org.json.JSONArray
import org.json.JSONObject

class EditRuleActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_edit_rule)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val recyclerView = findViewById<RecyclerView>(R.id.app_card_recycler)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Get installed apps and set adapter
        val appsData = getInstalledApps(this)
        val adapter = AppSelectCardComponent(this, appsData)
        recyclerView.adapter = adapter

    }

    fun getInstalledApps(activity: Activity): JSONArray {
        val packageManager = activity.packageManager
        val appsList = JSONArray()

        val packages = packageManager.getInstalledApplications(0)
        for (app in packages) {
            val appInfo = packageManager.getApplicationInfo(app.packageName, 0)
            val appName = packageManager.getApplicationLabel(appInfo).toString()
            val appIcon = appInfo.loadIcon(packageManager)
            val isActive = true // You can define the logic to check if the app is active

            val jsonObject = JSONObject()
            jsonObject.put("name", appName)
            jsonObject.put("icon", appIcon)
            jsonObject.put("active", isActive)
            appsList.put(jsonObject)
        }
        return appsList
    }

}