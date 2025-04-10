package com.kkt981019.bitcoin_chart.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kkt981019.bitcoin_chart.network.Data.CoinDetailResponse
import com.kkt981019.bitcoin_chart.repository.WebSocketRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import okhttp3.WebSocket
import javax.inject.Inject

@HiltViewModel
class CoinDTScreenVM @Inject constructor(
    private val repository: WebSocketRepository
) : ViewModel() {

    // Ticker 데이터를 저장할 StateFlow (Compose에서 관찰)
    private val _tickerState = MutableLiveData<CoinDetailResponse?>(null)
    val tickerState: LiveData<CoinDetailResponse?> = _tickerState

    // 웹소켓 객체 (나중에 종료할 때 사용)
    private var webSocket: WebSocket? = null

    fun startDetailWebSocket(marketCode: String) {
        // 기존 연결이 있으면 종료
        webSocket?.close(1000, "New detail screen request")

        // 새 웹소켓 연결: Ticker + Orderbook을 동시에 받거나, Ticker만 받도록 설정
        webSocket = repository.startDetailSocket(
            marketCode = marketCode,
            onCoinDetailUpdate = { coinDetail ->
                Log.d("asdasda4", coinDetail.toString())
                // 수신된 티커 데이터를 StateFlow에 저장
                _tickerState.postValue(coinDetail)
            },
            onOrderbookUpdate = {
                // 주문호가 데이터가 필요하다면 처리
            }
        )
    }

    override fun onCleared() {
        webSocket?.close(1000, "ViewModel cleared")
        super.onCleared()
    }
}