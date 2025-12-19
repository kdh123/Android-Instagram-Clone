package com.dhkim.login

import app.cash.turbine.test
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
import org.junit.Assert
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var viewModel: LoginViewModel
    private val loginRepository = mockk<LoginRepository>()
    private val userRepository = mockk<UserRepository>(relaxed = true)
    private val loginUseCase = LoginUseCase(loginRepository)
    private val testUser = User(
        id = "testId",
        name = "testName",
        email = "testEmail",
        profileUrl = "testProfileUrl"
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        viewModel = LoginViewModel(
            loginUseCase = loginUseCase
        )
    }

    @Test
    fun whenLoginSucceeds_sendsSuccessToastAndUpdateUser() = runTest {
        coEvery { loginRepository.login() } returns flowOf(Unit)
        coEvery { userRepository.getUser() } returns flowOf(testUser)

        viewModel.sideEffect.test {
            viewModel.onAction(LoginAction.Login)

            val actualSideEffect = awaitItem()
            Assert.assertEquals(LoginSideEffect.NavigateToHome, actualSideEffect)

            coVerify(exactly = 1) { loginUseCase() }
        }
    }

    @Test
    fun whenLoginFails_sendsErrorToast() = runTest {
        val errorMessage = "Login failed!"
        coEvery { loginRepository.login() } throws IllegalStateException(errorMessage)

        viewModel.sideEffect.test {
            viewModel.onAction(LoginAction.Login)
            
            val actualSideEffect = awaitItem()
            Assert.assertEquals(LoginSideEffect.ShowToastMessage(errorMessage), actualSideEffect)
            
            coVerify(exactly = 1) { loginUseCase() }
        }
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
}