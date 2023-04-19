package com.programmersbox.uiviews.lists

import android.content.Intent
import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DismissDirection
import androidx.compose.material3.DismissValue
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SwipeToDismiss
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.contentColorFor
import androidx.compose.material3.rememberDismissState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.github.skydoves.colorpicker.compose.AlphaSlider
import com.github.skydoves.colorpicker.compose.BrightnessSlider
import com.github.skydoves.colorpicker.compose.ColorEnvelope
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import com.programmersbox.favoritesdatabase.CustomList
import com.programmersbox.favoritesdatabase.CustomListInfo
import com.programmersbox.favoritesdatabase.ListDao
import com.programmersbox.favoritesdatabase.toDbModel
import com.programmersbox.favoritesdatabase.toItemModel
import com.programmersbox.sharedutils.MainLogo
import com.programmersbox.uiviews.R
import com.programmersbox.uiviews.utils.BackButton
import com.programmersbox.uiviews.utils.Cached
import com.programmersbox.uiviews.utils.ComposableUtils
import com.programmersbox.uiviews.utils.InsetSmallTopAppBar
import com.programmersbox.uiviews.utils.LoadingDialog
import com.programmersbox.uiviews.utils.LocalCustomListDao
import com.programmersbox.uiviews.utils.LocalGenericInfo
import com.programmersbox.uiviews.utils.LocalNavController
import com.programmersbox.uiviews.utils.OtakuScaffold
import com.programmersbox.uiviews.utils.Screen
import com.programmersbox.uiviews.utils.animate
import com.programmersbox.uiviews.utils.components.BottomSheetDeleteScaffold
import com.programmersbox.uiviews.utils.dispatchIo
import com.programmersbox.uiviews.utils.getSystemDateTimeFormat
import com.programmersbox.uiviews.utils.navigateToDetails
import com.programmersbox.uiviews.utils.toComposeColor
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun OtakuCustomListScreen(
    logo: MainLogo,
    listDao: ListDao = LocalCustomListDao.current,
    vm: OtakuCustomListViewModel = viewModel { OtakuCustomListViewModel(listDao, createSavedStateHandle()) }
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val navController = LocalNavController.current
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    val customItem = vm.customItem
    var deleteList by remember { mutableStateOf(false) }

    if (deleteList) {
        var listName by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { deleteList = false },
            title = { Text(stringResource(R.string.delete_list_title)) },
            text = {
                Column {
                    Text(stringResource(R.string.are_you_sure_delete_list))
                    Text(customItem?.item?.name.orEmpty())
                    TextField(
                        value = listName,
                        onValueChange = { listName = it },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            vm.deleteAll()
                            navController.popBackStack()
                            deleteList = false
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    enabled = listName == customItem?.item?.name
                ) { Text(stringResource(id = R.string.confirm)) }
            },
            dismissButton = { TextButton(onClick = { deleteList = false }) { Text(stringResource(id = R.string.cancel)) } }
        )
    }

    var showAdd by remember { mutableStateOf(false) }

    if (showAdd) {
        var name by remember { mutableStateOf(customItem?.item?.name.orEmpty()) }
        AlertDialog(
            onDismissRequest = { showAdd = false },
            title = { Text(stringResource(R.string.update_list_name_title)) },
            text = {
                TextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.list_name)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            vm.rename(name)
                            showAdd = false
                        }
                    },
                    enabled = name.isNotEmpty()
                ) { Text(stringResource(id = R.string.confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { showAdd = false }) { Text(stringResource(id = R.string.cancel)) }
            }
        )
    }

    var showLoadingDialog by remember { mutableStateOf(false) }

    LoadingDialog(
        showLoadingDialog = showLoadingDialog,
        onDismissRequest = { showLoadingDialog = false }
    )

    BottomSheetDeleteScaffold(
        listOfItems = vm.items,
        multipleTitle = stringResource(R.string.remove_items),
        onRemove = { vm.removeItem(it) },
        onMultipleRemove = { it.forEach { i -> vm.removeItem(i) } },
        topBar = {
            Column {
                InsetSmallTopAppBar(
                    title = { Text(customItem?.item?.name.orEmpty()) },
                    navigationIcon = { BackButton() },
                    actions = {
                        IconButton(
                            onClick = {
                                context.startActivity(
                                    Intent.createChooser(
                                        Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_TEXT, customItem?.list.orEmpty().joinToString("\n") { "${it.title} - ${it.url}" })
                                            putExtra(Intent.EXTRA_TITLE, customItem?.item?.name.orEmpty())
                                        },
                                        context.getString(R.string.share_item, customItem?.item?.name.orEmpty())
                                    )
                                )
                            }
                        ) { Icon(Icons.Default.Share, null) }
                        IconButton(
                            onClick = {
                                customItem?.item?.uuid?.let { Screen.CustomListEditorScreen.navigate(navController, it) }
                                /*showAdd = true*/
                            }
                        ) { Icon(Icons.Default.Edit, null) }
                        IconButton(onClick = { deleteList = true }) { Icon(Icons.Default.DeleteForever, null, tint = Color.Red) }
                        Text("(${customItem?.list.orEmpty().size})")
                    },
                    scrollBehavior = scrollBehavior
                )

                SearchBar(
                    query = vm.searchQuery,
                    onQueryChange = vm::setQuery,
                    onSearch = { vm.searchBarActive = false },
                    active = vm.searchBarActive,
                    onActiveChange = { vm.searchBarActive = it },
                    placeholder = { Text(stringResource(id = R.string.search)) },
                    trailingIcon = {
                        IconButton(onClick = { vm.setQuery("") }) {
                            Icon(Icons.Default.Cancel, null)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        itemsIndexed(vm.items) { index, item ->
                            ListItem(
                                headlineContent = { Text(item.title) },
                                leadingContent = { Icon(Icons.Filled.Search, contentDescription = null) },
                                modifier = Modifier.clickable {
                                    vm.setQuery(item.title)
                                    vm.searchBarActive = false
                                }
                            )
                            if (index != vm.items.lastIndex) {
                                Divider()
                            }
                        }
                    }
                }
            }
        },
        itemUi = { item ->
            Row {
                val logoDrawable = remember { AppCompatResources.getDrawable(context, logo.logoId) }
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(item.imageUrl)
                        .lifecycle(LocalLifecycleOwner.current)
                        .crossfade(true)
                        .build(),
                    placeholder = rememberDrawablePainter(logoDrawable),
                    error = rememberDrawablePainter(logoDrawable),
                    contentScale = ContentScale.Crop,
                    contentDescription = item.title,
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .size(ComposableUtils.IMAGE_WIDTH, ComposableUtils.IMAGE_HEIGHT)
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 16.dp, top = 4.dp)
                ) {
                    Text(item.source, style = MaterialTheme.typography.labelMedium)
                    Text(item.title, style = MaterialTheme.typography.titleSmall)
                    Text(item.description, style = MaterialTheme.typography.bodyMedium, maxLines = 3)
                }
            }
        }
    ) { padding, ts ->
        LazyColumn(
            contentPadding = padding,
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(vertical = 4.dp),
        ) {
            items(ts) { item ->
                CustomItem(
                    item = item,
                    logo = logo,
                    showLoadingDialog = { showLoadingDialog = it },
                    onDelete = { vm.removeItem(it) },
                    modifier = Modifier.animateItemPlacement()
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CustomItem(
    item: CustomListInfo,
    logo: MainLogo,
    onDelete: (CustomListInfo) -> Unit,
    showLoadingDialog: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val genericInfo = LocalGenericInfo.current
    val navController = LocalNavController.current
    val context = LocalContext.current
    var showPopup by remember { mutableStateOf(false) }

    if (showPopup) {
        val onDismiss = { showPopup = false }

        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(stringResource(R.string.removeNoti, item.title)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDismiss()
                        onDelete(item)
                    }
                ) { Text(stringResource(R.string.yes)) }
            },
            dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.no)) } }
        )
    }

    val dismissState = rememberDismissState(
        confirmValueChange = {
            if (it == DismissValue.DismissedToStart) {
                showPopup = true
            }
            false
        }
    )

    SwipeToDismiss(
        modifier = modifier,
        state = dismissState,
        directions = setOf(DismissDirection.EndToStart),
        background = {
            val color by animateColorAsState(
                when (dismissState.targetValue) {
                    DismissValue.Default -> Color.Transparent
                    DismissValue.DismissedToStart -> Color.Red
                    DismissValue.DismissedToEnd -> Color.Red
                }
            )
            val scale by animateFloatAsState(if (dismissState.targetValue == DismissValue.Default) 0.75f else 1f)

            Box(
                Modifier
                    .fillMaxSize()
                    .background(color)
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = null,
                    modifier = Modifier.scale(scale),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        dismissContent = {
            ElevatedCard(
                onClick = {
                    genericInfo
                        .toSource(item.source)
                        ?.let { source ->
                            Cached.cache[item.url]?.let {
                                flow {
                                    emit(
                                        it
                                            .toDbModel()
                                            .toItemModel(source)
                                    )
                                }
                            } ?: source.getSourceByUrlFlow(item.url)
                        }
                        ?.dispatchIo()
                        ?.onStart { showLoadingDialog(true) }
                        ?.onEach {
                            showLoadingDialog(false)
                            navController.navigateToDetails(it)
                        }
                        ?.onCompletion { showLoadingDialog(false) }
                        ?.launchIn(scope)
                },
                modifier = Modifier
                    .height(ComposableUtils.IMAGE_HEIGHT)
                    .padding(horizontal = 4.dp)
            ) {
                Row {
                    val logoDrawable = remember { AppCompatResources.getDrawable(context, logo.logoId) }
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(item.imageUrl)
                            .lifecycle(LocalLifecycleOwner.current)
                            .crossfade(true)
                            .build(),
                        placeholder = rememberDrawablePainter(logoDrawable),
                        error = rememberDrawablePainter(logoDrawable),
                        contentScale = ContentScale.Crop,
                        contentDescription = item.title,
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .size(ComposableUtils.IMAGE_WIDTH, ComposableUtils.IMAGE_HEIGHT)
                    )

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 16.dp, top = 4.dp)
                    ) {
                        Text(item.source, style = MaterialTheme.typography.labelMedium)
                        Text(item.title, style = MaterialTheme.typography.titleSmall)
                        Text(item.description, style = MaterialTheme.typography.bodyMedium, maxLines = 3)
                    }
                }
            }
        }
    )
}

class CustomListEditorViewModel(
    private val listDao: ListDao,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val uuid = savedStateHandle.get<String>("uuid")?.let(UUID::fromString)

    var customItem: CustomList? by mutableStateOf(null)
        private set

    init {
        uuid?.let(listDao::getCustomListItemFlow)
            ?.onEach { customItem = it }
            ?.launchIn(viewModelScope)
    }

    fun save(name: String, containerColor: Color) {
        viewModelScope.launch {
            customItem?.item?.copy(
                name = name,
                containerColor = containerColor.toString()
            )?.let { listDao.updateFullList(it) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CustomListEditor(
    listDao: ListDao = LocalCustomListDao.current,
    vm: CustomListEditorViewModel = viewModel { CustomListEditorViewModel(listDao, createSavedStateHandle()) }
) {
    val navController = LocalNavController.current
    val surface = vm.customItem?.item?.containerColor?.toColorInt()?.toComposeColor() ?: MaterialTheme.colorScheme.surface
    val controller = rememberColorPickerController()
    var containerColor by remember { mutableStateOf(surface) }
    val context = LocalContext.current
    var isLeaving by remember { mutableStateOf(false) }
    var name by remember(vm.customItem) { mutableStateOf(vm.customItem?.item?.name.orEmpty()) }

    /*BackHandler(true) {
        isLeaving = true
    }*/

    if (isLeaving) {
        AlertDialog(
            onDismissRequest = { isLeaving = false },
            title = { Text("Save?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        isLeaving = false
                        vm.save(name, containerColor)
                        navController.popBackStack()
                    }
                ) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { navController.popBackStack() }) { Text("Leave Without Saving") } }
        )
    }

    OtakuScaffold(
        topBar = {
            InsetSmallTopAppBar(
                title = { Text("Edit ${vm.customItem?.item?.name}") },
                navigationIcon = { IconButton(onClick = { isLeaving = true }) { Icon(Icons.Default.ArrowBack, null) } },
            )
        },
        bottomBar = {
            BottomAppBar {
                OutlinedButton(
                    onClick = { isLeaving = true },
                    shape = CircleShape.copy(topEnd = CornerSize(0f), bottomEnd = CornerSize(0f)),
                    modifier = Modifier.weight(1f)
                ) { Text("Cancel") }
                OutlinedButton(
                    onClick = {
                        containerColor = surface
                        name = vm.customItem?.item?.name.orEmpty()
                    },
                    shape = RectangleShape,
                    modifier = Modifier.weight(1f)
                ) { Text("Reset") }
                OutlinedButton(
                    onClick = { isLeaving = true },
                    shape = CircleShape.copy(topStart = CornerSize(0f), bottomStart = CornerSize(0f)),
                    modifier = Modifier.weight(1f)
                ) { Text("Save") }
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding)
        ) {

            HsvColorPicker(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .padding(10.dp),
                controller = controller,
                onColorChanged = { colorEnvelope: ColorEnvelope -> containerColor = colorEnvelope.color },
                initialColor = surface
            )

            AlphaSlider(
                controller = controller,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
                    .height(35.dp)
                    .align(Alignment.CenterHorizontally),
            )

            BrightnessSlider(
                controller = controller,
                initialColor = surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
                    .height(35.dp)
                    .align(Alignment.CenterHorizontally),
            )

            Text(
                "Preview",
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            vm.customItem?.let {

                @Composable
                fun textColor(bgColor: Color): State<Color> {
                    return if (containerColor.luminance() > .5) {
                        bgColor
                    } else {
                        contentColorFor(bgColor)
                    }.animate()
                }

                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = containerColor.animate().value,
                    )
                ) {
                    val time = remember { context.getSystemDateTimeFormat().format(it.item.time) }
                    ListItem(
                        colors = ListItemDefaults.colors(
                            containerColor = containerColor.animate().value,
                            headlineColor = textColor(MaterialTheme.colorScheme.surface).value,
                            overlineColor = textColor(MaterialTheme.colorScheme.surfaceVariant).value,
                            supportingColor = textColor(MaterialTheme.colorScheme.surfaceVariant).value,
                            trailingIconColor = textColor(MaterialTheme.colorScheme.surfaceVariant).value
                        ),
                        overlineContent = { Text(stringResource(id = R.string.custom_list_updated_at, time)) },
                        trailingContent = { Text("(${it.list.size})") },
                        headlineContent = { Text(it.item.name) },
                        supportingContent = {
                            Column {
                                it.list.take(3).forEach { info ->
                                    Text(info.title, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                            }
                        }
                    )
                }
            }

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(id = R.string.list_name)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}