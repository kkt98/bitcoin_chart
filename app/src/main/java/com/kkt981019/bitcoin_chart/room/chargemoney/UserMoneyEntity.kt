package com.kkt981019.bitcoin_chart.room.chargemoney

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_money")
data class UserMoneyEntity(
    @PrimaryKey
    val id: Int = 0,  // 한 명만 쓸 거니까 0 고정
    val money: Long             // 현재 잔액 (KRW)
)