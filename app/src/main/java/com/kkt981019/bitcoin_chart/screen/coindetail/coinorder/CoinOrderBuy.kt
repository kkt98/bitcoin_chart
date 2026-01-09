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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kkt981019.bitcoin_chart.util.callCoinPnl
import com.kkt981019.bitcoin_chart.viewmodel.MyCoinViewModel
import com.kkt981019.bitcoin_chart.viewmodel.MyPageViewModel
import com.kkt981019.bitcoin_chart.viewmodel.TradeHistoryViewModel
import java.text.DecimalFormat

@Composable
fun CoinOrderBuy(
    currentPrice: Double,  // 현재 코인 가격
    format: com.kkt981019.bitcoin_chart.util.DecimalFormat.TradeFormatters, // 가격 포맷터
    context: Context,      // Toast 등에서 사용할 Context
    symbol: String,        // 코인 심볼 (예: "KRW-BTC")
    koreanName: String,
    englishName: String,
    myPageViewModel: MyPageViewModel = hiltViewModel(), // 잔액 관리용 ViewModel
    myCoinViewModel: MyCoinViewModel = hiltViewModel(),  // 보유 코인 관리용 ViewModel
    tradeHistoryViewModel: TradeHistoryViewModel = hiltViewModel()
) {

    // 현재 보유 KRW 잔액 (Room에서 가져와서 ViewModel에 보관 중인 값)
    val balance by myPageViewModel.balance.collectAsState()

    LaunchedEffect(symbol) {
        myCoinViewModel.loadCoin(symbol)
    }

    // StateFlow -> Compose State 로 변환하여 화면에서 사용
    val myCoin by myCoinViewModel.currentCoin.collectAsState()
    // 보유 수량 (없으면 0)
    val holdingAmount = myCoin?.amount ?: 0.0

    // 매수 평균 단가 (없으면 0)
    val avgPrice = myCoin?.avgPrice ?: 0.0

    val pnl = callCoinPnl(
        holdingAmount = holdingAmount,
        avgPrice = avgPrice,
        currentPrice = currentPrice
    )

    var lastRatio by remember { mutableStateOf<Double?>(null) }

    // 수량 입력 필드 포커스 상태
    var isQtyFocused by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxHeight()
            .padding(8.dp)
    ) {

        Column(Modifier.fillMaxSize()) {

            // 상단 "주문 가능" 영역 (사용자가 쓸 수 있는 KRW 표시)
            Row(
                Modifier.fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("주문 가능", color = Color(0xFF000000), fontSize = 12.sp)
                Text(
                    text = "${"%,d".format(balance)} KRW", // 3자리 콤마 포맷
                    color = Color.Black,
                    fontSize = 12.sp
                )
            }

            Spacer(Modifier.height(8.dp))

            // 현재가 영역 (고정 가격 표시)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
                    .border(
                        width = 1.dp,
                        color = Color(0xFF000000),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("가격", color = Color(0xFF000000), fontSize = 12.sp)
                Text(
                    text = format.priceDf.format(currentPrice),
                    color = Color.Black,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(8.dp))

            // 사용자가 입력하는 수량 (문자열 상태)
            var qty by remember { mutableStateOf("0") }

            // 문자열 수량을 Double로 변환한 값 (계산용)
            val qtyNum = qty.toDoubleOrNull() ?: 0.0

            // 총 매수 금액 = 수량 * 현재가
            val total = if (lastRatio == 1.0) {
                balance.toDouble()          // 최대 선택이면 잔액 그대로
            } else {
                qtyNum * currentPrice
            }
            // "총액 지정하여 매수" 다이얼로그 열림 상태
            var showAmountDialog by remember { mutableStateOf(false) }

            // 비율 드롭다운 메뉴 열림 상태
            var ratioMenuExpanded by remember { mutableStateOf(false) }

            // 수량 입력 + 비율 버튼 영역
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
                    .border(
                        1.dp,
                        Color(0xFF000000),
                        shape = RoundedCornerShape(8.dp)
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 왼쪽: "수량" 텍스트 + 수량 입력 필드
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("수량", color = Color(0xFF000000), fontSize = 12.sp)

                    Spacer(Modifier.weight(1f))

                    // 수량 입력 필드 (BasicTextField)
                    BasicTextField(
                        value = qty,
                        onValueChange = { s ->
                            // 숫자와 소수점만 입력 허용
                            qty = s.filter { it.isDigit() || it == '.' }
                            lastRatio = null
                        },
                        singleLine = true,
                        textStyle = LocalTextStyle.current.copy(
                            color = Color(0xFF000000),
                            fontSize = 12.sp,
                            textAlign = TextAlign.End
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        cursorBrush = SolidColor(Color(0xFF000000)),
                        modifier = Modifier
                            .widthIn(min = 24.dp)
                            .onFocusChanged { focusState ->
                                isQtyFocused = focusState.isFocused
                            },
                        decorationBox = { inner ->
                            Box(
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                // ✅ 포커스 없고 비어 있을 때만 0 표시
                                if (!isQtyFocused && qty.isBlank()) {
                                    Text(
                                        text = "0",
                                        color = Color(0x80000000),
                                        fontSize = 12.sp
                                    )
                                }
                                inner()
                            }
                        }
                    )
                }

                // 오른쪽: "비율" 버튼 및 드롭다운 메뉴
                Box(
                    modifier = Modifier
                        .width(56.dp)
                        .fillMaxHeight()
                        .background(
                            color = Color(0xFF9E9E9E),
                            shape = RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp)
                        )
                        .clickable { ratioMenuExpanded = true },
                    contentAlignment = Alignment.Center
                ) {
                    Text("비율", color = Color.White, fontSize = 12.sp)

                    // 비율 선택 드롭다운 메뉴
                    DropdownMenu(
                        expanded = ratioMenuExpanded,
                        onDismissRequest = { ratioMenuExpanded = false }
                    ) {
                        // label: 화면에 보이는 텍스트, ratio: 잔액에 곱할 비율
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
                                    Text(
                                        if (label == "최대") label else "$label%",
                                        fontSize = 14.sp
                                    )
                                },
                                onClick = {
                                    ratioMenuExpanded = false
                                    lastRatio = ratio // 마지막으로 선택한 비율 기억

                                    // 잔액이 0 이하인 경우
                                    if (balance <= 0L) {
                                        Toast.makeText(
                                            context,
                                            "주문 가능 금액이 유효하지 않습니다.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        return@DropdownMenuItem
                                    }

                                    // 사용할 금액 = 잔액 * 선택한 비율
                                    val useAmount = balance * ratio

                                    // 수량 = 사용할 금액 / 현재가
                                    val computedQty = useAmount / currentPrice

                                    if (currentPrice <= 0.0) {
                                        Toast.makeText(context, "유효한 가격이 아닙니다.", Toast.LENGTH_SHORT).show()
                                        return@DropdownMenuItem
                                    }

                                    // 소수 8자리까지 포맷팅해서 수량에 반영
                                    qty = DecimalFormat("0.########").format(computedQty)

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

            // 총액 표시 영역
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
                    .border(
                        width = 1.dp,
                        color = Color(0xFF000000),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("총액", color = Color(0xFF000000), fontSize = 12.sp)
                Text(
                    text = DecimalFormat("#,##0").format(total), // 총 매수 금액
                    color = Color.Black,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(8.dp))

            // 초기화 / 매수 버튼 영역
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 수량 초기화 버튼
                Button(
                    onClick = { qty = "0" },
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color(0xFFE0E0E0),
                        contentColor = Color.Black
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(width = 0.dp)
                ) {
                    Text("초기화")
                }

                // 매수 버튼
                Button(
                    onClick = {
                        // 총액이 0 이하일 경우 (수량을 안 넣었거나 0인 경우) 방지
                        if (total <= 0.0) {
                            Toast.makeText(context, "유효한 수량을 입력하세요.", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        val spend = if (lastRatio == 1.0) {
                            balance                // 🔹 그냥 잔액 전부 차감
                        } else {
                            total.toLong()
                        }

                        // 잔액 부족 체크
                        if (spend > balance) {
                            Toast.makeText(context, "잔액이 부족합니다.", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        // 잔액 차감 (user_money 테이블 업데이트)
                        myPageViewModel.onSpend(spend)

                        // 보유 코인 저장 (평균 단가 포함)
                        // qtyNum: 이번에 매수한 수량
                        // currentPrice: 이번에 매수한 1코인 가격
                        myCoinViewModel.onBuy(
                            symbol = symbol,
                            qty = qtyNum,
                            price = currentPrice,
                            korName = koreanName,
                            engName = englishName
                        )

                        tradeHistoryViewModel.addTrade(symbol, "BUY", currentPrice, qtyNum, spend.toDouble())

                        //수량 0 으로 초기화
                        qty = "0"
                        lastRatio = null

                        Toast.makeText(context, "매수완료", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red,
                        contentColor = Color.White,
                    )
                ) { Text("매수") }
            }

            Spacer(Modifier.height(8.dp))

            // "총액 지정하여 매수" 버튼 (다이얼로그 열기용)
            Button(
                onClick = {
                    showAmountDialog = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Red,
                    contentColor = Color.White,
                )
            ) { Text("총액 지정하여 매수") }

            Spacer(Modifier.height(8.dp))

            if (holdingAmount > 0.0) {
                Column(
                    Modifier.fillMaxWidth()
                ) {

                    Divider(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                        thickness = 1.dp
                    )

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = "총 보유자산",
                        fontSize = 20.sp,
                        color = Color.Black,
                    )

                    Spacer(Modifier.height(6.dp))

                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "매수평균가",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                        Text(
                            text = DecimalFormat("#,##0.##").format(avgPrice),
                            fontSize = 12.sp,
                            color = Color.Black
                        )
                    }

                    Spacer(Modifier.height(6.dp))

                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "평가금액",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                        Text(
                            text = DecimalFormat("#,##0").format(pnl.evalAmount),
                            fontSize = 12.sp,
                            color = Color.Black
                        )
                    }

                    Spacer(Modifier.height(6.dp))

                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "평가손익",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                        Text(
                            text = DecimalFormat("#,##0").format(pnl.profit),
                            fontSize = 12.sp,
                            color = when {
                                pnl.profit > 0 -> Color.Red
                                pnl.profit < 0 -> Color.Blue
                                else -> Color.Black
                            }
                        )
                    }

                    Spacer(Modifier.height(6.dp))

                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "수익률",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                        Text(
                            text = String.format("%.2f%%", pnl.profitRate),
                            fontSize = 12.sp,
                            color = when {
                                pnl.profitRate > 0 -> Color.Red
                                pnl.profitRate < 0 -> Color.Blue
                                else -> Color.Black
                            }
                        )
                    }
                }
            }

            // 총액 지정 다이얼로그
            TotalAmountDialog(
                show = showAmountDialog,
                onDismiss = { showAmountDialog = false },
                currentPrice = currentPrice,
                availableBalance = balance,
                onConfirm = { amount ->
                    // 입력된 총액이랑 현재가가 0 이하인 경우 방지
                    if (amount <= 0.0 || currentPrice <= 0.0) {
                        Toast.makeText(context, "유효한 총액을 입력하세요.", Toast.LENGTH_SHORT).show()
                        return@TotalAmountDialog
                    }
                    // 총액 기반으로 수량 계산 후 입력 필드에 반영
                    val computedQty = amount / currentPrice
                    qty = DecimalFormat("0.########").format(computedQty)
                    lastRatio = null   // 🔹 총액으로 설정하면 비율모드 해제

                    Toast.makeText(
                        context,
                        "총액 ${DecimalFormat("#,##0").format(amount)} KRW로 수량 설정",
                        Toast.LENGTH_SHORT
                    ).show()
                },
                title = "총액 지정하여 매수", // 다이얼로그 제목
                qtyLabel = "매수 수량",        // 수량 라벨 텍스트
                btnText = "매수",             // 확인 버튼 텍스트
                btnColor = Color.Red          // 확인 버튼 색상
            )
        }
    }
}
