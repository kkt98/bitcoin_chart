package com.kkt981019.bitcoin_chart.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kkt981019.bitcoin_chart.repository.TradeHistoryRepository
import com.kkt981019.bitcoin_chart.room.trade_history.TradeHistoryEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TradeHistoryViewModel @Inject constructor(
    private val tradeHistoryRepository: TradeHistoryRepository
) : ViewModel() {

    // 특정 심볼의 거래내역 리스트
    private val _trades = MutableStateFlow<List<TradeHistoryEntity>>(emptyList())
    val trades: StateFlow<List<TradeHistoryEntity>> = _trades

    // 특정 코인의 거래내역 불러오기
    fun loadTrades(symbol: String) {
        viewModelScope.launch {
            _trades.value = tradeHistoryRepository.getTrades(symbol)
        }
    }

    // 매수 / 매도 시 거래내역 추가
    fun addTrade(
        symbol: String,
        side: String,    // "BUY" 또는 "SELL"
        price: Double,
        amount: Double,
        total: Double
    ) {
        viewModelScope.launch {
            tradeHistoryRepository.addTrade(symbol, side, price, amount, total)
            // 같은 코인 화면이라면 추가 후 다시 로딩
            _trades.value = tradeHistoryRepository.getTrades(symbol)
        }
    }

    // 해당 코인 거래내역 전부 삭제
    fun deleteHistory() {
        viewModelScope.launch {
            tradeHistoryRepository.getAllDelete()
        }
    }
}