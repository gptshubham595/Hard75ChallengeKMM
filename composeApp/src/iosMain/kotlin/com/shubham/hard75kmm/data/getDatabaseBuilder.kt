package com.shubham.hard75kmm.data

import androidx.room.Room
import androidx.room.RoomDatabase
import com.shubham.hard75kmm.data.db.database.AppDatabase
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask

fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {
    val dbFilePath = NSSearchPathForDirectoriesInDomains(
        NSDocumentDirectory,
        NSUserDomainMask,
        true
    ).first() as String + "/hard75.db"
    return Room.databaseBuilder<AppDatabase>(
        name = dbFilePath,
    )
}