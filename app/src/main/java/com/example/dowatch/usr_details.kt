package com.example.dowatch

import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RatingBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.google.android.material.snackbar.Snackbar
import jp.wasabeef.glide.transformations.BlurTransformation
import kotlinx.coroutines.launch
import kotlin.collections.forEach


class usr_details : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_usr_details)

        val back = findViewById<ImageButton>(R.id.Back)
        val poster = findViewById<com.google.android.material.card.MaterialCardView>(R.id.posterCard)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { _, insets ->

            val systemBars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars()
            )

            back.translationY = systemBars.top.toFloat()
            poster.translationY = systemBars.top.toFloat()

            insets
        }
        back.setOnClickListener {
            finish()
        }

        val loading=findViewById<FrameLayout>(R.id.loading)
        val content=findViewById<FrameLayout>(R.id.contentusrdet)
        content.visibility=View.GONE
        loading.visibility=View.VISIBLE
        val showId = intent.getIntExtra("SHOW_ID", -1)
        lifecycleScope.launch {


            val app = application as Application
            val show = app.database.showDao().getOne(showId)

            if(show !=null){
                val title = findViewById<TextView>(R.id.title)
                val year = findViewById<TextView>(R.id.year)
                val usereview = findViewById<TextView>(R.id.usr_review)
                val usrating = findViewById<TextView>(R.id.myrating)
                val thirdpartyid=show.externalId

                searchMoviedetail(thirdpartyid)
                title.text=show.title
                year.text=show.year

                val norate=findViewById<LinearLayout>(R.id.norating)
                val yesrate=findViewById<LinearLayout>(R.id.rating_area)
                if(show.notes!=null&&show.myrating!=null){
                    usereview.text=show.notes
                    usrating.text = "${show.myrating ?: "No rating"}"
                }
                else{
                    norate.visibility=View.VISIBLE
                    yesrate.visibility=View.GONE
                }

            }
            content.visibility=View.VISIBLE
            loading.visibility=View.GONE
        }
    }
    private suspend fun searchMoviedetail(id: String) {
            try {
                val response = RetrofitClient.api.getResult(
                    BuildConfig.OMDB_API_KEY,
                    id
                )
                val header = findViewById<ImageView>(R.id.header)
                val poster = findViewById<ImageView>(R.id.poster)
                val rating = findViewById<TextView>(R.id.rating)
                val genreContainer = findViewById<LinearLayout>(R.id.genreContainer)
                val genres = response.Genre?.split(", ")
                val about = findViewById<TextView>(R.id.about)

                about.text=response.Plot
                rating.text=response.imdbRating

                genres?.forEach { genre ->
                    val genreView = layoutInflater.inflate(
                        R.layout.categories,
                        genreContainer,
                        false
                    )
                    val text = genreView.findViewById<TextView>(R.id.genre)
                    text.text = genre
                    genreContainer.addView(genreView)
                }

                Glide.with(this@usr_details)
                    .load(response.Poster)
                    .into(poster)

                Glide.with(this@usr_details)
                    .load(response.Poster)
                    .transform(
                        CenterCrop(),
                        BlurTransformation(25, 3)
                    )
                    .into(header)

            } catch (e: Exception) {
                Snackbar.make(
                    findViewById(R.id.main),
                    "Unable to load movie",
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }
    }