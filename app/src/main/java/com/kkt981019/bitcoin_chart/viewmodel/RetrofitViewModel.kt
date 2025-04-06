package com.kkt981019.bitcoin_chart.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.switchMap
import com.kkt981019.bitcoin_chart.network.Data.CoinData
import com.kkt981019.bitcoin_chart.repository.RetrofitRepository
import com.kkt981019.bitcoin_chart.repository.WebSocketRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import okhttp3.WebSocket
import javax.inject.Inject

@HiltViewModel
class RetrofitViewModel @Inject constructor(
    private val repository: RetrofitRepository,
    private val webSocketRepository: WebSocketRepository
) : ViewModel() {

    // 초기 데이터를 담을 MutableLiveData
    private val _coinList = MutableLiveData<List<CoinData>>()
    val coinList: LiveData<List<CoinData>> get() = _coinList

    // 처음 Retrofit에서 코인의 symbol, 한국이름 영어이름 가져오기
    val allMarkets = liveData(Dispatchers.IO) {
        try {
            val markets = repository.getAllMarket()
            emit(markets)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    // 스크린에서 KRW, BTC, USDT 탭 선택
    private val _marketPrefix = MutableLiveData("KRW-")
    val marketPrefix: LiveData<String> = _marketPrefix
    fun setMarketPrefix(prefix: String) {
        _marketPrefix.value = prefix
        Log.d("asdasd3", prefix)
    }

    private var currentSocket: WebSocket? = null


    init {
        // 초기 데이터 로드(Retrofit 사용)
        val coinLoader = marketPrefix.switchMap { prefix ->
            allMarkets.switchMap { markets ->
                liveData(Dispatchers.IO) {
                    if (!markets.isNullOrEmpty()) {

                        val filteredMarkets = markets.filter { it.market.startsWith(prefix) }

                        val marketSymbols = filteredMarkets.map { it.market }

                        val tickerList = repository.getAllPrice(marketSymbols) ?: emptyList()

                        val coins = filteredMarkets.map { market ->
                            val ticker = tickerList.find { it.market == market.market }
                            CoinData(
                                koreanName = market.koreanName,
                                englishName = market.englishName,
                                symbol = market.market,
                                tradePrice = ticker?.trade_price?.toDouble(),
                                changeRate = ticker?.signed_change_rate?.toDouble(),
                                volume = ticker?.acc_trade_price_24h?.toDouble(),
                                signed = ticker?.signed_change_price?.toDouble(),
                                change = ticker?.change ?: ""
                            )
                        }.sortedByDescending { it.volume ?: 0.0 }
                        // 초기 데이터 할당
                        _coinList.postValue(coins)
                        // 웹소켓 업데이트 시작
                        startWebSocketUpdates(coins)
                        emit(coins)
                    } else {
                        Log.d("asdasdas5", ":null")
                        emit(emptyList())
                    }
                }
            }
        }
        // 구독하여 체인을 활성화
        coinLoader.observeForever {  }
    }

    //실시간 코인 업데이트 (WebSocket 사용)
    private fun startWebSocketUpdates(coinList: List<CoinData>) {
        // 기본적으로 전에 사용하던 웹 소켓 닫아주기
        currentSocket?.close(1000, "Switching market prefix")
        val marketCodes = coinList.map { it.symbol }
        currentSocket = webSocketRepository.startSocket(marketCodes) { websocketResponse ->

            // 기존 코인 리스트에서 업데이트 대상 코인을 찾아 갱신
            val currentList = _coinList.value ?: return@startSocket
            val updatedList = currentList.map { coin ->
                if (coin.symbol == websocketResponse.code) {
                    coin.copy(
                        tradePrice = websocketResponse.trade_price.toDoubleOrNull() ?: coin.tradePrice,
                        changeRate = websocketResponse.signed_change_rate.toDoubleOrNull() ?: coin.changeRate,
                        volume = websocketResponse.acc_trade_price_24h.toDoubleOrNull() ?: coin.volume,
                        signed = websocketResponse.signed_change_price.toDoubleOrNull() ?: coin.signed,
                        change = websocketResponse.change
                    )
                } else {
                    coin
                }
            }
            _coinList.postValue(updatedList)
        }
    }
}
