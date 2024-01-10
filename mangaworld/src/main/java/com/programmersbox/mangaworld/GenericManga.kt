package com.programmersbox.mangaworld

import android.Manifest
import android.app.DownloadManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Environment
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ChromeReaderMode
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FormatLineSpacing
import androidx.compose.material.icons.filled.Pages
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import androidx.core.app.TaskStackBuilder
import androidx.core.net.toUri
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.programmersbox.favoritesdatabase.DbModel
import com.programmersbox.gsonutils.toJson
import com.programmersbox.helpfulutils.downloadManager
import com.programmersbox.helpfulutils.requestPermissions
import com.programmersbox.mangaworld.downloads.DownloadScreen
import com.programmersbox.mangaworld.downloads.DownloadViewModel
import com.programmersbox.mangaworld.reader.ReadActivity
import com.programmersbox.mangaworld.reader.ReadView
import com.programmersbox.mangaworld.reader.ReadViewModel
import com.programmersbox.models.ApiService
import com.programmersbox.models.ChapterModel
import com.programmersbox.models.InfoModel
import com.programmersbox.models.ItemModel
import com.programmersbox.models.Storage
import com.programmersbox.sharedutils.AppUpdate
import com.programmersbox.source_utilities.NetworkHelper
import com.programmersbox.uiviews.GenericInfo
import com.programmersbox.uiviews.settings.ComposeSettingsDsl
import com.programmersbox.uiviews.utils.ChapterModelSerializer
import com.programmersbox.uiviews.utils.ComponentState
import com.programmersbox.uiviews.utils.LocalNavController
import com.programmersbox.uiviews.utils.M3CoverCard
import com.programmersbox.uiviews.utils.M3PlaceHolderCoverCard
import com.programmersbox.uiviews.utils.NotificationLogo
import com.programmersbox.uiviews.utils.PreferenceSetting
import com.programmersbox.uiviews.utils.ShowWhen
import com.programmersbox.uiviews.utils.SliderSetting
import com.programmersbox.uiviews.utils.SwitchSetting
import com.programmersbox.uiviews.utils.adaptiveGridCell
import com.programmersbox.uiviews.utils.dispatchIo
import com.programmersbox.uiviews.utils.updatePref
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.koin.dsl.module
import java.io.File

val appModule = module {
    single<GenericInfo> { GenericManga(get(), get()) }
    single { NetworkHelper(get()) }
    single { NotificationLogo(R.drawable.manga_world_round_logo) }
    single { ChapterHolder() }
}

class ChapterHolder {
    var chapterModel: ChapterModel? = null
    var chapters: List<ChapterModel>? = null
}

class GenericManga(
    val context: Context,
    val chapterHolder: ChapterHolder
) : GenericInfo {

    override val sourceType: String get() = "manga"

    override val deepLinkUri: String get() = "mangaworld://"

    override val apkString: AppUpdate.AppUpdates.() -> String? get() = { if (BuildConfig.FLAVOR == "noFirebase") manga_no_firebase_file else manga_file }
    override val scrollBuffer: Int = 4

    override fun chapterOnClick(
        model: ChapterModel,
        allChapters: List<ChapterModel>,
        infoModel: InfoModel,
        context: Context,
        activity: FragmentActivity,
        navController: NavController
    ) {
        chapterHolder.chapters = allChapters
        if (runBlocking { context.useNewReaderFlow.first() }) {
            chapterHolder.chapterModel = model
            ReadViewModel.navigateToMangaReader(navController, infoModel.title, model.url, model.sourceUrl)
        } else {
            context.startActivity(
                Intent(context, ReadActivity::class.java).apply {
                    putExtra("currentChapter", model.toJson(ChapterModel::class.java to ChapterModelSerializer()))
                    putExtra("allChapters", allChapters.toJson(ChapterModel::class.java to ChapterModelSerializer()))
                    putExtra("mangaTitle", infoModel.title)
                    putExtra("mangaUrl", model.url)
                    putExtra("mangaInfoUrl", model.sourceUrl)
                }
            )
        }
    }

    private fun downloadFullChapter(model: ChapterModel, title: String) {
        //val fileLocation = runBlocking { context.folderLocationFlow.first() }
        val fileLocation = DOWNLOAD_FILE_PATH

        val direct = File("$fileLocation$title/${model.name}/")
        if (!direct.exists()) direct.mkdir()

        GlobalScope.launch {
            model.getChapterInfo()
                .dispatchIo()
                .map { it.mapNotNull(Storage::link) }
                .map {
                    it.mapIndexed { index, s ->
                        //val location = "/$fileLocation/$title/${model.name}"

                        //val file = File(Environment.getExternalStorageDirectory().path + location, "${String.format("%03d", index)}.png")

                        DownloadManager.Request(s.toUri())
                            //.setDestinationUri(file.toUri())
                            .setDestinationInExternalPublicDir(
                                Environment.DIRECTORY_DOWNLOADS,
                                "MangaWorld/$title/${model.name}/${String.format("%03d", index)}"
                            )
                            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                            .setAllowedOverRoaming(true)
                            .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE or DownloadManager.Request.NETWORK_WIFI)
                            .setMimeType("image/*")
                            .setTitle(model.name)
                            .addRequestHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) Gecko/20100101 Firefox/77")
                            .addRequestHeader("Accept-Language", "en-US,en;q=0.5")
                    }
                }
                .onEach { it.fastForEach(context.downloadManager::enqueue) }
                .collect()
        }
    }

    override fun downloadChapter(
        model: ChapterModel,
        allChapters: List<ChapterModel>,
        infoModel: InfoModel,
        context: Context,
        activity: FragmentActivity,
        navController: NavController
    ) {
        activity.requestPermissions(
            *if (Build.VERSION.SDK_INT >= 33) arrayOf(Manifest.permission.READ_MEDIA_VIDEO)
            else arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        ) { p -> if (p.isGranted) downloadFullChapter(model, infoModel.title.ifBlank { infoModel.url }) }
    }

    override fun sourceList(): List<ApiService> = emptyList()

    override fun toSource(s: String): ApiService? = null

    @Composable
    override fun ComposeShimmerItem() {
        LazyVerticalGrid(
            columns = adaptiveGridCell(),
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) { items(10) { M3PlaceHolderCoverCard(placeHolder = R.drawable.manga_world_round_logo) } }
    }

    @OptIn(
        ExperimentalFoundationApi::class
    )
    @Composable
    override fun ItemListView(
        list: List<ItemModel>,
        favorites: List<DbModel>,
        listState: LazyGridState,
        onLongPress: (ItemModel, ComponentState) -> Unit,
        modifier: Modifier,
        paddingValues: PaddingValues,
        onClick: (ItemModel) -> Unit,
    ) {
        LazyVerticalGrid(
            columns = adaptiveGridCell(),
            state = listState,
            contentPadding = paddingValues,
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = modifier.fillMaxSize(),
        ) {
            itemsIndexed(
                list,
                key = { i, it -> "${it.url}$i" },
                contentType = { _, i -> i }
            ) { _, it ->
                M3CoverCard(
                    onLongPress = { c -> onLongPress(it, c) },
                    imageUrl = it.imageUrl,
                    name = it.title,
                    headers = it.extras,
                    placeHolder = R.drawable.manga_world_round_logo,
                    favoriteIcon = {
                        if (favorites.any { f -> f.url == it.url }) {
                            Icon(
                                Icons.Default.Favorite,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.align(Alignment.TopStart)
                            )
                            Icon(
                                Icons.Default.FavoriteBorder,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.align(Alignment.TopStart)
                            )
                        }
                    }
                ) { onClick(it) }
            }
        }
    }

    @OptIn(com.google.accompanist.permissions.ExperimentalPermissionsApi::class)
    override fun composeCustomPreferences(): ComposeSettingsDsl.() -> Unit = {

        viewSettings {
            val navController = LocalNavController.current
            PreferenceSetting(
                settingTitle = { Text(stringResource(R.string.downloaded_manga)) },
                settingIcon = { Icon(Icons.AutoMirrored.Filled.LibraryBooks, null, modifier = Modifier.fillMaxSize()) },
                modifier = Modifier.clickable(
                    indication = rememberRipple(),
                    interactionSource = remember { MutableInteractionSource() }
                ) { navController.navigate(DownloadViewModel.DownloadRoute) { launchSingleTop = true } }
            )
        }

        generalSettings {
            /*if (BuildConfig.DEBUG) {

                val folderLocation by context.folderLocationFlow.collectAsState(initial = DOWNLOAD_FILE_PATH)

                val folderIntent = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
                    uri?.path?.removePrefix("/tree/primary:")?.let {
                        //context.folderLocation = "$it/"
                        scope.launch { context.updatePref(FOLDER_LOCATION, it) }
                        println(it)
                    }
                }

                val storagePermissions = rememberMultiplePermissionsState(
                    listOf(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    )
                )

                var resetFolderDialog by remember { mutableStateOf(false) }

                if (resetFolderDialog) {
                    AlertDialog(
                        onDismissRequest = { resetFolderDialog = false },
                        title = { Text("Reset Folder Location to") },
                        text = { Text(DOWNLOAD_FILE_PATH) },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    scope.launch { context.updatePref(FOLDER_LOCATION, DOWNLOAD_FILE_PATH) }
                                    resetFolderDialog = false
                                }
                            ) { Text("Reset") }
                        },
                        dismissButton = { TextButton(onClick = { resetFolderDialog = false }) { Text(stringResource(R.string.cancel)) } }
                    )
                }



                PermissionsRequired(
                    multiplePermissionsState = storagePermissions,
                    permissionsNotGrantedContent = {
                        PreferenceSetting(
                            endIcon = {
                                IconButton(onClick = { resetFolderDialog = true }) {
                                    androidx.compose.material3.Icon(Icons.Default.FolderDelete, null)
                                }
                            },
                            settingTitle = { androidx.compose.material3.Text(stringResource(R.string.folder_location)) },
                            summaryValue = { androidx.compose.material3.Text(folderLocation) },
                            settingIcon = { androidx.compose.material3.Icon(Icons.Default.Folder, null, modifier = Modifier.fillMaxSize()) },
                            modifier = Modifier.clickable(
                                indication = rememberRipple(),
                                interactionSource = remember { MutableInteractionSource() }
                            ) { storagePermissions.launchMultiplePermissionRequest() }
                        )
                    },
                    permissionsNotAvailableContent = {
                        NeedsPermissions {
                            context.startActivity(
                                Intent().apply {
                                    action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                                    data = Uri.fromParts("package", context.packageName, null)
                                }
                            )
                        }
                    }
                ) {
                    PreferenceSetting(
                        endIcon = {
                            IconButton(onClick = { resetFolderDialog = true }) {
                                androidx.compose.material3.Icon(Icons.Default.FolderDelete, null)
                            }
                        },
                        settingTitle = { androidx.compose.material3.Text(stringResource(R.string.folder_location)) },
                        summaryValue = { androidx.compose.material3.Text(folderLocation) },
                        settingIcon = { androidx.compose.material3.Icon(Icons.Default.Folder, null, modifier = Modifier.fillMaxSize()) },
                        modifier = Modifier.clickable(
                            indication = rememberRipple(),
                            interactionSource = remember { MutableInteractionSource() }
                        ) {
                            if (storagePermissions.allPermissionsGranted) {
                                folderIntent.launch(folderLocation.toUri())
                            } else {
                                storagePermissions.launchMultiplePermissionRequest()
                            }
                        }
                    )
                }
            }*/
        }

        playerSettings {
            val scope = rememberCoroutineScope()
            val context = LocalContext.current

            var padding by remember { mutableFloatStateOf(runBlocking { context.pagePadding.first().toFloat() }) }

            SliderSetting(
                sliderValue = padding,
                settingTitle = { Text(stringResource(R.string.reader_padding_between_pages)) },
                settingSummary = { Text(stringResource(R.string.default_padding_summary)) },
                settingIcon = { Icon(Icons.Default.FormatLineSpacing, null) },
                range = 0f..10f,
                updateValue = { padding = it },
                onValueChangedFinished = { scope.launch { context.updatePref(PAGE_PADDING, padding.toInt()) } }
            )

            val reader by context.useNewReaderFlow.collectAsState(true)

            SwitchSetting(
                settingTitle = { Text(stringResource(R.string.useNewReader)) },
                summaryValue = { Text(stringResource(R.string.reader_summary_setting)) },
                settingIcon = { Icon(Icons.AutoMirrored.Filled.ChromeReaderMode, null, modifier = Modifier.fillMaxSize()) },
                value = reader,
                updateValue = { scope.launch { context.updatePref(USER_NEW_READER, it) } }
            )

            val listOrPager by context.listOrPager.collectAsState(initial = true)

            ShowWhen(reader) {
                SwitchSetting(
                    settingTitle = { Text(stringResource(R.string.list_or_pager_title)) },
                    summaryValue = { Text(stringResource(R.string.list_or_pager_description)) },
                    value = listOrPager,
                    updateValue = { scope.launch { context.updatePref(LIST_OR_PAGER, it) } },
                    settingIcon = { Icon(if (listOrPager) Icons.AutoMirrored.Filled.List else Icons.Default.Pages, null) }
                )
            }
        }

    }

    @OptIn(
        ExperimentalMaterial3Api::class,
        ExperimentalComposeUiApi::class,
        ExperimentalAnimationApi::class,
        ExperimentalFoundationApi::class
    )
    override fun NavGraphBuilder.globalNavSetup() {
        composable(
            ReadViewModel.MangaReaderRoute,
            arguments = listOf(
                navArgument("mangaTitle") { nullable = true },
                navArgument("mangaUrl") { nullable = true },
                navArgument("mangaInfoUrl") { nullable = true },
                navArgument("downloaded") {},
                navArgument("filePath") { nullable = true }
            ),
            enterTransition = { fadeIn() },
            exitTransition = { fadeOut() },
        ) { ReadView() }
    }

    override fun NavGraphBuilder.settingsNavSetup() {
        composable(
            DownloadViewModel.DownloadRoute,
            enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Up) },
            exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Down) },
        ) { DownloadScreen() }
    }

    override fun deepLinkDetails(context: Context, itemModel: ItemModel?): PendingIntent? {
        val deepLinkIntent = Intent(
            Intent.ACTION_VIEW,
            deepLinkDetailsUri(itemModel),
            context,
            MainActivity::class.java
        )

        return TaskStackBuilder.create(context).run {
            addNextIntentWithParentStack(deepLinkIntent)
            getPendingIntent(itemModel?.hashCode() ?: 0, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        }
    }

    override fun deepLinkSettings(context: Context): PendingIntent? {
        val deepLinkIntent = Intent(
            Intent.ACTION_VIEW,
            deepLinkSettingsUri(),
            context,
            MainActivity::class.java
        )

        return TaskStackBuilder.create(context).run {
            addNextIntentWithParentStack(deepLinkIntent)
            getPendingIntent(13, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        }
    }

}