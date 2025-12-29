package com.dhkim.domain.feed.exception

class FeedContentUploadFailException(override val message: String = "feed content upload failed") : Exception(message)