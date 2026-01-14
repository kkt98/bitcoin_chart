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
class MyPageViewModel @Inject constructor(
    private val myPageRepository: MyPageRepository,      // 잔액 관련(충전/차감/조회) Repository
    private val myCoinRepository: MyCoinRepository,      // 보유 코인(Room) Repository
    private val webSocketRepository: WebSocketRepository // 업비트 티커 웹소켓 Repository
) : ViewModel() {

    // -------------------------------
    // 1) 보유 KRW 잔액 (StateFlow)
    // -------------------------------

    // 내부에서만 값을 변경할 수 있는 MutableStateFlow
    private val _balance = MutableStateFlow(0L)

    // 외부(Compose 등)에서는 읽기 전용 StateFlow 로 사용
    val balance: StateFlow<Long> = _balance.asStateFlow()

    // -------------------------------
    // 2) 보유 코인 리스트 (Room Flow → StateFlow)
    // -------------------------------

    // Room 의 getAllCoinsFlow() 를 StateFlow 로 변환
    // DB 내용이 바뀌면 자동으로 새로운 리스트가 emit 됨
    val myCoins: StateFlow<List<MyCoinEntity>> =
        myCoinRepository
            .getAllCoinsFlow()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.Eagerly,
                initialValue = emptyList()
            )

    // -------------------------------
    // 3) 실시간 가격 / 소켓 관리
    // -------------------------------

    // 실시간 현재가 맵 (symbol → trade_price)
    private val priceMap = mutableStateMapOf<String, Double>()

    // 현재 열려 있는 티커 웹소켓
    private var tickerSocket: WebSocket? = null

    // -------------------------------
    // 4) 상단 요약 박스에 들어갈 값들
    // -------------------------------

    // 총 매수 금액 (각 코인: amount * avgPrice 의 합)
    var totalBuyAmount by mutableStateOf(0.0)
        private set

    // 총 평가 금액 (각 코인: amount * 현재가 의 합)
    var totalEvalAmount by mutableStateOf(0.0)
        private set

    // 총 평가 손익 (총 평가 금액 - 총 매수 금액)
    var totalProfit by mutableStateOf(0.0)
        private set

    // 총 수익률 (평가손익 / 총 매수 * 100)
    var totalProfitRate by mutableStateOf(0.0)
        private set

    // 총 보유 자산 (보유 KRW + 총 평가 금액)
    var totalAsset by mutableStateOf(0.0)
        private set

    // -------------------------------
    // 5) 초기화 블록 (ViewModel 생성 시 한 번 실행)
    // -------------------------------
    init {
        // 5-1) 잔액 초기 세팅
        viewModelScope.launch {
            // user_money 테이블이 비어 있으면 초기값 넣어주기
            myPageRepository.initBalanceIfNeeded()
            // DB 에서 현재 잔액 읽어와서 StateFlow 에 반영
            _balance.value = myPageRepository.getUserMoney()
        }

        // 5-2) 보유 코인 Flow 구독
        viewModelScope.launch {
            // myCoins(StateFlow)를 collect → coins 가 바뀔 때마다 콜백
            myCoins.collect { coins ->
                // 코인 목록이 바뀌면 티커 웹소켓 구독 목록 다시 세팅
                restartTickerSocket(coins)
                // 요약(총매수, 총평가, 수익률, 총보유자산 등) 다시 계산
                recomputeSummary(coins)
            }
        }
    }

    // -------------------------------
    // 6) 외부에서 호출하는 API들
    // -------------------------------

    // (옵션) 잔액만 강제로 다시 불러오고 싶을 때
    fun refreshBalance() {
        viewModelScope.launch {
            _balance.value = myPageRepository.getUserMoney()
            // 잔액이 바뀌었으니, 총 보유자산 다시 계산
            recomputeSummary(myCoins.value)
        }
    }

    // KRW 충전 (예: “KRW 충전” 버튼)
    fun onCharge(amount: Long) {
        viewModelScope.launch {
            // DB 상의 user_money 증가
            myPageRepository.charge(amount)
            // 최신 잔액 다시 가져와서 StateFlow 갱신
            _balance.value = myPageRepository.getUserMoney()
            // 잔액 변경 반영해서 요약 값 재계산
            recomputeSummary(myCoins.value)
        }
    }

    // 매수/매도 시 실제로 돈을 쓰는 부분
    fun onSpend(amount: Long) {
        viewModelScope.launch {
            // DB 상의 user_money 감소
            myPageRepository.spend(amount)
            // 최신 잔액 다시 가져와서 StateFlow 갱신
            _balance.value = myPageRepository.getUserMoney()
            // 잔액 변경 반영해서 요약 값 재계산
            recomputeSummary(myCoins.value)
        }
    }

    // -------------------------------
    // 7) 티커 웹소켓 재구독 로직
    // -------------------------------

    // 보유 코인 목록이 바뀔 때마다 호출됨
    private fun restartTickerSocket(coins: List<MyCoinEntity>) {
        // 기존에 열려 있던 소켓이 있으면 정리
        tickerSocket?.close(1000, "MyPage closed")

        if (coins.isEmpty()) return  // 보유 코인이 없으면 구독할 필요 없음

        // 업비트 마켓 코드 리스트 (예: ["KRW-BTC", "KRW-ETH", ...])
        val markets = coins.map { it.symbol }

        // 새 티커 소켓 열기
        tickerSocket = webSocketRepository.startTickerSocket(markets) { ws ->
            // 웹소켓에서 받은 trade_price 가 String 이라면 Double 로 변환
            val price = ws.trade_price.toDoubleOrNull() ?: return@startTickerSocket

            viewModelScope.launch {
                // 심볼(code)에 해당하는 현재가 갱신
                priceMap[ws.code] = price
                // 실시간 가격 업데이트에 맞게 요약값 다시 계산
                recomputeSummary(myCoins.value)
            }
        }
    }

    // -------------------------------
    // 8) 상단 요약 박스 재계산 로직
    // -------------------------------

    /** 상단 요약 박스용 값 재계산 */
    private fun recomputeSummary(coins: List<MyCoinEntity>) {

        // 총 매수 금액 = Σ (각 코인 보유수량 * 매수평균가)
        val buy = coins.sumOf { coin ->
            coin.amount * coin.avgPrice
        }

        // 총 평가 금액 = Σ (각 코인 보유수량 * 현재가)
        // 현재가가 아직 안들어온 코인은 avgPrice 를 임시 현재가로 사용
        val eval = coins.sumOf { coin ->
            val currentPrice = priceMap[coin.symbol] ?: coin.avgPrice
            coin.amount * currentPrice
        }

        // 평가손익 = 총 평가금액 - 총 매수금액
        val profit = eval - buy

        // 수익률 = (평가손익 / 총매수) * 100 (총매수 0이면 0%)
        val rate = if (buy > 0.0) (profit / buy) * 100.0 else 0.0

        // 계산 결과를 Compose state 에 반영 → UI 자동 갱신
        totalBuyAmount   = buy
        totalEvalAmount  = eval
        totalProfit      = profit
        totalProfitRate  = rate
        totalAsset       = _balance.value + eval  // 총 보유자산 = 현금 + 평가금액
    }

    // -------------------------------
    // 9) UI 에서 현재가가 필요할 때 사용
    // -------------------------------

    // 가격맵에서 해당 심볼 현재가를 가져오고,
    // 아직 안 들어온 경우 avgPrice 를 대신 사용
    fun getCurrentPrice(symbol: String, avgPrice: Double): Double {
        return priceMap[symbol] ?: avgPrice
    }

    // -------------------------------
    // 10) ViewModel 정리 시 소켓 종료
    // -------------------------------
    override fun onCleared() {
        super.onCleared()
        tickerSocket?.close(1000, "MyPageViewModel cleared")
    }
}
