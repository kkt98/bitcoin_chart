package com.kkt981019.bitcoin_chart.room.mycoin

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "my_coin")
data class MyCoinEntity(
    @PrimaryKey
    val symbol: String,  // 예: "KRW-BTC"
    val amount: Double,  // 보유 수량
    val avgPrice: Double, // 매수 평균 단가
    val koreanName: String
)
