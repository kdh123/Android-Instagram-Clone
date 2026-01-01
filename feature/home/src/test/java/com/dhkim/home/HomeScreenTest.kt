package com.dhkim.home

import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.getValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import com.dhkim.domain.feed.model.Feed
import com.dhkim.domain.feed.repository.FeedRepository
import com.dhkim.domain.feed.useCase.GetFeedUploadStatusesUseCase
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
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
class HomeScreenTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val feedRepository = mockk<FeedRepository>()
    private val getFeedsUseCase = GetFeedsUseCase(feedRepository)
    private val getFeedUploadStatusesUseCase = GetFeedUploadStatusesUseCase(feedRepository)

    @get:Rule
    val composeRule = createComposeRule()

    private lateinit var viewModel: HomeViewModel

    private val fakeFeeds = List(10) {
        Feed(
            feedId = "feedId$it",
            userId = "userId$it",
            userName = "Tester$it",
            userProfileImage = "profileImage$it",
            imageUrls = listOf("imageUrl1"),
            caption = "Test Caption $it",
            timestamp = 123456789L,
            likeCount = 10,
            commentCount = 5
        )
    }

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun whenFeedsLoadedSuccessfully_showsFeedList() = runTest {
        coEvery { feedRepository.getFeeds() } returns flowOf(PagingData.from(fakeFeeds))
        coEvery { feedRepository.getFeedUploadStatuses() } returns flowOf()

        viewModel = HomeViewModel(getFeedsUseCase, getFeedUploadStatusesUseCase, feedRepository)

        composeRule.setContent {
            val feeds = viewModel.feeds.collectAsLazyPagingItems()
            val feedUploadStatuses by viewModel.feedUploadStatuses.collectAsStateWithLifecycle()
            val feedState = rememberLazyListState()

            HomeScreen(
                feedState = feedState,
                feedUploadStatuses = feedUploadStatuses,
                feeds = feeds
            )
        }

        composeRule.waitUntilAtLeastOneExists(
            hasText("Tester0"),
            300
        )
    }

    @Test
    fun whenFeedsLoadingFails_doesNotShowFeedItems() = runTest {
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
        coEvery { feedRepository.getFeedUploadStatuses() } returns flowOf()

        viewModel = HomeViewModel(getFeedsUseCase, getFeedUploadStatusesUseCase, feedRepository)

        composeRule.setContent {
            val feeds = viewModel.feeds.collectAsLazyPagingItems()
            val feedUploadStatuses by viewModel.feedUploadStatuses.collectAsStateWithLifecycle()
            val feedState = rememberLazyListState()

            HomeScreen(
                feedState = feedState,
                feedUploadStatuses = feedUploadStatuses,
                feeds = feeds
            )
        }

        composeRule.waitUntilDoesNotExist(
            hasTestTag("feed_0"),
            300
        )
    }
}
