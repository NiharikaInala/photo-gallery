package com.example.photogallery.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import coil.ImageLoader
import com.example.photogallery.MainActivity
import com.example.photogallery.data.Photo
import com.example.photogallery.data.PhotoRepository
import com.example.photogallery.di.NetworkModule
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@UninstallModules(NetworkModule::class)
@RunWith(AndroidJUnit4::class)
class AppStartupTest {
    private var repositoryCalls = 0
    private val images = TestImageLoader()

    @BindValue
    @JvmField
    val repository: PhotoRepository = object : PhotoRepository {
        override suspend fun loadPhotos(): List<Photo> {
            repositoryCalls++
            return listOf(testPhoto())
        }
    }

    @BindValue
    @JvmField
    val imageLoader: ImageLoader = images.create(ApplicationProvider.getApplicationContext())

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun launchesWithInjectedDataAndDoesNotLoadListImages() {
        composeRule.onNodeWithText("sample.jpg").assertIsDisplayed()
        composeRule.runOnIdle {
            assertEquals(1, repositoryCalls)
            assertEquals(0, images.requests.size)
        }
    }

    @Test
    fun toolbarBackReturnsToList() {
        composeRule.onNodeWithText("sample.jpg").performClick()
        composeRule.onNodeWithText("Alice").assertIsDisplayed()
        composeRule.onNodeWithText("Back").performClick()
        composeRule.onNodeWithText("Alice").assertDoesNotExist()
        composeRule.onNodeWithText("sample.jpg").assertIsDisplayed()
    }

    @Test
    fun systemBackReturnsToList() {
        composeRule.onNodeWithText("sample.jpg").performClick()
        composeRule.onNodeWithText("Alice").assertIsDisplayed()
        composeRule.activityRule.scenario.onActivity { activity ->
            activity.onBackPressedDispatcher.onBackPressed()
        }
        composeRule.onNodeWithText("Alice").assertDoesNotExist()
        composeRule.onNodeWithText("sample.jpg").assertIsDisplayed()
    }

    @Test
    fun recreationKeepsSelectionWithoutReloadingCatalog() {
        composeRule.onNodeWithText("sample.jpg").performClick()
        composeRule.onNodeWithText("Alice").assertIsDisplayed()
        composeRule.activityRule.scenario.recreate()
        composeRule.onNodeWithText("Alice").assertIsDisplayed()
        composeRule.runOnIdle { assertEquals(1, repositoryCalls) }
    }
}