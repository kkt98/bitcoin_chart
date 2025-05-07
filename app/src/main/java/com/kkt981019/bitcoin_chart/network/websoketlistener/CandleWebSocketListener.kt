package com.kkt981019.bitcoin_chart.network.websoketlistener

import com.google.gson.Gson
import com.google.gson.JsonParser
import com.kkt981019.bitcoin_chart.network.Data.WebSocketCandleResponse
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString

class CandleWebSocketListener(
    private val marketCode: String,
    private val interval: String = "1s",  // ex: "1s", "1m", "1h", "1d"
    private val onCandleUpdate: (WebSocketCandleResponse) -> Unit
) : WebSocketListener() {
    override fun onOpen(ws: WebSocket, resp: Response) {
        val msg = """
        [
          {"ticket":"candle-ticket"},
          {"type":"candle.$interval","codes":[${Gson().toJson(marketCode)}]}
        ]
        """.trimIndent()
        ws.send(msg)
    }
    override fun onMessage(ws: WebSocket, text: String) {
        try {
            val json = JsonParser().parse(text).asJsonObject
            if (json.get("type")?.asString == "candle.$interval") {
                val candle = Gson().fromJson(text, WebSocketCandleResponse::class.java)
                onCandleUpdate(candle)
            }
        } catch(e: Exception){ e.printStackTrace() }
    }
    override fun onMessage(ws: WebSocket, bytes: ByteString) = onMessage(ws, bytes.utf8())
    override fun onClosing(ws: WebSocket, code: Int, reason: String) { ws.close(1000, null) }
    override fun onFailure(ws: WebSocket, t: Throwable, resp: Response?) = t.printStackTrace()
}