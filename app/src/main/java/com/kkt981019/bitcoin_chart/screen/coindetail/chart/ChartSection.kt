import android.graphics.Paint
import android.util.Log
import androidx.compose.foundation.background
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.github.mikephil.charting.charts.CandleStickChart
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.kkt981019.bitcoin_chart.viewmodel.CoinDtChartViewModel

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
                IncrementalCandleChartWithPriceBox(
                    entries = minuteCandles,
                    xLabels = minuteLabels,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}

@Composable
fun IncrementalCandleChartWithPriceBox(
    entries: List<CandleEntry>,
    modifier: Modifier = Modifier,
    xLabels: List<String>
) {
    val lastCandle = entries.lastOrNull()
    val lastClose = lastCandle?.close ?: 0f
    val lastOpen  = lastCandle?.open  ?: 0f
    var priceBoxOffsetY by remember { mutableStateOf<Float?>(null) }
    var axisRightTextSizePx by remember { mutableStateOf<Float?>(null) }

    // 봉 색 결정
    val priceBoxColor = when {
        lastClose > lastOpen -> Color.Red   // 양봉
        lastClose < lastOpen -> Color.Blue  // 음봉
        else                 -> Color.Gray  // 도지
    }

    Box(modifier = modifier) {
        IncrementalCandleChart(
            entries = entries,
            xLabels = xLabels,
            modifier = Modifier.fillMaxSize(),
            onCurrentPriceYPx = { yPx -> priceBoxOffsetY = yPx },
            onAxisRightTextSizePx = { px -> axisRightTextSizePx = px }
        )

        if (entries.isNotEmpty() && priceBoxOffsetY != null && axisRightTextSizePx != null) {
            val density = LocalDensity.current
            val yDp = with(density) { priceBoxOffsetY!!.toDp() }
            val fontSizeSp = pxToSp(axisRightTextSizePx!!)

            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(y = yDp - 14.dp)
                    .background(
                        color = priceBoxColor,
                    )
            ) {
                Text(
                    text = "  ${String.format("%,.0f", lastClose)}  ",
                    color = Color.White,
                    fontSize = fontSizeSp.sp,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
fun IncrementalCandleChart(
    entries: List<CandleEntry>,
    modifier: Modifier = Modifier,
    xLabels: List<String>,
    onCurrentPriceYPx: ((Float) -> Unit)? = null, // y픽셀값 콜백
    onAxisRightTextSizePx: ((Float) -> Unit)? = null,
) {
    // 1) CandleDataSet 한 번만 생성
    val candleDataSet = remember {
        CandleDataSet(mutableListOf(), "OHLC").apply {
            // 음봉
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
                description.isEnabled = false
                setDrawGridBackground(false)
                setPinchZoom(true)
                isDragEnabled = true
                setScaleEnabled(true)
                setScaleXEnabled(true)
                setScaleYEnabled(false)
                viewPortHandler.setMaximumScaleX(3f)
                viewPortHandler.setMinimumScaleX(1f)
                xAxis.position     = XAxis.XAxisPosition.BOTTOM
                axisLeft.isEnabled  = false
                axisRight.isEnabled = true
                legend.isEnabled    = false
                setVisibleXRangeMaximum(30f)
                setVisibleXRangeMinimum(10f)
                data = candleData
                invalidate()
            }
        },
        update = { chart ->
            if (chart.data == null) {
                chart.data = candleData
            }
            if (entries.isEmpty()) {
                chart.clear()
                return@AndroidView
            }

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
                valueFormatter = IndexAxisValueFormatter(xLabels)
            }
            chart.xAxis.axisMinimum = 0f
            chart.xAxis.axisMaximum = newSize.toFloat()

            if (firstZoom.value) {
                chart.zoom(4f, 1.5f, entries.last().x, 0f)
                chart.moveViewToX(entries.last().x)

                // 화면에 보이는 index 구하기
                val minX = chart.lowestVisibleX.toInt().coerceAtLeast(0)
                val maxX = chart.highestVisibleX.toInt().coerceAtMost(entries.lastIndex)

//                if (entries.isNotEmpty() && minX <= maxX) {
//                    val visibleEntries = entries.subList(minX, maxX + 1)
//                    val maxVisibleHigh = visibleEntries.maxOf { it.high }
//                    chart.axisRight.axisMaximum = maxVisibleHigh      // 최고가로 맞춤 (트레이딩뷰와 동일)
//                }

                firstZoom.value = false
            }

            // axisRight 가격 포맷
            chart.axisRight.valueFormatter = object : com.github.mikephil.charting.formatter.ValueFormatter() {
                override fun getFormattedValue(value: Float): String =
                    String.format("%,.0f", value)
            }

            onAxisRightTextSizePx?.invoke(chart.axisRight.textSize)

            // 현재가 y픽셀값을 Compose로 전달
            if (entries.isNotEmpty() && onCurrentPriceYPx != null) {
                chart.post {
                    val lastClose = entries.lastOrNull()?.close ?: 0f
                    val mpPoint = chart.getTransformer(YAxis.AxisDependency.RIGHT)
                        .getPixelForValues(0f, lastClose)
                    onCurrentPriceYPx(mpPoint.y.toFloat())
                }
            }

            chart.data.notifyDataChanged()
            chart.notifyDataSetChanged()
            chart.invalidate()
        },
        modifier = modifier
    )
}

@Composable
fun pxToSp(px: Float): Float {
    val density = LocalDensity.current
    return with(density) { px / density.density }
}