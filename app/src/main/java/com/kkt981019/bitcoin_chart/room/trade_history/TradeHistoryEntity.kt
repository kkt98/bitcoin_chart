package com.kkt981019.bitcoin_chart.room.trade_history

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trade_history")
data class TradeHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val symbol: String,      // 예: "KRW-DOGE"
    val type: String,        // "BUY" 또는 "SELL"
    val price: Double,       // 체결 가격 (1코인 가격)
    val amount: Double,      // 체결 수량
    val total: Double,       // 체결 총액 = price * amount
    val time: Long           // 체결 시각 (System.currentTimeMillis())
)