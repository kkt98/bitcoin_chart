package com.kkt981019.bitcoin_chart.screen

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.kkt981019.bitcoin_chart.viewmodel.CoinDTScreenVM
import java.text.DecimalFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoinDetailScreen(
    symbol: String,
    koreanName: String,
    navController: NavController,
    viewModel: CoinDTScreenVM = hiltViewModel() // Hilt로 주입받음
) {
    // 화면 진입 시, 해당 심볼로 웹소켓 연결 시작
    LaunchedEffect(symbol) {
        viewModel.startDetailWebSocket(symbol)
    }

    // ViewModel의 LiveData를 observeAsState로 관찰
    val coinDetail by viewModel.tickerState.observeAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "$koreanName ($symbol)") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "뒤로가기 버튼"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
                // 데이터가 도착하면 Row로 세 숫자를 균등하게 표시
            Column(
                    modifier = Modifier.fillMaxSize(),
                ) {

                // 현재가 소수점 표시용 포맷터
                val df = when {
                    symbol.startsWith("KRW") -> DecimalFormat("#,##0.##")
                    symbol.startsWith("BTC") -> DecimalFormat("0.00000000")
                    else -> DecimalFormat("#,##0.000#####")
                }

                // volume 문자열 포맷 처리 예시
                val volumeString = when {
                    symbol.startsWith("KRW") -> {
                        val value = coinDetail?.signed_change_rate?.toDoubleOrNull() ?: 0.0
                        // 예를 들어, 백분율 형태로 표시하려면
                        String.format("%.2f%%", value * 100)
                    }
                    symbol.startsWith("BTC") -> {
                        val value = coinDetail?.signed_change_rate?.toDoubleOrNull() ?: 0.0
                        String.format("%.3f", value)
                    }
                    else -> {
                        val value = coinDetail?.signed_change_rate?.toDoubleOrNull() ?: 0.0
                        String.format("%,.3f", value)
                    }
                }

                val si = DecimalFormat("#,##0.###")

                val color = when(coinDetail?.change) {
                    "EVEN" -> Color.Black
                    "RISE" -> Color.Red
                    else -> Color.Blue
                }

                Column (
                    modifier = Modifier.fillMaxSize(),
//                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceEvenly
                ) {
                    // trade_price는 숫자로 변환 후 포맷팅
                    Text(text = df.format(coinDetail?.trade_price?.toDoubleOrNull() ?: 0.0),
                        style = MaterialTheme.typography.headlineSmall,
                        color = color)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp)
                    )  {
                        Text(text = volumeString,
                            style = MaterialTheme.typography.bodyMedium,
                            color = color)

                        Text(text = si.format(coinDetail?.signed_change_price?.toDoubleOrNull() ?: 0.0),
                            style = MaterialTheme.typography.bodyMedium,
                            color = color)
                    }
                }

            }

            // 필요에 따라 추가 UI 구성 가능 (예: 주문호가, 차트 등)
        }
    }
}
