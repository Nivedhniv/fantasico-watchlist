package com.example.dowatch
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomsheet.BottomSheetDialog
import android.view.View
import com.google.android.material.snackbar.Snackbar
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.FrameLayout
import android.widget.ImageView
import android.app.DatePickerDialog
import android.widget.Toast
import android.widget.Button
import android.widget.EditText
import android.widget.GridLayout
import android.widget.HorizontalScrollView
import android.widget.ImageButton
import java.util.Calendar
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RatingBar
import jp.wasabeef.glide.transformations.BlurTransformation
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import androidx.room.PrimaryKey
import com.bumptech.glide.Glide
import com.example.dowatch.data.Show
import  com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.coroutines.launch
import android.util.Log
import com.google.android.material.button.MaterialButtonToggleGroup
import android.R.attr.id
import android.text.TextUtils

class details : AppCompatActivity() {
    private var movieDetails: TmdbDetailsResponse? = null
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_details)

        val mediaType = intent.getStringExtra("mediaType") ?: "movie"
        val back = findViewById<ImageButton>(R.id.Back)
        back.setOnClickListener {
            finish()
        }

        val poster = findViewById<com.google.android.material.card.MaterialCardView>(R.id.posterCard)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { _, insets ->

            val systemBars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars()
            )

            back.translationY = systemBars.top.toFloat()
            insets
        }
        val id = intent.getStringExtra("id")
        if (id != null) {
            lifecycleScope.launch {
                val content = findViewById<FrameLayout>(R.id.content)
                val loading=findViewById<FrameLayout>(R.id.loading)
                val success = searchMoviedetail(id, mediaType)

                if (success) {
                    content.visibility = View.VISIBLE
                    loading.visibility = View.GONE
                }
            }
            val retry = findViewById<Button>(R.id.retry)
            val errormsg = findViewById<LinearLayout>(R.id.error)
            val content = findViewById<FrameLayout>(R.id.content)
            val loading=findViewById<FrameLayout>(R.id.loading)
            retry.setOnClickListener {
                lifecycleScope.launch {
                    errormsg.visibility = View.GONE
                    loading.visibility = View.VISIBLE
                    val success = searchMoviedetail(id, mediaType)
                    loading.visibility = View.GONE
                    if (success) {
                        content.visibility = View.VISIBLE
                    }
                }
            }
        } else {
            Toast.makeText(this, "Unable to load movie", Toast.LENGTH_SHORT).show()
        }

        val addshow = findViewById<Button>(R.id.addbtn)

        addshow.setOnClickListener {
            val movie = movieDetails ?: return@setOnClickListener
            val id = movie.id.toString()
            Addshowsheet(id, mediaType)
        }

    }


    private suspend fun searchMoviedetail(
        id: String,
        mediaType: String = "movie"
    ): Boolean {
        return try {
            val response = when (mediaType) {
                "tv" -> TmdbRetrofitClient.api.getTvDetails(id.toInt())
                else -> TmdbRetrofitClient.api.getMovieDetails(id.toInt())
            }

            movieDetails = response

            val title = findViewById<TextView>(R.id.title)
            val posterarea = findViewById<ImageView>(R.id.poster)
            val year = findViewById<TextView>(R.id.year)
            val rating = findViewById<TextView>(R.id.rating)
            val about = findViewById<TextView>(R.id.about)
            val header = findViewById<ImageView>(R.id.header)

            val genres = response.genres

            title.text = response.title ?: response.name ?: "Unknown title"

            year.text = response.release_date
                ?.takeIf { it.isNotBlank() }
                ?: response.first_air_date
                    ?.takeIf { it.isNotBlank() }
                        ?: "Year unavailable"

            rating.text = response.vote_average?.let {
                String.format(java.util.Locale.US, "%.1f", it)
            } ?: "N/A"

            about.text = response.overview ?: "No description available."
            val readMore = findViewById<TextView>(R.id.click)
            val readGrad = findViewById<View>(R.id.reveal)
            var expanded = false
            readMore.setOnClickListener {
                expanded = !expanded

                about.animate()
                    .alpha(0f)
                    .setDuration(120)
                    .withEndAction {
                        if (expanded) {
                            about.maxLines = Int.MAX_VALUE
                            about.ellipsize = null
                            about.setPadding(
                                about.paddingLeft,
                                about.paddingTop,
                                about.paddingRight,
                                (40 * resources.displayMetrics.density).toInt()
                            )
                            readMore.text = "Tap to collapse"
                            readGrad.visibility = View.GONE
                        } else {
                            about.maxLines = 5
                            about.ellipsize = TextUtils.TruncateAt.END
                            readMore.text = "Tap to expand"
                            readGrad.visibility = View.VISIBLE
                            about.setPadding(
                                about.paddingLeft,
                                about.paddingTop,
                                about.paddingRight,
                                (20 * resources.displayMetrics.density).toInt()
                            )
                        }
                        about.alpha = 0f
                        about.animate()
                            .alpha(1f)
                            .setDuration(180)
                            .start()
                    }
                    .start()
            }
            val genreContainer =
                findViewById<LinearLayout>(R.id.genreContainer)

            genreContainer.removeAllViews()

            genres?.forEach { genre ->
                val genreView = layoutInflater.inflate(
                    R.layout.categories,
                    genreContainer,
                    false
                )

                val text = genreView.findViewById<TextView>(R.id.genre)
                text.text = genre.name

                genreContainer.addView(genreView)
            }

            val posterUrl = response.poster_path?.let {
                "https://image.tmdb.org/t/p/w500$it"
            }

            val backdropUrl = response.backdrop_path?.let {
                "https://image.tmdb.org/t/p/w1280$it"
            }

            Glide.with(this@details)
                .load(posterUrl)
                .into(posterarea)

            Glide.with(this@details)
                .load(backdropUrl ?: posterUrl)
                .into(header)

                    try {
                        // Fetch credits once
                        val credits = when (mediaType) {
                            "tv" -> TmdbRetrofitClient.api.getTvCredits(id.toInt())
                            else -> TmdbRetrofitClient.api.getMovieCredits(id.toInt())
                        }

                        // Cast
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

                            Glide.with(this@details)
                                .load(imageUrl)
                                .circleCrop()
                                .into(personView.findViewById(R.id.peoplepic))

                            castContainer.addView(personView)
                        }

                        // Crew, using the same credits response
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

                                Glide.with(this@details)
                                    .load(imageUrl)
                                    .circleCrop()
                                    .into(personView.findViewById(R.id.peoplepic))

                                crewContainer.addView(personView)
                            }

                    } catch (e: Exception) {
                        Log.e(
                            "TMDB_CREDITS",
                            "Failed to load cast/crew for $id ($mediaType)",
                            e
                        )
                    }
            true

        } catch (e: Exception) {
            Log.e("TMDB_DETAILS", "Failed to load details for ID: $id", e)

            val errormsg = findViewById<LinearLayout>(R.id.error)
            val content = findViewById<FrameLayout>(R.id.content)

            content.visibility = View.GONE
            errormsg.visibility = View.VISIBLE

            val goback = findViewById<Button>(R.id.goback)

            goback.setOnClickListener {
                finish()
            }

            false
        }
    }


    //sheet function

    private fun Addshowsheet(id: String, mediaType: String = "movie") {
        val sheetview = layoutInflater.inflate(
            R.layout.add_show,
            null
        )

        val dialog = BottomSheetDialog(this)
        dialog.setContentView(sheetview)
        dialog.show()

        var movie: TmdbDetailsResponse? = null

        val bottomSheet = dialog.findViewById<View>(
            com.google.android.material.R.id.design_bottom_sheet
        )

        bottomSheet?.let {
            val behavior = BottomSheetBehavior.from(it)
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.isDraggable = false
        }

        val closebtn =
            sheetview.findViewById<ImageButton>(R.id.closeBtn)

        closebtn.setOnClickListener {
            dialog.dismiss()
        }

        val posterurl =
            sheetview.findViewById<ImageView>(R.id.posterurl)

        val showrating =
            sheetview.findViewById<RatingBar>(R.id.ratingBar)

        val review =
            sheetview.findViewById<
                    com.google.android.material.textfield.TextInputEditText
                    >(R.id.review)

        val formtitle =
            sheetview.findViewById<TextView>(R.id.titleform)

        val reviewarea =
            sheetview.findViewById<LinearLayout>(R.id.reviewsection)

        reviewarea.visibility = View.GONE

        val ratingarea =
            sheetview.findViewById<LinearLayout>(R.id.ratingSection)

        ratingarea.visibility = View.GONE

        val statusGroup =
            sheetview.findViewById<MaterialButtonToggleGroup>(
                R.id.statusGroup
            )

        var curstatus = "null"

        statusGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                val addButton = sheetview.findViewById<Button>(R.id.Addmovie)

                addButton.visibility = View.VISIBLE

                curstatus = when (checkedId) {
                    R.id.btnWatching -> "Currently Watching"
                    R.id.btnCompleted -> "Completed"
                    R.id.btnTowatch -> "Plan To Watch"
                    else -> curstatus
                }

                val showReview = checkedId != R.id.btnTowatch

                ratingarea.visibility =
                    if (showReview) View.VISIBLE else View.GONE

                reviewarea.visibility =
                    if (showReview) View.VISIBLE else View.GONE
            }
        }

        lifecycleScope.launch {
            try {
                movie = when (mediaType) {
                    "tv" -> TmdbRetrofitClient.api.getTvDetails(id.toInt())
                    else -> TmdbRetrofitClient.api.getMovieDetails(id.toInt())
                }

                val response = movie!!

                formtitle.text =
                    response.title ?: response.name ?: "Unknown title"

                val posterUrl = response.poster_path?.let {
                    "https://image.tmdb.org/t/p/w500$it"
                }

                Glide.with(this@details)
                    .load(posterUrl)
                    .into(posterurl)

            } catch (e: Exception) {
                Log.e(
                    "TMDB_ADD",
                    "Failed to load $mediaType details for ID: $id",
                    e
                )

                Toast.makeText(
                    this@details,
                    "Unable to load show details",
                    Toast.LENGTH_SHORT
                ).show()

                dialog.dismiss()
            }
        }

        val addmovie = sheetview.findViewById<Button>(R.id.Addmovie)

        addmovie.setOnClickListener {
            if (curstatus == "null") {
                Toast.makeText(
                    this@details,
                    "Select a status",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            val response = movie ?: run {
                Toast.makeText(
                    this@details,
                    "Please wait for the show details to load",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            val show = Show(
                externalId = "${mediaType}_${response.id}",
                title = response.title ?: response.name
                ?: return@setOnClickListener,
                posterurl = response.poster_path?.let {
                    "https://image.tmdb.org/t/p/w500$it"
                } ?: "",
                status = curstatus,
                myrating = showrating.rating,
                year = response.release_date?.take(4)
                    ?: response.first_air_date?.take(4)
                    ?: "",
                notes = review.text?.toString()?.ifBlank { null }
            )

            lifecycleScope.launch {
                try {
                    val app = application as Application

                    app.database.showDao().insertshow(show)

                    Toast.makeText(
                        this@details,
                        "Added to Watchlist",
                        Toast.LENGTH_SHORT
                    ).show()
                    val addshow = findViewById<Button>(R.id.addbtn)
                    addshow.visibility=View.GONE
                    dialog.dismiss()

                } catch (e: Exception) {
                    android.util.Log.e("Dowatch", "Insert failed", e)

                    Toast.makeText(
                        this@details,
                        "Failed to add show: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

}