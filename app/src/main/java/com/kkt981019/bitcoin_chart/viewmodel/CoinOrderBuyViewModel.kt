package com.kkt981019.bitcoin_chart.viewmodel

import androidx.lifecycle.ViewModel
import com.kkt981019.bitcoin_chart.repository.MyCoinRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CoinOrderBuyViewModell  @Inject constructor(
    private val myPageRepository: MyCoinRepository
) : ViewModel() {



}