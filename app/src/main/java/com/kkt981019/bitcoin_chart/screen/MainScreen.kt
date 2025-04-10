package com.kkt981019.bitcoin_chart.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.kkt981019.bitcoin_chart.network.Data.CoinData
import com.kkt981019.bitcoin_chart.viewmodel.MainScreenVM
import kotlinx.coroutines.delay
import okhttp3.WebSocket
import java.text.DecimalFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navController: NavController,
    viewModel : MainScreenVM = hiltViewModel(),
) {
    // Material3 테마의 컬러
    val backgroundColor = MaterialTheme.colorScheme.background
    val surfaceColor = MaterialTheme.colorScheme.surface

    val coinList by viewModel.coinList.observeAsState(emptyList())

    // 검색어 상태
    var searchQuery by remember { mutableStateOf("") }

    // 탭 상태
    val tabTitles = listOf("KRW", "BTC", "USDT")
    var selectedTabIndex by remember { mutableStateOf(0) }

    // 탭 선택 시 접두어 업데이트
    LaunchedEffect(selectedTabIndex) {
        when (selectedTabIndex) {
            0 -> viewModel.setMarketPrefix("KRW-")
            1 -> viewModel.setMarketPrefix("BTC-")
            2 -> viewModel.setMarketPrefix("USDT-")
        }
    }

    // 한글명, 영어명
    var useLanguage by remember { mutableStateOf(false) }
    // 현재가 정렬 상태 (NONE: 원래 순서, DESC: 높은순, ASC: 낮은순)
    var currentPriceSort by remember { mutableStateOf(PriceSort.NONE) }
    // 어떤 기준으로 정렬할지 결정 ("none", "price", "rate")
    var sortBy by remember { mutableStateOf("none") }

    // currentPriceSort 상태에 따라 coinList 정렬 (tradePrice가 null이면 0.0 사용)
    val sortedCoinList = when (currentPriceSort) {
        PriceSort.DESC -> coinList.sortedByDescending { it.tradePrice ?: 0.0 }
        PriceSort.ASC -> coinList.sortedBy { it.tradePrice ?: 0.0 }
        PriceSort.NONE -> coinList
    }

    val sortedByRate = when (currentPriceSort) {
        PriceSort.DESC -> coinList.sortedByDescending { it.changeRate ?: 0.0 }
        PriceSort.ASC -> coinList.sortedBy { it.changeRate ?: 0.0 }
        PriceSort.NONE -> coinList
    }

    val sortedByVolume = when (currentPriceSort) {
        PriceSort.DESC -> coinList.sortedByDescending { it.volume ?: 0.0 }
        PriceSort.ASC -> coinList.sortedBy { it.volume ?: 0.0 }
        PriceSort.NONE -> coinList
    }

    // 선택된 정렬 기준에 따라 표시할 리스트 결정
    val displayedCoinList = when (sortBy) {
        "price" -> sortedCoinList
        "rate" -> sortedByRate
        "volume" -> sortedByVolume
        else -> coinList
    }

    val finalFilterList = if (searchQuery.isNotBlank()) {
        displayedCoinList.filter { coin ->
            coin.koreanName.contains(searchQuery, ignoreCase = true) ||
                    coin.englishName.contains(searchQuery, ignoreCase = true) ||
                    coin.symbol.contains(searchQuery, ignoreCase = true)
        }
    } else {
        displayedCoinList
    }

    Scaffold(
        modifier = Modifier.background(backgroundColor),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "거래소",
                        color = Color.Black
                    )
                },
                actions = {
                    // 우측에 추가 아이콘이나 버튼이 필요하면 배치
                },
                modifier = Modifier.background(surfaceColor)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            // 검색 영역
            SearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it }
            )

            // 탭 영역
            TabRow(
                selectedTabIndex = selectedTabIndex,
                modifier = Modifier.background(backgroundColor),
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = (selectedTabIndex == index),
                        onClick = { selectedTabIndex = index },
                        text = { Text(title) }
                    )
                }
            }

            // 헤더 (한글명 / 하한가 / 전일대비 / 거래대금)
            CoinListHeader(
                useLanguage,
                onToggleLanguage =  { useLanguage = !useLanguage },
                onCurrentPriceClick = {
                    currentPriceSort = when (currentPriceSort) {
                        PriceSort.NONE -> PriceSort.DESC
                        PriceSort.DESC -> PriceSort.ASC
                        PriceSort.ASC -> PriceSort.NONE
                    }
                    sortBy = "price"
                },

                onChangeRateClick = {
                    currentPriceSort = when (currentPriceSort) {
                        PriceSort.NONE -> PriceSort.DESC
                        PriceSort.DESC -> PriceSort.ASC
                        PriceSort.ASC -> PriceSort.NONE
                    }
                    sortBy = "rate"
                },
                onVolumeClick = {
                    currentPriceSort = when (currentPriceSort) {
                        PriceSort.NONE -> PriceSort.DESC
                        PriceSort.DESC -> PriceSort.ASC
                        PriceSort.ASC -> PriceSort.NONE
                    }
                    sortBy = "volume"
                }
            )
            var currentSocket: WebSocket? = null

            // 코인 리스트
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(finalFilterList) { coin ->
                    CoinItemRow(
                        coin,
                        backgroundColor = backgroundColor,
                        useLanguage, selectedTabIndex,
                        onClick = {
                            navController.navigate("coin_detail/${coin.symbol}/${coin.koreanName}")
                            currentSocket?.close(1000, "change page")
                        }
                    )
                }
            }
        }
    }
}

/** 검색 영역 **/
@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Spacer(modifier = Modifier.width(8.dp))
        TextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.weight(1f),
            placeholder = {
                Text(text = "코인명/심볼 검색", color = Color.Gray)
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Black
            ),
            leadingIcon = { Icon(  // 왼쪽 Icon 지정
                imageVector = Icons.Default.Search
                , contentDescription = null
            )}
        )
    }
}

/** 열(컬럼) 헤더: "한글명 / 현재가 / 전일대비 / 거래대금" **/
@Composable
fun CoinListHeader(
    useEnglish: Boolean,
    onToggleLanguage: () -> Unit,
    onCurrentPriceClick: () -> Unit,
    onChangeRateClick: () -> Unit,
    onVolumeClick: () -> Unit
) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),

        ) {
            Text(
                text = if(useEnglish) "영문명" else "한글명",
                modifier = Modifier
                    .weight(1f)
                    .clickable { onToggleLanguage() },
                textAlign = TextAlign.Start,
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "현재가",
                modifier = Modifier
                    .weight(1f)
                    .clickable { (onCurrentPriceClick()) },
                textAlign = TextAlign.End,
                style = MaterialTheme.typography.bodySmall,
            )
            Text(
                text = "전일대비",
                modifier = Modifier
                    .weight(1f)
                    .clickable { (onChangeRateClick()) },
                textAlign = TextAlign.End,
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "거래대금",
                modifier = Modifier
                    .weight(1f)
                    .clickable { (onVolumeClick()) },
                textAlign = TextAlign.End,
                style = MaterialTheme.typography.bodySmall
            )
        }
        Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f), thickness = 0.5.dp)
    }

/** 코인 리스트 아이템 **/
@Composable
fun CoinItemRow(
    coin: CoinData,
    backgroundColor: Color,
    useEnglish: Boolean,
    selectedTabIndex: Int,
    onClick: () -> Unit)
{

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
                delay(80)
                borderColor.value = Color.Transparent
                delay(80)
            }
        } else if (newPrice < previousPrice.value) {
            // 하락한 경우: 파란색 테두리 깜빡임
            repeat(3) {
                borderColor.value = Color.Blue
                delay(80)
                borderColor.value = Color.Transparent
                delay(80)
            }
        }
        // 변동이 없으면 아무 효과 없음
        previousPrice.value = newPrice
    }


    val color = when(coin.change) {
        "EVEN" -> Color.Black
        "RISE" -> Color.Red
        else -> Color.Blue
    }

    //소수점 보여주기 (현재가)
    val df = when (selectedTabIndex) {
        0 -> DecimalFormat("#,##0.##")
        1 -> DecimalFormat("0.00000000")
        else-> DecimalFormat("#,##0.000#####")
    }

    // valume
    val volumeString = when (selectedTabIndex) {
        0 -> {
            "${DecimalFormat("#,##0").format(coin.volume?.div(1_000_000) ?: 0)}백만"
        }
        1 -> String.format("%.3f", coin.volume)
        else-> String.format("%,.3f", coin.volume)
    }

    //전일대비
    val si = DecimalFormat("#,##0.###")

    // 코인이름 영여 or 한글 설정
    val coinName = if (useEnglish) coin.englishName else coin.koreanName

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .background(backgroundColor)
            .border(width = 2.dp, color = borderColor.value, shape = MaterialTheme.shapes.medium)
            .padding(vertical = 8.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 한글명
        Column(modifier = Modifier.weight(1f)) {

            Text(text = coinName, style = MaterialTheme.typography.bodyMedium)
            Text(text = coin.symbol, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        }

        // 현재가
        Column(modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.End) {
            Text(
                text = df.format(coin.tradePrice ?: 0.0),
                color = color,
                style = MaterialTheme.typography.bodyMedium,
            )
            Text("")
        }

        // 전일대비
        Column(modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.End) {
            Text(
                text = String.format("%.2f%%", (coin.changeRate?.times(100)) ?: 0.0),
                color = color,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.End
            )
            when (selectedTabIndex) {
                0 -> Text(text = si.format(coin.signed ?: 0.0), style = MaterialTheme.typography.labelSmall, color = color)
                1 -> Text("")
                2 -> Text("")
            }
        }

        // 거래대금
        Column(modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.End) {
            Text(
                text = volumeString,
                color = Color.Black,
                style = MaterialTheme.typography.bodyMedium,
            )
            Text("")
        }
    }
    Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f), thickness = 0.5.dp)
}

enum class PriceSort { NONE, DESC, ASC }