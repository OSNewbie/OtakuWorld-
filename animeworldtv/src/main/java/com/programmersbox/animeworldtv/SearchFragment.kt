package com.programmersbox.animeworldtv

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.widget.Toast
import androidx.core.app.ActivityOptionsCompat
import androidx.fragment.app.Fragment
import androidx.leanback.app.SearchFragment
import androidx.leanback.app.SearchSupportFragment
import androidx.leanback.widget.*
import androidx.lifecycle.lifecycleScope
import com.programmersbox.anime_sources.Sources
import com.programmersbox.models.ItemModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * A simple [Fragment] subclass.
 * Use the [SearchFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SearchFragment : SearchSupportFragment(), SearchSupportFragment.SearchResultProvider {
    private val rowsAdapter = ArrayObjectAdapter(ListRowPresenter())
    private val handler = Handler()
    private val delayedLoad = SearchRunnable()
    private val searchList = mutableListOf<ItemModel>()

    inner class SearchRunnable : Runnable {

        private var searchQuery = ""

        fun setSearchQuery(query: String) {
            searchQuery = query
        }

        override fun run() {

            searchList.filter { it.title.contains(searchQuery, true) }
                .groupBy { it.title.firstOrNull().toString() }
                .entries.forEach {
                    val (t, u) = it
                    val listRowAdapter = ArrayObjectAdapter(CardPresenter())

                    listRowAdapter.addAll(0, u)

                    val header = HeaderItem(t.hashCode().toLong(), t)

                    rowsAdapter.add(ListRow(header, listRowAdapter))
                }
        }

    }

    override fun getResultsAdapter(): ObjectAdapter = rowsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSearchResultProvider(this)
        setOnItemViewClickedListener(ItemViewClickedListener())
        /*Sources.WCO_SUBBED
            .flatMapSingle {
                it.getList()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
            }*/
        /*Sources.WCO_SUBBED.getList()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy { searchList.addAll(it) }
            .addTo(disposable)*/
    }

    override fun onQueryTextChange(newQuery: String): Boolean {
        rowsAdapter.clear()
        if (!TextUtils.isEmpty(newQuery)) {
            delayedLoad.setSearchQuery(newQuery)
            handler.removeCallbacks(delayedLoad)
            handler.postDelayed(delayedLoad, SEARCH_DELAY_MS)
        }
        return true
    }

    override fun onQueryTextSubmit(query: String): Boolean {
        rowsAdapter.clear()
        if (!TextUtils.isEmpty(query)) {
            //delayedLoad.setSearchQuery(query)
            //handler.removeCallbacks(delayedLoad)
            //handler.postDelayed(delayedLoad, SEARCH_DELAY_MS)
            /*Sources.PUTLOCKERTV
            Sources.WCO_SUBBED*/

            /*Observable.combineLatest(
                Sources.PUTLOCKERTV
                    .searchList(query, list = searchList)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .onErrorReturnItem(emptyList())
                    .toObservable(),
                Sources.WCO_SUBBED
                    .searchList(query, list = searchList)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .onErrorReturnItem(emptyList())
                    .toObservable(),
                Sources.VIDSTREAMING
                    .searchList(query, list = searchList)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .onErrorReturnItem(emptyList())
                    .toObservable()
            ) { p, w, v -> listOf(p, w, v).flatten().sortedBy { it.title } }*/

            /*sourcePublish
                .flatMapSingle {
                    it.searchList(query, list = searchList)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .onErrorReturnItem(emptyList())
                }*/
            lifecycleScope.launch {
                combine(
                    Sources.searchSources
                        .filter { it.canPlay }
                        .map { a ->
                            a.searchSourceList(query, list = emptyList())
                            //.map { SearchModel(a.serviceName, it) }
                        }
                ) { it.toList().flatten().sortedBy(ItemModel::title) }
                    .debounce(SEARCH_DELAY_MS)
                    .map { d -> d.groupBy { it.source.serviceName } }
                    .flowOn(Dispatchers.Main)
                    .onEach {
                        it.forEach { item ->
                            val (t, u) = item
                            val listRowAdapter = ArrayObjectAdapter(CardPresenter())

                            listRowAdapter.addAll(0, u)

                            val header = HeaderItem(t.hashCode().toLong(), t)

                            rowsAdapter.add(ListRow(header, listRowAdapter))
                        }
                    }
                    .collect()
            }
        }
        return true
    }

    companion object {
        private const val SEARCH_DELAY_MS = 300L
    }

    private inner class ItemViewClickedListener : OnItemViewClickedListener {
        override fun onItemClicked(
            itemViewHolder: Presenter.ViewHolder,
            item: Any,
            rowViewHolder: RowPresenter.ViewHolder,
            row: Row
        ) {
            if (item is ItemModel) {
                val intent = Intent(context!!, DetailsActivity::class.java)
                intent.putExtra(DetailsActivity.MOVIE, item)

                val bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(
                    activity!!,
                    (itemViewHolder.view as CustomImageCardView).mainImageView,
                    DetailsActivity.SHARED_ELEMENT_NAME
                )
                    .toBundle()
                startActivity(intent, bundle)
            } else if (item is String) {
                if (item.contains(getString(R.string.error_fragment))) {
                    val intent = Intent(context!!, BrowseErrorActivity::class.java)
                    startActivity(intent)
                } else {
                    Toast.makeText(context!!, item, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}