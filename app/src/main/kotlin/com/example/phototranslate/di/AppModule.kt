package com.example.phototranslate.di

import android.content.Context
import android.app.Application
import com.example.phototranslate.application.PhotoTranslateApp
import com.example.phototranslate.repository.DefaultOcrRepository
import com.example.phototranslate.repository.DefaultTranslateRepository
import com.example.phototranslate.repository.OcrRepository
import com.example.phototranslate.repository.TranslateRepository
import com.example.phototranslate.usecase.DefaultOcrUseCase
import com.example.phototranslate.usecase.DefaultTranslateUseCase
import com.example.phototranslate.usecase.OcrUseCase
import com.example.phototranslate.usecase.TranslateUseCase
import com.google.mlkit.textrecognition.TextRecognition
import dagger.Module
import dagger.Provides
import dagger.Singleton
import dagger.hilt.InstallIn
import dagger.hilt.components.AndroidComponent

/**
 * Hilt module for providing application-scoped dependencies.
 * This module injects ML Kit clients, repositories, and use cases into the ViewModel layer.
 * 
 * Follows clean architecture principles:
 * - Application-level components (ML Kit clients) are provided at app scope
 * - Repositories wrap the SDK details
 * - Use Cases contain business logic and depend on Repositories
 */
@Module
@InstallIn(AndroidComponent.APPLICATION::class)
object AppModule {

    /**
     * Provides the ML Kit Text Recognition client.
     * Singleton instance shared across the app for efficiency.
     * Configured for FAST performance mode suitable for real-time OCR.
     */
    @Provides
    @Singleton
    fun provideTextRecognitionClient(): TextRecognition {
        return PhotoTranslateApp.app().getTextRecognitionClient()
    }

    /**
     * Provides the OcrRepository implementation.
     * Wraps the ML Kit TextRecognition client behind a clean repository interface.
     */
    @Provides
    @Singleton
    fun provideOcrRepository(textRecognitionClient: TextRecognition): OcrRepository {
        return DefaultOcrRepository(textRecognitionClient)
    }

    /**
     * Provides the OcrUseCase implementation.
     * Encapsulates OCR business logic using the OcrRepository.
     * Injected into ViewModels for use in UI components.
     */
    @Provides
    @Singleton
    fun provideOcrUseCase(ocrRepository: OcrRepository): OcrUseCase {
        return DefaultOcrUseCase(ocrRepository)
    }

    /**
     * Provides the TranslateRepository implementation.
     * Wraps the ML Kit Translate SDK with model management capabilities.
     * Injected with the Application context for model management operations.
     */
    @Provides
    @Singleton
    fun provideTranslateRepository(@Application context: Context): TranslateRepository {
        return DefaultTranslateRepository(context)
    }

    /**
     * Provides the TranslateUseCase implementation.
     * Encapsulates translation business logic including language detection
     * and model management. Injected into ViewModels.
     */
    @Provides
    @Singleton
    fun provideTranslateUseCase(translateRepository: TranslateRepository): TranslateUseCase {
        return DefaultTranslateUseCase(translateRepository)
    }
}
