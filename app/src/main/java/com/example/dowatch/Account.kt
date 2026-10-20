package com.example.dowatch

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Account : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContentView(R.layout.activity_account)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val terms=findViewById<LinearLayout>(R.id.terms)
        val help=findViewById<LinearLayout>(R.id.help)
        terms.setOnClickListener{
            startActivity(Intent(this, Terms::class.java).putExtra(Terms.EXTRA_PAGE, Terms.PAGE_LEGAL))
        }
        help.setOnClickListener {
            startActivity(Intent(this, Terms::class.java).putExtra(Terms.EXTRA_PAGE, Terms.PAGE_HELP))
        }

        val backbtn=findViewById<ImageButton>(R.id.Back)
        backbtn.setOnClickListener {
            finish()
        }

    }
}