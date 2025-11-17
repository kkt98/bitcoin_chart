package com.kkt981019.bitcoin_chart.repository

import com.kkt981019.bitcoin_chart.room.chargemoney.UserMoneyDao
import com.kkt981019.bitcoin_chart.room.chargemoney.UserMoneyEntity
import javax.inject.Inject

class MyPageRepository @Inject constructor(private val userMoneyDao: UserMoneyDao) {

    suspend fun getUserMoney(): Long {
        return (userMoneyDao.getUserMoney()?.money ?: 0L)
    }

    suspend fun initBalanceIfNeeded() {
        val current = userMoneyDao.getUserMoney()
        if (current == null) {
            userMoneyDao.upsertBalance(UserMoneyEntity(money = 0L))
        }
    }

     suspend fun charge(amount: Long) {
         userMoneyDao.increase(amount)
    }

     suspend fun spend(amount: Long) {
         userMoneyDao.decrease(amount)
    }

}