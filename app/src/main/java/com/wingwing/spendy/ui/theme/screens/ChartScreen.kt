package com.wingwing.spendy.ui.theme.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wingwing.spendy.ui.theme.Yellow40

data class StatItem(
    val category: String,
    val amount: Int
)

@Composable
fun ChartScreen() {
    val statList = listOf(
        StatItem("카페/음료", 12000),
        StatItem("식비", 32000),
        StatItem("마트/편의점", 15000),
        StatItem("교통", 8000)
    )
    val total = statList.sumOf { it.amount }.takeIf { it > 0 } ?: 1

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Text(
            text = "소비 통계",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        Spacer(Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Yellow40),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.PieChart,
                    contentDescription = "통계",
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )
                Spacer(Modifier.width(16.dp))
                Column {
                    Text("총 소비", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black)
                    Text("₩${total}", fontSize = 16.sp, color = Color.Black)
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        Text("카테고리별", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Yellow40)
        Spacer(Modifier.height(8.dp))

        statList.forEach { stat ->
            Column(Modifier.padding(vertical = 8.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stat.category, fontSize = 16.sp, modifier = Modifier.weight(1f), color = Color.Black)
                    Text("₩${stat.amount}", fontWeight = FontWeight.Bold, color = Color.Black)
                }
                LinearProgressIndicator(
                    progress = stat.amount / total.toFloat(),
                    color = Yellow40,
                    trackColor = Color(0xFFFFFDE7),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                )
            }
        }
    }
}