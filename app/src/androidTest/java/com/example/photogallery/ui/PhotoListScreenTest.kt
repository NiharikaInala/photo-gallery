package com.example.photogallery.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.photogallery.data.Photo
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PhotoListScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun showsLoadingIndicatorWhileFirstPageLoads() {
        show(PhotoListUiState(isLoading = true))

        composeRule.onNodeWithTag("initial_loading").assertIsDisplayed()
    }

    @Test
    fun showsMessageWhenNoPhotosAreAvailable() {
        show(PhotoListUiState())

        composeRule.onNodeWithText("No photos available.").assertIsDisplayed()
    }

    @Test
    fun retryFromErrorInvokesCallback() {
        var retryRequested = false
        show(
            state = PhotoListUiState(error = PhotoListError.NETWORK),
            onRetry = { retryRequested = true },
        )

        composeRule.onNodeWithText("Unable to connect. Check your connection and try again.")
            .assertIsDisplayed()
        composeRule.onNodeWithText("Retry").performClick()
        composeRule.runOnIdle { assertTrue(retryRequested) }
    }

    @Test
    fun scrollingNearEndRequestsNextPage() {
        var loadMoreRequested = false
        show(
            state = PhotoListUiState(
                photos = photos(25),
                visibleCount = 20,
            ),
            onLoadMore = { loadMoreRequested = true },
        )

        composeRule.onNodeWithTag("photo_list").performScrollToIndex(19)
        composeRule.runOnIdle { assertTrue(loadMoreRequested) }
    }

    private fun show(
        state: PhotoListUiState,
        onLoadMore: () -> Unit = {},
        onRetry: () -> Unit = {},
    ) {
        composeRule.setContent {
            MaterialTheme {
                PhotoListScreenContent(
                    state = state,
                    onPhotoClick = {},
                    onLoadMore = onLoadMore,
                    onRetry = onRetry,
                )
            }
        }
    }

    private fun photos(count: Int) = List(count) { index ->
        Photo(
            id = index.toLong(),
            fileName = "$index.jpeg",
            width = 600,
            height = 400,
            author = "Author $index",
            imageUrl = "https://example.test/$index",
        )
    }
}
