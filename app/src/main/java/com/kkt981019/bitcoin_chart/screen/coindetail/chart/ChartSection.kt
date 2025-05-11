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
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.kkt981019.bitcoin_chart.network.Data.RetrofitCandleResponse
import com.kkt981019.bitcoin_chart.network.Data.WebSocketCandleResponse
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

        // ── 차트
        if (selectedIndex in 0..6) {
            CombinedCandleChart(entries = minuteCandles, modifier = Modifier.fillMaxSize())
        } else {
            CombinedCandleVolumeChart(
                data = dayCandles,  // 기존 일봉 렌더러
                modifier = Modifier.fillMaxSize()
            )
        }
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

                // --- 확대/이동 가능하도록 설정 ---
                isDragEnabled      = true    // 드래그로 이동 허용
                setScaleEnabled(true)        // 전체 스케일링 허용
                setScaleXEnabled(true)       // X축 스케일링 허용
                setScaleYEnabled(false)      // Y축은 고정

                // 최대/최소 확대 배율 지정 (optional)
                viewPortHandler.setMaximumScaleX(3f)   // 최대 4배 확대
                viewPortHandler.setMinimumScaleX(1f)   // 최소 1배(원래 크기)

                // 한 화면에 최대로 보일 엔트리 개수 지정
                // 예: MAX 50 캔들, MIN 10 캔들
                // 한 화면에 보일 캔들 개수: 최대 30개, 최소 30개
                setVisibleXRangeMaximum(50f)
                setVisibleXRangeMinimum(10f)
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
                // ─── 색상 ───
                decreasingColor         = android.graphics.Color.BLUE
                increasingColor         = android.graphics.Color.RED

                // ─── 페인트 스타일 ───
                setIncreasingPaintStyle(android.graphics.Paint.Style.FILL)
                setDecreasingPaintStyle(android.graphics.Paint.Style.FILL)
                setShowCandleBar(true)
                shadowColorSameAsCandle = true
                axisDependency = YAxis.AxisDependency.RIGHT
                setDrawValues(false)
            }

            // 4) 차트에 데이터 적용
            chart.data = CandleData(candleSet)

            // 이전 줌 리셋
            chart.fitScreen()

            val scaleX = 5f
            val scaleY = 1f
            chart.zoom(scaleX, scaleY, sortedData.size.toFloat(), 0f)

            // 뷰포트 이동
            chart.moveViewToX(sortedData.size.toFloat())

            chart.invalidate()
        },
        modifier = modifier
    )
}

@Composable
fun CombinedCandleChart(
    entries: List<CandleEntry>,        // 차트에 그릴 CandleEntry 리스트 (시가·고가·저가·종가 정보)
    modifier: Modifier = Modifier
) {
    AndroidView(
        factory = { ctx ->
            CandleStickChart(ctx).apply {
                // ── 차트 기본 설정 ──
                description.isEnabled = false            // 차트 설명 텍스트 비활성화
                setDrawGridBackground(false)             // 그리드 배경 비활성화
                setPinchZoom(true)                        // 핀치 줌 허용

                // ── 축 설정 ──
                axisLeft.isEnabled  = false               // 왼쪽 Y축 비활성화
                axisRight.isEnabled = true                // 오른쪽 Y축 활성화
                xAxis.position      = XAxis.XAxisPosition.BOTTOM // X축 레이블 아래쪽 배치
                legend.isEnabled    = false               // 범례(legend) 비활성화

                // ── 사용자 인터랙션 (줌/드래그) ──
                isDragEnabled    = true                   // 드래그로 차트 이동 허용
                setScaleEnabled(true)                     // 전체 스케일링(줌) 허용
                setScaleXEnabled(true)                    // X축 방향 줌 허용
                setScaleYEnabled(false)                   // Y축 방향 줌 비허용 (고정)

                // ── 줌 배율 제한 (optional) ──
                viewPortHandler.setMaximumScaleX(3f)      // X축 최대 3배 확대
                viewPortHandler.setMinimumScaleX(1f)      // X축 최소 1배 (원래 크기)

                // ── 한 화면에 표시할 캔들 개수 범위 ──
                setVisibleXRangeMaximum(50f)              // 최대 50개 봉 표시
                setVisibleXRangeMinimum(10f)              // 최소 10개 봉 표시
            }
        },
        update = { chart ->
            if (entries.isEmpty()) {
                chart.clear()                            // 데이터 없으면 차트 비움
                return@AndroidView
            }

            // ── CandleDataSet 생성 ──
            val candleSet = CandleDataSet(entries, "OHLC").apply {
                decreasingColor         = android.graphics.Color.BLUE  // 하락봉 색상
                increasingColor         = android.graphics.Color.RED   // 상승봉 색상
                setIncreasingPaintStyle(android.graphics.Paint.Style.FILL)  // 상승봉 내부 채우기
                setDecreasingPaintStyle(android.graphics.Paint.Style.FILL)  // 하락봉 내부 채우기
                setShowCandleBar(true)                              // 캔들 바 모양 보이기
                shadowColorSameAsCandle = true                      // 꼬리(shadow) 색상 봉과 동일
                axisDependency = YAxis.AxisDependency.RIGHT         // 데이터 축을 오른쪽 Y축에 종속
                setDrawValues(false)                                // 각 엔트리 값 텍스트 비표시
            }

            // ── 차트에 데이터 바인딩 및 리프레시 ──
            chart.data = CandleData(candleSet)                     // CandleDataSet을 CandleData로 래핑
            chart.fitScreen()                                      // 기존 줌/이동 설정 초기화

            // ── 초기 줌(스케일) 설정 ──
            val scaleX = 5f                                        // X축 확대 배율
            val scaleY = 1f                                        // Y축 확대 배율 (사용 안 함)
            chart.zoom(scaleX, scaleY, entries.size.toFloat(), 0f)// 지정한 pivot 위치로 줌

            // ── 뷰포트 이동 ──
            chart.moveViewToX(entries.size.toFloat())              // 가장 마지막 봉 위치로 이동

            chart.invalidate()                                     // 실제 그리기
        },
        modifier = modifier
    )
}



