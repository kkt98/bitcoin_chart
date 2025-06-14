import android.graphics.Paint
import android.view.MotionEvent
import android.view.View
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.github.mikephil.charting.charts.CandleStickChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.CandleData
import com.github.mikephil.charting.data.CandleDataSet
import com.github.mikephil.charting.data.CandleEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.listener.ChartTouchListener
import com.github.mikephil.charting.listener.OnChartGestureListener
import com.kkt981019.bitcoin_chart.viewmodel.CoinDtChartViewModel

@Composable
fun ChartSection(
    symbol: String,
    modifier: Modifier = Modifier,
    viewModel: CoinDtChartViewModel = hiltViewModel()
) {
    val minuteCandles by viewModel.minuteCandleState.observeAsState()
    val minuteLabels by viewModel.minuteTimeLabels.observeAsState(emptyList())

    var selectedIndex by remember { mutableStateOf(0) }
    val tabs = listOf("1분", "3분", "5분", "15분", "30분", "1시간", "4시간", "1일")

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
                        fontSize = 12.sp,
                        color = if (isSel) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

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
    var lastLowestVisibleX by remember { mutableStateOf(0f) }
    var lastVisibleRange by remember { mutableStateOf(0f) }
    var isLoadingPrev by remember { mutableStateOf(false) }

    LaunchedEffect(addedCount) {
        if (addedCount > 0) {
            chartRef.value?.apply {
                moveViewToX(lastLowestVisibleX + addedCount)
                setVisibleXRange(lastVisibleRange, lastVisibleRange)
            }
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
            tabIndex = tabIndex,
            modifier = Modifier.fillMaxSize(),
            onLastVisibleClose = { close, y, open ->
                lastClose = close
                lastOpen = open
                boxOffsetY = y
            },
            onAxisRightTextSizePx = { px -> rightTextPx = px },
            onReachedStart = {
                chartRef.value?.let { chart ->
                    isLoadingPrev = true
                    lastLowestVisibleX = chart.lowestVisibleX
                    lastVisibleRange = chart.visibleXRange
                    viewModel.fetchPreviousCandles(symbol, tabIndex) { added ->
                        addedCount = added
                        isLoadingPrev = false
                    }
                }
            },
            chartRef = chartRef
        )

        // 가격 박스
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

        LaunchedEffect(tabIndex, entries.size) {
            chartRef.value?.let { chart ->
                if (entries.isNotEmpty()) {
                    val lastIdx = entries.last().x.toInt()  // 또는 entries.size - 1
                    val curRightIdx = chart.highestVisibleX.toInt()

                    // → 이미 마지막 봉을 보고 있을 때만 이동
                    if (curRightIdx == lastIdx) {
                        chart.moveViewToX(entries.last().x)
                    }
                }
            }
        }

        // 이전 데이터 로딩 스피너
        if (isLoadingPrev) {
            Box(
                Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.CenterStart
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
        }
    }
}

@Composable
fun IncrementalCandleChart(
    entries: List<CandleEntry>,
    xLabels: List<String>,
    tabIndex: Int,
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
            neutralColor = android.graphics.Color.GRAY
            shadowColorSameAsCandle = true
            setDrawValues(false)
            axisDependency = YAxis.AxisDependency.RIGHT
        }
    }
    val candleData = remember { CandleData(dataSet) }
    var firstZoom by remember(tabIndex) { mutableStateOf(true) }

    AndroidView(
        factory = { ctx ->
            CandleStickChart(ctx).apply {
                overScrollMode = View.OVER_SCROLL_NEVER
                description.isEnabled = false
                setDrawGridBackground(false)
                setPinchZoom(true)
                setScaleEnabled(true)
                setScaleXEnabled(true)
                setScaleYEnabled(false)
                setDoubleTapToZoomEnabled(true)
                setDragDecelerationEnabled(false)
                isDragEnabled = true
                setDragOffsetX(20f)
                isAutoScaleMinMaxEnabled = true


                xAxis.apply {
                    position = XAxis.XAxisPosition.BOTTOM
                    granularity = 1f
                    setGranularityEnabled(true)
                    setAvoidFirstLastClipping(true)
                }

                axisLeft.isEnabled = false
                axisRight.apply {
                    isEnabled = true
                    valueFormatter = object : ValueFormatter() {
                        override fun getFormattedValue(value: Float) = String.format("%,.0f", value)
                    }
                    textSize.let { onAxisRightTextSizePx?.invoke(it) }
                }

                setOnChartGestureListener(object : OnChartGestureListener {
                    override fun onChartScale(me: MotionEvent?, scaleX: Float, scaleY: Float) = postUpdate()
                    override fun onChartTranslate(me: MotionEvent?, dX: Float, dY: Float) = postUpdate()
                    override fun onChartGestureEnd(me: MotionEvent?, last: ChartTouchListener.ChartGesture?) {
                        if (lowestVisibleX <= 2f) onReachedStart?.invoke()
                        postUpdate()
                    }
                    override fun onChartGestureStart(me: MotionEvent?, last: ChartTouchListener.ChartGesture?) {}
                    override fun onChartLongPressed(me: MotionEvent?) {}
                    override fun onChartDoubleTapped(me: MotionEvent?) {}
                    override fun onChartSingleTapped(me: MotionEvent?) {}
                    override fun onChartFling(me1: MotionEvent?, me2: MotionEvent?, velocityX: Float, velocityY: Float) {}

                    private fun postUpdate() {
                        post {
                            data?.let { cd ->
                                val set = cd.getDataSetByIndex(0) as CandleDataSet
                                val idx = highestVisibleX.toInt().coerceAtMost(set.entryCount - 1)
                                val e = set.getEntryForIndex(idx)
                                val pt = getTransformer(YAxis.AxisDependency.RIGHT)
                                    .getPixelForValues(0f, e.close)
                                onLastVisibleClose?.invoke(e.close, pt.y.toFloat(), e.open)
                            }
                        }
                    }
                })

                data = candleData
                chartRef.value = this
            }
        },
        update = { chart ->
            val curLowest = chart.lowestVisibleX

            dataSet.values = entries
            chart.data.notifyDataChanged()
            chart.notifyDataSetChanged()

            chart.xAxis.apply {
                valueFormatter = IndexAxisValueFormatter(xLabels)
                setLabelCount(minOf(xLabels.size, 6), false)
            }

            if (firstZoom && entries.isNotEmpty()) {
                chart.setVisibleXRangeMinimum(30f)
                chart.moveViewToX(entries.last().x)
                firstZoom = false
            }

            chart.setVisibleXRangeMinimum(10f)
            chart.setVisibleXRangeMaximum(200f)

            chart.moveViewToX(curLowest)

            chart.invalidate()

            chart.post {
                if (chart.data != null) {
                    val set = chart.data.getDataSetByIndex(0) as CandleDataSet
                    if (set.entryCount > 0) {
                        val lastIdx = set.entryCount - 1

                        // ★ “현재 보이는 가장 오른쪽 봉 인덱스”가 마지막 봉 인덱스일 때만
                        if (chart.highestVisibleX.toInt() == lastIdx) {
                            val lastEntry = set.getEntryForIndex(lastIdx)
                            val pt = chart.getTransformer(YAxis.AxisDependency.RIGHT)
                                .getPixelForValues(0f, lastEntry.close)
                            onLastVisibleClose?.invoke(
                                lastEntry.close,
                                pt.y.toFloat(),
                                lastEntry.open
                            )
                        }
                    }
                }
            }
        },
        modifier = modifier
    )
}

@Composable
fun pxToSp(px: Float): Float {
    val density = LocalDensity.current
    val spValue: TextUnit = with(density) { px.toSp() }
    return spValue.value
}
