package com.kkt981019.bitcoin_chart.viewmodel

import androidx.lifecycle.ViewModel
import com.kkt981019.bitcoin_chart.repository.WebSocketRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CoinDtTradeViewModel @Inject constructor(
    private val webSocketRepository: WebSocketRepository
) : ViewModel() {
}