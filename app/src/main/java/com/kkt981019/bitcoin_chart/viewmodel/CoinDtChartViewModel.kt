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

    private val _minuteCandleState = MutableLiveData<List<CandleEntry>>(emptyList())
    val minuteCandleState: LiveData<List<CandleEntry>> = _minuteCandleState

    private val _minuteTimeLabels = MutableLiveData<List<String>>(emptyList())
    val minuteTimeLabels: LiveData<List<String>> = _minuteTimeLabels

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
                Log.d("dateCheck", "[처음로드] ${past.size}개")
                past.forEach { r ->
                    val key = (r.timestamp / windowMs) * windowMs
                    Log.d("dateCheck", r.candleDateTimeKst)
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

    fun fetchPreviousCandles(
        symbol: String,
        tabIndex: Int,
        onComplete: (Int) -> Unit // 추가된 데이터 개수 콜백
    ) {
        val unitList = listOf(1, 3, 5, 15, 30, 60, 240)
        val unitMin = unitList[tabIndex]
        val windowMs = unitMin * 60_000L

        val oldestKey = candleMap.keys.minOrNull() ?: return
        val oldestDate = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.KOREA)
            .format(java.util.Date(oldestKey - windowMs))

        viewModelScope.launch {
            val past = repo.getMinuteCandle(symbol, unitMin, to = oldestDate)
            val before = past.filter {
                val key = (it.timestamp / windowMs) * windowMs
                !candleMap.containsKey(key)
            }
            Log.d("dateCheck2", "[이후로드] ${past.size}개")
            before.forEach { r ->
                val key = (r.timestamp / windowMs) * windowMs
                Log.d("dateCheck2", r.candleDateTimeKst)
                val hhmm = r.candleDateTimeKst.substring(11, 16)
                candleMap[key] = AggregatedCandle(
                    time = key.toFloat(),
                    open = r.openingPrice,
                    high = r.highPrice,
                    low = r.lowPrice,
                    close = r.tradePrice,
                    label = hhmm
                )
            }
            postMinuteData()
            onComplete(before.size) // 몇 개 추가됐는지 콜백
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
                    idx.toFloat(), // x: 연속 인덱스
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


