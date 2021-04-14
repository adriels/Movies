package com.adr.movies.data.entity

import android.os.Parcel
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class MovieTvList(
    var id: Int? = null,
    val items: ArrayList<MovieTv>? = null
) : Parcelable

@Parcelize
data class MovieTv(
    val poster_path: String?,
    val genre_ids: List<Int> = emptyList(),
    val id: Int,
    val original_title: String?,
    val overview: String?,
    val release_date: String?,
    val media_type: String?
) : Parcelable
