package com.kkt981019.bitcoin_chart.util

data class CoinProfitSum(    val totalBuyAmount: Double,
                                 val evalAmount: Double,
                                 val profit: Double,
                                 val profitRate: Double,)

fun callCoinPnl(
    holdingAmount: Double,
    avgPrice: Double,
    currentPrice: Double
): CoinProfitSum {
    val totalBuyAmount = holdingAmount * avgPrice
    val evalAmount = holdingAmount * currentPrice
    val profit = evalAmount - totalBuyAmount
    val profitRate = if (totalBuyAmount > 0) {
        (profit / totalBuyAmount) * 100.0
    } else 0.0

    return CoinProfitSum(
        totalBuyAmount = totalBuyAmount,
        evalAmount = evalAmount,
        profit = profit,
        profitRate = profitRate
    )
}
