package com.kkt981019.bitcoin_chart.screen.coindetail.orderbook

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kkt981019.bitcoin_chart.network.Data.CoinDetailResponse
import com.kkt981019.bitcoin_chart.network.Data.OrderbookResponse
import com.kkt981019.bitcoin_chart.network.Data.WebsocketResponse
import java.text.DecimalFormat

@Composable
fun OrderBookSection(
    orderbook: OrderbookResponse?,   // 호가 데이터
    ticker: WebsocketResponse?,      // 현재 시세 데이터 (ticker)
    changeRate: String,
    symbol: String
) {
    val currentPrice = ticker?.trade_price?.toDoubleOrNull() ?: 0.0
    val units = orderbook?.orderbook_units ?: emptyList()

    val dsPrice = DecimalFormat("#,##0.000")
    val format = com.kkt981019.bitcoin_chart.util.DecimalFormat.getTradeFormatters(symbol.substringBefore("-"))
    val baseRateValue = changeRate.replace("%", "").toDoubleOrNull() ?: 0.0

    val totalCount = units.size * 2
    val middleIndex = (totalCount / 2 / 1.4).toInt()

    if (units.isEmpty()) {
        // 데이터 없을 때 로딩 표시
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp)
        ) {
            androidx.compose.material3.CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        }
    } else {
        val listState = rememberLazyListState(
            initialFirstVisibleItemIndex = middleIndex
        )

        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxWidth()
        ) {
            // 1) 매도 섹션
            items(units.reversed()) { unit ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 1.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val diffPercent = if (currentPrice != 0.0) {
                        (unit.askPrice - currentPrice) / currentPrice * 100.0
                    } else 0.0
                    val combinedValue = baseRateValue + diffPercent
                    val diffText = String.format("%.2f%%", combinedValue)
                    val diffColor = when {
                        combinedValue < 0 -> Color.Blue
                        combinedValue > 0 -> Color.Red
                        else -> Color.Black
                    }

                    // askSize
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .background(color = Color.Blue.copy(alpha = 0.1f))
                            .padding(8.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = dsPrice.format(unit.askSize),
                            fontSize = 12.sp,
                        )
                    }
                    Spacer(modifier = Modifier.width(1.dp))
                    // askPrice
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .background(color = Color.Blue.copy(alpha = 0.1f))
                            .then(
                                if (currentPrice == unit.askPrice)
                                    Modifier.border(width = 1.dp, color = Color.Black)
                                else Modifier
                            )
                            .padding(8.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        Row {
                            Text(
                                text = format.priceDf.format(unit.askPrice),
                                fontSize = 12.sp,
                                color = diffColor
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = diffText,
                                fontSize = 12.sp,
                                color = diffColor
                            )
                        }
                    }
                    // 오른쪽 컬럼 (비워둘 수도 있음)
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(8.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        // 필요한 경우 정보 추가
                    }
                }
            }

            // 2) 매수 섹션
            items(units) { unit ->
                val diffPercent = if (currentPrice != 0.0) {
                    (unit.bidPrice - currentPrice) / currentPrice * 100.0
                } else 0.0
                val combinedValue = baseRateValue + diffPercent
                val diffText = String.format("%.2f%%", combinedValue)
                val diffColor = when {
                    combinedValue < 0 -> Color.Blue
                    combinedValue > 0 -> Color.Red
                    else -> Color.Black
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 1.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 왼쪽 컬럼 (bidSize)
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(8.dp),
                        horizontalAlignment = Alignment.Start
                    ) {}

                    // bidPrice
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .background(color = Color.Red.copy(alpha = 0.1f))
                            .then(
                                if (currentPrice == unit.bidPrice)
                                    Modifier.border(width = 1.dp, color = Color.Black)
                                else Modifier
                            )
                            .padding(8.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        Row {
                            Text(
                                text = format.priceDf.format(unit.bidPrice),
                                fontSize = 12.sp,
                                color = diffColor
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = diffText,
                                fontSize = 12.sp,
                                color = diffColor
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(1.dp))
                    // 오른쪽 컬럼 (bidSize)
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .background(color = Color.Red.copy(alpha = 0.1f))
                            .padding(8.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = dsPrice.format(unit.bidSize),
                            fontSize = 12.sp,
                        )
                    }
                }
            }
        }
    }
}