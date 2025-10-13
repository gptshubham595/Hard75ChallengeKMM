package com.shubham.hard75kmm.data.db.entities

enum class DayStatus {
    LOCKED,      // Future days (Gray)
    FAILED,      // Not started or missed (Red)
    IN_PROGRESS, // Some tasks done (Yellow)
    COMPLETED;    // All tasks done (Green)

    companion object{
        fun getRandomStatus(): DayStatus {
            return entries.toTypedArray().random()
        }
    }
}