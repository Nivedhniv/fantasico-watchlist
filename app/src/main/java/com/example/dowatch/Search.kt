package com.example.dowatch

import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.graphics.Color
import com.google.android.material.color.MaterialColors
import android.widget.ImageButton
import android.widget.ImageView

class Search : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContentView(R.layout.activity_search)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val searchtxt = findViewById<SearchView>(R.id.Search)
        val backbtn = findViewById<ImageButton>(R.id.Back)

        val searchEditText = searchtxt.findViewById<EditText>(
            androidx.appcompat.R.id.search_src_text
        )

        val searchIcon = searchtxt.findViewById<ImageView>(
            androidx.appcompat.R.id.search_mag_icon
        )
        val closeIcon = searchtxt.findViewById<ImageView>(
            androidx.appcompat.R.id.search_close_btn
        )

        val color = MaterialColors.getColor(
            searchtxt,
            com.google.android.material.R.attr.colorOnSurface
        )

        val colordark = MaterialColors.getColor(
            searchtxt,
            com.google.android.material.R.attr.colorOnSurfaceInverse
        )

        closeIcon.setColorFilter(colordark)
        searchIcon.setColorFilter(colordark)
        searchEditText.setHintTextColor(color)

        searchEditText.setTextColor(
            MaterialColors.getColor(
                searchtxt,
                com.google.android.material.R.attr.colorOnSurfaceInverse
            )
        )
        searchtxt.setOnQueryTextListener(object:SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                Toast.makeText(this@Search,query,Toast.LENGTH_SHORT).show()
                return true

            }
            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        }
        )
        backbtn.setOnClickListener {
            finish()
        }
    }

}