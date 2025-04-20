package com.kkt981019.bitcoin_chart.screen.coindetail.tapscreen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.kkt981019.bitcoin_chart.network.Data.CoinDetailResponse
import com.kkt981019.bitcoin_chart.network.Data.OrderbookResponse
import java.text.DecimalFormat

@Composable
fun OrderBookSection(
    orderbook: OrderbookResponse?,   // 호가 데이터
    ticker: CoinDetailResponse?,      // 현재 시세 데이터 (ticker)
    changeRate: String,
    symbol: String
) {
    val currentPrice = ticker?.trade_price?.toDoubleOrNull() ?: 0.0
    val units = orderbook?.orderbook_units ?: emptyList()

    val dfPrice =  when {
        symbol.startsWith("KRW") -> DecimalFormat("#,##0.0####")
        symbol.startsWith("BTC") -> DecimalFormat("0.00000000")
        else -> DecimalFormat("#,##0.000#####")
    }
    val dsPrice = DecimalFormat("#,##0.000")

    val baseRateValue = changeRate.replace("%", "").toDoubleOrNull() ?: 0.0


    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        // --------------------------------
        // 1) 매도 섹션
        // --------------------------------
        items(units.reversed()) { unit ->
            // 한 줄에 3개의 Column을 동일 비율로 배치 (Row + weight(1f))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 1.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // askPrice 기준 변동률
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

                // 왼쪽 컬럼 (askSize)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .background(color = Color.Blue.copy(alpha = 0.1f))
                        .padding(6.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = dsPrice.format(unit.askSize),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                Spacer(modifier = Modifier.width(1.dp))
                // 중앙 컬럼 (askPrice)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .background(color = Color.Blue.copy(alpha = 0.1f))
                        .then(if (currentPrice == unit.askPrice)
                            Modifier.border(width = 1.dp, color = Color.Black)  // 경계선 색상은 원하는 대로 조절
                        else Modifier
                        )
                        .padding(6.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Row(modifier = Modifier
                        ) {

                        Text(
                            text = dfPrice.format(unit.askPrice),
                            style = MaterialTheme.typography.bodyMedium,
                            color = diffColor
                        )
                        Spacer(modifier = Modifier.width(8.dp)) // 8.dp 간격 추가
                        Text(
                            text = diffText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = diffColor
                        )
                    }

                }
                // 오른쪽 컬럼
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(6.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    // 필요하다면 다른 텍스트나 UI 요소를 배치
                    // 예: Text("기타 정보", color = Color.Red)
                }
            }
        }
        // --------------------------------
        // 2) 매수 섹션
        // --------------------------------
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
                        .padding(6.dp),
                    horizontalAlignment = Alignment.Start
                ) {

                }

                // 중앙 컬럼 (bidPrice)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .background(color = Color.Red.copy(alpha = 0.1f))
                        .padding(6.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Row {
                        Text(
                        text = dfPrice.format(unit.bidPrice),
                        style = MaterialTheme.typography.bodyMedium,
                        color = diffColor
                        )
                        Spacer(modifier = Modifier.width(8.dp)) // 8.dp 간격 추가
                        Text(
                            text = diffText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = diffColor
                        )
                    }
                }

                Spacer(modifier = Modifier.width(1.dp))

                // 오른쪽 컬럼 (추가 정보가 없으면 비워둠)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .background(color = Color.Red.copy(alpha = 0.1f))
                        .padding(6.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = dsPrice.format(unit.bidSize),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}
