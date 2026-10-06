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
import android.view.View
import android.widget.ScrollView
import android.widget.LinearLayout
import androidx.lifecycle.lifecycleScope
import android.util.Log
import android.widget.GridLayout
import kotlinx.coroutines.launch
import android.widget.TextView
import com.example.dowatch.BuildConfig
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import android.view.inputmethod.InputMethodManager
import android.content.Intent
import android.widget.FrameLayout
import com.google.android.material.snackbar.Snackbar

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

        val suggestions = findViewById<com.google.android.material.card.MaterialCardView>(R.id.searchSuggestions)
        val searchtxt = findViewById<SearchView>(R.id.Search)
        val backbtn = findViewById<ImageButton>(R.id.Back)
        val searchEditText = searchtxt.findViewById<EditText>(
            androidx.appcompat.R.id.search_src_text
        )
        searchEditText.background = null
        searchEditText.setPadding(0, 0, 0, 0)

        val searchIcon = searchtxt.findViewById<ImageView>(
            androidx.appcompat.R.id.search_mag_icon
        )
        val closeIcon = searchtxt.findViewById<ImageView>(
            androidx.appcompat.R.id.search_close_btn
        )

        val color = MaterialColors.getColor(
            searchtxt,
            com.google.android.material.R.attr.colorOnSurfaceInverse
        )

        val colordark = MaterialColors.getColor(
            searchtxt,
            com.google.android.material.R.attr.colorOnSurface
        )

        closeIcon.setColorFilter(colordark)
        searchIcon.setColorFilter(colordark)

        searchEditText.setTextColor(
            MaterialColors.getColor(
                searchtxt,
                com.google.android.material.R.attr.colorOnSurface
            )
        )
        //search actions
        searchtxt.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            val loading=findViewById<FrameLayout>(R.id.loading)
            override fun onQueryTextSubmit(query: String?): Boolean {
                suggestions.visibility = View.GONE

                searchMovies(query ?: "")
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText != null && newText.trim().length >= 3) {

                    suggestions.visibility = View.VISIBLE

                    val suggestionContainer =
                        findViewById<LinearLayout>(R.id.suggestionContainer)

                    lifecycleScope.launch {
                        try {
                            val response = RetrofitClient.api.searchMovies(
                                BuildConfig.OMDB_API_KEY,
                                newText.trim()
                            )

                            suggestionContainer.removeAllViews()

                            response.Search?.forEach { result ->

                                val suggestion = layoutInflater.inflate(
                                    R.layout.sugestion,
                                    suggestionContainer,
                                    false
                                )

                                val name = suggestion.findViewById<TextView>(R.id.Name)
                                name.text = result.Title

                                suggestion.setOnClickListener {
                                    val selectedTitle = result.Title ?: return@setOnClickListener

                                    searchEditText.setText(selectedTitle)
                                    searchMovies(selectedTitle)
                                }

                                suggestionContainer.addView(suggestion)
                            }

                        } catch (e: Exception) {
                            Log.e("SUGGESTION_ERROR", "Suggestion search failed", e)

                            suggestionContainer.removeAllViews()
                            suggestions.visibility = View.GONE
                        }
                    }
                }
                else {
                    suggestions.visibility = View.GONE
                }
                return true
            }
        })

        backbtn.setOnClickListener {
            finish()
        }
    }
    private fun searchMovies(query: String) {
        val suggestions = findViewById<com.google.android.material.card.MaterialCardView>(R.id.searchSuggestions)

        suggestions.visibility = View.GONE

        lifecycleScope.launch {
            val loading=findViewById<FrameLayout>(R.id.loading)
            val resultarea=findViewById<ScrollView>(R.id.resultarea)
            resultarea.visibility=View.GONE
            loading.visibility= View.VISIBLE
            try{
                val response = RetrofitClient.api.searchMovies(
                    BuildConfig.OMDB_API_KEY,
                    query
                )

                val resultsContainer =
                    findViewById<GridLayout>(R.id.result)

                resultsContainer.removeAllViews()

                response.Search?.forEach { result ->

                    val movieCard = layoutInflater.inflate(
                        R.layout.result,
                        resultsContainer,
                        false
                    )

                    val poster =
                        movieCard.findViewById<ImageView>(R.id.poster)

                    val title =
                        movieCard.findViewById<TextView>(R.id.title)
                    title.text = result.Title

                    Glide.with(this@Search)
                        .load(result.Poster)
                        .into(poster)
                    loading.visibility= View.GONE
                    resultarea.visibility=View.VISIBLE
                    resultsContainer.addView(movieCard)

                    movieCard.setOnClickListener {
                        val intent = Intent(this@Search, details::class.java)
                        intent.putExtra("id", result.imdbID)
                        intent.putExtra("poster", result.Poster)
                        startActivity(intent)
                    }
                }
            }
            catch (e: Exception) {
                Log.e("SEARCH_ERROR", "SEARCH FAILED", e)

                loading.visibility = View.GONE

                Snackbar.make(
                    findViewById(R.id.main),
                    "Unable to Connect",
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }
    }
}
