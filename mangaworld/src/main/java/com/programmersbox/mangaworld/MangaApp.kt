package com.programmersbox.mangaworld

import android.graphics.Bitmap
import androidx.core.content.FileProvider
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import com.programmersbox.sharedutils.FirebaseDb
import com.programmersbox.uiviews.OtakuApp
import org.koin.core.context.loadKoinModules

class MangaApp : OtakuApp() {
    override fun onCreated() {
        loadKoinModules(appModule)

        SubsamplingScaleImageView.setPreferredBitmapConfig(Bitmap.Config.ARGB_8888)

        FirebaseDb.DOCUMENT_ID = "favoriteManga"
        FirebaseDb.CHAPTERS_ID = "chaptersRead"
        FirebaseDb.COLLECTION_ID = "mangaworld"
        FirebaseDb.ITEM_ID = "mangaUrl"
        FirebaseDb.READ_OR_WATCHED_ID = "chapterCount"
    }
}

class GenericFileProvider : FileProvider()