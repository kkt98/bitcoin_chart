package com.kkt981019.bitcoin_chart.screen.coindetail

import ChartSection
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.kkt981019.bitcoin_chart.R
import com.kkt981019.bitcoin_chart.screen.coindetail.coinorder.CoinOrderSection
import com.kkt981019.bitcoin_chart.screen.coindetail.orderbook.OrderBookSection
import com.kkt981019.bitcoin_chart.screen.coindetail.trade.TradeSection
import com.kkt981019.bitcoin_chart.util.DecimalFormat.getTradeFormatters
import com.kkt981019.bitcoin_chart.viewmodel.CoinDtOrderBookViewModel
import com.kkt981019.bitcoin_chart.viewmodel.FavoriteViewModel
import com.kkt981019.bitcoin_chart.viewmodel.MyPageViewModel
import java.text.DecimalFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoinDetailScreen(
    symbol: String,
    koreanName: String,
    englishName: String,
    navController: NavController,
    viewModel: CoinDtOrderBookViewModel = hiltViewModel(), // Hilt로 주입받음
    favoriteViewModel: FavoriteViewModel = hiltViewModel(),
) {
    // 화면 진입 시, 해당 심볼로 웹소켓 연결 시작
    LaunchedEffect(symbol) {
        viewModel.startOrderBook(symbol)
        viewModel.startTicker(symbol)
    }

    val favList by favoriteViewModel.favorites.observeAsState(emptyList())
    val isFavorite = remember(favList) {
        favList.any { it.market == symbol }
    }

    // ViewModel의 LiveData를 observeAsState로 관찰
    val ticker by viewModel.tickerState.observeAsState()
    val orderbook by viewModel.orderbookState.observeAsState() //호가정보

    // 탭 UI 구성
    val tabTitles = listOf("호가", "주문", "차트", "시세")
    var selectedTabIndex by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "$koreanName ($symbol)", fontSize = 21.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "뒤로가기 버튼"
                        )
                    }
                },
                actions = {
                    IconToggleButton(
                        checked = isFavorite,
                        onCheckedChange = {
                            favoriteViewModel.toggleFavorite(
                                market = symbol,
                                kor    = koreanName,
                                eng    = englishName
                            )
                        }
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Filled.Star else Icons.Outlined.Star,
                            contentDescription = if (isFavorite) "즐겨찾기 해제" else "즐겨찾기",
                            tint = if (isFavorite) Color(0xFF2979FF) else Color.Gray,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }
            )
        }
    ) { inner ->
        val topInset = (inner.calculateTopPadding() - 16.dp).coerceAtLeast(0.dp)

        Column(
            modifier = Modifier
                .padding(
                    top    = topInset,
                    start  = inner.calculateStartPadding(LayoutDirection.Ltr),
                    end    = inner.calculateEndPadding(LayoutDirection.Ltr),
                    bottom = inner.calculateBottomPadding()
                )
                .fillMaxSize()
        ) {
            val format= getTradeFormatters(symbol.substringBefore('-'))

            val si = DecimalFormat("#,##0.###")

            val color = when(ticker?.change) {
                "EVEN" -> Color.Black
                "RISE" -> Color.Red
                else -> Color.Blue
            }

            // 티커의 change 값에 따라 다른 아이콘을 표시
            val changeIcon: Painter? = when (ticker?.change) {
                "EVEN" -> null
                "RISE" -> painterResource(id = R.drawable.triangle_up)
                else -> painterResource(id = R.drawable.triangle_down)
            }

            val changeRate = String.format("%.2f%%", (ticker?.signed_change_rate?.toDoubleOrNull()?.times(100)) ?: 0.0)

            // trade_price와 Row를 감싸는 Column에 padding(16.dp) 적용
            Column(modifier = Modifier.padding(16.dp)) {
                // trade_price는 숫자로 변환 후 포맷팅
                Text(
                    text = format.priceDf.format(ticker?.trade_price?.toDoubleOrNull() ?: 0.0),
                    fontSize = 21.sp,
                    color = color
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = changeRate,
                        fontSize = 14.sp,
                        color = color,
                        modifier = Modifier.padding(end = 14.dp)
                    )

                    if (changeIcon != null) {
                        Icon(
                            painter = changeIcon,
                            contentDescription = "변동 아이콘",
                            tint = color,
                            modifier = Modifier.size(15.dp) // 아이콘 크기를 24.dp로 설정
                                .padding(end = 6.dp)
                        )
                    }

                    Text(
                        text = si.format(ticker?.signed_change_price?.toDoubleOrNull() ?: 0.0),
                        fontSize = 14.sp,
                        color = color
                    )
                }
            }

            //탭
            TabRow(
                selectedTabIndex = selectedTabIndex,
                // 탭 배경색(선택사항)
                containerColor = Color.Transparent,
                // 인디케이터(밑줄) 커스터마이즈
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        modifier = Modifier
                            .tabIndicatorOffset(tabPositions[selectedTabIndex])
                            .height(3.dp),        // 인디케이터 높이 조절
                        color = Color(0xFF2979FF)       // 원하는 색으로 변경
                    )
                }
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
                1 -> CoinOrderSection(orderbook, ticker, changeRate, symbol, koreanName, englishName)
                2 -> ChartSection(symbol)
                3 -> TradeSection(symbol, color)
            }
        }
    }
}
