package com.wingwing.spendy.ui.theme.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun FriendsScreen() {
    var searchQuery by remember { mutableStateOf("") }
    var friends by remember { mutableStateOf(listOf("철수", "영희", "민수")) }
    var newFriend by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // 검색/추가 영역
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text("친구 검색") },
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { /* 검색 기능 */ }) {
                        Icon(Icons.Default.Search, contentDescription = "검색")
                    }
                }
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = newFriend,
                        onValueChange = { newFriend = it },
                        label = { Text("친구 추가") },
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = {
                        if (newFriend.isNotBlank()) {
                            friends = friends + newFriend
                            newFriend = ""
                        }
                    }) {
                        Icon(Icons.Default.Add, contentDescription = "추가")
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        Text(
            text = "친구 목록",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))

        // 친구 리스트
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(friends.filter { it.contains(searchQuery) }) { friend ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize(),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(6.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 아바타(이니셜)
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = friend.take(1),
                                color = Color.White,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(Modifier.width(16.dp))
                        Text(
                            text = friend,
                            modifier = Modifier.weight(1f),
                            fontSize = 18.sp
                        )
                        IconButton(onClick = { /* 채팅 */ }) {
                            Icon(Icons.Default.Chat, contentDescription = "채팅")
                        }
                        IconButton(onClick = { /* 통계 */ }) {
                            Icon(Icons.Default.ShowChart, contentDescription = "통계")
                        }
                    }
                }
            }
        }
    }
}