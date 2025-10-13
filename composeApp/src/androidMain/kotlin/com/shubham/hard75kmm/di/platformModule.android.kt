package com.shubham.hard75kmm.di

import com.shubham.hard75kmm.data.db.DatabaseDriverFactory
import org.koin.dsl.module

actual val platformModule = module {
    single { DatabaseDriverFactory(get()) }
}
