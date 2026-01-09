package com.kkt981019.bitcoin_chart.repository

import com.kkt981019.bitcoin_chart.room.trade_history.TradeHistoryDao
import com.kkt981019.bitcoin_chart.room.trade_history.TradeHistoryEntity
import javax.inject.Inject

class TradeHistoryRepository @Inject constructor(
    private val tradeHistoryDao: TradeHistoryDao
) {
    suspend fun addTrade(
        symbol: String,
        side: String,
        price: Double,
        amount: Double,
        total: Double
    ) {
        val trade = TradeHistoryEntity(
            symbol = symbol,
            type = side,
            price = price,
            amount = amount,
            total = total,
            time = System.currentTimeMillis()
        )
        tradeHistoryDao.insert(trade)
    }

    suspend fun deleteHistory(symbol: String) {
        tradeHistoryDao.deleteBySymbol(symbol)
    }

    suspend fun getTrades(symbol: String) =
        tradeHistoryDao.getTradesBySymbol(symbol)

    suspend fun getAllDelete() =
        tradeHistoryDao.getAllDelete()


}