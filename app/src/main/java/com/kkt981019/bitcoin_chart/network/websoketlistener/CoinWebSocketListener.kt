package com.kkt981019.bitcoin_chart.network.websoketlistener

import com.google.gson.Gson
import com.kkt981019.bitcoin_chart.network.Data.WebsocketResponse
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString

class CoinWebSocketListener(
    private val marketCodes: List<String>?,
    private val onTickerUpdate: (WebsocketResponse) -> Unit
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
        try {
            // JSON 메시지를 TickerResponse 객체로 파싱
            val ticker = Gson().fromJson(text, WebsocketResponse::class.java)
            onTickerUpdate(ticker)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    //바이트 메시지 수신 시 처리
    override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
        onMessage(webSocket, bytes.utf8())
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        webSocket.close(1000, null)
    }

    //실패시
    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        t.printStackTrace()
    }
}