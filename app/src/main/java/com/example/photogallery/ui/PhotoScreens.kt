package com.example.photogallery.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.imageLoader
import coil.request.ImageRequest
import com.example.photogallery.R
import com.example.photogallery.data.Photo

private const val LOAD_MORE_THRESHOLD = 5

@Composable
fun PhotoListScreen(
    viewModel: PhotoViewModel = hiltViewModel(),
    onPhotoClick: (Photo) -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    PhotoListScreenContent(
        state = state,
        onPhotoClick = onPhotoClick,
        onLoadMore = viewModel::loadNextPage,
        onRetry = viewModel::loadPhotos
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoListScreenContent(
    state: PhotoListUiState,
    onPhotoClick: (Photo) -> Unit,
    onLoadMore: () -> Unit,
    onRetry: () -> Unit
) {
    val visiblePhotos = state.photos.take(state.visibleCount)
    val listState = rememberLazyListState()
    LaunchedEffect(listState, state.visibleCount, state.photos.size) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1 }
            .collect { lastVisibleIndex ->
                val reachedEnd = lastVisibleIndex >= state.visibleCount - LOAD_MORE_THRESHOLD
                val hasMorePhotos = state.visibleCount < state.photos.size
                if (reachedEnd && hasMorePhotos) {
                    onLoadMore()
                }
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.photos_title)) })
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            when {
                state.isLoading && visiblePhotos.isEmpty() -> {
                    CircularProgressIndicator(Modifier.testTag("initial_loading"))
                }

                state.error != null && visiblePhotos.isEmpty() -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = stringResource(state.error.messageRes))
                        Button(onClick = onRetry) { Text(stringResource(R.string.retry)) }
                    }
                }

                visiblePhotos.isEmpty() -> {
                    Text(stringResource(R.string.empty_photos))
                }

                else -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("photo_list"),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(visiblePhotos, key = { photo -> photo.id }) { photo ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                                    .clickable { onPhotoClick(photo) }
                            ) {
                                Text(
                                    text = photo.fileName,
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoDetailScreen(
    photo: Photo,
    onBack: () -> Unit,
    imageLoader: ImageLoader = LocalContext.current.imageLoader
) {
    val context = LocalContext.current
    val author = if (photo.author.isNullOrBlank()) {
        stringResource(R.string.unknown_author)
    } else {
        photo.author
    }

    val isLandscapeImage = photo.width > photo.height
    var retryCount by remember(photo.imageUrl) { mutableIntStateOf(0) }
    var imageState by remember(photo.imageUrl, retryCount) {
        mutableStateOf(ImageState.Loading)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.detail_title),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    Button(
                        onClick = onBack,
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.back),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .testTag("detail_viewport"),
        ) {
            val imageHeight = maxWidth * (photo.height.toFloat() / photo.width)
            val topSpace = if (isLandscapeImage) {
                ((maxHeight - imageHeight) / 2).coerceAtLeast(0.dp)
            } else {
                0.dp
            }
            val request = remember(context, photo.imageUrl, retryCount) {
                ImageRequest.Builder(context)
                    .data(photo.imageUrl)
                    .placeholder(R.drawable.image_placeholder)
                    .crossfade(false)
                    .build()
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(Modifier.height(topSpace))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(imageHeight)
                        .testTag("photo_image"),
                    contentAlignment = Alignment.Center
                ) {
                    key(retryCount) {
                        AsyncImage(
                            model = request,
                            imageLoader = imageLoader,
                            contentDescription = stringResource(R.string.image_by_author, author),
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit,
                            onLoading = { imageState = ImageState.Loading },
                            onSuccess = { imageState = ImageState.Loaded },
                            onError = { imageState = ImageState.Failed }
                        )
                    }
                    if (imageState == ImageState.Loading) {
                        CircularProgressIndicator(Modifier.size(24.dp).testTag("image_loading"))
                    }
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    text = author,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .testTag("photo_author"),
                )
                if (imageState == ImageState.Failed) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(stringResource(R.string.image_load_error))
                        Button(onClick = { retryCount++ }) {
                            Text(stringResource(R.string.retry_image))
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
            }
        }

    }

}

private enum class ImageState { Loading, Loaded, Failed }