
package com.example.dowatch
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TmdbApi {

    @GET("search/movie")
    suspend fun searchMovies(
        @Query("query") query: String
    ): TmdbSearchResponse

    @GET("search/tv")
    suspend fun searchTvShows(
        @Query("query") query: String
    ): TmdbSearchResponse

    @GET("movie/{id}")
    suspend fun getMovieDetails(
        @Path("id") id: Int
    ): TmdbDetailsResponse

    @GET("tv/{id}")
    suspend fun getTvDetails(
        @Path("id") id: Int
    ): TmdbDetailsResponse

    @GET("movie/{id}/credits")
    suspend fun getMovieCredits(
        @Path("id") id: Int
    ): TmdbCreditsResponse

    @GET("tv/{id}/credits")
    suspend fun getTvCredits(
        @Path("id") id: Int
    ): TmdbCreditsResponse
}
