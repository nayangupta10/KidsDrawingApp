package com.nayan.kidsdrawingapp

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.media.MediaScannerConnection
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.ImageButton
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

    private var mImageButtonCurrentPaint: ImageButton? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        drawingView.setSizeForBrush(20.toFloat())

        mImageButtonCurrentPaint = llPaintColor[0] as ImageButton
        mImageButtonCurrentPaint!!.setImageDrawable(
            ContextCompat.getDrawable(
                this,
                R.drawable.pallet_pressed
            )
        )

        ibBrush.setOnClickListener {
            showBrushSizeChooserDialog()
        }

        ibGallery.setOnClickListener {
            if (isReadStorageAllowed()) {
                //run our code to get image from gallery

            } else {
                requestStoragePermission()
            }
        }

        ibUndo.setOnClickListener {
            drawingView.onClickUndo()
        }

        ibSave.setOnClickListener {
            if (isReadStorageAllowed())
                BitMapAsyncTask(getBitMapFromView(flDrawingViewContainer)).execute()
            else
                requestStoragePermission()
        }
    }

    private fun showBrushSizeChooserDialog() {
        val brushSizeDialog = Dialog(this)
        brushSizeDialog.setContentView(R.layout.dialog_brush_size)
        brushSizeDialog.setTitle("Brush Size")
        brushSizeDialog.show()
        val smallBrush = brushSizeDialog.ibSmall
        smallBrush.setOnClickListener {
            drawingView.setSizeForBrush(10.toFloat())
            brushSizeDialog.dismiss()
        }

        brushSizeDialog.ibMedium.setOnClickListener {
            drawingView.setSizeForBrush(20.toFloat())
            brushSizeDialog.dismiss()
        }

        brushSizeDialog.ibLarge.setOnClickListener {
            drawingView.setSizeForBrush(30.toFloat())
            brushSizeDialog.dismiss()
        }
    }

    fun painClicked(view: View) {
        if (view !== mImageButtonCurrentPaint) {
            val imageButton = view as ImageButton

            val colorTag = imageButton.tag.toString()
            drawingView.setColor(colorTag)
            imageButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.pallet_pressed))
            mImageButtonCurrentPaint!!.setImageDrawable(
                ContextCompat.getDrawable(
                    this,
                    R.drawable.pallet_normal
                )
            )
            mImageButtonCurrentPaint = view
        }
    }

    private fun requestStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ).toString()
            )
        ) {
            Toast.makeText(this, "Need permission to add background", Toast.LENGTH_SHORT).show()
        }
        ActivityCompat.requestPermissions(
            this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
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
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(
                    this,
                    "Permission granted now you read the storage.",
                    Toast.LENGTH_SHORT
                ).show()

            } else {
                Toast.makeText(this, "Oops you just denied the permission.", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun isReadStorageAllowed(): Boolean {
        val result =
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)

        val pickPhotoIntent =
            Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(pickPhotoIntent, GALLERY)

        return result == PackageManager.PERMISSION_GRANTED
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == GALLERY) {
                try {
                    if (data!!.data != null) {
                        ivBackground.visibility = View.VISIBLE
                        ivBackground.setImageURI(data.data)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun getBitMapFromView(view: View): Bitmap {
        val returnedBitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(returnedBitmap)
        val bgDrawable = view.background
        if (bgDrawable != null) {
            bgDrawable.draw(canvas)
        } else {
            canvas.drawColor(Color.WHITE)
        }
        view.draw(canvas)
        return returnedBitmap
    }

    private inner class BitMapAsyncTask(val mBitmap: Bitmap) : AsyncTask<Any, Void, String>() {

        private lateinit var mProgressDialog: Dialog

        override fun onPreExecute() {
            super.onPreExecute()
            showProgressDialog()
        }

        override fun doInBackground(vararg params: Any?): String {
            var result = ""
            try {
                val bytes = ByteArrayOutputStream()
                mBitmap.compress(Bitmap.CompressFormat.PNG, 90, bytes)
                val f =
                    File(
                        externalCacheDir!!.absoluteFile.toString()
                                + File.separator + "KidDrawingApp_"
                                + System.currentTimeMillis() / 1000 + ".png"
                    )
                val fos = FileOutputStream(f)
                fos.write(bytes.toByteArray())
                fos.close()
                result = f.absolutePath
            } catch (e: Exception) {
                result = ""
                e.printStackTrace()
            }
            return result
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            cancelProgressDialog()
            if (result!!.isNotEmpty()) {
                Toast.makeText(
                    this@MainActivity,
                    "File Saved Successfully: $result",
                    Toast.LENGTH_SHORT
                )
                    .show()
            } else {
                Toast.makeText(
                    this@MainActivity,
                    "Something went wrong while saving file.",
                    Toast.LENGTH_SHORT
                ).show()
            }
            MediaScannerConnection.scanFile(this@MainActivity, arrayOf(result), null) { path, uri ->
                val shareIntent = Intent()
                shareIntent.action = Intent.ACTION_SEND
                shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
                shareIntent.type = "image/png"
                startActivity(Intent.createChooser(shareIntent, "Share"))
            }

        }


        private fun showProgressDialog() {
            mProgressDialog = Dialog(this@MainActivity)
            mProgressDialog.setContentView(R.layout.dialog_custom_progress_bar)
            mProgressDialog.show()
        }

        private fun cancelProgressDialog() {
            mProgressDialog.cancel()
        }
    }


    companion object {
        private const val STORAGE_PERMISSION_CODE = 1
        private const val GALLERY = 2
    }
}