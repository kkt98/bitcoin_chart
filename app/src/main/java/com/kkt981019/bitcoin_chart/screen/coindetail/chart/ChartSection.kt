import android.graphics.Paint
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
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.kkt981019.bitcoin_chart.viewmodel.CoinDtChartViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChartSection(
    symbol: String,
    modifier: Modifier = Modifier,
    viewModel: CoinDtChartViewModel = hiltViewModel()
) {
    val minuteCandles by viewModel.minuteCandleState.observeAsState(emptyList())
    val minuteLabels  by viewModel.minuteTimeLabels.observeAsState(emptyList())

    var selectedIndex by remember { mutableStateOf(0) }
    val tabs = listOf("1m","3m","5m","15m","30m","1h","4h")

    LaunchedEffect(symbol, selectedIndex) {
        viewModel.fetchCandles(symbol, selectedIndex)
    }

    Column(modifier.fillMaxWidth()) {
        // ── 탭 바 ──
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
                            1.dp,
                            if (isSel) MaterialTheme.colorScheme.primary else Color.LightGray,
                            RoundedCornerShape(4.dp)
                        )
                        .clickable { selectedIndex = idx }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = title,
                        color = if (isSel) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        if (selectedIndex in 0..6) {
            // 분봉 차트: 탭 인덱스가 바뀌면 완전히 재마운트되어 firstZoom 초기화
            key(selectedIndex) {
                IncrementalCandleChart(
                    entries = minuteCandles,
                    xLabels = minuteLabels,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
fun IncrementalCandleChart(
    entries: List<CandleEntry>,
    modifier: Modifier = Modifier,
    xLabels: List<String>
) {
    var open = 0.0F
    var close = 0.0F

    entries.forEach { candle ->
        Log.d("Candle", "open=${candle.open}, close=${candle.close}")
        open = candle.open
        close = candle.close
    }

    // 1) CandleDataSet 한 번만 생성
    val candleDataSet = remember {
        CandleDataSet(mutableListOf(), "OHLC").apply {
            // 음봄
            decreasingColor         = android.graphics.Color.BLUE
            setDecreasingPaintStyle(Paint.Style.FILL)
            // 양봉
            increasingColor         = android.graphics.Color.RED
            setIncreasingPaintStyle(Paint.Style.FILL)

            setDrawValues(false)
            shadowColorSameAsCandle = true
            neutralColor = android.graphics.Color.GRAY
            axisDependency = YAxis.AxisDependency.RIGHT
        }
    }
    // 2) CandleData 한 번만 생성
    val candleData = remember { CandleData(candleDataSet) }
    val firstZoom = remember { mutableStateOf(true) }

    AndroidView(
        factory = { ctx ->
            CandleStickChart(ctx).apply {
                // ── 공통 차트 설정 ──
                description.isEnabled       = false
                setDrawGridBackground(false)
                setPinchZoom(true)
                isDragEnabled      = true
                setScaleEnabled(true)
                setScaleXEnabled(true)
                setScaleYEnabled(false)
                viewPortHandler.setMaximumScaleX(3f)
                viewPortHandler.setMinimumScaleX(1f)

                xAxis.position     = XAxis.XAxisPosition.BOTTOM
                axisLeft.isEnabled  = false
                axisRight.isEnabled = true
                legend.isEnabled    = false
                setVisibleXRangeMaximum(50f)
                setVisibleXRangeMinimum(10f)

                // 최초 한 번 데이터 바인딩
                data = candleData
                invalidate()
            }
        },
        update = { chart ->
            // ① data가 null이면 재할당
            if (chart.data == null) {
                chart.data = candleData
            }
            // ② entries가 비어 있으면 clear하고 리턴
            if (entries.isEmpty()) {
                chart.clear()
                return@AndroidView
            }

            // ③ 증분 업데이트 로직
            val set = chart.data.getDataSetByIndex(0) as CandleDataSet
            val oldSize = set.entryCount
            val newSize = entries.size

            when {
                newSize > oldSize -> {
                    for (i in oldSize until newSize) {
                        set.addEntry(entries[i])
                    }
                }
                newSize == oldSize -> {
                    set.removeLast()
                    set.addEntry(entries.last())
                }
            }

            chart.xAxis.apply {
                valueFormatter     = IndexAxisValueFormatter(xLabels)
            }

            // ④ X축 범위 갱신
            chart.xAxis.axisMinimum = 0f
            chart.xAxis.axisMaximum = newSize.toFloat()

            if (firstZoom.value) {
                // 2배 확대하고, 마지막 봉이 보이도록 이동
                chart.zoom(4f, 1f, entries.last().x, 0f)
                chart.moveViewToX(entries.last().x)
                firstZoom.value = false
            }

            // ⑤ 변경 알림 & 리렌더링
            chart.data.notifyDataChanged()
            chart.notifyDataSetChanged()
            chart.invalidate()
        },
        modifier = modifier
    )
}
