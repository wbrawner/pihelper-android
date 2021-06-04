package com.wbrawner.pihelper

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import com.wbrawner.piholeclient.NAME_BASE_URL
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

const val ENCRYPTED_SHARED_PREFS_FILE_NAME = "pihelper.prefs"

@Module
@InstallIn(SingletonComponent::class)
object PiHelperModule {
    @Provides
    @Singleton
    fun providesSharedPreferences(@ApplicationContext context: Context): SharedPreferences =
        EncryptedSharedPreferences.create(
            ENCRYPTED_SHARED_PREFS_FILE_NAME,
            "pihelper",
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

    @Provides
    @Singleton
    @Named(NAME_BASE_URL)
    fun providesBaseUrl(sharedPreferences: SharedPreferences) = sharedPreferences
        .getString(KEY_BASE_URL, "")
}