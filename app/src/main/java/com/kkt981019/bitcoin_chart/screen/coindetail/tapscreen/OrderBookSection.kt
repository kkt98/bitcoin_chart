package com.kkt981019.bitcoin_chart.screen.coindetail.tapscreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.kkt981019.bitcoin_chart.network.Data.CoinDetailResponse
import com.kkt981019.bitcoin_chart.network.Data.OrderbookResponse
import java.text.DecimalFormat

@Composable
fun OrderBookSection(
    orderbook: OrderbookResponse?,   // 호가 데이터
    ticker: CoinDetailResponse?      // 현재 시세 데이터 (ticker), 현재가를 기준으로 변화율 계산용
) {
//    // ticker의 현재 거래 가격을 Double로 변환 (없으면 0.0)
//    val currentPrice = ticker?.trade_price?.toDoubleOrNull() ?: 0.0
//
//    // LazyColumn을 사용하여 각 호가 레벨(주문 단위)를 여러 줄로 표시
//    LazyColumn(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(horizontal = 8.dp)
//    ) {
//        // orderbook_units가 null이면 빈 리스트 처리
//        items(orderbook?.orderbook_units ?: emptyList()) { unit ->
//            // 각 호가 단위에서 ask와 bid 가격의 변동률(%)를 계산
//            // 변동률 = (해당 호가 가격 - 현재가) / 현재가 * 100
//            // currentPrice가 0이면 0%로 처리
//            val askChangePercent = if (currentPrice != 0.0) {
//                (unit.ask_price - currentPrice) / currentPrice * 100
//            } else {
//                0.0
//            }
//            val bidChangePercent = if (currentPrice != 0.0) {
//                (unit.bid_price - currentPrice) / currentPrice * 100
//            } else {
//                0.0
//            }
//
//            // 포맷터: 천단위 콤마 등
//            val dfPrice = DecimalFormat("#,##0")
//            val dfPercent = DecimalFormat("0.00") // 소수점 둘째자리까지
//
//            // 각 호가 단위를 Row로 표시 (매도: 빨간색, 매수: 파란색)
//            Row(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(vertical = 4.dp),
//                horizontalArrangement = Arrangement.SpaceBetween,
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                // 왼쪽: 매도(ask) 정보
//                Column(horizontalAlignment = Alignment.Start) {
//                    Text(
//                        text = dfPrice.format(unit.ask_price),
//                        style = MaterialTheme.typography.bodyMedium,
//                        color = Color.Red
//                    )
//                    Spacer(modifier = Modifier.padding(top = 2.dp))
//                    Text(
//                        text = dfPercent.format(askChangePercent) + "%",
//                        style = MaterialTheme.typography.labelSmall,
//                        color = Color.Red
//                    )
//                    Spacer(modifier = Modifier.padding(top = 2.dp))
//                    Text(
//                        text = unit.ask_size.toString(),
//                        style = MaterialTheme.typography.labelSmall,
//                        color = Color.Red
//                    )
//                }
//                // 오른쪽: 매수(bid) 정보
//                Column(horizontalAlignment = Alignment.End) {
//                    Text(
//                        text = dfPrice.format(unit.bid_price),
//                        style = MaterialTheme.typography.bodyMedium,
//                        color = Color.Blue
//                    )
//                    Spacer(modifier = Modifier.padding(top = 2.dp))
//                    Text(
//                        text = dfPercent.format(bidChangePercent) + "%",
//                        style = MaterialTheme.typography.labelSmall,
//                        color = Color.Blue
//                    )
//                    Spacer(modifier = Modifier.padding(top = 2.dp))
//                    Text(
//                        text = unit.bid_size.toString(),
//                        style = MaterialTheme.typography.labelSmall,
//                        color = Color.Blue
//                    )
//                }
//            }
//        }
//    }
}