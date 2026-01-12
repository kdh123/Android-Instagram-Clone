package com.dhkim.domain.user.exception

class NoUserFoundException(override val message: String = "No User Found") : Exception(message)