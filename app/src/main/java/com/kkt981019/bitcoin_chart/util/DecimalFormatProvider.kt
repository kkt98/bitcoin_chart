package com.kkt981019.bitcoin_chart.util

import java.text.DecimalFormat

object DecimalFormat {

    // 1) 포맷터를 담을 데이터 클래스
    data class TradeFormatters(
        val priceDf: DecimalFormat, // 가격
        val volumeDf: DecimalFormat, // 거래량
        val amountDf: DecimalFormat // 거래금액
    )

    // 2) moneyName 에 따라 한 번에 생성해 주는 헬퍼
    fun getTradeFormatters(moneyName: String): TradeFormatters =
        when {
            moneyName.startsWith("KRW") -> TradeFormatters(
                priceDf  = DecimalFormat("#,##0.#####"),
                volumeDf = DecimalFormat("#,##0.00000000"),
                amountDf = DecimalFormat("#,##0")
            )
            moneyName.startsWith("BTC") -> TradeFormatters(
                priceDf  = DecimalFormat("0.00000000"),
                volumeDf = DecimalFormat("#,##0.00000000"),
                amountDf = DecimalFormat("#,##0.00000000")
            )
            else -> TradeFormatters(
                priceDf  = DecimalFormat("#,##0.00######"),
                volumeDf = DecimalFormat("#,##0.00######"),
                amountDf = DecimalFormat("#,##0.000")
            )
        }

}