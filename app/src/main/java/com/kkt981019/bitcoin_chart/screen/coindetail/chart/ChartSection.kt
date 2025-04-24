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
                axisRight.isEnabled = false
                xAxis.position = XAxis.XAxisPosition.BOTTOM
                legend.isEnabled = false
            }
        },
        update = { chart ->
            if (data.isEmpty()) {
                chart.clear()
                return@AndroidView
            }

            // 1) X축 레이블 (예: "02:26")
            val labels = data.map { it.candleDateTimeKst.substring(11, 16) }
            chart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)

            // 2) 캔들 엔트리
            val candleEntries = data.mapIndexed { i, c ->
                CandleEntry(
                    i.toFloat(),
                    c.highPrice.toFloat(),
                    c.lowPrice.toFloat(),
                    c.openingPrice.toFloat(),
                    c.tradePrice.toFloat()   // tradePrice가 종가입니다
                )
            }
            val candleSet = CandleDataSet(candleEntries, "OHLC").apply {
                decreasingColor = android.graphics.Color.BLUE
                increasingColor = android.graphics.Color.RED
                shadowColorSameAsCandle = true
                axisDependency = YAxis.AxisDependency.LEFT
                setDrawValues(false)
            }

            // 3) 볼륨 바 엔트리
            val barEntries = data.mapIndexed { i, c ->
                BarEntry(i.toFloat(), c.candleAccTradeVolume.toFloat())
            }
//            val barSet = BarDataSet(barEntries, "Volume").apply {
//                color = android.graphics.Color.DKGRAY
//                axisDependency = YAxis.AxisDependency.RIGHT
//                setDrawValues(false)
//            }

            // 4) CombinedData에 합치기
            chart.data = CandleData(candleSet)
            chart.invalidate()


            chart.invalidate()
        },
        modifier = modifier
    )
}
