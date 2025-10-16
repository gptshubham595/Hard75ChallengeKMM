package com.shubham.hard75kmm.di

import com.russhwolf.settings.Settings
import com.shubham.hard75kmm.data.db.database.AppDatabase
import com.shubham.hard75kmm.data.db.database.getRoomDatabase
import com.shubham.hard75kmm.data.repositories.ChallengeRepository
import com.shubham.hard75kmm.data.repositories.TaskRepository
import com.shubham.hard75kmm.ui.viewmodel.AuthViewModel
import com.shubham.hard75kmm.ui.viewmodel.ChallengeViewModel
import com.shubham.hard75kmm.ui.viewmodel.GalleryViewModel
import com.shubham.hard75kmm.ui.viewmodel.LeaderboardViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module


val commonModule = module {
//    single<AppDatabase // SQLITE> {
//        val driver = get<DatabaseDriverFactory>().createDriver()
//
//        val challengeDaysAdapter = Challenge_days.Adapter(
//            statusAdapter = object : ColumnAdapter<DayStatus, String> {
//                override fun decode(databaseValue: String): DayStatus =
//                    DayStatus.valueOf(databaseValue)
//
//                override fun encode(value: DayStatus): String = value.name
//            },
//            completedTaskIdsAdapter = object : ColumnAdapter<List<String>, String> {
//                override fun decode(databaseValue: String): List<String> =
//                    if (databaseValue.isEmpty()) listOf() else databaseValue.split(",")
//
//                override fun encode(value: List<String>): String = value.joinToString(",")
//            }
//        )
//
//        AppDatabase(
//            driver = driver,
//            challenge_daysAdapter = challengeDaysAdapter
//        )
//    }

    single<AppDatabase> { getRoomDatabase(get()) }
    single { get<AppDatabase>().challengeDao() }

    // Settings (for key-value storage)
    single { Settings() }

    // Repositories
    singleOf(::ChallengeRepository)
    singleOf(::TaskRepository)

    // ViewModels (ViewModels)
    viewModelOf(::AuthViewModel)
    viewModelOf(::ChallengeViewModel)
    viewModelOf(::GalleryViewModel)
    viewModelOf(::LeaderboardViewModel)
}
