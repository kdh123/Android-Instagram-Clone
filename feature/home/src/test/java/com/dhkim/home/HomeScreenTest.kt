package com.dhkim.home

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
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

        viewModel = HomeViewModel(getFeedsUseCase)

        composeRule.setContent {
            val feeds = viewModel.feeds.collectAsLazyPagingItems()
            HomeScreen(feeds = feeds)
        }

        composeRule.waitUntilAtLeastOneExists(
            hasText("feedId0, userId0, Tester0, profileImage0, [imageUrl1], Test Caption 0, 123456789, 10, 5"),
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

        viewModel = HomeViewModel(getFeedsUseCase)

        composeRule.setContent {
            val feeds = viewModel.feeds.collectAsLazyPagingItems()
            HomeScreen(feeds = feeds)
        }

        composeRule.waitUntilDoesNotExist(
            hasTestTag("feed_0"),
            300
        )
    }
}
