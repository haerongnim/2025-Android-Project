package com.wingwing.spendy.ui.theme.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable

fun ChartScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center, ) {
        Text("📊 소비 통계 화면입니다")
    }
}