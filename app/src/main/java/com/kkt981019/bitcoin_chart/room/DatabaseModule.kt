package com.kkt981019.bitcoin_chart.room

import android.content.Context
import androidx.room.Room
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
    fun provideFavoriteDatabase(
        @ApplicationContext ctx: Context
    ): FavoriteDatabase {
        return Room.databaseBuilder(
            ctx,
            FavoriteDatabase::class.java,
            "favorite_db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideFavoriteDao(
        db: FavoriteDatabase
    ): FavoriteDao = db.favoriteDao()
}