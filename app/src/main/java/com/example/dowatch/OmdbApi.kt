package com.example.dowatch

import retrofit2.http.GET
import retrofit2.http.Query

interface OmdbApi {
    @GET("/")
    suspend fun searchMovies(
        @Query("apikey") apiKey: String,
        @Query("s") query: String
    ): OmdbResponse

    @GET("/")
    suspend fun getResult(
        @Query("apikey") apiKey: String,
        @Query("i") id: String
    ): OmdbResult
}