package com.kkt981019.bitcoin_chart.network

import com.google.gson.Gson
import com.kkt981019.bitcoin_chart.network.Data.TickerResponse
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString

class CoinWebSocketListener(
    private val marketCodes: List<String>,
    private val onTickerUpdate: (TickerResponse) -> Unit
) : WebSocketListener() {

    // 연결이 성공하면 구독 메시지를 전송합니다.
    override fun onOpen(webSocket: WebSocket, response: Response) {
        // API 문서에 따라 티커와 체결 메시지 구독 요청 보내기
        // 여기서는 ticker 메시지만 구독하는 예시입니다.
        val subscribeMessage = """
            [
              {"ticket": "unique-ticket"},
              {"type": "ticker", "codes": ${Gson().toJson(marketCodes)} }
            ]
        """.trimIndent()
        webSocket.send(subscribeMessage)
    }

    // 텍스트 메시지 수신 시 처리
    override fun onMessage(webSocket: WebSocket, text: String) {
        super.onMessage(webSocket, text)
    }

    //바이트 메시지 수신 시 처리
    override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
        super.onMessage(webSocket, bytes)
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        super.onClosing(webSocket, code, reason)
    }

    //실패시
    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        super.onFailure(webSocket, t, response)
    }
}