package com.piggylabs.piggyflow.features.tracker.di

import com.piggylabs.piggyflow.features.tracker.data.BrandfetchLogoRepository
import com.piggylabs.piggyflow.features.tracker.domain.repository.BrandLogoRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class TrackerModule {

    @Binds
    @Singleton
    abstract fun bindBrandLogoRepository(impl: BrandfetchLogoRepository): BrandLogoRepository
}
