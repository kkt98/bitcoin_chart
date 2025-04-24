import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.github.mikephil.charting.charts.CandleStickChart
import com.github.mikephil.charting.charts.CombinedChart
import com.github.mikephil.charting.charts.CombinedChart.DrawOrder
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.kkt981019.bitcoin_chart.network.Data.RetrofitCandleResponse
import com.kkt981019.bitcoin_chart.viewmodel.CoinDtChartViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChartSection(
    symbol: String,
    modifier: Modifier = Modifier,
    viewModel: CoinDtChartViewModel = hiltViewModel()
) {
    val minuteCandles by viewModel.minuteCandleState.observeAsState(emptyList())
    val dayCandles    by viewModel.dayCandleState.observeAsState(emptyList())

    var selectedIndex by remember { mutableStateOf(0) }
    val tabs = listOf("1m","3m","5m","15m","30m","1h","4h","24h")

    // 화면 진입·탭 변경 시 데이터 불러오기
    LaunchedEffect(symbol, selectedIndex) {
        viewModel.fetchCandles(symbol, selectedIndex)
    }

    Column(modifier = modifier.fillMaxWidth()) {
        // ─── 탭
        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            tabs.forEachIndexed { idx, title ->
                val isSel = idx == selectedIndex
                Box(
                    modifier = Modifier
                        .border(
                            width = 1.dp,
                            color = if (isSel) MaterialTheme.colorScheme.primary else Color.LightGray,
                            shape = RoundedCornerShape(4.dp)
                        )
                        .clickable { selectedIndex = idx }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = title,
                        color = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        // ─── 차트
        val data = if (selectedIndex in 0..6) minuteCandles else dayCandles
        CombinedCandleVolumeChart(data = data, modifier = Modifier
            .fillMaxSize()
        )
    }
}

@Composable
fun CombinedCandleVolumeChart(
    data: List<RetrofitCandleResponse>,
    modifier: Modifier = Modifier
) {
    AndroidView(
        factory = { ctx ->
            CandleStickChart(ctx).apply {
                description.isEnabled = false
                setDrawGridBackground(false)
                setPinchZoom(true)
                axisLeft.isEnabled         = false
                axisRight.isEnabled        = true
                xAxis.position = XAxis.XAxisPosition.BOTTOM
                legend.isEnabled = false
            }
        },
        update = { chart ->
            if (data.isEmpty()) {
                chart.clear()
                return@AndroidView
            }

            // 1) 데이터 순서 뒤집기 (오래된 순 -> 최신 순)
            val sortedData = data.asReversed()
//            "0.9"
            // 2) X축 라벨 준비
            val labels = sortedData.map { c ->
                when (c.unit) {
                    1, 3, 5, 15, 30, 60, 240 ->
                        c.candleDateTimeKst.substring(11, 16)  // 분봉
                    else ->
                        c.candleDateTimeKst.substring(0, 10)   // 일봉
                }
            }
            chart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)

            // 3) 캔들 엔트리 생성
            val candleEntries = sortedData.mapIndexed { i, c ->
                CandleEntry(
                    i.toFloat(),
                    c.highPrice.toFloat(),
                    c.lowPrice.toFloat(),
                    c.openingPrice.toFloat(),
                    c.tradePrice.toFloat()   // 종가
                )
            }
            val candleSet = CandleDataSet(candleEntries, "OHLC").apply {
                decreasingColor          = android.graphics.Color.BLUE
                increasingColor          = android.graphics.Color.RED
                shadowColorSameAsCandle  = true
                axisDependency           = YAxis.AxisDependency.RIGHT
                setDrawValues(false)
            }

            // 4) 차트에 데이터 적용
            chart.data = CandleData(candleSet)
            chart.invalidate()
        },
        modifier = modifier
    )
}
