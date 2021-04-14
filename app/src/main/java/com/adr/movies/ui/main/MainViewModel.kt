package com.adr.movies.ui.main

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.adr.movies.data.entity.GenreList
import com.adr.movies.data.entity.MovieTvList
import com.adr.movies.data.remote.ApiClientProxy
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable

class MainViewModel : ViewModel() {

    val listLiveData = MutableLiveData<MovieTvList>()
    val genreListLiveData = MutableLiveData<Pair<GenreList, GenreList>>()
    val errorLiveData = MutableLiveData<Throwable>()

    private var completeList: MovieTvList? = null
    private var listId = 1

    private val compositeDisposable = CompositeDisposable()

    fun getMovieTvList(context: Context) {
        compositeDisposable.add(
            ApiClientProxy.getMoviesTvList(context, listId)
                .subscribe(
                    {
                        it?.let {
                            if (completeList != null) {
                                if (it.id!! > completeList?.id!!) {
                                    completeList!!.id = it.id
                                    completeList!!.items?.addAll(it.items!!)
                                }
                            } else
                                completeList = it

                            listLiveData.postValue(completeList!!)
                            listId++
                        }
                    },
                    {
                        errorLiveData.postValue(it)
                    })
        )
    }

    fun restartList() {
        completeList = null
        listId = 1
    }

    fun getGenresLists(context: Context) {
        compositeDisposable.add(
            Observable.zip(
                ApiClientProxy.getMoviesGenres(context),
                ApiClientProxy.getTvGenres(context),
                { movieGenreList: GenreList, tvGenreList: GenreList ->
                    Pair(movieGenreList, tvGenreList)
                }
            )
                .subscribe({ genreListLiveData.postValue(it) }, { errorLiveData.postValue(it) })
        )
    }

    override fun onCleared() {
        compositeDisposable.dispose()
        super.onCleared()
    }
}