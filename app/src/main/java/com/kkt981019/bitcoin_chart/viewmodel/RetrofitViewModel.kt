package com.kkt981019.bitcoin_chart.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.switchMap
import com.kkt981019.bitcoin_chart.network.Data.CoinData
import com.kkt981019.bitcoin_chart.repository.RetrofitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

@HiltViewModel
class RetrofitViewModel @Inject constructor(
    private val repository: RetrofitRepository
) : ViewModel() {

    // 전체 마켓 데이터를 가져오는 LiveData
    val allMarkets = liveData(Dispatchers.IO) {
        try {
            val markets = repository.getAllMarket()
            emit(markets)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    // 선택된 마켓 접두어 (기본은 "KRW-")
    private val _marketPrefix = MutableLiveData("KRW-")
    val marketPrefix: LiveData<String> = _marketPrefix

    fun setMarketPrefix(prefix: String) {
        _marketPrefix.value = prefix
    }

    val coinList: LiveData<List<CoinData>> = marketPrefix.switchMap { prefix ->
        allMarkets.switchMap { markets ->
            liveData(Dispatchers.IO) {
                if (markets != null && markets.isNotEmpty()) {
                    val filteredMarkets = markets.filter { it.market.startsWith(prefix) }
                    val marketSymbols = filteredMarkets.map { it.market }
                    val tickerList = repository.getAllPrice(marketSymbols) ?: emptyList()
                    val coins = filteredMarkets.map { market ->
                        // 해당 market에 맞는 티커 데이터 찾기
                        val ticker = tickerList.find { it.market == market.market }
                        CoinData(
                            koreanName = market.koreanName,
                            englishName = market.englishName,
                            symbol = market.market,
                            tradePrice = ticker?.trade_price?.toDouble(),
                            changeRate = ticker?.signed_change_rate?.toDouble(),
                            volume = ticker?.acc_trade_price_24h?.toDouble(),
                            signed = ticker?.signed_change_price?.toDouble()
                        )
                    }.sortedByDescending { it.volume ?: 0.0 } // 거래대금(volume)이 높은 순으로 정렬
                    emit(coins)
                } else {
                    emit(emptyList())
                }
            }
        }
    }
}
