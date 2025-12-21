package com.dhkim.domain.user.exception

class NouUserFoundException(override val message: String = "No User Found") : Exception(message)