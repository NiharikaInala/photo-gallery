package com.example.photogallery.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import coil.ImageLoader
import coil.decode.DataSource
import coil.intercept.Interceptor
import coil.request.ErrorResult
import coil.request.SuccessResult
import com.example.photogallery.data.Photo
import java.io.IOException
import java.util.concurrent.CopyOnWriteArrayList
import kotlinx.coroutines.CompletableDeferred

internal fun testPhoto(width: Int = 600, height: Int = 400) = Photo(
    id = 1L,
    fileName = "sample.jpg",
    width = width,
    height = height,
    author = "Alice",
    imageUrl = "https://example.test/photo",
)

internal class TestImageLoader(
    private val failFirstRequest: Boolean = false,
    private val requestGate: CompletableDeferred<Unit>? = null,
) {
    val requests = CopyOnWriteArrayList<Any>()

    fun create(context: Context): ImageLoader {
        val bitmap = Bitmap.createBitmap(30, 20, Bitmap.Config.ARGB_8888).apply {
            eraseColor(Color.BLUE)
        }
        return ImageLoader.Builder(context).components {
            add(Interceptor { chain ->
                requests.add(chain.request.data)
                requestGate?.await()
                if (failFirstRequest && requests.size == 1) {
                    ErrorResult(null, chain.request, IOException("Test image failure"))
                } else {
                    SuccessResult(
                        drawable = BitmapDrawable(context.resources, bitmap),
                        request = chain.request,
                        dataSource = DataSource.MEMORY,
                    )
                }
            })
        }.build()
    }
}
