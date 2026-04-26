package com.dhkim.data.user.extension

import com.dhkim.data.user.model.UserDto
import com.dhkim.domain.user.model.User

fun User.toUserDto(): UserDto {
    return UserDto(
        id = id,
        name = name,
        email = email,
        profileUrl = profileUrl
    )
}

fun UserDto.toUser(): User {
    return User(
        id = id,
        name = name,
        email = email,
        profileUrl = profileUrl
    )
}