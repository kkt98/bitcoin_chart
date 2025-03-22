package com.kkt981019.bitcoin_chart.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.kkt981019.bitcoin_chart.repository.RetrofitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

@HiltViewModel
class RetrofitViewModel @Inject constructor(
    private val repository: RetrofitRepository
) : ViewModel() {

    val allMarkets = liveData(Dispatchers.IO) {
        try {
            val names = repository.getAllMarket()
            emit(names)
        } catch (e: Exception) {
            // 에러 발생 시 빈 리스트 반환 및 로그 출력
        }
    }

}