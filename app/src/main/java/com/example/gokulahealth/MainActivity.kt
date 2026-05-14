package com.example.gokulahealth

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView

class MainActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // --- 1. LANGUAGE TOGGLE LOGIC (Fixed Cast to TextView) ---

        val btnEnglish = findViewById<TextView>(R.id.btnEnglish)
        btnEnglish.setOnClickListener {
            LocaleHelper.setLocale(this, "en")
            restartActivity() // Using fresh intent for a cleaner switch
        }

        val btnKannada = findViewById<TextView>(R.id.btnKannada)
        btnKannada.setOnClickListener {
            LocaleHelper.setLocale(this, "kn")
            restartActivity()
        }

        // --- 2. NAVIGATION LOGIC ---

        // Cattle Profile (Registration Screen)
        findViewById<Button>(R.id.btnCattleProfile).setOnClickListener {
            startActivity(Intent(this, CattleRegistrationActivity::class.java))
        }

        // Milk Diary (Logs, Yield, and Profit)
        findViewById<Button>(R.id.btnMilkDiary).setOnClickListener {
            startActivity(Intent(this, MilkDiaryActivity::class.java))
        }

        // Vaccination (Offline Reminders)
        findViewById<Button>(R.id.btnVaccination).setOnClickListener {
            startActivity(Intent(this, VaccinationActivity::class.java))
        }

        // Cattle Gallery (View Records)
        findViewById<Button>(R.id.btnCattleGallery).setOnClickListener {
            startActivity(Intent(this, CattleListActivity::class.java))
        }
    }

    /**
     * Freshly restarts the Activity to ensure all localized strings
     * are reloaded from the 'res' folders correctly.
     */
    private fun restartActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }
}