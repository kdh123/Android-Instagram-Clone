package com.dhkim.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dhkim.common.handle
import com.dhkim.domain.reels.useCase.GetReelsUseCase
import com.dhkim.domain.reels.useCase.PrefetchReelsUseCase
import com.dhkim.domain.user.useCase.GetUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val getUserUseCase: GetUserUseCase,
    private val getReelsUseCase: GetReelsUseCase,
    private val prefetchReelsUseCase: PrefetchReelsUseCase
) : ViewModel() {

    val isLoggedIn: StateFlow<Boolean?> = getUserUseCase()
        .map { it != null }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )

    fun prefetchReelsUseCase() {
        viewModelScope.handle(
            block = {
                val reels = getReelsUseCase().first()
                reels.forEach { reel ->
                    try {
                        async {
                            prefetchReelsUseCase(reel.url)
                        }.await()
                    } catch (_: Exception) {

                    }
                }
            },
            onError = {

            }
        )
    }
}