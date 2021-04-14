package com.adr.movies.data.entity

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class SearchList(
    val results: ArrayList<MovieTv> = arrayListOf()
) : Parcelable