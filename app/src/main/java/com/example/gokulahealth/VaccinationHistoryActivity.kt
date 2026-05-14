package com.example.gokulahealth

import android.os.Bundle
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gokulahealth.viewmodel.CattleViewModel
import kotlinx.coroutines.launch

// Changed to BaseActivity for localization support
class VaccinationHistoryActivity : BaseActivity() {
    private lateinit var viewModel: CattleViewModel
    private lateinit var adapter: VaccinationAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vaccination_history)

        // --- 1. ENABLE BACK NAVIGATION ---
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val cattleId = intent.getIntExtra("CATTLE_ID", -1)
        val cattleName = intent.getStringExtra("CATTLE_NAME") ?: getString(R.string.btn_add_cattle)

        // --- 2. LOCALIZED DYNAMIC HEADER ---
        // This pulls the format from strings.xml: "%1$s's Health Records"
        // or "%1$s ಅವರ ಆರೋಗ್ಯ ದಾಖಲೆಗಳು"
        val localizedTitle = getString(R.string.health_records_title, cattleName)

        findViewById<TextView>(R.id.tvHistoryHeader).text = localizedTitle
        supportActionBar?.title = localizedTitle

        // --- 3. SETUP RECYCLERVIEW ---
        val rv = findViewById<RecyclerView>(R.id.rvVaccinationHistory)
        adapter = VaccinationAdapter(emptyList())
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = adapter

        viewModel = ViewModelProvider(this)[CattleViewModel::class.java]

        // --- 4. OBSERVE OFFLINE DATA ---
        lifecycleScope.launch {
            viewModel.getVaccinations(cattleId).collect { vaccinations ->
                adapter.updateList(vaccinations)
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}