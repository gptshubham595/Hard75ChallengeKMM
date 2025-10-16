package com.shubham.hard75kmm.di

import com.shubham.hard75kmm.data.ImageStorage
//import com.shubham.hard75kmm.data.db.DatabaseDriverFactory
import com.shubham.hard75kmm.data.db.getDatabaseBuilder
import com.shubham.hard75kmm.data.repositories.AuthService
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    // This provides the Android-specific database driver
//    single { DatabaseDriverFactory(get()) } // This might be removable if not used elsewhere
    single { getDatabaseBuilder(get()) }
    // This provides the Android version of AuthService, which requires a Context
    factory { AuthService() }

    single { ImageStorage(get()) }
}

