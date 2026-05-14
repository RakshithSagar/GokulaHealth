package com.example.gokulahealth

import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import java.io.File

class FullScreenImageActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_full_screen_image)

        supportActionBar?.hide()

        val imageView = findViewById<ImageView>(R.id.ivFullScreen)
        val btnClose = findViewById<ImageButton>(R.id.btnCloseFull)

        // Get the path passed from the Adapter
        val imagePath = intent.getStringExtra("IMAGE_PATH")

        if (imagePath != null) {
            val imgFile = File(imagePath)
            if (imgFile.exists()) {
                val bitmap = BitmapFactory.decodeFile(imgFile.absolutePath)
                imageView.setImageBitmap(bitmap)
            }
        }

        btnClose.setOnClickListener { finish() }
    }
}