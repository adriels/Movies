package com.adr.movies.data.entity

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class GenreList(
    val genres: ArrayList<Genre>
) : Parcelable

@Parcelize
data class Genre(
    val id: Int,
    val name: String
) : Parcelable