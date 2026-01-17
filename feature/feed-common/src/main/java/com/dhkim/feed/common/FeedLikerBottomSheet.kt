package com.dhkim.feed.common

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.dhkim.designsystem.InstagramTheme
import com.dhkim.ui.LoadingSpinner
import com.skydoves.landscapist.glide.GlideImage
import kotlinx.coroutines.flow.flowOf

@Composable
fun FeedLikerBottomSheet(
    users: LazyPagingItems<UserItem>
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
    ) {
        items(
            count = users.itemCount + 3,
            key = { index ->
                if (index < 3) {
                    "$index"
                } else {
                    users.itemKey { it.id }.invoke(index - 3)
                }
            },
            contentType = { index ->
                when (index) {
                    0 -> "title"
                    1 -> "search"
                    2 -> "loading"
                    else -> users.itemContentType { "user_item" }.invoke(index - 3)
                }
            }
        ) { index ->
            when (index) {
                0 -> {
                    Text(
                        text = stringResource(R.string.like),
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                }

                1 -> {
                    InstagramSearchBar(
                        searchQuery = "검색",
                        onSearchQueryChange = {}
                    )
                }

                2 -> {
                    if (users.loadState.refresh is LoadState.Loading) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                        ) {
                            LoadingSpinner(
                                modifier = Modifier
                                    .align(Alignment.Center)
                            )
                        }
                    }
                }

                else -> {
                    val user = users[index - 3] ?: return@items
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp)
                    ) {
                        GlideImage(
                            imageModel = { user.profileImageUrl },
                            previewPlaceholder = painterResource(R.drawable.ic_dummy_background),
                            modifier = Modifier
                                .clip(CircleShape)
                                .size(42.dp)
                        )
                        Text(
                            text = user.name,
                            style = InstagramTheme.typography.bodyMedium,
                            modifier = Modifier
                                .padding(start = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun InstagramSearchBar(
    modifier: Modifier = Modifier,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit
) {
    TextField(
        value = searchQuery,
        onValueChange = onSearchQueryChange,
        modifier = modifier
            .fillMaxWidth()
            .padding(10.dp),
        placeholder = {
            Text(
                text = stringResource(R.string.search),
                style = InstagramTheme.typography.bodyMedium,
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "",
                tint = Color.Gray,
                modifier = Modifier
                    .size(24.dp)
            )
        },
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color(0xFFF2F2F2),
            unfocusedContainerColor = InstagramTheme.colors.secondary,
            disabledContainerColor = Color(0xFFF2F2F2),
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
        ),
        shape = RoundedCornerShape(12.dp),
        singleLine = true,
        textStyle = InstagramTheme.typography.bodyMedium
    )
}

@FeedLikerBottomSheetPreviews
@Composable
private fun FeedLikerBottomSheetPreview() {
    val users = mutableListOf<UserItem>().apply {
        repeat(10) {
            add(
                UserItem(
                    id = "userId$it",
                    name = "Tester$it",
                    profileImageUrl = "",
                    isFollowing = false
                )
            )
        }
    }
    val mockUsersFlow = flowOf(PagingData.from(users))
    val items = mockUsersFlow.collectAsLazyPagingItems()

    InstagramTheme {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.background
        ) {
            FeedLikerBottomSheet(items)
        }
    }
}

@Preview(showBackground = false, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(showBackground = false, uiMode = Configuration.UI_MODE_NIGHT_NO)
annotation class FeedLikerBottomSheetPreviews