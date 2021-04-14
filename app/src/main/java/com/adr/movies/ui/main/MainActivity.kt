package com.adr.movies.ui.main

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.view.View
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.adr.movies.R
import com.adr.movies.data.AppPreference
import com.adr.movies.data.entity.MovieTv
import com.adr.movies.data.entity.MovieTvList
import com.adr.movies.data.remote.ApiErrorUtil
import com.adr.movies.ui.detail.DetailActivity
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: MainViewModel
    private var completeList: ArrayList<MovieTv>? = arrayListOf()
    private var isLoading = true
    private var newList = true
    private var selectedMovieTv: MovieTv? = null

    companion object {
        const val DETAIL_REQUEST_CODE = 123
        //const val MOVIETV_SUBSCRIPTION = "MOVIETV_SUBSCRIPTION"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setUpSearchAndCancel()
        initViewModel()
        viewModel.getGenresLists(this)
    }

    private fun setUpSearchAndCancel() {
        searchButton.setOnQueryTextFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                searchRecyclerView.visibility = View.VISIBLE
                searchCancelText.visibility = View.VISIBLE
            } else {
                searchRecyclerView.visibility = View.GONE
                searchCancelText.visibility = View.GONE
            }
        }
        searchButton.setOnCloseListener {
            searchButton.setQuery("", false)
            true
        }
        searchCancelText.setOnClickListener {
            searchButton.onActionViewCollapsed()
        }
        searchButton.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                with((searchRecyclerView.adapter as SearchRecyclerViewAdapter)) {
                    if (!newText.isNullOrBlank()) {
                        /*movieTvList =
                            completeList?.filter { it.original_title!!.contains(newText, true) }
                        notifyDataSetChanged()*/
                        loadMoreItems(true)
                        viewModel.searchMoviesTv(this@MainActivity, newText)
                    } else {
                        loadMoreItems(false)
                        viewModel.dispose()
                        movieTvList = listOf()
                        notifyDataSetChanged()
                    }
                }
                return true
            }
        })
        searchRecyclerView.adapter = SearchRecyclerViewAdapter(arrayListOf()) {
            selectedMovieTv = it
            startActivityForResult(
                Intent(this, DetailActivity::class.java)
                    .putExtra(DetailActivity.MOVIETV, it), DETAIL_REQUEST_CODE
            )
        }
    }

    private fun initViewModel() {
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        viewModel.listLiveData.observe(this, { setUpRecycler(it) })
        viewModel.genreListLiveData.observe(this, {
            AppPreference(this).setGenres(it)
            viewModel.getMovieTvList(this)
        })
        viewModel.searchListLiveData.observe(this, {
            loadMoreItems(false)
            with(searchRecyclerView.adapter as SearchRecyclerViewAdapter) {
                movieTvList =
                    it.results.filter { it.media_type == DetailActivity.MOVIE || it.media_type == DetailActivity.TV }
                notifyDataSetChanged()
            }
        })
        viewModel.errorLiveData.observe(this, {
            circularProgressBar.visibility = View.GONE
            Snackbar.make(
                mainRecyclerView,
                ApiErrorUtil.getErrorMessage(it, this),
                Snackbar.LENGTH_LONG
            )
                .setAction(getString(R.string.retry)) {
                    circularProgressBar.visibility = View.VISIBLE
                    viewModel.getGenresLists(this)
                }
                .setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE)
                .show()
        })
    }

    private fun setUpRecycler(movieTvList: MovieTvList?) {
        swipeRefresh.isRefreshing = false
        circularProgressBar.visibility = View.GONE
        loadMoreItems(false)
        movieTvList?.let {
            completeList = it.items
            nothingToShowHere.visibility = View.GONE
            allMoviesTvContainer.visibility = View.VISIBLE
            if (mainRecyclerView.adapter == null) {
                mainRecyclerView.adapter = MainRecyclerViewAdapter(it) { movieTvDetail ->
                    selectedMovieTv = movieTvDetail
                    startActivityForResult(
                        Intent(this, DetailActivity::class.java)
                            .putExtra(DetailActivity.MOVIETV, movieTvDetail), DETAIL_REQUEST_CODE
                    )
                }
                mainRecyclerView.addItemDecoration(object : RecyclerView.ItemDecoration() {
                    override fun getItemOffsets(
                        outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State
                    ) {
                        with(outRect) {
                            bottom = 60
                        }
                    }
                })
            } else if (it.items != (mainRecyclerView.adapter as MainRecyclerViewAdapter).list.items) {
                with((mainRecyclerView.adapter as MainRecyclerViewAdapter)) {
                    val oldSize = list.items?.size!!
                    val newSize = it.items?.size!!
                    list = it
                    if (!newList)
                        notifyItemRangeInserted(oldSize - 1, newSize - oldSize)
                    else {
                        notifyItemRangeRemoved(newSize - 1, oldSize - newSize)
                        notifyItemRangeChanged(0, newSize)
                    }
                    newList = false
                }
            }
            (mainRecyclerView.adapter as MainRecyclerViewAdapter).notifyDataSetChanged()
        }
        if (movieTvList == null || movieTvList.items.isNullOrEmpty()) {
            allMoviesTvContainer.visibility = View.GONE
            nothingToShowHere.visibility = View.VISIBLE
        }
        if (!AppPreference(this).getSubscriptions().isNullOrEmpty()) {
            val subscriptionsList = AppPreference(this).getSubscriptions()
            subscribedMoviesTvContainer.visibility = View.VISIBLE
            if (subscribedRecyclerView.adapter == null) {
                subscribedRecyclerView.adapter = MainRecyclerViewAdapter(
                    MovieTvList(null, subscriptionsList), true
                ) { movieTvDetail ->
                    selectedMovieTv = movieTvDetail
                    startActivityForResult(
                        Intent(this, DetailActivity::class.java)
                            .putExtra(DetailActivity.MOVIETV, movieTvDetail), DETAIL_REQUEST_CODE
                    )
                }
                subscribedRecyclerView.addItemDecoration(object : RecyclerView.ItemDecoration() {
                    override fun getItemOffsets(
                        outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State
                    ) {
                        with(outRect) {
                            right = 40
                        }
                    }
                })
            } else if (subscriptionsList != (subscribedRecyclerView.adapter as MainRecyclerViewAdapter).list.items) {
                with((subscribedRecyclerView.adapter as MainRecyclerViewAdapter)) {
                    list = MovieTvList(null, subscriptionsList)
                    notifyDataSetChanged()
                }
            }
        } else
            subscribedMoviesTvContainer.visibility = View.GONE

        /*mainRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val layoutManager = LinearLayoutManager::class.java.cast(recyclerView.layoutManager)
                val lastItemVisible = layoutManager?.findLastVisibleItemPosition()
                val endHasBeenReached = lastItemVisible == -1

                if (endHasBeenReached && !isLoading) {
                    loadMoreItems(true)
                    viewModel.getMovieTvList(this@MainActivity)
                }
            }
        })*/

        scrollView.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { v, _, scrollY, _, oldScrollY ->
            if (v.getChildAt(v.childCount - 1) != null) {
                if ((scrollY >= (v.getChildAt(v.childCount - 1).measuredHeight - v.measuredHeight))
                    && scrollY > oldScrollY
                ) {
                    if (!isLoading) {
                        loadMoreItems(true)
                        viewModel.getMovieTvList(this)
                    }
                }
            }
        })

        swipeRefresh.setOnRefreshListener {
            newList = true
            viewModel.restartList()
            viewModel.getMovieTvList(this)
        }
    }

    private fun loadMoreItems(loading: Boolean) {
        isLoading = loading
        linearProgressBar.visibility = if (loading) View.VISIBLE else View.GONE
        //swipeRefresh.isEnabled = !loading
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                DETAIL_REQUEST_CODE -> {
                    /*val subscribed = data?.getBooleanExtra(MOVIETV_SUBSCRIPTION, false)
                    val subscribedList = AppPreference(this).getSubscriptions()
                    val index = subscribedList.indexOfFirst { it.id == selectedMovieTv?.id }
                    if (subscribed!!) {
                        if (index == -1) {
                            subscribedList.add(selectedMovieTv!!)
                            (subscribedRecyclerView.adapter as MainRecyclerViewAdapter)
                                .list = MovieTvList(null, subscribedList)
                            /*(subscribedRecyclerView.adapter as MainRecyclerViewAdapter)
                                .notifyItemInserted(subscribedList.size - 1)*/
                        }
                    } else {
                        if (index >= 0) {
                            subscribedList.remove(selectedMovieTv)
                            (subscribedRecyclerView.adapter as MainRecyclerViewAdapter)
                                .list = MovieTvList(null, subscribedList)
                            /*(subscribedRecyclerView.adapter as MainRecyclerViewAdapter)
                                .notifyItemRemoved(index)*/
                        }
                    }*/
                    val subscribedList = AppPreference(this).getSubscriptions() ?: arrayListOf()
                    if (subscribedList.isNullOrEmpty())
                        subscribedMoviesTvContainer.visibility = View.GONE
                    else {
                        with((subscribedRecyclerView.adapter as MainRecyclerViewAdapter)) {
                            if (list.items != subscribedList) {
                                list = MovieTvList(null, subscribedList)
                                notifyDataSetChanged()
                                subscribedMoviesTvContainer.visibility = View.VISIBLE
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onBackPressed() {
        if (searchButton.hasFocus())
            searchButton.onActionViewCollapsed()
        else
            super.onBackPressed()
    }
}