package com.thatonedev.notifly.activities

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.thatonedev.notifly.R

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

        }

        findViewById<Button>(R.id.restore_rules_btn).setOnClickListener {

        }

    }
}