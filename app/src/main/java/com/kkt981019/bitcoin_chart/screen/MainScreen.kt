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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.kkt981019.bitcoin_chart.R
import com.kkt981019.bitcoin_chart.network.Data.CoinData
import com.kkt981019.bitcoin_chart.util.DecimalFormat.getTradeFormatters
import com.kkt981019.bitcoin_chart.viewmodel.MainScreenVM
import kotlinx.coroutines.delay
import java.text.DecimalFormat
import kotlin.text.substringBefore

// 가격 정렬 상태를 정의하는 enum
enum class PriceSort { NONE, DESC, ASC }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navController: NavController,
    viewModel: MainScreenVM = hiltViewModel() // Hilt를 통한 ViewModel 주입
) {
    // LiveData로부터 코인 목록을 구독하고 초기값은 빈 리스트
    val coins by viewModel.coins.observeAsState(emptyList())

    // 검색어, 언어 토글, 정렬 상태, 정렬 기준, 탭 인덱스 설정
    var query       by remember { mutableStateOf("") }
    var useEnglish  by remember { mutableStateOf(false) }
    var priceSort   by remember { mutableStateOf(PriceSort.NONE) }
    var sortBy      by remember { mutableStateOf("none") }
    val tabs        = listOf("KRW","BTC","USDT","관심")
    var selectedTab by rememberSaveable { mutableStateOf(0) }

    // 컴포저블 시작 시 및 탭 변경 시 ViewModel에 데이터 요청
    LaunchedEffect(selectedTab) {
        viewModel.fetchCoins(selectedTab)
    }

    // 테마에서 배경 색상 가져오기
    val bg = MaterialTheme.colorScheme.background

    // 정렬 로직: 가격, 변동률, 거래대금 순으로 오름/내림차순 처리
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
    // 사용자가 선택한 기준에 따라 정렬된 리스트 선택
    val displayed = when (sortBy) {
        "price"  -> sortedByPrice
        "rate"   -> sortedByRate
        "volume" -> sortedByVol
        else     -> coins
    }
    // 검색어가 있다면 이름/심볼로 필터링
    val filtered = if (query.isNotBlank()) {
        displayed.filter {
            it.koreanName.contains(query, true)
                    || it.englishName.contains(query, true)
                    || it.symbol.contains(query, true)
        }
    } else displayed

    // 화면 레이아웃: Scaffold로 구조 구성
    Scaffold(
        modifier = Modifier.background(bg)
    ) { inner ->
        // 안전 영역 패딩 계산
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
            // 검색 바 Row
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
                        focusedContainerColor   = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor   = Color.Black,
                        unfocusedIndicatorColor = Color.Gray,
                        cursorColor             = Color.Black,
                        focusedPlaceholderColor   = Color.Gray,
                        unfocusedPlaceholderColor = Color.Gray,
                    )
                )
            }

            // 탭 선택 영역
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor   = Color.Transparent,
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        modifier = Modifier
                            .tabIndicatorOffset(tabPositions[selectedTab])
                            .height(3.dp),
                        color = Color(0xFF2979FF)
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected               = selectedTab == index,
                        onClick                = { selectedTab = index },
                        text                   = { Text(title) },
                        selectedContentColor   = Color.Black,
                        unselectedContentColor = Color.Black
                    )
                }
            }

            // 목록 헤더 (탭이 0~3인 경우만 표시)
            if (selectedTab in 0..3) {
                CoinListHeader(
                    useEnglish = useEnglish,
                    priceSort  = priceSort,
                    sortBy     = sortBy,
                    onToggleLanguage   = { useEnglish = !useEnglish },
                    onCurrentPriceClick = {
                        // 가격 클릭 시 오름/내림/기본 순환
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

            // 코인 목록 출력
            LazyColumn(Modifier.fillMaxSize()) {
                items(filtered) { coin ->
                    CoinItemRow(
                        coin               = coin,
                        backgroundColor    = bg,
                        useEnglish         = useEnglish,
                        selectedTabIndex   = selectedTab,
                        onClick            = {
                            // 클릭 시 상세 화면으로 네비게이션
                            navController.navigate(
                                "coin_detail/${coin.symbol}/${coin.koreanName}/${coin.englishName}"
                            )
                        }
                    )
                }
            }
        }
    }
}

/**
 * 코인 목록의 각 컬럼 제목과 정렬 아이콘을 표시하는 헤더 컴포저블
 */
@Composable
fun CoinListHeader(
    useEnglish: Boolean,
    priceSort: PriceSort,
    sortBy: String,
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
        // (1) 이름 컬럼
        Box(
            modifier = Modifier.weight(1f)
                .clickable(onClick = onToggleLanguage),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (useEnglish) "영문명" else "한글명",
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(Modifier.width(4.dp))
                Icon(
                    painter           = painterResource(R.drawable.exchange),
                    contentDescription = null,
                    modifier          = Modifier.size(8.dp),
                )
            }
        }

        // (2) 현재가 컬럼
        Box(
            modifier = Modifier.weight(1f).clickable(onClick = onCurrentPriceClick),
            contentAlignment = Alignment.CenterEnd
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("현재가", style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.width(4.dp))
                // 오름/내림 정렬 아이콘 색 설정
                val priceUpTint   = if (sortBy=="price" && priceSort==PriceSort.ASC )  Color.Blue else Color.Gray
                val priceDownTint = if (sortBy=="price" && priceSort==PriceSort.DESC) Color.Blue else Color.Gray
                Column(
                    verticalArrangement = Arrangement.Center,
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

        // (3) 전일대비 컬럼
        Box(
            modifier = Modifier.weight(1f).clickable(onClick = onChangeRateClick),
            contentAlignment = Alignment.CenterEnd
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("전일대비", style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.width(4.dp))
                val rateUpTint   = if (sortBy=="rate" && priceSort==PriceSort.ASC )  Color.Blue else Color.Gray
                val rateDownTint = if (sortBy=="rate" && priceSort==PriceSort.DESC) Color.Blue else Color.Gray
                Column(
                    verticalArrangement = Arrangement.Center,
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

        // (4) 거래대금 컬럼
        Box(
            modifier = Modifier.weight(1f).clickable(onClick = onVolumeClick),
            contentAlignment = Alignment.CenterEnd
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("거래대금", style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.width(4.dp))
                val volUpTint   = if (sortBy=="volume" && priceSort==PriceSort.ASC )  Color.Blue else Color.Gray
                val volDownTint = if (sortBy=="volume" && priceSort==PriceSort.DESC) Color.Blue else Color.Gray
                Column(
                    verticalArrangement = Arrangement.Center,
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

    // 구분선
    Divider(
        color     = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
        thickness = 0.5.dp
    )
}

/**
 * 각 코인 항목을 표시하는 리스트 행 컴포저블
 */
@Composable
fun CoinItemRow(
    coin: CoinData,
    backgroundColor: Color,
    useEnglish: Boolean,
    selectedTabIndex: Int,
    onClick: () -> Unit
) {
    // 상승/하락/변동 없음에 따른 텍스트 색상 설정
    val color = when (coin.change) {
        "RISE" -> Color.Red
        "EVEN" -> Color.Black
        else    -> Color.Blue
    }

    // 화폐 단위에 맞춘 거래량 포맷터 가져오기
    val format = getTradeFormatters(coin.symbol.substringBefore('-'))

    // 거래대금 단위 표시 (백만 단위 등)
    val volumeText = when {
        coin.symbol.startsWith("KRW") -> "${DecimalFormat("#,##0").format((coin.volume ?: 0.0) / 1_000_000)}백만"
        coin.symbol.startsWith("BTC") -> String.format("%.3f", coin.volume)
        else -> String.format("%,.3f", coin.volume)
    }

    // 표시할 이름: 영어/한글 선택
    val name = if (useEnglish) coin.englishName else coin.koreanName

    // 이전 가격과 테두리 색상을 기억하여 깜빡임 효과 구현
    val previousPrice = remember(coin.symbol) { mutableStateOf(coin.tradePrice ?: 0.0) }
    val borderColor   = remember(coin.symbol) { mutableStateOf(Color.Transparent) }

    LaunchedEffect(coin.tradePrice) {
        val newPrice = coin.tradePrice ?: 0.0
        if (newPrice > previousPrice.value) {
            // 가격 상승 시 빨간색 테두리 깜빡임
            repeat(3) {
                borderColor.value = Color.Red; delay(50)
                borderColor.value = Color.Transparent; delay(50)
            }
        } else if (newPrice < previousPrice.value) {
            // 가격 하락 시 파란색 테두리 깜빡임
            repeat(3) {
                borderColor.value = Color.Blue; delay(50)
                borderColor.value = Color.Transparent; delay(50)
            }
        }
        previousPrice.value = newPrice
    }

    // 행 레이아웃
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(backgroundColor)
            .padding(vertical = 8.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 이름 및 심볼
        Column(Modifier.weight(1f)) {
            Text(name, fontSize = 13.sp)
            Text(coin.symbol, fontSize = 10.sp, color = Color.Gray)
        }

        // 현재가 (테두리 효과 포함)
        Column(Modifier.weight(1f), horizontalAlignment = Alignment.End) {
            Box(Modifier.border(1.dp, borderColor.value).padding(2.dp)) {
                Text(format.priceDf.format(coin.tradePrice ?: 0.0), fontSize = 13.sp, color = color)
            }
        }

        // 전일 대비 및 절대 변동값
        Column(Modifier.weight(1f), horizontalAlignment = Alignment.End) {
            Text(String.format("%.2f%%", (coin.changeRate ?: 0.0) * 100), color = color, fontSize = 13.sp)
            if (selectedTabIndex == 0) {
                Text(text = DecimalFormat("#,##0.###").format(coin.signed ?: 0.0), fontSize = 10.sp, color = color)
            }
        }

        // 거래대금 표시
        Column(Modifier.weight(1f), horizontalAlignment = Alignment.End) {
            Text(volumeText, color = Color.Black, fontSize = 13.sp)
        }
    }

    // 각 항목 구분선
    Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f), thickness = 0.5.dp)
}
