package com.kkt981019.bitcoin_chart.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kkt981019.bitcoin_chart.network.Data.CoinDetailResponse
import com.kkt981019.bitcoin_chart.network.Data.OrderbookResponse
import com.kkt981019.bitcoin_chart.network.Data.RetrofitTradeResponse
import com.kkt981019.bitcoin_chart.network.Data.WebSocketTradeResponse
import com.kkt981019.bitcoin_chart.repository.RetrofitRepository
import com.kkt981019.bitcoin_chart.repository.WebSocketRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import okhttp3.WebSocket
import javax.inject.Inject

@HiltViewModel
class CoinDTScreenVM @Inject constructor(
    private val webSocketRepository: WebSocketRepository,
    private val retrofitRepository: RetrofitRepository
) : ViewModel() {

    // Ticker 데이터를 저장할 StateFlow
    private val _tickerState = MutableLiveData<CoinDetailResponse?>(null)
    val tickerState: LiveData<CoinDetailResponse?> = _tickerState

    // 주문호가 데이터 (예: OrderbookResponse)
    private val _orderbookState = MutableLiveData<OrderbookResponse?>(null)
    val orderbookState: LiveData<OrderbookResponse?> = _orderbookState

    private val _tradeState =  MutableLiveData<List<WebSocketTradeResponse>>(emptyList())
    val tradeState: LiveData<List<WebSocketTradeResponse>> = _tradeState

    // 웹소켓 객체 (나중에 종료할 때 사용)
    private var webSocket: WebSocket? = null

    fun startDetailWebSocket(marketCode: String) {
        viewModelScope.launch {
            // 1) Retrofit으로 과거 100건 조회
            val history: List<RetrofitTradeResponse> = retrofitRepository.getTrade(marketCode)
            Log.d("CoinDTScreenVM", "Fetched history size=${history.size}")

            // 2) Retrofit 결과를 WebSocketTradeResponse 형식으로 매핑
            val initialTrades = history.map { rt ->
                WebSocketTradeResponse(
                    type = "",
                    code = rt.market,
                    tradePrice = rt.tradePrice,
                    tradeVolume = rt.tradeVolume,
                    askBid = rt.askBid,
                    tradeTime = rt.tradeTimeUtc,
                    prevClosingPrice = 0.0,
                    change = "",
                    changePrice = 0.0,
                    tradeDate = "",
                    tradeTimestamp = 0,
                    timestamp = 0,
                    sequentialId = 0,
                    bestAskSize = 0.0,
                    bestBidSize = 0.0,
                    bestBidPrice = 0.0,
                    bestAskPrice = 0.0,
                    streamType = ""
                )
            }
            // 초기값 세팅
            _tradeState.postValue(initialTrades)

            // 2) 기존 소켓 연결이 있으면 종료
            webSocket?.close(1000, "Re-subscribing")

            // 3) WebSocket 구독 시작
            webSocket = webSocketRepository.startDetailSocket(
                marketCode = marketCode,
                onCoinDetailUpdate = { coinDetail ->
                    _tickerState.postValue(coinDetail)
                },
                onOrderbookUpdate = { orderbook ->
                    _orderbookState.postValue(orderbook)
                },
                onTradeUpdate = { trade ->
                    // 새 체결을 맨 앞에 붙이고 최대 30건 유지
                    val current = _tradeState.value.orEmpty()
                    _tradeState.postValue((listOf(trade) + current).take(100))
                }
            )
        }

    }

    override fun onCleared() {
        webSocket?.close(1000, "ViewModel cleared")
        super.onCleared()
    }
}