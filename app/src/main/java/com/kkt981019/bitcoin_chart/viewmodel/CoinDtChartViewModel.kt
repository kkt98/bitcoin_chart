package com.kkt981019.bitcoin_chart.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kkt981019.bitcoin_chart.network.Data.RetrofitCandleResponse
import com.kkt981019.bitcoin_chart.network.Data.WebSocketCandleResponse
import com.kkt981019.bitcoin_chart.repository.RetrofitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CoinDtChartViewModel @Inject constructor(
    private val retrofitRepository: RetrofitRepository
) : ViewModel() {

    // 일봉리스트 저장용
    private val _dayCandleState = MutableLiveData<List<RetrofitCandleResponse>>(emptyList())
    val dayCandleState: LiveData<List<RetrofitCandleResponse>> = _dayCandleState

    private val _minuteCandleState = MutableLiveData<List<RetrofitCandleResponse>>(emptyList())
    val minuteCandleState: LiveData<List<RetrofitCandleResponse>> = _minuteCandleState

    // 탭 인덱스를 받아서 분봉(unit) 또는 일봉을 불러오는 함수
    fun fetchCandles(market: String, tabIndex: Int) {
        viewModelScope.launch {
            when (tabIndex) {
                // 0~6: 분봉 (1m,3m,5m,15m,30m,1h,4h)
                in 0..6 -> {
                    val unit = listOf(1, 3, 5, 15, 30, 60, 240)[tabIndex]
                    val data = retrofitRepository.getMinuteCandle(market, unit)
                    _minuteCandleState.postValue(data)
                }
                // 7: 24h → 일봉 API 사용
                7 -> {
                    val data = retrofitRepository.getDayCandle(market)
                    _dayCandleState.postValue(data)
                }
            }
        }
    }

}