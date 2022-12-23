package com.wbrawner.pihelper

import com.wbrawner.pihelper.shared.PiholeAPIService
import com.wbrawner.pihelper.shared.Store
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [PiHelperModule::class]
)
abstract class TestModule {

    @Binds
    @Singleton
    abstract fun bindsPiholeAPIService(apiService: FakeAPIService): PiholeAPIService

    companion object {
        @Provides
        @Singleton
        fun providesStore(
            apiService: PiholeAPIService,
        ): Store = Store(apiService)
    }
}
