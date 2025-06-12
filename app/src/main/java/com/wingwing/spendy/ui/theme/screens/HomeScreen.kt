package com.wingwing.spendy.ui.theme.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.wingwing.spendy.navigation.Screen


@Composable
fun HomeScreen(navController: NavController = rememberNavController()) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("🏠 홈 화면입니다")
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = { navController.navigate(Screen.Ocr.route) }) {
                Text("OCR로 영수증 등록")
            }
        }
    }
}