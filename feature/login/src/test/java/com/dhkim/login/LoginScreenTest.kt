package com.dhkim.login

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.dhkim.domain.login.repository.LoginRepository
import com.dhkim.domain.login.useCase.LoginUseCase
import com.dhkim.domain.user.model.User
import com.dhkim.domain.user.repository.UserRepository
import io.mockk.coEvery
import io.mockk.coVerify
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
class LoginScreenTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var viewModel: LoginViewModel

    private val loginRepository = mockk<LoginRepository>(relaxed = true)
    private val userRepository = mockk<UserRepository>(relaxed = true)

    private val loginUseCase = LoginUseCase(loginRepository)

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
        viewModel = LoginViewModel(
            loginUseCase = loginUseCase
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun whenLoginSucceeds_uiIsUpdatedWithUserInfo() = runTest {
        coEvery { loginRepository.login() } returns flowOf(Unit)
        coEvery { userRepository.getUser() } returns flowOf(testUser)

        composeRule.setContent {
            LoginScreen(onAction = viewModel::onAction)
        }

        composeRule.onNodeWithTag(testTag = "login_button").assertIsDisplayed()
        composeRule.onNodeWithTag(testTag = "login_button").performClick()

        coVerify(exactly = 1) { loginUseCase() }
    }
}