package com.dhkim.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "feed_upload_status")
data class FeedUploadStatusEntity(
    @PrimaryKey val feedId: String,
    @ColumnInfo(name = "thumbnail", typeAffinity = ColumnInfo.BLOB) val thumbnail: ByteArray,
    @ColumnInfo(name = "imageUrls") val imageUrls: List<String>,
    @ColumnInfo(name = "uploadStatus") val uploadStatus: Int,
    @ColumnInfo(name = "shouldUpload") val shouldUpload: Boolean,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as FeedUploadStatusEntity
        if (feedId != other.feedId) return false
        if (uploadStatus != other.uploadStatus) return false
        if (!thumbnail.contentEquals(other.thumbnail)) return false
        return true
    }

    override fun hashCode(): Int {
        var result = feedId.hashCode()
        result = 31 * result + uploadStatus
        result = 31 * result + thumbnail.contentHashCode()
        return result
    }
}