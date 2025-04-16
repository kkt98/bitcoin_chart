package com.kkt981019.bitcoin_chart.network

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.kkt981019.bitcoin_chart.network.Data.CoinDetailResponse
import com.kkt981019.bitcoin_chart.network.Data.OrderbookResponse
import com.kkt981019.bitcoin_chart.network.Data.TradeResponse
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString

class CoinDetailWebSocketListener(
    private val marketCode: String,
    private val onCoinDetailUpdate: (CoinDetailResponse) -> Unit,
    private val onOrderbookUpdate: (OrderbookResponse) -> Unit,
    private val onTradeUpdate: (TradeResponse) -> Unit
) : WebSocketListener()
{

    override fun onOpen(webSocket: WebSocket, response: Response) {
        // 티커, 호가, 캔들 등 필요한 구독 메시지를 보내기
        val subscribeMessage = """
            [
              {"ticket": "detail-ticket"},
              {"type": "ticker", "codes": ${Gson().toJson(listOf(marketCode))}},
              {"type": "orderbook", "codes": ${Gson().toJson(listOf(marketCode))}},
              {"type": "trade", "codes": ${Gson().toJson(listOf(marketCode))}}
            ]
        """.trimIndent()
        webSocket.send(subscribeMessage)
    }

//    {"type": "candle", "codes": ["$marketCode"], "interval": "1m"}


    override fun onMessage(webSocket: WebSocket, text: String) {
        try {
            val jsonObject = JsonParser().parse(text).asJsonObject
            Log.d("asdasdasd33", jsonObject.toString())

            when {
                // 1) trade 메시지 먼저
                jsonObject.has("best_bid_price") -> {
                    val trade = Gson().fromJson(text, TradeResponse::class.java)
                    Log.d("asdasdasd22", trade.toString())
                    onTradeUpdate(trade)
                }
                // 2) orderbook
                jsonObject.has("orderbook_units") -> {
                    val orderbook = Gson().fromJson(text, OrderbookResponse::class.java)
                    onOrderbookUpdate(orderbook)
                }
                // 3) ticker
                jsonObject.has("trade_price") -> {
                    val coinDetail = Gson().fromJson(text, CoinDetailResponse::class.java)
                    onCoinDetailUpdate(coinDetail)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
        onMessage(webSocket, bytes.utf8())
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        webSocket.close(1000, null)
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        t.printStackTrace()
    }
}
