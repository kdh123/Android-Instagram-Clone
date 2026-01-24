package com.dhkim.data.feed.repository

import androidx.paging.PagingData
import androidx.paging.map
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.dhkim.data.feed.dataSource.FeedLocalDataSource
import com.dhkim.data.feed.dataSource.FeedRemoteDataSource
import com.dhkim.data.feed.extension.toComment
import com.dhkim.data.feed.extension.toEntity
import com.dhkim.data.feed.extension.toFeed
import com.dhkim.data.feed.extension.toFeedUploadStatus
import com.dhkim.data.feed.extension.toHiddenFeed
import com.dhkim.data.feed.extension.toLikeFeed
import com.dhkim.data.feed.extension.toUser
import com.dhkim.data.feed.extension.toUserDto
import com.dhkim.data.feed.extension.toMyFeedEntity
import com.dhkim.data.feed.extension.toReply
import com.dhkim.data.feed.work.FeedLikeSyncWorker
import com.dhkim.database.entity.LikeEntity
import com.dhkim.domain.feed.model.Comment
import com.dhkim.domain.feed.model.Feed
import com.dhkim.domain.feed.model.FeedUploadStatus
import com.dhkim.domain.feed.model.HiddenFeed
import com.dhkim.domain.feed.model.LikeFeed
import com.dhkim.domain.feed.model.Reply
import com.dhkim.domain.feed.repository.FeedRepository
import com.dhkim.domain.user.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import java.io.File
import javax.inject.Inject

class FeedRepositoryImpl @Inject constructor(
    private val localDataSource: FeedLocalDataSource,
    private val remoteDataSource: FeedRemoteDataSource,
    private val workManager: WorkManager
) : FeedRepository {

    override fun getMyFeeds(): Flow<Set<Feed>> {
        return localDataSource.getMyFeeds().map { entities ->
            entities.map { it.toFeed() }.toSet()
        }
    }

    override fun getHomeFeeds(): Flow<PagingData<Feed>> {
        return remoteDataSource.getHomeFeed().map { feeds ->
            feeds.map { it.toFeed() }
        }
    }

    override fun uploadFeed(feed: Feed, userId: String): Flow<Unit> {
        return flow {
            remoteDataSource.uploadFeed(feed, userId).first()
            localDataSource.updateMyFeed(feed.toMyFeedEntity())
            emit(Unit)
        }
    }

    override fun uploadImage(storagePath: String, file: File): Flow<String> {
        return remoteDataSource.uploadImage(storagePath, file)
    }

    override fun getFeedUploadStatuses(): Flow<List<FeedUploadStatus>> {
        return localDataSource.getFeedUploadStatuses().map { statuses ->
            statuses.map { it.toFeedUploadStatus() }
        }
    }

    override fun getFeedUploadStatus(feedId: String): Flow<FeedUploadStatus?> {
        return localDataSource.getFeedUploadStatus(feedId).map { it?.toFeedUploadStatus() }
    }

    override suspend fun insertFeedUploadStatus(feedUploadStatus: FeedUploadStatus) {
        localDataSource.insertFeedUploadStatus(feedUploadStatus.toEntity())
    }

    override suspend fun deleteFeedUploadStatus(feedId: String) {
        localDataSource.deleteFeedUploadStatus(feedId)
    }

    override suspend fun clearFeedUploadStatuses() {
        localDataSource.clearFeedUploadStatuses()
    }

    override fun getHiddenFeeds(): Flow<Set<HiddenFeed>> {
        return localDataSource.getHiddenFeeds().map { hiddenFeeds ->
            hiddenFeeds.map { it.toHiddenFeed() }.toSet()
        }
    }

    override fun isHidden(feedId: String): Flow<Boolean> {
        return localDataSource.isHidden(feedId)
    }

    override suspend fun syncHiddenFeeds(userId: String) {
        val hiddenFeeds = remoteDataSource.getHiddenFeeds(userId).map { it.toHiddenFeed() }
        if (hiddenFeeds.isNotEmpty()) {
            localDataSource.insertAllHiddenFeeds(hiddenFeeds.map { it.toEntity() })
        }
    }

    override suspend fun hideFeed(userId: String, hiddenFeed: HiddenFeed) {
        localDataSource.insertHiddenFeed(hiddenFeed.toEntity())
        remoteDataSource.hideFeed(userId, hiddenFeed.feedId).first()
    }

    override suspend fun unhideFeed(userId: String, feedId: String) {
        localDataSource.unhideFeed(feedId)
        remoteDataSource.unhideFeed(userId, feedId).first()
    }

    override suspend fun clearHiddenFeeds() {
        localDataSource.clearHiddenFeeds()
    }

    override suspend fun toggleLikeCountVisibility(feedId: String, userId: String, isVisible: Boolean) {
        val myFeed = localDataSource.getMyFeed(feedId).first()
        val homeFeed = localDataSource.getHomeFeed(feedId).first()

        remoteDataSource.toggleLikeCountVisibility(feedId, userId, isVisible = !isVisible)
        localDataSource.updateHomeFeed(homeFeed?.copy(isLikeCountVisible = !isVisible))
        localDataSource.updateMyFeed(myFeed?.copy(isLikeCountVisible = !isVisible))
    }

    override suspend fun toggleLike(feedId: String, userId: String, userName: String, isLiked: Boolean) {
        enqueueLikeWorker(feedId)

        val homeFeed = localDataSource.getHomeFeed(feedId).first() ?: return
        val likeCount = homeFeed.likeCount
        if (isLiked) {
            val (representativeLikerId, representativeLikeName) = if (likeCount <= 1) {
                "" to ""
            } else {
                homeFeed.representativeLikeId to homeFeed.representativeLikeName
            }
            localDataSource.deleteLike(feedId, userId)
            localDataSource.updateHomeFeed(
                homeFeed.copy(
                    likeCount = (likeCount - 1).let { if (it < 0) 0 else it },
                    representativeLikeId = representativeLikerId,
                    representativeLikeName = representativeLikeName
                )
            )
        } else {
            val (representativeLikerId, representativeLikeName) = if (likeCount >= 1) {
                homeFeed.representativeLikeId to homeFeed.representativeLikeName
            } else {
                userId to userName
            }
            localDataSource.insertLike(LikeEntity(feedId, userId))
            localDataSource.updateHomeFeed(
                homeFeed.copy(
                    likeCount = likeCount + 1,
                    representativeLikeId = representativeLikerId,
                    representativeLikeName = representativeLikeName
                )
            )
        }
    }

    override suspend fun remoteToggleLike(feedId: String, user: User) {
        val userId = user.id
        val isLiked = localDataSource.observeIsLiked(feedId, userId).first()
        val likeUserDto = user.toUserDto()
        remoteDataSource.toggleLike(feedId, likeUserDto, isLiked)
    }

    override suspend fun syncLikeFeeds(userId: String) {
        val likeFeeds = remoteDataSource.getLikesByUser(userId).map { it.toLikeFeed() }
        if (likeFeeds.isNotEmpty()) {
            localDataSource.insertAllLike(likeFeeds.map { it.toEntity() })
        }
    }

    override fun getAllLikedFeeds(userId: String): Flow<Set<LikeFeed>> {
        return localDataSource.getAllLikedFeed(userId).map { feeds ->
            feeds.map { it.toLikeFeed() }.toSet()
        }
    }

    override fun getLikeFeed(feedId: String, userId: String): Flow<LikeFeed?> {
        return localDataSource.getLikeFeed(feedId, userId).map { it?.toLikeFeed() }
    }

    override suspend fun updateLikeFeed(likeFeed: LikeFeed) {
        localDataSource.insertLike(likeFeed.toEntity())
    }

    override suspend fun getUnSyncedLikes(): List<LikeFeed> {
        return localDataSource.getUnSyncedLikes().map { it.toLikeFeed() }
    }

    override suspend fun toggleEnableComment(feedId: String, userId: String, isEnabled: Boolean) {
        val myFeed = localDataSource.getMyFeed(feedId).first()
        val homeFeed = localDataSource.getHomeFeed(feedId).first()

        remoteDataSource.toggleEnableComment(feedId, userId, isEnabled = !isEnabled)
        localDataSource.updateHomeFeed(homeFeed?.copy(isCommentEnabled = !isEnabled))
        localDataSource.updateMyFeed(myFeed?.copy(isCommentEnabled = !isEnabled))
    }

    override fun getLikeUsers(feedId: String): Flow<PagingData<User>> {
        return remoteDataSource.getLikeUsers(feedId).map { pagingData ->
            pagingData.map { it.toUser() }
        }
    }

    override fun getComments(feedId: String): Flow<PagingData<Comment>> {
        return remoteDataSource.getComments(feedId).map { pagingData ->
            pagingData.map { it.toComment() }
        }
    }

    override suspend fun addComment(feedId: String, user: User, comment: String): Comment {
        val addedComment = remoteDataSource.addComment(feedId, user.toUserDto(), comment).toComment()
        val currentHomeFeed = localDataSource.getHomeFeed(feedId).first()
        if (currentHomeFeed != null) {
            val commentCount = currentHomeFeed.commentCount
            localDataSource.updateHomeFeed(currentHomeFeed.copy(commentCount = commentCount + 1))
        }
        return addedComment
    }

    override suspend fun deleteComment(feedId: String, commentId: String) {
        val remainingReplyCount = remoteDataSource.deleteComment(feedId, commentId).value
        val currentHomeFeed = localDataSource.getHomeFeed(feedId).first()
        if (currentHomeFeed != null) {
            localDataSource.updateHomeFeed(currentHomeFeed.copy(commentCount = remainingReplyCount))
        }
    }

    override fun getReplies(commentId: String): Flow<List<Reply>> {
        return remoteDataSource.getReplies(commentId).map { replies ->
            replies.map { it.toReply() }
        }
    }

    override suspend fun replyComment(
        feedId: String,
        commentId: String,
        user: User,
        comment: String
    ): Reply {
        return remoteDataSource.replyComment(feedId, commentId, user.toUserDto(), comment).toReply()
    }

    override suspend fun deleteReply(feedId: String, replyId: String): Reply {
        TODO()
    }

    private fun enqueueLikeWorker(feedId: String) {
        val data = workDataOf("KEY_FEED_ID" to feedId)
        val request = OneTimeWorkRequestBuilder<FeedLikeSyncWorker>()
            .setInputData(data)
            .setConstraints(Constraints(requiredNetworkType = NetworkType.CONNECTED))
            .build()

        workManager.enqueueUniqueWork(
            "sync_like_$feedId",
            ExistingWorkPolicy.REPLACE,
            request
        )
    }
}