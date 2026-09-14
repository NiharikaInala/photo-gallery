package com.example.photogallery.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.photogallery.data.Photo
import kotlinx.coroutines.CompletableDeferred
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PhotoDetailScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    private fun show(
        photo: Photo,
        images: TestImageLoader = TestImageLoader(),
        fontScale: Float = 1f,
    ) {
        val loader = images.create(ApplicationProvider.getApplicationContext())
        composeRule.setContent {
            val density = Density(LocalDensity.current.density, fontScale)
            CompositionLocalProvider(LocalDensity provides density) {
                MaterialTheme {
                    Box(Modifier.requiredSize(300.dp, 480.dp)) {
                        PhotoDetailScreen(photo, onBack = {}, imageLoader = loader)
                    }
                }
            }
        }
    }

    @Test
    fun landscapeImageIsFullWidthAndVerticallyCentered() {
        show(testPhoto())

        val viewport = composeRule.onNodeWithTag("detail_viewport").fetchSemanticsNode().boundsInRoot
        val image = composeRule.onNodeWithTag("photo_image").fetchSemanticsNode().boundsInRoot
        val author = composeRule.onNodeWithTag("photo_author").fetchSemanticsNode().boundsInRoot
        assertEquals(viewport.left, image.left, 1f)
        assertEquals(viewport.width, image.width, 1f)
        assertEquals(viewport.center.y, image.center.y, 1f)
        assertEquals(image.width * 400f / 600f, image.height, 1f)
        assertTrue(author.top > image.bottom)
    }

    @Test
    fun portraitImageStartsAtTopAndPreservesRatio() {
        show(testPhoto(600, 650))

        val viewport = composeRule.onNodeWithTag("detail_viewport").fetchSemanticsNode().boundsInRoot
        val image = composeRule.onNodeWithTag("photo_image").fetchSemanticsNode().boundsInRoot
        assertEquals(viewport.top, image.top, 1f)
        assertEquals(viewport.width, image.width, 1f)
        assertEquals(image.width * 650f / 600f, image.height, 1f)
    }

    @Test
    fun tallPortraitAuthorRemainsReachableWithLargeText() {
        show(testPhoto(300, 1200), fontScale = 2f)
        composeRule.onNodeWithTag("photo_author").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun missingAuthorUsesLocalizedFallback() {
        show(testPhoto().copy(author = null))
        composeRule.onNodeWithText("Unknown author").assertIsDisplayed()
    }

    @Test
    fun showsLoadingIndicatorWhileImageRequestRuns() {
        val requestGate = CompletableDeferred<Unit>()
        show(testPhoto(), TestImageLoader(requestGate = requestGate))

        composeRule.onNodeWithTag("image_loading").assertIsDisplayed()
        requestGate.complete(Unit)
        composeRule.waitUntil(5_000) {
            composeRule.onAllNodes(hasTestTag("image_loading"))
                .fetchSemanticsNodes().isEmpty()
        }
    }

    @Test
    fun failedImageCanBeRetried() {
        val images = TestImageLoader(failFirstRequest = true)
        show(testPhoto(), images)

        composeRule.waitUntil(5_000) {
            composeRule.onAllNodes(hasText("Retry image")).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("Unable to load this image.").assertIsDisplayed()

        composeRule.onNodeWithText("Retry image").performClick()
        composeRule.waitUntil(5_000) { images.requests.size == 2 }

        composeRule.onNodeWithText("Unable to load this image.").assertDoesNotExist()
        assertEquals(listOf(testPhoto().imageUrl, testPhoto().imageUrl), images.requests.toList())
    }
}
