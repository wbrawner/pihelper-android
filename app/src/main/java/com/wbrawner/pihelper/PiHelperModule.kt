package com.wbrawner.pihelper

import androidx.security.crypto.EncryptedSharedPreferences
import com.wbrawner.piholeclient.NAME_BASE_URL
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

const val ENCRYPTED_SHARED_PREFS_FILE_NAME = "pihelper.prefs"

val piHelperModule = module {
    single {
        EncryptedSharedPreferences.create(
            ENCRYPTED_SHARED_PREFS_FILE_NAME,
            "pihelper",
            get(),
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    viewModel {
        AddPiHelperViewModel(get(), get())
    }

    viewModel {
        PiHelperViewModel(get())
    }

    single(named(NAME_BASE_URL)) {
        get<EncryptedSharedPreferences>().getString(KEY_BASE_URL, "")
    }
}