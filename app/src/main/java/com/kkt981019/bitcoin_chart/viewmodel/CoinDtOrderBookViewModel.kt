package com.kkt981019.bitcoin_chart.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kkt981019.bitcoin_chart.network.Data.CoinDetailResponse
import com.kkt981019.bitcoin_chart.network.Data.OrderbookResponse
import com.kkt981019.bitcoin_chart.network.Data.WebsocketResponse
import com.kkt981019.bitcoin_chart.repository.RetrofitRepository
import com.kkt981019.bitcoin_chart.repository.WebSocketRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import okhttp3.WebSocket
import javax.inject.Inject

@HiltViewModel
class CoinDtOrderBookViewModel @Inject constructor(
    private val webSocketRepository: WebSocketRepository
) : ViewModel() {

    // Ticker 데이터를 저장할 LiveData
    private val _tickerState = MutableLiveData<WebsocketResponse?>(null)
    val tickerState: LiveData<WebsocketResponse?> = _tickerState

    // 주문호가 데이터 저장용
    private val _orderbookState = MutableLiveData<OrderbookResponse?>(null)
    val orderbookState: LiveData<OrderbookResponse?> = _orderbookState

    private var tickerSocket: WebSocket? = null
    private var orderBookSocket: WebSocket? = null

    fun startOrderBook(symbol: String) {
        orderBookSocket?.close(1000, "ViewModel cleared")

        viewModelScope.launch {
            orderBookSocket = webSocketRepository.startOrderBookSocket(
                marketCode = symbol,
                onOrderbookUpdate = { orderBook ->
                    _orderbookState.postValue(orderBook)
                }
            )
        }
    }

    fun startTicker(symbol: String) {
        tickerSocket?.close(1000, "ViewModel cleared")

        viewModelScope.launch {
            tickerSocket = webSocketRepository.startTickerSocket(
                marketCodes = listOf(symbol),
                onTickerUpdate = { coinDetail ->
                    _tickerState.postValue(coinDetail)
                }
            )

        }
    }

    override fun onCleared() {
        tickerSocket?.close(1000, "ViewModel cleared")
        orderBookSocket?.close(1000, "ViewModel cleared")
        super.onCleared()
    }
}