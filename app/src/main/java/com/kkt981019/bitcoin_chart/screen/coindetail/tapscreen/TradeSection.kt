package com.kkt981019.bitcoin_chart.screen.coindetail.tapscreen

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
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
import com.kkt981019.bitcoin_chart.network.Data.WebSocketTradeResponse
import java.text.DecimalFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

// 2) TradeSection 은 단일 TradeResponse 가 아닌 리스트를 받도록!
@Composable
fun TradeSection(trades: List<WebSocketTradeResponse>, color: Color) {
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
    Log.d("asdasdas1234", trades.toString())
    when (selectedTab) {
        0 -> TradeList(trades, color, changeVolume, onChangeVolume = {changeVolume = !changeVolume})
        1 -> DailyList()
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TradeList(
    trades: List<WebSocketTradeResponse>,
    color: Color,
    changeVolume: Boolean,
    onChangeVolume: () -> Unit
) {

    val coinName = trades[0].code.substringAfter('-') //코인이름(영문) ex) BTC
    val moneyName = trades[0].code.substringBefore('-') //KRW or BTC or USDT

    // 체결시간이 UTC라 KST로 변경
    val changeToKoreaTime = remember {
        DateTimeFormatter.ofPattern("HH:mm:ss")
            .withZone(ZoneId.of("Asia/Seoul"))
    }

    //체결가격
    val dfPrice =  when(moneyName) {
        "KRW" -> DecimalFormat("#,##0.0#")
        "BTC" -> DecimalFormat("#,##0.00000000")
        else -> DecimalFormat("#,##0.00######")
    }

    //체결량
    val dfCode =  when(moneyName) {
        "KRW" -> DecimalFormat("#,##0.00000000")
        "BTC" -> DecimalFormat("#,##0.00000000")
        else -> DecimalFormat("#,##0.00######")
    }

    //체결액
    val dfAmount =  when(moneyName) {
        "KRW" -> DecimalFormat("#,##0")
        "BTC" -> DecimalFormat("#,##0.00000000")
        else -> DecimalFormat("#,##0.000")
    }

    LazyColumn(modifier = Modifier.fillMaxWidth()) {
        stickyHeader {
            // 헤더도 IntrinsicSize.Min 적용
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)      // 자식 높이에 맞춰서
                    .background(Color.White)
                    .border(1.dp, color = Color.LightGray)
                    .padding(vertical = 8.dp),
            ) {
                // 1열
                Text(
                    "체결시간",
                    Modifier
                        .weight(0.5f)
                        .fillMaxHeight()           // Divider가 헤더 높이에 꽉 차게
                        .padding(horizontal = 4.dp),
                    textAlign = TextAlign.Center
                )
                Divider(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(1.dp),
                    color = Color.LightGray
                )
                // 2열
                Text(
                    "체결가격($moneyName)",
                    Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(horizontal = 4.dp),
                    textAlign = TextAlign.Center
                )
                Divider(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(1.dp),
                    color = Color.LightGray
                )
                // 3열
                Text(
                    text = if (changeVolume) "체결량($coinName)" else "체결액($moneyName)",
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(horizontal = 4.dp)
                        .clickable { (onChangeVolume()) },
                    textAlign = TextAlign.Center
                )
            }
            Divider() // 헤더 아래 가로줄
        }

        items(trades) { t ->

            //한국 시간으로 변경
            val koreaTime = remember(t.tradeTimestamp) {
                changeToKoreaTime.format(Instant.ofEpochMilli(t.tradeTimestamp))
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)      // 개별 row 높이에도 적용
                    .padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 체결시간
                Text(
                    text = koreaTime,
                    modifier = Modifier
                        .weight(0.5f)
                        .fillMaxHeight()
                        .padding(8.dp),
                    textAlign = TextAlign.Center
                )
                Divider(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(1.dp),
                    color = Color.LightGray
                )
                // 체결가격
                Text(
                    text = dfPrice.format(t.tradePrice),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(8.dp),
                    textAlign = TextAlign.End,
                    color = color
                )
                Divider(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(1.dp),
                    color = Color.LightGray
                )

                val amount = t.tradePrice * t.tradeVolume
                // 체결액, 체결량
                Text(                        //체결량                            //체결액
                    text = if (changeVolume) dfCode.format(t.tradeVolume) else dfAmount.format(amount),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(8.dp),
                    textAlign = TextAlign.End,
                    color = if (t.askBid == "ASK") Color.Blue else Color.Red
                )
            }
            Divider() // 각 row 아래 가로줄
        }
    }
}

@Composable
fun DailyList() {


}