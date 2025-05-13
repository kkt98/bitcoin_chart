package com.kkt981019.bitcoin_chart.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.mikephil.charting.data.CandleEntry
import com.kkt981019.bitcoin_chart.network.Data.RetrofitCandleResponse
import com.kkt981019.bitcoin_chart.network.Data.WebSocketCandleResponse
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

    // 분봉 CandleEntry 리스트
    private val _minuteCandleState = MutableLiveData<List<CandleEntry>>(emptyList())
    val minuteCandleState: LiveData<List<CandleEntry>> = _minuteCandleState

    // 분봉 X축 레이블 ("HH:mm")
    private val _minuteTimeLabels = MutableLiveData<List<String>>(emptyList())
    val minuteTimeLabels: LiveData<List<String>> = _minuteTimeLabels

    // 일봉 리스트
    private val _dayCandleState = MutableLiveData<List<RetrofitCandleResponse>>(emptyList())
    val dayCandleState: LiveData<List<RetrofitCandleResponse>> = _dayCandleState

    private var socket: WebSocket? = null
    private val candleMap = mutableMapOf<Long, AggregatedCandle>()

    private var currentWindowMs: Long = 0L

    fun fetchCandles(symbol: String, tabIndex: Int) {
        // 이전 소켓 즉시 해제
        socket?.cancel()
        candleMap.clear()

        if (tabIndex in 0..6) {
            // 분봉 탭 (1m,3m,5m,15m,...)
            val unitList = listOf(1, 3, 5, 15, 30, 60, 240)
            val unitMin = unitList[tabIndex]
            val interval = "${unitMin}m"  // "1m" etc.

            viewModelScope.launch {
                // 1) REST로 과거 분봉 로드
                val past = repo.getMinuteCandle(symbol, unitMin)

                Log.d("asdasd4444", past.toString())
                val windowMs = unitMin * 60_000L
                currentWindowMs = windowMs
                val sdf = SimpleDateFormat("HH:mm", Locale.KOREA)

                past.forEach { r ->
                    val key = (r.timestamp / windowMs) * windowMs
                    val hhmm = r.candleDateTimeKst.substring(11,16)
                    candleMap[key] = AggregatedCandle(
                        time  = key.toFloat(),
                        open  = r.openingPrice,
                        high  = r.highPrice,
                        low   = r.lowPrice,
                        close = r.tradePrice,
                        label = hhmm
                    )
                }
                // 초기 데이터 발행
                postMinuteData()

                // 2) WebSocket 실시간 구독 시작
                startAggregation(symbol, interval)
            }
        } else {
            // 일봉 탭(24h)
            viewModelScope.launch {
                _dayCandleState.postValue(repo.getDayCandle(symbol))
            }
        }
    }

    private fun startAggregation(symbol: String, interval: String) {
        val unitMin  = interval.dropLast(1).toInt()
        val windowMs = unitMin * 60_000L

        socket = webSocketRepository.starCandleSocket(symbol, interval) { ws ->
            val key  = (ws.timestamp / windowMs) * windowMs
            val hhmm = ws.candleDateTimeKst.substring(11,16)
            val agg = candleMap.getOrPut(key) {
                AggregatedCandle(
                    time  = key.toFloat(),
                    open  = ws.openPrice,
                    high  = ws.highPrice,
                    low   = ws.lowPrice,
                    close = ws.tradePrice,
                    label = hhmm
                )
            }.apply {
                high  = maxOf(high, ws.highPrice)
                low   = minOf(low, ws.lowPrice)
                close = ws.tradePrice
                label = hhmm
            }
            postMinuteData()
        }
    }

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


