package com.kkt981019.bitcoin_chart.viewmodel

import android.util.Log
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
            Log.d("asdasd1", markets.toString())
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    // allMarkets의 값이 emit된 후 coinList를 계산
    val coinList = allMarkets.switchMap { markets ->
        liveData(Dispatchers.IO) {
            if (markets!!.isNotEmpty()) {
                val krwMarkets = markets.filter { it.market.startsWith("KRW-") }
                val krwMarketSymbols = krwMarkets.map { it.market }
                // 티커 데이터 가져오기 (null 안전하게 처리)
                val tickerList = repository.getAllPrice(krwMarketSymbols) ?: emptyList()

                Log.d("asdasd3", tickerList.toString())
                val coins = krwMarkets.map { market ->

                    // 해당 market에 맞는 티커 데이터 찾기
                    val ticker = tickerList.find { it.market == market.market }
                    CoinData(
                        koreanName = market.koreanName,
                        englishName = market.englishName,
                        symbol = market.market,
                        currentPrice = "",
                        tradePrice = ticker?.trade_price?.toDouble(),
                        changeRate = ticker?.signed_change_rate?.toDouble(),
                        volume = ticker?.acc_trade_price_24h?.toDouble()
                    )
                }
                emit(coins)
            } else {
                emit(emptyList())
            }
        }
    }
}
