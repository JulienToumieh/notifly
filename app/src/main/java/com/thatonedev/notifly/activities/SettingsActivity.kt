package com.thatonedev.notifly.activities

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.CreateDocument
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.thatonedev.notifly.MainActivity
import com.thatonedev.notifly.R
import com.thatonedev.notifly.components.RuleComponent
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