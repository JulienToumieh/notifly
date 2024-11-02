package com.thatonedev.notifly.activities

import android.annotation.SuppressLint
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
import com.thatonedev.notifly.MainActivity
import com.thatonedev.notifly.R

class PermissionActivity : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_permission)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        val btnNotificationPermission = findViewById<Button>(R.id.notification_permission_btn)
        val btnBatteryOptimization = findViewById<Button>(R.id.battery_optimization_btn)
        val btnClosePermissions = findViewById<Button>(R.id.close_permissions_btn)

        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        val isIgnoringBatteryOptimizations = powerManager.isIgnoringBatteryOptimizations(packageName)

        if (isIgnoringBatteryOptimizations) {
            btnBatteryOptimization.isEnabled = false
        }

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val isNotificationListenerEnabled = notificationManager.isNotificationPolicyAccessGranted

        if (isNotificationListenerEnabled) {
            btnNotificationPermission.isEnabled = false
        }

        btnNotificationPermission.setOnClickListener {
            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
        }

        btnBatteryOptimization.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val packageName = packageName
                val intent = Intent()
                intent.action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                intent.data = Uri.parse("package:$packageName")

                if (packageManager.resolveActivity(intent, 0) != null) {
                    startActivity(intent)
                } else {
                    // Handle the case where the intent can't be resolved
                    // This may happen on some devices that don't support this feature
                }
            }
        }

        btnClosePermissions.setOnClickListener {
            startActivity(Intent(Intent(this, MainActivity::class.java)))
        }
    }
    override fun onResume() {
        super.onResume()

        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        val isIgnoringBatteryOptimizations = powerManager.isIgnoringBatteryOptimizations(packageName)


        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val isNotificationListenerEnabled = notificationManager.isNotificationPolicyAccessGranted

        // Check Battery Optimization
        if (isIgnoringBatteryOptimizations) {
            findViewById<Button>(R.id.battery_optimization_btn).isEnabled = false
        }

        // Check Notification Listener
        if (isNotificationListenerEnabled) {
            findViewById<Button>(R.id.notification_permission_btn).isEnabled = false
        }
    }
}