package com.kkt981019.bitcoin_chart.room

import androidx.room.Database
import androidx.room.RoomDatabase
import com.kkt981019.bitcoin_chart.room.chargemoney.UserMoneyDao
import com.kkt981019.bitcoin_chart.room.chargemoney.UserMoneyEntity
import com.kkt981019.bitcoin_chart.room.favorite.FavoriteDao
import com.kkt981019.bitcoin_chart.room.favorite.FavoriteEntity
import com.kkt981019.bitcoin_chart.room.mycoin.MyCoinDao
import com.kkt981019.bitcoin_chart.room.mycoin.MyCoinEntity
import com.kkt981019.bitcoin_chart.room.trade_history.TradeHistoryDao
import com.kkt981019.bitcoin_chart.room.trade_history.TradeHistoryEntity

@Database(entities = [FavoriteEntity::class,
                     UserMoneyEntity::class,
                        MyCoinEntity::class,
                        TradeHistoryEntity::class], version = 4)
abstract class AppDatabase: RoomDatabase() {

    abstract fun favoriteDao(): FavoriteDao

    abstract fun userMoneyDao(): UserMoneyDao

    abstract fun myCoinDao(): MyCoinDao

    abstract fun tradeHistoryDao(): TradeHistoryDao

}