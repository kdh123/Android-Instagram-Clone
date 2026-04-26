package com.dhkim.home

import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.getValue
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTextInput
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import com.dhkim.domain.feed.model.Comment
import com.dhkim.domain.feed.model.Feed
import com.dhkim.domain.feed.model.HiddenFeed
import com.dhkim.domain.feed.model.LikeFeed
import com.dhkim.domain.feed.model.Reply
import com.dhkim.domain.feed.repository.FeedRepository
import com.dhkim.domain.feed.useCase.AddCommentUseCase
import com.dhkim.domain.feed.useCase.DeleteCommentUseCase
import com.dhkim.domain.feed.useCase.DeleteReplyUseCase
import com.dhkim.domain.feed.useCase.GetCommentsUseCase
import com.dhkim.domain.feed.useCase.GetFeedUploadStatusesUseCase
import com.dhkim.domain.feed.useCase.GetFeedsUseCase
import com.dhkim.domain.feed.useCase.GetLikeFeedsUseCase
import com.dhkim.domain.feed.useCase.GetLikersUseCase
import com.dhkim.domain.feed.useCase.GetMyFeedsUseCase
import com.dhkim.domain.feed.useCase.GetRepliesUseCase
import com.dhkim.domain.feed.useCase.HideFeedUseCase
import com.dhkim.domain.feed.useCase.ReplyCommentUseCase
import com.dhkim.domain.feed.useCase.ToggleEnableCommentUseCase
import com.dhkim.domain.feed.useCase.ToggleFeedLikeCountVisibilityUseCase
import com.dhkim.domain.feed.useCase.ToggleFeedLikeUseCase
import com.dhkim.domain.feed.useCase.UnhideFeedUseCase
import com.dhkim.domain.user.model.User
import com.dhkim.domain.user.repository.UserRepository
import com.dhkim.domain.user.useCase.GetUserUseCase
import com.dhkim.network.ConnectivityChecker
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
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
    private val getLikersUseCase = GetLikersUseCase(feedRepository)
    private val getFeedUploadStatusesUseCase = GetFeedUploadStatusesUseCase(feedRepository)
    private val toggleFeedLikeCountVisibilityUseCase = ToggleFeedLikeCountVisibilityUseCase(feedRepository, getUserUseCase)
    private val hideFeedUseCase = HideFeedUseCase(feedRepository, getUserUseCase)
    private val unhideFeedUseCase = UnhideFeedUseCase(feedRepository, getUserUseCase)
    private val toggleFeedLikeUseCase = ToggleFeedLikeUseCase(feedRepository, getUserUseCase)
    private val toggleEnableCommentUseCase = ToggleEnableCommentUseCase(feedRepository, getUserUseCase)
    private val getMyFeedsUseCase = GetMyFeedsUseCase(feedRepository)
    private val getLikeFeedsUseCase = GetLikeFeedsUseCase(feedRepository, getUserUseCase)
    private val getCommentUseCase = GetCommentsUseCase(feedRepository)
    private val addCommentUseCase = AddCommentUseCase(feedRepository, getUserUseCase)
    private val deleteCommentUseCase = DeleteCommentUseCase(feedRepository)
    private val getReliesUseCase = GetRepliesUseCase(feedRepository)
    private val replyCommentUseCase = ReplyCommentUseCase(feedRepository, getUserUseCase)
    private val deleteReplyUseCase = DeleteReplyUseCase(feedRepository)
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

    private val fakeComments = List(10) {
        Comment(
            commentId = "commentId$it",
            targetId = "feedId$it",
            user = User(
                id = "user$it",
                name = "Tester$it",
                email = "testEmail$it",
                profileUrl = "testProfileUrl$it"
            ),
            content = "Test Comment $it",
            timeAt = 123456789L,
            replyCount = 0,
            likeCount = 4,
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
            getLikersUseCase = getLikersUseCase,
            getCommentsUseCase = getCommentUseCase,
            addCommentUseCase = addCommentUseCase,
            deleteCommentUseCase = deleteCommentUseCase,
            getUserUseCase = getUserUseCase,
            getRepliesUseCase = getReliesUseCase,
            replyCommentUseCase = replyCommentUseCase,
            deleteReplyUseCase = deleteReplyUseCase,
            connectivityChecker = connectivityChecker
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Test
    fun whenFeedsLoadedSuccessfully_showsFeedList() = runTest {
        coEvery { feedRepository.getAllLikedFeeds(any()) } returns flowOf(setOf(LikeFeed("feedId1", "userId1")))
        coEvery { feedRepository.toggleEnableComment(any(), any(), any()) } returns Unit
        coEvery { feedRepository.toggleLikeCountVisibility(any(), any(), any()) } returns Unit
        coEvery { feedRepository.getHiddenFeeds() } returns flowOf(setOf(HiddenFeed("feedId1", 1234567890)))
        coEvery { feedRepository.getMyFeeds() } returns flowOf(myFeeds)
        coEvery { feedRepository.getHomeFeeds() } returns flowOf(PagingData.from(fakeFeeds))
        coEvery { feedRepository.getFeedUploadStatuses() } returns flowOf(emptyList())
        coEvery { userRepository.getUser() } returns flowOf(testUser)
        coEvery { connectivityChecker.isNetworkAvailable() } returns flowOf(true)

        composeRule.setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val feeds = viewModel.feeds.collectAsLazyPagingItems()
            val likers = viewModel.likers.collectAsLazyPagingItems()
            val comments = viewModel.comments.collectAsLazyPagingItems()
            val replies by viewModel.replies.collectAsStateWithLifecycle()
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
                likers = likers,
                comments = comments,
                replies = replies,
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
        coEvery { feedRepository.toggleEnableComment(any(), any(), any()) } returns Unit
        coEvery { feedRepository.toggleLikeCountVisibility(any(), any(), any()) } returns Unit
        coEvery { feedRepository.getHiddenFeeds() } returns flowOf(setOf(HiddenFeed("feedId1", 1234567890)))
        coEvery { feedRepository.getFeedUploadStatuses() } returns flowOf(emptyList())
        coEvery { feedRepository.getMyFeeds() } returns flowOf(myFeeds)
        coEvery { feedRepository.getHomeFeeds() } returns flowOf(PagingData.from(fakeFeeds))
        coEvery { feedRepository.getAllLikedFeeds(any()) } returns flowOf(setOf(LikeFeed("feedId1", "userId1")))
        coEvery { userRepository.getUser() } returns flowOf(testUser)
        coEvery { connectivityChecker.isNetworkAvailable() } returns flowOf(true)

        composeRule.setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val feeds = viewModel.feeds.collectAsLazyPagingItems()
            val likers = viewModel.likers.collectAsLazyPagingItems()
            val comments = viewModel.comments.collectAsLazyPagingItems()
            val replies by viewModel.replies.collectAsStateWithLifecycle()
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
                likers = likers,
                comments = comments,
                replies = replies,
                onAction = viewModel::onAction,
                onFeedLayoutChange = {}
            )
        }

        composeRule.waitUntilDoesNotExist(
            hasTestTag("feed_0"),
            300
        )
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Test
    fun whenReplyPosted_showsNewReplyInList() = runTest {
        coEvery { feedRepository.getAllLikedFeeds(any()) } returns flowOf(setOf(LikeFeed("feedId1", "userId1")))
        coEvery { feedRepository.toggleEnableComment(any(), any(), any()) } returns Unit
        coEvery { feedRepository.toggleLikeCountVisibility(any(), any(), any()) } returns Unit
        coEvery { feedRepository.getHiddenFeeds() } returns flowOf(setOf(HiddenFeed("feedId1", 1234567890)))
        coEvery { feedRepository.getFeedUploadStatuses() } returns flowOf(emptyList())
        coEvery { feedRepository.getMyFeeds() } returns flowOf(myFeeds)
        coEvery { feedRepository.getHomeFeeds() } returns flowOf(PagingData.from(fakeFeeds))
        coEvery { feedRepository.getComments(any()) } returns flowOf(PagingData.from(fakeComments))
        coEvery { userRepository.getUser() } returns flowOf(testUser)
        coEvery { connectivityChecker.isNetworkAvailable() } returns flowOf(true)
        val replyFlows = mutableMapOf<String, MutableStateFlow<List<Reply>>>()

        every {
            feedRepository.getReplies(any())
        } answers {
            val commentId = firstArg<String>()

            replyFlows.getOrPut(commentId) {
                MutableStateFlow(emptyList())
            }
        }

        coEvery {
            feedRepository.replyComment(any(), any(), any(), any())
        } answers {
            val commentId = secondArg<String>()
            val user = thirdArg<User>()
            val comment = "hello"

            val reply = Reply(
                replyId = "replyId",
                commentId = commentId,
                user = user,
                content = comment,
                timeAt = 123456789L,
                likeCount = 0
            )

            val flow = replyFlows.getOrPut(commentId) {
                MutableStateFlow(emptyList())
            }
            flow.update { it + reply }
            reply
        }


        composeRule.setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val feeds = viewModel.feeds.collectAsLazyPagingItems()
            val likers = viewModel.likers.collectAsLazyPagingItems()
            val comments = viewModel.comments.collectAsLazyPagingItems()
            val replies by viewModel.replies.collectAsStateWithLifecycle()
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
                likers = likers,
                comments = comments,
                replies = replies,
                onAction = viewModel::onAction,
                onFeedLayoutChange = {}
            )
        }

        composeRule.waitUntilAtLeastOneExists(
            hasText("Tester0"),
            300
        )

        val commentIcon = composeRule.onNodeWithTag(
            "comment_icon_${fakeFeeds[0].feedId}",
            useUnmergedTree = true
        )
        commentIcon.assertExists().assertHasClickAction()
        commentIcon.performSemanticsAction(SemanticsActions.OnClick)

        composeRule.waitUntilAtLeastOneExists(
            hasText("Test Comment 0"),
            300
        )

        composeRule.onNodeWithTag("reply_button_commentId0").performSemanticsAction(SemanticsActions.OnClick)
        composeRule.onNodeWithTag("reply_to_username").assertExists()
        composeRule.onNodeWithTag("comment_text_field").performSemanticsAction(SemanticsActions.OnClick)
        composeRule.onNodeWithTag("comment_text_field").performTextInput("hello")
        composeRule.onNodeWithTag("add_comment_button").performSemanticsAction(SemanticsActions.OnClick)

        composeRule.waitUntilAtLeastOneExists(
            hasText("hello"),
            300
        )
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Test
    fun whenCommentDeleted_thenCommentRemovedFromList() = runTest {
        coEvery { feedRepository.getAllLikedFeeds(any()) } returns flowOf(setOf(LikeFeed("feedId1", "userId1")))
        coEvery { feedRepository.toggleEnableComment(any(), any(), any()) } returns Unit
        coEvery { feedRepository.toggleLikeCountVisibility(any(), any(), any()) } returns Unit
        coEvery { feedRepository.getHiddenFeeds() } returns flowOf(setOf(HiddenFeed("feedId1", 1234567890)))
        coEvery { feedRepository.getFeedUploadStatuses() } returns flowOf(emptyList())
        coEvery { feedRepository.getMyFeeds() } returns flowOf(myFeeds)
        coEvery { feedRepository.getHomeFeeds() } returns flowOf(PagingData.from(fakeFeeds))
        coEvery { userRepository.getUser() } returns flowOf(testUser)
        coEvery { connectivityChecker.isNetworkAvailable() } returns flowOf(true)
        val replyFlows = mutableMapOf<String, MutableStateFlow<List<Reply>>>()

        every {
            feedRepository.getReplies(any())
        } answers {
            val commentId = firstArg<String>()

            replyFlows.getOrPut(commentId) {
                MutableStateFlow(emptyList())
            }
        }

        coEvery {
            feedRepository.replyComment(any(), any(), any(), any())
        } answers {
            val commentId = secondArg<String>()
            val user = thirdArg<User>()
            val comment = "hello"

            val reply = Reply(
                replyId = "replyId",
                commentId = commentId,
                user = user,
                content = comment,
                timeAt = 123456789L,
                likeCount = 0
            )

            val flow = replyFlows.getOrPut(commentId) {
                MutableStateFlow(emptyList())
            }
            flow.update { it + reply }
            reply
        }

        val comments = MutableStateFlow(fakeComments)

        every {
            feedRepository.getComments(any())
        } answers {
            flowOf(PagingData.from(comments.value))
        }

        coEvery {
            feedRepository.addComment(any(), any(), any())
        } answers {
            val feedId = firstArg<String>()
            val user = secondArg<User>()
            val content = thirdArg<String>()
            val comment = Comment(
                commentId = "commentId_0",
                targetId = feedId,
                user = user,
                content = content,
                timeAt = 123456789L,
                replyCount = 0,
                likeCount = 0
            )
            comments.update { it + comment }
            comment
        }

        coEvery {
            feedRepository.deleteComment(any(), any())
        } answers {
            val commentId = secondArg<String>()
            val updateComments = comments.value.filter { it.commentId != commentId }
            comments.update { updateComments }
        }


        composeRule.setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val feeds = viewModel.feeds.collectAsLazyPagingItems()
            val likers = viewModel.likers.collectAsLazyPagingItems()
            val comments = viewModel.comments.collectAsLazyPagingItems()
            val replies by viewModel.replies.collectAsStateWithLifecycle()
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
                likers = likers,
                comments = comments,
                replies = replies,
                onAction = viewModel::onAction,
                onFeedLayoutChange = {}
            )
        }

        composeRule.waitUntilAtLeastOneExists(
            hasText("Tester0"),
            300
        )

        val commentIcon = composeRule.onNodeWithTag(
            "comment_icon_${fakeFeeds[0].feedId}",
            useUnmergedTree = true
        )
        commentIcon.assertExists().assertHasClickAction()
        commentIcon.performSemanticsAction(SemanticsActions.OnClick)

        composeRule.waitUntilAtLeastOneExists(
            hasText("Test Comment 0"),
            300
        )

        composeRule.onNodeWithTag("comment_item_commentId0").performSemanticsAction(SemanticsActions.OnLongClick)
        composeRule.onNodeWithTag("delete_comment_button_commentId0").assertExists()
        composeRule.onNodeWithTag("delete_comment_button_commentId0").performSemanticsAction(SemanticsActions.OnClick)
        composeRule.onNodeWithText("Test Comment 0").assertDoesNotExist()
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Test
    fun whenReplyDeleted_thenReplyRemovedFromList() = runTest {
        coEvery { feedRepository.getAllLikedFeeds(any()) } returns flowOf(setOf(LikeFeed("feedId1", "userId1")))
        coEvery { feedRepository.toggleEnableComment(any(), any(), any()) } returns Unit
        coEvery { feedRepository.toggleLikeCountVisibility(any(), any(), any()) } returns Unit
        coEvery { feedRepository.getHiddenFeeds() } returns flowOf(setOf(HiddenFeed("feedId1", 1234567890)))
        coEvery { feedRepository.getFeedUploadStatuses() } returns flowOf(emptyList())
        coEvery { feedRepository.getMyFeeds() } returns flowOf(myFeeds)
        coEvery { feedRepository.getHomeFeeds() } returns flowOf(PagingData.from(fakeFeeds))
        coEvery { feedRepository.getComments(any()) } returns flowOf(PagingData.from(fakeComments))
        coEvery { userRepository.getUser() } returns flowOf(testUser)
        coEvery { connectivityChecker.isNetworkAvailable() } returns flowOf(true)
        val replyFlows = mutableMapOf<String, MutableStateFlow<List<Reply>>>()

        every {
            feedRepository.getReplies(any())
        } answers {
            val commentId = firstArg<String>()

            replyFlows.getOrPut(commentId) {
                MutableStateFlow(emptyList())
            }
        }

        coEvery {
            feedRepository.replyComment(any(), any(), any(), any())
        } answers {
            val commentId = secondArg<String>()
            val user = thirdArg<User>()
            val comment = "hello"

            val reply = Reply(
                replyId = "replyId",
                commentId = commentId,
                user = user,
                content = comment,
                timeAt = 123456789L,
                likeCount = 0
            )

            val flow = replyFlows.getOrPut(commentId) {
                MutableStateFlow(emptyList())
            }
            flow.update { it + reply }
            reply
        }

        coEvery {
            feedRepository.deleteReply(any(), any(), any())
        } answers {
            val commentId = secondArg<String>()
            val replyId = thirdArg<String>()
            val comment = "hello"

            val reply = Reply(
                replyId = replyId,
                commentId = commentId,
                user = testUser,
                content = comment,
                timeAt = 123456789L,
                likeCount = 0
            )

            val flow = replyFlows.getOrPut(commentId) {
                MutableStateFlow(emptyList())
            }
            val updateReplies = flow.value.filter { it.replyId != replyId }
            flow.update { updateReplies }
            reply
        }


        composeRule.setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val feeds = viewModel.feeds.collectAsLazyPagingItems()
            val likers = viewModel.likers.collectAsLazyPagingItems()
            val comments = viewModel.comments.collectAsLazyPagingItems()
            val replies by viewModel.replies.collectAsStateWithLifecycle()
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
                likers = likers,
                comments = comments,
                replies = replies,
                onAction = viewModel::onAction,
                onFeedLayoutChange = {}
            )
        }

        composeRule.waitUntilAtLeastOneExists(
            hasText("Tester0"),
            300
        )

        val commentIcon = composeRule.onNodeWithTag(
            "comment_icon_${fakeFeeds[0].feedId}",
            useUnmergedTree = true
        )
        commentIcon.assertExists().assertHasClickAction()
        commentIcon.performSemanticsAction(SemanticsActions.OnClick)

        composeRule.waitUntilAtLeastOneExists(
            hasText("Test Comment 0"),
            300
        )

        composeRule.onNodeWithTag("reply_button_commentId0").performSemanticsAction(SemanticsActions.OnClick)
        composeRule.onNodeWithTag("reply_to_username").assertExists()
        composeRule.onNodeWithTag("comment_text_field").performSemanticsAction(SemanticsActions.OnClick)
        composeRule.onNodeWithTag("comment_text_field").performTextInput("hello")
        composeRule.onNodeWithTag("add_comment_button").performSemanticsAction(SemanticsActions.OnClick)

        composeRule.waitUntilAtLeastOneExists(
            hasText("hello"),
            300
        )
        composeRule.onNodeWithTag("reply_item_replyId").performSemanticsAction(SemanticsActions.OnLongClick)
        composeRule.onNodeWithTag("delete_reply_button_replyId").assertExists()
        composeRule.onNodeWithTag("delete_reply_button_replyId").performSemanticsAction(SemanticsActions.OnClick)
        composeRule.waitUntilDoesNotExist(
            hasTestTag("reply_item_replyId"),
            300
        )
    }
}
