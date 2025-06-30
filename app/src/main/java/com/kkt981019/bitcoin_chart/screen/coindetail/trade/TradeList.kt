package com.kkt981019.bitcoin_chart.screen.coindetail.trade

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kkt981019.bitcoin_chart.R
import com.kkt981019.bitcoin_chart.util.DecimalFormat
import com.kkt981019.bitcoin_chart.viewmodel.CoinDtTradeViewModel
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlin.text.substringAfter
import kotlin.text.substringBefore

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TradeList(
    symbol: String,
    color: Color,
    changeVolume: Boolean,
    onChangeVolume: () -> Unit,
    viewModel: CoinDtTradeViewModel = hiltViewModel()
) {
    LaunchedEffect(symbol) {
        viewModel.startTrade(symbol)
    }

    val trades by viewModel.tradeState.observeAsState(emptyList())

    if (trades.isEmpty()) {
        return
    }

    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
    // UTC 문자열 → KST 문자열 변환
    fun String.utcToKST(): String = runCatching {
        val utcTime = LocalTime.parse(this, timeFormatter)
        val kstTime = utcTime.plusHours(9)    // UTC+9
        kstTime.format(timeFormatter)
    }.getOrElse { this }

    // 2) 안전하게 첫 요소에 접근
    val code = trades.first().code
    val coinName = code.substringAfter('-') // ex: "BTC"
    val moneyName = code.substringBefore('-') // ex: "KRW"
    val format = DecimalFormat.getTradeFormatters(moneyName)

    LazyColumn(modifier = Modifier.fillMaxWidth()) {
        stickyHeader {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
                    .background(Color.White)
                    .border(1.dp, color = Color.LightGray)
            ) {
                // 1열
                Text(
                    "체결시간",
                    Modifier
                        .weight(0.5f)
                        .fillMaxHeight()           // Divider가 헤더 높이에 꽉 차게
                        .padding(8.dp),
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
                        .padding( 8.dp),
                    textAlign = TextAlign.Center
                )
                Divider(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(1.dp),
                    color = Color.LightGray
                )
                // 3열
                Row(modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(8.dp)
                    .clickable { (onChangeVolume()) },
                    verticalAlignment   = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center)
                {
                    Text(
                        text = if (changeVolume) "체결량($coinName)" else "체결액($moneyName)",
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.width(4.dp))
                    Icon(
                        painter = painterResource(R.drawable.exchange),
                        contentDescription = null,
                        modifier = Modifier.size(10.dp),
                    )
                }
            }
            Divider() // 헤더 아래 가로줄
        }

        items(trades) { t ->

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),   // 개별 row 높이에도 적용
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 체결시간
                Text(
                    text =  remember(t.tradeTime) { t.tradeTime.utcToKST() },
                    modifier = Modifier
                        .weight(0.5f)
                        .fillMaxHeight()
                        .padding(10.dp),
                    textAlign = TextAlign.Center,
                    fontSize = 13.sp
                )
                Divider(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(1.dp),
                    color = Color.LightGray
                )

//
                // 체결가격
                Text(
                    text = format.priceDf.format(t.tradePrice),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(10.dp),
                    textAlign = TextAlign.End,
                    color = color,
                    fontSize = 13.sp
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
                    text = if (changeVolume) format.volumeDf.format(t.tradeVolume) else format.amountDf.format(amount),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(10.dp),
                    textAlign = TextAlign.End,
                    color = if (t.askBid == "ASK") Color.Blue else Color.Red,
                    fontSize = 13.sp
                )
            }
            Divider() // 각 row 아래 가로줄
        }
    }
}