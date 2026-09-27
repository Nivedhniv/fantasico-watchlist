package com.example.dowatch

data class OmdbResponse(
    val Search: List<SearchResult>?,
    val totalres: String?,
    val response: String?)

data class SearchResult(
    val Title: String?,
    val Year: String?,
    val imdbID: String?,
    val Type: String?,
    val Poster: String?
)
data class OmdbResult(
    val Title: String?,
    val Year: String?,
    val imdbID: String?,
    val Type: String?,
    val Plot:String?,
    val imdbRating: String?,
    val Poster: String?,
    val Genre: String?
)