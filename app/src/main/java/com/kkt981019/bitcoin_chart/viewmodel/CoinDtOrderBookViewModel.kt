package com.kkt981019.bitcoin_chart.viewmodel

import androidx.lifecycle.ViewModel
import com.kkt981019.bitcoin_chart.repository.RetrofitRepository
import com.kkt981019.bitcoin_chart.repository.WebSocketRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CoinDtOrderBookViewModel @Inject constructor(
    private val webSocketRepository: WebSocketRepository,
    private val retrofitRepository: RetrofitRepository
) : ViewModel() {
}