package com.programmersbox.uiviews.all

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerState
import com.google.accompanist.pager.rememberPagerState
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.programmersbox.models.ItemModel
import com.programmersbox.models.sourceFlow
import com.programmersbox.sharedutils.MainLogo
import com.programmersbox.uiviews.R
import com.programmersbox.uiviews.utils.*
import com.programmersbox.uiviews.utils.components.InfiniteListHandler
import kotlinx.coroutines.launch
import androidx.compose.material3.MaterialTheme as M3MaterialTheme

@OptIn(ExperimentalPagerApi::class)
@ExperimentalMaterial3Api
@ExperimentalAnimationApi
@ExperimentalMaterialApi
@ExperimentalFoundationApi
@Composable
fun AllView(
    allVm: AllViewModel,
    logo: MainLogo,
) {
    val context = LocalContext.current
    val isConnected by allVm.observeNetwork.collectAsState(initial = true)
    val source by sourceFlow.collectAsState(initial = null)

    LaunchedEffect(isConnected) {
        if (allVm.sourceList.isEmpty() && source != null && isConnected && allVm.count != 1) allVm.reset(context, source!!)
    }

    val scope = rememberCoroutineScope()
    val state = rememberLazyGridState()
    val showButton by remember { derivedStateOf { state.firstVisibleItemIndex > 0 } }
    val scrollBehaviorTop = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    val pagerState = rememberPagerState()

    OtakuScaffold(
        modifier = Modifier.nestedScroll(scrollBehaviorTop.nestedScrollConnection),
        topBar = {
            Column {
                InsetSmallTopAppBar(
                    title = { Text(stringResource(R.string.currentSource, source?.serviceName.orEmpty())) },
                    actions = {
                        AnimatedVisibility(visible = showButton) {
                            IconButton(onClick = { scope.launch { state.animateScrollToItem(0) } }) {
                                Icon(Icons.Default.ArrowUpward, null)
                            }
                        }
                    },
                    scrollBehavior = scrollBehaviorTop
                )

                TabRow(
                    // Our selected tab is our current page
                    selectedTabIndex = pagerState.currentPage,
                    // Override the indicator, using the provided pagerTabIndicatorOffset modifier
                    indicator = { tabPositions ->
                        TabRowDefaults.Indicator(
                            Modifier.pagerTabIndicatorOffset(pagerState, tabPositions)
                        )
                    }
                ) {
                    // Add tabs for all of our pages
                    Tab(
                        text = { Text(stringResource(R.string.all)) },
                        selected = pagerState.currentPage == 0,
                        onClick = { scope.launch { pagerState.animateScrollToPage(0) } },
                    )

                    Tab(
                        text = { Text(stringResource(R.string.search)) },
                        selected = pagerState.currentPage == 1,
                        onClick = { scope.launch { pagerState.animateScrollToPage(1) } },
                    )
                }
            }
        }
    ) { p1 ->
        var showBanner by remember { mutableStateOf(false) }
        M3OtakuBannerBox(
            showBanner = showBanner,
            placeholder = logo.logoId,
            modifier = Modifier.padding(p1)
        ) { itemInfo ->
            Crossfade(targetState = isConnected) { connected ->
                when (connected) {
                    false -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(p1),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Image(
                                Icons.Default.CloudOff,
                                null,
                                modifier = Modifier.size(50.dp, 50.dp),
                                colorFilter = ColorFilter.tint(M3MaterialTheme.colorScheme.onBackground)
                            )
                            Text(stringResource(R.string.you_re_offline), style = M3MaterialTheme.typography.titleLarge)
                        }
                    }
                    true -> {

                        HorizontalPager(
                            count = 2,
                            state = pagerState,
                        ) { page ->
                            when (page) {
                                0 -> AllScreen(
                                    allVm = allVm,
                                    p1 = p1,
                                    itemInfo = itemInfo,
                                    state = state,
                                    showBanner = { showBanner = it }
                                )
                                1 -> SearchScreen(
                                    allVm = allVm,
                                    p1 = p1,
                                    itemInfo = itemInfo,
                                    showBanner = { showBanner = it }
                                )
                            }

                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AllScreen(
    allVm: AllViewModel,
    p1: PaddingValues,
    itemInfo: MutableState<ItemModel?>,
    state: LazyGridState,
    showBanner: (Boolean) -> Unit
) {
    val info = LocalGenericInfo.current
    val source by sourceFlow.collectAsState(initial = null)
    val navController = LocalNavController.current
    val context = LocalContext.current
    OtakuScaffold(
        modifier = Modifier.padding(p1)
    ) { p ->
        if (allVm.sourceList.isEmpty()) {
            info.ComposeShimmerItem()
        } else {
            val refresh = rememberSwipeRefreshState(isRefreshing = allVm.isRefreshing)
            SwipeRefresh(
                modifier = Modifier.padding(p),
                state = refresh,
                onRefresh = { source?.let { allVm.reset(context, it) } }
            ) {
                info.AllListView(
                    list = allVm.sourceList,
                    listState = state,
                    favorites = allVm.favoriteList,
                    onLongPress = { item, c ->
                        itemInfo.value = if (c == ComponentState.Pressed) item else null
                        showBanner(c == ComponentState.Pressed)
                    }
                ) { navController.navigateToDetails(it) }
            }
        }

        if (source?.canScrollAll == true && allVm.sourceList.isNotEmpty()) {
            InfiniteListHandler(listState = state, buffer = info.scrollBuffer) {
                source?.let { allVm.loadMore(context, it) }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SearchScreen(
    allVm: AllViewModel,
    p1: PaddingValues = PaddingValues(0.dp),
    itemInfo: MutableState<ItemModel?>,
    showBanner: (Boolean) -> Unit
) {

    val info = LocalGenericInfo.current
    val focusManager = LocalFocusManager.current
    val searchList = allVm.searchList
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    val source by sourceFlow.collectAsState(initial = null)
    val navController = LocalNavController.current

    OtakuScaffold(
        modifier = Modifier
            .padding(p1)
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            OutlinedTextField(
                value = allVm.searchText,
                onValueChange = { allVm.searchText = it },
                label = {
                    Text(
                        stringResource(
                            R.string.searchFor,
                            source?.serviceName.orEmpty()
                        )
                    )
                },
                trailingIcon = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(searchList.size.toString())
                        IconButton(onClick = { allVm.searchText = "" }) {
                            Icon(Icons.Default.Cancel, null)
                        }
                    }
                },
                modifier = Modifier
                    .padding(5.dp)
                    .fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = {
                    focusManager.clearFocus()
                    allVm.search()
                })
            )
        }
    ) { p ->
        Box(modifier = Modifier.padding(p)) {
            SwipeRefresh(
                state = rememberSwipeRefreshState(isRefreshing = allVm.isSearching),
                onRefresh = {},
                swipeEnabled = false
            ) {
                info.SearchListView(
                    list = searchList,
                    listState = rememberLazyGridState(),
                    favorites = allVm.favoriteList,
                    onLongPress = { item, c ->
                        itemInfo.value = if (c == ComponentState.Pressed) item else null
                        showBanner(c == ComponentState.Pressed)
                    }
                ) { navController.navigateToDetails(it) }
            }
        }
    }
}

// This is because we can't get access to the library one
@OptIn(ExperimentalPagerApi::class)
fun Modifier.pagerTabIndicatorOffset(
    pagerState: PagerState,
    tabPositions: List<TabPosition>,
    pageIndexMapping: (Int) -> Int = { it },
): Modifier = layout { measurable, constraints ->
    if (tabPositions.isEmpty()) {
        // If there are no pages, nothing to show
        layout(constraints.maxWidth, 0) {}
    } else {
        val currentPage = minOf(tabPositions.lastIndex, pageIndexMapping(pagerState.currentPage))
        val currentTab = tabPositions[currentPage]
        val previousTab = tabPositions.getOrNull(currentPage - 1)
        val nextTab = tabPositions.getOrNull(currentPage + 1)
        val fraction = pagerState.currentPageOffset
        val indicatorWidth = if (fraction > 0 && nextTab != null) {
            lerp(currentTab.width, nextTab.width, fraction).roundToPx()
        } else if (fraction < 0 && previousTab != null) {
            lerp(currentTab.width, previousTab.width, -fraction).roundToPx()
        } else {
            currentTab.width.roundToPx()
        }
        val indicatorOffset = if (fraction > 0 && nextTab != null) {
            lerp(currentTab.left, nextTab.left, fraction).roundToPx()
        } else if (fraction < 0 && previousTab != null) {
            lerp(currentTab.left, previousTab.left, -fraction).roundToPx()
        } else {
            currentTab.left.roundToPx()
        }
        val placeable = measurable.measure(
            Constraints(
                minWidth = indicatorWidth,
                maxWidth = indicatorWidth,
                minHeight = 0,
                maxHeight = constraints.maxHeight
            )
        )
        layout(constraints.maxWidth, maxOf(placeable.height, constraints.minHeight)) {
            placeable.placeRelative(
                indicatorOffset,
                maxOf(constraints.minHeight - placeable.height, 0)
            )
        }
    }
}