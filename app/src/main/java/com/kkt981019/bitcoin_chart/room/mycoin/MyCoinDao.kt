package com.kkt981019.bitcoin_chart.room.mycoin

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface MyCoinDao {

    // 코인 추가 or 업데이트(중복 symbol이면 덮어쓰기)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(myCoin: MyCoinEntity)

    // 코인 삭제
    @Delete
    suspend fun delete(myCoin: MyCoinEntity)

    // 특정 코인 조회
    @Query("SELECT * FROM my_coin WHERE symbol = :symbol LIMIT 1")
    suspend fun getCoin(symbol: String): MyCoinEntity?

    // 전체 코인 조회
    @Query("SELECT * FROM my_coin ORDER BY symbol ASC")
    suspend fun getAllCoins(): List<MyCoinEntity>

    @Query("SELECT * FROM my_coin ORDER BY symbol ASC")
    fun getAllCoinsFlow(): kotlinx.coroutines.flow.Flow<List<MyCoinEntity>>

    // 수량만 업데이트
    @Query("UPDATE my_coin SET amount = :amount WHERE symbol = :symbol")
    suspend fun updateAmount(symbol: String, amount: Double)
}