package com.piggylabs.piggyflow.features.adddata.presentation.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.piggylabs.piggyflow.core.designsystem.theme.appColors
import com.piggylabs.piggyflow.core.domain.model.Account

/**
 * The building blocks shared by the Add Income and Add Expense forms. Both screens are
 * the same mockup with a different field set, so the chrome lives here and each screen
 * only owns its own fields and save logic.
 */

/** Currencies offered by the amount chip, paired with the symbol shown next to the amount. */
internal data class CurrencyOption(val code: String, val symbol: String) {
    val label: String get() = "$code ($symbol)"
}

internal val currencyOptions = listOf(
    CurrencyOption("INR", "₹"),
    CurrencyOption("USD", "$"),
    CurrencyOption("EUR", "€"),
    CurrencyOption("GBP", "£"),
    CurrencyOption("AED", "د.إ")
)

internal val tagSuggestions =
    listOf("Personal", "Work", "Family", "Recurring", "Business", "Tax Related")

internal const val DESCRIPTION_LIMIT = 100

// ---------------------------------------------------------------------------
// Chrome
// ---------------------------------------------------------------------------

@Composable
internal fun EntryTopBar(
    title: String,
    subtitle: String,
    hasReceipt: Boolean,
    onBack: () -> Unit,
    onReceipt: () -> Unit
) {
    val colors = appColors()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.background)
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(colors.surface)
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = colors.text,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = colors.text)
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = colors.textMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .clickable(onClick = onReceipt)
                .padding(horizontal = 6.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.ReceiptLong,
                contentDescription = "Attach receipt",
                tint = colors.accent,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (hasReceipt) "Attached" else "Receipt",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.accent
            )
        }
    }
}

/** The pill save button plus the "Save & Add Another" text action beneath it. */
@Composable
internal fun EntrySaveBar(
    saveLabel: String,
    enabled: Boolean,
    onSave: () -> Unit,
    onSaveAndAddAnother: () -> Unit
) {
    val colors = appColors()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.background)
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 10.dp)
    ) {
        Button(
            onClick = onSave,
            enabled = enabled,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.accent,
                contentColor = Color.White,
                disabledContainerColor = colors.accent.copy(alpha = 0.4f),
                disabledContentColor = Color.White.copy(alpha = 0.7f)
            )
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircleOutline,
                contentDescription = null,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(text = saveLabel, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
        }

        Text(
            text = "Save & Add Another",
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (enabled) colors.accent else colors.accent.copy(alpha = 0.4f),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .clickable(enabled = enabled, onClick = onSaveAndAddAnother)
                .padding(vertical = 12.dp)
        )
    }
}

// ---------------------------------------------------------------------------
// Amount
// ---------------------------------------------------------------------------

@Composable
internal fun AmountCard(
    amountInput: String,
    onAmountChange: (String) -> Unit,
    currency: CurrencyOption,
    amountColor: Color,
    onCurrencyClick: () -> Unit
) {
    val colors = appColors()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Amount", fontSize = 14.sp, color = colors.textMuted)

                Spacer(modifier = Modifier.height(6.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = currency.symbol,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = amountColor
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(modifier = Modifier.weight(1f)) {
                        if (amountInput.isEmpty()) {
                            Text(
                                text = "0",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = amountColor.copy(alpha = 0.35f)
                            )
                        }
                        BasicTextField(
                            value = amountInput,
                            onValueChange = onAmountChange,
                            singleLine = true,
                            textStyle = TextStyle(
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = amountColor
                            ),
                            cursorBrush = SolidColor(colors.accent),
                            visualTransformation = AmountGroupingTransformation,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(colors.surfaceMuted)
                    .clickable(onClick = onCurrencyClick)
                    .padding(start = 14.dp, end = 10.dp, top = 10.dp, bottom = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = currency.label,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.text
                )
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = colors.textMuted,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
internal fun ReceiptChip(onRemove: () -> Unit) {
    val colors = appColors()

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(colors.accentSoft)
            .padding(start = 12.dp, end = 6.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.ReceiptLong,
            contentDescription = null,
            tint = colors.onAccentSoft,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Receipt attached",
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = colors.onAccentSoft
        )
        Spacer(modifier = Modifier.width(6.dp))
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Remove receipt",
            tint = colors.onAccentSoft,
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .clickable(onClick = onRemove)
                .padding(4.dp)
        )
    }
}

// ---------------------------------------------------------------------------
// Field rows
// ---------------------------------------------------------------------------

@Composable
internal fun FieldLabel(text: String) {
    Text(
        text = text,
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
        color = appColors().textMuted,
        modifier = Modifier.padding(start = 4.dp, top = 10.dp, bottom = 6.dp)
    )
}

/** The green square that leads every row - solid once the field carries a value. */
@Composable
internal fun IconBadge(icon: ImageVector, filled: Boolean) {
    val colors = appColors()

    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (filled) colors.accent else colors.accentSoft),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (filled) Color.White else colors.onAccentSoft,
            modifier = Modifier.size(20.dp)
        )
    }
}

/** Read-only row that opens a picker sheet. */
@Composable
internal fun FieldCard(
    icon: ImageVector,
    filled: Boolean,
    text: String,
    isPlaceholder: Boolean,
    onClick: () -> Unit
) {
    val colors = appColors()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconBadge(icon = icon, filled = filled)
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = text,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = if (isPlaceholder) colors.textMuted else colors.text,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = colors.textMuted,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

/** Row that hosts an inline single-line text field rather than a picker. */
@Composable
internal fun InputCard(
    icon: ImageVector,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String
) {
    val colors = appColors()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconBadge(icon = icon, filled = value.isNotBlank())
            Spacer(modifier = Modifier.width(12.dp))
            Box(modifier = Modifier.weight(1f)) {
                if (value.isEmpty()) {
                    Text(text = placeholder, fontSize = 16.sp, color = colors.textMuted)
                }
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(fontSize = 16.sp, color = colors.text),
                    cursorBrush = SolidColor(colors.accent),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
internal fun DateTimeCard(
    dateText: String,
    timeText: String,
    onDateClick: () -> Unit,
    onTimeClick: () -> Unit
) {
    val colors = appColors()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = onDateClick)
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconBadge(icon = Icons.Default.CalendarMonth, filled = false)
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = dateText,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.text,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            VerticalDivider(
                modifier = Modifier.height(32.dp),
                color = colors.textMuted.copy(alpha = 0.2f)
            )

            Row(
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = onTimeClick)
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconBadge(icon = Icons.Default.Schedule, filled = false)
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = timeText,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.text,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = colors.textMuted,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
internal fun DescriptionCard(
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit
) {
    val colors = appColors()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row {
                IconBadge(icon = Icons.Default.Description, filled = value.isNotBlank())
                Spacer(modifier = Modifier.width(12.dp))
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 56.dp)
                ) {
                    if (value.isEmpty()) {
                        Text(text = placeholder, fontSize = 15.sp, color = colors.textMuted)
                    }
                    BasicTextField(
                        value = value,
                        onValueChange = onValueChange,
                        textStyle = LocalTextStyle.current.copy(
                            fontSize = 15.sp,
                            color = colors.text
                        ),
                        cursorBrush = SolidColor(colors.accent),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Text(
                text = "${value.length}/$DESCRIPTION_LIMIT",
                fontSize = 12.sp,
                color = colors.textMuted,
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
internal fun TagsCard(tags: List<String>, onClick: () -> Unit) {
    val colors = appColors()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconBadge(icon = Icons.Default.LocalOffer, filled = tags.isNotEmpty())
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = if (tags.isEmpty()) "Add Tag" else tags.joinToString(", "),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = if (tags.isEmpty()) colors.textMuted else colors.text,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = colors.textMuted,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
internal fun InfoBanner(
    icon: ImageVector,
    title: String,
    body: String,
    illustration: String
) {
    val colors = appColors()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.accentSoft),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.surface),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = colors.onAccentSoft,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.onAccentSoft
                )
                Text(
                    text = body,
                    fontSize = 12.sp,
                    color = colors.onAccentSoft.copy(alpha = 0.85f)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(text = illustration, fontSize = 26.sp)
        }
    }
}

// ---------------------------------------------------------------------------
// Picker sheets
// ---------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun OptionSheet(
    title: String,
    onDismiss: () -> Unit,
    content: @Composable (dismiss: () -> Unit) -> Unit
) {
    val colors = appColors()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = colors.background
    ) {
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = colors.text,
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 12.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp)
        ) {
            content(onDismiss)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun OptionRow(
    leading: String,
    label: String,
    subtitle: String? = null,
    selected: Boolean,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null
) {
    val colors = appColors()

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(onClick = onClick, onLongClick = onLongClick)
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (selected) colors.accentSoft else colors.surfaceMuted),
                contentAlignment = Alignment.Center
            ) {
                Text(text = leading, fontSize = 18.sp)
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    fontSize = 15.sp,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                    color = colors.text
                )
                if (!subtitle.isNullOrBlank()) {
                    Text(text = subtitle, fontSize = 12.sp, color = colors.textMuted)
                }
            }

            if (selected) {
                Icon(
                    imageVector = Icons.Default.CheckCircleOutline,
                    contentDescription = null,
                    tint = colors.accent,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        HorizontalDivider(
            color = colors.textMuted.copy(alpha = 0.1f),
            modifier = Modifier.padding(start = 72.dp)
        )
    }
}

/** Shown inside a sheet when there is nothing to pick yet. */
@Composable
internal fun SheetEmptyState(text: String) {
    Text(
        text = text,
        fontSize = 13.sp,
        color = appColors().textMuted,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TagSheet(
    selected: List<String>,
    onToggle: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = appColors()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var customTag by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = colors.background
    ) {
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Text(
                text = "Add Tag",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = colors.text
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = customTag,
                onValueChange = { customTag = it },
                placeholder = { Text("Create your own tag", color = colors.textMuted) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = colors.accent,
                    unfocusedIndicatorColor = colors.textMuted.copy(alpha = 0.3f),
                    focusedTextColor = colors.text,
                    unfocusedTextColor = colors.text
                ),
                trailingIcon = {
                    Text(
                        text = "Add",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (customTag.isBlank()) colors.textMuted else colors.accent,
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable(enabled = customTag.isNotBlank()) {
                                val tag = customTag.trim()
                                if (tag.isNotBlank() && tag !in selected) onToggle(tag)
                                customTag = ""
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            )
        }

        val allTags = (tagSuggestions + selected).distinct()

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 320.dp)
                .padding(top = 12.dp, bottom = 24.dp)
        ) {
            items(allTags) { tag ->
                OptionRow(
                    leading = "🏷️",
                    label = tag,
                    selected = tag in selected,
                    onClick = { onToggle(tag) }
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------

/** "HDFC Bank •••• 2345" when the account number is known, otherwise just the name. */
internal fun accountLabel(account: Account): String {
    val digits = account.accountNumber.filter { it.isDigit() }
    return if (digits.length >= 4) {
        "${account.name} •••• ${digits.takeLast(4)}"
    } else {
        account.name
    }
}

/** Keeps the raw amount to digits with at most one decimal point and two decimals. */
internal fun sanitizeAmount(input: String): String {
    val filtered = input.filter { it.isDigit() || it == '.' }
    val firstDot = filtered.indexOf('.')
    if (firstDot < 0) return filtered

    val intPart = filtered.substring(0, firstDot)
    val decPart = filtered.substring(firstDot + 1).filter { it.isDigit() }.take(2)
    return "$intPart.$decPart"
}

/**
 * Renders the raw amount with Indian digit grouping (12,34,567.89) while the field
 * keeps storing plain digits.
 */
internal object AmountGroupingTransformation : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {
        val raw = text.text
        val intPart = raw.substringBefore('.')
        val hasDot = raw.contains('.')
        val decPart = if (hasDot) raw.substringAfter('.') else ""

        val groupedInt = groupIndian(intPart)
        val output = buildString {
            append(groupedInt)
            if (hasDot) {
                append('.')
                append(decPart)
            }
        }

        // Transformed index of each original index within the integer part.
        val intOffsets = IntArray(intPart.length + 1)
        var digitsSeen = 0
        groupedInt.forEachIndexed { index, char ->
            if (char != ',') {
                digitsSeen++
                intOffsets[digitsSeen] = index + 1
            }
        }

        val mapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int =
                if (offset <= intPart.length) {
                    intOffsets[offset.coerceAtLeast(0)]
                } else {
                    groupedInt.length + (offset - intPart.length)
                }

            override fun transformedToOriginal(offset: Int): Int =
                if (offset <= groupedInt.length) {
                    groupedInt.take(offset).count { it != ',' }
                } else {
                    intPart.length + (offset - groupedInt.length)
                }
        }

        return TransformedText(AnnotatedString(output), mapping)
    }

    /** Last three digits, then groups of two - the lakh/crore convention. */
    private fun groupIndian(digits: String): String {
        if (digits.length <= 3) return digits

        val head = digits.dropLast(3)
        val tail = digits.takeLast(3)
        val groupedHead = head.reversed()
            .chunked(2)
            .joinToString(",")
            .reversed()

        return "$groupedHead,$tail"
    }
}
