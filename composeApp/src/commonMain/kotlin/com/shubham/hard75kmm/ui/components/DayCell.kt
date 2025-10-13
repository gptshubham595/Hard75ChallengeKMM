package com.shubham.hard75kmm.ui.components
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shubham.hard75kmm.data.db.entities.DayStatus
import com.shubham.hard75kmm.db.Challenge_days

@Composable
fun DayCell(day: Challenge_days) {
    val color = when (day.status) {
        DayStatus.LOCKED -> Color.Gray
        DayStatus.FAILED -> Color.Red
        DayStatus.IN_PROGRESS -> Color(0xFFE69B00) // An amber/yellow color
        DayStatus.COMPLETED -> Color(0xFF2E7D32) // A dark green color
    }

    Card(
        modifier = Modifier
            .padding(4.dp)
            .aspectRatio(1f),
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Text(
                text = day.dayNumber.toString(),
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
}