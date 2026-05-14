package com.example.gokulahealth

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.gokulahealth.data.Vaccination
import com.example.gokulahealth.viewmodel.CattleViewModel
import kotlinx.coroutines.launch
import java.util.*

class VaccinationActivity : AppCompatActivity() {

    private lateinit var cattleViewModel: CattleViewModel
    private var selectedCattleId: Int = -1
    private var selectedCattleName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vaccination)

        // --- 1. PERMISSION SOLICITOR (Android 13+) ---
        // This forces the "Allow Notifications" popup on your phone
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }

        // --- 2. NAVIGATION SETUP ---
        val btnBackArrow = findViewById<ImageButton>(R.id.btnBackArrow)
        btnBackArrow.setOnClickListener {
            finish()
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Schedule Vaccination"

        // --- 3. HARD RESET CHANNEL SETUP ---
        createNotificationChannel()

        cattleViewModel = ViewModelProvider(this)[CattleViewModel::class.java]

        val spinnerCattle = findViewById<Spinner>(R.id.spinnerCattle)
        val etVaccineName = findViewById<EditText>(R.id.etVaccineName)
        val datePicker = findViewById<DatePicker>(R.id.datePicker)
        val timePicker = findViewById<TimePicker>(R.id.timePicker)
        val btnSetReminder = findViewById<Button>(R.id.btnSetReminder)

        lifecycleScope.launch {
            cattleViewModel.getAllCattle().collect { cattleList ->
                if (cattleList.isNotEmpty()) {
                    val adapter = ArrayAdapter(this@VaccinationActivity, android.R.layout.simple_spinner_dropdown_item, cattleList.map { it.name })
                    spinnerCattle.adapter = adapter
                    spinnerCattle.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(p0: AdapterView<*>?, p1: View?, pos: Int, p3: Long) {
                            selectedCattleId = cattleList[pos].id
                            selectedCattleName = cattleList[pos].name
                        }
                        override fun onNothingSelected(p0: AdapterView<*>?) {}
                    }
                }
            }
        }

        btnSetReminder.setOnClickListener {
            val vaccineName = etVaccineName.text.toString()
            if (vaccineName.isBlank() || selectedCattleId == -1) {
                Toast.makeText(this, "Please fill in all details", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val calendar = Calendar.getInstance().apply {
                set(datePicker.year, datePicker.month, datePicker.dayOfMonth, timePicker.hour, timePicker.minute, 0)
            }
            val dueDateMillis = calendar.timeInMillis

            if (dueDateMillis > System.currentTimeMillis()) {
                cattleViewModel.insertVaccination(Vaccination(cattleId = selectedCattleId, vaccineName = vaccineName, dueDate = dueDateMillis))
                scheduleAlarm(vaccineName, selectedCattleName, dueDateMillis)
                Toast.makeText(this, "Alert set for $selectedCattleName", Toast.LENGTH_LONG).show()
                finish()
            } else {
                Toast.makeText(this, "Please select a future date and time", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressedDispatcher.onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // NEW CHANNEL ID TO FORCE RESET SYSTEM CACHE
            val channelId = "VACCINE_ALERTS_NEW"
            val name = "Cattle Health Alerts"
            val descriptionText = "Urgent Veterinary Reminders"
            val importance = NotificationManager.IMPORTANCE_HIGH

            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
                enableVibration(true)
                setShowBadge(true)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            }

            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun scheduleAlarm(vaccineName: String, cattleName: String, timeInMillis: Long) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, VaccinationReceiver::class.java).apply {
            putExtra("VACCINE_NAME", vaccineName)
            putExtra("CATTLE_NAME", cattleName)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            this,
            (vaccineName + cattleName).hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
        } else {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
        }
    }
}