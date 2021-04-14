package com.adr.movies.data.remote

import android.content.Context
import com.adr.movies.R
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers

object ApiClientProxy {
    fun getMoviesTvList(context: Context, listId: Int) =
        ApiClient.getServiceClient().getList(listId, context.getString(R.string.movies_api_key))
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())

    fun getMoviesGenres(context: Context) =
        ApiClient.getServiceClient().getMoviesGenres(context.getString(R.string.movies_api_key))
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())

    fun getTvGenres(context: Context) =
        ApiClient.getServiceClient().getTvGenres(context.getString(R.string.movies_api_key))
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
}