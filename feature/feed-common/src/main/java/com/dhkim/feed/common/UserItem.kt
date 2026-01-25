package com.dhkim.feed.common

import androidx.compose.runtime.Immutable

@Immutable
data class UserItem(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val profileImageUrl: String = "",
    val isFollowing: Boolean = false,
)
