package com.shubham.hard75kmm.di
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

fun initKoin(appDeclaration: KoinAppDeclaration = {}) {
    startKoin {
        appDeclaration()
        modules(
            commonModule,
            platformModule // This will bring in the correct platform-specific dependencies
        )
    }
}
