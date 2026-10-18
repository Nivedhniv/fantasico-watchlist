
package com.example.dowatch
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import kotlin.jvm.java
import retrofit2.converter.gson.GsonConverterFactory

object TmdbRetrofitClient {

    private val client = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader(
                    "Authorization",
                    "Bearer ${BuildConfig.TMDB_READ_ACCESS_TOKEN}"
                )
                .addHeader("accept", "application/json")
                .build()

            chain.proceed(request)
        }
        .build()

    val retrofit = Retrofit.Builder()
        .baseUrl("https://api.themoviedb.org/3/")
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val api = retrofit.create(TmdbApi::class.java)
}
