package com.kkt981019.bitcoin_chart.screen.coindetail

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kkt981019.bitcoin_chart.network.Data.OrderbookResponse
import com.kkt981019.bitcoin_chart.network.Data.WebsocketResponse
import java.text.DecimalFormat

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CoinOrderSection(
    orderbook: OrderbookResponse?,
    ticker: WebsocketResponse?,
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

        // 수량 바 최대값(정규화용)
        val maxAsk = (units.maxOfOrNull { it.askSize } ?: 1.0).toFloat()
        val maxBid = (units.maxOfOrNull { it.bidSize } ?: 1.0).toFloat()

        @Composable
        fun LadderRow(
            priceText: String,
            percent: Double,
            amountText: String,
            isAsk: Boolean,
            isCurrent: Boolean,
            progress: Float,
        ) {
            // 체결부색: 매도=파랑, 매수=빨강 (스크린샷과 동일 톤)
            val bidTint = Color(0xFFEB4D4D)       // 빨강 (매수)
            val askTint = Color(0xFF3B82F6)       // 파랑 (매도)
            val sideTint = if (isAsk) askTint else bidTint

            val bgLite = sideTint.copy(alpha = 0.06f)

            // 등락 색상(양수=빨강, 음수=파랑)
            val pctColor = when {
                percent > 0 -> bidTint
                percent < 0 -> askTint
                else -> Color.Unspecified
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(bgLite.takeIf { isAsk } ?: Color.Transparent)
                    .border(
                        width = if (isCurrent) 1.dp else 0.dp,
                        color = if (isCurrent) Color.Black.copy(alpha = 0.6f) else Color.Transparent
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 가격 + 대비%
                Column(
                    modifier = Modifier
                        .weight(1.4f)
                        .then(
                            if (isCurrent) Modifier.border(1.dp, Color.Black.copy(alpha = 0.6f)) else Modifier
                        )
                        .background(bgLite.takeIf { !isAsk } ?: Color.Transparent)
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(text = priceText, fontSize = 12.sp, color = pctColor)
                    Text(text = String.format("%.2f%%", percent), fontSize = 10.sp, color = pctColor)
                }

                // 수량 + 비례 배경바
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(30.dp)
                        .padding(horizontal = 8.dp),
                    contentAlignment = if (isAsk) Alignment.CenterEnd else Alignment.CenterStart
                ) {
                    // 수량 텍스트
                    Text(
                        text = amountText,
                        fontSize = 12.sp,
                        modifier = Modifier
                            .align(if (isAsk) Alignment.CenterEnd else Alignment.CenterStart)
                    )
                }
            }
        }

        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            // ── 매도(ASK): 위쪽
            items(units.reversed()) { unit ->
                val diffPercent = if (currentPrice != 0.0)
                    (unit.askPrice - currentPrice) / currentPrice * 100.0
                else 0.0
                val combinedValue = baseRateValue + diffPercent

                LadderRow(
                    priceText = format.priceDf.format(unit.askPrice),
                    percent = combinedValue,
                    amountText = dsPrice.format(unit.askSize),
                    isAsk = true,
                    isCurrent = currentPrice == unit.askPrice,
                    progress = (unit.askSize.toFloat() / maxAsk)
                )
            }

            // ── 매수(BID): 아래쪽
            items(units) { unit ->
                val diffPercent = if (currentPrice != 0.0)
                    (unit.bidPrice - currentPrice) / currentPrice * 100.0
                else 0.0
                val combinedValue = baseRateValue + diffPercent

                LadderRow(
                    priceText = format.priceDf.format(unit.bidPrice),
                    percent = combinedValue,
                    amountText = dsPrice.format(unit.bidSize),
                    isAsk = false,
                    isCurrent = currentPrice == unit.bidPrice,
                    progress = (unit.bidSize.toFloat() / maxBid)
                )
            }
        }
    }
}
