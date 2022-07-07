package com.example.drawingapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import kotlin.system.exitProcess

class ManuActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manu)



        val button_start: Button = findViewById(R.id.btn_start)
        val button_exit: Button = findViewById(R.id.btn_exit)
        val button_about: Button = findViewById(R.id.btn_about)


        button_start.setOnClickListener {

            val intent = Intent(this@ManuActivity, MainActivity::class.java)
            startActivity(intent)


        }

        button_about.setOnClickListener{

            val intent1 = Intent(this@ManuActivity, AboutActivity::class.java)
            startActivity(intent1)

        }

        button_exit.setOnClickListener {

            moveTaskToBack(true);
            exitProcess(-1)
            finishAffinity()


        }

    }
}


