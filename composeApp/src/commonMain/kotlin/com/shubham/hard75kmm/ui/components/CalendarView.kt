package com.shubham.hard75kmm.ui.components
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.shubham.hard75kmm.db.Challenge_days

@Composable
fun CalendarView(days: List<Challenge_days>) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        contentPadding = PaddingValues(8.dp)
    ) {
        items(days, key = { it.dayNumber }) { day ->
            DayCell(day)
        }
    }
}
