package com.adr.movies.ui.main

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.adr.movies.R
import com.adr.movies.data.AppPreference
import com.adr.movies.data.entity.MovieTv
import com.adr.movies.ui.detail.DetailActivity.Companion.MOVIE
import com.adr.movies.ui.detail.DetailActivity.Companion.TV
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import kotlinx.android.synthetic.main.item_searched_moviestv.view.*

class SearchRecyclerViewAdapter(
    var movieTvList: List<MovieTv>?,
    val onClickMovieTv: (MovieTv) -> Unit
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        SearchedViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_searched_moviestv, parent, false)
        )

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (!movieTvList.isNullOrEmpty())
            (holder as SearchedViewHolder).bind(movieTvList!![position])
    }

    override fun getItemCount(): Int = movieTvList?.size ?: 0

    inner class SearchedViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(data: MovieTv) {
            with(itemView) {
                var subscribed = checkButton(context, data)

                Glide.with(context)
                    .load("https://image.tmdb.org/t/p/w500/${data.poster_path}")
                    .into(movieTvSearchPoster)

                movieTvSearchTitle.text = data.original_title?: context.getString(R.string.no_title)

                when (data.media_type) {
                    MOVIE -> AppPreference(context).getMovieGenres().genres.forEach {
                        if (!data.genre_ids.isNullOrEmpty()) {
                            if (data.genre_ids[0] == it.id) {
                                movieTvSearchGenre.text = it.name
                                return@forEach
                            }
                        } else {
                            movieTvSearchGenre.text = context.getString(R.string.no_genre)
                            return@forEach
                        }
                    }
                    TV -> AppPreference(context).getTvGenres().genres.forEach {
                        if (!data.genre_ids.isNullOrEmpty()) {
                            if (data.genre_ids[0] == it.id) {
                                movieTvSearchGenre.text = it.name
                                return@forEach
                            }
                        } else {
                            movieTvSearchGenre.text = context.getString(R.string.no_genre)
                            return@forEach
                        }
                    }
                    else -> movieTvSearchGenre.text = context.getString(R.string.no_genre)
                }

                subscribeSearchButton.setOnClickListener {
                    val list = AppPreference(context).getSubscriptions() ?: arrayListOf()
                    if (subscribed)
                        list.remove(data)
                    else
                        list.add(data)
                    AppPreference(context).setSubscriptions(list)

                    subscribed = checkButton(context, data, list)
                }

                setOnClickListener { onClickMovieTv(data) }
            }
        }

        private fun checkButton(
            context: Context,
            data: MovieTv,
            list: ArrayList<MovieTv>? = arrayListOf()
        ): Boolean {
            val founded = list?.indexOfFirst { filteredMovieTv -> filteredMovieTv.id == data.id }
            val subscribed = founded!! >= 0
            with(itemView.subscribeSearchButton as MaterialButton) {
                if (subscribed) {
                    strokeWidth = 0
                    background.setTint(ContextCompat.getColor(context, R.color.white_opacity_40))
                    text = context.getString(R.string.subscribed)
                    setTextColor(ContextCompat.getColor(context, R.color.black_two))
                } else {
                    strokeWidth = 1
                    background.setTint(
                        ContextCompat.getColor(context, android.R.color.transparent)
                    )
                    text = context.getString(R.string.subscribe)
                    setTextColor(ContextCompat.getColor(context, R.color.white_opacity_40))
                }
            }
            return subscribed
        }
    }
}