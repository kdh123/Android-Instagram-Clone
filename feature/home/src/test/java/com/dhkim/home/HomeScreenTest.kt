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
import com.dhkim.domain.feed.model.HiddenFeed
import com.dhkim.domain.feed.repository.FeedRepository
import com.dhkim.domain.feed.useCase.GetFeedUploadStatusesUseCase
import com.dhkim.domain.feed.useCase.GetFeedsUseCase
import com.dhkim.domain.feed.useCase.HideFeedUseCase
import com.dhkim.domain.feed.useCase.UnhideFeedUseCase
import com.dhkim.domain.feed.useCase.UpdateEnableFeedCommentUseCase
import com.dhkim.domain.feed.useCase.UpdateFeedLikeCountVisibilityUseCase
import com.dhkim.domain.user.model.User
import com.dhkim.domain.user.repository.UserRepository
import com.dhkim.domain.user.useCase.GetUserUseCase
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
    private val userRepository = mockk<UserRepository>()
    private val getFeedsUseCase = GetFeedsUseCase(feedRepository)
    private val getUserUseCase = GetUserUseCase(userRepository)
    private val getFeedUploadStatusesUseCase = GetFeedUploadStatusesUseCase(feedRepository)
    private val updateFeedLikeCountVisibilityUseCase = UpdateFeedLikeCountVisibilityUseCase(feedRepository)
    private val updateEnableFeedCommentUseCase = UpdateEnableFeedCommentUseCase(feedRepository)
    private val hideFeedUseCase = HideFeedUseCase(feedRepository, getUserUseCase)
    private val unhideFeedUseCase = UnhideFeedUseCase(feedRepository, getUserUseCase)

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

    private val testUser = User(
        id = "testId",
        name = "testName",
        email = "testEmail",
        profileUrl = "testProfileUrl"
    )

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
        coEvery { feedRepository.updateCommentVisibility(any(), any()) } returns Unit
        coEvery { feedRepository.updateLikeCountVisibility(any(), any()) } returns Unit
        coEvery { feedRepository.getHiddenFeeds() } returns flowOf(setOf(HiddenFeed("feedId1", 1234567890)))
        coEvery { feedRepository.getFeedUploadStatuses() } returns flowOf()
        coEvery { feedRepository.getHomeFeeds() } returns flowOf(PagingData.from(fakeFeeds))
        coEvery { feedRepository.getFeedUploadStatuses() } returns flowOf()
        coEvery { userRepository.getUser() } returns flowOf(testUser)

        viewModel = HomeViewModel(
            getFeedsUseCase,
            getFeedUploadStatusesUseCase,
            updateFeedLikeCountVisibilityUseCase,
            updateEnableFeedCommentUseCase,
            hideFeedUseCase,
            unhideFeedUseCase,
            getUserUseCase,
        )

        composeRule.setContent {
            val feeds = viewModel.feeds.collectAsLazyPagingItems()
            val menuVisibleFeed by viewModel.menuVisibleFeed.collectAsStateWithLifecycle()
            val feedUploadStatuses by viewModel.feedUploadStatuses.collectAsStateWithLifecycle()
            val feedState = rememberLazyListState()

            HomeScreen(
                feedState = feedState,
                feedUploadStatuses = feedUploadStatuses,
                feeds = feeds,
                menuVisibleFeed = menuVisibleFeed,
                onAction = viewModel::onAction,
                onFeedLayoutChange = {}
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
        coEvery { feedRepository.updateCommentVisibility(any(), any()) } returns Unit
        coEvery { feedRepository.updateLikeCountVisibility(any(), any()) } returns Unit
        coEvery { feedRepository.getHiddenFeeds() } returns flowOf(setOf(HiddenFeed("feedId1", 1234567890)))
        coEvery { feedRepository.getFeedUploadStatuses() } returns flowOf()
        coEvery { feedRepository.getHomeFeeds() } returns flowOf(PagingData.from(fakeFeeds))
        coEvery { feedRepository.getFeedUploadStatuses() } returns flowOf()
        coEvery { userRepository.getUser() } returns flowOf(testUser)

        viewModel = HomeViewModel(
            getFeedsUseCase,
            getFeedUploadStatusesUseCase,
            updateFeedLikeCountVisibilityUseCase,
            updateEnableFeedCommentUseCase,
            hideFeedUseCase,
            unhideFeedUseCase,
            getUserUseCase,
        )

        composeRule.setContent {
            val feeds = viewModel.feeds.collectAsLazyPagingItems()
            val menuVisibleFeed by viewModel.menuVisibleFeed.collectAsStateWithLifecycle()
            val feedUploadStatuses by viewModel.feedUploadStatuses.collectAsStateWithLifecycle()
            val feedState = rememberLazyListState()

            HomeScreen(
                feedState = feedState,
                feedUploadStatuses = feedUploadStatuses,
                feeds = feeds,
                menuVisibleFeed = menuVisibleFeed,
                onAction = viewModel::onAction,
                onFeedLayoutChange = {}
            )
        }

        composeRule.waitUntilDoesNotExist(
            hasTestTag("feed_0"),
            300
        )
    }
}
