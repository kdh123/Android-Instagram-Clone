package com.dhkim.data.feed.dataSource

import android.util.Log
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.dhkim.data.feed.extension.toHomeEntity
import com.dhkim.data.feed.model.FeedDto
import com.dhkim.database.AppDatabase
import com.dhkim.database.entity.HomeFeedEntity
import com.dhkim.database.entity.HomeFeedRemoteKey
import com.google.firebase.database.DatabaseReference
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalPagingApi::class)
class HomeFeedRemoteMediator(
    private val query: DatabaseReference,
    private val database: AppDatabase
) : RemoteMediator<Int, HomeFeedEntity>() {

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, HomeFeedEntity>
    ): MediatorResult {
        return try {
            val loadKey = when (loadType) {
                LoadType.REFRESH -> null

                LoadType.APPEND -> {
                    val remoteKey = database
                        .homeFeedRemoteKeyDao()
                        .getLastKey()

                    remoteKey?.nextKey
                        ?: return MediatorResult.Success(
                            endOfPaginationReached = true
                        )
                }

                LoadType.PREPEND -> {
                    return MediatorResult.Success(
                        endOfPaginationReached = true
                    )
                }
            }

            val snapshot = if (loadKey == null) {
                query
                    .orderByKey()
                    .limitToFirst(state.config.pageSize)
                    .get()
                    .await()
            } else {
                query
                    .orderByKey()
                    .startAfter(loadKey)
                    .limitToFirst(state.config.pageSize)
                    .get()
                    .await()
            }

            val entities = snapshot.children.mapNotNull { child ->
                child.getValue(FeedDto::class.java)
                    ?.toHomeEntity()
            }.map { it.copy(imageUrls = it.imageUrls.distinct()) }

            val endOfPaginationReached = entities.size < state.config.pageSize

            database.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    database.homeFeedRemoteKeyDao().clearRemoteKeys()
                    database.feedDao().clearHomeFeeds()
                }

                val keys = entities.map {
                    HomeFeedRemoteKey(
                        feedId = it.feedId,
                        nextKey = it.feedId
                    )
                }

                database.homeFeedRemoteKeyDao().insertAll(keys)
                database.feedDao().insertHomeFeeds(entities)
                Log.i("HomeFeedRemoteMediator", "Inserted ${entities.size} entities")
            }

            MediatorResult.Success(
                endOfPaginationReached = endOfPaginationReached
            )
        } catch (e: Exception) {
            MediatorResult.Error(e)
        }
    }
}
