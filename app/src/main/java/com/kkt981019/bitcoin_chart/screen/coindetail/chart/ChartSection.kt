import android.graphics.Paint
import android.util.Log
import android.view.MotionEvent
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
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.listener.ChartTouchListener
import com.kkt981019.bitcoin_chart.viewmodel.CoinDtChartViewModel

@Composable
fun ChartSection(
    symbol: String,
    modifier: Modifier = Modifier,
    viewModel: CoinDtChartViewModel = hiltViewModel()
) {
    // <--- 초기값을 null로!
    val minuteCandles: List<CandleEntry>? by viewModel.minuteCandleState.observeAsState(null)
    val minuteLabels: List<String> by viewModel.minuteTimeLabels.observeAsState(emptyList())


    var selectedIndex by remember { mutableStateOf(0) }
    val tabs = listOf("1m", "3m", "5m", "15m", "30m", "1h", "4h")

    Log.d("candleDebug", "${ minuteCandles.toString()}")


    LaunchedEffect(symbol, selectedIndex) {
        viewModel.fetchCandles(symbol, selectedIndex)
    }

    Column(modifier.fillMaxWidth()) {
        Row(
            Modifier
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 8.dp, horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            tabs.forEachIndexed { idx, title ->
                val isSel = idx == selectedIndex
                Box(
                    Modifier
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

        key(selectedIndex) {
            // --- 데이터 로딩/없음/정상 3단계 분기 ---
            when {
                minuteCandles == null -> Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) { Text("차트 데이터를 불러오는 중...") }

                minuteCandles!!.isEmpty() -> Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) { Text("차트 데이터 없음") }

                else -> IncrementalCandleChartWithPriceBox(
                    symbol = symbol,
                    tabIndex = selectedIndex,
                    entries = minuteCandles!!,
                    xLabels = minuteLabels,
                    modifier = Modifier.fillMaxSize(),
                    viewModel = viewModel
                )
            }
        }
    }
}

@Composable
fun IncrementalCandleChartWithPriceBox(
    symbol: String,
    tabIndex: Int,
    entries: List<CandleEntry>,
    xLabels: List<String>,
    modifier: Modifier = Modifier,
    viewModel: CoinDtChartViewModel
) {
    val chartRef = remember { mutableStateOf<CandleStickChart?>(null) }
    var addedCount by remember { mutableStateOf(0) }

    // 데이터 추가 후 차트 위치 보정
    LaunchedEffect(addedCount) {
        if (addedCount > 0) {
            chartRef.value?.moveViewToX(addedCount.toFloat())
            addedCount = 0
        }
    }

    var lastClose by remember { mutableStateOf(0f) }
    var lastOpen by remember { mutableStateOf(0f) }
    var boxOffsetY by remember { mutableStateOf<Float?>(null) }
    var rightTextPx by remember { mutableStateOf<Float?>(null) }

    val boxColor = when {
        lastClose > lastOpen -> Color.Red
        lastClose < lastOpen -> Color.Blue
        else -> Color.Gray
    }


    Box(modifier = modifier) {
        IncrementalCandleChart(
            entries = entries,
            xLabels = xLabels,
            modifier = Modifier.fillMaxSize(),
            onLastVisibleClose = { close, y, open ->
                lastClose = close
                lastOpen = open
                boxOffsetY = y
            },
            onAxisRightTextSizePx = { px -> rightTextPx = px },
            onReachedStart = {
                viewModel.fetchPreviousCandles(symbol, tabIndex) { added ->
                    addedCount = added
                }
            },
            chartRef = chartRef
        )

        if (entries.isNotEmpty() && boxOffsetY != null && rightTextPx != null) {
            val density = LocalDensity.current
            val yDp = with(density) { boxOffsetY!!.toDp() }
            val fontSp = pxToSp(rightTextPx!!)

            Box(
                Modifier
                    .align(Alignment.TopEnd)
                    .offset(y = yDp - 14.dp)
                    .background(boxColor)
            ) {
                Text(
                    text = "  ${"%,.0f".format(lastClose)}  ",
                    color = Color.White,
                    fontSize = fontSp.sp,
                    modifier = Modifier.padding(vertical = 1.dp)
                )
            }
        }
    }
}

@Composable
fun IncrementalCandleChart(
    entries: List<CandleEntry>,
    xLabels: List<String>,
    modifier: Modifier = Modifier,
    onAxisRightTextSizePx: ((Float) -> Unit)? = null,
    onLastVisibleClose: ((Float, Float, Float) -> Unit)? = null,
    onReachedStart: (() -> Unit)? = null,
    chartRef: MutableState<CandleStickChart?>
) {
    val dataSet = remember {
        CandleDataSet(mutableListOf(), "OHLC").apply {
            decreasingColor = android.graphics.Color.BLUE
            setDecreasingPaintStyle(Paint.Style.FILL)
            increasingColor = android.graphics.Color.RED
            setIncreasingPaintStyle(Paint.Style.FILL)
            setDrawValues(false)
            shadowColorSameAsCandle = true
            neutralColor = android.graphics.Color.GRAY
            axisDependency = YAxis.AxisDependency.RIGHT
        }
    }
    val candleData = remember { CandleData(dataSet) }
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
//                viewPortHandler.setMaximumScaleX(3f)
//                viewPortHandler.setMinimumScaleX(1f)
                xAxis.position = XAxis.XAxisPosition.BOTTOM
                axisLeft.isEnabled = false
                axisRight.isEnabled = true
                legend.isEnabled = false
//                setVisibleXRangeMaximum(30f)  // 한 화면에 30봉 고정
//                setVisibleXRangeMinimum(10f)
                this.data = candleData

                chartRef.value = this
            }
        },
        update = { chart ->
            chartRef.value = chart
            val OVERSCROLL_BUFFER = 5
            if (entries.isNotEmpty()) {
                chart.xAxis.axisMinimum = -OVERSCROLL_BUFFER.toFloat()
                dataSet.setValues(entries)
            }

            chart.setOnChartGestureListener(object : com.github.mikephil.charting.listener.OnChartGestureListener {
                override fun onChartScale(me: MotionEvent?, sx: Float, sy: Float) = postUpdate()
                override fun onChartTranslate(me: MotionEvent?, dx: Float, dy: Float) = postUpdate()
                override fun onChartGestureEnd(me: MotionEvent?, last: ChartTouchListener.ChartGesture?) {
                    val isOverScroll = chart.lowestVisibleX <= 2f
                    if (isOverScroll) {
                        onReachedStart?.invoke()
                    }
                    postUpdate()
                }
                override fun onChartGestureStart(me: MotionEvent?, last: ChartTouchListener.ChartGesture?) {}
                override fun onChartLongPressed(me: MotionEvent?) {}
                override fun onChartDoubleTapped(me: MotionEvent?) {}
                override fun onChartSingleTapped(me: MotionEvent?) {}
                override fun onChartFling(me1: MotionEvent?, me2: MotionEvent?, vx: Float, vy: Float) {}

                private fun postUpdate() {
                    chart.post {
                        chart.data?.let { cd ->
                            val set = cd.getDataSetByIndex(0) as CandleDataSet
                            val idx = chart.highestVisibleX.toInt().coerceAtMost(set.entryCount - 1)
                            val e = set.getEntryForIndex(idx)
                            val pt = chart.getTransformer(YAxis.AxisDependency.RIGHT)
                                .getPixelForValues(0f, e.close)
                            onLastVisibleClose?.invoke(e.close, pt.y.toFloat(), e.open)
                        }
                    }
                }
                private fun postCheckStart() {
                    chart.post {
                        if (chart.lowestVisibleX <= 0f) {
                            onReachedStart?.invoke()
                        }
                    }
                }
            })

            if (entries.isEmpty()) {
                chart.clear()
            } else {
                dataSet.setValues(entries)
            }

            if (firstZoom.value && entries.isNotEmpty()) {
                chart.zoom(2f, 1f, entries.last().x, 0f)
                chart.moveViewToX(entries.last().x)
                firstZoom.value = false
            }

            chart.axisRight.valueFormatter = object : com.github.mikephil.charting.formatter.ValueFormatter() {
                override fun getFormattedValue(v: Float) = String.format("%,.0f", v)
            }
            onAxisRightTextSizePx?.invoke(chart.axisRight.textSize)

            chart.post {
                chart.data?.let { cd2 ->
                    val set2 = cd2.getDataSetByIndex(0) as CandleDataSet
                    val idx = chart.highestVisibleX.toInt().coerceAtMost(set2.entryCount - 1)
                    val e = set2.getEntryForIndex(idx)
                    val pt = chart.getTransformer(YAxis.AxisDependency.RIGHT)
                        .getPixelForValues(0f, e.close)
                    onLastVisibleClose?.invoke(e.close, pt.y.toFloat(), e.open)
                }
            }

            candleData.notifyDataChanged()
            chart.notifyDataSetChanged()
            chart.invalidate()
        },
        modifier = modifier
    )
}

@Composable
fun pxToSp(px: Float): Float {
    val density = LocalDensity.current
    return px / density.density
}