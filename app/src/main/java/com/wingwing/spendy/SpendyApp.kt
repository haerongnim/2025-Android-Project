package com.wingwing.spendy

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.wingwing.spendy.ui.theme.screens.* // 네가 작성한 화면 컴포저블이 여기에 있다면

import androidx.compose.runtime.collectAsState
//import androidx.compose.runtime.livedata.observeAsState

// material icons
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Group
import com.wingwing.spendy.navigation.Screen

@Composable
fun SpendyApp() {
    val navController = rememberNavController()

    val items = listOf(
        Screen.Home,
        Screen.History,
        Screen.Chart,
        Screen.Friends
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
                items.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        selected = currentRoute == screen.route,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.startDestinationId) { inclusive = false }
                                launchSingleTop = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) { HomeScreen() }
            composable(Screen.History.route) { HistoryScreen() }
            composable(Screen.Chart.route) { ChartScreen() }
            composable(Screen.Friends.route) { FriendsScreen() }
        }
    }
}