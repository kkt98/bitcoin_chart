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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.kkt981019.bitcoin_chart.viewmodel.MyPageViewModel
import java.text.DecimalFormat

@Composable
fun CoinOrderBuy(
    currentPrice: Double,
    format: com.kkt981019.bitcoin_chart.util.DecimalFormat.TradeFormatters,
    context: Context,
    symbol: String,
    myPageViewModel: MyPageViewModel = hiltViewModel()
) {

    val balance = myPageViewModel.balance // 현재 내가 가진 돈

    Box(
        modifier = Modifier
            .fillMaxHeight()
            .padding(12.dp)
    ) {

        Column(Modifier.fillMaxSize()) {

            //주문가능 영역 (내가 현재 가지고 있는 돈)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("주문 가능", color = Color(0xFF000000), fontSize = 12.sp)
                Text(
                    text = "${"%,d".format(balance)} KRW",
                    color = Color.Black,
                    fontSize = 12.sp
                )
            }

            Spacer(Modifier.height(8.dp))

            // 가격 영역
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
                    .border(
                        width = 1.dp,
                        color = Color(0xFF000000),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("가격", color = Color(0xFF000000), fontSize = 12.sp)
                Text("${format.priceDf.format(currentPrice)}", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(8.dp))

            var qty by remember { mutableStateOf("0") }
            val qtyNum = qty.toDoubleOrNull() ?: 0.0
            val total = qtyNum * currentPrice

            // ✅ 다이얼로그 열림 상태
            var showAmountDialog by remember { mutableStateOf(false) }

            // ▼ 드롭다운 열림 상태
            var ratioMenuExpanded by remember { mutableStateOf(false) }

            // 수량 영역
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
                    .border(
                        1.dp,
                        Color(0xFF000000),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // ▶ 왼쪽: 라벨 + 숫자 입력(오른쪽 정렬)
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("수량", color = Color(0xFF000000), fontSize = 12.sp)

                    Spacer(Modifier.weight(1f))

                    BasicTextField(
                        value = qty,
                        onValueChange = { s ->
                            qty = s.filter { it.isDigit() || it == '.' }
                        },
                        singleLine = true,
                        textStyle = LocalTextStyle.current.copy(
                            color = Color(0xFF000000),
                            fontSize = 12.sp,
                            textAlign = TextAlign.End
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        cursorBrush = SolidColor(Color(0xFF000000)),
                        decorationBox = { inner ->
                            Box(
                                modifier = Modifier.widthIn(min = 24.dp),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                if (qty.isBlank()) {
                                    Text("0", color = Color(0x80000000), fontSize = 12.sp)
                                }
                                inner()
                            }
                        }
                    )
                }

                // ▶ 오른쪽: '비율' 버튼 + 드롭다운
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

                    // ▼ 드롭다운 메뉴
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
                                    Text(
                                        if (label == "최대") label else "$label%",
                                        fontSize = 14.sp
                                    )
                                },
                                onClick = {
                                    ratioMenuExpanded = false

                                    // 현재가 / 잔액 체크
                                    if (balance <= 0L) {
                                        Toast.makeText(
                                            context,
                                            "주문 가능 금액이 유효하지 않습니다.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        return@DropdownMenuItem
                                    }

                                    // 비율만큼 사용할 금액
                                    val useAmount = balance * ratio

                                    // 수량 계산 = (사용 금액 / 현재가)
                                    val computedQty = useAmount / currentPrice

                                    // 수량 필드에 반영 (소수 8자리까지)
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

            // 총액 text 영역
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
                    .border(
                        width = 1.dp,
                        color = Color(0xFF000000),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("총액", color = Color(0xFF000000), fontSize = 12.sp)
                Text(
                    text = DecimalFormat("#,##0.##").format(total),
                    color = Color.Black,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(8.dp))

            //초기화, 매수 버튼
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 초기화
                Button(
                    onClick = { qty = "0" },
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
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
                        // 총액이 0 이하이면 막기
                        if (total <= 0.0) {
                            Toast.makeText(context, "유효한 수량을 입력하세요.", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        // 잔액 부족 체크
                        if (total.toLong() > balance) {
                            Toast.makeText(context, "잔액이 부족합니다.", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        // 4) 잔액 차감
                        myPageViewModel.onSpend(total.toLong())

                        Toast.makeText(context, "매수완료", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red,
                        contentColor = Color.White,
                    )
                ) { Text("매수") }
            }

            Spacer(Modifier.height(8.dp))

            // 총액 지정하여 매수 버튼
            Button(
                onClick = {
                    showAmountDialog = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Red,
                    contentColor = Color.White,
                )
            ) { Text("총액 지정하여 매수") }

            TotalAmountDialog(
                show = showAmountDialog,
                onDismiss = { showAmountDialog = false },
                currentPrice = currentPrice,
                availableBalance = balance,
                onConfirm = { amount ->
                    // 사용자가 입력/버튼으로 확정한 총액(amount)으로 수량 계산
                    if (amount <= 0.0 || currentPrice <= 0.0) {
                        Toast.makeText(context, "유효한 총액을 입력하세요.", Toast.LENGTH_SHORT).show()
                        return@TotalAmountDialog
                    }
                    val computedQty = amount / currentPrice
                    qty = DecimalFormat("0.########").format(computedQty)
                    Toast.makeText(
                        context,
                        "총액 ${DecimalFormat("#,##0").format(amount)} KRW로 수량 설정",
                        Toast.LENGTH_SHORT
                    ).show()
                },
                title = "총액 지정하여 매수",
                qtyLabel = "매수 수량",
                btnText = "매수",
                btnColor = Color.Red
            )
        }
    }
}

