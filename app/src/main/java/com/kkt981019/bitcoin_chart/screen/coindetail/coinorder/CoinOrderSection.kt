package com.kkt981019.bitcoin_chart.screen.coindetail.coinorder

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialogDefaults.shape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
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
    // 👉 오른쪽 패널 예약 영역
    rightContent: (@Composable () -> Unit)? = null, // 나중에 주문패널 넣을 슬롯
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

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(30.dp)
                                .padding(horizontal = 8.dp),
                            contentAlignment = Alignment.CenterEnd
                        ) {

                            AutoSizeText(
                                text = amountText,
                                modifier = Modifier.align(if (isAsk) Alignment.CenterEnd else Alignment.CenterStart),
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

        // ── 가운데 구분선

        Spacer(Modifier.width(8.dp))

        Box(
            Modifier
                .width(1.dp)
                .fillMaxHeight()
                .background(Color(0xFF000000))
        )

        Spacer(Modifier.width(8.dp))

        // ── 오른쪽: 예약 공간(고정폭). 나중에 주문패널 꽂기
        Box(
            modifier = Modifier
                .weight(1.5f)
                .fillMaxHeight()
                .border(1.dp, Color(0xFF000000))
                .padding(12.dp)
        ) {

            Column(Modifier.fillMaxSize()) {

                //주문가능 영역 (내가 현재 가지고 있는 돈)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("주문 가능", color = Color(0xFF000000), fontSize = 12.sp)
                    Text("${"00"} KRW", color = Color.Black, fontSize = 12.sp)
                }

                Spacer(Modifier.height(8.dp))

                // 가격 영역
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp)
                        .border(
                            width = 1.dp,
                            color = Color(0xFF000000),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 12.dp),                    // 안쪽 여백
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("가격", color = Color(0xFF000000), fontSize = 12.sp)
                    Text("${currentPrice}", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(Modifier.height(8.dp))

                var qty by remember { mutableStateOf("0") }
                val qtyNum = qty.toDoubleOrNull() ?: 0.0
                val total = qtyNum * currentPrice

                // 수량 영역
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp)
                        .border(
                            1.dp,
                            Color(0xFF000000),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // ▶ 왼쪽: 라벨 + 숫자 입력(오른쪽 정렬)
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("수량", color = Color(0xFF000000), fontSize = 12.sp)

                        Spacer(Modifier.weight(1f))

                        BasicTextField(
                            value = qty,
                            onValueChange = { s ->

                                qty = s.filter { it.isDigit() || it == '.' }
                            },
                            singleLine = true,
                            textStyle = LocalTextStyle.current.copy(
                                color = Color(0xFF000000),
                                fontSize = 12.sp,
                                textAlign = TextAlign.End
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            cursorBrush = SolidColor(Color(0xFF000000)),
                            decorationBox = { inner ->
                                Box(
                                    modifier = Modifier.widthIn(min = 24.dp),
                                    contentAlignment = Alignment.CenterEnd
                                ) {
                                    if (qty.isBlank()) {
                                        Text("0", color = Color(0x80000000), fontSize = 12.sp)
                                    }
                                    inner()
                                }
                            }
                        )
                    }

                    // ▶ 오른쪽: '비율' 버튼
                    Box(
                        modifier = Modifier
                            .width(56.dp)
                            .fillMaxHeight()
                            .background(
                                color = Color(0xFF9E9E9E),
                                shape = RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp)
                            )
                            .clickable { /* TODO: 비율 선택 BottomSheet 열기 */ },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("비율", color = Color.White, fontSize = 12.sp)
                    }
                }

                Spacer(Modifier.height(8.dp))

                // 총액 text 영역
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp)
                        .border(
                            width = 1.dp,
                            color = Color(0xFF000000),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 12.dp),                    // 안쪽 여백
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("총액", color = Color(0xFF000000), fontSize = 12.sp)
                    Text(
                        text = DecimalFormat("#,##0.##").format(total),
                        color = Color.Black,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(Modifier.height(8.dp))

                //초기화, 매수 버튼
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 초기화
                    Button(
                        onClick = {
                            // 요청: 수량을 "0"으로
                            qty = "0"
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color(0xFFE0E0E0),  // 밝은 회색 배경
                            contentColor = Color.Black
                        ),
                        border = ButtonDefaults.outlinedButtonBorder.copy(width = 0.dp) // 테두리 안 보이게
                    ) {
                        Text("초기화")
                    }

                    // 매수
                    Button(
                        onClick = {
                            Toast
                                .makeText(context, "매수완료", Toast.LENGTH_SHORT)
                                .show()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Red,
                            contentColor = Color.White,
                        )
                    ) {
                        Text("매수")
                    }
                }

                Spacer(Modifier.height(8.dp))

                // 총액 지정하여 매수 버튼
                Button(
                    onClick = {
                        Toast
                            .makeText(context, "총액 지정 매수", Toast.LENGTH_SHORT)
                            .show()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red,
                        contentColor = Color.White,
                    )
                ) {
                    Text("총액 지정하여 매수")
                }
            }

        }

    }
}
