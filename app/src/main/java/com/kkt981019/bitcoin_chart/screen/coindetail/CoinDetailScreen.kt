package com.kkt981019.bitcoin_chart.screen.coindetail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.kkt981019.bitcoin_chart.R
import com.kkt981019.bitcoin_chart.screen.coindetail.chart.ChartSection
import com.kkt981019.bitcoin_chart.screen.coindetail.orderbook.OrderBookSection
import com.kkt981019.bitcoin_chart.screen.coindetail.trade.TradeSection
import com.kkt981019.bitcoin_chart.viewmodel.CoinDTScreenVM
import java.text.DecimalFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoinDetailScreen(
    symbol: String,
    koreanName: String,
    navController: NavController,
    viewModel: CoinDTScreenVM = hiltViewModel() // Hilt로 주입받음
) {
    // 화면 진입 시, 해당 심볼로 웹소켓 연결 시작
    LaunchedEffect(symbol) {
        viewModel.startDetailAll(symbol)
//        viewModel.startDetailDay(symbol)
    }

    // ViewModel의 LiveData를 observeAsState로 관찰
    val ticker by viewModel.tickerState.observeAsState()
    val orderbook by viewModel.orderbookState.observeAsState()
    val trades by viewModel.tradeState.observeAsState(emptyList())
    val dayCandle by viewModel.dayCandleState.observeAsState(emptyList())

    // 탭 UI 구성
    val tabTitles = listOf("호가", "차트", "시세")
    var selectedTabIndex by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "$koreanName ($symbol)") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "뒤로가기 버튼"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
                // 데이터가 도착하면 Row로 세 숫자를 균등하게 표시

            // 현재가 소수점 표시용 포맷터
            val df = when {
                symbol.startsWith("KRW") -> DecimalFormat("#,##0.#####")
                symbol.startsWith("BTC") -> DecimalFormat("0.00000000")
                else -> DecimalFormat("#,##0.00######")
            }

            val si = DecimalFormat("#,##0.###")

            val color = when(ticker?.change) {
                "EVEN" -> Color.Black
                "RISE" -> Color.Red
                else -> Color.Blue
            }

            // 티커의 change 값에 따라 다른 아이콘을 표시
            val changeIcon: Painter? = when (ticker?.change) {
                "EVEN" -> null
                "RISE" -> painterResource(id = R.drawable.triangle)
                else -> painterResource(id = R.drawable.inverted_triangle)
            }

            val changeRate = String.format("%.2f%%", (ticker?.signed_change_rate?.toDoubleOrNull()?.times(100)) ?: 0.0)

            // trade_price와 Row를 감싸는 Column에 padding(16.dp) 적용
            Column(modifier = Modifier.padding(16.dp)) {
                // trade_price는 숫자로 변환 후 포맷팅
                Text(
                    text = df.format(ticker?.trade_price?.toDoubleOrNull() ?: 0.0),
                    style = MaterialTheme.typography.headlineSmall,
                    color = color
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = changeRate,
                        style = MaterialTheme.typography.bodyMedium,
                        color = color,
                        modifier = Modifier.padding(end = 14.dp)
                    )

                    if (changeIcon != null) {
                        Icon(
                            painter = changeIcon,
                            contentDescription = "변동 아이콘",
                            tint = color,
                            modifier = Modifier.size(20.dp) // 아이콘 크기를 24.dp로 설정
                                .padding(end = 6.dp)
                        )
                    }

                    Text(
                        text = si.format(ticker?.signed_change_price?.toDoubleOrNull() ?: 0.0),
                        style = MaterialTheme.typography.bodyMedium,
                        color = color
                    )
                }
            }

            //탭
            TabRow(
                selectedTabIndex = selectedTabIndex,
                contentColor = MaterialTheme.colorScheme.onSurface,
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        modifier = Modifier.weight(1f),
                        text = { Text(title) },
                        selected = (selectedTabIndex == index),
                        onClick = { selectedTabIndex = index }
                    )
                }
            }

            //호가, 차트, 시세 스크린
            when (selectedTabIndex) {
                0 -> OrderBookSection(orderbook, ticker, changeRate, symbol)
                1 -> ChartSection(symbol)
                2 -> TradeSection(trades, dayCandle, color)
            }

        }
    }
}
