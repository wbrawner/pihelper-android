package com.wbrawner.pihelper

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module


val piHelperModule = module {
    viewModel {
        AddPiHelperViewModel(get(), get())
    }

    viewModel {
        PiHelperViewModel(get())
    }
}