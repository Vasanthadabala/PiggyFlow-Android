package com.piggylabs.piggyflow.features.budget.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.piggylabs.piggyflow.core.designsystem.theme.appColors
import com.piggylabs.piggyflow.core.domain.model.BudgetGoal
import com.piggylabs.piggyflow.core.domain.model.BudgetPriority
import com.piggylabs.piggyflow.core.domain.model.Category

/**
 * Creates or edits one monthly category budget.
 *
 * The category field is a free-text box with the user's existing categories offered as
 * suggestions: spending is matched to a goal by category name, so picking from the list
 * is what makes the goal track anything, but a name can still be typed for a category
 * that has not been used yet.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetGoalSheet(
    goal: BudgetGoal?,
    categories: List<Category>,
    isDuplicate: (String) -> Boolean,
    onDismiss: () -> Unit,
    onSave: (name: String, emoji: String, priority: String, monthlyLimit: Double) -> Unit,
    onDelete: (() -> Unit)?
) {
    val colors = appColors()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var categoryName by remember { mutableStateOf(goal?.categoryName.orEmpty()) }
    var categoryEmoji by remember { mutableStateOf(goal?.categoryEmoji.orEmpty()) }
    var priority by remember { mutableStateOf(goal?.priority ?: BudgetPriority.NEEDS) }
    var limit by remember {
        mutableStateOf(goal?.monthlyLimit?.let { "%.0f".format(it) }.orEmpty())
    }
    var categoryMenuOpen by remember { mutableStateOf(false) }

    val limitValue = limit.toDoubleOrNull()
    val duplicate = categoryName.isNotBlank() && isDuplicate(categoryName)
    val isValid = categoryName.isNotBlank() &&
        limitValue != null &&
        limitValue > 0 &&
        !duplicate

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.background,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = if (goal == null) "New Budget Goal" else "Edit Budget Goal",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.text
            )

            Spacer(Modifier.height(14.dp))

            Text(text = "Category", fontSize = 14.sp, color = colors.textMuted)
            Spacer(Modifier.height(5.dp))

            ExposedDropdownMenuBox(
                expanded = categoryMenuOpen && categories.isNotEmpty(),
                onExpandedChange = { categoryMenuOpen = it }
            ) {
                OutlinedTextField(
                    value = categoryName,
                    onValueChange = {
                        categoryName = it
                        // Typing a name that is not a known category clears the borrowed emoji.
                        categoryEmoji = categories
                            .firstOrNull { c -> c.name.equals(it.trim(), ignoreCase = true) }
                            ?.emoji
                            .orEmpty()
                    },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                        .height(56.dp),
                    singleLine = true,
                    placeholder = {
                        Text(
                            text = "e.g. Groceries",
                            style = TextStyle(fontSize = 14.sp, color = colors.textMuted)
                        )
                    },
                    shape = RoundedCornerShape(20),
                    isError = duplicate,
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        errorIndicatorColor = Color.Transparent,
                        focusedContainerColor = colors.container,
                        unfocusedContainerColor = colors.container,
                        errorContainerColor = colors.container,
                        cursorColor = colors.text
                    ),
                    trailingIcon = {
                        if (categories.isNotEmpty()) {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryMenuOpen)
                        }
                    },
                    textStyle = TextStyle(
                        fontWeight = FontWeight.W500,
                        fontSize = 16.sp,
                        color = colors.text
                    )
                )

                ExposedDropdownMenu(
                    expanded = categoryMenuOpen && categories.isNotEmpty(),
                    onDismissRequest = { categoryMenuOpen = false },
                    containerColor = colors.surface
                ) {
                    categories.forEach { category ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "${category.emoji} ${category.name}".trim(),
                                    fontSize = 14.sp,
                                    color = colors.text
                                )
                            },
                            onClick = {
                                categoryName = category.name
                                categoryEmoji = category.emoji
                                categoryMenuOpen = false
                            }
                        )
                    }
                }
            }

            if (duplicate) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "This category already has a budget goal.",
                    fontSize = 12.sp,
                    color = colors.negative
                )
            }

            Spacer(Modifier.height(14.dp))

            Text(text = "Priority", fontSize = 14.sp, color = colors.textMuted)
            Spacer(Modifier.height(5.dp))

            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                BudgetPriority.all.forEachIndexed { index, option ->
                    SegmentedButton(
                        selected = priority == option,
                        onClick = { priority = option },
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = BudgetPriority.all.size
                        ),
                        colors = SegmentedButtonDefaults.colors(
                            activeContainerColor = colors.accentSoft,
                            activeContentColor = colors.onAccentSoft,
                            inactiveContainerColor = colors.background,
                            inactiveContentColor = colors.textMuted
                        )
                    ) {
                        Text(text = option, fontSize = 13.sp)
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            Text(text = "Monthly limit", fontSize = 14.sp, color = colors.textMuted)
            Spacer(Modifier.height(5.dp))

            OutlinedTextField(
                value = limit,
                onValueChange = { limit = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                singleLine = true,
                placeholder = {
                    Text(
                        text = "Enter amount",
                        style = TextStyle(fontSize = 14.sp, color = colors.textMuted)
                    )
                },
                shape = RoundedCornerShape(20),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.CurrencyRupee,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = colors.textMuted
                    )
                },
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedContainerColor = colors.container,
                    unfocusedContainerColor = colors.container,
                    cursorColor = colors.text
                ),
                textStyle = TextStyle(
                    fontWeight = FontWeight.W500,
                    fontSize = 16.sp,
                    color = colors.text
                )
            )

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = {
                    onSave(categoryName.trim(), categoryEmoji, priority, limitValue ?: 0.0)
                },
                enabled = isValid,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 0.dp,
                    pressedElevation = 2.dp
                ),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.accent,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = if (goal == null) "Add Goal" else "Save Changes",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                )
            }

            if (onDelete != null) {
                Spacer(Modifier.height(10.dp))
                OutlinedButton(
                    onClick = onDelete,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.negative)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(text = "Delete Goal", fontSize = 15.sp)
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
        }
    }
}
