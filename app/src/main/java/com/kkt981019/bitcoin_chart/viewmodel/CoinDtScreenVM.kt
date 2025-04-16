package com.kkt981019.bitcoin_chart.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kkt981019.bitcoin_chart.network.Data.CoinDetailResponse
import com.kkt981019.bitcoin_chart.network.Data.OrderbookResponse
import com.kkt981019.bitcoin_chart.network.Data.TradeResponse
import com.kkt981019.bitcoin_chart.repository.WebSocketRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import okhttp3.WebSocket
import javax.inject.Inject

@HiltViewModel
class CoinDTScreenVM @Inject constructor(
    private val repository: WebSocketRepository
) : ViewModel() {

    // Ticker 데이터를 저장할 StateFlow
    private val _tickerState = MutableLiveData<CoinDetailResponse?>(null)
    val tickerState: LiveData<CoinDetailResponse?> = _tickerState

    // 주문호가 데이터 (예: OrderbookResponse)
    private val _orderbookState = MutableLiveData<OrderbookResponse?>(null)
    val orderbookState: LiveData<OrderbookResponse?> = _orderbookState

    private val _tradeState = MutableLiveData<TradeResponse?>(null)
    val tradeState: LiveData<TradeResponse?> = _tradeState

    // 웹소켓 객체 (나중에 종료할 때 사용)
    private var webSocket: WebSocket? = null

    fun startDetailWebSocket(marketCode: String) {
        // 기존 연결이 있으면 종료
        webSocket?.close(1000, "New detail screen request")

        // 새 웹소켓 연결
        webSocket = repository.startDetailSocket(
            marketCode = marketCode,
            onCoinDetailUpdate = { coinDetail ->
                Log.d("asdasda4", coinDetail.toString())
                // 수신된 티커 데이터를 StateFlow에 저장
                _tickerState.postValue(coinDetail)
            },
            onOrderbookUpdate = { orderbook ->
                _orderbookState.postValue(orderbook)
            },
            onTradeUpdate = { trade ->
                Log.d("asdasd1111", trade.toString())
                _tradeState.postValue(trade)
            }
        )
    }

    override fun onCleared() {
        webSocket?.close(1000, "ViewModel cleared")
        super.onCleared()
    }
}