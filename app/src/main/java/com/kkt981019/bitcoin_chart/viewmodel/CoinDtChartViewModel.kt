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
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class CoinDtChartViewModel @Inject constructor(
    private val repo: RetrofitRepository,
    private val webSocketRepository: WebSocketRepository
) : ViewModel() {

    // 분봉 상태
    private val _minuteCandleState = MutableLiveData<List<CandleEntry>>(emptyList())
    val minuteCandleState: LiveData<List<CandleEntry>> = _minuteCandleState

    private val _minuteTimeLabels = MutableLiveData<List<String>>(emptyList())
    val minuteTimeLabels: LiveData<List<String>> = _minuteTimeLabels

    // 일봉 상태
    private val _dayCandleState = MutableLiveData<List<RetrofitCandleResponse>>(emptyList())
    val dayCandleState: LiveData<List<RetrofitCandleResponse>> = _dayCandleState

    private var socket: WebSocket? = null
    private val candleMap = mutableMapOf<Long, AggregatedCandle>()

    fun fetchCandles(symbol: String, tabIndex: Int) {
        socket?.cancel()
        candleMap.clear()

        if (tabIndex in 0..6) {
            // ── 분봉 탭(1m~4h) ──
            val unitList = listOf(1, 3, 5, 15, 30, 60, 240)
            val unitMin = unitList[tabIndex]
            val interval = "${unitMin}m"
            val windowMs = unitMin * 60_000L

            viewModelScope.launch {
                // 1) REST 과거 분봉
                val past = repo.getMinuteCandle(symbol, unitMin)
                past.forEach { r ->
                    val key = (r.timestamp / windowMs) * windowMs
                    val hhmm = r.candleDateTimeKst.substring(11, 16)
                    candleMap[key] = AggregatedCandle(
                        time  = key.toFloat(),
                        open  = r.openingPrice,
                        high  = r.highPrice,
                        low   = r.lowPrice,
                        close = r.tradePrice,
                        label = hhmm
                    )
                }
                postMinuteData()

                // 2) WebSocket 분봉 스트림 구독
                startAggregation(symbol, interval, windowMs)
            }

        } else {
            // ── 일봉 탭(24h) ──
            viewModelScope.launch {
                // 1) REST 과거 일봉
                val pastDays = repo.getDayCandle(symbol)
                val windowMs = 24 * 60 * 60 * 1000L
                pastDays.forEach { r ->
                    // "yyyy-MM-ddTHH:mm:ss" 형태 문자열에서 timestamp 뽑기
                    val date = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.KOREA)
                        .parse(r.candleDateTimeKst)!!
                    val key = (date.time / windowMs) * windowMs
                    candleMap[key] = AggregatedCandle(
                        time  = key.toFloat(),
                        open  = r.openingPrice,
                        high  = r.highPrice,
                        low   = r.lowPrice,
                        close = r.tradePrice,
                        label = r.candleDateTimeKst.substring(0, 10)
                    )
                }
                // 초기 그리기
                postDayData()

                // 2) WebSocket 체결 스트림 구독
                startDailyAggregation(symbol)
            }
        }
    }

    // 분봉용: Upbit의 분·시간 단위 캔들 스트림
    private fun startAggregation(
        symbol: String,
        interval: String,
        windowMs: Long
    ) {
        socket = webSocketRepository.starCandleSocket(symbol, interval) { ws: WebSocketCandleResponse ->
            val key  = (ws.timestamp / windowMs) * windowMs
            val hhmm = ws.candleDateTimeKst.substring(11, 16)
            val agg = candleMap.getOrPut(key) {
                AggregatedCandle(key.toFloat(), ws.openPrice, ws.highPrice, ws.lowPrice, ws.tradePrice, hhmm)
            }.apply {
                high  = maxOf(high,  ws.highPrice)
                low   = minOf(low,   ws.lowPrice)
                close = ws.tradePrice
                label = hhmm
            }
            postMinuteData()
        }
    }

    // 일봉용: Upbit의 체결(trade) 스트림을 받아 하루(24h) 단위로 집계
    private fun startDailyAggregation(symbol: String) {
        val windowMs = 24 * 60 * 60 * 1000L

        socket = webSocketRepository.startTradeSocket(symbol) { tick: WebSocketTradeResponse ->
            // timestamp 필드(밀리초)를 기준으로 키 계산
            val key = (tick.timestamp / windowMs) * windowMs
            // 라벨은 "yyyy-MM-dd"
            val dateLabel = tick.tradeDate

            val agg = candleMap.getOrPut(key) {
                AggregatedCandle(key.toFloat(), tick.tradePrice, tick.tradePrice, tick.tradePrice, tick.tradePrice, dateLabel)
            }.apply {
                high  = maxOf(high,  tick.tradePrice)
                low   = minOf(low,   tick.tradePrice)
                close = tick.tradePrice
            }
            postDayData()
        }
    }

    // 분봉 LiveData 발행
    private fun postMinuteData() {
        val sorted = candleMap.entries
            .sortedBy { it.key }
            .map { it.value }

        _minuteCandleState.postValue(
            sorted.mapIndexed { idx, c ->
                CandleEntry(
                    idx.toFloat(),
                    c.high.toFloat(),
                    c.low.toFloat(),
                    c.open.toFloat(),
                    c.close.toFloat()
                )
            }
        )
        _minuteTimeLabels.postValue(sorted.map { it.label })
    }

    // 일봉 LiveData 발행
    private fun postDayData() {
        val sorted = candleMap.entries
            .sortedBy { it.key }
            .map { it.value }

        val list = sorted.map { c ->
            RetrofitCandleResponse(
                candleDateTimeKst = "${c.label}T00:00:00",
                openingPrice = c.open,
                highPrice = c.high,
                lowPrice = c.low,
                tradePrice = c.close,
                unit = 1440,
                market = "",
                candleDateTimeUtc = "",
                candleAccTradePrice = 0.0,
                candleAccTradeVolume = 0.0,
                timestamp = 0
            )
        }
        _dayCandleState.postValue(list)
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
        socket = null
        super.onCleared()
    }
}


