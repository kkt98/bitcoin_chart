package com.kkt981019.bitcoin_chart.screen

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.kkt981019.bitcoin_chart.viewmodel.MyPageViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyPageScreen(navController: NavHostController,
                 myPageViewModel: MyPageViewModel = hiltViewModel())
{


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("내 정보") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "뒤로가기")
                    }
                },
                // 오른쪽 끝 액션 버튼 추가
                actions = {
                    Button(
                        onClick = {
                            myPageViewModel.onCharge(10_000_000)
                            Toast.makeText(
                                navController.context,
                                "1,000만 원 충전 완료!",
                                Toast.LENGTH_SHORT
                            ).show()
                            Log.d("chargemoneyyyyy", myPageViewModel.balance.toString())
                        },
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .height(36.dp),
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Red,
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                    ) {
                        Text("충전", fontSize = 13.sp)
                    }
                }
            )
        }
    )  { inner ->
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