package com.kkt981019.bitcoin_chart.room

import androidx.room.Database
import androidx.room.RoomDatabase
import com.kkt981019.bitcoin_chart.room.chargemoney.UserMoneyDao
import com.kkt981019.bitcoin_chart.room.chargemoney.UserMoneyEntity
import com.kkt981019.bitcoin_chart.room.favorite.FavoriteDao
import com.kkt981019.bitcoin_chart.room.favorite.FavoriteEntity
import com.kkt981019.bitcoin_chart.room.mycoin.MyCoinDao
import com.kkt981019.bitcoin_chart.room.mycoin.MyCoinEntity

@Database(entities = [FavoriteEntity::class,
                     UserMoneyEntity::class,
                        MyCoinEntity::class], version = 3)
abstract class AppDatabase: RoomDatabase() {

    abstract fun favoriteDao(): FavoriteDao

    abstract fun userMoneyDao(): UserMoneyDao

    abstract fun myCoinDao(): MyCoinDao

}