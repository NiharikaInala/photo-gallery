package com.example.photogallery.di

import android.content.Context
import coil.ImageLoader
import com.example.photogallery.data.PhotoRepository
import com.example.photogallery.data.PhotoRepositoryImpl
import com.example.photogallery.data.PicsumApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideImageLoader(@ApplicationContext context: Context): ImageLoader =
        ImageLoader.Builder(context).build()

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder().build()

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl(PicsumApi.BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    @Provides
    @Singleton
    fun providePicsumApi(retrofit: Retrofit): PicsumApi = retrofit.create(PicsumApi::class.java)

    @Provides
    @Singleton
    fun providePhotoRepository(api: PicsumApi): PhotoRepository = PhotoRepositoryImpl(api)
}