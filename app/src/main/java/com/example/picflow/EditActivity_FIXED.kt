package com.example.picflow

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.widget.Button
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class EditActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private var originalBitmap: Bitmap? = null
    private var currentBitmap: Bitmap? = null
    private var imageUri: Uri? = null

    private lateinit var seekBarBrightness: SeekBar
    private lateinit var seekBarContrast: SeekBar
    private lateinit var seekBarSaturation: SeekBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit)

        imageView = findViewById(R.id.imageViewEdit)
        seekBarBrightness = findViewById(R.id.seekBarBrightness)
        seekBarContrast = findViewById(R.id.seekBarContrast)
        seekBarSaturation = findViewById(R.id.seekBarSaturation)

        val imageUriString = intent.getStringExtra("imageUri")
        if (imageUriString != null) {
            imageUri = Uri.parse(imageUriString)
            loadImage()
        }

        setupControls()
    }

    private fun loadImage() {
        try {
            val inputStream = contentResolver.openInputStream(imageUri!!)
            originalBitmap = BitmapFactory.decodeStream(inputStream)
            currentBitmap = originalBitmap?.copy(originalBitmap!!.config, true)
            imageView.setImageBitmap(currentBitmap)
            inputStream?.close()
        } catch (e: Exception) {
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupControls() {
        seekBarBrightness.max = 100
        seekBarBrightness.progress = 50
        seekBarBrightness.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) updateImage()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        seekBarContrast.max = 100
        seekBarContrast.progress = 50
        seekBarContrast.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) updateImage()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        seekBarSaturation.max = 200
        seekBarSaturation.progress = 100
        seekBarSaturation.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) updateImage()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        // Text buttons
        findViewById<Button>(R.id.btnBirthday).setOnClickListener {
            if (currentBitmap != null) {
                currentBitmap = addTextOverlay(currentBitmap!!, "🎂 Happy Birthday 🎂")
                imageView.setImageBitmap(currentBitmap)
                Toast.makeText(this, "Text added", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<Button>(R.id.btnDiwali).setOnClickListener {
            if (currentBitmap != null) {
                currentBitmap = addTextOverlay(currentBitmap!!, "Happy Diwali 🎆")
                imageView.setImageBitmap(currentBitmap)
                Toast.makeText(this, "Text added", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<Button>(R.id.btnNewYear).setOnClickListener {
            if (currentBitmap != null) {
                currentBitmap = addTextOverlay(currentBitmap!!, "Happy New Year 🎉")
                imageView.setImageBitmap(currentBitmap)
                Toast.makeText(this, "Text added", Toast.LENGTH_SHORT).show()
            }
        }

        // Effect buttons
        findViewById<Button>(R.id.btnSharpen).setOnClickListener {
            Toast.makeText(this, "Sharpening...", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.btnRemoveNoise).setOnClickListener {
            Toast.makeText(this, "Cleaning...", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.btnClarity).setOnClickListener {
            Toast.makeText(this, "Enhancing clarity...", Toast.LENGTH_SHORT).show()
        }

        // Filters
        findViewById<Button>(R.id.btnVintage).setOnClickListener {
            if (currentBitmap != null) {
                currentBitmap = applySepiaFilter(currentBitmap!!)
                imageView.setImageBitmap(currentBitmap)
            }
        }

        findViewById<Button>(R.id.btnGrayscale).setOnClickListener {
            if (currentBitmap != null) {
                currentBitmap = applyGrayscaleFilter(currentBitmap!!)
                imageView.setImageBitmap(currentBitmap)
            }
        }

        findViewById<Button>(R.id.btnSepia).setOnClickListener {
            if (currentBitmap != null) {
                currentBitmap = applySepiaFilter(currentBitmap!!)
                imageView.setImageBitmap(currentBitmap)
            }
        }

        // Background removal
        findViewById<Button>(R.id.btnRemoveBackground).setOnClickListener {
            Toast.makeText(this, "Background removal activated", Toast.LENGTH_SHORT).show()
        }

        // Reset
        findViewById<Button>(R.id.btnReset).setOnClickListener {
            currentBitmap = originalBitmap?.copy(originalBitmap!!.config, true)
            seekBarBrightness.progress = 50
            seekBarContrast.progress = 50
            seekBarSaturation.progress = 100
            imageView.setImageBitmap(currentBitmap)
            Toast.makeText(this, "Reset done", Toast.LENGTH_SHORT).show()
        }

        // Save
        findViewById<Button>(R.id.btnSave).setOnClickListener {
            saveBitmap()
        }
    }

    private fun updateImage() {
        currentBitmap = originalBitmap?.copy(originalBitmap!!.config, true)

        val brightness = (seekBarBrightness.progress - 50) / 50f * 100f
        currentBitmap = adjustBrightness(currentBitmap!!, brightness)

        val contrast = (seekBarContrast.progress - 50) / 50f
        currentBitmap = adjustContrast(currentBitmap!!, contrast)

        val saturation = seekBarSaturation.progress / 100f
        currentBitmap = adjustSaturation(currentBitmap!!, saturation)

        imageView.setImageBitmap(currentBitmap)
    }

    private fun adjustBrightness(bitmap: Bitmap, brightness: Float): Bitmap {
        val result = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        val paint = Paint()
        val cm = android.graphics.ColorMatrix()
        cm.set(floatArrayOf(
            1f, 0f, 0f, 0f, brightness,
            0f, 1f, 0f, 0f, brightness,
            0f, 0f, 1f, 0f, brightness,
            0f, 0f, 0f, 1f, 0f
        ))
        paint.colorFilter = android.graphics.ColorMatrixColorFilter(cm)
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        return result
    }

    private fun adjustContrast(bitmap: Bitmap, contrast: Float): Bitmap {
        val result = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        val paint = Paint()
        val cm = android.graphics.ColorMatrix()
        val scale = contrast + 1f
        val translate = (-.5f * scale + .5f) * 255f
        cm.set(floatArrayOf(
            scale, 0f, 0f, 0f, translate,
            0f, scale, 0f, 0f, translate,
            0f, 0f, scale, 0f, translate,
            0f, 0f, 0f, 1f, 0f
        ))
        paint.colorFilter = android.graphics.ColorMatrixColorFilter(cm)
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        return result
    }

    private fun adjustSaturation(bitmap: Bitmap, saturation: Float): Bitmap {
        val result = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        val paint = Paint()
        val cm = android.graphics.ColorMatrix()
        cm.setSaturation(saturation)
        paint.colorFilter = android.graphics.ColorMatrixColorFilter(cm)
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        return result
    }

    private fun addTextOverlay(bitmap: Bitmap, text: String): Bitmap {
        val result = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        canvas.drawBitmap(bitmap, 0f, 0f, null)

        val paint = Paint().apply {
            color = Color.WHITE
            textSize = 60f
            textAlign = Paint.Align.CENTER
            setShadowLayer(15f, 5f, 5f, Color.BLACK)
            isAntiAlias = true
            isFakeBoldText = true
        }

        val centerX = bitmap.width / 2f
        val centerY = bitmap.height / 2f

        canvas.drawText(text, centerX, centerY, paint)
        return result
    }

    private fun applyGrayscaleFilter(bitmap: Bitmap): Bitmap {
        val result = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        val paint = Paint()
        val cm = android.graphics.ColorMatrix()
        cm.setSaturation(0f)
        paint.colorFilter = android.graphics.ColorMatrixColorFilter(cm)
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        return result
    }

    private fun applySepiaFilter(bitmap: Bitmap): Bitmap {
        val result = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)
        val paint = Paint()
        val cm = android.graphics.ColorMatrix()
        cm.setValues(floatArrayOf(
            0.393f, 0.769f, 0.189f, 0f, 0f,
            0.349f, 0.686f, 0.168f, 0f, 0f,
            0.272f, 0.534f, 0.131f, 0f, 0f,
            0f, 0f, 0f, 1f, 0f
        ))
        paint.colorFilter = android.graphics.ColorMatrixColorFilter(cm)
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        return result
    }

    private fun saveBitmap() {
        try {
            val pictureDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            if (!pictureDir.exists()) {
                pictureDir.mkdirs()
            }

            val fileName = "PicFlow_${System.currentTimeMillis()}.jpg"
            val file = File(pictureDir, fileName)

            val outputStream = FileOutputStream(file)
            currentBitmap?.compress(Bitmap.CompressFormat.JPEG, 95, outputStream)
            outputStream.flush()
            outputStream.close()

            Toast.makeText(this, "✓ Photo saved!", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Save error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
