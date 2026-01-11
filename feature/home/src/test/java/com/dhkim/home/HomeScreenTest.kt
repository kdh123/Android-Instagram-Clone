package com.dhkim.home

import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
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
import com.dhkim.domain.feed.model.LikeFeed
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
import com.dhkim.network.ConnectivityChecker
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
    private val toggleFeedLikeCountVisibilityUseCase = ToggleFeedLikeCountVisibilityUseCase(feedRepository, getUserUseCase)
    private val hideFeedUseCase = HideFeedUseCase(feedRepository, getUserUseCase)
    private val unhideFeedUseCase = UnhideFeedUseCase(feedRepository, getUserUseCase)
    private val toggleFeedLikeUseCase = ToggleFeedLikeUseCase(feedRepository, getUserUseCase)
    private val toggleEnableCommentUseCase = ToggleEnableCommentUseCase(feedRepository, getUserUseCase)
    private val getMyFeedsUseCase = GetMyFeedsUseCase(feedRepository)
    private val getLikeFeedsUseCase = GetLikeFeedsUseCase(feedRepository, getUserUseCase)
    private val connectivityChecker = mockk<ConnectivityChecker>()

    @get:Rule
    val composeRule = createComposeRule()

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

    @OptIn(ExperimentalMaterial3Api::class)
    @Test
    fun whenFeedsLoadedSuccessfully_showsFeedList() = runTest {
        coEvery { feedRepository.getAllLikedFeeds(any()) } returns flowOf(setOf(LikeFeed("feedId1", "userId1")))
        coEvery { feedRepository.toggleEnableComment(any(), any()) } returns Unit
        coEvery { feedRepository.toggleLikeCountVisibility(any(), any()) } returns Unit
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

        composeRule.setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val feeds = viewModel.feeds.collectAsLazyPagingItems()
            val feedState = rememberLazyListState()
            val bottomSheetScaffoldState = rememberBottomSheetScaffoldState(
                bottomSheetState = rememberStandardBottomSheetState(
                    initialValue = SheetValue.Hidden,
                    skipHiddenState = false
                )
            )

            HomeScreen(
                uiState = uiState,
                feedState = feedState,
                bottomSheetScaffoldState = bottomSheetScaffoldState,
                feeds = feeds,
                onAction = viewModel::onAction,
                onFeedLayoutChange = {}
            )
        }

        composeRule.waitUntilAtLeastOneExists(
            hasText("Tester0"),
            300
        )
    }

    @OptIn(ExperimentalMaterial3Api::class)
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
        coEvery { feedRepository.toggleEnableComment(any(), any()) } returns Unit
        coEvery { feedRepository.toggleLikeCountVisibility(any(), any()) } returns Unit
        coEvery { feedRepository.getHiddenFeeds() } returns flowOf(setOf(HiddenFeed("feedId1", 1234567890)))
        coEvery { feedRepository.getFeedUploadStatuses() } returns flowOf()
        coEvery { feedRepository.getMyFeeds() } returns flowOf(myFeeds)
        coEvery { feedRepository.getHomeFeeds() } returns flowOf(PagingData.from(fakeFeeds))
        coEvery { feedRepository.getFeedUploadStatuses() } returns flowOf()
        coEvery { feedRepository.getAllLikedFeeds(any()) } returns flowOf(setOf(LikeFeed("feedId1", "userId1")))
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

        composeRule.setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val feeds = viewModel.feeds.collectAsLazyPagingItems()
            val feedState = rememberLazyListState()
            val bottomSheetScaffoldState = rememberBottomSheetScaffoldState(
                bottomSheetState = rememberStandardBottomSheetState(
                    initialValue = SheetValue.Hidden,
                    skipHiddenState = false
                )
            )

            HomeScreen(
                uiState = uiState,
                feedState = feedState,
                bottomSheetScaffoldState = bottomSheetScaffoldState,
                feeds = feeds,
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
