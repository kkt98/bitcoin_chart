package com.kkt981019.bitcoin_chart.util

import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TimestampAxisValueFormatter(
    private val pattern: String = "HH:mm",
    private val locale: Locale = Locale.getDefault()
) : ValueFormatter() {
    private val sdf = SimpleDateFormat(pattern, locale)

    override fun getFormattedValue(value: Float): String {
        // value == 밀리세컨드 단위 → Date 객체로 바꾼 뒤 포맷
        return sdf.format(Date(value.toLong()))
    }
}