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

const val ADD_ROUTE = "add_route"

fun NavGraphBuilder.add(
    onBack: () -> Unit
) {
    composable(ADD_ROUTE) {
        val viewModel = hiltViewModel<AddViewModel>()
        val context = LocalContext.current
        val galleryImages = viewModel.galleryImages.collectAsLazyPagingItems()
        val selectImageMode by viewModel.selectImageMode.collectAsStateWithLifecycle()

        LaunchedEffect(Unit) {
            viewModel.sideEffect.collect {
                when (it) {
                    is AddSideEffect.ShowToast -> {
                        Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        AddScreen(
            galleryImages = galleryImages,
            selectImageMode = selectImageMode,
            onAction = viewModel::onAction,
            onBack = onBack
        )
    }
}