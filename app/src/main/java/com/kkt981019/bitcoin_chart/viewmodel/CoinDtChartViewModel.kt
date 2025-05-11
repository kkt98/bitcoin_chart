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
import javax.inject.Inject

@HiltViewModel
class CoinDtChartViewModel @Inject constructor(
    private val repo: RetrofitRepository,
    private val webSocketRepository: WebSocketRepository
) : ViewModel() {

    private val _minuteCandleState = MutableLiveData<List<CandleEntry>>(emptyList())
    val minuteCandleState: LiveData<List<CandleEntry>> = _minuteCandleState

    private val _dayCandleState = MutableLiveData<List<RetrofitCandleResponse>>(emptyList())
    val dayCandleState: LiveData<List<RetrofitCandleResponse>> = _dayCandleState

    private var socket: WebSocket? = null
    private val candleMap = mutableMapOf<Long, AggregatedCandle>()

    fun fetchCandles(symbol: String, tabIndex: Int) {
        // 1) 기존 소켓 닫고, 맵 초기화
        socket?.close(1000, "switch interval")
        candleMap.clear()

        if (tabIndex in 0..6) {
            val unitMin = listOf(1,3,5,15,30,60,240)[tabIndex]
            val windowMs = unitMin * 60_000L

            viewModelScope.launch {
                // 2) Retrofit으로 과거 분봉 불러와 candleMap에 채우기
                val past = repo.getMinuteCandle(symbol, unitMin)
                past.forEach { r ->
                    // timestamp 필드를 ms단위로 가지고 있다고 가정
                    val key = (r.timestamp / windowMs) * windowMs
                    candleMap[key] = AggregatedCandle(
                        time  = key.toFloat(),
                        open  = r.openingPrice,
                        high  = r.highPrice,
                        low   = r.lowPrice,
                        close = r.tradePrice
                    )
                }

                // 3) 초기값 방출
                _minuteCandleState.postValue(
                    candleMap.entries
                        .sortedBy { it.key }
                        .mapIndexed { idx, (_, c) ->
                            CandleEntry(
                                idx.toFloat(),
                                c.high.toFloat(),
                                c.low.toFloat(),
                                c.open.toFloat(),
                                c.close.toFloat()
                            )
                        }
                )

                // 4) WebSocket으로 실시간 집계 시작
                startAggregation(symbol, unitMin)
            }

        } else {
            // 24h 탭은 기존대로 REST
            viewModelScope.launch {
                _dayCandleState.postValue(repo.getDayCandle(symbol))
            }
        }
    }

    private fun startAggregation(symbol: String, intervalMin: Int) {
        val windowMs = intervalMin * 60_000L
        socket = webSocketRepository.starCandleSocket(symbol) { ws ->
            val key = (ws.timestamp / windowMs) * windowMs

            val agg = candleMap.getOrPut(key) {
                AggregatedCandle(
                    time  = key.toFloat(),
                    open  = ws.openPrice,
                    high  = ws.highPrice,
                    low   = ws.lowPrice,
                    close = ws.tradePrice
                )
            }
            // 봉 갱신
            agg.high  = maxOf(agg.high, ws.highPrice)
            agg.low   = minOf(agg.low, ws.lowPrice)
            agg.close = ws.tradePrice

            // 새 리스트 방출
            _minuteCandleState.postValue(
                candleMap.entries
                    .sortedBy { it.key }
                    .mapIndexed { idx, (_, c) ->
                        CandleEntry(
                            idx.toFloat(),
                            c.high.toFloat(),
                            c.low.toFloat(),
                            c.open.toFloat(),
                            c.close.toFloat()
                        )
                    }
            )
        }
    }

    data class AggregatedCandle(
        val time: Float,
        var open: Double,
        var high: Double,
        var low: Double,
        var close: Double
    )
}

