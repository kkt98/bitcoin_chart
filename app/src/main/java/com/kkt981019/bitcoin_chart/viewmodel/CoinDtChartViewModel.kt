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
    private val repo: RetrofitRepository
) : ViewModel() {

    private val _dayCandleState    = MutableLiveData<List<RetrofitCandleResponse>>(emptyList())
    val dayCandleState: LiveData<List<RetrofitCandleResponse>> = _dayCandleState

    private val _minuteCandleState = MutableLiveData<List<RetrofitCandleResponse>>(emptyList())
    val minuteCandleState: LiveData<List<RetrofitCandleResponse>> = _minuteCandleState

    fun fetchCandles(market: String, tabIndex: Int) {
        viewModelScope.launch {
            when (tabIndex) {
                in 0..6 -> {
                    val unit = listOf(1,3,5,15,30,60,240)[tabIndex]
                    val allMinutes = repo.getMinuteCandle(market, unit)
                    // 오래된 → 최신 순으로 뒤집어서 방출
                    _minuteCandleState.postValue(allMinutes.asReversed())
                }
                7 -> {
                    val allDays = repo.getDayCandle(market)
                    _dayCandleState.postValue(allDays.asReversed())
                }
            }
        }
    }
}