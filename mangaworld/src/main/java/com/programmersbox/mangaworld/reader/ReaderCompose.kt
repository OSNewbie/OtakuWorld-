@file:Suppress("INLINE_FROM_HIGHER_PLATFORM")
@file:OptIn(ExperimentalMaterialApi::class)

package com.programmersbox.mangaworld.reader

import android.annotation.SuppressLint
import android.content.Context
import android.text.format.DateFormat
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.FormatLineSpacing
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Pages
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItemColors
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberSwipeToDismissState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bumptech.glide.load.model.GlideUrl
import com.programmersbox.favoritesdatabase.ItemDao
import com.programmersbox.helpfulutils.battery
import com.programmersbox.helpfulutils.timeTick
import com.programmersbox.mangaworld.ChapterHolder
import com.programmersbox.mangaworld.LIST_OR_PAGER
import com.programmersbox.mangaworld.PAGE_PADDING
import com.programmersbox.mangaworld.R
import com.programmersbox.mangaworld.listOrPager
import com.programmersbox.mangaworld.pagePadding
import com.programmersbox.uiviews.GenericInfo
import com.programmersbox.uiviews.utils.BatteryInformation
import com.programmersbox.uiviews.utils.ComposableUtils
import com.programmersbox.uiviews.utils.HideSystemBarsWhileOnScreen
import com.programmersbox.uiviews.utils.LightAndDarkPreviews
import com.programmersbox.uiviews.utils.LocalActivity
import com.programmersbox.uiviews.utils.LocalGenericInfo
import com.programmersbox.uiviews.utils.LocalItemDao
import com.programmersbox.uiviews.utils.LocalNavController
import com.programmersbox.uiviews.utils.LocalSettingsHandling
import com.programmersbox.uiviews.utils.PreviewTheme
import com.programmersbox.uiviews.utils.SliderSetting
import com.programmersbox.uiviews.utils.SwitchSetting
import com.programmersbox.uiviews.utils.adaptiveGridCell
import com.programmersbox.uiviews.utils.components.HazeScaffold
import com.programmersbox.uiviews.utils.components.OtakuScaffold
import com.programmersbox.uiviews.utils.dataStore
import com.programmersbox.uiviews.utils.updatePref
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.glide.GlideImage
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import io.ktor.client.request.header
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.engawapg.lib.zoomable.rememberZoomState
import net.engawapg.lib.zoomable.zoomable
import org.koin.compose.koinInject

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@ExperimentalMaterial3Api
@ExperimentalComposeUiApi
@ExperimentalAnimationApi
@ExperimentalFoundationApi
@Composable
fun ReadView(
    context: Context = LocalContext.current,
    genericInfo: GenericInfo = LocalGenericInfo.current,
    ch: ChapterHolder = koinInject(),
    dao: ItemDao = LocalItemDao.current,
    readVm: ReadViewModel = viewModel {
        ReadViewModel(
            handle = createSavedStateHandle(),
            genericInfo = genericInfo,
            chapterHolder = ch,
            dao = dao
        )
    },
) {
    HideSystemBarsWhileOnScreen()

    val scope = rememberCoroutineScope()
    val pullRefreshState = rememberPullRefreshState(refreshing = readVm.isLoadingPages, onRefresh = readVm::refresh)

    val pages = readVm.pageList

    val listOrPager by context.listOrPager.collectAsState(initial = true)

    val pagerState = rememberPagerState(
        initialPage = 0,
        initialPageOffsetFraction = 0f
    ) { pages.size + 1 }

    val listState = rememberLazyListState()
    val currentPage by remember { derivedStateOf { if (listOrPager) listState.firstVisibleItemIndex else pagerState.currentPage } }

    val paddingPage by context.pagePadding.collectAsState(initial = 4)
    var settingsPopup by remember { mutableStateOf(false) }
    val settingsHandling = LocalSettingsHandling.current

    if (settingsPopup) {
        AlertDialog(
            properties = DialogProperties(usePlatformDefaultWidth = false),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            onDismissRequest = { settingsPopup = false },
            title = { Text(stringResource(R.string.settings)) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    SliderSetting(
                        scope = scope,
                        settingIcon = Icons.Default.BatteryAlert,
                        settingTitle = R.string.battery_alert_percentage,
                        settingSummary = R.string.battery_default,
                        preferenceUpdate = { settingsHandling.setBatteryPercentage(it) },
                        initialValue = runBlocking { settingsHandling.batteryPercentage.firstOrNull() ?: 20 },
                        range = 1f..100f
                    )
                    HorizontalDivider()
                    val activity = LocalActivity.current
                    SliderSetting(
                        scope = scope,
                        settingIcon = Icons.Default.FormatLineSpacing,
                        settingTitle = R.string.reader_padding_between_pages,
                        settingSummary = R.string.default_padding_summary,
                        preferenceUpdate = { activity.updatePref(PAGE_PADDING, it) },
                        initialValue = runBlocking { context.dataStore.data.first()[PAGE_PADDING] ?: 4 },
                        range = 0f..10f
                    )
                    HorizontalDivider()
                    SwitchSetting(
                        settingTitle = { Text(stringResource(R.string.list_or_pager_title)) },
                        summaryValue = { Text(stringResource(R.string.list_or_pager_description)) },
                        value = listOrPager,
                        updateValue = { scope.launch { activity.updatePref(LIST_OR_PAGER, it) } },
                        settingIcon = { Icon(if (listOrPager) Icons.AutoMirrored.Filled.List else Icons.Default.Pages, null) }
                    )
                }
            },
            confirmButton = { TextButton(onClick = { settingsPopup = false }) { Text(stringResource(R.string.ok)) } }
        )
    }

    val activity = LocalActivity.current

    fun showToast() {
        activity.runOnUiThread { Toast.makeText(context, R.string.addedChapterItem, Toast.LENGTH_SHORT).show() }
    }

    val listShowItems by remember { derivedStateOf { listState.isScrolledToTheEnd() && listOrPager } }
    val pagerShowItems by remember { derivedStateOf { pagerState.currentPage >= pages.size && !listOrPager } }

    val listIndex by remember { derivedStateOf { listState.layoutInfo.visibleItemsInfo.firstOrNull()?.index ?: 0 } }
    LaunchedEffect(listIndex, pagerState.currentPage, readVm.showInfo) {
        if (readVm.firstScroll && (listIndex > 0 || pagerState.currentPage > 0)) {
            readVm.showInfo = false
            readVm.firstScroll = false
        }
    }

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect { listState.scrollToItem(it) }
    }

    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex }.collect { pagerState.scrollToPage(it) }
    }

    val showItems by remember { derivedStateOf { readVm.showInfo || listShowItems || pagerShowItems } }

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    var showBottomSheet by remember { mutableStateOf(false) }

    BackHandler(drawerState.isOpen || showBottomSheet) {
        scope.launch {
            when {
                drawerState.isOpen -> drawerState.close()
                showBottomSheet -> showBottomSheet = false
            }
        }
    }

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
        ) {
            SheetView(
                readVm = readVm,
                onSheetHide = { showBottomSheet = false },
                currentPage = currentPage,
                pages = pages,
                listOrPager = listOrPager,
                pagerState = pagerState,
                listState = listState
            )
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                DrawerView(readVm = readVm, showToast = ::showToast)
            }
        },
        gesturesEnabled = readVm.list.size > 1
    ) {
        HazeScaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                AnimatedVisibility(
                    visible = showItems,
                    enter = slideInVertically() + fadeIn(),
                    exit = slideOutVertically() + fadeOut()
                ) {
                    TopBar(
                        scrollBehavior = scrollBehavior,
                        pages = pages,
                        currentPage = currentPage,
                    )
                }
            },
            bottomBar = {
                AnimatedVisibility(
                    visible = showItems,
                    enter = slideInVertically { it / 2 } + fadeIn(),
                    exit = slideOutVertically { it / 2 } + fadeOut()
                ) {
                    BottomBar(
                        onPageSelectClick = { showBottomSheet = true },
                        onSettingsClick = { settingsPopup = true },
                        chapterChange = ::showToast,
                        vm = readVm
                    )
                }
            },
            blurTopBar = true,
            blurBottomBar = true
        ) { p ->
            Box(
                modifier = Modifier.pullRefresh(pullRefreshState)
            ) {
                val spacing = LocalContext.current.dpToPx(paddingPage).dp
                Crossfade(targetState = listOrPager, label = "") {
                    if (it) {
                        ListView(
                            listState = listState,
                            pages = pages,
                            readVm = readVm,
                            itemSpacing = spacing,
                            paddingValues = PaddingValues(bottom = p.calculateBottomPadding())
                        ) { readVm.showInfo = !readVm.showInfo }
                    } else {
                        PagerView(
                            pagerState = pagerState,
                            pages = pages,
                            vm = readVm,
                            itemSpacing = spacing,
                        ) { readVm.showInfo = !readVm.showInfo }
                    }
                }

                PullRefreshIndicator(
                    refreshing = readVm.isLoadingPages,
                    state = pullRefreshState,
                    modifier = Modifier.align(Alignment.TopCenter),
                    backgroundColor = MaterialTheme.colorScheme.background,
                    contentColor = MaterialTheme.colorScheme.onBackground,
                    scale = true
                )
            }
        }
    }
}

@Composable
private fun PaddingValues.animate() = PaddingValues(
    start = calculateStartPadding(LocalLayoutDirection.current).animate().value,
    end = calculateEndPadding(LocalLayoutDirection.current).animate().value,
    top = calculateTopPadding().animate().value,
    bottom = calculateBottomPadding().animate().value,
)

@Composable
private fun Dp.animate() = animateDpAsState(targetValue = this, label = "")

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun DrawerView(
    readVm: ReadViewModel,
    showToast: () -> Unit,
) {
    val drawerScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    OtakuScaffold(
        modifier = Modifier.nestedScroll(drawerScrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                scrollBehavior = drawerScrollBehavior,
                title = { Text(readVm.title) },
                actions = { PageIndicator(currentPage = readVm.list.size - readVm.currentChapter, pageCount = readVm.list.size) }
            )
        }
    ) { p ->
        LazyColumn(
            state = rememberLazyListState(
                readVm.currentChapter.coerceIn(
                    0,
                    readVm.list.lastIndex.coerceIn(minimumValue = 0, maximumValue = Int.MAX_VALUE)
                )
            ),
            contentPadding = p,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            itemsIndexed(readVm.list) { i, c ->

                var showChangeChapter by remember { mutableStateOf(false) }

                if (showChangeChapter) {
                    AlertDialog(
                        onDismissRequest = { showChangeChapter = false },
                        title = { Text(stringResource(R.string.changeToChapter, c.name)) },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    showChangeChapter = false
                                    readVm.currentChapter = i
                                    readVm.addChapterToWatched(readVm.currentChapter, showToast)
                                }
                            ) { Text(stringResource(R.string.yes)) }
                        },
                        dismissButton = {
                            TextButton(onClick = { showChangeChapter = false }) { Text(stringResource(R.string.no)) }
                        }
                    )
                }

                WrapHeightNavigationDrawerItem(
                    modifier = Modifier
                        .padding(bottom = 4.dp)
                        .padding(horizontal = 4.dp),
                    label = { Text(c.name) },
                    selected = readVm.currentChapter == i,
                    onClick = { showChangeChapter = true },
                    shape = RoundedCornerShape(8.0.dp)//MaterialTheme.shapes.medium
                )

                if (i < readVm.list.lastIndex) HorizontalDivider()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class, ExperimentalFoundationApi::class)
@Composable
fun SheetView(
    readVm: ReadViewModel,
    onSheetHide: () -> Unit,
    currentPage: Int,
    pages: List<String>,
    listOrPager: Boolean,
    pagerState: PagerState,
    listState: LazyListState,
) {
    val scope = rememberCoroutineScope()
    val sheetScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    Scaffold(
        modifier = Modifier.nestedScroll(sheetScrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                scrollBehavior = sheetScrollBehavior,
                title = { Text(readVm.list.getOrNull(readVm.currentChapter)?.name.orEmpty()) },
                actions = { PageIndicator(currentPage + 1, pages.size) },
                navigationIcon = {
                    IconButton(onClick = onSheetHide) {
                        Icon(Icons.Default.Close, null)
                    }
                }
            )
        }
    ) { p ->
        LazyVerticalGrid(
            columns = adaptiveGridCell(),
            contentPadding = p,
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            itemsIndexed(pages) { i, it ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .size(ComposableUtils.IMAGE_WIDTH, ComposableUtils.IMAGE_HEIGHT)
                        .border(
                            animateDpAsState(if (currentPage == i) 4.dp else 0.dp, label = "").value,
                            color = animateColorAsState(
                                if (currentPage == i) MaterialTheme.colorScheme.primary
                                else Color.Transparent, label = ""
                            ).value
                        )
                        .clickable {
                            scope.launch {
                                if (currentPage == i) onSheetHide()
                                if (listOrPager) listState.animateScrollToItem(i) else pagerState.animateScrollToPage(i)
                            }
                        }
                ) {
                    KamelImage(
                        resource = asyncPainterResource(it),
                        onLoading = { CircularProgressIndicator(progress = { it }) },
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.Center)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.Black
                                    ),
                                    startY = 50f
                                ),
                                alpha = .5f
                            )
                    ) {
                        Text(
                            (i + 1).toString(),
                            style = MaterialTheme
                                .typography
                                .bodyLarge
                                .copy(textAlign = TextAlign.Center, color = Color.White),
                            maxLines = 2,
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.Center)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ListView(
    listState: LazyListState,
    pages: List<String>,
    readVm: ReadViewModel,
    itemSpacing: Dp,
    paddingValues: PaddingValues,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        state = listState,
        verticalArrangement = Arrangement.spacedBy(itemSpacing),
        contentPadding = paddingValues,
    ) { reader(pages, readVm, onClick) }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PagerView(
    pagerState: PagerState,
    pages: List<String>,
    vm: ReadViewModel,
    itemSpacing: Dp,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    VerticalPager(
        state = pagerState,
        modifier = modifier.fillMaxSize(),
        pageSpacing = itemSpacing,
        beyondBoundsPageCount = 1,
        key = { it }
    ) { page ->
        pages.getOrNull(page)?.let {
            ChapterPage(it, vm.isDownloaded, onClick, vm.headers, ContentScale.Fit)
        } ?: Box(modifier = Modifier.fillMaxSize()) {
            LastPageReached(
                isLoading = vm.isLoadingPages,
                currentChapter = vm.currentChapter,
                lastChapter = vm.list.lastIndex,
                chapterName = vm.list.getOrNull(vm.currentChapter)?.name.orEmpty(),
                nextChapter = { vm.addChapterToWatched(++vm.currentChapter) {} },
                previousChapter = { vm.addChapterToWatched(--vm.currentChapter) {} },
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

@Composable
private fun LastPageReached(
    isLoading: Boolean,
    currentChapter: Int,
    lastChapter: Int,
    chapterName: String,
    nextChapter: () -> Unit,
    previousChapter: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val alpha by animateFloatAsState(targetValue = if (isLoading) 0f else 1f, label = "")

    ChangeChapterSwipe(
        nextChapter = nextChapter,
        previousChapter = previousChapter,
        isLoading = isLoading,
        currentChapter = currentChapter,
        lastChapter = lastChapter,
        modifier = modifier
    ) {
        Box(Modifier.fillMaxSize()) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            Column(
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    chapterName,
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                    modifier = Modifier.graphicsLayer { this.alpha = alpha }
                ) {
                    Text(
                        stringResource(id = R.string.lastPage),
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.CenterHorizontally)
                    )
                    if (currentChapter <= 0) {
                        Text(
                            stringResource(id = R.string.reachedLastChapter),
                            style = MaterialTheme.typography.headlineSmall,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.CenterHorizontally)
                        )
                    }
                }

                Text(
                    stringResource(id = R.string.swipeChapter),
                    style = MaterialTheme.typography.labelLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        /*ConstraintLayout(Modifier.fillMaxSize()) {

            val (loading, name, lastInfo, swipeInfo, ad) = createRefs()

            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.constrainAs(loading) {
                        centerVerticallyTo(parent)
                        centerHorizontallyTo(parent)
                    }
                )
            }

            //readVm.list.size - readVm.currentChapter
            //If things start getting WAY too long, just replace with "Chapter ${vm.list.size - vm.currentChapter}
            Text(
                chapterName,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .constrainAs(name) {
                        top.linkTo(parent.top)
                        centerHorizontallyTo(parent)
                    }
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier
                    .graphicsLayer { this.alpha = alpha }
                    .constrainAs(lastInfo) {
                        centerVerticallyTo(parent)
                        centerHorizontallyTo(parent)
                        bottom.linkTo(swipeInfo.top)
                        top.linkTo(name.bottom)
                    }
            ) {
                Text(
                    stringResource(id = R.string.lastPage),
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.CenterHorizontally)
                )
                if (currentChapter <= 0) {
                    Text(
                        stringResource(id = R.string.reachedLastChapter),
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.CenterHorizontally)
                    )
                }
            }

            Text(
                stringResource(id = R.string.swipeChapter),
                style = MaterialTheme.typography.labelLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .constrainAs(swipeInfo) {
                        bottom.linkTo(ad.top)
                        centerHorizontallyTo(parent)
                    }
            )

            if (BuildConfig.BUILD_TYPE == "release" && false) {
                val context = LocalContext.current
                AndroidView(
                    modifier = Modifier
                        .constrainAs(ad) {
                            bottom.linkTo(parent.bottom)
                            centerHorizontallyTo(parent)
                        }
                        .fillMaxWidth(),
                    factory = {
                        AdView(it).apply {
                            setAdSize(AdSize.BANNER)
                            adUnitId = context.getString(R.string.ad_unit_id)
                            loadAd(adRequest)
                        }
                    }
                )
            } else {
                Spacer(
                    Modifier
                        .height(10.dp)
                        .constrainAs(ad) {
                            bottom.linkTo(parent.bottom)
                            centerHorizontallyTo(parent)
                        }
                )
            }

        }*/
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangeChapterSwipe(
    nextChapter: () -> Unit,
    previousChapter: () -> Unit,
    currentChapter: Int,
    lastChapter: Int,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    BoxWithConstraints(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .heightIn(min = 100.dp)
            .wrapContentHeight()
    ) {
        val dismissState = rememberSwipeToDismissState(
            confirmValueChange = {
                when (it) {
                    SwipeToDismissValue.StartToEnd -> nextChapter()
                    SwipeToDismissValue.EndToStart -> previousChapter()
                    else -> Unit
                }
                false
            }
        )

        SwipeToDismissBox(
            state = dismissState,
            enableDismissFromStartToEnd = !isLoading && currentChapter < lastChapter,
            enableDismissFromEndToStart = !isLoading && currentChapter > 0,
            backgroundContent = {
                val direction = dismissState.dismissDirection
                val scale by animateFloatAsState(if (dismissState.targetValue == SwipeToDismissValue.Settled) 0.75f else 1f, label = "")

                val alignment = when (direction) {
                    SwipeToDismissValue.StartToEnd -> Alignment.CenterStart
                    SwipeToDismissValue.EndToStart -> Alignment.CenterEnd
                    else -> Alignment.Center
                }

                val icon = when (direction) {
                    SwipeToDismissValue.StartToEnd -> Icons.Default.FastRewind
                    SwipeToDismissValue.EndToStart -> Icons.Default.FastForward
                    else -> Icons.Default.Pages
                }

                Box(
                    Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    contentAlignment = alignment
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        modifier = Modifier.scale(scale),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            },
            content = {
                OutlinedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(this@BoxWithConstraints.maxHeight / 2)
                ) { content() }
            }
        )
    }
}

private fun LazyListScope.reader(pages: List<String>, vm: ReadViewModel, onClick: () -> Unit) {
    items(pages, key = { it }, contentType = { it }) { ChapterPage(it, vm.isDownloaded, onClick, vm.headers, ContentScale.FillWidth) }
    item {
        LastPageReached(
            isLoading = vm.isLoadingPages,
            currentChapter = vm.currentChapter,
            lastChapter = vm.list.lastIndex,
            chapterName = vm.list.getOrNull(vm.currentChapter)?.name.orEmpty(),
            nextChapter = { vm.addChapterToWatched(++vm.currentChapter) {} },
            previousChapter = { vm.addChapterToWatched(--vm.currentChapter) {} },
        )
    }
}

@Composable
private fun ChapterPage(
    chapterLink: String,
    isDownloaded: Boolean,
    openCloseOverlay: () -> Unit,
    headers: Map<String, String>,
    contentScale: ContentScale,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .requiredHeightIn(min = 100.dp),
        contentAlignment = Alignment.Center
    ) {
        ZoomableImage(
            painter = chapterLink,
            isDownloaded = isDownloaded,
            headers = headers,
            modifier = Modifier.fillMaxWidth(),
            contentScale = contentScale,
            onClick = openCloseOverlay
        )
    }
}

@Composable
private fun ZoomableImage(
    painter: String,
    isDownloaded: Boolean,
    headers: Map<String, String>,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
    onClick: () -> Unit = {},
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RectangleShape)
            /*.clickable(
                indication = null,
                onClick = onClick,
                interactionSource = remember { MutableInteractionSource() }
            )*/
            .zoomable(
                rememberZoomState(),
                enableOneFingerZoom = false,
                onTap = { onClick() }
            )
    ) {
        val scope = rememberCoroutineScope()
        var showTheThing by remember { mutableStateOf(true) }

        if (showTheThing) {
            if (isDownloaded) {
                val url = remember(painter) { GlideUrl(painter) { headers } }
                GlideImage(
                    imageModel = { if (isDownloaded) painter else url },
                    imageOptions = ImageOptions(contentScale = contentScale),
                    loading = { CircularProgressIndicator(modifier = Modifier.align(Alignment.Center)) },
                    failure = {
                        Text(
                            stringResource(R.string.pressToRefresh),
                            modifier = Modifier
                                .align(Alignment.Center)
                                .clickable {
                                    scope.launch {
                                        showTheThing = false
                                        delay(1000)
                                        showTheThing = true
                                    }
                                }
                        )
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .heightIn(min = ComposableUtils.IMAGE_HEIGHT)
                        .align(Alignment.Center)
                        .clipToBounds()
                )
            } else {
                KamelImage(
                    resource = asyncPainterResource(painter) {
                        requestBuilder {
                            headers.forEach { (t, u) -> header(t, u) }
                        }
                    },
                    onLoading = {
                        val progress by animateFloatAsState(targetValue = it, label = "")
                        CircularProgressIndicator(progress = { progress })
                    },
                    onFailure = {
                        Text(
                            stringResource(R.string.pressToRefresh),
                            modifier = Modifier
                                .clickable {
                                    scope.launch {
                                        showTheThing = false
                                        delay(1000)
                                        showTheThing = true
                                    }
                                }
                        )
                    },
                    contentDescription = null,
                    contentScale = contentScale,
                    modifier = Modifier
                        .fillMaxSize()
                        .fillMaxWidth()
                        .heightIn(min = ComposableUtils.IMAGE_HEIGHT)
                        .clipToBounds()
                )
            }
        }
    }
}

private fun clampOffset(centerPoint: Offset, offset: Offset, scale: Float): Offset {
    val maxPosition = centerPoint * (scale - 1)

    return offset.copy(
        x = offset.x.coerceIn(-maxPosition.x, maxPosition.x),
        y = offset.y.coerceIn(-maxPosition.y, maxPosition.y)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@ExperimentalAnimationApi
@Composable
private fun TopBar(
    scrollBehavior: TopAppBarScrollBehavior,
    pages: List<String>,
    currentPage: Int,
    modifier: Modifier = Modifier,
) {
    CenterAlignedTopAppBar(
        windowInsets = WindowInsets(0.dp),
        scrollBehavior = scrollBehavior,
        modifier = modifier,
        navigationIcon = {
            val context = LocalContext.current
            var batteryColor by remember { mutableStateOf(Color.White) }
            var batteryIcon by remember { mutableStateOf(BatteryInformation.BatteryViewType.UNKNOWN) }
            var batteryPercent by remember { mutableFloatStateOf(0f) }
            val batteryInformation = remember { BatteryInformation(context) }

            LaunchedEffect(Unit) {
                batteryInformation.composeSetupFlow(
                    Color.White
                ) {
                    batteryColor = it.first
                    batteryIcon = it.second
                }
                    .launchIn(this)
            }

            DisposableEffect(LocalContext.current) {
                val batteryInfo = context.battery {
                    batteryPercent = it.percent
                    batteryInformation.batteryLevel.tryEmit(it.percent)
                    batteryInformation.batteryInfo.tryEmit(it)
                }
                onDispose { context.unregisterReceiver(batteryInfo) }
            }
            Row(
                modifier = Modifier.padding(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    batteryIcon.composeIcon,
                    contentDescription = null,
                    tint = animateColorAsState(
                        if (batteryColor == Color.White) MaterialTheme.colorScheme.onSurface
                        else batteryColor, label = ""
                    ).value
                )
                Text(
                    "${batteryPercent.toInt()}%",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        },
        title = {
            var time by remember { mutableLongStateOf(System.currentTimeMillis()) }

            val activity = LocalActivity.current

            DisposableEffect(LocalContext.current) {
                val timeReceiver = activity.timeTick { _, _ -> time = System.currentTimeMillis() }
                onDispose { activity.unregisterReceiver(timeReceiver) }
            }

            Text(
                DateFormat.getTimeFormat(LocalContext.current).format(time).toString(),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(4.dp)
            )
        },
        actions = {
            PageIndicator(
                currentPage = currentPage + 1,
                pageCount = pages.size,
                modifier = Modifier
                    .padding(4.dp)
                    .align(Alignment.CenterVertically)
            )
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
    )
}

@Composable
private fun BottomBar(
    vm: ReadViewModel,
    onPageSelectClick: () -> Unit,
    onSettingsClick: () -> Unit,
    chapterChange: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BottomAppBar(
        modifier = modifier,
        windowInsets = WindowInsets(0.dp),
        containerColor = Color.Transparent
    ) {
        val prevShown = vm.currentChapter < vm.list.lastIndex
        val nextShown = vm.currentChapter > 0

        AnimatedVisibility(
            visible = prevShown && vm.list.size > 1,
            enter = expandHorizontally(expandFrom = Alignment.Start),
            exit = shrinkHorizontally(shrinkTowards = Alignment.Start)
        ) {
            PreviousButton(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .weight(
                        when {
                            prevShown && nextShown -> 8f / 3f
                            prevShown -> 4f
                            else -> 4f
                        }
                    ),
                previousChapter = chapterChange,
                vm = vm
            )
        }

        GoBackButton(
            modifier = Modifier
                .weight(
                    animateFloatAsState(
                        when {
                            prevShown && nextShown -> 8f / 3f
                            prevShown || nextShown -> 4f
                            else -> 8f
                        }, label = ""
                    ).value
                )
        )

        AnimatedVisibility(
            visible = nextShown && vm.list.size > 1,
            enter = expandHorizontally(),
            exit = shrinkHorizontally()
        ) {
            NextButton(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .weight(
                        when {
                            prevShown && nextShown -> 8f / 3f
                            nextShown -> 4f
                            else -> 4f
                        }
                    ),
                nextChapter = chapterChange,
                vm = vm
            )
        }
        //The three buttons above will equal 8f
        //So these two need to add up to 2f
        IconButton(
            onClick = onPageSelectClick,
            modifier = Modifier.weight(1f)
        ) { Icon(Icons.Default.GridOn, null) }

        IconButton(
            onClick = onSettingsClick,
            modifier = Modifier.weight(1f)
        ) { Icon(Icons.Default.Settings, null) }
    }
}

@Composable
@ExperimentalMaterial3Api
private fun WrapHeightNavigationDrawerItem(
    label: @Composable () -> Unit,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: (@Composable () -> Unit)? = null,
    badge: (@Composable () -> Unit)? = null,
    shape: Shape = CircleShape,
    colors: NavigationDrawerItemColors = NavigationDrawerItemDefaults.colors(),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    Surface(
        shape = shape,
        color = colors.containerColor(selected).value,
        modifier = modifier
            .heightIn(min = 56.dp)
            .fillMaxWidth()
            .selectable(
                selected = selected,
                onClick = onClick,
                interactionSource = interactionSource,
                role = Role.Tab,
                indication = null
            )
    ) {
        Row(
            Modifier.padding(start = 16.dp, end = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                val iconColor = colors.iconColor(selected).value
                CompositionLocalProvider(LocalContentColor provides iconColor, content = icon)
                Spacer(Modifier.width(12.dp))
            }
            Box(Modifier.weight(1f)) {
                val labelColor = colors.textColor(selected).value
                CompositionLocalProvider(LocalContentColor provides labelColor, content = label)
            }
            if (badge != null) {
                Spacer(Modifier.width(12.dp))
                val badgeColor = colors.badgeColor(selected).value
                CompositionLocalProvider(LocalContentColor provides badgeColor, content = badge)
            }
        }
    }
}

@ExperimentalAnimationApi
@Composable
private fun PageIndicator(
    currentPage: Int,
    pageCount: Int,
    modifier: Modifier = Modifier,
) {
    Text(
        "$currentPage/$pageCount",
        style = MaterialTheme.typography.bodyLarge,
        modifier = modifier
    )
}

private fun Context.dpToPx(dp: Int): Int = (dp * resources.displayMetrics.density).toInt()

private fun LazyListState.isScrolledToTheEnd() = layoutInfo.visibleItemsInfo.lastOrNull()?.index == layoutInfo.totalItemsCount - 1
private fun LazyListState.isScrolledToTheBeginning() = layoutInfo.visibleItemsInfo.firstOrNull()?.index == 0

@Composable
private fun GoBackButton(modifier: Modifier = Modifier) {
    val navController = LocalNavController.current
    OutlinedButton(
        onClick = { navController.popBackStack() },
        modifier = modifier,
        border = BorderStroke(ButtonDefaults.outlinedButtonBorder.width, MaterialTheme.colorScheme.primary)
    ) { Text(stringResource(id = R.string.goBack), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary) }
}

@Composable
private fun NextButton(
    vm: ReadViewModel,
    modifier: Modifier = Modifier,
    nextChapter: () -> Unit,
) {
    Button(
        onClick = { vm.addChapterToWatched(--vm.currentChapter, nextChapter) },
        modifier = modifier
    ) { Text(stringResource(id = R.string.loadNextChapter)) }
}

@Composable
private fun PreviousButton(
    vm: ReadViewModel,
    modifier: Modifier = Modifier,
    previousChapter: () -> Unit,
) {
    TextButton(
        onClick = { vm.addChapterToWatched(++vm.currentChapter, previousChapter) },
        modifier = modifier
    ) { Text(stringResource(id = R.string.loadPreviousChapter)) }
}

@Composable
private fun SliderSetting(
    scope: CoroutineScope,
    settingIcon: ImageVector,
    @StringRes settingTitle: Int,
    @StringRes settingSummary: Int,
    preferenceUpdate: suspend (Int) -> Unit,
    initialValue: Int,
    range: ClosedFloatingPointRange<Float>,
) {
    var sliderValue by remember { mutableFloatStateOf(initialValue.toFloat()) }

    SliderSetting(
        sliderValue = sliderValue,
        settingTitle = { Text(stringResource(id = settingTitle)) },
        settingSummary = { Text(stringResource(id = settingSummary)) },
        range = range,
        updateValue = {
            sliderValue = it
            scope.launch { preferenceUpdate(sliderValue.toInt()) }
        },
        settingIcon = { Icon(settingIcon, null) }
    )
}

@LightAndDarkPreviews
@Composable
fun LastPagePreview() {
    PreviewTheme {
        LastPageReached(
            isLoading = true,
            currentChapter = 3,
            lastChapter = 4,
            chapterName = "Name".repeat(100),
            nextChapter = {},
            previousChapter = {}
        )
    }
}