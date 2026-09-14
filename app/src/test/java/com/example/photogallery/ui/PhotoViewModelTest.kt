package com.example.photogallery.ui

import com.example.photogallery.data.Photo
import com.example.photogallery.data.PhotoRepository
import com.google.gson.JsonSyntaxException
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class PhotoViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun emptyCatalogRemainsEmpty() = runTest(dispatcher) {
        val viewModel = PhotoViewModel(repository { emptyList() })
        runCurrent()

        viewModel.loadNextPage()

        assertTrue(viewModel.uiState.value.photos.isEmpty())
        assertEquals(0, viewModel.uiState.value.visibleCount)
    }

    @Test
    fun exactPageStopsAtTwenty() = runTest(dispatcher) {
        val viewModel = PhotoViewModel(repository { photos(20) })
        runCurrent()

        viewModel.loadNextPage()

        assertEquals(20, viewModel.uiState.value.visibleCount)
    }

    @Test
    fun partialSecondPageIsReachable() = runTest(dispatcher) {
        val viewModel = PhotoViewModel(repository { photos(21) })
        runCurrent()

        assertEquals(20, viewModel.uiState.value.visibleCount)
        viewModel.loadNextPage()
        assertEquals(21, viewModel.uiState.value.visibleCount)
    }

    @Test
    fun partialThirdPageIsReachable() = runTest(dispatcher) {
        val viewModel = PhotoViewModel(repository { photos(41) })
        runCurrent()

        assertEquals(20, viewModel.uiState.value.visibleCount)
        viewModel.loadNextPage()
        assertEquals(40, viewModel.uiState.value.visibleCount)
        viewModel.loadNextPage()
        assertEquals(41, viewModel.uiState.value.visibleCount)
    }

    @Test
    fun loadingRemainsActiveUntilResponseArrives() = runTest(dispatcher) {
        val response = CompletableDeferred<List<Photo>>()
        val viewModel = PhotoViewModel(repository { response.await() })
        runCurrent()

        assertTrue(viewModel.uiState.value.isLoading)

        response.complete(photos(2))
        runCurrent()

        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(2, viewModel.uiState.value.visibleCount)
    }

    @Test
    fun networkErrorShowsMessageAndRetryLoadsPhotos() = runTest(dispatcher) {
        var calls = 0
        val viewModel = PhotoViewModel(repository {
            calls++
            if (calls == 1) throw IOException("timeout")
            photos(21)
        })
        runCurrent()

        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(PhotoListError.NETWORK, viewModel.uiState.value.error)

        viewModel.loadPhotos()
        runCurrent()

        assertFalse(viewModel.uiState.value.isLoading)
        assertNull(viewModel.uiState.value.error)
        assertEquals(20, viewModel.uiState.value.visibleCount)
        assertEquals(2, calls)
    }

    @Test
    fun serverErrorShowsMessage() = runTest(dispatcher) {
        val viewModel = PhotoViewModel(repository {
            throw HttpException(Response.error<Any>(503, "Service unavailable".toResponseBody()))
        })
        runCurrent()
        assertEquals(PhotoListError.SERVICE, viewModel.uiState.value.error)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun invalidJsonShowsMessage() = runTest(dispatcher) {
        val viewModel = PhotoViewModel(repository { throw JsonSyntaxException("invalid JSON") })
        runCurrent()
        assertEquals(PhotoListError.INVALID_DATA, viewModel.uiState.value.error)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun ignoresAnotherLoadWhileOneIsRunning() = runTest(dispatcher) {
        var calls = 0
        val response = CompletableDeferred<List<Photo>>()
        val viewModel = PhotoViewModel(repository {
            calls++
            response.await()
        })
        runCurrent()

        viewModel.loadPhotos()
        runCurrent()

        assertEquals(1, calls)
        response.complete(emptyList())
        runCurrent()
    }

    @Test
    fun cancellationIsNotShownAsAnError() = runTest(dispatcher) {
        val viewModel = PhotoViewModel(repository {
            throw CancellationException("cancelled")
        })
        runCurrent()

        assertNull(viewModel.uiState.value.error)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun failedReloadKeepsExistingPhotos() = runTest(dispatcher) {
        var calls = 0
        val viewModel = PhotoViewModel(repository {
            calls++
            if (calls > 1) throw IOException()
            photos(21)
        })
        runCurrent()

        viewModel.loadNextPage()
        viewModel.loadPhotos()
        runCurrent()

        assertEquals(21, viewModel.uiState.value.visibleCount)
        assertEquals(21, viewModel.uiState.value.photos.size)
    }

    private fun repository(load: suspend () -> List<Photo>) = object : PhotoRepository {
        override suspend fun loadPhotos() = load()
    }

    private fun photos(count: Int) = List(count) { index ->
        Photo(index.toLong(), "$index.jpeg", 600, 400, "Author $index", "https://example.test/$index")
    }
}
