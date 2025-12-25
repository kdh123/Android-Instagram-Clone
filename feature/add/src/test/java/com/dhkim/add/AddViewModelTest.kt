package com.dhkim.add

import androidx.paging.PagingData
import app.cash.turbine.test
import com.dhkim.domain.common.model.GalleryImage
import com.dhkim.domain.common.repository.GalleryRepository
import com.dhkim.domain.common.useCase.GetGalleryImagesUseCase
import com.dhkim.domain.common.useCase.GetRecentGalleryImageUseCase
import com.dhkim.domain.feed.useCase.UploadFeedUseCase
import io.mockk.coEvery
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AddViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val galleryRepository = mockk<GalleryRepository>()
    private val uploadFeedUseCase = mockk<UploadFeedUseCase>()
    private val getGalleryImagesUseCase = GetGalleryImagesUseCase(galleryRepository)
    private val getRecentGalleryImageUseCase = GetRecentGalleryImageUseCase(galleryRepository)

    private lateinit var viewModel: AddViewModel

    private val fakeGalleryImages = List(10) {
        GalleryImage(
            id = it.toLong(),
            uri = "imageUri$it",
            name = "imageName$it",
            dateAdded = 123548
        )
    }

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @Test
    fun whenChangeModeToMultiple_thenStateUpdatedToMultiple() = runTest {
        coEvery { galleryRepository.getRecentGalleryImageUseCase() } returns flowOf(fakeGalleryImages[0])
        coEvery { galleryRepository.getGalleryImages(any()) } returns flowOf(PagingData.from(fakeGalleryImages))

        viewModel = AddViewModel(
            uploadFeedUseCase,
            getGalleryImagesUseCase,
            getRecentGalleryImageUseCase
        )

        viewModel.selectImageState.test {
            awaitItem() // Initial

            // Act: Click the button to switch to multiple selection mode.
            viewModel.onAction(AddAction.ChangeSelectImageMode)

            val item = awaitItem()

            assertEquals((item as SelectImageState.Multiple).imageUris.size, 1)
            assertEquals(item.imageUris[0], fakeGalleryImages[0].uri)
        }
    }

    @Test
    fun whenChangeModeToSingle_thenStateUpdatedToSingleWithLastSelectedImage() = runTest {
        coEvery { galleryRepository.getRecentGalleryImageUseCase() } returns flowOf(fakeGalleryImages[0])
        coEvery { galleryRepository.getGalleryImages(any()) } returns flowOf(PagingData.from(fakeGalleryImages))

        viewModel = AddViewModel(
            uploadFeedUseCase,
            getGalleryImagesUseCase,
            getRecentGalleryImageUseCase
        )

        viewModel.selectImageState.test {
            awaitItem() // Initial

            // Act: Switch to multiple selection mode.
            viewModel.onAction(AddAction.ChangeSelectImageMode)

            awaitItem() // After mode change
            
            // Act: Select additional images.
            viewModel.onAction(AddAction.SelectImage(fakeGalleryImages[3].uri))
            awaitItem()
            viewModel.onAction(AddAction.SelectImage(fakeGalleryImages[4].uri))
            assertEquals((awaitItem() as SelectImageState.Multiple).imageUris.size, 3)

            // Act: Switch back to single selection mode.
            viewModel.onAction(AddAction.ChangeSelectImageMode)

            assertEquals((awaitItem() as SelectImageState.Single).imageUri, fakeGalleryImages[4].uri)
        }
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
}