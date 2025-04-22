package com.kkt981019.bitcoin_chart.viewmodel

import androidx.lifecycle.ViewModel
import com.kkt981019.bitcoin_chart.repository.RetrofitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CoinDtChartViewModel @Inject constructor(
    private val retrofitRepository: RetrofitRepository
) : ViewModel() {



}