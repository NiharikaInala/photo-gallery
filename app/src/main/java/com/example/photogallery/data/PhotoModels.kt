package com.example.photogallery.data

import com.google.gson.annotations.SerializedName

data class Photo(
    val id: Long,
    val fileName: String,
    val width: Int,
    val height: Int,
    val author: String?,
    val imageUrl: String,
)

data class PicsumPhotoDto(
    val id: Long,

    val author: String? = null,

    @SerializedName("author_url")
    val authorUrl: String? = null,

    @SerializedName("filename")
    val fileName: String? = null,

    val format: String? = null,

    val width: Int = 0,

    val height: Int = 0,

    @SerializedName("post_url")
    val postUrl: String? = null
)

