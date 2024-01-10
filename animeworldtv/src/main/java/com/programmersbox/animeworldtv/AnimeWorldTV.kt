package com.programmersbox.animeworldtv

import android.app.Application
import com.programmersbox.loggingutils.Loged
import com.programmersbox.sharedutils.AppLogo
import com.programmersbox.sharedutils.FirebaseDb
import com.programmersbox.sharedutils.FirebaseUIStyle
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.loadKoinModules
import org.koin.core.context.startKoin
import org.koin.dsl.module

class AnimeWorldTV : Application() {

    override fun onCreate() {
        super.onCreate()

        Loged.FILTER_BY_PACKAGE_NAME = "programmersbox"
        Loged.TAG = this::class.java.simpleName

        FirebaseDb.DOCUMENT_ID = "favoriteShows"
        FirebaseDb.CHAPTERS_ID = "episodesWatched"
        FirebaseDb.COLLECTION_ID = "animeworld"
        FirebaseDb.ITEM_ID = "showUrl"
        FirebaseDb.READ_OR_WATCHED_ID = "numEpisodes"

        startKoin {
            androidLogger()
            androidContext(this@AnimeWorldTV)
            loadKoinModules(
                module {
                    single { AppLogo(applicationInfo.loadIcon(packageManager), applicationInfo.icon) }
                    single { FirebaseUIStyle(R.style.Theme_OtakuWorld) }
                }
            )
        }
    }

}