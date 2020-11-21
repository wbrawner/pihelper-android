package com.wbrawner.pihelper

import androidx.security.crypto.EncryptedSharedPreferences
import com.wbrawner.pihelper.shared.KtorPiHoleApiService
import com.wbrawner.pihelper.shared.PiHoleApiService
import com.wbrawner.pihelper.shared.httpClient
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

const val ENCRYPTED_SHARED_PREFS_FILE_NAME = "pihelper.prefs"
const val NAME_BASE_URL = "baseUrl"

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

    single<PiHoleApiService> {
        KtorPiHoleApiService(httpClient())
    }
}