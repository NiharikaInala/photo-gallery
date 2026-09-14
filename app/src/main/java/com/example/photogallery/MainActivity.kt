package com.example.photogallery

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.ImageLoader
import com.example.photogallery.ui.PhotoDetailScreen
import com.example.photogallery.ui.PhotoListScreen
import com.example.photogallery.ui.PhotoViewModel
import com.example.photogallery.ui.theme.PhotoGalleryTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var imageLoader: ImageLoader

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PhotoGalleryTheme {
                val viewModel: PhotoViewModel = hiltViewModel()
                val selectedPhoto by viewModel.selectedPhoto.collectAsStateWithLifecycle()

                BackHandler(enabled = selectedPhoto != null) {
                    viewModel.clearSelectedPhoto()
                }

                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    PhotoListScreen(
                        viewModel = viewModel,
                        onPhotoClick = viewModel::selectedPhoto
                    )

                    selectedPhoto?.let { photo ->
                        PhotoDetailScreen(
                            photo = photo,
                            onBack = viewModel::clearSelectedPhoto,
                            imageLoader = imageLoader
                        )
                    }
                }
            }
        }
    }
}