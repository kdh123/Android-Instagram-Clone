package com.dhkim.data.login.model

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class UserDto(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val profileUrl: String = ""
)
