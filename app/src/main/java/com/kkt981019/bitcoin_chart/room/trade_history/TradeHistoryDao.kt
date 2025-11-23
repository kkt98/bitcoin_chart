package com.kkt981019.bitcoin_chart.room.trade_history

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface TradeHistoryDao {

    @Insert
    suspend fun insert(trade: TradeHistoryEntity)

    @Query("DELETE FROM trade_history WHERE symbol = :symbol")
    suspend fun deleteBySymbol(symbol: String)

    @Query("SELECT * FROM trade_history WHERE symbol = :symbol ORDER BY time DESC")
    suspend fun getTradesBySymbol(symbol: String): List<TradeHistoryEntity>

    @Query("SELECT * FROM trade_history ORDER BY time DESC")
    suspend fun getAllTrades(): List<TradeHistoryEntity>

}