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
import android.graphics.Matrix
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.github.mikephil.charting.charts.CandleStickChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.listener.ChartTouchListener
import com.github.mikephil.charting.listener.OnChartGestureListener
import com.kkt981019.bitcoin_chart.viewmodel.CoinDtChartViewModel

@Composable
fun ChartSection(
    symbol: String,
    modifier: Modifier = Modifier,
    viewModel: CoinDtChartViewModel = hiltViewModel()
) {
    val minuteCandles: List<CandleEntry>? by viewModel.minuteCandleState.observeAsState(null)
    val minuteLabels: List<String> by viewModel.minuteTimeLabels.observeAsState(emptyList())

    var selectedIndex by remember { mutableStateOf(0) }
    val tabs = listOf("1m", "3m", "5m", "15m", "30m", "1h", "4h")

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

    // 과거 데이터 추가 전/후 화면 상태 저장용
    var lastLowestVisibleX by remember { mutableStateOf(0f) }
    var lastVisibleRange by remember { mutableStateOf(0f) }

    // 추가된 봉 수만큼 이동 및 줌 레인지 유지
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
            modifier = Modifier.fillMaxSize(),
            onLastVisibleClose = { close, y, open ->
                lastClose = close
                lastOpen = open
                boxOffsetY = y
            },
            onAxisRightTextSizePx = { px -> rightTextPx = px },
            onReachedStart = {
                chartRef.value?.let { chart ->
                    // 과거 fetch 전 현재 보이는 구간 저장
                    lastLowestVisibleX = chart.lowestVisibleX
                    lastVisibleRange = chart.visibleXRange
                    viewModel.fetchPreviousCandles(symbol, tabIndex) { added ->
                        addedCount = added
                    }
                }
            },
            chartRef = chartRef
        )

        // 종가 표시 박스
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
            neutralColor = android.graphics.Color.GRAY
            shadowColorSameAsCandle = true
            setDrawValues(false)
            axisDependency = YAxis.AxisDependency.RIGHT
        }
    }
    val candleData = remember { CandleData(dataSet) }
    var firstZoom by remember { mutableStateOf(true) }

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
                setDragOffsetX(20f)
                xAxis.position = XAxis.XAxisPosition.BOTTOM
                axisLeft.isEnabled = false
                axisRight.isEnabled = true
                legend.isEnabled = false
                isAutoScaleMinMaxEnabled = true
                data = candleData
                chartRef.value = this
            }
        },
        update = { chart ->
            // ① 데이터 교체
            dataSet.values = entries
            chart.data.notifyDataChanged()
            chart.notifyDataSetChanged()

            // 최초 로드 시 줌 초기화
            if (firstZoom && entries.isNotEmpty()) {
                chart.setVisibleXRange(30f, 30f)
                chart.moveViewToX(entries.last().x)
                firstZoom = false
            }

            chart.xAxis.apply {
                // 1) IndexAxisValueFormatter 사용
                valueFormatter = IndexAxisValueFormatter(xLabels)
                // 2) 인덱스 단위로 라벨 찍기
                granularity = 1f
                // 3) 최대 라벨 갯수 (원하는 값으로)
                setLabelCount(minOf(xLabels.size, 6), true)
            }

            // 최소/최대 줌 범위 설정
            chart.setVisibleXRangeMinimum(30f)
            chart.setVisibleXRangeMaximum(200f)

            // 오버스크롤 및 제스처 리스너
//            chart.xAxis.axisMinimum = -5f
            chart.setOnChartGestureListener(object : OnChartGestureListener {
                override fun onChartScale(me: MotionEvent?, scaleX: Float, scaleY: Float) = postUpdate()
                override fun onChartTranslate(me: MotionEvent?, dX: Float, dY: Float) = postUpdate()
                override fun onChartGestureEnd(me: MotionEvent?, last: ChartTouchListener.ChartGesture?) {
                    if (chart.lowestVisibleX <= 2f) onReachedStart?.invoke()
                    postUpdate()
                }
                override fun onChartGestureStart(me: MotionEvent?, last: ChartTouchListener.ChartGesture?) {}
                override fun onChartLongPressed(me: MotionEvent?) {}
                override fun onChartDoubleTapped(me: MotionEvent?) {}
                override fun onChartSingleTapped(me: MotionEvent?) {}
                override fun onChartFling(me1: MotionEvent?, me2: MotionEvent?, velocityX: Float, velocityY: Float) {}

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
            })

            // 오른쪽 Y축 포맷 및 텍스트 크기 콜백
            chart.axisRight.apply {
                valueFormatter = object : com.github.mikephil.charting.formatter.ValueFormatter() {
                    override fun getFormattedValue(value: Float) = String.format("%,.0f", value)
                }
                onAxisRightTextSizePx?.invoke(textSize)
            }

            // 차트 갱신
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
