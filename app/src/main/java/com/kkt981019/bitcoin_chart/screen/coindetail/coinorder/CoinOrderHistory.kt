package com.kkt981019.bitcoin_chart.screen.coindetail.coinorder

import android.content.Context
import android.icu.text.DecimalFormat
import android.widget.Space
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.kkt981019.bitcoin_chart.viewmodel.TradeHistoryViewModel
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CoinOrderHistory(
    context: Context, symbol: String,
    tradeHistoryViewModel: TradeHistoryViewModel = hiltViewModel()
) {
    // 심볼 바뀔 때마다 해당 코인 거래내역 로드
    LaunchedEffect(symbol) {
        tradeHistoryViewModel.loadTrades(symbol)
    }

    // StateFlow<List<TradeHistoryEntity>> -> State 로 변환
    val trades by tradeHistoryViewModel.trades.collectAsState()

    LazyColumn {

        items(items = trades) { trade ->

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
            ) {
                // 1) 매수/매도 + 심볼
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (trade.type == "BUY") "매수" else "매도",
                        fontSize = 20.sp,
                        color = if (trade.type == "BUY") Color.Red else Color.Blue
                    )

                    Text(
                        text = trade.symbol,
                        fontSize = 20.sp,
                        color = Color.Black
                    )
                }

                Spacer(Modifier.height(8.dp))

                Divider(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                    thickness = 1.dp
                )

                Spacer(Modifier.height(8.dp))

                // 2) 시간
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "시간",
                        fontSize = 12.sp,
                        color = Color.Black
                    )
                    Text(
                        text = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                            .format(Date(trade.time)),
                        fontSize = 12.sp,
                        color = Color.Black
                    )
                }

                Spacer(Modifier.height(5.dp))

                // 3) 가격
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "가격",
                        fontSize = 12.sp,
                        color = Color.Black
                    )
                    Text(
                        text = DecimalFormat("#,##0.##").format(trade.price),
                        fontSize = 12.sp,
                        color = Color.Black
                    )
                }

                Spacer(Modifier.height(5.dp))

                // 4) 수량
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "수량",
                        fontSize = 12.sp,
                        color = Color.Black
                    )
                    Text(
                        text = DecimalFormat("0.########").format(trade.amount),
                        fontSize = 12.sp,
                        color = Color.Black
                    )
                }

                Spacer(Modifier.height(5.dp))

                // 5) 총액
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "총액",
                        fontSize = 12.sp,
                        color = Color.Black
                    )
                    Text(
                        text = DecimalFormat("#,##0").format(trade.total),
                        fontSize = 12.sp,
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(Modifier.height(8.dp))

                Divider(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                    thickness = 1.dp
                )
            }
        }
    }
}