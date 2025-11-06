package com.kkt981019.bitcoin_chart.room.chargemoney

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface UserMoneyDao {

    // 현재 잔액 가져오기
    @Query("SELECT * FROM user_money WHERE id = 0")
    suspend fun getUserMoney(): UserMoneyEntity?

    // 현재 잔액을 DB에 저장하거나 업데이트
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBalance(balance: UserMoneyEntity)

    // 금액 더하기 (충전)
    @Query("UPDATE user_money SET money = money + :amount WHERE id = 0")
    suspend fun increase(amount: Long)

    // 금액 빼기 (매수)
    @Query("UPDATE user_money SET money = money - :amount WHERE id = 0")
    suspend fun decrease(amount: Long)
}