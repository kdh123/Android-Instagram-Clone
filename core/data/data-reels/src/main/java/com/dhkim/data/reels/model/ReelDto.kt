package com.dhkim.data.reels.model

import com.dhkim.domain.reels.model.Reel
import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class ReelDto(
    val reelId: String = "",
    val url: String = "",
    val caption: String = "",
    val likeCount: Int = 0,
    val commentCount: Int = 0,
    val userId: String = "",
    val userName: String = "",
    val userProfileImage: String = ""
) {
    fun toReel(): Reel {
        return Reel(
            id = reelId,
            url = url,
            caption = caption,
            likeCount = likeCount,
            commentCount = commentCount,
            userId = userId,
            userName = userName,
            userProfileImage = userProfileImage
        )
    }
}
