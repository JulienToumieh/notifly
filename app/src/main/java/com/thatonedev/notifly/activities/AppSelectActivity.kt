package com.thatonedev.notifly.activities

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
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

class AppSelectActivity : AppCompatActivity() {

    private lateinit var adapter: AppSelectCardComponent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_app_select)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        Handler(Looper.getMainLooper()).postDelayed({
            showAppSelector()
        }, 500)
    }

    private fun showAppSelector(){
        val recyclerView = findViewById<RecyclerView>(R.id.app_card_recycler)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val appsData = getInstalledApps(this)
        adapter = AppSelectCardComponent(this, appsData)
        recyclerView.adapter = adapter

        val searchBar: EditText = findViewById(R.id.app_search_bar)
        searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adapter.filter(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun getInstalledApps(activity: Activity): JSONArray {
        val packageManager = activity.packageManager
        val appsList = mutableListOf<JSONObject>()

        val packages = packageManager.getInstalledApplications(0)
        for (app in packages) {
            val appInfo = packageManager.getApplicationInfo(app.packageName, 0)
            val appName = packageManager.getApplicationLabel(appInfo).toString()
            val iconDrawable = appInfo.loadIcon(packageManager)

            val jsonObject = JSONObject().apply {
                put("name", appName)
                put("packageName", app.packageName)
                put("icon", iconDrawable)
            }
            appsList.add(jsonObject)
        }
        appsList.sortBy { it.getString("name") }

        return JSONArray(appsList)
    }
}