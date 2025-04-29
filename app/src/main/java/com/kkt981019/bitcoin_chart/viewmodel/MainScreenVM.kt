package com.kkt981019.bitcoin_chart.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kkt981019.bitcoin_chart.network.Data.CoinData
import com.kkt981019.bitcoin_chart.repository.FavoriteRepository
import com.kkt981019.bitcoin_chart.repository.RetrofitRepository
import com.kkt981019.bitcoin_chart.repository.WebSocketRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.WebSocket
import javax.inject.Inject

// MainScreenVM.kt
@HiltViewModel
class MainScreenVM @Inject constructor(
    private val repository: RetrofitRepository,
    private val webSocketRepository: WebSocketRepository,
    private val favoriteRepository: FavoriteRepository
) : ViewModel() {

    private val _coins = MutableLiveData<List<CoinData>>(emptyList())
    val coins: MutableLiveData<List<CoinData>> = _coins

    private var currentSocket: WebSocket? = null

    /** 탭이 바뀔 때마다 호출하세요 */
    fun fetchCoins(tabIndex: Int) {
        // 1) 이전 소켓 정리
        currentSocket?.close(1000, "Tab changed")

        viewModelScope.launch(Dispatchers.IO) {
            val list = when (tabIndex) {
                in 0..2 -> {
                    // KRW/BTC/USDT 탭
                    val prefixes = listOf("KRW-","BTC-","USDT-")
                    val prefix = prefixes[tabIndex]
                    val markets = repository.getAllMarket()
                        ?.filter { it.market.startsWith(prefix) }

                    val symbols = markets?.map { it.market }
                    val tickers = repository.getAllPrice(symbols).orEmpty()

                    markets?.map { m ->
                        val t = tickers.find { it.market == m.market }
                        CoinData(
                            koreanName = m.koreanName,
                            englishName= m.englishName,
                            symbol     = m.market,
                            tradePrice = t?.trade_price?.toDoubleOrNull(),
                            changeRate = t?.signed_change_rate?.toDoubleOrNull(),
                            volume     = t?.acc_trade_price_24h?.toDoubleOrNull(),
                            signed     = t?.signed_change_price?.toDoubleOrNull(),
                            change     = t?.change ?: ""
                        )
                    }?.sortedByDescending { it.volume ?: 0.0 }
                }
                3 -> {
                    // 관심 탭
                    val favs = favoriteRepository.getAllFavorites()
                    if (favs.isEmpty()) {
                        emptyList()
                    } else {
                        val symbols = favs.map { it.market }
                        val tickers = repository.getAllPrice(symbols).orEmpty()
                        favs.mapNotNull { fav ->
                            tickers.find { it.market == fav.market }?.let { t ->
                                CoinData(
                                    koreanName = fav.koreanName,
                                    englishName= fav.englishName,
                                    symbol     = fav.market,
                                    tradePrice = t.trade_price?.toDoubleOrNull(),
                                    changeRate = t.signed_change_rate?.toDoubleOrNull(),
                                    volume     = t.acc_trade_price_24h?.toDoubleOrNull(),
                                    signed     = t.signed_change_price?.toDoubleOrNull(),
                                    change     = t.change ?: ""
                                )
                            }
                        }
                    }
                }
                else -> emptyList()
            }

            // 2) 초기 데이터 방출
            _coins.postValue(list)

            // 3) WebSocket 업데이트 시작 (관심 탭에도 동작하나, 원치 않으면 단순 return)
            startWebSocketUpdates(list)
        }
    }

    private fun startWebSocketUpdates(initial: List<CoinData>?) {
        currentSocket = webSocketRepository.startSocket(initial?.map { it.symbol }) { ws ->
            // WebSocket 콜백은 백그라운드 스레드!
            val updated = _coins.value.orEmpty().map { coin ->
                if (coin.symbol == ws.code) coin.copy(
                    tradePrice = ws.trade_price.toDoubleOrNull() ?: coin.tradePrice,
                    changeRate = ws.signed_change_rate.toDoubleOrNull() ?: coin.changeRate,
                    volume     = ws.acc_trade_price_24h.toDoubleOrNull() ?: coin.volume,
                    signed     = ws.signed_change_price.toDoubleOrNull() ?: coin.signed,
                    change     = ws.change
                ) else coin
            }
            _coins.postValue(updated)
        }
    }

    override fun onCleared() {
        super.onCleared()
        currentSocket?.close(1000, "ViewModel cleared")
    }
}


