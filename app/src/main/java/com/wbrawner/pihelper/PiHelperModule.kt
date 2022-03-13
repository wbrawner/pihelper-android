package com.wbrawner.pihelper

import com.wbrawner.pihelper.shared.PiholeAPIService
import com.wbrawner.pihelper.shared.Store
import com.wbrawner.pihelper.shared.create
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PiHelperModule {
    @Provides
    @Singleton
    fun providesPiholeAPIService(): PiholeAPIService = PiholeAPIService.create()

    @Provides
    @Singleton
    fun providesStore(
        apiService: PiholeAPIService,
    ): Store = Store(apiService)
}