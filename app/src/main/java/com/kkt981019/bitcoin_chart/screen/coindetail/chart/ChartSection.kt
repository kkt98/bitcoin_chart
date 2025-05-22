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
import androidx.compose.ui.input.pointer.pointerInput
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
            IncrementalCandleChartWithPriceBox(
                symbol    = symbol,
                tabIndex  = selectedIndex,
                entries   = minuteCandles,
                xLabels   = minuteLabels,
                modifier  = Modifier.fillMaxSize(),
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
    // ① 차트 인스턴스 레퍼런스
    val chartRef   = remember { mutableStateOf<CandleStickChart?>(null) }
    var addedCount by remember { mutableStateOf(0) }

    // ② 과거 로드 후 위치 보정
    LaunchedEffect(addedCount) {
        if (addedCount > 0) {
            chartRef.value?.moveViewToX(addedCount.toFloat())
            addedCount = 0
        }
    }

    var lastClose   by remember { mutableStateOf(0f) }
    var lastOpen    by remember { mutableStateOf(0f) }
    var boxOffsetY  by remember { mutableStateOf<Float?>(null) }
    var rightTextPx by remember { mutableStateOf<Float?>(null) }

    val boxColor = when {
        lastClose > lastOpen -> Color.Red
        lastClose < lastOpen -> Color.Blue
        else                  -> Color.Gray
    }

    Box(modifier = modifier) {
        IncrementalCandleChart(
            entries             = entries,
            xLabels             = xLabels,
            modifier            = Modifier.fillMaxSize(),
            onLastVisibleClose  = { close, y, open ->
                lastClose  = close
                lastOpen   = open
                boxOffsetY = y
            },
            onAxisRightTextSizePx = { px -> rightTextPx = px },
            onReachedStart      = {
                viewModel.fetchPreviousCandles(symbol, tabIndex) { added ->
                    addedCount = added
                }
            },
            chartRef = chartRef
        )

        if (entries.isNotEmpty() && boxOffsetY != null && rightTextPx != null) {
            val density = LocalDensity.current
            val yDp     = with(density) { boxOffsetY!!.toDp() }
            val fontSp  = pxToSp(rightTextPx!!)

            Box(
                Modifier
                    .align(Alignment.TopEnd)
                    .offset(y = yDp - 14.dp)
                    .background(boxColor, RoundedCornerShape(6.dp))
            ) {
                Text(
                    text     = "  ${"%,.0f".format(lastClose)}  ",
                    color    = Color.White,
                    fontSize = fontSp.sp,
                    modifier = Modifier.padding(vertical = 2.dp)
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
    onAxisRightTextSizePx: ((Float)->Unit)? = null,
    onLastVisibleClose: ((Float,Float,Float)->Unit)? = null,
    onReachedStart: (() -> Unit)? = null,
    chartRef: MutableState<CandleStickChart?>    // ③ 차트 참조 파라미터
) {
    val dataSet = remember {
        CandleDataSet(mutableListOf(), "OHLC").apply {
            decreasingColor         = android.graphics.Color.BLUE
            setDecreasingPaintStyle(Paint.Style.FILL)
            increasingColor         = android.graphics.Color.RED
            setIncreasingPaintStyle(Paint.Style.FILL)
            setDrawValues(false)
            shadowColorSameAsCandle = true
            neutralColor            = android.graphics.Color.GRAY
            axisDependency          = YAxis.AxisDependency.RIGHT
        }
    }
    val candleData = remember { CandleData(dataSet) }
    val firstZoom  = remember { mutableStateOf(true) }

    AndroidView(
        factory = { ctx ->
            CandleStickChart(ctx).apply {
                description.isEnabled       = false
                setDrawGridBackground(false)
                setPinchZoom(true)
                isDragEnabled    = true
                setScaleEnabled (true)
                setScaleXEnabled (true)
                setScaleYEnabled (false)
                viewPortHandler.setMaximumScaleX(3f)
                viewPortHandler.setMinimumScaleX(1f)
                xAxis.position     = XAxis.XAxisPosition.BOTTOM
                axisLeft.isEnabled  = false
                axisRight.isEnabled = true
                legend.isEnabled    = false
                setVisibleXRangeMaximum(30f)  // 한 화면에 30봉 고정
                setVisibleXRangeMinimum(10f)
                this.data = candleData

                chartRef.value = this     // factory에서 레퍼런스 저장
            }
        },
        update = { chart ->
            chartRef.value = chart      // update에서도 저장

            // 제스처 리스너
            chart.setOnChartGestureListener(object : com.github.mikephil.charting.listener.OnChartGestureListener {
                override fun onChartScale(me: MotionEvent?, sx: Float, sy: Float)   = postUpdate()
                override fun onChartTranslate(me: MotionEvent?, dx: Float, dy: Float) = postUpdate()
                override fun onChartGestureEnd(me: MotionEvent?, last: ChartTouchListener.ChartGesture?) {
                    postUpdate()
                    postCheckStart()
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
                            val e   = set.getEntryForIndex(idx)
                            val pt  = chart.getTransformer(YAxis.AxisDependency.RIGHT)
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

            // ① 전체 데이터 교체
            if (entries.isEmpty()) {
                chart.clear()
            } else {
                dataSet.setValues(entries)
            }

            // ② 첫 진입 줌/이동
            if (firstZoom.value && entries.isNotEmpty()) {
                chart.zoom(2f, 1f, entries.last().x, 0f)
                chart.moveViewToX(entries.last().x)
                firstZoom.value = false
            }

            // ③ 우측 축 포맷 & 크기 콜백
            chart.axisRight.valueFormatter = object : com.github.mikephil.charting.formatter.ValueFormatter() {
                override fun getFormattedValue(v: Float) = String.format("%,.0f", v)
            }
            onAxisRightTextSizePx?.invoke(chart.axisRight.textSize)

            // ④ 초기 박스 위치 콜백
            chart.post {
                chart.data?.let { cd2 ->
                    val set2 = cd2.getDataSetByIndex(0) as CandleDataSet
                    val idx  = chart.highestVisibleX.toInt().coerceAtMost(set2.entryCount - 1)
                    val e    = set2.getEntryForIndex(idx)
                    val pt   = chart.getTransformer(YAxis.AxisDependency.RIGHT)
                        .getPixelForValues(0f, e.close)
                    onLastVisibleClose?.invoke(e.close, pt.y.toFloat(), e.open)
                }
            }

            // ⑤ 리프레시
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