package com.example.imageeditapp

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.widget.Button
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.segmentation.Segmentation
import com.google.mlkit.vision.segmentation.SegmentationMaskOptions
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
    private lateinit var seekBarQuality: SeekBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit)

        imageView = findViewById(R.id.imageViewEdit)
        seekBarBrightness = findViewById(R.id.seekBarBrightness)
        seekBarContrast = findViewById(R.id.seekBarContrast)
        seekBarSaturation = findViewById(R.id.seekBarSaturation)
        seekBarQuality = findViewById(R.id.seekBarQuality)

        val imageUriString = intent.getStringExtra("imageUri")
        if (imageUriString != null) {
            imageUri = Uri.parse(imageUriString)
            loadImage()
        }

        setupControls()
    }

    private fun loadImage() {
        try {
            imageUri?.let { uri ->
                val inputStream = contentResolver.openInputStream(uri)
                originalBitmap = BitmapFactory.decodeStream(inputStream)
                currentBitmap = originalBitmap?.copy(originalBitmap!!.config, true)
                imageView.setImageBitmap(currentBitmap)
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error loading image: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupControls() {
        seekBarBrightness.max = 100
        seekBarBrightness.progress = 50
        seekBarBrightness.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    updateImage()
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        seekBarContrast.max = 100
        seekBarContrast.progress = 50
        seekBarContrast.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    updateImage()
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        seekBarSaturation.max = 200
        seekBarSaturation.progress = 100
        seekBarSaturation.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    updateImage()
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        seekBarQuality.max = 100
        seekBarQuality.progress = 100
        seekBarQuality.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    updateImage()
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        // Text overlay buttons
        findViewById<Button>(R.id.btnBirthday).setOnClickListener {
            currentBitmap = ImageProcessor.addTextOverlay(currentBitmap!!, "🎂 Happy Birthday 🎂")
            imageView.setImageBitmap(currentBitmap)
        }

        findViewById<Button>(R.id.btnDiwali).setOnClickListener {
            currentBitmap = ImageProcessor.addTextOverlay(currentBitmap!!, "Happy Diwali")
            imageView.setImageBitmap(currentBitmap)
        }

        findViewById<Button>(R.id.btnNewYear).setOnClickListener {
            currentBitmap = ImageProcessor.addTextOverlay(currentBitmap!!, "Happy New Year")
            imageView.setImageBitmap(currentBitmap)
        }

        // Effect buttons
        findViewById<Button>(R.id.btnSharpen).setOnClickListener {
            lifecycleScope.launch(Dispatchers.Default) {
                val result = ImageProcessor.applySharpen(currentBitmap!!)
                withContext(Dispatchers.Main) {
                    currentBitmap = result
                    imageView.setImageBitmap(result)
                }
            }
        }

        findViewById<Button>(R.id.btnRemoveNoise).setOnClickListener {
            lifecycleScope.launch(Dispatchers.Default) {
                val result = ImageProcessor.removeNoise(currentBitmap!!)
                withContext(Dispatchers.Main) {
                    currentBitmap = result
                    imageView.setImageBitmap(result)
                }
            }
        }

        findViewById<Button>(R.id.btnClarity).setOnClickListener {
            lifecycleScope.launch(Dispatchers.Default) {
                val result = ImageProcessor.enhanceClarity(currentBitmap!!)
                withContext(Dispatchers.Main) {
                    currentBitmap = result
                    imageView.setImageBitmap(result)
                }
            }
        }

        findViewById<Button>(R.id.btnRemoveBackground).setOnClickListener {
            removeBackgroundWithML()
        }

        findViewById<Button>(R.id.btnVintage).setOnClickListener {
            currentBitmap = ImageProcessor.applyFilter(currentBitmap!!, "vintage")
            imageView.setImageBitmap(currentBitmap)
        }

        findViewById<Button>(R.id.btnGrayscale).setOnClickListener {
            currentBitmap = ImageProcessor.applyFilter(currentBitmap!!, "grayscale")
            imageView.setImageBitmap(currentBitmap)
        }

        findViewById<Button>(R.id.btnSepia).setOnClickListener {
            currentBitmap = ImageProcessor.applyFilter(currentBitmap!!, "sepia")
            imageView.setImageBitmap(currentBitmap)
        }

        findViewById<Button>(R.id.btnReset).setOnClickListener {
            resetImage()
        }

        findViewById<Button>(R.id.btnSave).setOnClickListener {
            saveBitmap()
        }
    }

    private fun updateImage() {
        currentBitmap = originalBitmap?.copy(originalBitmap!!.config, true)

        val brightness = (seekBarBrightness.progress - 50) / 50f * 100f
        currentBitmap = ImageProcessor.adjustBrightness(currentBitmap!!, brightness)

        val contrast = (seekBarContrast.progress - 50) / 50f
        currentBitmap = ImageProcessor.adjustContrast(currentBitmap!!, contrast)

        val saturation = seekBarSaturation.progress / 100f
        currentBitmap = ImageProcessor.adjustSaturation(currentBitmap!!, saturation)

        if (seekBarQuality.progress < 100) {
            currentBitmap = ImageProcessor.scaleImage(currentBitmap!!, seekBarQuality.progress)
        }

        imageView.setImageBitmap(currentBitmap)
    }

    private fun removeBackgroundWithML() {
        Toast.makeText(this, "Processing...", Toast.LENGTH_SHORT).show()
        lifecycleScope.launch(Dispatchers.Default) {
            try {
                val inputImage = InputImage.fromBitmap(currentBitmap!!)
                val options = SegmentationMaskOptions.Builder()
                    .setRawSizeMaskIncluded(true)
                    .build()

                val segmenter = Segmentation.getClient(options)
                segmenter.process(inputImage)
                    .addOnSuccessListener { result ->
                        lifecycleScope.launch(Dispatchers.Default) {
                            val mask = result.confidenceMasks[0]
                            val processedBitmap = applySegmentationMask(currentBitmap!!, mask)
                            withContext(Dispatchers.Main) {
                                currentBitmap = processedBitmap
                                imageView.setImageBitmap(processedBitmap)
                                Toast.makeText(this@EditActivity, "Background removed", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    .addOnFailureListener { e ->
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@EditActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@EditActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun applySegmentationMask(bitmap: Bitmap, mask: Bitmap): Bitmap {
        val result = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)

        for (y in 0 until bitmap.height) {
            for (x in 0 until bitmap.width) {
                val originalPixel = bitmap.getPixel(x, y)
                val maskPixel = mask.getPixel(x % mask.width, y % mask.height)

                val maskValue = android.graphics.Color.red(maskPixel) / 255f

                val a = (android.graphics.Color.alpha(originalPixel) * maskValue).toInt()
                val r = android.graphics.Color.red(originalPixel)
                val g = android.graphics.Color.green(originalPixel)
                val b = android.graphics.Color.blue(originalPixel)

                result.setPixel(x, y, android.graphics.Color.argb(a, r, g, b))
            }
        }

        return result
    }

    private fun resetImage() {
        currentBitmap = originalBitmap?.copy(originalBitmap!!.config, true)
        seekBarBrightness.progress = 50
        seekBarContrast.progress = 50
        seekBarSaturation.progress = 100
        seekBarQuality.progress = 100
        imageView.setImageBitmap(currentBitmap)
        Toast.makeText(this, "Image reset", Toast.LENGTH_SHORT).show()
    }

    private fun saveBitmap() {
        try {
            val pictureDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            if (!pictureDir.exists()) {
                pictureDir.mkdirs()
            }

            val fileName = "IMG_EDIT_${System.currentTimeMillis()}.jpg"
            val file = File(pictureDir, fileName)

            val outputStream = FileOutputStream(file)
            currentBitmap?.compress(Bitmap.CompressFormat.JPEG, 95, outputStream)
            outputStream.flush()
            outputStream.close()

            Toast.makeText(this, "Image saved to Pictures", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Error saving image: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
