package com.kkt981019.bitcoin_chart.util

import java.text.DecimalFormat

object DecimalFormatProvider {
    fun price(symbol: String): DecimalFormat = when {
        symbol.startsWith("KRW") -> DecimalFormat("#,##0.#####")
        symbol.startsWith("BTC") -> DecimalFormat("0.00000000")
        else                      -> DecimalFormat("#,##0.00######")
    }

    fun volume(symbol: String): DecimalFormat = when {
        symbol.startsWith("KRW") -> DecimalFormat("#,##0.00000000")
        symbol.startsWith("BTC") -> DecimalFormat("#,##0.00000000")
        else                      -> DecimalFormat("#,##0.00######")
    }

    fun amount(symbol: String): DecimalFormat = when {
        symbol.startsWith("KRW") -> DecimalFormat("#,##0")
        symbol.startsWith("BTC") -> DecimalFormat("#,##0.00000000")
        else                      -> DecimalFormat("#,##0.000")
    }
}