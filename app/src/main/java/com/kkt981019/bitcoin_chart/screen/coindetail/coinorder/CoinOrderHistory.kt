package com.kkt981019.bitcoin_chart.screen.coindetail.coinorder

import android.content.Context
import android.icu.text.DecimalFormat
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.kkt981019.bitcoin_chart.viewmodel.TradeHistoryViewModel
import androidx.compose.foundation.lazy.items
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
            // 여기서 시간/가격/수량/총액 UI 그리면 됨
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp, horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // 시간
                Text(
                    text = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                        .format(Date(trade.time)),
                    fontSize = 12.sp,
                    color = Color.Gray
                )

                // 매수/매도 타입
                Text(
                    text = if (trade.type == "BUY") "매수" else "매도",
                    fontSize = 12.sp,
                    color = if (trade.type == "BUY") Color.Red else Color.Blue
                )

                // 가격
                Text(
                    text = DecimalFormat("#,##0.##").format(trade.price),
                    fontSize = 12.sp,
                    color = Color.Black
                )

                // 수량
                Text(
                    text = DecimalFormat("0.########").format(trade.amount),
                    fontSize = 12.sp,
                    color = Color.Black
                )

                // 총액
                Text(
                    text = DecimalFormat("#,##0").format(trade.total),
                    fontSize = 12.sp,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}