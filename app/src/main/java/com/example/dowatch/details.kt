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

class details : AppCompatActivity() {
    private var movieDetails: OmdbResult? = null

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_details)

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
            poster.translationY = systemBars.top.toFloat()

            insets
        }

        val id = intent.getStringExtra("id")
        if (id != null) {
            lifecycleScope.launch {
                val content =
                    findViewById<FrameLayout>(R.id.contentusrdet)
                val loading=findViewById<FrameLayout>(R.id.loading)
                val success = searchMoviedetail(id)
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
                    val success = searchMoviedetail(id)
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
            val id = movie.imdbID ?: return@setOnClickListener

            Addshowsheet(id)
        }

    }

    private suspend fun searchMoviedetail(id: String): Boolean {
        return try {

            val response = RetrofitClient.api.getResult(
                BuildConfig.OMDB_API_KEY,
                id
            )

            movieDetails = response

            val title = findViewById<TextView>(R.id.title)
            val posterarea = findViewById<ImageView>(R.id.poster)
            val year = findViewById<TextView>(R.id.year)
            val rating = findViewById<TextView>(R.id.rating)
            val about = findViewById<TextView>(R.id.about)
            val header = findViewById<ImageView>(R.id.header)

            val genres = response.Genre?.split(", ")

            title.text = response.Title
            year.text = response.Year
            rating.text = response.imdbRating
            about.text = response.Plot

            val genreContainer =
                findViewById<LinearLayout>(R.id.genreContainer)

            genreContainer.removeAllViews()

            genres?.forEach { genre ->

                val genreView = layoutInflater.inflate(
                    R.layout.categories,
                    genreContainer,
                    false
                )

                val text =
                    genreView.findViewById<TextView>(R.id.genre)

                text.text = genre

                genreContainer.addView(genreView)
            }

            Glide.with(this@details)
                .load(response.Poster)
                .into(posterarea)

            Glide.with(this@details)
                .load(response.Poster)
                .transform(
                    CenterCrop(),
                    BlurTransformation(25, 3)
                )
                .into(header)

            true

        } catch (e: Exception) {

            val errormsg =
                findViewById<LinearLayout>(R.id.error)

            val content =
                findViewById<FrameLayout>(R.id.content)

            content.visibility = View.GONE
            errormsg.visibility = View.VISIBLE

            val goback =
                findViewById<Button>(R.id.goback)

            goback.setOnClickListener {
                finish()
            }

            false
        }
    }

    //sheet function
    private fun Addshowsheet( id:String) {
        val sheetview = layoutInflater.inflate(
            R.layout.add_show,
            null
        )

        val dialog = BottomSheetDialog(this)
        dialog.setContentView(sheetview)
        dialog.show()
        var movie: OmdbResult? = null

        val bottomSheet = dialog.findViewById<View>(
            com.google.android.material.R.id.design_bottom_sheet
        )
        bottomSheet?.let {
            val behavior = BottomSheetBehavior.from(it)
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            behavior.isDraggable = false
        }
        val closebtn = sheetview.findViewById<ImageButton>(R.id.closeBtn)
        closebtn.setOnClickListener {
            dialog.dismiss()
        }
        //working sheets
        val posterurl = sheetview.findViewById<ImageView>(R.id.posterurl)
        val showrating = sheetview.findViewById<RatingBar>(R.id.ratingBar)
        val showdate = sheetview.findViewById<EditText>(R.id.date)
        val review =
            sheetview.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.review)
        val formtitle = sheetview.findViewById<TextView>(R.id.titleform)

        val reviewarea = sheetview.findViewById<LinearLayout>(R.id.reviewsection)
        reviewarea.visibility = View.GONE
        val ratingarea = sheetview.findViewById<LinearLayout>(R.id.ratingSection)
        ratingarea.visibility = View.GONE
        val datearea = sheetview.findViewById<LinearLayout>(R.id.datearea)
        datearea.visibility = View.GONE

        //dropdown

        val statusOptions = arrayOf(
            "Watching",
            "Completed",
            "Plan to watch",
            "Upcoming"
        )
        val dropdown = sheetview.findViewById<AutoCompleteTextView>(R.id.status)
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            statusOptions
        )
        dropdown.setAdapter(adapter)

        dropdown.setOnItemClickListener { parent, _, position, _ ->

            when (parent.getItemAtPosition(position).toString()) {

                "Watching" -> {
                    ratingarea.visibility = View.VISIBLE
                    datearea.visibility = View.GONE
                    reviewarea.visibility = View.VISIBLE
                }

                "Completed" -> {
                    ratingarea.visibility = View.VISIBLE
                    datearea.visibility = View.GONE
                    reviewarea.visibility = View.VISIBLE
                }

                "Plan to watch" -> {
                    ratingarea.visibility = View.GONE
                    datearea.visibility = View.GONE
                    reviewarea.visibility = View.GONE
                }

                "Upcoming" -> {
                    ratingarea.visibility = View.GONE
                    datearea.visibility = View.VISIBLE
                    reviewarea.visibility = View.GONE

                }
            }
        }
        lifecycleScope.launch {
            try {
                movie = RetrofitClient.api.getResult(
                    BuildConfig.OMDB_API_KEY,
                    id
                )
                val response = movie!!

                // Fill sheet information here
                formtitle.text = response.Title

                Glide.with(this@details)
                    .load(response.Poster)
                    .into(posterurl)

            } catch (e: Exception) {
                val errormsg=findViewById<LinearLayout>(R.id.error)
                errormsg.visibility=View.VISIBLE
                val goback=findViewById<Button>(R.id.goback)
                val retry=findViewById<Button>(R.id.retry)

                retry.setOnClickListener {
                    lifecycleScope.launch{
                        searchMoviedetail(id)
                    }
                }
                goback.setOnClickListener {
                    finish()
                }
            }
        }
        //drop down finished
        val addmovie = sheetview.findViewById<Button>(R.id.Addmovie)
        addmovie.setOnClickListener {

            if (dropdown.text.isNullOrBlank()) {
                Toast.makeText(this, "Unable to load movie", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }

            val response = movie ?: return@setOnClickListener

            val show = Show(
                externalId = response.imdbID ?: return@setOnClickListener,
                title = response.Title ?: return@setOnClickListener,
                posterurl = response.Poster ?: "",
                status = dropdown.text.toString(),
                myrating = showrating.rating,
                year= response.Year ?: "",
                date = showdate.text.toString().ifBlank { null },
                notes = review.text?.toString()?.ifBlank { null }
            )

            lifecycleScope.launch {
                val app = application as Application
                app.database.showDao().insertshow(show)

                val shows = app.database.showDao().getAllShows()
                dialog.dismiss()

                closebtn.setOnClickListener {
                    dialog.dismiss()
                }
                Toast.makeText(this@details, "Added to Watchlist", Toast.LENGTH_SHORT).show()
            }
        }
    }
}