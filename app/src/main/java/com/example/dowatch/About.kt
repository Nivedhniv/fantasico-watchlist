
package com.example.dowatch

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.content.ActivityNotFoundException
import com.google.android.material.bottomsheet.BottomSheetDialog

class About : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContentView(R.layout.activity_about)

        val back = findViewById<ImageButton>(R.id.Back)

        ViewCompat.setOnApplyWindowInsetsListener(
            findViewById(R.id.main)
        ) { _, insets ->
            val systemBars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars()
            )

            back.translationY = systemBars.top.toFloat()
            insets
        }

        val terms = findViewById<LinearLayout>(R.id.terms)
        val help = findViewById<LinearLayout>(R.id.help)
        val backbtn = findViewById<ImageButton>(R.id.Back)
        val contact = findViewById<LinearLayout>(R.id.contact)
        val changelog = findViewById<LinearLayout>(R.id.changelog)
        val git=findViewById<LinearLayout>(R.id.Github)

        terms.setOnClickListener {
            startActivity(
                Intent(this, Terms::class.java)
                    .putExtra(Terms.EXTRA_PAGE, Terms.PAGE_LEGAL)
            )
        }

        help.setOnClickListener {
            startActivity(
                Intent(this, Terms::class.java)
                    .putExtra(Terms.EXTRA_PAGE, Terms.PAGE_HELP)
            )
        }

        backbtn.setOnClickListener {
            finish()
        }

        changelog.setOnClickListener {
            change()
        }

        contact.setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:nivivx@gmail.com")
                putExtra(Intent.EXTRA_SUBJECT, "Contact from Dowatch")
            }

            try {
                startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(
                    this,
                    "No email app found on this device",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        git.setOnClickListener {
            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://github.com/Nivedhniv/fantasico-watchlist.git")
            )
            startActivity(intent)
        }

        val clear=findViewById<LinearLayout>(R.id.clear)

        clear.setOnClickListener {

        }


    }

    private fun change() {
        val sheetview = layoutInflater.inflate(
            R.layout.changelog,
            null
        )

        val dialog = BottomSheetDialog(this)
        dialog.setContentView(sheetview)
        dialog.show()
    }
}
