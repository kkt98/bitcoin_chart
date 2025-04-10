package com.kkt981019.bitcoin_chart.network

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.kkt981019.bitcoin_chart.network.Data.CoinDetailResponse
import com.kkt981019.bitcoin_chart.network.Data.OrderbookResponse
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString

class CoinDetailWebSocketListener(
    private val marketCode: String,
    private val onCoinDetailUpdate: (CoinDetailResponse) -> Unit,
    private val onOrderbookUpdate: (OrderbookResponse) -> Unit) : WebSocketListener()
{

    override fun onOpen(webSocket: WebSocket, response: Response) {
        // 티커, 호가, 캔들 등 필요한 구독 메시지를 보내기
        val subscribeMessage = """
            [
              {"ticket": "detail-ticket"},
              {"type": "ticker", "codes": ${Gson().toJson(listOf(marketCode))}},
              {"type": "orderbook", "codes": ${Gson().toJson(listOf(marketCode))}},
            ]
        """.trimIndent()
        webSocket.send(subscribeMessage)
    }

//    {"type": "candle", "codes": ["$marketCode"], "interval": "1m"}


    override fun onMessage(webSocket: WebSocket, text: String) {
        try {
            // 먼저 JSON 전체를 파싱해서 어떤 종류의 메시지인지 확인합니다.
            val jsonObject = JsonParser().parse(text).asJsonObject


            if (jsonObject.has("trade_price")) {
                // 티커 데이터로 간주 (CoinDetailResponse)
                val coinDetail = Gson().fromJson(text, CoinDetailResponse::class.java)
                onCoinDetailUpdate(coinDetail)
            } else if (jsonObject.has("orderbook_units")) {
                // 주문호가 데이터로 간주 (OrderbookResponse)
                val orderbook = Gson().fromJson(text, OrderbookResponse::class.java)
                onOrderbookUpdate(orderbook)
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
