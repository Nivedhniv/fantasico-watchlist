package com.example.dowatch

import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Button
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import kotlinx.coroutines.launch
import kotlin.collections.forEach
import androidx.core.view.isVisible
import android.util.Log
import android.text.TextUtils
import android.widget.RatingBar
import android.widget.Toast

class usr_details : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_usr_details)

        val back = findViewById<ImageButton>(R.id.Back)
        val poster =
            findViewById<com.google.android.material.card.MaterialCardView>(R.id.posterCard)

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

        val loading = findViewById<FrameLayout>(R.id.loading)
        val content = findViewById<FrameLayout>(R.id.contentusrdet)

        val showId = intent.getIntExtra("SHOW_ID", -1)
        lifecycleScope.launch {

            val app = application as Application
            val show = app.database.showDao().getOne(showId)

            if (show != null) {
                val ratingBar = findViewById<RatingBar>(R.id.myrating)
                val userReview = findViewById<TextView>(R.id.usereview)
                val reviewSection =
                    findViewById<com.google.android.material.textfield.TextInputLayout>(
                        R.id.reviewsection
                    )
                val reviewInput =
                    findViewById<com.google.android.material.textfield.TextInputEditText>(
                        R.id.review
                    )
                val buttons = findViewById<LinearLayout>(R.id.upno)
                val cancelButton = findViewById<Button>(R.id.cancelReview)
                val updateButton = findViewById<Button>(R.id.updateReview)
                val tapToEdit = findViewById<LinearLayout>(R.id.taptoedit)

                // The values currently saved in Room.
                var originalRating = show.myrating ?: 0f
                var originalReview = show.notes.orEmpty()

                var isEditing = false
                var loadingSavedValues = false

                fun displayReview() {
                    userReview.text = originalReview

                    if (isEditing) {
                        // Editing mode
                        userReview.visibility = View.GONE
                        reviewSection.visibility = View.VISIBLE
                        tapToEdit.visibility = View.GONE

                        ratingBar.isEnabled = true
                        reviewInput.isEnabled = true

                        buttons.visibility = View.VISIBLE
                        cancelButton.visibility = View.VISIBLE

                    } else {
                        // Normal viewing mode
                        userReview.visibility =
                            if (originalReview.isBlank()) View.GONE else View.VISIBLE

                        reviewSection.visibility =
                            if (originalReview.isBlank()) View.VISIBLE else View.GONE

                        tapToEdit.visibility = View.VISIBLE
                        ratingBar.isEnabled = false
                        reviewInput.isEnabled = false

                        buttons.visibility = View.GONE
                    }

                    ratingBar.rating = originalRating
                    buttons.visibility = View.GONE
                }

                fun checkForChanges() {
                    if (!isEditing || loadingSavedValues) return

                    val ratingChanged = ratingBar.rating != originalRating
                    val reviewChanged =
                        reviewInput.text?.toString().orEmpty() != originalReview

                    buttons.visibility = View.VISIBLE
                    cancelButton.visibility = View.VISIBLE
                    updateButton.visibility =
                        if (ratingChanged || reviewChanged) View.VISIBLE else View.GONE
                }

                fun enterEditMode() {
                    isEditing = true
                    loadingSavedValues = true

                    ratingBar.rating = originalRating
                    reviewInput.setText(originalReview)

                    displayReview()

                    // Show the parent container and Cancel immediately
                    buttons.visibility = View.VISIBLE
                    cancelButton.visibility = View.VISIBLE
                    updateButton.visibility = View.GONE

                    loadingSavedValues = false
                }

                // Initial display.
                displayReview()

                // Tap the pencil hint to add or edit a review.
                tapToEdit.setOnClickListener {
                    enterEditMode()
                }

                ratingBar.setOnRatingBarChangeListener { _, _, _ ->
                    checkForChanges()
                }

                reviewInput.addTextChangedListener(
                    object : android.text.TextWatcher {
                        override fun beforeTextChanged(
                            s: CharSequence?, start: Int, count: Int, after: Int
                        ) = Unit

                        override fun onTextChanged(
                            s: CharSequence?, start: Int, before: Int, count: Int
                        ) {
                            checkForChanges()
                        }

                        override fun afterTextChanged(s: android.text.Editable?) = Unit
                    }
                )

                cancelButton.setOnClickListener {
                    loadingSavedValues = true

                    ratingBar.rating = originalRating
                    reviewInput.setText(originalReview)

                    isEditing = false
                    displayReview()

                    loadingSavedValues = false
                }

                updateButton.setOnClickListener {
                    val updatedShow = show.copy(
                        myrating = ratingBar.rating,
                        notes = reviewInput.text
                            ?.toString()
                            ?.ifBlank { null }
                    )

                    lifecycleScope.launch {
                        try {
                            app.database.showDao().updateShow(updatedShow)

                            // Establish the newly saved values as the baseline.
                            originalRating = updatedShow.myrating ?: 0f
                            originalReview = updatedShow.notes.orEmpty()

                            isEditing = false
                            displayReview()

                            Toast.makeText(
                                this@usr_details,
                                "Review updated",
                                Toast.LENGTH_SHORT
                            ).show()

                        } catch (e: Exception) {
                            Log.e("Dowatch", "Review update failed", e)

                            Toast.makeText(
                                this@usr_details,
                                "Couldn't update review",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }

                val parts = show.externalId.split("_", limit = 2)
                val mediaType = parts.getOrNull(0) ?: "movie"
                val thirdpartyid = parts.getOrNull(1) ?: show.externalId

                val success = searchMoviedetail(thirdpartyid, mediaType)

                if (success) {
                    content.visibility = View.VISIBLE
                    loading.visibility = View.GONE
                }

                val status = findViewById<Button>(R.id.status)
                status.text = show.status

                val options = findViewById<LinearLayout>(R.id.options)
                val complete = findViewById<Button>(R.id.completed)
                val watching = findViewById<Button>(R.id.Watch)
                val later = findViewById<Button>(R.id.later)
                val drop = findViewById<Button>(R.id.Drop)
                val delete = findViewById<Button>(R.id.delete)

                if (show.status in listOf("Upcoming")) {
                    drop.visibility = View.GONE
                    drop.visibility=View.GONE
                    later.visibility = View.GONE
                    complete.visibility = View.GONE
                }
                if (show.status in listOf("Dropped", "Completed")) {
                    drop.visibility = View.GONE
                    watching.visibility=View.GONE
                    later.visibility = View.GONE
                    complete.visibility = View.GONE
                }
                if (show.status in listOf("Currently Watching")) {
                    watching.visibility= View.GONE
                }
                if (show.status in listOf("Plan To Watch")) {
                    later.visibility= View.GONE
                }
                status.setOnClickListener {
                    options.isVisible = !options.isVisible
                }
                delete.setOnClickListener {
                    lifecycleScope.launch {
                        try {
                            val app = application as Application
                            app.database.showDao().delete(show.id)
                            Toast.makeText(
                                this@usr_details,
                                "Deleted",
                                Toast.LENGTH_LONG
                            ).show()
                            finish()
                        } catch (e: Exception) {
                            Toast.makeText(
                                this@usr_details,
                                "Failed to delete: ${e.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }

                complete.setOnClickListener {
                    lifecycleScope.launch {
                        try {
                            val app = application as Application
                            app.database.showDao().complete(show.id)
                            status.text = "Completed"
                            options.visibility= View.GONE
                            Toast.makeText(
                                this@usr_details,
                                "Completed ${show.title}",
                                Toast.LENGTH_LONG
                            ).show()

                        } catch (e: Exception) {
                            Toast.makeText(
                                this@usr_details,
                                "Failed to update: ${e.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
                drop.setOnClickListener {
                    lifecycleScope.launch {
                        try {
                            val app = application as Application
                            app.database.showDao().dropped(show.id)
                            status.text = "Dropped"
                            options.visibility= View.GONE
                            drop.visibility=View.GONE
                            watching.visibility=View.GONE
                            later.visibility=View.GONE
                            complete.visibility=View.GONE
                            Toast.makeText(
                                this@usr_details,
                                "Dropped ${show.title}",
                                Toast.LENGTH_LONG
                            ).show()

                        } catch (e: Exception) {
                            Toast.makeText(
                                this@usr_details,
                                "Failed to Drop: ${e.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
                watching.setOnClickListener {
                    lifecycleScope.launch {
                        try {
                            val app = application as Application
                            app.database.showDao().watching(show.id)
                            status.text = "Currently Watching"
                            options.visibility= View.GONE
                            watching.visibility=View.GONE
                            Toast.makeText(
                                this@usr_details,
                                "Watching ${show.title}",
                                Toast.LENGTH_LONG
                            ).show()

                        } catch (e: Exception) {
                            Toast.makeText(
                                this@usr_details,
                                "Failed to Watch: ${e.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
                later.setOnClickListener {
                    lifecycleScope.launch {
                        try {
                            val app = application as Application
                            app.database.showDao().later(show.id)
                            status.text = "Completed"
                            options.visibility= View.GONE
                            Toast.makeText(
                                this@usr_details,
                                "Plan To Watch ${show.title}",
                                Toast.LENGTH_LONG
                            ).show()

                        } catch (e: Exception) {
                            Toast.makeText(
                                this@usr_details,
                                "Failed to update Watch later: ${e.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }

                    }
                }

                val title=findViewById<TextView>(R.id.title)
                val year=findViewById<TextView>(R.id.year)
                title.text = show.title
                year.text = show.year

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
