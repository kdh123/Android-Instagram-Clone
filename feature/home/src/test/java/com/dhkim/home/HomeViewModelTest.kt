package com.dhkim.home

import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import androidx.paging.testing.asSnapshot
import app.cash.turbine.test
import com.dhkim.domain.feed.model.Feed
import com.dhkim.domain.feed.model.HiddenFeed
import com.dhkim.domain.feed.repository.FeedRepository
import com.dhkim.domain.feed.useCase.GetFeedUploadStatusesUseCase
import com.dhkim.domain.feed.useCase.GetFeedsUseCase
import com.dhkim.domain.feed.useCase.GetLikeFeedsUseCase
import com.dhkim.domain.feed.useCase.GetMyFeedsUseCase
import com.dhkim.domain.feed.useCase.HideFeedUseCase
import com.dhkim.domain.feed.useCase.ToggleEnableCommentUseCase
import com.dhkim.domain.feed.useCase.ToggleFeedLikeUseCase
import com.dhkim.domain.feed.useCase.UnhideFeedUseCase
import com.dhkim.domain.feed.useCase.ToggleFeedLikeCountVisibilityUseCase
import com.dhkim.domain.user.model.User
import com.dhkim.domain.user.repository.UserRepository
import com.dhkim.domain.user.useCase.GetUserUseCase
import com.dhkim.feed.common.toFeedItem
import com.dhkim.network.ConnectivityChecker
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
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
    private val userRepository = mockk<UserRepository>()
    private val getFeedsUseCase = GetFeedsUseCase(feedRepository)
    private val getUserUseCase = GetUserUseCase(userRepository)
    private val getFeedUploadStatusesUseCase = GetFeedUploadStatusesUseCase(feedRepository)
    private val toggleFeedLikeCountVisibilityUseCase = ToggleFeedLikeCountVisibilityUseCase(feedRepository, getUserUseCase)
    private val hideFeedUseCase = HideFeedUseCase(feedRepository, getUserUseCase)
    private val unhideFeedUseCase = UnhideFeedUseCase(feedRepository, getUserUseCase)
    private val toggleFeedLikeUseCase = ToggleFeedLikeUseCase(feedRepository, getUserUseCase)
    private val toggleEnableCommentUseCase = ToggleEnableCommentUseCase(feedRepository, getUserUseCase)
    private val getMyFeedsUseCase = GetMyFeedsUseCase(feedRepository)
    private val getLikeFeedsUseCase = GetLikeFeedsUseCase(feedRepository, getUserUseCase)
    private val connectivityChecker = mockk<ConnectivityChecker>()

    private lateinit var viewModel: HomeViewModel

    private val myFeeds = List(3) {
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
    }.toSet()

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

    @Test
    fun whenFetchFeedsSucceeds_emitsSuccessfulPagingData() = runTest {
        coEvery { feedRepository.toggleEnableComment(any(), any(), any()) } returns Unit
        coEvery { feedRepository.toggleLikeCountVisibility(any(), any(), any()) } returns Unit
        coEvery { feedRepository.getHiddenFeeds() } returns flowOf(setOf(HiddenFeed("feedId1", 1234567890)))
        coEvery { feedRepository.getFeedUploadStatuses() } returns flowOf()
        coEvery { feedRepository.getMyFeeds() } returns flowOf(myFeeds)
        coEvery { feedRepository.getHomeFeeds() } returns flowOf(PagingData.from(fakeFeeds))
        coEvery { feedRepository.getFeedUploadStatuses() } returns flowOf()
        coEvery { userRepository.getUser() } returns flowOf(testUser)
        coEvery { connectivityChecker.isNetworkAvailable() } returns flowOf(true)

        viewModel = HomeViewModel(
            getMyFeedsUseCase = getMyFeedsUseCase,
            getFeedsUseCase = getFeedsUseCase,
            getFeedUploadStatusesUseCase = getFeedUploadStatusesUseCase,
            toggleFeedLikeCountVisibilityUseCase = toggleFeedLikeCountVisibilityUseCase,
            hideFeedUseCase = hideFeedUseCase,
            unhideFeedUseCase = unhideFeedUseCase,
            toggleFeedLikeUseCase = toggleFeedLikeUseCase,
            toggleEnableCommentUseCase = toggleEnableCommentUseCase,
            getLikeFeedsUseCase = getLikeFeedsUseCase,
            getUserUseCase = getUserUseCase,
            connectivityChecker = connectivityChecker
        )
        viewModel.feeds.test {
            val userId = getUserUseCase().first()?.id ?: ""
            assertEquals(fakeFeeds.map { it.toFeedItem(userId) }, flowOf(awaitItem()).asSnapshot())
        }
    }

    @Test
    fun whenFetchFeedsFails_emitsErrorPagingData() = runTest {
        val exception = Exception("Network error occurred!")
        coEvery { feedRepository.getMyFeeds() } returns flowOf(myFeeds)
        val errorPagingData = PagingData.from(
            data = listOf<Feed>(),
            sourceLoadStates = LoadStates(
                refresh = LoadState.Error(exception),
                prepend = LoadState.NotLoading(endOfPaginationReached = true),
                append = LoadState.NotLoading(endOfPaginationReached = true)
            )
        )
        coEvery { feedRepository.getHomeFeeds() } returns flowOf(errorPagingData)
        coEvery { feedRepository.getFeedUploadStatuses() } returns flowOf()
        coEvery { userRepository.getUser() } returns flowOf(testUser)
        coEvery { feedRepository.toggleEnableComment(any(), any(), any()) } returns Unit
        coEvery { feedRepository.toggleLikeCountVisibility(any(), any(), any()) } returns Unit
        coEvery { feedRepository.getHiddenFeeds() } returns flowOf(setOf(HiddenFeed("feedId1", 1234567890)))
        coEvery { connectivityChecker.isNetworkAvailable() } returns flowOf(true)

        viewModel = HomeViewModel(
            getMyFeedsUseCase = getMyFeedsUseCase,
            getFeedsUseCase = getFeedsUseCase,
            getFeedUploadStatusesUseCase = getFeedUploadStatusesUseCase,
            toggleFeedLikeCountVisibilityUseCase = toggleFeedLikeCountVisibilityUseCase,
            hideFeedUseCase = hideFeedUseCase,
            unhideFeedUseCase = unhideFeedUseCase,
            toggleFeedLikeUseCase = toggleFeedLikeUseCase,
            toggleEnableCommentUseCase = toggleEnableCommentUseCase,
            getLikeFeedsUseCase = getLikeFeedsUseCase,
            getUserUseCase = getUserUseCase,
            connectivityChecker = connectivityChecker
        )
        viewModel.feeds.test {
            val userId = getUserUseCase().first()?.id ?: ""
            assertEquals(flowOf(errorPagingData).asSnapshot().map { it.toFeedItem(userId) }, flowOf(awaitItem()).asSnapshot())
        }
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
}
