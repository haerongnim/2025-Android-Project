package com.wingwing.spendy.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "홈", Icons.Default.Home)
    object History : Screen("history", "내역", Icons.Default.List)
    object Chart : Screen("chart", "통계", Icons.Default.PieChart)
    object Friends : Screen("friends", "친구", Icons.Default.Group)
}