package com.example.phototranslate.usecase

import kotlinx.coroutines.flow.Flow

/**
 * Base interface for all use cases in the application.
 * Use Cases encapsulate business logic and are injected into ViewModels.
 */
interface UseCase<out Result> {
    operator fun invoke(): Result
}

/**
 * Flow-based use case for asynchronous operations that emit multiple values over time.
 * Used for operations like model download progress, real-time OCR processing, etc.
 */
abstract class FlowUseCase<Result> : UseCase<Flow<Result>>() {
    abstract fun executeInternal(): Flow<Result>

    override fun invoke(): Flow<Result> = executeInternal()
}
