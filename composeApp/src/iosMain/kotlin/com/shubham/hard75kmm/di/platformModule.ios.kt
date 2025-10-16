package com.shubham.hard75kmm.di

import com.shubham.hard75kmm.data.ImageStorage
//import com.shubham.hard75kmm.data.db.DatabaseDriverFactory
import com.shubham.hard75kmm.data.getDatabaseBuilder
import com.shubham.hard75kmm.data.repositories.AuthService
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule = module {
//    single { DatabaseDriverFactory() }
    single { getDatabaseBuilder() }
    factory { AuthService() }
    single { ImageStorage() }
}