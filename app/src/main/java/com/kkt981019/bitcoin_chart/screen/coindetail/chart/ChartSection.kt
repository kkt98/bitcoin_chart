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
    val tabs = listOf("1m", "3m", "5m", "15m", "30m", "1h", "4h")

    LaunchedEffect(symbol, selectedIndex) {
        viewModel.fetchCandles(symbol, selectedIndex)
    }

    Column(modifier.fillMaxWidth()) {
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

        key(selectedIndex) {
            IncrementalCandleChartWithPriceBox(
                entries = minuteCandles,
                xLabels = minuteLabels,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
fun IncrementalCandleChartWithPriceBox(
    entries: List<CandleEntry>,
    modifier: Modifier = Modifier,
    xLabels: List<String>
) {
    var lastVisibleClose    by remember { mutableStateOf(0f) }
    var lastVisibleOpen     by remember { mutableStateOf(0f) }
    var priceBoxOffsetY     by remember { mutableStateOf<Float?>(null) }
    var axisRightTextSizePx by remember { mutableStateOf<Float?>(null) }

    val priceBoxColor = when {
        lastVisibleClose > lastVisibleOpen -> Color.Red
        lastVisibleClose < lastVisibleOpen -> Color.Blue
        else                                -> Color.Gray
    }

    Box(modifier = modifier) {
        IncrementalCandleChart(
            entries = entries,
            xLabels = xLabels,
            modifier = Modifier.fillMaxSize(),
            onLastVisibleClose = { close, yPx, open ->
                lastVisibleClose = close
                lastVisibleOpen  = open
                priceBoxOffsetY  = yPx
            },
            onAxisRightTextSizePx = { px -> axisRightTextSizePx = px }
        )

        if (entries.isNotEmpty() && priceBoxOffsetY != null && axisRightTextSizePx != null) {
            val density = LocalDensity.current
            val yDp       = with(density) { priceBoxOffsetY!!.toDp() }
            val fontSizeSp= pxToSp(axisRightTextSizePx!!)

            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(y = yDp - 14.dp)
                    .background(priceBoxColor, RoundedCornerShape(6.dp))
            ) {
                Text(
                    text = "  ${"%,.0f".format(lastVisibleClose)}  ",
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
    onAxisRightTextSizePx: ((Float) -> Unit)? = null,
    onLastVisibleClose: ((Float, Float, Float) -> Unit)? = null
) {
    val candleDataSet = remember {
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
    val candleData = remember { CandleData(candleDataSet) }
    val firstZoom  = remember { mutableStateOf(true) }

    AndroidView(
        factory = { ctx ->
            CandleStickChart(ctx).apply {
                // 기본 차트 설정만
                description.isEnabled       = false
                setDrawGridBackground(false)
                setPinchZoom(true)
                isDragEnabled = true
                setScaleXEnabled(true)
                setScaleYEnabled(false)
                setScaleEnabled(true)
                viewPortHandler.setMaximumScaleX(3f)
                viewPortHandler.setMinimumScaleX(1f)
                xAxis.position     = XAxis.XAxisPosition.BOTTOM
                axisLeft.isEnabled  = false
                axisRight.isEnabled = true
                legend.isEnabled    = false
                setVisibleXRangeMaximum(30f)
                setVisibleXRangeMinimum(10f)
                data = candleData
            }
        },
        update = { chart ->
            // 1) 제스처 리스너를 update 블록에서 매번 재설정
            chart.setOnChartGestureListener(object : com.github.mikephil.charting.listener.OnChartGestureListener {
                override fun onChartScale(me: MotionEvent?, scaleX: Float, scaleY: Float)   = postUpdate()
                override fun onChartTranslate(me: MotionEvent?, dX: Float, dY: Float)        = postUpdate()
                override fun onChartGestureStart(me: MotionEvent?, lastPerformedGesture: com.github.mikephil.charting.listener.ChartTouchListener.ChartGesture?) {}
                override fun onChartGestureEnd(me: MotionEvent?, lastPerformedGesture: com.github.mikephil.charting.listener.ChartTouchListener.ChartGesture?) {}
                override fun onChartLongPressed(me: MotionEvent?) {}
                override fun onChartDoubleTapped(me: MotionEvent?) {}
                override fun onChartSingleTapped(me: MotionEvent?) {}
                override fun onChartFling(me1: MotionEvent?, me2: MotionEvent?, velocityX: Float, velocityY: Float) {}

                private fun postUpdate() {
                    chart.post {
                        chart.data?.let { data ->
                            val set = data.getDataSetByIndex(0) as CandleDataSet
                            val idx = chart.highestVisibleX.toInt().coerceAtMost(set.entryCount - 1)
                            val e   = set.getEntryForIndex(idx)
                            val pt  = chart.getTransformer(YAxis.AxisDependency.RIGHT)
                                .getPixelForValues(0f, e.close)
                            onLastVisibleClose?.invoke(e.close, pt.y.toFloat(), e.open)
                        }
                    }
                }
            })

            // 2) 데이터 추가/갱신 로직
            if (chart.data == null) chart.data = candleData
            if (entries.isEmpty()) {
                chart.clear()
                return@AndroidView
            }
            val set     = chart.data.getDataSetByIndex(0) as CandleDataSet
            val oldSize = set.entryCount
            val newSize = entries.size
            when {
                newSize > oldSize  -> for (i in oldSize until newSize) set.addEntry(entries[i])
                newSize == oldSize -> {
                    set.removeLast()
                    set.addEntry(entries.last())
                }
            }
            chart.xAxis.apply {
                valueFormatter = IndexAxisValueFormatter(xLabels)
                axisMinimum    = 0f
                axisMaximum    = newSize.toFloat()
            }

            // 3) 첫 진입 시 줌/이동
            if (firstZoom.value) {
                chart.zoom(4f, 1f, entries.last().x, 0f)
                chart.moveViewToX(entries.last().x)
                firstZoom.value = false
            }

            // 4) 우측 축 포맷/텍스트 크기 콜백
            chart.axisRight.valueFormatter = object : com.github.mikephil.charting.formatter.ValueFormatter() {
                override fun getFormattedValue(value: Float): String =
                    String.format("%,.0f", value)
            }
            onAxisRightTextSizePx?.invoke(chart.axisRight.textSize)

            // 5) 초기 표시 후에도 박스 위치 콜백 실행
            chart.post {
                val set2 = chart.data.getDataSetByIndex(0) as CandleDataSet
                val idx  = chart.highestVisibleX.toInt().coerceAtMost(set2.entryCount - 1)
                val e    = set2.getEntryForIndex(idx)
                val pt   = chart.getTransformer(YAxis.AxisDependency.RIGHT)
                    .getPixelForValues(0f, e.close)
                onLastVisibleClose?.invoke(e.close, pt.y.toFloat(), e.open)
            }

            // 6) 리프레시
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
    return px / density.density
}