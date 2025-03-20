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
    // 기본 테마 색상 사용
    val backgroundColor = MaterialTheme.colorScheme.background
    val surfaceColor = MaterialTheme.colorScheme.surface

    // 검색 상태
    var searchQuery by remember { mutableStateOf("") }

    // 탭 상태
    val tabTitles = listOf("KRW", "BTC", "USDT")
    var selectedTabIndex by remember { mutableStateOf(0) }

    // 코인 리스트 예시 데이터
    val coinList = listOf(
        CoinData("비트코인", "BTC/USDT", 28753.25, 0.87, 261998602.0),
        CoinData("리플", "XRP/USDT", 0.48, 3.12, 119056.0),
        CoinData("솔라나", "SOL/USDT", 22.15, -1.23, 75123.0)
        // ...
    )

    Scaffold(
        Modifier.background(backgroundColor) ,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "거래소",
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                },
                actions = {
                    // 우측에 추가 아이콘이나 버튼이 필요하다면 배치
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
                backgroundColor = surfaceColor
            )

            // 탭 영역
            TabRow(
                selectedTabIndex = selectedTabIndex,
                Modifier.background(backgroundColor),
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

@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    backgroundColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            tint = Color.Gray
        )
        Spacer(modifier = Modifier.width(8.dp))
        TextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.weight(1f),
            placeholder = {
                Text(text = "코인명/심볼 검색", color = Color.Gray)
            },
//            colors = TextFieldDefaults.colors(
//                textColor = MaterialTheme.colorScheme.onSurface,
//                backgroundColor = Color.Transparent,
//                cursorColor = MaterialTheme.colorScheme.primary,
//                focusedIndicatorColor = MaterialTheme.colorScheme.primary,
//                unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
//            )
        )
    }
}

data class CoinData(
    val name: String,
    val symbol: String,
    val currentPrice: Double,
    val changeRate: Double,  // +면 상승, -면 하락
    val volume: Double
)

@Composable
fun CoinItemRow(coin: CoinData, backgroundColor: Color) {
    // 상승/하락에 따라 텍스트 색상 구분 (예시로 상승은 빨간색, 하락은 파란색)
    val changeColor = if (coin.changeRate >= 0) Color.Red else Color.Blue

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .padding(vertical = 8.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 코인명과 심볼
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = coin.name,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = coin.symbol,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                style = MaterialTheme.typography.bodySmall
            )
        }

        // 현재가
        Text(
            text = String.format("%,.2f", coin.currentPrice),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.End
        )

        // 변동률
        Text(
            text = String.format("%.2f%%", coin.changeRate),
            color = changeColor,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.End
        )

        // 거래량
        Text(
            text = String.format("%,.0f", coin.volume),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.End
        )
    }
    Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f), thickness = 0.5.dp)
}
