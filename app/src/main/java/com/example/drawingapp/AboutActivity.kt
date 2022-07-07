package com.example.drawingapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView

class AboutActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)




        val buttton_back: Button = findViewById(R.id.button2)





        buttton_back.setOnClickListener {
            val intent = Intent(this@AboutActivity, ManuActivity::class.java)
            startActivity(intent)
        }



    }
}