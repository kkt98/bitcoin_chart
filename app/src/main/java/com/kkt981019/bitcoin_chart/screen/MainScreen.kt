package com.kkt981019.bitcoin_chart.screen

import androidx.compose.foundation.background
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
import com.kkt981019.bitcoin_chart.network.Data.CoinData
import com.kkt981019.bitcoin_chart.viewmodel.RetrofitViewModel
import java.text.DecimalFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel : RetrofitViewModel = hiltViewModel()
) {
    // Material3 테마의 컬러
    val backgroundColor = MaterialTheme.colorScheme.background
    val surfaceColor = MaterialTheme.colorScheme.surface

    // 검색어 상태
    var searchQuery by remember { mutableStateOf("") }

    // 탭 상태
    val tabTitles = listOf("KRW", "BTC", "USDT")
    var selectedTabIndex by remember { mutableStateOf(0) }
    var useLanguage by remember { mutableStateOf(false) }

    val coinList by viewModel.coinList.observeAsState(emptyList())

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
//                onCurrentPriceClick = TODO(),
//                onChangeRateClick = TODO(),
//                onVolumeClick = TODO()
            )

            // 코인 리스트
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(coinList) { coin ->
                    CoinItemRow(coin, backgroundColor = backgroundColor, useLanguage)
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
        onCurrentPriceClick: () -> Unit = {},
        onChangeRateClick: () -> Unit = {},
        onVolumeClick: () -> Unit = {}
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),

        ) {
            Text(
                text = if(useEnglish) "영문명" else "한글명",
                modifier = Modifier.weight(1f)
                    .clickable { onToggleLanguage() },
                textAlign = TextAlign.Start,
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "현재가",
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.End,
                style = MaterialTheme.typography.bodySmall,
            )
            Text(
                text = "전일대비",
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.End,
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "거래대금",
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.End,
                style = MaterialTheme.typography.bodySmall
            )
        }
        Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f), thickness = 0.5.dp)
    }

/** 코인 리스트 아이템 **/
@Composable
fun CoinItemRow(coin: CoinData, backgroundColor: Color, useEnglish: Boolean) {
    // 전일대비(%)가 +면 빨간색, -면 파란색
    val changeColor = if ((coin.changeRate ?: 0.0) >= 0) Color.Red else Color.Blue

    //소수점 아래 0이면 생략
    val df = DecimalFormat("#,##0.##")

    // 코인이름 영여 or 한글 설정
    val coinName = if (useEnglish) coin.englishName else coin.koreanName

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .padding(vertical = 8.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 한글명
        Column(modifier = Modifier.weight(1f)) {

            Text(text = coinName, style = MaterialTheme.typography.bodyMedium)
            Text(text = coin.symbol, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        }

        // 현재가
        Text(
            text = df.format(coin.tradePrice),
            modifier = Modifier.weight(1f),
            color = changeColor,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.End
        )

        // 전일대비
        Text(
            text = String.format("%.2f%%", coin.changeRate!! * 100),
            modifier = Modifier.weight(1f),
            color = changeColor,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.End
        )

        // 거래대금
        Text(
            text = String.format("%,.0f백만", (coin.volume?.div(1_000_000))),
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.End
        )
    }
    Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f), thickness = 0.5.dp)
}
