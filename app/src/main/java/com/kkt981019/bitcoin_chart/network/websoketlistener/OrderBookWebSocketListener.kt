package com.kkt981019.bitcoin_chart.network.websoketlistener

import com.google.gson.Gson
import com.google.gson.JsonParser
import com.kkt981019.bitcoin_chart.network.Data.OrderbookResponse
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString

class OrderBookWebSocketListener(
    private val marketCode: String,
    private val onOrderbookUpdate: (OrderbookResponse) -> Unit
) : WebSocketListener() {
    override fun onOpen(ws: WebSocket, resp: Response) {
        val msg = """
        [
          {"ticket":"orderbook-ticket"},
          {"type": "orderbook", "codes": ${Gson().toJson(listOf(marketCode))}}
        ]
        """.trimIndent()
        ws.send(msg)
    }
    override fun onMessage(ws: WebSocket, text: String) {
        try {
            val json = JsonParser().parse(text).asJsonObject
            if (json.has("orderbook_units")) {
                val book = Gson().fromJson(text, OrderbookResponse::class.java)
                onOrderbookUpdate(book)
            }
        } catch(e: Exception){ e.printStackTrace() }
    }
    override fun onMessage(ws: WebSocket, bytes: ByteString) = onMessage(ws, bytes.utf8())
    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        webSocket.close(1000, null)
    }
    override fun onFailure(ws: WebSocket, t: Throwable, resp: Response?) = t.printStackTrace()
}