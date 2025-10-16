package com.shubham.hard75kmm.data.db.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.TypeConverters
import com.shubham.hard75kmm.data.db.ChallengeDao
import com.shubham.hard75kmm.data.db.converters.DayStatusConverter
import com.shubham.hard75kmm.data.db.converters.StringListConverter
import com.shubham.hard75kmm.data.db.entities.ChallengeDay

@Database(
    entities = [ChallengeDay::class],
    version = 1
)
@TypeConverters(StringListConverter::class, DayStatusConverter::class)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun challengeDao(): ChallengeDao
}

@Suppress("KotlinNoActualForExpect")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}