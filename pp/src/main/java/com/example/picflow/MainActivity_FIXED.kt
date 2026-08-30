package com.example.picflow

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private var selectedImageUri: Uri? = null

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            selectedImageUri = uri
            imageView.setImageURI(uri)
            Toast.makeText(this, "Photo selected ✓", Toast.LENGTH_SHORT).show()
        }
    }

    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && selectedImageUri != null) {
            imageView.setImageURI(selectedImageUri)
            Toast.makeText(this, "Photo taken ✓", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imageView = findViewById(R.id.imageViewPreview)
        val btnGallery = findViewById<Button>(R.id.btnGallery)
        val btnCamera = findViewById<Button>(R.id.btnCamera)
        val btnEdit = findViewById<Button>(R.id.btnEdit)

        btnGallery.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        btnCamera.setOnClickListener {
            try {
                val photoFile = File(cacheDir, "photo_${System.currentTimeMillis()}.jpg")
                selectedImageUri = FileProvider.getUriForFile(
                    this,
                    "$packageName.fileprovider",
                    photoFile
                )
                takePictureLauncher.launch(selectedImageUri)
            } catch (e: Exception) {
                Toast.makeText(this, "Camera error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

        btnEdit.setOnClickListener {
            if (selectedImageUri != null) {
                val intent = Intent(this, EditActivity::class.java)
                intent.putExtra("imageUri", selectedImageUri.toString())
                startActivity(intent)
            } else {
                Toast.makeText(this, "Pick a photo first", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
