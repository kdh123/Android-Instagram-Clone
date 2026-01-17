package com.dhkim.home

import com.dhkim.domain.user.model.User
import com.dhkim.feed.common.UserItem

fun User.toUserItem(): UserItem {
    return UserItem(
        id = id,
        name = name,
        profileImageUrl = profileUrl,
        isFollowing = false
    )
}