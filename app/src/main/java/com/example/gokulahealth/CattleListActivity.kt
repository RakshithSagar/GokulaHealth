package com.example.gokulahealth

import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gokulahealth.viewmodel.CattleViewModel
import kotlinx.coroutines.launch

class CattleListActivity : AppCompatActivity() {

    private lateinit var cattleViewModel: CattleViewModel
    private lateinit var adapter: CattleAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cattle_list)

        // 1. Connect ViewModel FIRST so we can use it in the adapter
        cattleViewModel = ViewModelProvider(this)[CattleViewModel::class.java]

        // 2. Back button click handler
        val btnBackFromList = findViewById<ImageButton>(R.id.btnBackFromList)
        btnBackFromList.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // 3. Setup the RecyclerView
        val recyclerView = findViewById<RecyclerView>(R.id.rvCattle)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // --- UPDATED: Passing the delete logic into the Adapter ---
        adapter = CattleAdapter(emptyList()) { cattleToDelete ->
            // When the user clicks "Delete" in the popup, this code runs:
            cattleViewModel.deleteCattle(cattleToDelete)
        }

        recyclerView.adapter = adapter

        // 4. Fetch and display the cattle list
        lifecycleScope.launch {
            cattleViewModel.getAllCattle().collect { list ->
                adapter.updateList(list)
            }
        }
    }
}