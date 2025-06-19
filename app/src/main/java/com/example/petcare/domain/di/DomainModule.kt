package com.example.petcare.domain.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object DomainModule {
    // If you have use cases or other domain-specific classes that need to be
    // provided by Hilt, add them here using @Provides.
    // For example:
    // @Provides @Singleton fun provideMyUseCase(): MyUseCase = MyUseCase()
}