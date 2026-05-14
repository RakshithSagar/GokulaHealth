package com.example.gokulahealth

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

class VaccinationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val vaccineName = intent.getStringExtra("VACCINE_NAME") ?: "Vaccination"
        val cattleName = intent.getStringExtra("CATTLE_NAME") ?: "Cattle"

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "VACCINE_CHANNEL"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Vaccination Alerts",
                NotificationManager.IMPORTANCE_HIGH // HIGH importance is required for sound
            ).apply {
                description = "Urgent reminders for cattle health"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm) // Using an alarm icon
            .setContentTitle("Vaccination Alert: $cattleName")
            .setContentText("Time for the $vaccineName shot.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM) // Tells Android this is an alarm
            .setDefaults(NotificationCompat.DEFAULT_ALL) // FORCES sound and vibration
            .setAutoCancel(true)

        val notificationId = (vaccineName + cattleName).hashCode()
        notificationManager.notify(notificationId, builder.build())
    }
}