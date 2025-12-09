package com.kkt981019.bitcoin_chart.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kkt981019.bitcoin_chart.repository.MyCoinRepository
import com.kkt981019.bitcoin_chart.room.mycoin.MyCoinEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyCoinViewModel @Inject constructor(
    private val myCoinRepository: MyCoinRepository
) : ViewModel() {

    // 전체 보유 코인 목록
    private val _myCoins = MutableStateFlow<List<MyCoinEntity>>(emptyList())
    val myCoins: StateFlow<List<MyCoinEntity>> = _myCoins.asStateFlow()

    // 현재 화면에서 보고 있는 "특정 심볼" 코인 1개 정보
    private val _currentCoin = MutableStateFlow<MyCoinEntity?>(null)
    val currentCoin: StateFlow<MyCoinEntity?> = _currentCoin.asStateFlow()

    init {
        refreshMyCoins()
    }

    // 전체 보유 코인 목록 갱신
    private fun refreshMyCoins() {
        viewModelScope.launch {
            _myCoins.value = myCoinRepository.getAllCoins()
        }
    }

    // 매수 처리
    fun onBuy(symbol: String, qty: Double, price: Double, korName: String, engName: String) {
        viewModelScope.launch {
            myCoinRepository.buy(symbol, qty, price, korName, engName)
            // 전체 리스트 갱신
            refreshMyCoins()
            // 방금 매수한 심볼에 대한 단일 코인 정보도 다시 가져오기
            loadCoin(symbol)
        }
    }

    fun onSell(symbol: String, qty: Double) {
        viewModelScope.launch {
            myCoinRepository.sell(symbol = symbol, qty = qty)
            refreshMyCoins()
            loadCoin(symbol)
        }
    }

    // 특정 심볼 코인 한 개 로드해서 상태에 저장
    fun loadCoin(symbol: String) {
        viewModelScope.launch {
            _currentCoin.value = myCoinRepository.getCoin(symbol)
        }
    }
}
