import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.DecimalFormat

@Composable
fun TotalAmountDialog(
    show: Boolean,
    onDismiss: () -> Unit,
    currentPrice: Double,
    availableBalance: Double,
    onConfirm: (Double) -> Unit
) {
    if (!show) return

    var amountInput by remember { mutableStateOf("") }
    val total = amountInput.toDoubleOrNull() ?: 0.0
    val qty = if (currentPrice > 0) total / currentPrice else 0.0

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        androidx.compose.material3.Surface(
            shape = RoundedCornerShape(size = 16.dp),
            color = Color.White,
            tonalElevation = 8.dp
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "총액 지정하여 매수",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Spacer(Modifier.height(16.dp))

                // 보유 / 현재가 / 매수수량
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("보유", fontSize = 14.sp)
                    Text("${DecimalFormat("#,##0").format(availableBalance)} KRW", fontWeight = FontWeight.Bold)
                }

                Spacer(Modifier.height(10.dp))

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("현재가", fontSize = 14.sp)
                    Text("${DecimalFormat("#,##0").format(currentPrice)} KRW", fontWeight = FontWeight.Bold)
                }

                Spacer(Modifier.height(10.dp))

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("매수 수량", fontSize = 14.sp)
                    Text("${DecimalFormat("0.########").format(qty)} BTC", fontWeight = FontWeight.Bold)
                }

                Spacer(Modifier.height(8.dp))

                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .background(Color(0xFF9E9E9E))
                )

                Spacer(Modifier.height(8.dp))

                // 금액 버튼 행
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val amountButtons = listOf(
                        "1만" to 10_000,
                        "10만" to 100_000,
                        "100만" to 1_000_000,
                        "1000만" to 10_000_000,
                        "1억" to 100_000_000
                    )

                    amountButtons.forEach { (label, amt) ->
                        Button(
                            onClick = {
                                val current = amountInput.toDoubleOrNull() ?: 0.0
                                amountInput = (current + amt).toInt().toString()
                            },
                            modifier = Modifier
                                .weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFF5F5F5),
                                contentColor = Color.Black
                            ),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                text = "+$label",
                                fontSize = 7.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                softWrap = false,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                // 총액 입력 필드
                OutlinedTextField(
                    value = amountInput,
                    onValueChange = { s ->
                        amountInput = s.filter { it.isDigit() }
                    },
                    singleLine = true,
                    placeholder = { Text("총액을 입력해 주세요") },
                    suffix = { Text("KRW") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(16.dp))

                // 초기화 버튼
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = { amountInput = "" }) {
                        Text("초기화", color = Color.Gray)
                    }
                }

                Spacer(Modifier.height(8.dp))

                // 하단 버튼 (취소 / 매수)
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFE0E0E0),
                            contentColor = Color.Black
                        )
                    ) { Text("취소") }

                    Button(
                        onClick = {
                            onConfirm(total)
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Red,
                            contentColor = Color.White
                        )
                    ) { Text("매수") }
                }
            }
        }
    }
}
