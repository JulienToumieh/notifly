package com.thatonedev.notifly.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Switch
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.materialswitch.MaterialSwitch
import com.thatonedev.notifly.R
import com.thatonedev.notifly.components.AppSelectCardComponent
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class AppSelectActivity : AppCompatActivity(), AppSelectCardComponent.OnDataPass {

    private lateinit var adapter: AppSelectCardComponent
    private lateinit var selectedApps: JSONArray
    private lateinit var appsData: JSONArray

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_app_select)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val ruleArray = loadRulesFromFile(this)
        val ruleId = intent.getIntExtra("RULE_ID", 0)
        selectedApps = JSONArray(ruleArray.getJSONObject(ruleId).getString("apps"))

        Handler(Looper.getMainLooper()).postDelayed({
            showAppSelector()
        }, 100)

        findViewById<Button>(R.id.app_select_confirm_button).setOnClickListener {
            ruleArray.put(ruleId, ruleArray.getJSONObject(ruleId).put("apps", selectedApps.toString()))
            saveRulesToFile(this, ruleArray)

            val intent = Intent(this, EditRuleActivity::class.java).apply {
                putExtra("RULE_ID", ruleId)
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            startActivity(intent)
            finish()
        }

        findViewById<CheckBox>(R.id.app_select_all_checkbox).setOnCheckedChangeListener { _, isChecked ->
            adapter.selectAllApps(isChecked)
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

    private fun showAppSelector(){
        val recyclerView = findViewById<RecyclerView>(R.id.app_card_recycler)
        recyclerView.layoutManager = LinearLayoutManager(this)

        appsData = getInstalledApps(this)
        adapter = AppSelectCardComponent(this, appsData)
        recyclerView.adapter = adapter

        val searchBar: EditText = findViewById(R.id.app_search_bar)
        searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adapter.filter(s.toString(), findViewById<MaterialSwitch>(R.id.app_select_active_switch).isChecked)
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        findViewById<MaterialSwitch>(R.id.app_select_active_switch).setOnCheckedChangeListener { _, isChecked ->
            adapter.filter(searchBar.text.toString(), isChecked)
        }
    }

    private fun getInstalledApps(activity: Activity): JSONArray {
        val packageManager = activity.packageManager
        val appsList = mutableListOf<JSONObject>()

        val selectedPackageNames = mutableSetOf<String>()
        for (i in 0 until selectedApps.length()) {
            selectedPackageNames.add(selectedApps.getString(i))
        }

        val packages = packageManager.getInstalledApplications(0)
        for (app in packages) {
            val appInfo = packageManager.getApplicationInfo(app.packageName, 0)
            val appName = packageManager.getApplicationLabel(appInfo).toString()
            val iconDrawable = appInfo.loadIcon(packageManager)

            val appSelected = selectedPackageNames.contains(app.packageName)

            val jsonObject = JSONObject().apply {
                put("name", appName)
                put("packageName", app.packageName)
                put("icon", iconDrawable)
                put("selected", appSelected)
            }
            appsList.add(jsonObject)
        }
        appsList.sortBy { it.getString("name") }

        return JSONArray(appsList)
    }

    override fun toggleAppCard(packageName: String, selected: Boolean) {
        if (selected) {
            var exists = false
            for (i in selectedApps.length() - 1 downTo 0) {
                if (selectedApps.getString(i) == packageName) {
                    exists = true
                    break
                }
            }
            if (!exists)
                selectedApps.put(packageName)

        } else {
            for (i in selectedApps.length() - 1 downTo 0) {
                if (selectedApps.getString(i) == packageName) {
                    selectedApps.remove(i)
                    break
                }
            }
        }
    }


}