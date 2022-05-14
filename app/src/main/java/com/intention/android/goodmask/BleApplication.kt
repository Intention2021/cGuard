//package com.intention.android.goodmask
//
//import android.app.Application
//import android.bluetooth.BluetoothAdapter.ERROR
//import android.content.Context
//import com.intention.android.goodmask.di.repositoryModule
//import com.intention.android.goodmask.di.viewModelModule
//import org.koin.android.ext.koin.androidContext
//import org.koin.android.ext.koin.androidFileProperties
//import org.koin.android.ext.koin.androidLogger
//import org.koin.core.context.GlobalContext.startKoin
//import java.util.logging.Level
//
//class BleApplication : Application() {
//
//    init{
//        instance = this
//    }
////
//    companion object {
//        lateinit var instance: BleApplication
//        fun applicationContext() : Context {
//            return instance.applicationContext
//        }
//    }
//
//    override fun onCreate() {
//        super.onCreate()
//
//        startKoin {
//            androidContext(this@BleApplication)
//            androidFileProperties()
//            modules(listOf(repositoryModule, viewModelModule))
//        }
//
//    }
//}