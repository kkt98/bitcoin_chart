package com.kkt981019.bitcoin_chart.screen

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.kkt981019.bitcoin_chart.viewmodel.MyPageViewModel
import java.text.DecimalFormat
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import com.kkt981019.bitcoin_chart.viewmodel.TradeHistoryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyPageScreen(
    navController: NavHostController,
    myPageViewModel: MyPageViewModel = hiltViewModel(),
    tradeHistoryViewModel: TradeHistoryViewModel = hiltViewModel()
) {

    val myCoins = myPageViewModel.myCoins

    var textFieldQuery by remember { mutableStateOf("") }

    var sortType by remember { mutableStateOf(SortType.NONE) }

    // 숫자 포맷터들
    val dfInt = remember { DecimalFormat("#,##0") }        // 정수용 (원화)
    val dfDouble = remember { DecimalFormat("#,##0.##") }  // 소수 약간 있는 숫자용

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
                        },
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .height(28.dp)
                            .defaultMinSize(minWidth = 0.dp, minHeight = 0.dp), // 기본 최소 크기 제거,
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.buttonColors(
                            contentColor = Color.Black,
                            containerColor = Color.Transparent
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF000000)),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                    ) {
                        Text("KRW 충전", fontSize = 11.sp)
                    }

                    Button(
                        onClick = {
                            Toast.makeText(
                                navController.context,
                                "삭제 완료",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .height(28.dp)
                            .defaultMinSize(minWidth = 0.dp, minHeight = 0.dp), // 기본 최소 크기 제거,
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.buttonColors(
                            contentColor = Color.Black,
                            containerColor = Color.Transparent
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF000000)),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                    ) {
                        Text("전체 거래내역 삭제", fontSize = 11.sp)
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
                            Text("보유 KRW", fontSize = 12.sp, color = Color.Gray)
                            Text(
                                text = dfInt.format(myPageViewModel.balance),
                                fontSize = 18.sp,
                                color = Color.Black
                            )
                        }

                        Spacer(Modifier.width(10.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text("총 보유자산", fontSize = 12.sp, color = Color.Gray)
                            Text(
                                text = dfInt.format(myPageViewModel.totalAsset),
                                fontSize = 18.sp,
                                color = Color.Black
                            )
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    // 아랫줄 1 (총매수 / 평가손익)
                    Row(Modifier.fillMaxWidth()) {

                        Row(
                            modifier = Modifier.weight(1f),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("총매수", fontSize = 12.sp, color = Color.Gray)
                            Text(
                                text = dfInt.format(myPageViewModel.totalBuyAmount),
                                fontSize = 12.sp,
                                color = Color.Black
                            )
                        }

                        Spacer(Modifier.width(10.dp))

                        Row(
                            modifier = Modifier.weight(1f),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("평가손익", fontSize = 12.sp, color = Color.Gray)

                            val profit = myPageViewModel.totalProfit
                            val profitColor =
                                when {
                                    profit > 0 -> Color.Red
                                    profit < 0 -> Color.Blue
                                    else -> Color.Black
                                }

                            Text(
                                text = dfInt.format(profit),
                                fontSize = 12.sp,
                                color = profitColor
                            )
                        }
                    }

                    Spacer(Modifier.height(6.dp))

                    // 아랫줄 2 (총평가 / 수익률)
                    Row(Modifier.fillMaxWidth()) {

                        Row(
                            modifier = Modifier.weight(1f),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("총평가", fontSize = 12.sp, color = Color.Gray)
                            Text(
                                text = dfInt.format(myPageViewModel.totalEvalAmount),
                                fontSize = 12.sp,
                                color = Color.Black
                            )
                        }

                        Spacer(Modifier.width(10.dp))

                        Row(
                            modifier = Modifier.weight(1f),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("수익률", fontSize = 12.sp, color = Color.Gray)

                            val rate = myPageViewModel.totalProfitRate
                            val rateColor =
                                when {
                                    rate > 0 -> Color.Red
                                    rate < 0 -> Color.Blue
                                    else -> Color.Black
                                }

                            Text(
                                text = String.format("%.2f%%", rate),
                                fontSize = 12.sp,
                                color = rateColor
                            )
                        }
                    }
                }
            }

            Row(
                Modifier.padding(start = 16.dp, end = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.Search, contentDescription = null)
                TextField(
                    value = textFieldQuery,
                    onValueChange = { textFieldQuery = it },
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

            Spacer(Modifier.width(8.dp))

            Divider(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                thickness = 1.dp
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth(),
            ) {
                TextButton(
                    modifier = Modifier.weight(1f),
                    onClick = {
                        sortType = when (sortType) {
                            SortType.NAME_ASC  -> SortType.NAME_DESC   // 오름 → 내림
                            SortType.NAME_DESC -> SortType.NONE        // 내림 → 해제
                            else               -> SortType.NAME_ASC    // 나머지 → 오름
                        }
                    }
                ) {
                    val text = when (sortType) {
                        SortType.NAME_ASC  -> "이름↑"
                        SortType.NAME_DESC -> "이름↓"
                        else               -> "이름↑↓"
                    }

                    Text(
                        text = text,
                        color = if (sortType == SortType.NAME_ASC || sortType == SortType.NAME_DESC)
                            Color.Blue
                        else
                            Color.Gray
                    )
                }

                TextButton(
                    modifier = Modifier.weight(1f),
                    onClick = {
                        sortType = when (sortType) {
                            SortType.PROFIT_DESC -> SortType.PROFIT_ASC  // 내림 → 오름
                            SortType.PROFIT_ASC  -> SortType.NONE        // 오름 → 해제
                            else                 -> SortType.PROFIT_DESC // 나머지 → 내림(기본)
                        }
                    }
                ) {
                    val text = when (sortType) {
                        SortType.PROFIT_ASC -> "수익률↑"
                        SortType.PROFIT_DESC -> "수익률↓"
                        else                 -> "수익률↑↓"
                    }

                    Text(
                        text = text,
                        color = if (sortType == SortType.PROFIT_ASC || sortType == SortType.PROFIT_DESC)
                            Color.Blue
                        else
                            Color.Gray
                    )
                }
            }

            Divider(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                thickness = 1.dp
            )

            // 검색어 기준으로 필터링
            val filteredCoins = if (textFieldQuery.isBlank()) {
                myCoins
            } else {
                myCoins.filter { coin ->
                    coin.korName.contains(textFieldQuery, ignoreCase = true) ||
                            coin.symbol.contains(textFieldQuery, ignoreCase = true)
                }
            }

            // 정렬 적용
            val sortedCoins = when (sortType) {
                SortType.NONE -> filteredCoins

                SortType.NAME_ASC -> {
                    filteredCoins.sortedBy { it.korName }
                }

                SortType.NAME_DESC -> {
                    filteredCoins.sortedByDescending { it.korName }
                }

                SortType.PROFIT_ASC -> {
                    filteredCoins.sortedBy { coin ->
                        val currentPrice = myPageViewModel.getCurrentPrice(
                            symbol = coin.symbol,
                            avgPrice = coin.avgPrice
                        )
                        val buyAmount  = coin.amount * coin.avgPrice
                        val evalAmount = coin.amount * currentPrice
                        val profit     = evalAmount - buyAmount
                        if (buyAmount > 0.0) (profit / buyAmount) * 100.0 else 0.0
                    }
                }

                SortType.PROFIT_DESC -> {
                    filteredCoins.sortedByDescending { coin ->
                        val currentPrice = myPageViewModel.getCurrentPrice(
                            symbol = coin.symbol,
                            avgPrice = coin.avgPrice
                        )
                        val buyAmount  = coin.amount * coin.avgPrice
                        val evalAmount = coin.amount * currentPrice
                        val profit     = evalAmount - buyAmount
                        if (buyAmount > 0.0) (profit / buyAmount) * 100.0 else 0.0
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            ) {
                items(sortedCoins) { item ->

                    // ---- 여기서 코인별 숫자 계산 ----
                    val currentPrice = myPageViewModel.getCurrentPrice(
                        symbol   = item.symbol,
                        avgPrice = item.avgPrice
                    )
                    val holdingAmount = item.amount
                    val avgPrice = item.avgPrice

                    val buyAmount  = holdingAmount * avgPrice            // 매수금액
                    val evalAmount = holdingAmount * currentPrice        // 평가금액
                    val profit     = evalAmount - buyAmount              // 평가손익
                    val profitRate =
                        if (buyAmount > 0.0) (profit / buyAmount) * 100.0 else 0.0

                    val profitColor = when {
                        profit > 0 -> Color.Red
                        profit < 0 -> Color.Blue
                        else       -> Color.Black
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                navController.navigate(
                                    "coin_detail/${item.symbol}/${item.korName}/${item.engName}"
                                )
                            }
                            .padding(vertical = 12.dp)
                    ) {
                        // ───── 1줄 : 코인명 / 평가손익 / 수익률 ─────
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.Start
                            ) {
                                Text(
                                    text = item.korName,
                                    fontSize = 18.sp,
                                    color = Color.Black
                                )
                                Text(
                                    text = item.symbol,
                                    fontSize = 14.sp,
                                    color = Color.Black
                                )
                                Text(
                                    text = "현재가 ${dfDouble.format(currentPrice)}",
                                    fontSize = 12.sp,
                                    color = Color.Red
                                )
                            }

                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.End
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("평가손익", fontSize = 14.sp, color = Color.Black)
                                    Text(
                                        text = dfInt.format(profit),
                                        fontSize = 14.sp,
                                        color = profitColor
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("수익률", fontSize = 14.sp, color = Color.Black)
                                    Text(
                                        text = String.format("%.2f%%", profitRate),
                                        fontSize = 14.sp,
                                        color = profitColor
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        // ───── 2줄 : 보유수량 / 매수평균가 ─────
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {

                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.End
                            ){
                                Text(
                                    text = "${dfDouble.format(holdingAmount)} ${item.symbol}",
                                    fontSize = 12.sp,
                                    color = Color.Black
                                )
                                Text("보유수량", fontSize = 10.sp, color = Color.Gray)
                            }

                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.End
                            ) {
                                Text(
                                    text = "${dfDouble.format(item.avgPrice)} KRW",
                                    fontSize = 12.sp,
                                    color = Color.Black
                                )
                                Text("매수평균가", fontSize = 10.sp, color = Color.Gray)
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        // ───── 3줄 : 평가금액 / 매수금액 ─────
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {

                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.End
                            ) {
                                Text(
                                    text = "${dfInt.format(evalAmount)} KRW",
                                    fontSize = 12.sp,
                                    color = Color.Black
                                )
                                Text("평가금액", fontSize = 10.sp, color = Color.Gray)
                            }

                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.End
                            ) {
                                Text(
                                    text = "${dfInt.format(buyAmount)} KRW",
                                    fontSize = 12.sp,
                                    color = Color.Black
                                )
                                Text("매수금액", fontSize = 10.sp, color = Color.Gray)
                            }
                        }

                    }

                    Divider(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                        thickness = 1.dp
                    )
                }
            }
        }
    }
}

enum class SortType {
    NONE,
    NAME_ASC,
    NAME_DESC,
    PROFIT_ASC,
    PROFIT_DESC
}