package com.kkt981019.bitcoin_chart.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_markets")
data class FavoriteEntity(
    @PrimaryKey
    val market: String,        // "KRW-BTC", "KRW-XRP" 처럼 Upbit 마켓 코드 (식별자)
    val koreanName: String,    // "비트코인", "리플" 등 화면에 바로 표시할 한글명
    val englishName: String,   // "BTC/ KRW", "XRP/ KRW" 등 간단 표시용
    val addedAt: Long = System.currentTimeMillis()
)