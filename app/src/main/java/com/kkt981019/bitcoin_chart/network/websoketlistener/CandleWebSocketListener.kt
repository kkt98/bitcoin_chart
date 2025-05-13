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
    private val interval: String, // 1s, 1m, 3m, 5m, 15m, 30m, 60m, 240m…
    private val onCandleUpdate: (WebSocketCandleResponse) -> Unit
) : WebSocketListener() {

    // candle.1m, candle.3m, …
    private val candleType = "candle.${interval}"

    override fun onOpen(ws: WebSocket, resp: Response) {
        // isOnlyRealtime: true 옵션도 추가해 주시면 REST 초기 로드 후 "실시간" 업데이트만 받습니다.
        val msg = """
        [
          {"ticket":"candle-ticket"},
          {
            "type":"$candleType",
            "codes":[${Gson().toJson(marketCode)}],
            "isOnlyRealtime":true
          },
          {"format":"DEFAULT"}
        ]
        """.trimIndent()
        ws.send(msg)
    }

    override fun onMessage(ws: WebSocket, text: String) {
        try {
            val json = JsonParser().parse(text).asJsonObject
            // candle.1m, candle.3m, … 에 해당하는 메시지만 처리
            if (json.get("type")?.asString == candleType) {
                val candle = Gson().fromJson(text, WebSocketCandleResponse::class.java)
                onCandleUpdate(candle)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onMessage(ws: WebSocket, bytes: ByteString) = onMessage(ws, bytes.utf8())
    override fun onClosing(ws: WebSocket, code: Int, reason: String) { ws.close(1000, null) }
    override fun onFailure(ws: WebSocket, t: Throwable, resp: Response?) = t.printStackTrace()
}
