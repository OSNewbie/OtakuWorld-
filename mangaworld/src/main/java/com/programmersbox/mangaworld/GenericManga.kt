package com.programmersbox.mangaworld

import android.Manifest
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.os.Environment
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.preference.SwitchPreference
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.programmersbox.gsonutils.toJson
import com.programmersbox.helpfulutils.downloadManager
import com.programmersbox.helpfulutils.requestPermissions
import com.programmersbox.manga_sources.Sources
import com.programmersbox.manga_sources.utilities.NetworkHelper
import com.programmersbox.models.ApiService
import com.programmersbox.models.ChapterModel
import com.programmersbox.models.Storage
import com.programmersbox.models.sourcePublish
import com.programmersbox.uiviews.BaseListFragment
import com.programmersbox.uiviews.GenericInfo
import com.programmersbox.uiviews.ItemListAdapter
import com.programmersbox.uiviews.SettingsDsl
import com.programmersbox.uiviews.utils.AppUpdate
import com.programmersbox.uiviews.utils.AutoFitGridLayoutManager
import com.programmersbox.uiviews.utils.ChapterModelSerializer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import org.koin.dsl.module
import java.io.File

val appModule = module {
    single<GenericInfo> { GenericManga(get()) }
    single { NetworkHelper(get()) }
}

class GenericManga(val context: Context) : GenericInfo {

    private val disposable = CompositeDisposable()

    override val showMiddleChapterButton: Boolean get() = false

    override val apkString: AppUpdate.AppUpdates.() -> String? get() = { manga_file }

    override fun createAdapter(context: Context, baseListFragment: BaseListFragment): ItemListAdapter<RecyclerView.ViewHolder> =
        (MangaGalleryAdapter(context, baseListFragment) as ItemListAdapter<RecyclerView.ViewHolder>)

    override fun createLayoutManager(context: Context): RecyclerView.LayoutManager =
        AutoFitGridLayoutManager(context, 360).apply { orientation = GridLayoutManager.VERTICAL }

    override fun chapterOnClick(model: ChapterModel, allChapters: List<ChapterModel>, context: Context) {
        context.startActivity(
            Intent(context, ReadActivity::class.java).apply {
                putExtra("currentChapter", model.toJson(ChapterModel::class.java to ChapterModelSerializer()))
                putExtra("allChapters", allChapters.toJson(ChapterModel::class.java to ChapterModelSerializer()))
                putExtra("mangaTitle", model.name)
                putExtra("mangaUrl", model.url)
            }
        )
    }

    private fun downloadFullChapter(model: ChapterModel, title: String) {
        val fileLocation = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() + "/MangaWorld/"

        val direct = File("$fileLocation$title/${model.name}/")
        if (!direct.exists()) direct.mkdir()

        model.getChapterInfo()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .map { it.mapNotNull(Storage::link) }
            .map {
                it.mapIndexed { index, s ->
                    DownloadManager.Request(s.toUri())
                        .setDestinationInExternalPublicDir(Environment.DIRECTORY_PICTURES, "MangaWorld/$title/${model.name}/$index.png")
                        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                        .setAllowedOverRoaming(true)
                        .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE or DownloadManager.Request.NETWORK_WIFI)
                        .setMimeType("image/jpeg")
                        .setTitle(model.name)
                        .addRequestHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) Gecko/20100101 Firefox/77")
                        .addRequestHeader("Accept-Language", "en-US,en;q=0.5")
                }
            }
            .subscribeBy { it.forEach(context.downloadManager::enqueue) }
            .addTo(disposable)
    }

    override fun downloadChapter(chapterModel: ChapterModel, title: String) {
        MainActivity.activity.requestPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE) { p ->
            if (p.isGranted) downloadFullChapter(chapterModel, title)
        }
    }

    override fun customPreferences(preferenceScreen: SettingsDsl) {
        preferenceScreen.generalSettings {
            it.addPreference(
                SwitchPreference(it.context).apply {
                    title = it.context.getString(R.string.showAdultSources)
                    isChecked = context.showAdult
                    setOnPreferenceChangeListener { _, newValue ->
                        context.showAdult = newValue as Boolean
                        if (!newValue && sourcePublish.value == Sources.TSUMINO) {
                            sourcePublish.onNext(Sources.values().random())
                        }
                        true
                    }
                    icon = ContextCompat.getDrawable(it.context, R.drawable.ic_baseline_text_format_24)
                }
            )
        }
    }

    override fun sourceList(): List<ApiService> =
        if (context.showAdult) Sources.values().toList() else Sources.values().filterNot(Sources::isAdult).toList()

    override fun toSource(s: String): ApiService? = try {
        Sources.valueOf(s)
    } catch (e: IllegalArgumentException) {
        null
    }
}