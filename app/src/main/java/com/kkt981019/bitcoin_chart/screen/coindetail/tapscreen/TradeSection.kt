package com.kkt981019.bitcoin_chart.screen.coindetail.tapscreen

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.kkt981019.bitcoin_chart.network.Data.TradeResponse

@Composable
fun TradeSection(trade: TradeResponse?) {

    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("체결", "일별")

    Column {
        TabRow(selectedTabIndex = selectedTab) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }

        when (selectedTab) {
            0 -> TradeList()
            1 -> DailyList()
        }
    }
}

@Composable
fun TradeList() {

}

@Composable
fun DailyList() {


}