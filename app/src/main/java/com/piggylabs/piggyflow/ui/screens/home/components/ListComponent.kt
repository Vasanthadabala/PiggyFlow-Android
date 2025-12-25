package com.piggylabs.piggyflow.ui.screens.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.piggylabs.piggyflow.data.local.entity.ExpenseEntity
import com.piggylabs.piggyflow.data.local.entity.IncomeEntity
import com.piggylabs.piggyflow.ui.navigation.ListDataDetails
import com.piggylabs.piggyflow.ui.theme.appColors
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

sealed class TransactionUi {
    data class Expense(val data: ExpenseEntity) : TransactionUi()
    data class Income(val data: IncomeEntity) : TransactionUi()
}

@Composable
fun ListComponent(navController: NavHostController, transaction: TransactionUi){

    val isExpense = transaction is TransactionUi.Expense

    val title = if (isExpense)
        transaction.data.categoryName
    else
        (transaction as TransactionUi.Income).data.categoryName.ifBlank { "Income" }

    val emoji = if (isExpense)
        transaction.data.categoryEmoji
    else
        (transaction as TransactionUi.Income).data.categoryEmoji.ifBlank { "💰" }

    val date = if (isExpense)
        transaction.data.date
    else
        (transaction as TransactionUi.Income).data.date

    val amount = if (isExpense)
        transaction.data.amount
    else
        (transaction as TransactionUi.Income).data.amount

    val amountColor = if (isExpense) appColors().red else Color(0xFF27C152)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 0.dp, vertical = 6.dp)
            .clickable{
                val type = if (isExpense) "expense" else "income"

                val id = if (isExpense)
                    transaction.data.id
                else
                    (transaction as TransactionUi.Income).data.id

                navController.navigate("${ListDataDetails.route}/$type/$id"){
                    launchSingleTop = true
                }
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = appColors().container),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background( if (isExpense) Color.Red.copy(alpha = 0.15f) else Color(0xFF27C152).copy(alpha = 0.2f) ),
                    contentAlignment = Alignment.Center
                ) {

                    if (emoji.isBlank()) {
                        Text(
                            text = title.trim().firstOrNull()?.uppercase() ?: "",
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Text(
                            text = emoji,
                            fontSize = 22.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    verticalArrangement = Arrangement.spacedBy((-6).dp)
                ) {
                    Text(
                        text = title,
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = appColors().text
                    )

                    Text(
                        text = formatDateForUI(date),
                        textAlign = TextAlign.Center,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color.Gray
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                if(isExpense) {
                    Text(
                        text = "-",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = appColors().red
                    )
                }else{
                    Text(
                        text = "+",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = appColors().green
                    )
                }

                Text(
                    text = "₹$amount",
                    textAlign = TextAlign.Center,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    color = amountColor
                )
            }
        }
    }
}

fun formatDateForUI(dbDate: String): String {
    val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val outputFormatter = DateTimeFormatter.ofPattern("MMM dd", Locale.ENGLISH)

    val parsedDate = LocalDate.parse(dbDate, inputFormatter)
    return parsedDate.format(outputFormatter)
}