package com.kkt981019.bitcoin_chart.repository

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kkt981019.bitcoin_chart.room.mycoin.MyCoinDao
import com.kkt981019.bitcoin_chart.room.mycoin.MyCoinEntity
import javax.inject.Inject

class MyCoinRepository @Inject constructor(private val myCoinDao: MyCoinDao) {

    // 매수 처리 (평균 단가 자동 계산)
    suspend fun buy(symbol: String, qty: Double, price: Double) {
        val existing = myCoinDao.getCoin(symbol)

        if (existing == null) {
            // 처음 구매한 코인
            myCoinDao.upsert(
                MyCoinEntity(
                    symbol = symbol,
                    amount = qty,
                    avgPrice = price
                )
            )
        } else {
            // 추가 매수 → 평균 단가 재계산
            val newAmount = existing.amount + qty
            val newAvgPrice =
                (existing.amount * existing.avgPrice + qty * price) / newAmount

            myCoinDao.upsert(
                existing.copy(
                    amount = newAmount,
                    avgPrice = newAvgPrice
                )
            )
        }
    }

    // 매도 처리 (남은 수량 <= 0 → 삭제)
    suspend fun sell(symbol: String, qty: Double) {
        val existing = myCoinDao.getCoin(symbol) ?: return

        val remaining = existing.amount - qty

        if (remaining <= 0) {
            myCoinDao.delete(existing) // 전량 매도 → 기록 삭제
        } else {
            myCoinDao.updateAmount(symbol, remaining)
        }
    }

    // 단일 코인 조회
    suspend fun getCoin(symbol: String) = myCoinDao.getCoin(symbol)

    // 전체 보유 코인 조회
    suspend fun getAllCoins() = myCoinDao.getAllCoins()
}