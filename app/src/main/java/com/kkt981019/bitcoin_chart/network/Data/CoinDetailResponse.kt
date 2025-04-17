package com.kkt981019.bitcoin_chart.network.Data

import com.google.gson.annotations.SerializedName

data class CoinDetailResponse(
    @SerializedName("trade_price") val trade_price: String,
    @SerializedName("signed_change_rate") val signed_change_rate: String,
    @SerializedName("signed_change_price") val signed_change_price: String,
    @SerializedName("change") val change: String,
)

data class OrderbookResponse(
    val orderbook_units: List<OrderbookUnit>
)

data class OrderbookUnit(
    @SerializedName("ask_price") val askPrice: Double,
    @SerializedName("bid_price") val bidPrice: Double,
    @SerializedName("ask_size") val askSize: Double,
    @SerializedName("bid_size") val bidSize: Double
)

data class WebSocketTradeResponse(
    /** 메시지 종류 (“trade”) */
    @SerializedName("type") val type: String,
    /** 마켓 코드 (ex. “KRW-BTC”) */
    @SerializedName("code") val code: String,
    /** 체결 가격 */
    @SerializedName("trade_price") val tradePrice: Double,
    /** 체결량 */
    @SerializedName("trade_volume") val tradeVolume: Double,
    /** 매수/매도 구분 (“ASK” 또는 “BID”) */
    @SerializedName("ask_bid") val askBid: String,
    /** 전일 종가 */
    @SerializedName("prev_closing_price") val prevClosingPrice: Double,
    /** 전일 대비 (“RISE”, “EVEN”, “FALL”) */
    @SerializedName("change") val change: String,
    /** 부호 없는 전일 대비 값 */
    @SerializedName("change_price") val changePrice: Double,
    /** 체결 일자 (UTC 기준, yyyy-MM-dd) */
    @SerializedName("trade_date") val tradeDate: String,
    /** 체결 시각 (UTC 기준, HH:mm:ss) */
    @SerializedName("trade_time") val tradeTime: String,
    /** 체결 타임스탬프 (밀리초) */
    @SerializedName("trade_timestamp") val tradeTimestamp: Long,
    /** 타임스탬프 (밀리초) */
    @SerializedName("timestamp") val timestamp: Long,
    /** 체결 번호 (유니크) */
    @SerializedName("sequential_id") val sequentialId: Long,
    /** 최우선 매도 호가 */
    @SerializedName("best_ask_price") val bestAskPrice: Double,
    /** 최우선 매도 잔량 */
    @SerializedName("best_ask_size") val bestAskSize: Double,
    /** 최우선 매수 호가 */
    @SerializedName("best_bid_price") val bestBidPrice: Double,
    /** 최우선 매수 잔량 */
    @SerializedName("best_bid_size") val bestBidSize: Double,
    /** 스트림 타입 (“SNAPSHOT” 또는 “REALTIME”) */
    @SerializedName("stream_type") val streamType: String
)

data class RetrofitTradeResponse (
    /** 종목 코드 */
    @SerializedName("market") val market: String,
    /** 체결 일자 */
    @SerializedName("trade_date_utc") val tradeDateUtc: String,
    /** 체결 시각 */
    @SerializedName("trade_time_utc") val tradeTimeUtc: String,
    /** 체결 타임스탬프 */
    @SerializedName("timestamp") val timestamp: Long,
    /** 체결 가격 */
    @SerializedName("trade_price") val tradePrice: Double,
    /** 체결량 */
    @SerializedName("trade_volume") val tradeVolume: Double,
    /** 전일 종가 */
    @SerializedName("prev_closing_price") val prevClosingPrice: Double,
    /** 변화량 */
    @SerializedName("change_price") val changePrice: Double,
    /** 매도/매수 */
    @SerializedName("ask_bid") val askBid: String,
    /** 체결 번호 */
    @SerializedName("sequential_id") val sequentialId: Long,
)

