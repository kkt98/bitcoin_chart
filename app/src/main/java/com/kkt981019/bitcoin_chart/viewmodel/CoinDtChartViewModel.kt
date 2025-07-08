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
import kotlinx.coroutines.Dispatchers
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

    private val _minuteCandleState = MutableLiveData<List<CandleEntry>>(emptyList())
    val minuteCandleState: LiveData<List<CandleEntry>> = _minuteCandleState

    private val _minuteTimeLabels = MutableLiveData<List<String>>(emptyList())
    val minuteTimeLabels: LiveData<List<String>> = _minuteTimeLabels

    private var tradeSocket: WebSocket? = null
    private var candleSocket: WebSocket? = null
    private val candleMap = mutableMapOf<Long, AggregatedCandle>()

    /** 정상적인 종료를 위한 메서드 */
    fun stopTrade() {
        tradeSocket?.close(1000, "normal shutdown")
        tradeSocket = null
    }

    fun stopCandle() {
        candleSocket?.close(1000, "normal shutdown")
        candleSocket = null
    }

    /** REST 스냅샷 + WebSocket 구독 */
    fun fetchCandles(symbol: String, tabIndex: Int) {
        // 이전 스트림 종료 및 데이터 초기화
        stopTrade()
        stopCandle()
        candleMap.clear()

        if (tabIndex !in 0..7) return

        val unitList = listOf(1, 3, 5, 15, 30, 60, 240, 1440)
        val unitMin = unitList[tabIndex]
        val interval = if (unitMin == 1440) "1일" else "${unitMin}m"
        val windowMs = if (unitMin == 1440) 86_400_000L else unitMin * 60_000L

        viewModelScope.launch(Dispatchers.IO) {
            // 1) 과거 데이터(REST) 조회
            try {
                val nowSeoul = OffsetDateTime.now(ZoneId.of("Asia/Seoul")).withSecond(0).withNano(0)
                val toIso = nowSeoul.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                val past: List<RetrofitCandleResponse> = if (unitMin == 1440) {
                    repo.getDayCandle(symbol, to = toIso)
                } else {
                    repo.getMinuteCandle(symbol, unitMin, to = toIso)
                }
                past.forEach { r ->
                    val key = (r.timestamp / windowMs) * windowMs
                    val label = if (unitMin == 1440) r.candleDateTimeKst.substring(5, 10)
                    else r.candleDateTimeKst.substring(11, 16)
                    candleMap[key] = AggregatedCandle(
                        time  = key.toFloat(),
                        open  = r.openingPrice,
                        high  = r.highPrice,
                        low   = r.lowPrice,
                        close = r.tradePrice,
                        label = label
                    )
                }
                postMinuteData()
            } catch (e: Exception) {
                Log.e("ChartVM", "REST fetch failed", e)
                candleMap.clear()
                postMinuteData()
            }

            // 2) 실시간 스트림 구독
            startAggregation(symbol, interval, windowMs)
        }
    }

    /** 과거 캔들 추가 로드 */
    fun fetchPreviousCandles(
        symbol: String,
        tabIndex: Int,
        onComplete: (Int) -> Unit
    ) {
        if (tabIndex !in 0..7) { onComplete(0); return }

        val unitList = listOf(1, 3, 5, 15, 30, 60, 240, 1440)
        val unitMin = unitList[tabIndex]
        val windowMs = if (unitMin == 1440) 86_400_000L else unitMin * 60_000L
        val oldestKey = candleMap.keys.minOrNull() ?: run { onComplete(0); return }
        val oldestDate = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.KOREA)
            .format(java.util.Date(oldestKey - windowMs))

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val past: List<RetrofitCandleResponse> = if (unitMin == 1440) {
                    repo.getDayCandle(symbol, to = "$oldestDate+09:00")
                } else {
                    repo.getMinuteCandle(symbol, unitMin, to = "$oldestDate+09:00")
                }
                val before = past.filterNot { r ->
                    val key = (r.timestamp / windowMs) * windowMs
                    candleMap.containsKey(key)
                }
                before.forEach { r ->
                    val key = (r.timestamp / windowMs) * windowMs
                    val label = r.candleDateTimeKst.substring(11, 16)
                    candleMap[key] = AggregatedCandle(
                        time  = key.toFloat(),
                        open  = r.openingPrice,
                        high  = r.highPrice,
                        low   = r.lowPrice,
                        close = r.tradePrice,
                        label = label
                    )
                }
                postMinuteData()
                onComplete(before.size)
            } catch (e: Exception) {
                Log.e("ChartVM", "Load previous failed", e)
                onComplete(0)
            }
        }
    }

    /** 채널별 WebSocket 구독 */
    private fun startAggregation(
        symbol: String,
        interval: String,
        windowMs: Long
    ) {
        if (interval == "1일") {
            tradeSocket = webSocketRepository.startTradeSocket(symbol) { ws ->
                val key = (ws.timestamp / windowMs) * windowMs
                val dateLabel = Instant.ofEpochMilli(key)
                    .atZone(ZoneId.of("Asia/Seoul"))
                    .format(DateTimeFormatter.ofPattern("MM-dd"))

                val agg = candleMap.getOrPut(key) {
                    AggregatedCandle(key.toFloat(), ws.tradePrice, ws.tradePrice, ws.tradePrice, ws.tradePrice, dateLabel)
                }
                agg.apply {
                    high = maxOf(high, ws.tradePrice)
                    low  = minOf(low,  ws.tradePrice)
                    close= ws.tradePrice
                }
                postMinuteData()
            }
        } else {
            candleSocket = webSocketRepository.starCandleSocket(symbol, interval) { ws ->
                val key  = (ws.timestamp / windowMs) * windowMs
                val label= ws.candleDateTimeKst.substring(11, 16)
                val agg  = candleMap.getOrPut(key) {
                    AggregatedCandle(key.toFloat(), ws.openPrice, ws.highPrice, ws.lowPrice, ws.tradePrice, label)
                }
                agg.apply {
                    high  = maxOf(high, ws.highPrice)
                    low   = minOf(low,  ws.lowPrice)
                    close = ws.tradePrice
                    this.label = label
                }
                postMinuteData()
            }
        }
    }

    /** LiveData 업데이트 */
    private fun postMinuteData() {
        val sorted = candleMap.entries.sortedBy { it.key }.map { it.value }
        _minuteCandleState.postValue(
            sorted.mapIndexed { i, c ->
                CandleEntry(i.toFloat(), c.high.toFloat(), c.low.toFloat(), c.open.toFloat(), c.close.toFloat())
            }
        )
        _minuteTimeLabels.postValue(sorted.map { it.label })
    }

    data class AggregatedCandle(
        var time: Float,
        var open: Double,
        var high: Double,
        var low: Double,
        var close: Double,
        var label: String
    )

    override fun onCleared() {
        stopTrade()
        stopCandle()
        super.onCleared()
    }
}
