package com.example.chargingdetector

import android.app.*
import android.content.*
import android.media.RingtoneManager
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import android.widget.*
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class MainActivity : AppCompatActivity() {

    private lateinit var statusText: TextView
    private val channelId = "charging_alert_channel"

    private val chargingReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {

            val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
            val plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)

            val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL

            val isPlugged = plugged == BatteryManager.BATTERY_PLUGGED_USB ||
                    plugged == BatteryManager.BATTERY_PLUGGED_AC ||
                    plugged == BatteryManager.BATTERY_PLUGGED_WIRELESS

            when {
                isPlugged && isCharging -> {
                    statusText.text = "✅ Charging Normally"
                }

                isPlugged && !isCharging -> {
                    statusText.text = "⚠ Cable Connected but NOT Charging!"
                    showNotification()
                }

                else -> {
                    statusText.text = "🔌 Charger Not Connected"
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createNotificationChannel()

        // Create UI Programmatically
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = android.view.Gravity.CENTER
            setPadding(40, 40, 40, 40)
        }

        val title = TextView(this).apply {
            text = "Charging Switch Detector"
            textSize = 22f
        }

        statusText = TextView(this).apply {
            text = "Waiting for charger..."
            textSize = 18f
        }

        layout.addView(title)
        layout.addView(statusText)

        setContentView(layout)
    }

    override fun onResume() {
        super.onResume()
        registerReceiver(
            chargingReceiver,
            IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        )
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(chargingReceiver)
    }

    private fun showNotification() {

        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("Charging Alert ⚠")
            .setContentText("Cable connected but NOT charging. Check switch!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setSound(soundUri)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(this)
            .notify(1, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Charging Alert Channel"
            val descriptionText = "Alerts when charger is connected but not charging"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }

            val notificationManager =
                getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
}
