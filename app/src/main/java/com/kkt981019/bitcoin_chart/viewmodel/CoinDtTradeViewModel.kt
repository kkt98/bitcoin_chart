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

    // 체결리스트 저장용
    private val _tradeState = MutableLiveData<List<WebSocketTradeResponse>>(emptyList())
    val tradeState: LiveData<List<WebSocketTradeResponse>> = _tradeState

    // 일봉리스트 저장용
    private val _dayCandleState = MutableLiveData<List<WebSocketCandleResponse>>(emptyList())
    val dayCandleState: LiveData<List<WebSocketCandleResponse>> = _dayCandleState


    // WebSocket 인스턴스 보관
    private var webSocket: WebSocket? = null

    fun startTrade(marketCode: String) {
        // 2) 기존 소켓 연결이 있으면 닫기
        webSocket?.close(1000, "re-subscribing")

        viewModelScope.launch {
            val restTrades = retrofitRepository.getTrade(marketCode)
            _tradeState.postValue(restTrades.map { it.toWS() })

            webSocket = webSocketRepository.startTradeSocket(
                marketCode = marketCode,
                onTradeUpdate = { trade ->
                    val current = _tradeState.value.orEmpty()
                    _tradeState.postValue((listOf(trade) + current).take(100))
                }
            )
        }
    }

    fun startCandle(marketCode: String) {
        // 기존 소켓 연결이 있으면 닫기
        webSocket?.close(1000, "re-subscribing")

        viewModelScope.launch {
            val restDays = retrofitRepository.getDayCandle(marketCode)
            _dayCandleState.postValue(restDays.map { it.toWS() })

            webSocket = webSocketRepository.starCandleSocket(
                marketCode = marketCode,
                onCandleUpdate = { candle ->
                    val current = _dayCandleState.value.orEmpty()
                    val newDate = candle.candleDateTimeUtc.take(10)

                    // 같은 날짜의 기존 캔들이 있으면 꺼내고, 없으면 null
                    val existing = current.firstOrNull { it.candleDateTimeUtc.take(10) == newDate }

                    // 누적된 캔들 생성 (기존이 없으면 새로 받은 candle 그대로)
                    val updatedCandle = existing?.copy(
                        candleAccTradeVolume = existing.candleAccTradeVolume + candle.candleAccTradeVolume,
                        tradePrice = candle.tradePrice
                    ) ?: candle

                    // 기존 리스트에서 같은 날짜는 제외하고
                    val others = current.filter { it.candleDateTimeUtc.take(10) != newDate }

                    // 새로운 데이터를 앞으로, 뒤에 이전 데이터
                    val merged = (listOf(updatedCandle) + others)

                    _dayCandleState.postValue(merged)
                }
            )
        }
    }

    override fun onCleared() {
        webSocket?.close(1000, "ViewModel cleared")
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
    type                   = "candle",
    code                   = market,
    candleDateTimeUtc      = candleDateTimeUtc,
    candleDateTimeKst      = candleDateTimeKst,
    openPrice              = 0.0,
    highPrice              = 0.0,
    lowPrice               = 0.0,
    tradePrice             = tradePrice,
    candleAccTradePrice    = candleAccTradePrice,
    candleAccTradeVolume   = candleAccTradeVolume,
    unit                   = "1d"
)