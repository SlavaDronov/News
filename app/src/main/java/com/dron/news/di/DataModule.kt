package com.dron.news.di

import android.content.Context
import androidx.room.Room
import androidx.work.WorkManager
import com.dron.news.data.local.NewDao
import com.dron.news.data.local.NewsDatabase
import com.dron.news.data.remote.NewsApiService
import com.dron.news.data.repository.NewsRepositoryImpl
import com.dron.news.data.repository.SettingsRepositoryImpl
import com.dron.news.domain.repository.NewsRepository
import com.dron.news.domain.repository.SettingsRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface DataModule {

    @Binds
    @Singleton
    fun bindSettingsRepository(
        impl: SettingsRepositoryImpl
    ): SettingsRepository

    @Binds
    @Singleton
    fun bindNewsRepository(
        impl: NewsRepositoryImpl
    ): NewsRepository

    companion object {

        @Provides
        @Singleton
        fun provideWorkManager(
            @ApplicationContext context: Context
        ): WorkManager = WorkManager.getInstance(context)

        @Provides
        @Singleton
        fun provideJson(): Json {
            return Json {
                ignoreUnknownKeys = true
                coerceInputValues = true
            }
        }

        @Provides
        @Singleton
        fun provideConverterFactory(
            json: Json
        ): Converter.Factory {
            return json.asConverterFactory("application/json".toMediaType())
        }

        // ✅ ЭТОТ МЕТОД ОБЯЗАТЕЛЕН!
        @Provides
        @Singleton
        fun provideOkHttpClient(): OkHttpClient {
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
            return OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .build()
        }

        @Provides
        @Singleton
        fun provideRetrofit(
            converterFactory: Converter.Factory,
            okHttpClient: OkHttpClient  // ← Теперь OkHttpClient будет предоставлен!
        ): Retrofit {
            return Retrofit.Builder()
                .baseUrl("https://newsapi.org/")
                .client(okHttpClient)
                .addConverterFactory(converterFactory)
                .build()
        }

        @Provides
        @Singleton
        fun provideApiService(
            retrofit: Retrofit
        ): NewsApiService {
            return retrofit.create(NewsApiService::class.java)
        }

        @Provides
        @Singleton
        fun provideNewsDatabase(
            @ApplicationContext context: Context
        ): NewsDatabase {
            return Room.databaseBuilder(
                context = context,
                klass = NewsDatabase::class.java,
                name = "news.db"
            ).fallbackToDestructiveMigration(true).build()
        }

        @Provides
        @Singleton
        fun provideNewsDao(
            database: NewsDatabase
        ): NewDao = database.newDao()
    }
}