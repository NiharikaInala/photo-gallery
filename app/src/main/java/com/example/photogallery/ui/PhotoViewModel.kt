package com.example.photogallery.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import retrofit2.HttpException
import com.example.photogallery.data.Photo
import com.example.photogallery.data.PhotoRepository
import com.google.gson.JsonParseException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class PhotoViewModel @Inject constructor(
    private val repository: PhotoRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(PhotoListUiState(isLoading = true))

    val uiState: StateFlow<PhotoListUiState> = _uiState.asStateFlow()

    private val _selectedPhoto = MutableStateFlow<Photo?>(null)

    val selectedPhoto: StateFlow<Photo?> = _selectedPhoto.asStateFlow()

    private val pageSize = 20

    private var loadJob: Job? = null

    init {
        loadPhotos()
    }

    fun loadPhotos() {
        if (loadJob?.isActive == true) return
        loadJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val photos = repository.loadPhotos()
                _uiState.update {
                    it.copy(
                        photos = photos,
                        visibleCount = photos.size.coerceAtMost(pageSize),
                        error = null
                    )
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: IOException) {
                showLoadError(PhotoListError.NETWORK)
            } catch (_: HttpException) {
                showLoadError(PhotoListError.SERVICE)
            } catch (_: JsonParseException) {
                showLoadError(PhotoListError.INVALID_DATA)
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }

    }

    fun loadNextPage() {
        _uiState.update { state ->
            state.copy(visibleCount = minOf(state.visibleCount + pageSize, state.photos.size))
        }
    }

    private fun showLoadError(error: PhotoListError) {
        _uiState.update { it.copy(error = error) }
    }

    fun selectedPhoto(photo: Photo) {
        _selectedPhoto.value = photo
    }

    fun clearSelectedPhoto() {
        _selectedPhoto.value = null
    }
}