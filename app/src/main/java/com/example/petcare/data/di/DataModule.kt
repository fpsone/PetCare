package com.example.petcare.data.di

import com.example.petcare.BuildConfig
import com.example.petcare.data.datasource.GeminiApiService
import com.example.petcare.data.repository.PetProfileRepositoryImpl
import com.example.petcare.data.repository.WearableDataRepositoryImpl
import com.example.petcare.data.repository.ChatRepositoryImpl
import com.example.petcare.data.repository.RankingRepositoryImpl
import com.example.petcare.domain.repository.PetProfileRepository
import com.example.petcare.domain.repository.WearableDataRepository
import com.example.petcare.domain.repository.ChatRepository
import com.example.petcare.domain.repository.RankingRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

private const val GEMINI_API_BASE_URL = "https://generativelanguage.googleapis.com/"

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {
    @Binds
    @Singleton
    abstract fun bindPetProfileRepository(impl: PetProfileRepositoryImpl): PetProfileRepository
    @Binds
    @Singleton
    abstract fun bindWearableDataRepository(impl: WearableDataRepositoryImpl): WearableDataRepository

    @Binds
    @Singleton
    abstract fun bindChatRepository(impl: ChatRepositoryImpl): ChatRepository

    @Binds
    @Singleton
    abstract fun bindRankingRepository(impl: RankingRepositoryImpl): RankingRepository

    companion object {
        @Provides
        @Singleton
        fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
            .build()

        @Provides
        @Singleton
        fun provideGeminiApiService(okHttpClient: OkHttpClient): GeminiApiService = Retrofit.Builder()
            .baseUrl(GEMINI_API_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GeminiApiService::class.java)
    }
}