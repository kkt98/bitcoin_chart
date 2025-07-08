package com.kkt981019.bitcoin_chart.repository

import com.kkt981019.bitcoin_chart.room.FavoriteDao
import com.kkt981019.bitcoin_chart.room.FavoriteEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavoriteRepository @Inject constructor(private val dao: FavoriteDao)  {

    suspend fun getAllFavorites(): List<FavoriteEntity> =
        dao.getAllFavorites()

    /**
     * 즐겨찾기 추가
     * @param market   Upbit 마켓 코드 (e.g. "KRW-BTC")
     * @param kor      한글명 (e.g. "비트코인")
     * @param eng      영문명 (e.g. "BTC/KRW")
     */
    suspend fun addFavorite(market: String, kor: String, eng: String) {
        dao.insert(FavoriteEntity(market, kor, eng))
    }

    /**
     * 즐겨찾기 제거
     * @param market   primary key 값만 있으면 삭제됩니다.
     */
    suspend fun removeFavorite(market: String) {
        dao.delete(FavoriteEntity(market, "", ""))
    }

}