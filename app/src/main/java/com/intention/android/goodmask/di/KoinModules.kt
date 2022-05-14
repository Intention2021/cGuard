//package com.intention.android.goodmask.di
//
//import com.intention.android.goodmask.BleRepository
//import com.intention.android.goodmask.viewmodel.BleViewModel
//import org.koin.androidx.viewmodel.dsl.viewModel
//import org.koin.dsl.module
//
//val viewModelModule = module {
//    viewModel { BleViewModel(get()) }
//}
//
//val repositoryModule = module{
//    single{
//        BleRepository()
//    }
//}