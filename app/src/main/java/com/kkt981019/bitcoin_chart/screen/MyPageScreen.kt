package com.kkt981019.bitcoin_chart.screen

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
                title = { Text("투자 내역") },
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
                                "1,000만 원 충전 완료",
                                Toast.LENGTH_SHORT
                            ).show()
                            Log.d("chargemoneyyyyy", myPageViewModel.balance.toString())
                        },
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .height(36.dp),
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.buttonColors(
                            contentColor = Color.Black,
                            containerColor = Color.Transparent
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFF8F8F8)),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                    ) {
                        Text("KRW 충전", fontSize = 13.sp)
                    }
                }
            )
        }
    ) { inner ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(inner)
        ) {
            // 요약 정보 박스
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF8F8F8))
                    .padding(16.dp)
            ) {
                Column {
                    // 윗줄 (보유 KRW, 총 보유자산)
                    Row(Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("보유 KRW", fontSize = 13.sp, color = Color.Gray)
                            Text("0", fontSize = 24.sp, color = Color.Black)
                        }

                        Spacer(Modifier.width(10.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text("총 보유자산", fontSize = 13.sp, color = Color.Gray)
                            Text("0", fontSize = 24.sp, color = Color.Black)
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    // 아랫줄 1 (총매수 / 평가손익)
                    Row(Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.weight(1f),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("총매수", fontSize = 13.sp, color = Color.Gray)
                            Text("0", fontSize = 14.sp, color = Color.Black)
                        }

                        Spacer(Modifier.width(10.dp))

                        Row(
                            modifier = Modifier.weight(1f),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("평가손익", fontSize = 13.sp, color = Color.Gray)
                            Text("0", fontSize = 14.sp, color = Color(0xFF1976D2)) // 파란색
                        }
                    }

                    Spacer(Modifier.height(4.dp))

                    // 아랫줄 2 (총평가 / 수익률)
                    Row(Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.weight(1f),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("총평가", fontSize = 13.sp, color = Color.Gray)
                            Text("0", fontSize = 14.sp, color = Color.Black)
                        }

                        Spacer(Modifier.width(10.dp))

                        Row(
                            modifier = Modifier.weight(1f),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("수익률", fontSize = 13.sp, color = Color.Gray)
                            Text("0.00%", fontSize = 14.sp, color = Color(0xFF1976D2))
                        }
                    }
                }
            }
            Row(
                Modifier.padding(start = 16.dp, end = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.Search, contentDescription = null)
//                Spacer(Modifier.height(4.dp))
                TextField(
                    value = "",
                    onValueChange = {  },
                    placeholder = { Text("코인명/심볼 검색", color = Color.Gray) },
                    modifier = Modifier.weight(1f),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor   = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor   = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor             = Color.Black,
                    )
                )
            }
        }
    }
}