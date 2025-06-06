package com.kkt981019.bitcoin_chart.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.mikephil.charting.data.CandleEntry
import com.kkt981019.bitcoin_chart.network.Data.RetrofitCandleResponse
import com.kkt981019.bitcoin_chart.network.Data.WebSocketCandleResponse
import com.kkt981019.bitcoin_chart.network.Data.WebSocketTradeResponse
import com.kkt981019.bitcoin_chart.repository.RetrofitRepository
import com.kkt981019.bitcoin_chart.repository.WebSocketRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import okhttp3.WebSocket
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class CoinDtChartViewModel @Inject constructor(
    private val repo: RetrofitRepository,
    private val webSocketRepository: WebSocketRepository
) : ViewModel() {

    // LiveData로 CandleEntry 리스트와 레이블을 관리
    private val _minuteCandleState = MutableLiveData<List<CandleEntry>>(emptyList())
    val minuteCandleState: LiveData<List<CandleEntry>> = _minuteCandleState

    private val _minuteTimeLabels = MutableLiveData<List<String>>(emptyList())
    val minuteTimeLabels: LiveData<List<String>> = _minuteTimeLabels

    private var socket: WebSocket? = null

    // key = (timestamp / windowMs) * windowMs (정렬용), value = AggregatedCandle
    private val candleMap = mutableMapOf<Long, AggregatedCandle>()

    /**
     * 최초 또는 탭 전환 시 캔들(REST + WebSocket)을 불러오는 함수
     */
    fun fetchCandles(symbol: String, tabIndex: Int) {
        // 이전 WebSocket이 연결되어 있으면 끊어주고, 데이터 초기화
        socket?.cancel()
        candleMap.clear()

        if (tabIndex in 0..7) {
            val unitList = listOf(1, 3, 5, 15, 30, 60, 240, 1440)
            val unitMin = unitList[tabIndex]
            val interval = if (unitMin == 1440) "1일" else "${unitMin}m"
            val windowMs = if (unitMin == 1440) 86_400_000L else unitMin * 60_000L

            viewModelScope.launch {
                // 현재 시각 ISO 포맷 (KST)
                val nowSeoul = OffsetDateTime.now(ZoneId.of("Asia/Seoul"))
                    .withSecond(0).withNano(0)
                val toIso = nowSeoul.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)

                // 1) REST로 과거 캔들 가져오기 (분/일 구분)
                val past: List<RetrofitCandleResponse> = if (unitMin == 1440) {
                    repo.getDayCandle(symbol, to = toIso)     // 일봉 전용
                } else {
                    repo.getMinuteCandle(symbol, unitMin, to = toIso)
                }

                past.forEachIndexed { index, r ->
                    Log.d("fetchCandlesDates", "[$index] candleDateTimeKst = ${r.candleDateTimeKst}")
                }

                // 받은 과거 캔들들을 key(timestamp)로 map에 넣는다.
                past.forEach { r ->
                    val key = (r.timestamp / windowMs) * windowMs
                    // 레이블: 분 단위는 “HH:mm”, 일 단위는 “MM-dd”
                    val label = if (unitMin == 1440) {
                        r.candleDateTimeKst.substring(5, 10)   // ex: "05-31"
                    } else {
                        r.candleDateTimeKst.substring(11, 16)  // ex: "14:05"
                    }

                    candleMap[key] = AggregatedCandle(
                        time  = key.toFloat(),
                        open  = r.openingPrice,
                        high  = r.highPrice,
                        low   = r.lowPrice,
                        close = r.tradePrice,
                        label = label
                    )
                }
                // 초기 데이터(REST) 정렬 → 재인덱싱 → LiveData 업데이트
                postMinuteData()

                // 2) WebSocket으로 실시간 데이터 구독 시작
                startAggregation(symbol, interval, windowMs)
            }
        }
    }

    /**
     * 사용자가 차트의 왼쪽 끝(가장 오래된 봉)에 도달했을 때 호출해서, 더 이전 데이터를 가져오는 함수
     */
    fun fetchPreviousCandles(
        symbol: String,
        tabIndex: Int,
        onComplete: (Int) -> Unit    // 새로 추가된 봉 개수를 알려주는 콜백
    ) {
        val unitList = listOf(1, 3, 5, 15, 30, 60, 240, 1440)
        val unitMin = unitList[tabIndex]
        val windowMs = if (unitMin == 1440) 86_400_000L else unitMin * 60_000L

        // candleMap에 남아 있는 제일 오래된 키를 가져와서, 그보다 windowMs만큼 이전 시각 문자열을 만든다
        val oldestKey = candleMap.keys.minOrNull() ?: return
        val oldestDate = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.KOREA)
            .format(java.util.Date(oldestKey))

        viewModelScope.launch {
            // 1) REST로 더 이전 캔들 가져오기 (분/일 구분)
            val past: List<RetrofitCandleResponse> = if (unitMin == 1440) {
                repo.getDayCandle(symbol, to = "$oldestDate+09:00")
            } else {
                repo.getMinuteCandle(symbol, unitMin, to = "$oldestDate+09:00")
            }
            past.forEachIndexed { index, r ->
                Log.d("fetchCandlesDates", "2-[$index] candleDateTimeKst = ${r.candleDateTimeKst}")
            }
            // 기존 candleMap에 없는 키들만 필터해서 추가
            val before = past.filter { r ->
                val key = (r.timestamp / windowMs) * windowMs
                !candleMap.containsKey(key)
            }
            before.forEach { r ->
                val key = (r.timestamp / windowMs) * windowMs
                val hhmm = if (unitMin == 1440) {
                    // 일봉: MM-dd 로 표시
                    SimpleDateFormat("MM-dd", Locale.KOREA).format(java.util.Date(key))
                } else {
                    // 분봉: HH:mm
                    r.candleDateTimeKst.substring(11, 16)
                }
                candleMap[key] = AggregatedCandle(
                    time  = key.toFloat(),
                    open  = r.openingPrice,
                    high  = r.highPrice,
                    low   = r.lowPrice,
                    close = r.tradePrice,
                    label = hhmm
                )
            }

            // 더 이전 데이터를 map에 넣었으면, 다시 정렬 → 재인덱싱 → LiveData 업데이트
            postMinuteData()

            // 콜백으로 “몇 개(before.size) 만큼 추가되었는지” 알리기
            onComplete(before.size)
        }
    }

    /**
     * WebSocket이 도착하는 실시간 데이터를 candleMap에 머지(agg 적용)한 뒤,
     * postMinuteData()를 호출해서 LiveData를 갱신한다.
     */
    private fun startAggregation(
        symbol: String,
        interval: String,
        windowMs: Long
    ) {
        socket = if (interval == "1일") {
            webSocketRepository.startTradeSocket(symbol) { ws: WebSocketTradeResponse ->
                val key = (ws.timestamp / windowMs) * windowMs

                // KST 0시 기준 레이블 (MM-dd)
                val dateLabel = Instant.ofEpochMilli(key)
                    .atZone(ZoneId.of("Asia/Seoul"))
                    .format(DateTimeFormatter.ofPattern("MM-dd"))

                val agg = candleMap.getOrPut(key) {
                    AggregatedCandle(
                        time  = key.toFloat(),
                        open  = ws.tradePrice,
                        high  = ws.tradePrice,
                        low   = ws.tradePrice,
                        close = ws.tradePrice,
                        label = dateLabel
                    )
                }
                // 실시간 체결이 들어올 때마다 월가/저가/종가를 갱신
                agg.apply {
                    high  = maxOf(high, ws.tradePrice)
                    low   = minOf(low,  ws.tradePrice)
                    close = ws.tradePrice
                    // label(날짜)은 하루 종일 동일하므로 갱신 불필요
                }
                postMinuteData()
            }
        } else {
            webSocketRepository.starCandleSocket(symbol, interval) { ws: WebSocketCandleResponse ->
                val key  = (ws.timestamp / windowMs) * windowMs
                val hhmm = ws.candleDateTimeKst.substring(11, 16)
                val agg = candleMap.getOrPut(key) {
                    AggregatedCandle(key.toFloat(), ws.openPrice, ws.highPrice, ws.lowPrice, ws.tradePrice, hhmm)
                }.apply {
                    high  = maxOf(high, ws.highPrice)
                    low   = minOf(low,  ws.lowPrice)
                    close = ws.tradePrice
                    label = hhmm
                }
                postMinuteData()
            }
        }
    }

    /**
     * candleMap을 “timestamp 키 순서” 로 정렬해서,
     *   1) sorted List<AggregatedCandle> 만들기
     *   2) List<AggregatedCandle> 을 mapIndexed로 순회하며 CandleEntry(x=i, ...)로 변환
     *   3) 그 결과를 LiveData로 즉시(value =) 업데이트
     *   4) 레이블도 함께 LiveData로 업데이트
     */
    private fun postMinuteData() {
        val sorted = candleMap.entries
            .sortedBy { it.key }
            .map { it.value }

        // **중요**: postValue() 대신.value= 로 동기 업데이트
        _minuteCandleState.value = sorted.mapIndexed { i, c ->
            CandleEntry(
                i.toFloat(),
                c.high.toFloat(),
                c.low.toFloat(),
                c.open.toFloat(),
                c.close.toFloat()
            )
        }
        _minuteTimeLabels.value = sorted.map { it.label }
    }

    data class AggregatedCandle(
        val time: Float,
        var open: Double,
        var high: Double,
        var low: Double,
        var close: Double,
        var label: String
    )

    override fun onCleared() {
        socket?.cancel()
        super.onCleared()
    }
}

