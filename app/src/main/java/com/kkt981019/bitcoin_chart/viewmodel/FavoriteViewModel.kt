package com.kkt981019.bitcoin_chart.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kkt981019.bitcoin_chart.repository.FavoriteRepository
import com.kkt981019.bitcoin_chart.room.FavoriteEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoriteViewModel @Inject constructor(
    private val favoriteRepository: FavoriteRepository
) : ViewModel() {

    // ① 화면에 노출할 LiveData
    private val _favorites = MutableLiveData<List<FavoriteEntity>>(emptyList())
    val favorites: LiveData<List<FavoriteEntity>> = _favorites

    init {
        // 앱 시작하자마자 혹은 화면 진입하자마자 전체 즐겨찾기 로드
        refreshFavorites()
    }

    // ② 전체 즐겨찾기 다시 불러오기
    private fun refreshFavorites() {
        viewModelScope.launch {
            val list = favoriteRepository.getAllFavorites()  // suspend → DAO에서 List<FavoriteEntity>
            _favorites.postValue(list)
        }
    }

    // ③ 즐겨찾기 추가
    fun addFavorite(market: String, kor: String, eng: String) {
        viewModelScope.launch {
            favoriteRepository.addFavorite(market, kor, eng)
            refreshFavorites()
        }
    }

    // ④ 즐겨찾기 삭제
    fun removeFavorite(market: String) {
        viewModelScope.launch {
            favoriteRepository.removeFavorite(market)
            refreshFavorites()
        }
    }

    // ⑤ 토글 (있으면 삭제, 없으면 추가)
    fun toggleFavorite(market: String, kor: String, eng: String) {
        val current = _favorites.value ?: emptyList()
        if (current.any { it.market == market }) {
            removeFavorite(market)
        } else {
            addFavorite(market, kor, eng)
        }
    }

    // ⑥ 현재 이 마켓이 즐겨찾기인지 체크
    fun isFavorite(market: String): Boolean {
        return (_favorites.value ?: emptyList()).any { it.market == market }
    }
}