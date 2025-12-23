package com.dhkim.add

import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue

@Stable
class AddState(
    val galleryListState: LazyGridState
) {

    val isAtTop by derivedStateOf {
        galleryListState.firstVisibleItemIndex == 1 && galleryListState.firstVisibleItemScrollOffset == 0
    }
}

@Composable
fun rememberAddState(
    galleryListState: LazyGridState = rememberLazyGridState()
): AddState {
    return AddState(
        galleryListState = galleryListState
    )
}