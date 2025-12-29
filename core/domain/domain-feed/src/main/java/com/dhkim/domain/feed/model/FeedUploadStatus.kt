package com.dhkim.domain.feed.model

data class FeedUploadStatus(
    val feedId: String,
    val thumbnail: ByteArray,
    val imageUrls: List<String>,
    val imageStatus: UploadState,
    val contentStatus: UploadState
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FeedUploadStatus

        if (imageStatus != other.imageStatus) return false
        if (contentStatus != other.contentStatus) return false
        if (feedId != other.feedId) return false
        if (!thumbnail.contentEquals(other.thumbnail)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = imageStatus.code
        result = 31 * result + contentStatus.hashCode()
        result = 31 * result + feedId.hashCode()
        result = 31 * result + thumbnail.contentHashCode()
        return result
    }
}

enum class UploadState(val code: Int) {
    IDLE(-1),
    LOADING(0),
    SUCCESS(1),
    FAIL(2)
}