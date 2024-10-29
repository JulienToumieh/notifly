package com.thatonedev.notifly.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.thatonedev.notifly.MainActivity
import com.thatonedev.notifly.R
import com.thatonedev.notifly.components.AppSelectCardComponent
import kotlinx.coroutines.MainScope
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class EditRuleActivity : AppCompatActivity(), AppSelectCardComponent.OnDataPass {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_edit_rule)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val filterTypeSpinnerOptions = listOf("All Notifications", "Content", "Title", "Text")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, filterTypeSpinnerOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        val ruleId = intent.getIntExtra("RULE_ID", 0)
        val rule = loadRulesFromFile(this).getJSONObject(ruleId)

        val ruleName = findViewById<TextView>(R.id.edit_rule_name)
        val ruleActiveSwitch = findViewById<Switch>(R.id.edit_rule_activate_switch)
        val ruleAddApps = findViewById<ImageButton>(R.id.edit_rule_add_apps)
        val ruleApplyToApps = findViewById<TextView>(R.id.edit_rule_apply_to_apps)
        val ruleKeywordOperationChip = findViewById<Chip>(R.id.edit_rule_keyword_operation_chip)
        val ruleKeywordChipContainer = findViewById<ChipGroup>(R.id.edit_rule_keyword_chip_container)
        val ruleKeywordInputText = findViewById<EditText>(R.id.edit_rule_keyword_input_text)
        val ruleAddKeywordButton = findViewById<TextView>(R.id.edit_rule_add_keyword_button)
        val ruleSaveButton = findViewById<FloatingActionButton>(R.id.edit_rule_save_button)
        val ruleFilterTypeSpinner = findViewById<Spinner>(R.id.edit_rule_filter_type_spinner)
        val ruleAddKeywordsContainer = findViewById<ConstraintLayout>(R.id.edit_rule_add_keywords_container)
        ruleFilterTypeSpinner.adapter = adapter

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
                    rule.put("keywords", newKeywords)
                }
            }
            ruleKeywordChipContainer.addView(chip)
        }


        ruleAddApps.setOnClickListener {
            val intent = Intent(this, AppSelectActivity::class.java).apply {
                putExtra("RULE_ID", ruleId)
            }
            startActivity(intent)
        }

        ruleActiveSwitch.setOnCheckedChangeListener { _, isChecked ->
            rule.put("active", isChecked)
        }

        ruleSaveButton.setOnClickListener {
            val newRules = loadRulesFromFile(this)
            newRules.put(ruleId, rule)
            saveRulesToFile(this, newRules)

            startActivity(Intent(this, MainActivity::class.java))
        }

        ruleFilterTypeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                val selectedItem = parent.getItemAtPosition(position).toString()
                rule.put("filterType", selectedItem)
                if (rule.getString("filterType") == "All Notifications"){
                    ruleKeywordOperationChip.visibility = View.GONE
                    ruleAddKeywordsContainer.visibility = View.GONE
                    ruleKeywordChipContainer.visibility = View.GONE
                } else{
                    ruleKeywordOperationChip.visibility = View.VISIBLE
                    ruleAddKeywordsContainer.visibility = View.VISIBLE
                    ruleKeywordChipContainer.visibility = View.VISIBLE
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

    override fun toggleAppCard(position: Int) {
        // Handle checkbox toggle logic
    }
}
