package com.adr.movies.data.remote

import android.util.Log
import com.adr.movies.BuildConfig
import com.adr.movies.data.entity.GenreList
import com.adr.movies.data.entity.MovieTvList
import hu.akarnokd.rxjava3.retrofit.RxJava3CallAdapterFactory
import io.reactivex.rxjava3.core.Observable
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

object ApiClient {
    private const val API_URL = "https://api.themoviedb.org/3/"

    private var mInterface: AppService

    init {
        val loggingInterceptor = HttpLoggingInterceptor(object : HttpLoggingInterceptor.Logger {
            override fun log(message: String) {
                Log.i("ApiClient", message)
            }
        })
        loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY

        val builder = OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)

        if (BuildConfig.DEBUG)
            builder.addInterceptor(loggingInterceptor)

        val client = builder.build()

        val restAdapter = Retrofit.Builder()
                .baseUrl(API_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                .client(client)
                .build()

        mInterface = restAdapter.create(AppService::class.java)
    }

    fun getServiceClient() = mInterface

    interface AppService {

        @GET("list/{list_id}")
        fun getList(
                @Path("list_id") listId: Int,
                @Query("api_key") apiKey: String
        ): Observable<MovieTvList>

        @GET("genre/movie/list")
        fun getMoviesGenres(
                @Query("api_key") apiKey: String
        ): Observable<GenreList>

        @GET("genre/tv/list")
        fun getTvGenres(
                @Query("api_key") apiKey: String
        ): Observable<GenreList>
    }
}