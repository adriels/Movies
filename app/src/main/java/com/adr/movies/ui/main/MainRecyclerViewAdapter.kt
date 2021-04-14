package com.adr.movies.ui.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.adr.movies.R
import com.adr.movies.data.AppPreference
import com.adr.movies.data.entity.MovieTv
import com.adr.movies.data.entity.MovieTvList
import com.adr.movies.ui.detail.DetailActivity.Companion.MOVIE
import com.adr.movies.ui.detail.DetailActivity.Companion.TV
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.item_main_moviestv.view.*
import kotlinx.android.synthetic.main.item_subscribed_moviestv.view.*

class MainRecyclerViewAdapter(
    var list: MovieTvList,
    private val subscribed: Boolean = false,
    val onClickMovieTv: ((MovieTv) -> Unit)
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val MAIN_CARDS = 1
        const val SUBSCRIBED_CARDS = 2
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        when (viewType) {
            MAIN_CARDS -> ViewHolderMoviesTv(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_main_moviestv, parent, false)
            )
            SUBSCRIBED_CARDS -> ViewHolderSubscribedMoviesTv(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_subscribed_moviestv, parent, false)
            )
            else -> throw IllegalArgumentException("Illegal viewType")
        }

    override fun getItemViewType(position: Int): Int =
        if (subscribed)
            SUBSCRIBED_CARDS
        else
            MAIN_CARDS

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            MAIN_CARDS -> (holder as ViewHolderMoviesTv).bind(list.items!![position])
            SUBSCRIBED_CARDS -> (holder as ViewHolderSubscribedMoviesTv).bind(list.items!![position])
        }
    }

    override fun getItemCount(): Int = list.items?.size!!

    inner class ViewHolderMoviesTv(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(data: MovieTv) {
            with(itemView) {
                Glide.with(this)
                    .load("https://image.tmdb.org/t/p/w500/${data.poster_path}")
                    .placeholder(R.drawable.ic_no_image)
                    .into(movieTvPoster)
                movieTvPoster.clipToOutline = true

                movieTvTitle.text = data.original_title

                when (data.media_type) {
                    MOVIE -> AppPreference(context).getMovieGenres().genres.forEach {
                        if (data.genre_ids[0] == it.id) {
                            movieTvGenre.text = it.name
                            return@forEach
                        }
                    }
                    TV -> AppPreference(context).getTvGenres().genres.forEach {
                        if (data.genre_ids[0] == it.id) {
                            movieTvGenre.text = it.name
                            return@forEach
                        }
                    }
                    else -> movieTvGenre.text = resources.getString(R.string.no_genre)
                }

                setOnClickListener { onClickMovieTv(data) }
            }
        }
    }

    inner class ViewHolderSubscribedMoviesTv(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(data: MovieTv) {
            with(itemView) {
                Glide.with(this)
                    .load("https://image.tmdb.org/t/p/w500/${data.poster_path}")
                    .placeholder(R.drawable.ic_no_image)
                    .into(subscribedMovieTv)
                subscribedMovieTv.clipToOutline = true

                setOnClickListener { onClickMovieTv(data) }
            }
        }
    }
}