package com.example.drawingapp

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.media.Image
import android.media.MediaScannerConnection
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.get
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.dialog_brush_size.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception

class MainActivity : AppCompatActivity() {

    private var myCurrentBrushColor: ImageButton? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val butttton_back: Button = findViewById(R.id.button3)

        butttton_back.setOnClickListener {
            val intent = Intent(this@MainActivity, ManuActivity::class.java)
            startActivity(intent)
        }


        drawingView.setSizeBrush(20.toFloat())
        myCurrentBrushColor = colorPallet[1] as ImageButton
        myCurrentBrushColor!!.setImageDrawable(
            ContextCompat.getDrawable(
                this,
                R.drawable.pallet_pressed
            )
        )

        imageButtonBrush.setOnClickListener {
            brushSizeChooserDialog()
        }


        val imageButtonGallery = findViewById<ImageButton>(R.id.imageButtonGallery)
        imageButtonGallery.setOnClickListener {
            if (isReadStorageAllowed()) {

                val pickPhotoIntent = Intent(
                    Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                )

                startActivityForResult(pickPhotoIntent, GALLERY)
            } else {
                requestStoragePermission()
            }
        }


        val imageButtonUndo = findViewById<ImageButton>(R.id.imageButtonUndo)
        imageButtonUndo.setOnClickListener {
            drawingView.onClickUndo()
        }


        val imageButtonSave = findViewById<ImageButton>(R.id.imageButtonSave)
        imageButtonSave.setOnClickListener {
            if (isReadStorageAllowed()) {

                BitmapAsyncTask(getBitmapFromView(drawingViewContainer)).execute()
            } else {

                requestStoragePermission()
            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == GALLERY) {
                try {
                    if (data!!.data != null) {

                        val background = findViewById<ImageView>(R.id.backgroundImage)
                        background.visibility = View.VISIBLE
                        background.setImageURI(data.data)
                    } else {

                        Toast.makeText(
                            this@MainActivity,
                            "Error in changing the background image please try again.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }


    private fun brushSizeChooserDialog() {
        val brushDialog = Dialog(this)
        brushDialog.setContentView(R.layout.dialog_brush_size)

        brushDialog.setTitle("Brush Size: ")
        val smallButton = brushDialog.imageButtonSmall
        val mediumButton = brushDialog.imageButtonMedium
        val largeButton = brushDialog.imageButtonLarge

        smallButton.setOnClickListener {
            drawingView.setSizeBrush(10.toFloat())
            brushDialog.dismiss()
        }

        mediumButton.setOnClickListener {
            drawingView.setSizeBrush(20.toFloat())
            brushDialog.dismiss()
        }

        largeButton.setOnClickListener {
            drawingView.setSizeBrush(30.toFloat())
            brushDialog.dismiss()
        }

        brushDialog.show()
    }


    fun paintClicked(view: View) {
        if (view != myCurrentBrushColor) {
            val imageButton = view as ImageButton
            val colorTag = imageButton.tag.toString()

            // Set newly chosen color pallet to pressed
            drawingView.setColor(colorTag)
            imageButton.setImageDrawable(
                ContextCompat.getDrawable(
                    this,
                    R.drawable.pallet_pressed
                )
            )


            myCurrentBrushColor!!.setImageDrawable(
                ContextCompat.getDrawable(
                    this,
                    R.drawable.pallet_normal
                )
            )

            myCurrentBrushColor = view
        }
    }


    private fun requestStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                arrayOf(
                    android.Manifest.permission.READ_EXTERNAL_STORAGE,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                ).toString()
            )
        ) {
            Toast.makeText(
                this,
                "Need permission to change the background.",
                Toast.LENGTH_SHORT
            ).show()
        }
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            ),
            STORAGE_PERMISSION_CODE
        )
    }

        override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORAGE_PERMISSION_CODE) {

            if (grantResults.isNotEmpty() &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED
            ) {
                Toast.makeText(
                    this@MainActivity,
                    "Permission granted, you can read the storage.",
                    Toast.LENGTH_SHORT
                ).show()
            } else {

                Toast.makeText(
                    this@MainActivity,
                    "Oops, you have just denied the permission.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }


    private fun isReadStorageAllowed(): Boolean {
        val result = ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        )

        return result == PackageManager.PERMISSION_GRANTED
    }


    private fun getBitmapFromView(view: View): Bitmap {

        val returnedBitmap = Bitmap.createBitmap(
            view.width,
            view.height,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(returnedBitmap)


        val backgroundDrawable = view.background
        if (backgroundDrawable != null) {
            backgroundDrawable.draw(canvas)
        } else {
            canvas.drawColor(Color.WHITE)
        }

        view.draw(canvas)

        return returnedBitmap
    }


    private inner class BitmapAsyncTask(val myBitmap: Bitmap) :
        AsyncTask<Any, Void, String>() {

        private lateinit var myProgressDialog: Dialog

        override fun doInBackground(vararg params: Any?): String {
            var result = ""
            try {

                val bytes = ByteArrayOutputStream()
                myBitmap.compress(Bitmap.CompressFormat.PNG, 90, bytes)


                val file = File(
                    externalCacheDir!!.absoluteFile.toString() +
                            File.separator + "DrawingApp" +
                            System.currentTimeMillis() / 1000 +
                            ".png"
                )

                val fileOut = FileOutputStream(file)
                fileOut.write(bytes.toByteArray())
                fileOut.close()
                result = file.absolutePath
            } catch (e: Exception) {
                result = ""
                e.printStackTrace()
            }

            return result
        }

        override fun onPreExecute() {
            super.onPreExecute()
            showProgressDialog()
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            cancelProgressDialog()
            if (result!!.isNotEmpty()) {
                Toast.makeText(
                    this@MainActivity,
                    "File saved successfully. $result",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    this@MainActivity,
                    "File could not be saved.",
                    Toast.LENGTH_SHORT
                ).show()
            }
            MediaScannerConnection.scanFile(this@MainActivity, arrayOf(result), null) { path, uri ->
                val shareIntent = Intent()
                shareIntent.action = Intent.ACTION_SEND
                shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
                shareIntent.type = "image/png"

                startActivity(
                    Intent.createChooser(
                        shareIntent, "Share"
                    )
                )
            }
        }


        private fun showProgressDialog() {
            myProgressDialog = Dialog(this@MainActivity)
            myProgressDialog.setContentView(R.layout.dialog_custom_process)
            myProgressDialog.show()
        }


        private fun cancelProgressDialog() {
            myProgressDialog.dismiss()
        }

    }


    companion object {
        private const val STORAGE_PERMISSION_CODE = 1
        private const val GALLERY = 2
    }

}