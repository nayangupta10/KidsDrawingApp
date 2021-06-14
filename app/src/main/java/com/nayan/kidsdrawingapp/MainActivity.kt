package com.nayan.kidsdrawingapp

import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.dialog_brush_size.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        drawingView.setSizeForBrush(20.toFloat())
        ibBrush.setOnClickListener {
            Toast.makeText(this, "Button works!", Toast.LENGTH_SHORT).show()
            showBrushSizeChooserDialog()
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
}