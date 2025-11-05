package com.kkt981019.bitcoin_chart.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyPageScreen(navController: NavHostController)  {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("내 정보") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "뒤로가기")
                    }
                }
            )
        }
    ) { inner ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(inner),
            contentAlignment = Alignment.Center
        ) {
            Text("여기에 가상머니 충전 & 거래내역 화면이 들어올 예정입니다.")
        }
    }
}