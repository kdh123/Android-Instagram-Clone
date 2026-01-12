package com.dhkim.data.feed.dataSource

import android.util.Log
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.dhkim.data.feed.extension.toMyFeedEntity
import com.dhkim.data.feed.model.FeedDto
import com.dhkim.database.AppDatabase
import com.dhkim.database.entity.MyFeedEntity
import com.dhkim.database.entity.MyFeedRemoteKey
import com.google.firebase.database.DatabaseReference
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalPagingApi::class)
class MyFeedRemoteMediator(
    private val feedRef: DatabaseReference,
    private val database: AppDatabase
) : RemoteMediator<Int, MyFeedEntity>() {

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, MyFeedEntity>
    ): MediatorResult {
        return try {
            val loadKey = when (loadType) {
                LoadType.REFRESH -> null

                LoadType.APPEND -> {
                    val remoteKey = database
                        .myFeedRemoteKeyDao()
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
                feedRef
                    .orderByKey()
                    .limitToFirst(state.config.pageSize)
                    .get()
                    .await()
            } else {
                feedRef
                    .orderByKey()
                    .startAfter(loadKey)
                    .limitToFirst(state.config.pageSize)
                    .get()
                    .await()
            }

            val entities = snapshot.children.mapNotNull { child ->
                child.getValue(FeedDto::class.java)
                    ?.toMyFeedEntity()
            }.map { it.copy(imageUrls = it.imageUrls.distinct()) }

            val endOfPaginationReached = entities.size < state.config.pageSize

            database.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    database.myFeedRemoteKeyDao().clearRemoteKeys()
                    database.myFeedDao().clearMyFeeds()
                }

                val keys = entities.map {
                    MyFeedRemoteKey(
                        feedId = it.feedId,
                        nextKey = it.feedId
                    )
                }

                database.myFeedRemoteKeyDao().insertAll(keys)
                database.myFeedDao().insertMyFeeds(entities)
                Log.i("MyFeedRemoteMediator", "Inserted ${entities.size} entities")
            }

            MediatorResult.Success(
                endOfPaginationReached = endOfPaginationReached
            )
        } catch (e: Exception) {
            MediatorResult.Error(e)
        }
    }
}
