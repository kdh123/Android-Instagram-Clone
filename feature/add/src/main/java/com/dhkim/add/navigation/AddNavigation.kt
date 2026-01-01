package com.dhkim.add.navigation

import android.widget.Toast
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.paging.compose.collectAsLazyPagingItems
import com.dhkim.add.AddFeedImageScreen
import com.dhkim.add.AddSideEffect
import com.dhkim.add.AddState
import com.dhkim.add.AddViewModel
import com.dhkim.add.FeedUploadScreen
import com.dhkim.ui.sharedViewModel
import kotlinx.coroutines.flow.collectLatest

const val ADD_ROUTE = "add_route"
const val ADD_IMAGE_ROUTE = "add_image_route"
const val FEED_UPLOAD_ROUTE = "feed_upload_route"

fun NavGraphBuilder.addImage(
    navController: NavHostController,
    addState: AddState,
    onBack: () -> Unit,
) {
    composable(ADD_IMAGE_ROUTE) { entry ->
        val context = LocalContext.current
        val viewModel = entry.sharedViewModel<AddViewModel>(navController)
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

                        AddSideEffect.NavigateToHome -> Unit
                        AddSideEffect.NavigateToFeedUpload -> {
                            navController.navigateToFeedUpload()
                        }
                    }
                }
        }

        AddFeedImageScreen(
            addState = addState,
            galleryImages = galleryImages,
            selectImageState = selectImageMode,
            onAction = viewModel::onAction,
            navigateToFeedUpload = navController::navigateToFeedUpload,
            onBack = onBack
        )
    }
}

fun NavGraphBuilder.feedUpload(
    navController: NavHostController,
    navigateToHome: () -> Unit,
) {
    composable(FEED_UPLOAD_ROUTE) { entry ->
        val context = LocalContext.current
        val viewModel = entry.sharedViewModel<AddViewModel>(navController)
        val feedUploadState by viewModel.feedUploadUiState.collectAsStateWithLifecycle()

        LaunchedEffect(Unit) {
            viewModel.sideEffect
                .collectLatest {
                    when (it) {
                        is AddSideEffect.ShowToast -> {
                            Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
                        }

                        is AddSideEffect.ScrollToItem -> Unit
                        AddSideEffect.NavigateToHome -> {
                            navigateToHome()
                        }

                        AddSideEffect.NavigateToFeedUpload -> Unit
                    }
                }
        }

        FeedUploadScreen(
            uiState = feedUploadState,
            onAction = viewModel::onAction,
            onBack = navController::navigateUp
        )
    }
}

fun NavController.navigateToFeedUpload() {
    navigate(FEED_UPLOAD_ROUTE)
}