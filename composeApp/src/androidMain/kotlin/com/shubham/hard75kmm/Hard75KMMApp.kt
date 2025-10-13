package com.shubham.hard75kmm

import android.app.Application
import com.shubham.hard75kmm.di.initKoin
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger

class Hard75KMMApp : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidLogger()
            androidContext(this@Hard75KMMApp)
        }
    }

}