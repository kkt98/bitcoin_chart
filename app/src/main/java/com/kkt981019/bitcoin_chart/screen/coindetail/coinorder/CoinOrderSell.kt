package com.kkt981019.bitcoin_chart.screen.coindetail.coinorder

import TotalAmountDialog
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kkt981019.bitcoin_chart.viewmodel.MyCoinViewModel
import java.text.DecimalFormat

@Composable
fun CoinOrderSell(
    currentPrice: Double, // 현재 선택된 코인의 시세 (실시간 가격)
    format: com.kkt981019.bitcoin_chart.util.DecimalFormat.TradeFormatters, // 가격 포맷터
    context: Context, // Toast 등 UI 용
    symbol: String, // 예: "KRW-BTC"
    myCoinViewModel: MyCoinViewModel = hiltViewModel() // 매도 로직 / 보유 코인 정보 조회 ViewModel
) {

    // symbol 변경될 때마다 해당 코인 정보 DB에서 가져와서 currentCoin 에 저장
    LaunchedEffect(symbol) {
        myCoinViewModel.loadCoin(symbol)
    }

    // StateFlow -> Compose State 로 변환하여 화면에서 사용
    val myCoin by myCoinViewModel.currentCoin.collectAsState()

    // 보유 수량 (없으면 0)
    val holdingAmount = myCoin?.amount ?: 0.0

    Box(
        modifier = Modifier
            .fillMaxHeight()
            .padding(12.dp)
    ) {

        Column(Modifier.fillMaxSize()) {

            // ------------------------------
            // 상단: 보유 수량 표시
            // ------------------------------
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("보유수량", color = Color.Black, fontSize = 12.sp)

                // 보유수량 + 단위 (예: BTC)
                Text(
                    "${DecimalFormat("0.########").format(holdingAmount)} ${symbol.substringAfter("-")}",
                    fontSize = 12.sp
                )
            }

            Spacer(Modifier.height(4.dp))

            // 보유 코인의 원화 환산 금액
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    "= ${DecimalFormat("#,##0").format(holdingAmount * currentPrice)} ${symbol.substringBefore("-")}",
                    color = Color.Gray,
                    fontSize = 10.sp
                )
            }

            Spacer(Modifier.height(8.dp))

            // ------------------------------
            // 가격 표시 (현재 시세)
            // ------------------------------
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
                    .border(
                        width = 1.dp,
                        color = Color.Black,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("가격", color = Color.Black, fontSize = 12.sp)
                Text(
                    format.priceDf.format(currentPrice),
                    color = Color.Black,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(8.dp))

            // ------------------------------------------
            // 매도 입력 상태 변수: 매도 수량 입력
            // ------------------------------------------
            var amountText by remember { mutableStateOf("0") }         // 입력 필드 값 (문자열)
            val amount = amountText.toDoubleOrNull() ?: 0.0            // 실제 매도 수량 (Double)
            val total = amount * currentPrice                          // 매도 시 받을 총액

            // 총액 지정하여 매도하는 다이얼로그
            var showAmountDialog by remember { mutableStateOf(false) }

            // 비율 매도 드롭다운 메뉴
            var ratioMenuExpanded by remember { mutableStateOf(false) }

            // ------------------------------
            // 수량 입력 Row
            // ------------------------------
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
                    .border(1.dp, Color.Black, RoundedCornerShape(8.dp)),
                verticalAlignment = Alignment.CenterVertically
            ) {

                // 왼쪽: "수량" + 입력창
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("수량", color = Color.Black, fontSize = 12.sp)

                    Spacer(Modifier.weight(1f))

                    // 매도 수량 입력하는 텍스트필드
                    BasicTextField(
                        value = amountText,
                        onValueChange = { s ->
                            // 숫자 또는 소수점만 허용
                            amountText = s.filter { it.isDigit() || it == '.' }
                        },
                        singleLine = true,
                        textStyle = LocalTextStyle.current.copy(
                            color = Color.Black,
                            fontSize = 12.sp,
                            textAlign = TextAlign.End
                        ),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal
                        ),
                        cursorBrush = SolidColor(Color.Black),
                        decorationBox = { inner ->
                            Box(
                                modifier = Modifier.widthIn(min = 24.dp),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                if (amountText.isBlank())
                                    Text(text = "0", color = Color.Gray, fontSize = 12.sp)
                                inner()
                            }
                        }
                    )
                }

                // 오른쪽: 비율 버튼
                Box(
                    modifier = Modifier
                        .width(56.dp)
                        .fillMaxHeight()
                        .background(
                            Color(0xFF9E9E9E),
                            RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp)
                        )
                        .clickable { ratioMenuExpanded = true },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "비율", color = Color.White, fontSize = 12.sp)

                    // 드롭다운 메뉴
                    DropdownMenu(
                        expanded = ratioMenuExpanded,
                        onDismissRequest = { ratioMenuExpanded = false }
                    ) {
                        val items = listOf(
                            "최대" to 1.0,
                            "75" to 0.75,
                            "50" to 0.50,
                            "25" to 0.25,
                            "10" to 0.10
                        )

                        items.forEach { (label, ratio) ->
                            DropdownMenuItem(
                                text = {
                                    Text(if (label == "최대") label else "$label%", fontSize = 14.sp)
                                },
                                onClick = {
                                    ratioMenuExpanded = false

                                    if (holdingAmount <= 0) {
                                        Toast.makeText(
                                            context,
                                            "보유 수량이 부족합니다.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        return@DropdownMenuItem
                                    }

                                    // 비율만큼 매도할 코인 수량
                                    val coinAmount = holdingAmount * ratio

                                    // 입력 필드에 반영
                                    amountText = DecimalFormat("0.########").format(coinAmount)

                                    Toast.makeText(
                                        context,
                                        if (label == "최대") "최대 선택" else "$label% 선택",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // ------------------------------
            // 총액 표시 (amount * currentPrice)
            // ------------------------------
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
                    .border(1.dp, Color.Black, RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("총액", color = Color.Black, fontSize = 12.sp)
                Text(
                    DecimalFormat("#,##0.##").format(total),
                    color = Color.Black,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(8.dp))

            // ------------------------------
            // 초기화 + 매도 버튼
            // ------------------------------
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 수량 초기화
                Button(
                    onClick = { amountText = "0" },
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color(0xFFE0E0E0),
                        contentColor = Color.Black
                    )
                ) {
                    Text("초기화")
                }

                // 매도 버튼
                Button(
                    onClick = {

                        // 1) 수량 유효성 체크
                        if (amount <= 0.0) {
                            Toast.makeText(context, "유효한 수량을 입력하세요.", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        // 2) 보유 수량보다 많이 팔려고 하는지 체크
                        if (amount > holdingAmount) {
                            Toast.makeText(context, "보유 수량을 초과했습니다.", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        // 실제 매도 로직 호출
                        myCoinViewModel.onSell(symbol = symbol, qty = amount)

                        amountText = "0"

                        Toast.makeText(context, "매도완료", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Blue,
                        contentColor = Color.White,
                    )
                ) { Text("매도") }
            }

            Spacer(Modifier.height(8.dp))

            // ------------------------------
            // 총액 지정하여 매도 버튼
            // ------------------------------
            Button(
                onClick = { showAmountDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Blue,
                    contentColor = Color.White,
                )
            ) { Text("총액 지정하여 매도") }

            Spacer(Modifier.height(8.dp))

            Column (
                Modifier.fillMaxWidth()
                    .padding(top = 8.dp)
            ) {

                Divider(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                    thickness = 1.dp
                )

                Text(
                    text = "총 보유자산",
                    fontSize = 12.sp,
                    color = Color.Black,
                )
            }

            // 총액 입력 다이얼로그
            TotalAmountDialog(
                show = showAmountDialog,
                onDismiss = { showAmountDialog = false },
                currentPrice = currentPrice,
                availableBalance = 0, // 매도는 잔액이 아니라 보유코인 기준
                onConfirm = { totalAmount ->
                    // 총액에서 수량 계산
                    if (totalAmount <= 0.0 || currentPrice <= 0.0) {
                        Toast.makeText(context, "유효한 총액을 입력하세요.", Toast.LENGTH_SHORT).show()
                        return@TotalAmountDialog
                    }
                    val computedAmount = totalAmount / currentPrice
                    amountText = DecimalFormat("0.########").format(computedAmount)

                    Toast.makeText(
                        context,
                        "총액 ${DecimalFormat("#,##0").format(totalAmount)} KRW로 수량 설정",
                        Toast.LENGTH_SHORT
                    ).show()
                },
                title = "총액 지정하여 매도",
                qtyLabel = "매수 매도",
                btnText = "매도",
                btnColor = Color.Blue
            )
        }
    }
}
