package com.dhkim.add.navigation

import android.widget.Toast
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.paging.compose.collectAsLazyPagingItems
import com.dhkim.add.AddScreen
import com.dhkim.add.AddSideEffect
import com.dhkim.add.AddViewModel
import com.dhkim.add.rememberAddState
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce

const val ADD_ROUTE = "add_route"

@OptIn(FlowPreview::class)
fun NavGraphBuilder.add(
    onBack: () -> Unit
) {
    composable(ADD_ROUTE) {
        val addState = rememberAddState()
        val viewModel = hiltViewModel<AddViewModel>()
        val context = LocalContext.current
        val galleryImages = viewModel.galleryImages.collectAsLazyPagingItems()
        val selectImageMode by viewModel.selectImageState.collectAsStateWithLifecycle()

        LaunchedEffect(Unit) {
            viewModel.sideEffect
                .collectLatest {
                    when (it) {
                        is AddSideEffect.ShowToast -> {
                            Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
                        }

                        is AddSideEffect.ScrollToItem -> {
                            val scrollIndex = galleryImages.itemSnapshotList.items
                                .indexOfFirst { galleryImage -> galleryImage.uri == it.imageUri }
                                .let { index -> if (index == -1) 0 else index }
                            addState.galleryScrollState.animateScrollTo(0)
                            addState.galleryListState.animateScrollToItem(scrollIndex)
                        }
                    }
                }
        }

        AddScreen(
            addState = addState,
            galleryImages = galleryImages,
            selectImageState = selectImageMode,
            onAction = viewModel::onAction,
            onBack = onBack
        )
    }
}