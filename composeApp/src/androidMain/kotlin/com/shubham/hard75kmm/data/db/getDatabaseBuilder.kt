package com.shubham.hard75kmm.data.db

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import com.shubham.hard75kmm.data.db.database.AppDatabase

fun getDatabaseBuilder(context: Context): RoomDatabase.Builder<AppDatabase> {
    val appContext = context.applicationContext
    val dbFile = appContext.getDatabasePath("hard75.db")
    return Room.databaseBuilder<AppDatabase>(
        context = appContext,
        name = dbFile.absolutePath
    )
}