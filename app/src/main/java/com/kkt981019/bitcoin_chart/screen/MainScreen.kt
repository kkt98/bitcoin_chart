package com.kkt981019.bitcoin_chart.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.kkt981019.bitcoin_chart.R
import com.kkt981019.bitcoin_chart.network.Data.CoinData
import com.kkt981019.bitcoin_chart.util.DecimalFormat.getTradeFormatters
import com.kkt981019.bitcoin_chart.viewmodel.MainScreenVM
import kotlinx.coroutines.delay
import java.text.DecimalFormat
import kotlin.text.substringBefore

enum class PriceSort { NONE, DESC, ASC }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navController: NavController,
    viewModel: MainScreenVM = hiltViewModel()
) {
    val coins by viewModel.coins.observeAsState(emptyList())

    // 검색어, 탭, 언어, 정렬 상태
    var query       by remember { mutableStateOf("") }
    var useEnglish  by remember { mutableStateOf(false) }
    var priceSort   by remember { mutableStateOf(PriceSort.NONE) }
    var sortBy      by remember { mutableStateOf("none") }
    val tabs        = listOf("KRW","BTC","USDT","관심")
    var selectedTab by remember { mutableStateOf(0) }

    // 화면 진입 & 탭 변경 시 데이터 로드
    LaunchedEffect(selectedTab) {
        viewModel.fetchCoins(selectedTab)
    }

    // 배경 색
    val bg      = MaterialTheme.colorScheme.background
    val surface = MaterialTheme.colorScheme.surface

    // 정렬·검색 처리
    val sortedByPrice = when (priceSort) {
        PriceSort.DESC -> coins.sortedByDescending { it.tradePrice ?: 0.0 }
        PriceSort.ASC  -> coins.sortedBy { it.tradePrice ?: 0.0 }
        else           -> coins
    }
    val sortedByRate = when (priceSort) {
        PriceSort.DESC -> coins.sortedByDescending { it.changeRate ?: 0.0 }
        PriceSort.ASC  -> coins.sortedBy { it.changeRate ?: 0.0 }
        else           -> coins
    }
    val sortedByVol  = when (priceSort) {
        PriceSort.DESC -> coins.sortedByDescending { it.volume ?: 0.0 }
        PriceSort.ASC  -> coins.sortedBy { it.volume ?: 0.0 }
        else           -> coins
    }
    val displayed = when (sortBy) {
        "price"  -> sortedByPrice
        "rate"   -> sortedByRate
        "volume" -> sortedByVol
        else     -> coins
    }
    val filtered = if (query.isNotBlank()) {
        displayed.filter {
            it.koreanName.contains(query, true)
                    || it.englishName.contains(query, true)
                    || it.symbol.contains(query, true)
        }
    } else displayed

    Scaffold(
        modifier = Modifier.background(bg),
        topBar = {
            TopAppBar(
                title = { Text("거래소") },
                modifier = Modifier.background(surface)
            )
        }
    ) { inner ->
        val topInset = (inner.calculateTopPadding() - 16.dp).coerceAtLeast(0.dp)
        Column(
            Modifier
                .fillMaxSize()
                .padding(
                    top    = topInset,
                    start  = inner.calculateStartPadding(LayoutDirection.Ltr),
                    end    = inner.calculateEndPadding(LayoutDirection.Ltr),
                    bottom = inner.calculateBottomPadding()
                )
        ) {
            // 검색 바
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.Search, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                TextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("코인명/심볼 검색", color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        // container 색
                        focusedContainerColor   = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        // 밑줄(Indicator) 색
                        focusedIndicatorColor   = Color.Black,
                        unfocusedIndicatorColor = Color.Gray,
                        // 커서 색
                        cursorColor             = Color.Black,
                        // 플레이스홀더 색
                        focusedPlaceholderColor   = Color.Gray,
                        unfocusedPlaceholderColor = Color.Gray,
                    )
                )
            }

            // 탭
            TabRow(
                selectedTabIndex = selectedTab,
                // 탭 배경색(선택사항)
                containerColor = Color.Transparent,
                // 인디케이터
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        modifier = Modifier
                            .tabIndicatorOffset(tabPositions[selectedTab])
                            .height(3.dp),        // 인디케이터 높이 조절
                        color = Color(0xFF2979FF)       // 원하는 색으로 변경
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) },
                        selectedContentColor = Color.Black,
                        unselectedContentColor = Color.Black
                    )
                }
            }

            // 헤더 (0..2 탭만 보여줘)
            if (selectedTab in 0..3) {
                CoinListHeader(
                    useEnglish = useEnglish,
                    priceSort = priceSort,
                    sortBy = sortBy,
                    onToggleLanguage = { useEnglish = !useEnglish },
                    onCurrentPriceClick = {
                        priceSort = when (priceSort) {
                            PriceSort.NONE -> PriceSort.DESC
                            PriceSort.DESC -> PriceSort.ASC
                            PriceSort.ASC  -> PriceSort.NONE
                        }
                        sortBy = "price"
                    },
                    onChangeRateClick = {
                        priceSort = when (priceSort) {
                            PriceSort.NONE -> PriceSort.DESC
                            PriceSort.DESC -> PriceSort.ASC
                            PriceSort.ASC  -> PriceSort.NONE
                        }
                        sortBy = "rate"
                    },
                    onVolumeClick = {
                        priceSort = when (priceSort) {
                            PriceSort.NONE -> PriceSort.DESC
                            PriceSort.DESC -> PriceSort.ASC
                            PriceSort.ASC  -> PriceSort.NONE
                        }
                        sortBy = "volume"
                    }
                )
            }

            // 리스트
            LazyColumn(Modifier.fillMaxSize()) {
                items(filtered) { coin ->
                    CoinItemRow(
                        coin = coin,
                        backgroundColor   = bg,
                        useEnglish        = useEnglish,
                        selectedTabIndex  = selectedTab
                    ) {
                        navController.navigate(
                            "coin_detail/${coin.symbol}/${coin.koreanName}/${coin.englishName}"
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CoinListHeader(
    useEnglish: Boolean,
    priceSort: PriceSort,
    sortBy: String,              // ← 여기 추가
    onToggleLanguage: () -> Unit,
    onCurrentPriceClick: () -> Unit,
    onChangeRateClick: () -> Unit,
    onVolumeClick: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        // (1) 이름
        Box(modifier = Modifier.weight(1f)
            .clickable(onClick = onToggleLanguage),
            contentAlignment = Alignment.CenterStart)
        {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (useEnglish) "영문명" else "한글명",
//                textAlign = TextAlign.Start,
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(Modifier.width(4.dp))
                Icon(
                    painter = painterResource(R.drawable.exchange),
                    contentDescription = null,
                    modifier = Modifier.size(8.dp),
                )
            }
        }

        // (2) 현재가
        Box(
            modifier = Modifier.weight(1f).clickable(onClick = onCurrentPriceClick),
            contentAlignment = Alignment.CenterEnd
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("현재가", style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.width(4.dp))
                // price 컬럼 전용 tint
                val priceUpTint   = if (sortBy=="price"  && priceSort==PriceSort.ASC )  Color.Blue else Color.Gray
                val priceDownTint = if (sortBy=="price"  && priceSort==PriceSort.DESC)  Color.Blue else Color.Gray
                Column(
                    verticalArrangement   = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        painter           = painterResource(R.drawable.triangle_up),
                        contentDescription = null,
                        modifier          = Modifier.size(8.dp),
                        tint              = priceUpTint
                    )
                    Icon(
                        painter           = painterResource(R.drawable.triangle_down),
                        contentDescription = null,
                        modifier          = Modifier.size(8.dp),
                        tint              = priceDownTint
                    )
                }
            }
        }

        // (3) 전일대비
        Box(
            modifier = Modifier.weight(1f).clickable(onClick = onChangeRateClick),
            contentAlignment = Alignment.CenterEnd
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("전일대비", style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.width(4.dp))
                // rate 컬럼 전용 tint
                val rateUpTint   = if (sortBy=="rate"  && priceSort==PriceSort.ASC )  Color.Blue else Color.Gray
                val rateDownTint = if (sortBy=="rate"  && priceSort==PriceSort.DESC)  Color.Blue else Color.Gray
                Column(
                    verticalArrangement   = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        painter           = painterResource(R.drawable.triangle_up),
                        contentDescription = null,
                        modifier          = Modifier.size(8.dp),
                        tint              = rateUpTint
                    )
                    Icon(
                        painter           = painterResource(R.drawable.triangle_down),
                        contentDescription = null,
                        modifier          = Modifier.size(8.dp),
                        tint              = rateDownTint
                    )
                }
            }
        }

        // (4) 거래대금
        Box(
            modifier = Modifier.weight(1f).clickable(onClick = onVolumeClick),
            contentAlignment = Alignment.CenterEnd
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("거래대금", style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.width(4.dp))
                // volume 컬럼 전용 tint
                val volUpTint   = if (sortBy=="volume"  && priceSort==PriceSort.ASC )  Color.Blue else Color.Gray
                val volDownTint = if (sortBy=="volume"  && priceSort==PriceSort.DESC)  Color.Blue else Color.Gray
                Column(
                    verticalArrangement   = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        painter           = painterResource(R.drawable.triangle_up),
                        contentDescription = null,
                        modifier          = Modifier.size(8.dp),
                        tint              = volUpTint
                    )
                    Icon(
                        painter           = painterResource(R.drawable.triangle_down),
                        contentDescription = null,
                        modifier          = Modifier.size(8.dp),
                        tint              = volDownTint
                    )
                }
            }
        }
    }

    Divider(
        color     = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
        thickness = 0.5.dp
    )
}



@Composable
fun CoinItemRow(
    coin: CoinData,
    backgroundColor: Color,
    useEnglish: Boolean,
    selectedTabIndex: Int,
    onClick: () -> Unit
) {
    val color = when (coin.change) {
        "RISE" -> Color.Red
        "EVEN" -> Color.Black
        else   -> Color.Blue
    }

    val format= getTradeFormatters(coin.symbol.substringBefore('-'))

    val volumeText = when {
        coin.symbol.startsWith("KRW") -> "${DecimalFormat("#,##0").format((coin.volume ?: 0.0) / 1_000_000)}백만"
        coin.symbol.startsWith("BTC") -> String.format("%.3f", coin.volume)
        else -> String.format("%,.3f", coin.volume)
    }

    val name = if (useEnglish) coin.englishName else coin.koreanName

    // 이전 가격와 테두리 색상 상태를 기억합니다.
    val previousPrice = remember(coin.symbol) { mutableStateOf(coin.tradePrice ?: 0.0) }
    val borderColor = remember(coin.symbol) { mutableStateOf(Color.Transparent) }

    // 가격이 바뀔 때마다 실행되는 효과 블록
    LaunchedEffect(coin.tradePrice) {
        val newPrice = coin.tradePrice ?: 0.0
        if (newPrice > previousPrice.value) {
            // 상승한 경우: 빨간색 테두리 깜빡임
            repeat(3) {
                borderColor.value = Color.Red
                delay(50)
                borderColor.value = Color.Transparent
                delay(50)
            }
        } else if (newPrice < previousPrice.value) {
            // 하락한 경우: 파란색 테두리 깜빡임
            repeat(3) {
                borderColor.value = Color.Blue
                delay(50)
                borderColor.value = Color.Transparent
                delay(50)
            }
        }
        // 변동이 없으면 아무 효과 없음
        previousPrice.value = newPrice
    }


    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(backgroundColor)
            .border(width = 2.dp, color = borderColor.value, shape = MaterialTheme.shapes.medium)
            .padding(vertical = 8.dp, horizontal = 16.dp),

        verticalAlignment = Alignment.CenterVertically
    ) {

        Column(Modifier.weight(1f)) {
            Text(name, style = MaterialTheme.typography.bodyMedium)
            Text(coin.symbol, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        }

        Column(Modifier.weight(1f), horizontalAlignment = Alignment.End) {
            Text(format.priceDf.format(coin.tradePrice ?: 0.0), color = color)
        }

        Column(Modifier.weight(1f), horizontalAlignment = Alignment.End) {
            Text(String.format("%.2f%%", (coin.changeRate ?: 0.0) * 100), color = color)
            when (selectedTabIndex) {
                0 -> Text(text = DecimalFormat("#,##0.###").format(coin.signed ?: 0.0),
                    style = MaterialTheme.typography.labelSmall, color = color)
                1 -> null
                2 -> null
            }
        }
        Column(Modifier.weight(1f), horizontalAlignment = Alignment.End) {
            Text(volumeText, color = Color.Black)
        }
    }
    Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f), thickness = 0.5.dp)
}
