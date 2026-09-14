package com.example.photogallery.data

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class PhotoRepositoryTest {
    @Test
    fun repositoryMapsLegacyPicsumResponse() = runTest {
        val repository = PhotoRepositoryImpl(
            object : PicsumApi {
                override suspend fun getPhotos(): List<PicsumPhotoDto> = listOf(
                    PicsumPhotoDto(
                        id = 0,
                        fileName = "0.jpeg",
                        width = 5000,
                        height = 3333,
                        author = "Author One",
                    ),
                    PicsumPhotoDto(
                        id = 1,
                        fileName = "1.jpeg",
                        width = 300,
                        height = 500,
                        author = "Author Two",
                    ),
                )
            }
        )

        val photos = repository.loadPhotos()

        assertEquals(2, photos.size)
        assertEquals("0.jpeg", photos.first().fileName)
        assertEquals("Author One", photos.first().author)
        assertEquals("https://picsum.photos/5000/3333?image=0", photos.first().imageUrl)
    }

    @Test
    fun repositoryUsesFallbackValuesWhenApiFieldsAreMissing() = runTest {
        val repository = PhotoRepositoryImpl(
            object : PicsumApi {
                override suspend fun getPhotos(): List<PicsumPhotoDto> = listOf(
                    PicsumPhotoDto(
                        id = 99,
                        width = 0,
                        height = 0,
                    )
                )
            }
        )

        val photo = repository.loadPhotos().first()

        assertEquals("photo-99", photo.fileName)
        assertEquals(null, photo.author)
        assertEquals("https://picsum.photos/1/1?image=99", photo.imageUrl)
    }
}
