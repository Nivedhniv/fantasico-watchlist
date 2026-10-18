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
import android.widget.Button
import androidx.core.view.isVisible
import android.util.Log
import android.text.TextUtils

class usr_details : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_usr_details)

        val back = findViewById<ImageButton>(R.id.Back)
        val poster =
            findViewById<com.google.android.material.card.MaterialCardView>(R.id.posterCard)
        val status=findViewById<Button>(R.id.status)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { _, insets ->

            val systemBars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars()
            )
            back.translationY = systemBars.top.toFloat()
            insets
        }
        back.setOnClickListener {
            finish()
        }

        val options=findViewById<LinearLayout>(R.id.options)

        status.setOnClickListener {
            options.isVisible = !options.isVisible
        }

        val loading = findViewById<FrameLayout>(R.id.loading)
        val content = findViewById<FrameLayout>(R.id.contentusrdet)

        val showId = intent.getIntExtra("SHOW_ID", -1)
        lifecycleScope.launch {

            val app = application as Application
            val show = app.database.showDao().getOne(showId)

            if (show != null) {
                val title = findViewById<TextView>(R.id.title)
                val year = findViewById<TextView>(R.id.year)
                val usereview = findViewById<TextView>(R.id.usr_review)
                val usrating = findViewById<TextView>(R.id.myrating)

                val parts = show.externalId.split("_", limit = 2)
                val mediaType = parts.getOrNull(0) ?: "movie"
                val thirdpartyid = parts.getOrNull(1) ?: show.externalId

                searchMoviedetail(thirdpartyid)
                val success = searchMoviedetail(thirdpartyid, mediaType)

                if (success) {
                    content.visibility = View.VISIBLE
                    loading.visibility = View.GONE
                }

                title.text = show.title
                year.text = show.year

                val norate = findViewById<LinearLayout>(R.id.norating)
                val yesrate = findViewById<LinearLayout>(R.id.rating_area)
                if (show.notes != null && show.myrating != null) {
                    usereview.text = show.notes
                    usrating.text = "${show.myrating ?: "No rating"}"
                } else {
                    norate.visibility = View.VISIBLE
                    yesrate.visibility = View.GONE
                }
                val retry = findViewById<Button>(R.id.retry)
                val errormsg = findViewById<LinearLayout>(R.id.error)
                retry.setOnClickListener {
                    lifecycleScope.launch {
                        errormsg.visibility = View.GONE
                        loading.visibility = View.VISIBLE
                        val success = searchMoviedetail(thirdpartyid, mediaType)

                        loading.visibility = View.GONE

                        if (success) {
                            content.visibility = View.VISIBLE
                        }
                    }
                }
            }
        }
    }


    private suspend fun searchMoviedetail(
        id: String,
        mediaType: String = "movie"
    ): Boolean {
        return try {
            val errormsg = findViewById<LinearLayout>(R.id.error)
            errormsg.visibility = View.GONE

            val response = when (mediaType) {
                "tv" -> TmdbRetrofitClient.api.getTvDetails(id.toInt())
                else -> TmdbRetrofitClient.api.getMovieDetails(id.toInt())
            }

            val header = findViewById<ImageView>(R.id.header)
            val poster = findViewById<ImageView>(R.id.poster)
            val rating = findViewById<TextView>(R.id.rating)
            val genreContainer =
                findViewById<LinearLayout>(R.id.genreContainer)
            val about = findViewById<TextView>(R.id.about)

            val readMore = findViewById<TextView>(R.id.click)
            val readGrad = findViewById<View>(R.id.reveal)
            var expanded = false

            readMore.setOnClickListener {
                expanded = !expanded

                if (expanded) {
                    about.maxLines = Int.MAX_VALUE
                    about.ellipsize = null
                    about.setPadding(
                        about.paddingLeft,
                        about.paddingTop,
                        about.paddingRight,
                        (40 * resources.displayMetrics.density).toInt()
                    )
                    readMore.text = "Click to collapse"
                    readGrad.visibility = View.GONE
                } else {
                    about.maxLines = 5
                    about.ellipsize = TextUtils.TruncateAt.END

                    readMore.text = "Click to expand"
                    readGrad.visibility = View.VISIBLE
                }
            }

            about.text = response.overview ?: "No description available."


            rating.text = response.vote_average?.let {
                String.format(java.util.Locale.US, "%.1f", it)
            } ?: "N/A"

            genreContainer.removeAllViews()

            response.genres?.forEach { genre ->
                val genreView = layoutInflater.inflate(
                    R.layout.categories,
                    genreContainer,
                    false
                )

                genreView.findViewById<TextView>(R.id.genre).text =
                    genre.name

                genreContainer.addView(genreView)
            }

            val posterUrl = response.poster_path?.let {
                "https://image.tmdb.org/t/p/w500$it"
            }

            val backdropUrl = response.backdrop_path?.let {
                "https://image.tmdb.org/t/p/w1280$it"
            }

            Glide.with(this@usr_details)
                .load(posterUrl)
                .into(poster)

            Glide.with(this@usr_details)
                .load(backdropUrl ?: posterUrl)
                .into(header)

            try {
                    // Fetch credits only once
                    val credits = when (mediaType) {
                        "tv" -> TmdbRetrofitClient.api.getTvCredits(response.id.toInt())
                        else -> TmdbRetrofitClient.api.getMovieCredits(response.id.toInt())
                    }

                    // Display cast
                    val castContainer =
                        findViewById<LinearLayout>(R.id.castContainer)

                    castContainer.removeAllViews()

                    credits.cast.orEmpty().take(10).forEach { member ->
                        val personView = layoutInflater.inflate(
                            R.layout.roundpic,
                            castContainer,
                            false
                        )

                        personView.findViewById<TextView>(R.id.name).text =
                            member.name

                        personView.findViewById<TextView>(R.id.role).text =
                            member.character ?: "Actor"

                        val imageUrl = member.profile_path?.let {
                            "https://image.tmdb.org/t/p/w185$it"
                        }

                        Glide.with(this@usr_details)
                            .load(imageUrl)
                            .circleCrop()
                            .into(personView.findViewById(R.id.peoplepic))

                        castContainer.addView(personView)
                    }

                    // Display crew using the SAME credits response
                    val crewContainer =
                        findViewById<LinearLayout>(R.id.crewContainer)

                    crewContainer.removeAllViews()

                    credits.crew.orEmpty()
                        .filter {
                            it.job in listOf(
                                "Director",
                                "Writer",
                                "Screenplay",
                                "Original Music Composer"
                            )
                        }
                        .distinctBy { "${it.id}_${it.job}" }
                        .take(10)
                        .forEach { member ->
                            val personView = layoutInflater.inflate(
                                R.layout.roundpic,
                                crewContainer,
                                false
                            )

                            personView.findViewById<TextView>(R.id.name).text =
                                member.name

                            personView.findViewById<TextView>(R.id.role).text =
                                member.job ?: member.department ?: "Crew"

                            val imageUrl = member.profile_path?.let {
                                "https://image.tmdb.org/t/p/w185$it"
                            }

                            Glide.with(this@usr_details)
                                .load(imageUrl)
                                .circleCrop()
                                .into(personView.findViewById(R.id.peoplepic))

                            crewContainer.addView(personView)
                        }

                } catch (e: Exception) {
                    Log.e(
                        "TMDB_CREDITS",
                        "Failed to load cast/crew for $response.id($mediaType)",
                        e
                    )

                }

            true

        } catch (e: Exception) {
            Log.e(
                "TMDB_USER_DETAILS",
                "Failed to load saved show ID: $id",
                e
            )

            val errormsg = findViewById<LinearLayout>(R.id.error)
            val content =
                findViewById<FrameLayout>(R.id.contentusrdet)

            content.visibility = View.GONE
            errormsg.visibility = View.VISIBLE

            val goback = findViewById<Button>(R.id.goback)

            goback.setOnClickListener {
                finish()
            }

            false
        }
    }


}
