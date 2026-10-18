
package com.example.dowatch

data class TmdbSearchResponse(
    val results: List<TmdbSearchResult>?
)

data class TmdbSearchResult(
    val id: Int,
    val title: String?,
    val name: String?,
    val overview: String?,
    val poster_path: String?,
    val vote_average: Double?,
    val release_date: String?,
    val first_air_date: String?,
    val genre_ids: List<Int>?
)

data class TmdbDetailsResponse(
    val id: Int,
    val title: String?,
    val name: String?,
    val overview: String?,
    val poster_path: String?,
    val backdrop_path: String?,
    val vote_average: Double?,
    val release_date: String?,
    val first_air_date: String?,
    val genres: List<TmdbGenre>?
)

data class TmdbGenre(
    val id: Int,
    val name: String
)
data class TmdbCreditsResponse(
    val cast: List<TmdbCastMember>?,
    val crew: List<TmdbCrewMember>?
)

data class TmdbCastMember(
    val id: Int,
    val name: String,
    val character: String?,
    val profile_path: String?
)

data class TmdbCrewMember(
    val id: Int,
    val name: String,
    val job: String?,
    val department: String?,
    val profile_path: String?
)