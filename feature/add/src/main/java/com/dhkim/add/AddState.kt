package com.dhkim.add

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue

@Stable
class AddState(
    val galleryListState: LazyGridState,
    val galleryScrollState: ScrollState
) {

    val isAtTop by derivedStateOf {
        galleryListState.firstVisibleItemIndex >= 1
    }
}

@Composable
fun rememberAddState(
    galleryListState: LazyGridState = rememberLazyGridState(),
    galleryScrollState: ScrollState = rememberScrollState()
): AddState {
    return AddState(
        galleryListState = galleryListState,
        galleryScrollState = galleryScrollState
    )
}
