package com.example.photogallery.data

import retrofit2.http.GET
import javax.inject.Inject

interface PicsumApi {
    companion object {
        const val BASE_URL = "https://picsum.photos/"
    }

    @GET("list")
    suspend fun getPhotos(): List<PicsumPhotoDto>
}

interface PhotoRepository {
    suspend fun loadPhotos(): List<Photo>
}

class PhotoRepositoryImpl @Inject constructor(
    private val api: PicsumApi
) : PhotoRepository {
    override suspend fun loadPhotos(): List<Photo> = api.getPhotos().map { dto ->
        val width = dto.width.coerceAtLeast(1)
        val height = dto.height.coerceAtLeast(1)

        Photo(
            id = dto.id,
            fileName = dto.fileName ?: "photo-${dto.id}",
            width = width,
            height = height,
            author = dto.author,
            imageUrl = "${PicsumApi.BASE_URL}$width/$height?image=${dto.id}"
        )
    }
}