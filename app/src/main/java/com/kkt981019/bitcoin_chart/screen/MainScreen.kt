package com.kkt981019.bitcoin_chart.screen

import androidx.compose.foundation.background
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
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    // Material3 테마의 컬러
    val backgroundColor = MaterialTheme.colorScheme.background
    val surfaceColor = MaterialTheme.colorScheme.surface

    // 검색어 상태
    var searchQuery by remember { mutableStateOf("") }

    // 탭 상태
    val tabTitles = listOf("KRW", "BTC", "USDT")
    var selectedTabIndex by remember { mutableStateOf(0) }

    // 코인 리스트 예시 데이터 (하한가를 추가)
    val coinList = listOf(
        CoinData("비트코인", "BTC/USDT", currentPrice = 28753.25, lowPrice = 28000.0, changeRate = 0.87, volume = 261998602.0),
        CoinData("리플",    "XRP/USDT", currentPrice =     0.48, lowPrice =     0.45, changeRate = 3.12, volume =     119056.0),
        CoinData("솔라나",  "SOL/USDT", currentPrice =    22.15, lowPrice =    21.90, changeRate = -1.23, volume =     75123.0)
        // 필요에 따라 데이터 추가
    )

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
                onQueryChange = { searchQuery = it },
                backgroundColor = Color.White
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
            CoinListHeader()

            // 코인 리스트
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(coinList) { coin ->
                    CoinItemRow(coin, backgroundColor = backgroundColor)
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
    backgroundColor: Color
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

/** 열(컬럼) 헤더: "한글명 / 하한가 / 전일대비 / 거래대금" **/
@Composable
fun CoinListHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = "한글명",
            modifier = Modifier.weight(1f),
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

/** 코인 정보 데이터 모델 (하한가 추가) **/
data class CoinData(
    val name: String,        // 한글명
    val symbol: String,      // 예: BTC/USDT
    val currentPrice: Double,
    val lowPrice: Double,    // 하한가
    val changeRate: Double,  // 전일대비(%, +면 상승, -면 하락)
    val volume: Double       // 거래대금
)

/** 코인 리스트 아이템 **/
@Composable
fun CoinItemRow(coin: CoinData, backgroundColor: Color) {
    // 전일대비(%)가 +면 빨간색, -면 파란색
    val changeColor = if (coin.changeRate >= 0) Color.Red else Color.Blue

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .padding(vertical = 8.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 한글명
        Column(modifier = Modifier.weight(1f)) {
            Text(text = coin.name, style = MaterialTheme.typography.bodyMedium)
            Text(text = coin.symbol, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        }

        // 현재가
        Text(
            text = String.format("%,.2f", coin.lowPrice),
            modifier = Modifier.weight(1f),
            color = changeColor,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.End
        )

        // 전일대비
        Text(
            text = String.format("%.2f%%", coin.changeRate),
            modifier = Modifier.weight(1f),
            color = changeColor,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.End
        )

        // 거래대금
        Text(
            text = String.format("%,.0f", coin.volume),
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.End
        )
    }
    Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f), thickness = 0.5.dp)
}
