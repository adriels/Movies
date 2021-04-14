package com.adr.movies.data

import android.content.Context
import com.adr.movies.data.entity.GenreList
import com.adr.movies.data.entity.MovieTv
import com.adr.movies.data.entity.MovieTvList
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class AppPreference(context: Context) {

    private var prefs = context.getSharedPreferences("MOVIES_PREFERENCE", Context.MODE_PRIVATE)

    companion object {
        const val SUBSCRIPTIONS = "SUBSCRIPTIONS"
        const val MOVIE_GENRES = "MOVIE_GENRES"
        const val TV_GENRES = "TV_GENRES"
    }

    fun setSubscriptions(movieTvList: List<MovieTv>) {
        prefs.edit().putString(SUBSCRIPTIONS, Gson().toJson(movieTvList)).apply()
    }

    fun getSubscriptions() =
        Gson().fromJson<ArrayList<MovieTv>>(
            prefs.getString(SUBSCRIPTIONS, ""), object : TypeToken<ArrayList<MovieTv>>() {}.type
        )

    fun setGenres(genreList: Pair<GenreList, GenreList>) {
        prefs.edit().putString(MOVIE_GENRES, Gson().toJson(genreList.first)).apply()
        prefs.edit().putString(TV_GENRES, Gson().toJson(genreList.second)).apply()
    }

    fun getMovieGenres() = Gson().fromJson(prefs.getString(MOVIE_GENRES, ""), GenreList::class.java)

    fun getTvGenres() = Gson().fromJson(prefs.getString(TV_GENRES, ""), GenreList::class.java)
}