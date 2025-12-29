package com.dhkim.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "feed_upload_status")
data class FeedUploadStatusEntity(
    @PrimaryKey val feedId: String,
    @ColumnInfo(name = "thumbnail", typeAffinity = ColumnInfo.BLOB) val thumbnail: ByteArray,
    @ColumnInfo(name = "imageUrls") val imageUrls: List<String>,
    // 0: loading, 1: success, 2: fail
    @ColumnInfo(name = "imageStatus") val imageStatus: Int,
    @ColumnInfo(name = "contentStatus") val contentStatus: Int,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as FeedUploadStatusEntity
        if (feedId != other.feedId) return false
        if (imageStatus != other.imageStatus) return false
        if (imageStatus != other.contentStatus) return false
        if (!thumbnail.contentEquals(other.thumbnail)) return false
        return true
    }

    override fun hashCode(): Int {
        var result = feedId.hashCode()
        result = 31 * result + imageStatus
        result = 31 * result + contentStatus
        result = 31 * result + thumbnail.contentHashCode()
        return result
    }
}