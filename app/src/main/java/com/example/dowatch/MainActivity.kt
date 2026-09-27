package com.example.dowatch

import android.os.Bundle
import android.widget.TextView
import android.content.Intent
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.view.View
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts


class MainActivity : AppCompatActivity()
{
    private lateinit var img: ImageView
    private lateinit var text: TextView

    private val filePicker = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            img.setImageURI(uri)
            img.visibility = View.VISIBLE
            text.visibility=View.GONE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val btn=findViewById<ImageButton>(R.id.searchpage)
        val acc=findViewById<ImageButton>(R.id.account)
        val ios=findViewById<TextView>(R.id.greeting)

        btn.setOnClickListener {
            val intent= Intent(this, Search::class.java)
            startActivity(intent)
        }
        acc.setOnClickListener {
            val intent= Intent(this, Account::class.java)
            startActivity(intent)
        }
        ios.setOnClickListener {
            val intent= Intent(this, Iosdemo::class.java)
            startActivity(intent)
        }

    }
}