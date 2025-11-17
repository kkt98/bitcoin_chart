package com.kkt981019.bitcoin_chart.room

import android.content.Context
import androidx.room.Room
import com.kkt981019.bitcoin_chart.room.chargemoney.UserMoneyDao
import com.kkt981019.bitcoin_chart.room.favorite.FavoriteDao
import com.kkt981019.bitcoin_chart.room.mycoin.MyCoinDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext ctx: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            ctx,
            AppDatabase::class.java,
            "app_db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideFavoriteDao(
        db: AppDatabase
    ): FavoriteDao = db.favoriteDao()

    @Provides
    @Singleton
    fun provideUserMoneyDao(db: AppDatabase): UserMoneyDao = db.userMoneyDao()

    @Provides
    @Singleton
    fun provideMyCoinDao(db: AppDatabase): MyCoinDao = db.myCoinDao()
}