package com.kkt981019.bitcoin_chart.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kkt981019.bitcoin_chart.repository.MyCoinRepository
import com.kkt981019.bitcoin_chart.repository.MyPageRepository
import com.kkt981019.bitcoin_chart.repository.WebSocketRepository
import com.kkt981019.bitcoin_chart.room.mycoin.MyCoinEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import okhttp3.WebSocket
import javax.inject.Inject

@HiltViewModel
class MyPageViewModel  @Inject constructor(
    private val myPageRepository: MyPageRepository,
    private val myCoinRepository: MyCoinRepository,
    private val webSocketRepository: WebSocketRepository
) : ViewModel(){

    // 보유 잔액
    var balance by mutableStateOf(0L)
        private set

    // 보유코인
    var myCoins by mutableStateOf<List<MyCoinEntity>>(emptyList())
        private set

    // 실시간 현재가(심볼 , 가격)
    private var priceMap: MutableMap<String, Double> = mutableMapOf()
    private var tickerSocket: WebSocket? = null

    // 총 매수 금액 (보유수량 * 매수평균가 총합)
    var totalBuyAmount by mutableStateOf(0.0)
        private set

    // 총 평가 금액 (보유수량 * 현재가 총합)
    var totalEvalAmount by mutableStateOf(0.0)
        private set

    // 평가 손익 (총 평가 - 총 매수)
    var totalProfit by mutableStateOf(0.0)
        private set

    // 수익률 (평가손익 / 총매수 * 100)
    var totalProfitRate by mutableStateOf(0.0)
        private set

    // 총 보유자산 (보유 KRW + 총 평가금액)
    var totalAsset by mutableStateOf(0.0)
        private set

    init {
        viewModelScope.launch {
            myPageRepository.initBalanceIfNeeded()
            balance = myPageRepository.getUserMoney()

            loadMyCoins()

            // 보유 코인 심볼들로 웹소켓 구독 시작
            startTickerSocket()

            recomputeSummary()
        }

    }

    fun onCharge(amount: Long) {
        viewModelScope.launch {
            myPageRepository.charge(amount)
            balance = myPageRepository.getUserMoney()
            recomputeSummary()
        }
    }

    fun onSpend(amount: Long) {
        viewModelScope.launch {
            myPageRepository.spend(amount)
            balance = myPageRepository.getUserMoney()
            recomputeSummary()
        }
    }

    /** Room 에서 내 모든 코인 불러오기 */
    private suspend fun loadMyCoins() {
        myCoins = myCoinRepository.getAllCoins()
    }

    private fun startTickerSocket() {
        // 기존 소켓 정리
        tickerSocket?.close(1000, "MyPage closed")

        if (myCoins.isEmpty()) return

        val markets = myCoins.map { it.symbol }

        tickerSocket = webSocketRepository.startTickerSocket(markets) { ws ->
            // ws.trade_price 가 String이면 toDoubleOrNull() 사용
            val price = ws.trade_price.toDoubleOrNull() ?: return@startTickerSocket

            // 실시간 가격 맵 업데이트
            priceMap[ws.code] = price

            // 가격 바뀔 때마다 요약 재계산
            viewModelScope.launch {
                recomputeSummary()
            }
        }
    }

    /** 상단 요약 박스용 값 재계산 */
    private fun recomputeSummary() {
        val coins = myCoins

        // 총 매수 금액 = 각 코인 (보유수량 * 매수평균가) 합
        val buy = coins.sumOf { coin ->
            coin.amount * coin.avgPrice
        }

        // 총 평가 금액 = 각 코인 (보유수량 * 현재가) 합
        // 현재가가 아직 안 들어온 코인은 avgPrice 를 임시로 사용
        val eval = coins.sumOf { coin ->
            val currentPrice = priceMap[coin.symbol] ?: coin.avgPrice
            coin.amount * currentPrice
        }

        val profit = eval - buy
        val rate = if (buy > 0.0) (profit / buy) * 100.0 else 0.0

        totalBuyAmount = buy // 총 매수 금액
        totalEvalAmount = eval // 총 평가 금액
        totalProfit = profit // 평가 손익
        totalProfitRate = rate // 수익률
        totalAsset = balance + eval // 총 보유자산
    }

    override fun onCleared() {
        super.onCleared()
        tickerSocket?.close(1000, "MyPageViewModel cleared")
    }

}