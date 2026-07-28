package com.example.phototranslate.di

import android.content.Context
import com.example.phototranslate.application.PhotoTranslateApp
import com.example.phototranslate.data.history.AppDatabase
import com.example.phototranslate.engine.MlKitTranslateEngine
import com.example.phototranslate.repository.DefaultHistoryRepository
import com.example.phototranslate.repository.HistoryRepository
import com.example.phototranslate.usecase.DefaultHistoryUseCase
import com.example.phototranslate.usecase.HistoryUseCase
import com.google.mlkit.textrecognition.TextRecognition
import dagger.Module
import dagger.Provides
import dagger.Singleton
import dagger.hilt.InstallIn
import dagger.hilt.components.AndroidComponent

/**
 * Extended Hilt module with History and Engine dependencies.
 */
@Module
@InstallIn(AndroidComponent.APPLICATION::class)
object AppModuleExtensions {

    /**
     * Provides the Room database instance.
     */
    @Provides
    @Singleton
    fun provideDatabase(context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }

    /**
     * Provides the HistoryRepository implementation.
     */
    @Provides
    @Singleton
    fun provideHistoryRepository(database: AppDatabase): HistoryRepository {
        return DefaultHistoryRepository(database)
    }

    /**
     * Provides the HistoryUseCase implementation.
     */
    @Provides
    @Singleton
    fun provideHistoryUseCase(historyRepository: HistoryRepository): HistoryUseCase {
        return DefaultHistoryUseCase(historyRepository)
    }

    /**
     * Provides the ML Kit Translate Engine.
     */
    @Provides
    @Singleton
    fun provideMlKitTranslateEngine(translateUseCase: com.example.phototranslate.usecase.TranslateUseCase): MlKitTranslateEngine {
        return MlKitTranslateEngine(translateUseCase)
    }
}
