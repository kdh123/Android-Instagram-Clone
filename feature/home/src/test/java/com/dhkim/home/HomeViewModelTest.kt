package com.dhkim.home

import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import androidx.paging.testing.asSnapshot
import app.cash.turbine.test
import com.dhkim.domain.feed.model.Comment
import com.dhkim.domain.feed.model.Feed
import com.dhkim.domain.feed.model.HiddenFeed
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
import com.dhkim.feed.common.ReplyGroup
import com.dhkim.feed.common.ReplyItem
import com.dhkim.feed.common.toFeedItem
import com.dhkim.feed.common.toRelativeTime
import com.dhkim.network.ConnectivityChecker
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
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

    private val fakeComments = List(10) {
        Comment(
            commentId = "commentId$it",
            feedId = "feedId$it",
            user = User(
                id = "user$it",
                name = "Tester$it",
                email = "testEmail$it",
                profileUrl = "testProfileUrl$it"
            ),
            content = "Test Comment",
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
            getRepliesUseCase = getReliesUseCase,
            replyCommentUseCase = replyCommentUseCase,
            deleteReplyUseCase = deleteReplyUseCase,
            getUserUseCase = getUserUseCase,
            connectivityChecker = connectivityChecker
        )
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

        viewModel.feeds.test {
            val userId = getUserUseCase().first()?.id ?: ""
            assertEquals(flowOf(errorPagingData).asSnapshot().map { it.toFeedItem(userId) }, flowOf(awaitItem()).asSnapshot())
        }
    }

    @Test
    fun whenShowComments_emitsCommentsPagingData() = runTest {
        coEvery { feedRepository.toggleEnableComment(any(), any(), any()) } returns Unit
        coEvery { feedRepository.toggleLikeCountVisibility(any(), any(), any()) } returns Unit
        coEvery { feedRepository.getHiddenFeeds() } returns flowOf(setOf(HiddenFeed("feedId1", 1234567890)))
        coEvery { feedRepository.getFeedUploadStatuses() } returns flowOf()
        coEvery { feedRepository.getMyFeeds() } returns flowOf(myFeeds)
        coEvery { feedRepository.getHomeFeeds() } returns flowOf(PagingData.from(fakeFeeds))
        coEvery { feedRepository.getFeedUploadStatuses() } returns flowOf()
        coEvery { feedRepository.getComments(any()) } returns flowOf(PagingData.from(fakeComments))
        coEvery { userRepository.getUser() } returns flowOf(testUser)
        coEvery { connectivityChecker.isNetworkAvailable() } returns flowOf(true)

        viewModel.feeds.test {
            val userId = getUserUseCase().first()?.id ?: ""
            assertEquals(fakeFeeds.map { it.toFeedItem(userId) }, flowOf(awaitItem()).asSnapshot())
        }

        viewModel.onAction(HomeAction.ShowComments(fakeFeeds[0].toFeedItem(testUser.id)))
        viewModel.comments.test {
            assertEquals(fakeComments.map { it.toCommentItem() }, flowOf(awaitItem()).asSnapshot())
        }
    }

    @Test
    fun whenReplyToComment_updatesRepliesFlow() = runTest {
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

        coEvery { feedRepository.toggleEnableComment(any(), any(), any()) } returns Unit
        coEvery { feedRepository.toggleLikeCountVisibility(any(), any(), any()) } returns Unit
        coEvery { feedRepository.getHiddenFeeds() } returns flowOf(setOf(HiddenFeed("feedId1", 1234567890)))
        coEvery { feedRepository.getFeedUploadStatuses() } returns flowOf(emptyList())
        coEvery { feedRepository.getMyFeeds() } returns flowOf(myFeeds)
        coEvery { feedRepository.getHomeFeeds() } returns flowOf(PagingData.from(fakeFeeds))
        coEvery { feedRepository.getComments(any()) } returns flowOf(PagingData.from(fakeComments))
        coEvery { userRepository.getUser() } returns flowOf(testUser)
        coEvery { connectivityChecker.isNetworkAvailable() } returns flowOf(true)

        viewModel.feeds.test {
            val userId = getUserUseCase().first()?.id ?: ""
            assertEquals(fakeFeeds.map { it.toFeedItem(userId) }, flowOf(awaitItem()).asSnapshot())
        }

        viewModel.onAction(HomeAction.ShowComments(fakeFeeds[0].toFeedItem(testUser.id)))
        viewModel.comments.test {
            assertEquals(fakeComments.map { it.toCommentItem() }, flowOf(awaitItem()).asSnapshot())
        }

        viewModel.onAction(HomeAction.ShowReplies(fakeComments[0].toCommentItem()))

        viewModel.onAction(
            HomeAction.ReplyComment(
                comment = fakeComments[0].toCommentItem(),
                content = "hello"
            )
        )

        viewModel.replies.test {
            assertEquals(
                awaitItem(),
                listOf(
                    ReplyGroup(
                        commentId = "commentId0",
                        replies = persistentListOf(
                            ReplyItem(
                                replyId = "replyId",
                                user = testUser.toUserItem(),
                                content = "hello",
                                timeAt = 123456789L.toRelativeTime(),
                                likeCount = 0
                            )
                        ),
                        recentAddedReplies = persistentListOf(
                            ReplyItem(
                                replyId = "replyId",
                                user = testUser.toUserItem(),
                                content = "hello",
                                timeAt = 123456789L.toRelativeTime(),
                                likeCount = 0
                            )
                        ),
                    )
                )
            )
        }
    }

    @Test
    fun whenCommentDeleted_removesCommentFromList() = runTest {
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
                feedId = feedId,
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

        coEvery { feedRepository.toggleEnableComment(any(), any(), any()) } returns Unit
        coEvery { feedRepository.toggleLikeCountVisibility(any(), any(), any()) } returns Unit
        coEvery { feedRepository.getHiddenFeeds() } returns flowOf(setOf(HiddenFeed("feedId1", 1234567890)))
        coEvery { feedRepository.getFeedUploadStatuses() } returns flowOf(emptyList())
        coEvery { feedRepository.getMyFeeds() } returns flowOf(myFeeds)
        coEvery { feedRepository.getHomeFeeds() } returns flowOf(PagingData.from(fakeFeeds))
        coEvery { userRepository.getUser() } returns flowOf(testUser)
        coEvery { connectivityChecker.isNetworkAvailable() } returns flowOf(true)

        viewModel.feeds.test {
            val userId = getUserUseCase().first()?.id ?: ""
            assertEquals(fakeFeeds.map { it.toFeedItem(userId) }, flowOf(awaitItem()).asSnapshot())
        }

        viewModel.onAction(HomeAction.ShowComments(fakeFeeds[0].toFeedItem(testUser.id)))
        viewModel.comments.test {
            assertEquals(fakeComments.map { it.toCommentItem() }, flowOf(awaitItem()).asSnapshot())
        }

        viewModel.onAction(HomeAction.DeleteComment(fakeComments[0].toCommentItem()))
        viewModel.comments.test {
            assertEquals(flowOf(awaitItem()).asSnapshot().size, 9)
        }
    }

    @Test
    fun whenReplyDeleted_removesReplyFromList() = runTest {
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
            feedRepository.deleteReply(any(), any())
        } answers {
            val comment = "hello"
            val reply = Reply(
                replyId = "replyId",
                commentId = "commentId0",
                user = testUser,
                content = comment,
                timeAt = 123456789L,
                likeCount = 0
            )

            val flow = replyFlows.getOrPut("commentId0") {
                MutableStateFlow(emptyList())
            }
            val updateReplies = flow.value.filter { it.replyId != reply.replyId }

            flow.update { updateReplies }
            reply
        }

        coEvery { feedRepository.toggleEnableComment(any(), any(), any()) } returns Unit
        coEvery { feedRepository.toggleLikeCountVisibility(any(), any(), any()) } returns Unit
        coEvery { feedRepository.getHiddenFeeds() } returns flowOf(setOf(HiddenFeed("feedId1", 1234567890)))
        coEvery { feedRepository.getFeedUploadStatuses() } returns flowOf(emptyList())
        coEvery { feedRepository.getMyFeeds() } returns flowOf(myFeeds)
        coEvery { feedRepository.getHomeFeeds() } returns flowOf(PagingData.from(fakeFeeds))
        coEvery { feedRepository.getComments(any()) } returns flowOf(PagingData.from(fakeComments))
        coEvery { userRepository.getUser() } returns flowOf(testUser)
        coEvery { connectivityChecker.isNetworkAvailable() } returns flowOf(true)

        viewModel.feeds.test {
            val userId = getUserUseCase().first()?.id ?: ""
            assertEquals(fakeFeeds.map { it.toFeedItem(userId) }, flowOf(awaitItem()).asSnapshot())
        }

        viewModel.onAction(HomeAction.ShowComments(fakeFeeds[0].toFeedItem(testUser.id)))
        viewModel.comments.test {
            assertEquals(fakeComments.map { it.toCommentItem() }, flowOf(awaitItem()).asSnapshot())
        }

        viewModel.onAction(HomeAction.ShowReplies(fakeComments[0].toCommentItem()))
        viewModel.onAction(
            HomeAction.ReplyComment(
                comment = fakeComments[0].toCommentItem(),
                content = "hello"
            )
        )

        viewModel.replies.test {
            assertEquals(
                awaitItem(),
                listOf(
                    ReplyGroup(
                        commentId = "commentId0",
                        replies = persistentListOf(
                            ReplyItem(
                                replyId = "replyId",
                                user = testUser.toUserItem(),
                                content = "hello",
                                timeAt = 123456789L.toRelativeTime(),
                                likeCount = 0
                            )
                        ),
                        recentAddedReplies = persistentListOf(
                            ReplyItem(
                                replyId = "replyId",
                                user = testUser.toUserItem(),
                                content = "hello",
                                timeAt = 123456789L.toRelativeTime(),
                                likeCount = 0
                            )
                        ),
                    )
                )
            )
        }

        viewModel.onAction(HomeAction.DeleteReply(comment = fakeComments[0].toCommentItem(), replyId = "replyId"))
        viewModel.replies.test {
            assertEquals(awaitItem().size, 0)
        }
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
}
