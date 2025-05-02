package com.kkt981019.bitcoin_chart.network.websoketlistener

import com.google.gson.Gson
import com.google.gson.JsonParser
import com.kkt981019.bitcoin_chart.network.Data.WebSocketTradeResponse
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString

class TradeWebSocketListener(
    private val marketCode: String,
    private val onTradeUpdate: (WebSocketTradeResponse) -> Unit
) : WebSocketListener() {
    override fun onOpen(ws: WebSocket, resp: Response) {
        val msg = """
        [
          {"ticket":"trade-ticket"},
          {"type":"trade","codes":[${Gson().toJson(marketCode)}]}
        ]
        """.trimIndent()
        ws.send(msg)
    }
    override fun onMessage(ws: WebSocket, text: String) {
        try {
            val json = JsonParser().parse(text).asJsonObject
            if (json.has("best_bid_price")) {
                val trade = Gson().fromJson(text, WebSocketTradeResponse::class.java)
                onTradeUpdate(trade)
            }
        } catch(e: Exception){ e.printStackTrace() }
    }
    override fun onMessage(ws: WebSocket, bytes: ByteString) = onMessage(ws, bytes.utf8())
    override fun onClosing(ws: WebSocket, code: Int, reason: String) { ws.close(1000, null) }
    override fun onFailure(ws: WebSocket, t: Throwable, resp: Response?) = t.printStackTrace()
}
