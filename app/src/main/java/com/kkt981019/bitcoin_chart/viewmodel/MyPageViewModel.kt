package com.kkt981019.bitcoin_chart.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kkt981019.bitcoin_chart.repository.FavoriteRepository
import com.kkt981019.bitcoin_chart.repository.MyPageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyPageViewModel  @Inject constructor(
    private val myPageRepository: MyPageRepository
) : ViewModel(){

    var balance by mutableStateOf(0L)
        private set

    init {
        viewModelScope.launch {
            myPageRepository.initBalanceIfNeeded()
            balance = myPageRepository.getUserMoney()
        }
    }

    fun onCharge(amount: Long) {
        viewModelScope.launch {
            myPageRepository.charge(amount)
            balance = myPageRepository.getUserMoney()
        }
    }

    fun onSpend(amount: Long) {
        viewModelScope.launch {
            myPageRepository.spend(amount)
            balance = myPageRepository.getUserMoney()
        }
    }

}