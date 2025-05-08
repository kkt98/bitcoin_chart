package com.kkt981019.bitcoin_chart.screen.coindetail.trade

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.kkt981019.bitcoin_chart.network.Data.WebSocketCandleResponse

// 2) TradeSection 은 단일 TradeResponse 가 아닌 리스트를 받도록!
@Composable
fun TradeSection(
    symbol: String,
    dayCandle: List<WebSocketCandleResponse>,
    color: Color
) {
    var selectedTab by remember { mutableStateOf(0) }
    var changeVolume by remember { mutableStateOf(false) }

    val tabs = listOf("체결", "일별")
    val corner = 4.dp

    Surface(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(),
        color = Color.Transparent,
        shape = RoundedCornerShape(corner),
        border = BorderStroke(1.dp, Color.Gray)
    ) {
        TabRow(
            selectedTabIndex = selectedTab,
            indicator = {},
            divider = {}
        ) {
            tabs.forEachIndexed { idx, title ->
                Tab(
                    selected = selectedTab == idx,
                    onClick = { selectedTab = idx },
                    modifier = Modifier
                        .height(36.dp)
                        .background(
                            if (selectedTab == idx) Color(0xFF2979FF) else Color.Transparent,
                            RoundedCornerShape(corner)
                        ),
                    text = {
                        Text(
                            text = title,
                            color = if (selectedTab == idx) Color.White else Color.Black
                        )
                    }
                )
            }
        }
    }
    when (selectedTab) {
        0 -> TradeList(symbol, color, changeVolume, onChangeVolume = {changeVolume = !changeVolume})
        1 -> DailyList(symbol)
    }
}