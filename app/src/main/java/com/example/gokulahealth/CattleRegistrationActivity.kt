package com.example.gokulahealth

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.MenuItem
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import com.example.gokulahealth.data.Cattle
import com.example.gokulahealth.viewmodel.CattleViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class CattleRegistrationActivity : BaseActivity() { // Inherits from BaseActivity for Language support

    private lateinit var cattleViewModel: CattleViewModel
    private var currentPhotoPath: String? = null
    private lateinit var ivCattlePhoto: ImageView

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) launchCamera()
    }

    private val takePhotoLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            currentPhotoPath?.let { path ->
                val bitmap = BitmapFactory.decodeFile(path)
                ivCattlePhoto.setImageBitmap(bitmap)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cattle_registration)

        // --- 1. LOCALIZED NAVIGATION SETUP ---
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        // Using localized string for the Action Bar title
        supportActionBar?.title = getString(R.string.title_registration)

        val btnBack = findViewById<ImageButton>(R.id.btnBackToDashboard)
        btnBack.setOnClickListener {
            finish()
        }

        // --- 2. INITIALIZE VIEWMODEL & UI ---
        cattleViewModel = ViewModelProvider(this)[CattleViewModel::class.java]

        val etEarTag = findViewById<EditText>(R.id.etEarTag)
        val etName = findViewById<EditText>(R.id.etName)
        val etBreed = findViewById<EditText>(R.id.etBreed)
        val etDob = findViewById<EditText>(R.id.etDob)
        ivCattlePhoto = findViewById(R.id.ivCattlePhoto)

        // --- 3. CAMERA LOGIC ---
        findViewById<Button>(R.id.btnCapturePhoto).setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                launchCamera()
            } else {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }

        // --- 4. SAVE PROFILE LOGIC (Localized) ---
        findViewById<Button>(R.id.btnSaveCattle).setOnClickListener {
            val tag = etEarTag.text.toString()
            val name = etName.text.toString()

            if (tag.isNotBlank() && name.isNotBlank()) {
                val cattle = Cattle(
                    earTagId = tag,
                    name = name,
                    breed = etBreed.text.toString(),
                    dateOfBirth = etDob.text.toString(),
                    imagePath = currentPhotoPath
                )
                cattleViewModel.insertCattle(cattle)

                // Localized Success Message
                Toast.makeText(this, getString(R.string.toast_cattle_added), Toast.LENGTH_SHORT).show()
                finish()
            } else {
                // Localized Error Message
                Toast.makeText(this, getString(R.string.toast_fill_details), Toast.LENGTH_SHORT).show()
            }
        }
    }

    // --- NAVIGATION OVERRIDES ---
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    private fun launchCamera() {
        val photoFile: File? = try { createImageFile() } catch (ex: Exception) { null }
        photoFile?.also {
            val photoURI: Uri = FileProvider.getUriForFile(this, "${packageName}.fileprovider", it)
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply { putExtra(MediaStore.EXTRA_OUTPUT, photoURI) }
            takePhotoLauncher.launch(intent)
        }
    }

    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("COW_${timeStamp}_", ".jpg", storageDir).apply { currentPhotoPath = absolutePath }
    }
}