package com.kkt981019.bitcoin_chart.screen.coindetail.coinorder

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.idapgroup.autosizetext.AutoSizeText
import com.kkt981019.bitcoin_chart.network.Data.OrderbookResponse
import com.kkt981019.bitcoin_chart.network.Data.WebsocketResponse
import java.text.DecimalFormat

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CoinOrderSection(
    orderbook: OrderbookResponse?,
    ticker: WebsocketResponse?,
    changeRate: String,
    symbol: String,
) {
    val currentPrice = ticker?.trade_price?.toDoubleOrNull() ?: 0.0
    val units = orderbook?.orderbook_units ?: emptyList()

    val dsPrice = DecimalFormat("#,##0.000")
    val format = com.kkt981019.bitcoin_chart.util.DecimalFormat
        .getTradeFormatters(symbol.substringBefore("-"))
    val baseRateValue = changeRate.replace("%", "").toDoubleOrNull() ?: 0.0

    val totalCount = units.size * 2
    val middleIndex = (totalCount / 2 / 1.4).toInt()

    val context = LocalContext.current

    // 전체를 Row로 감싸서 오른쪽에 자리를 확보
    Row(Modifier.fillMaxWidth()) {

        // ── 왼쪽: 오더북 영역
        Box(Modifier.weight(1f)) {
            if (units.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp)
                ) {
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                }
            } else {
                val listState = rememberLazyListState(
                    initialFirstVisibleItemIndex = middleIndex
                )

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
                    val bidTint = Color(0xFFEB4D4D) // 매수
                    val askTint = Color(0xFF3B82F6) // 매도
                    val sideTint = if (isAsk) askTint else bidTint
                    val bgLite = sideTint.copy(alpha = 0.06f)
                    val pctColor = when {
                        percent > 0 -> bidTint
                        percent < 0 -> askTint
                        else -> Color.Unspecified
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Max)
                            .background(Color.Transparent)
                            .border(
                                width = if (isCurrent) 1.dp else 0.dp,
                                color = if (isCurrent) Color.Black.copy(alpha = 0.6f) else Color.Transparent
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1.4f)
                                .fillMaxHeight()
                                .then(
                                    if (isCurrent) Modifier.border(
                                        1.dp,
                                        Color.Black.copy(alpha = 0.6f)
                                    ) else Modifier
                                )
                                .background(bgLite)
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text(text = priceText, fontSize = 12.sp, color = pctColor)
                            Text(text = String.format("%.2f%%", percent), fontSize = 10.sp, color = pctColor)
                        }

                        Spacer(modifier = Modifier.width(1.dp))   // 원하는 만큼 dp 조절

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .background(bgLite)
                                .padding(horizontal = 8.dp),
                            contentAlignment = Alignment.CenterEnd
                        ) {

                            AutoSizeText(
                                text = amountText,
                                modifier = Modifier.align(Alignment.CenterEnd),
                                maxLines = 1,
                                fontSize = 12.sp,
                                minFontSize = 1.sp,
                                lineHeight = 14.sp,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    // ── 매도(ASK): 위쪽  (✅ isAsk = true로 수정)
                    items(units.reversed()) { unit ->
                        val diffPercent = if (currentPrice != 0.0)
                            (unit.askPrice - currentPrice) / currentPrice * 100.0 else 0.0
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
                            (unit.bidPrice - currentPrice) / currentPrice * 100.0 else 0.0
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

        // ── 오른쪽 코인 사고 파는 영역
        Column(
            modifier = Modifier
                .weight(1.5f)
                .fillMaxHeight()
        ) {

            var selectedTab by remember { mutableStateOf(0) }

            val tabs = listOf("매수", "매도", "거래 내역")
            val corner = 4.dp

            Surface(
                modifier = Modifier
                    .fillMaxWidth(),
                color = Color.Transparent,
                shape = RoundedCornerShape(corner),
            ) {
                TabRow(
                    selectedTabIndex = selectedTab,
                    indicator = {},
                    divider = {}
                ) {
                    tabs.forEachIndexed { idx, title ->
                        Tab(
                            selected = selectedTab == idx,
                            onClick = { selectedTab = idx },
                            modifier = Modifier
                                .height(36.dp)
                                .background(
                                    color = if (selectedTab == idx)  Color.Transparent else Color(
                                        0xFFEEEEEE
                                    ),
                                ),
                            text = {
                                Text(
                                    text = title,
                                    fontSize = 11.sp,
                                    color = if (selectedTab == idx) {
                                        when (idx) {
                                            0 -> Color.Red    // 매수
                                            1 -> Color.Blue   // 매도
                                            else -> Color.Black // 거래내역
                                        }
                                    } else {
                                        Color.Black
                                    },
                                )
                            }
                        )
                    }
                }
            }
            when (selectedTab) {
                0 -> CoinOrderBuy(currentPrice, format, context, symbol)
                1 -> CoinOrderSell(currentPrice, format, context, symbol)
                2 -> CoinOrderHistory(context, symbol)
            }

        }

    }
}
