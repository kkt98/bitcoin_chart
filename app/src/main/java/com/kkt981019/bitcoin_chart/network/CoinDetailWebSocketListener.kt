package com.kkt981019.bitcoin_chart.network

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.kkt981019.bitcoin_chart.network.Data.WebSocketCandleResponse
import com.kkt981019.bitcoin_chart.network.Data.CoinDetailResponse
import com.kkt981019.bitcoin_chart.network.Data.OrderbookResponse
import com.kkt981019.bitcoin_chart.network.Data.WebSocketTradeResponse
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString

class CoinDetailWebSocketListener(
    private val marketCode: String,
    private val onCoinDetailUpdate: (CoinDetailResponse) -> Unit,
    private val onOrderbookUpdate: (OrderbookResponse) -> Unit,
    private val onTradeUpdate: (WebSocketTradeResponse) -> Unit,
    private val onCandleUpdate: (WebSocketCandleResponse) -> Unit
) : WebSocketListener()
{

    override fun onOpen(webSocket: WebSocket, response: Response) {
        // 티커, 호가, 캔들 등 필요한 구독 메시지를 보내기
        val subscribeMessage = """
            [
              {"ticket": "detail-ticket"},
              {"type": "ticker", "codes": ${Gson().toJson(listOf(marketCode))}},
              {"type": "orderbook", "codes": ${Gson().toJson(listOf(marketCode))}},
              {"type": "trade", "codes": ${Gson().toJson(listOf(marketCode))}},
              {"type":"candle.1s", "codes":${Gson().toJson(listOf(marketCode))}},
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
                // 1) trade
                jsonObject.has("best_bid_price") -> {
                    val trade = Gson().fromJson(text, WebSocketTradeResponse::class.java)
                    onTradeUpdate(trade)
                }
                // 2) orderbook
                jsonObject.has("orderbook_units") -> {
                    val book = Gson().fromJson(text, OrderbookResponse::class.java)
                    onOrderbookUpdate(book)
                }
                // 3) ticker (type 도 검증)
                jsonObject.get("type")?.asString == "ticker" -> {
                    val ticker = Gson().fromJson(text, CoinDetailResponse::class.java)
                    onCoinDetailUpdate(ticker)
                }
                // 4) candle
                jsonObject.get("type")?.asString == "candle.1s" -> {
                    val candle = Gson().fromJson(text, WebSocketCandleResponse::class.java)
                    onCandleUpdate(candle)
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
