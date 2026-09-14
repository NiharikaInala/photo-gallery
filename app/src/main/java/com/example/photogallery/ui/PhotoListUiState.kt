package com.example.photogallery.ui

import androidx.annotation.StringRes
import com.example.photogallery.R
import com.example.photogallery.data.Photo

data class PhotoListUiState(
    val photos: List<Photo> = emptyList(),
    val visibleCount: Int = 0,
    val isLoading: Boolean = false,
    val error: PhotoListError? = null
)

enum class PhotoListError(@param:StringRes val messageRes: Int) {
    NETWORK(R.string.network_error),
    SERVICE(R.string.service_error),
    INVALID_DATA(R.string.invalid_data_error),
}