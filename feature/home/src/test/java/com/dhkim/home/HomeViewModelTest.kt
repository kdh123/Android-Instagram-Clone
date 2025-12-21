package com.dhkim.home

import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import androidx.paging.testing.asSnapshot
import app.cash.turbine.test
import com.dhkim.domain.feed.model.Feed
import com.dhkim.domain.feed.repository.FeedRepository
import com.dhkim.domain.feed.useCase.GetFeedsUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val feedRepository = mockk<FeedRepository>()
    private val getFeedsUseCase = GetFeedsUseCase(feedRepository)

    private lateinit var viewModel: HomeViewModel

    private val fakeFeeds = List(10) {
        Feed(
            feedId = it.toString(),
            userId = "user1",
            userName = "Tester",
            userProfileImage = "",
            imageUrls = listOf("url1"),
            caption = "Test Caption",
            timestamp = 123456789L,
            likeCount = 10,
            commentCount = 5
        )
    }

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @Test
    fun whenFetchFeedsSucceeds_emitsSuccessfulPagingData() = runTest {
        coEvery { feedRepository.getFeeds() } returns flowOf(PagingData.from(fakeFeeds))

        viewModel = HomeViewModel(getFeedsUseCase)
        viewModel.feeds.test {
            assertEquals(fakeFeeds, flowOf(awaitItem()).asSnapshot())
        }
    }

    @Test
    fun whenFetchFeedsFails_emitsErrorPagingData() = runTest {
        val exception = Exception("Network error occurred!")
        val errorPagingData = PagingData.from(
            data = listOf<Feed>(),
            sourceLoadStates = LoadStates(
                refresh = LoadState.Error(exception),
                prepend = LoadState.NotLoading(endOfPaginationReached = true),
                append = LoadState.NotLoading(endOfPaginationReached = true)
            )
        )
        coEvery { feedRepository.getFeeds() } returns flowOf(errorPagingData)

        viewModel = HomeViewModel(getFeedsUseCase)
        viewModel.feeds.test {
            assertEquals(flowOf(errorPagingData).asSnapshot(), flowOf(awaitItem()).asSnapshot())
        }
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
}
