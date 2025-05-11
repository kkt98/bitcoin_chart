package com.kkt981019.bitcoin_chart.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kkt981019.bitcoin_chart.network.Data.RetrofitCandleResponse
import com.kkt981019.bitcoin_chart.network.Data.RetrofitTradeResponse
import com.kkt981019.bitcoin_chart.network.Data.WebSocketCandleResponse
import com.kkt981019.bitcoin_chart.network.Data.WebSocketTradeResponse
import com.kkt981019.bitcoin_chart.repository.RetrofitRepository
import com.kkt981019.bitcoin_chart.repository.WebSocketRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import okhttp3.WebSocket
import javax.inject.Inject

@HiltViewModel
class CoinDtTradeViewModel @Inject constructor(
    private val webSocketRepository: WebSocketRepository,
    private val retrofitRepository: RetrofitRepository
) : ViewModel() {

    // 체결용 소켓과 일봉용 소켓을 각각 보관
    private var tradeSocket: WebSocket? = null
    private var candleSocket: WebSocket? = null

    // 체결 리스트
    private val _tradeState = MutableLiveData<List<WebSocketTradeResponse>>(emptyList())
    val tradeState: LiveData<List<WebSocketTradeResponse>> = _tradeState

    // 일봉 리스트
    private val _dayCandleState = MutableLiveData<List<WebSocketCandleResponse>>(emptyList())
    val dayCandleState: LiveData<List<WebSocketCandleResponse>> = _dayCandleState

    fun startTrade(marketCode: String) {
        // 이전 trade 전용 소켓만 닫는다
        tradeSocket?.close(1000, "re-subscribing-trade")

        viewModelScope.launch {
            val restTrades = retrofitRepository.getTrade(marketCode)
            _tradeState.postValue(restTrades.map { it.toWS() })

            tradeSocket = webSocketRepository.startTradeSocket(
                marketCode = marketCode,
                onTradeUpdate = { trade ->
                    val current = _tradeState.value.orEmpty()
                    _tradeState.postValue((listOf(trade) + current).take(100))
                }
            )
        }
    }

    fun startCandle(marketCode: String) {
        // 이전 candle 전용 소켓만 닫는다
        candleSocket?.close(1000, "re-subscribing-candle")

        viewModelScope.launch {
            val restDays = retrofitRepository.getDayCandle(marketCode)
            _dayCandleState.postValue(restDays.map { it.toWS() })

            candleSocket = webSocketRepository.starCandleSocket(
                marketCode = marketCode,
                onCandleUpdate = { candle ->
                    val current = _dayCandleState.value.orEmpty()
                    val newDate = candle.candleDateTimeUtc.take(10)
                    val existing = current.firstOrNull { it.candleDateTimeUtc.take(10) == newDate }
                    val updated = existing?.copy(
                        candleAccTradeVolume = existing.candleAccTradeVolume + candle.candleAccTradeVolume,
                        tradePrice = candle.tradePrice
                    ) ?: candle
                    val merged = listOf(updated) + current.filter { it.candleDateTimeUtc.take(10) != newDate }
                    _dayCandleState.postValue(merged)
                }
            )
        }
    }

    override fun onCleared() {
        tradeSocket?.close(1000, "ViewModel cleared")
        candleSocket?.close(1000, "ViewModel cleared")
        super.onCleared()
    }
}


// Retrofit → WebSocket DTO 변환 확장함수
private fun RetrofitTradeResponse.toWS(): WebSocketTradeResponse = WebSocketTradeResponse(
    type               = "trade",
    code               = market,
    tradePrice         = tradePrice,
    tradeVolume        = tradeVolume,
    askBid             = askBid,
    prevClosingPrice   = prevClosingPrice,
    change             = "",
    changePrice        = changePrice,
    tradeDate          = tradeDateUtc,
    tradeTime          = tradeTimeUtc,
    tradeTimestamp     = timestamp,
    timestamp          = 0L,
    sequentialId       = sequentialId,
    bestAskPrice       = 0.0,
    bestAskSize        = 0.0,
    bestBidPrice       = 0.0,
    bestBidSize        = 0.0,
    streamType         = "SNAPSHOT"
)

private fun RetrofitCandleResponse.toWS(): WebSocketCandleResponse = WebSocketCandleResponse(
    type = "candle",
    code = market,
    candleDateTimeUtc = candleDateTimeUtc,
    candleDateTimeKst = candleDateTimeKst,
    openPrice = 0.0,
    highPrice = 0.0,
    lowPrice = 0.0,
    tradePrice = tradePrice,
    candleAccTradePrice = candleAccTradePrice,
    candleAccTradeVolume = candleAccTradeVolume,
    unit = "1d",
    timestamp = timestamp
)