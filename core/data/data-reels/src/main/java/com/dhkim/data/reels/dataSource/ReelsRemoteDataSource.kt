package com.dhkim.data.reels.dataSource

import com.dhkim.common.retryWithDelay
import com.dhkim.data.reels.model.ReelsDto
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ReelsRemoteDataSource @Inject constructor(
    private val firebaseDatabase: FirebaseDatabase,
    private val storage: FirebaseStorage
) {

    private val storageRef = storage.reference

    fun getReels(): Flow<List<ReelsDto>> {
        return flow {
            val reelsNames = listOf("sample1.mp4, sample2.mp4, sample3.mp4")
            val reelsUrls = reelsNames.map {
                ReelsDto(
                    id = "${System.currentTimeMillis()}",
                    url = "${storageRef.child(it).downloadUrl.await()}"
                )
            }
            emit(reelsUrls)
        }.retryWithDelay()
    }
}