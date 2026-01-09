package com.kkt981019.bitcoin_chart.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kkt981019.bitcoin_chart.repository.MyCoinRepository
import com.kkt981019.bitcoin_chart.repository.MyPageRepository
import com.kkt981019.bitcoin_chart.repository.WebSocketRepository
import com.kkt981019.bitcoin_chart.room.mycoin.MyCoinEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import okhttp3.WebSocket
import javax.inject.Inject

@HiltViewModel
class MyPageViewModel  @Inject constructor(
    private val myPageRepository: MyPageRepository,
    private val myCoinRepository: MyCoinRepository,
    private val webSocketRepository: WebSocketRepository
) : ViewModel() {

    // 보유 잔액
    private val _balance = MutableStateFlow(0L)
    val balance: StateFlow<Long> = _balance.asStateFlow()

    // StateFlow 로 보유코인 스트림
    val myCoins: StateFlow<List<MyCoinEntity>> =
        myCoinRepository
            .getAllCoinsFlow()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.Eagerly,
                initialValue = emptyList()
            )

    // 실시간 현재가(심볼 , 가격)
    private val priceMap = mutableStateMapOf<String, Double>()
    private var tickerSocket: WebSocket? = null

    var totalBuyAmount by mutableStateOf(0.0)
        private set
    var totalEvalAmount by mutableStateOf(0.0)
        private set
    var totalProfit by mutableStateOf(0.0)
        private set
    var totalProfitRate by mutableStateOf(0.0)
        private set
    var totalAsset by mutableStateOf(0.0)
        private set

    init {
        // 잔액 초기화
        viewModelScope.launch {
            myPageRepository.initBalanceIfNeeded()
            _balance.value = myPageRepository.getUserMoney()
        }

        // ✅ 코인 목록 Flow 를 계속 구독하면서
        //    - 소켓 재구독
        //    - 요약값 재계산
        viewModelScope.launch {
            myCoins.collect { coins ->
                restartTickerSocket(coins)
                recomputeSummary(coins)
            }
        }
    }

    fun refreshBalance() {
        viewModelScope.launch {
            _balance.value = myPageRepository.getUserMoney()
            recomputeSummary(myCoins.value)
        }
    }

    fun onCharge(amount: Long) {
        viewModelScope.launch {
            myPageRepository.charge(amount)
            _balance.value = myPageRepository.getUserMoney()
            recomputeSummary(myCoins.value)
        }
    }

    fun onSpend(amount: Long) {
        viewModelScope.launch {
            myPageRepository.spend(amount)
            _balance.value = myPageRepository.getUserMoney()
            recomputeSummary(myCoins.value)
        }
    }

    private fun restartTickerSocket(coins: List<MyCoinEntity>) {
        tickerSocket?.close(1000, "MyPage closed")

        if (coins.isEmpty()) return

        val markets = coins.map { it.symbol }

        tickerSocket = webSocketRepository.startTickerSocket(markets) { ws ->
            val price = ws.trade_price.toDoubleOrNull() ?: return@startTickerSocket

            viewModelScope.launch {
                priceMap[ws.code] = price
                // 실시간 가격 반영해서 요약값 다시 계산
                recomputeSummary(myCoins.value)
            }
        }
    }

    /** 상단 요약 박스용 값 재계산 */
    private fun recomputeSummary(coins: List<MyCoinEntity>) {

        val buy = coins.sumOf { coin ->
            coin.amount * coin.avgPrice
        }

        val eval = coins.sumOf { coin ->
            val currentPrice = priceMap[coin.symbol] ?: coin.avgPrice
            coin.amount * currentPrice
        }



        val profit = eval - buy
        val rate = if (buy > 0.0) (profit / buy) * 100.0 else 0.0

        totalBuyAmount = buy
        totalEvalAmount = eval
        totalProfit = profit
        totalProfitRate = rate
        totalAsset = _balance.value + eval
    }

    fun getCurrentPrice(symbol: String, avgPrice: Double): Double {
        return priceMap[symbol] ?: avgPrice
    }

    override fun onCleared() {
        super.onCleared()
        tickerSocket?.close(1000, "MyPageViewModel cleared")
    }
}
