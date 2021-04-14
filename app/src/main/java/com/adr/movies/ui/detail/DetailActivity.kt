package com.adr.movies.ui.detail

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.updateLayoutParams
import androidx.palette.graphics.Palette
import com.adr.movies.R
import com.adr.movies.data.AppPreference
import com.adr.movies.data.entity.MovieTv
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.button.MaterialButton
import kotlinx.android.synthetic.main.activity_detail.*
import java.text.SimpleDateFormat
import java.util.*

class DetailActivity : AppCompatActivity() {

    companion object {
        const val MOVIETV = "MOVIETV"
        const val MOVIE = "movie"
        const val TV = "tv"
    }

    //private lateinit var viewModel: DetailViewModel
    private lateinit var movieTv: MovieTv
    private var subscribed = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        detailToolbar.setNavigationOnClickListener { onBackPressed() }

        movieTv = intent?.extras!![MOVIETV] as MovieTv

        setPaletteColors()

        Glide.with(this)
            .load("https://image.tmdb.org/t/p/w500/${movieTv.poster_path}")
            .placeholder(R.drawable.ic_no_image)
            .into(movieTvDetailPoster)
        movieTvDetailPoster.clipToOutline = true

        movieTvDetailTitle.text = movieTv.original_title?: getString(R.string.no_title)

        movieTvDetailDescription.text = movieTv.overview?: getString(R.string.no_description)

        movieTvDetailYear.text = formatDate()

        // Presenta inconsistencias todavía
        /*val posterOriginalWidth = movieTvDetailPoster.layoutParams.width
        appBar.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { _, verticalOffset ->
            movieTvDetailPoster.updateLayoutParams { width = posterOriginalWidth + verticalOffset }
        })*/
    }

    private fun setPaletteColors() {
        Glide.with(this)
            .asBitmap()
            .load("https://image.tmdb.org/t/p/w500/${movieTv.poster_path}")
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    Palette.Builder(resource).generate {
                        it?.let {
                            detailActivityParent.background.setTint(it.dominantSwatch?.rgb!!)
                            movieTvDetailPoster.background.setTint(it.dominantSwatch?.rgb!!)
                            movieTvDetailTitle.setTextColor(it.dominantSwatch?.bodyTextColor!!)
                            movieTvDetailYear.setTextColor(it.dominantSwatch?.titleTextColor!!)
                            tvOverview.setTextColor(it.dominantSwatch?.bodyTextColor!!)
                            movieTvDetailDescription.setTextColor(it.dominantSwatch?.titleTextColor!!)
                            setUpButton(it)
                        }
                    }
                }

                override fun onLoadCleared(placeholder: Drawable?) {}
            })
    }

    private fun setUpButton(palette: Palette) {
        checkButton(null, palette)

        subscribeButton.setOnClickListener {
            val list = AppPreference(this).getSubscriptions() ?: arrayListOf()
            if (subscribed)
                list.remove(movieTv)
            else
                list.add(movieTv)
            AppPreference(this).setSubscriptions(list)
            checkButton(list, palette)
        }
    }

    private fun checkButton(editedList: ArrayList<MovieTv>? = null, palette: Palette) {
        val list = editedList ?: AppPreference(this).getSubscriptions() ?: arrayListOf()
        val founded = list.indexOfFirst { filteredMovieTv -> filteredMovieTv.id == movieTv.id }
        subscribed = founded >= 0
        with(subscribeButton as MaterialButton) {
            if (subscribed) {
                strokeWidth = 0
                background.setTint(ContextCompat.getColor(this@DetailActivity, R.color.white))
                text = getString(R.string.subscribed)
                setTextColor(palette.dominantSwatch?.rgb!!)
            } else {
                strokeWidth = 1
                background.setTint(
                    ContextCompat.getColor(this@DetailActivity, android.R.color.transparent)
                )
                text = getString(R.string.subscribe)
                setTextColor(ContextCompat.getColor(this@DetailActivity, R.color.white))
            }
        }
    }

    private fun formatDate(): String {
        return try {
            val date =
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(movieTv.release_date!!)
            val calendar = Calendar.getInstance(Locale.getDefault())
            calendar.time = date!!
            calendar.get(Calendar.YEAR).toString()
        } catch (e: Exception) {
            e.printStackTrace()
            movieTv.release_date?: getString(R.string.no_date)
        }
    }

    override fun onBackPressed() {
        //setResult(RESULT_OK, Intent().putExtra(MainActivity.MOVIETV_SUBSCRIPTION, subscribed))
        setResult(RESULT_OK)
        super.onBackPressed()
    }
}