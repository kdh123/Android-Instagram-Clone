package com.dhkim.login

import androidx.compose.runtime.getValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dhkim.domain.login.repository.LoginRepository
import com.dhkim.domain.login.useCase.LoginUseCase
import com.dhkim.domain.login.useCase.LogoutUseCase
import com.dhkim.domain.user.model.User
import com.dhkim.domain.user.repository.UserRepository
import com.dhkim.domain.user.useCase.GetUserUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
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
class LoginScreenTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var viewModel: LoginViewModel

    private val loginRepository = mockk<LoginRepository>(relaxed = true)
    private val userRepository = mockk<UserRepository>(relaxed = true)

    private val loginUseCase = LoginUseCase(loginRepository)
    private val logoutUseCase = LogoutUseCase(loginRepository)
    private val getUserUseCase = GetUserUseCase(userRepository)

    private val testUser = User(
        id = "testId",
        name = "Test User",
        email = "test@example.com",
        profileUrl = "https://test.com/profile.jpg"
    )

    @get:Rule
    val composeRule = createComposeRule()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun whenScreenIsFirstLoaded_andUserIsLoggedOut_showsLoginUi() {
        composeRule.setContent {
            LoginScreen(user = null, onAction = {})
        }

        composeRule.onNodeWithText("LoginScreen").assertIsDisplayed()
        composeRule.onNodeWithText("Sign in with Google").assertIsDisplayed()
    }

    @Test
    fun whenLoginSucceeds_uiIsUpdatedWithUserInfo() = runTest {
        val fakeUserFlow = MutableStateFlow<User?>(null)

        coEvery { loginRepository.login() } returns flowOf(Unit)
        coEvery { userRepository.getUser() } returns fakeUserFlow

        viewModel = LoginViewModel(
            loginUseCase = loginUseCase,
            logoutUseCase = logoutUseCase,
            getUserUseCase = getUserUseCase
        )

        composeRule.setContent {
            val user by viewModel.user.collectAsStateWithLifecycle()
            LoginScreen(user = user, onAction = viewModel::onAction)
        }

        composeRule.onNodeWithText("Sign in with Google").assertIsDisplayed()
        composeRule.onNodeWithText("Sign in with Google").performClick()

        fakeUserFlow.value = testUser

        val expectedWelcomeMessage = "Welcome, ${testUser.name} : ${testUser.email}"

        composeRule.waitUntilAtLeastOneExists(
            hasText(expectedWelcomeMessage),
            500
        )
    }

    @Test
    fun whenLogoutSucceeds_uiIsUpdatedWithUserInfo() = runTest {
        val fakeUserFlow = MutableStateFlow<User?>(testUser)

        coEvery { loginRepository.login() } returns flowOf(Unit)
        coEvery { userRepository.getUser() } returns fakeUserFlow

        viewModel = LoginViewModel(
            loginUseCase = loginUseCase,
            logoutUseCase = logoutUseCase,
            getUserUseCase = getUserUseCase
        )

        composeRule.setContent {
            val user by viewModel.user.collectAsStateWithLifecycle()
            LoginScreen(user = user, onAction = viewModel::onAction)
        }

        composeRule.onNodeWithText("Sign Out").assertIsDisplayed()
        composeRule.onNodeWithText("Sign Out").performClick()

        fakeUserFlow.value = null

        composeRule.waitUntilAtLeastOneExists(
            hasText("LoginScreen"),
            500
        )
    }
}