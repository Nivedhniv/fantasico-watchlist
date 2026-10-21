package com.example.dowatch

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.android.material.color.MaterialColors
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withTimeout
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class Search : AppCompatActivity() {

    private lateinit var suggestions: MaterialCardView
    private lateinit var searchTxt: SearchView
    private lateinit var searchEditText: EditText
    private lateinit var suggestionContainer: LinearLayout
    private lateinit var loading: FrameLayout
    private lateinit var resultArea: ScrollView
    private lateinit var resultsContainer: GridLayout
    private lateinit var mainView: View

    private var suggestionJob: Job? = null
    private var searchJob: Job? = null

    private var latestSuggestionQuery = ""
    private var latestSearchQuery = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_search)

        mainView = findViewById(R.id.main)

        ViewCompat.setOnApplyWindowInsetsListener(mainView) { view, insets ->
            val bars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars()
            )
            view.setPadding(
                bars.left,
                bars.top,
                bars.right,
                bars.bottom
            )
            insets
        }

        suggestions = findViewById(R.id.searchSuggestions)
        searchTxt = findViewById(R.id.Search)
        suggestionContainer = findViewById(R.id.suggestionContainer)
        loading = findViewById(R.id.loading)
        resultArea = findViewById(R.id.resultarea)
        resultsContainer = findViewById(R.id.result)
        val backButton = findViewById<ImageButton>(R.id.Back)

        searchEditText = searchTxt.findViewById(
            androidx.appcompat.R.id.search_src_text
        )

        searchEditText.background = null
        searchEditText.setPadding(0, 0, 0, 0)

        val searchIcon = searchTxt.findViewById<ImageView>(
            androidx.appcompat.R.id.search_mag_icon
        )
        val closeIcon = searchTxt.findViewById<ImageView>(
            androidx.appcompat.R.id.search_close_btn
        )

        val textColor = MaterialColors.getColor(
            searchTxt,
            com.google.android.material.R.attr.colorOnSurface
        )

        searchIcon.setColorFilter(textColor)
        closeIcon.setColorFilter(textColor)
        searchEditText.setTextColor(textColor)

        suggestions.visibility = View.GONE
        loading.visibility = View.GONE
        resultArea.visibility = View.GONE

        backButton.setOnClickListener {
            finish()
        }

        searchTxt.setOnQueryTextListener(
            object : SearchView.OnQueryTextListener {

                override fun onQueryTextSubmit(query: String?): Boolean {
                    val cleanQuery = query?.trim().orEmpty()

                    if (cleanQuery.isBlank()) {
                        showMessage("Enter a title to search.")
                        return true
                    }

                    suggestionJob?.cancel()
                    suggestions.visibility = View.GONE
                    hideKeyboard()
                    searchEditText.clearFocus()

                    searchMovies(cleanQuery)
                    return true
                }

                override fun onQueryTextChange(
                    newText: String?
                ): Boolean {
                    val query = newText?.trim().orEmpty()

                    if (query.length < MIN_QUERY_LENGTH) {
                        suggestionJob?.cancel()
                        latestSuggestionQuery = query
                        suggestionContainer.removeAllViews()
                        suggestions.visibility = View.GONE
                        return true
                    }

                    loadSuggestions(query)
                    return true
                }
            }
        )
    }

    private fun loadSuggestions(query: String) {
        suggestionJob?.cancel()
        latestSuggestionQuery = query

        suggestionJob = lifecycleScope.launch {
            // Avoid sending a request for every keystroke.
            delay(SUGGESTION_DEBOUNCE_MS)

            if (query != latestSuggestionQuery) return@launch

            suggestions.visibility = View.VISIBLE
            suggestionContainer.removeAllViews()

            try {
                val response = TmdbRetrofitClient.api.searchMovies(query)

                // Ignore responses belonging to older queries.
                if (query != latestSuggestionQuery) return@launch

                val results = response.results.orEmpty()
                    .filter { !it.title.isNullOrBlank() || !it.name.isNullOrBlank() }

                if (results.isEmpty()) {
                    suggestions.visibility = View.GONE
                    return@launch
                }

                results.forEach { result ->
                    val title = result.title
                        ?: result.name
                        ?: return@forEach

                    val suggestion = layoutInflater.inflate(
                        R.layout.sugestion,
                        suggestionContainer,
                        false
                    )

                    suggestion.findViewById<TextView>(
                        R.id.Name
                    ).text = title

                    suggestion.setOnClickListener {
                        searchEditText.setText(title)
                        suggestions.visibility = View.GONE
                        suggestionJob?.cancel()
                        hideKeyboard()
                        searchEditText.clearFocus()
                        searchMovies(title)
                    }

                    suggestionContainer.addView(suggestion)
                }

                if (suggestionContainer.childCount == 0) {
                    suggestions.visibility = View.GONE
                }

            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(
                    TAG_SUGGESTIONS,
                    "Suggestion search failed for query: $query",
                    e
                )

                if (query == latestSuggestionQuery) {
                    suggestionContainer.removeAllViews()
                    suggestions.visibility = View.GONE
                    showConnectionMessage(e)
                }
            }
        }
    }

    private fun searchMovies(query: String) {
        val cleanQuery = query.trim()

        if (cleanQuery.isBlank()) {
            showMessage("Enter a title to search.")
            return
        }

        searchJob?.cancel()
        suggestionJob?.cancel()

        latestSearchQuery = cleanQuery
        suggestions.visibility = View.GONE
        resultArea.visibility = View.GONE
        resultsContainer.removeAllViews()
        loading.visibility = View.VISIBLE

        searchJob = lifecycleScope.launch {
            try {
                /*
                 * Each endpoint is handled independently.
                 * If one fails, the other can still provide results.
                 */
                // supervisorScope prevents one failed endpoint from cancelling
                // the parent search coroutine and crashing the Activity.
                val (movieResponse, tvResponse) = supervisorScope {
                    val movieTask = async {
                        TmdbRetrofitClient.api.searchMovies(cleanQuery)
                    }

                    val tvTask = async {
                        TmdbRetrofitClient.api.searchTvShows(cleanQuery)
                    }

                    val movies = try {
                        movieTask.await()
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        Log.e(TAG_SEARCH, "Movie search failed for: $cleanQuery", e)
                        null
                    }

                    val tvShows = try {
                        tvTask.await()
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        Log.e(TAG_SEARCH, "TV search failed for: $cleanQuery", e)
                        null
                    }

                    movies to tvShows
                }

                if (cleanQuery != latestSearchQuery) return@launch

                if (movieResponse == null && tvResponse == null) {
                    showMessage(
                        "Couldn't connect to TMDb. Check your internet connection and try again."
                    )
                    return@launch
                }

                val combinedResults = buildList {
                    movieResponse?.results.orEmpty().forEach {
                        add(it to "movie")
                    }
                    tvResponse?.results.orEmpty().forEach {
                        add(it to "tv")
                    }
                }

                resultsContainer.removeAllViews()

                combinedResults.forEach { (result, mediaType) ->
                    val title = result.title
                        ?: result.name
                        ?: return@forEach

                    val movieCard = layoutInflater.inflate(
                        R.layout.result,
                        resultsContainer,
                        false
                    )

                    val poster = movieCard.findViewById<ImageView>(
                        R.id.poster
                    )
                    val titleView = movieCard.findViewById<TextView>(
                        R.id.title
                    )

                    titleView.text = title

                    val posterUrl = result.poster_path?.let {
                        "https://image.tmdb.org/t/p/w500$it"
                    }

                    Glide.with(this@Search)
                        .load(posterUrl)
                        .into(poster)

                    resultsContainer.addView(movieCard)

                    movieCard.setOnClickListener {
                        lifecycleScope.launch {
                            openDetails(
                                result.id,
                                mediaType,
                                posterUrl
                            )
                        }
                    }
                }

                if (resultsContainer.childCount == 0) {
                    showMessage("No matching titles found.")
                } else {
                    resultArea.visibility = View.VISIBLE

                    if (movieResponse == null || tvResponse == null) {
                        showMessage(
                            "Some results couldn't be loaded. Showing available titles."
                        )
                    }
                }

            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(
                    TAG_SEARCH,
                    "Search failed for query: $cleanQuery",
                    e
                )
                showConnectionMessage(e)
            } finally {
                // A cancelled older search must not hide a newer search's loader.
                if (cleanQuery == latestSearchQuery) {
                    loading.visibility = View.GONE
                }
            }
        }
    }

    private suspend fun openDetails(
        externalId: Int,
        mediaType: String,
        posterUrl: String?
    ) {
        try {
            val app = application as Application
            val externalKey = "${mediaType}_$externalId"

            val savedShow = app.database.showDao()
                .getShowByExternalId(externalKey)

            val target = if (savedShow != null) {
                usr_details::class.java
            } else {
                details::class.java
            }

            val intent = Intent(this@Search, target).apply {
                putExtra("id", externalId.toString())
                putExtra("mediaType", mediaType)
                putExtra("poster", posterUrl)

                if (savedShow != null) {
                    putExtra("SHOW_ID", savedShow.id)
                }
            }

            startActivity(intent)

        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG_SEARCH, "Couldn't open title details", e)
            showMessage("Couldn't open this title. Please try again.")
        }
    }

    private fun showConnectionMessage(error: Throwable) {
        val message = when (error) {
            is SocketTimeoutException ->
                "TMDb took too long to respond. Check your connection and retry."

            is UnknownHostException ->
                "Couldn't find TMDb. Check your internet or DNS settings."

            is HttpException -> when (error.code()) {
                401 -> "TMDb rejected the API credentials."
                403 -> "TMDb access was denied."
                429 -> "Too many requests. Please wait and retry."
                in 500..599 -> "TMDb is temporarily unavailable."
                else -> "TMDb returned an error (${error.code()})."
            }

            is IOException ->
                "Network connection failed. Check your internet and retry."

            else ->
                "Something went wrong. Please try again."
        }

        if (!isFinishing && !isDestroyed) {
            Snackbar.make(mainView, message, Snackbar.LENGTH_LONG)
                .setAction("Retry") {
                    val query = searchTxt.query?.toString()?.trim().orEmpty()
                        .ifBlank { latestSearchQuery }
                    if (query.isNotBlank()) {
                        searchMovies(query)
                    }
                }
                .show()
        }
    }

    private fun showMessage(message: String) {
        if (!isFinishing && !isDestroyed) {
            Snackbar.make(
                mainView,
                message,
                Snackbar.LENGTH_LONG
            ).show()
        }
    }

    private fun hideKeyboard() {
        val imm = getSystemService(
            Context.INPUT_METHOD_SERVICE
        ) as InputMethodManager

        imm.hideSoftInputFromWindow(searchEditText.windowToken, 0)
    }

    override fun onDestroy() {
        suggestionJob?.cancel()
        searchJob?.cancel()
        super.onDestroy()
    }

    companion object {
        private const val MIN_QUERY_LENGTH = 3
        private const val SUGGESTION_DEBOUNCE_MS = 400L

        private const val TAG_SEARCH = "SEARCH_ERROR"
        private const val TAG_SUGGESTIONS = "SUGGESTION_ERROR"
    }
}
