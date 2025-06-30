package com.kkt981019.bitcoin_chart.screen.coindetail.trade

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.idapgroup.autosizetext.AutoSizeText
import com.kkt981019.bitcoin_chart.network.Data.WebSocketCandleResponse
import com.kkt981019.bitcoin_chart.viewmodel.CoinDtTradeViewModel
import java.text.DecimalFormat
import kotlin.text.substringAfter
import kotlin.text.substringBefore

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DailyList(
    symbol: String,
    viewModel: CoinDtTradeViewModel = hiltViewModel()
) {

    LaunchedEffect(symbol) {
        viewModel.startCandle(symbol, "1s")
    }

    val dayCandle by viewModel.dayCandleState.observeAsState(emptyList())

    if (dayCandle.isEmpty()) {
        return
    }

    LazyColumn(modifier = Modifier.fillMaxWidth()) {
        stickyHeader {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)      // 자식 높이에 맞춰서
                    .background(Color.White)
                    .border(1.dp, color = Color.LightGray)
            ) {
                // 1열
                Text(
                    "일자",
                    Modifier
                        .weight(0.5f)
                        .fillMaxHeight()           // Divider가 헤더 높이에 꽉 차게
                        .padding(8.dp),
                    textAlign = TextAlign.Center
                )
                Divider(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(1.dp),
                    color = Color.LightGray
                )
                // 2열
                Text(
                    "종가(${dayCandle[0].code.substringBefore("-")})",
                    Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding( 8.dp),
                    textAlign = TextAlign.Center
                )
                Divider(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(1.dp),
                    color = Color.LightGray
                )
                // 3열
                Text(
                    "전일대비",
                    Modifier
                        .weight(0.7f)
                        .fillMaxHeight()
                        .padding(8.dp),
                    textAlign = TextAlign.Center
                )
                Divider(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(1.dp),
                    color = Color.LightGray
                )
                // 4열
                Text(
                    text ="거래량(${dayCandle[0].code.substringAfter("-")})",
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(8.dp),
                    textAlign = TextAlign.Center
                )
            }
            Divider() // 헤더 아래 가로줄
        }

        itemsIndexed(dayCandle) { index, candle ->

            val prevClose = dayCandle.getOrNull(index + 1)?.tradePrice ?: candle.tradePrice
            val diffAmount = candle.tradePrice - prevClose
            val diffRate = if (prevClose != 0.0) (diffAmount / prevClose) * 100 else 0.0

            val format =  com.kkt981019.bitcoin_chart.util.DecimalFormat.getTradeFormatters(candle.code.substringBefore("-"))

            val textColor = when {
                diffRate > 0 -> Color.Red
                diffRate < 0 -> Color.Blue
                else -> Color.Black
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),   // 개별 row 높이에도 적용
                verticalAlignment = Alignment.CenterVertically
            ) {

                // 일자
                Text(
                    text = candle.candleDateTimeUtc.substring(5, 10),
                    modifier = Modifier
                        .weight(0.5f)
                        .fillMaxHeight()
                        .padding(10.dp),
                    textAlign = TextAlign.Center,
                    fontSize = 13.sp
                )
                Divider(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(1.dp),
                    color = Color.LightGray
                )

                // 종가
                Text(
                    text = format.priceDf.format(candle.tradePrice),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(10.dp),
                    textAlign = TextAlign.End,
                    color = textColor,
                    fontSize = 13.sp
                )
                Divider(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(1.dp),
                    color = Color.LightGray
                )

                // 전일대비
                Column(
                    modifier = Modifier.weight(0.7f).padding(10.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = String.format("%.2f%%", diffRate ?: 0.0),
                        color = textColor,
                        fontSize = 13.sp
                    )

                    when(candle.code.substringBefore("-")) {
                        "KRW" ->Text(
                            text = DecimalFormat("#,###.#####").format(diffAmount),
                            color = textColor,
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 9.sp
                        )
                        else -> null
                    }
                }

                Divider(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(1.dp),
                    color = Color.LightGray
                )

                // 거래량
                AutoSizeText(
                    text = DecimalFormat("#,###.########").format(candle.candleAccTradeVolume),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(10.dp),
                    textAlign = TextAlign.End,
                    maxLines = 1,
                    fontSize = 13.sp,
                    minFontSize = 1.sp,
                    lineHeight = 14.sp,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Divider() // 각 row 아래 가로줄
        }
    }
}