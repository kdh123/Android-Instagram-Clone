package com.dhkim.home

import com.dhkim.domain.feed.model.LikeUser
import com.dhkim.feed.common.UserItem

fun LikeUser.toUserItem(): UserItem {
    return UserItem(
        id = id,
        name = name,
        profileImageUrl = profileImageUrl,
        isFollowing = false
    )
}