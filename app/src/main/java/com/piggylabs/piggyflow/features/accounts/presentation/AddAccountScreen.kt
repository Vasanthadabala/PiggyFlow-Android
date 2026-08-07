package com.piggylabs.piggyflow.features.accounts.presentation

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContactPage
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.piggylabs.piggyflow.core.domain.model.AccountType
import com.piggylabs.piggyflow.features.accounts.presentation.AccountsViewModel
import com.piggylabs.piggyflow.core.designsystem.theme.appColors
import androidx.lifecycle.compose.collectAsStateWithLifecycle

private data class AccountTypeConfig(
    val type: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val color: Color
)

private val accountTypes = listOf(
    AccountTypeConfig(
        AccountType.BANK,
        "Bank Account",
        "Savings, Current or Salary Account",
        Icons.Default.AccountBalance,
        Color(0xFF15803D)
    ),
    AccountTypeConfig(
        AccountType.CREDIT_CARD,
        "Credit Card",
        "Add your credit card to track spends and payments",
        Icons.Default.CreditCard,
        Color(0xFF9B1B3F)
    ),
    AccountTypeConfig(
        AccountType.WALLET,
        "E-Wallet",
        "UPI, Paytm, PhonePe, Amazon Pay and more",
        Icons.Default.AccountBalanceWallet,
        Color(0xFF1D4ED8)
    ),
    AccountTypeConfig(
        AccountType.CASH,
        "Cash",
        "Physical cash in hand",
        Icons.Default.Wallet,
        Color(0xFFF5A623)
    ),
    AccountTypeConfig(
        AccountType.BUSINESS,
        "Business Account",
        "Business or company account",
        Icons.Default.BusinessCenter,
        Color(0xFF7C3AED)
    ),
    AccountTypeConfig(
        AccountType.OTHER,
        "Other",
        "Investments, Loans, Pocket Money and more",
        Icons.Default.MoreHoriz,
        Color(0xFF6B7280)
    )
)

/** Sub-types offered per account type in the "Account Type" dropdown. */
private val accountSubTypes = mapOf(
    AccountType.BANK to listOf("Savings Account", "Current Account", "Salary Account"),
    AccountType.CREDIT_CARD to listOf("Credit Card", "Charge Card"),
    AccountType.WALLET to listOf("Wallet", "UPI", "Prepaid Card"),
    AccountType.CASH to listOf("Physical Cash"),
    AccountType.BUSINESS to listOf("Business Account", "Company Account"),
    AccountType.OTHER to listOf("Investment", "Loan", "Pocket Money")
)

private val currencies = listOf("INR (₹)", "USD ($)", "EUR (€)", "GBP (£)", "AED (د.إ)")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAccountScreen(navController: NavHostController, accountId: Int? = null) {
    val context = LocalContext.current
    val colors = appColors()
    val viewModel: AccountsViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val existing = uiState.accounts.firstOrNull { it.id == accountId }

    var selectedType by remember { mutableStateOf(AccountType.BANK) }
    var accountName by remember { mutableStateOf("") }
    var balanceInput by remember { mutableStateOf("") }
    var accountSubType by remember { mutableStateOf("Savings Account") }
    var currency by remember { mutableStateOf("INR (₹)") }

    // Existing fields not in the new UI but kept so editing doesn't drop them.
    var accountNumber by remember { mutableStateOf("") }
    var creditLimit by remember { mutableStateOf<Double?>(null) }
    var dueDay by remember { mutableStateOf<Int?>(null) }
    var prefilled by remember { mutableStateOf(false) }

    LaunchedEffect(existing?.id) {
        val account = existing
        if (account != null && !prefilled) {
            selectedType = account.type
            accountName = account.name
            balanceInput = formatAmountInput(account.balance)
            accountNumber = account.accountNumber
            creditLimit = account.creditLimit
            dueDay = account.dueDay
            accountSubType = accountSubTypes[account.type]?.firstOrNull() ?: account.type
            prefilled = true
        }
    }

    fun save() {
        val name = accountName.trim()
        if (name.isBlank()) {
            Toast.makeText(context, "Please enter an account name", Toast.LENGTH_SHORT).show()
            return
        }

        val balance = balanceInput.trim().toDoubleOrNull()
        if (balanceInput.isNotBlank() && balance == null) {
            Toast.makeText(context, "Enter a valid balance amount", Toast.LENGTH_SHORT).show()
            return
        }

        val resolvedBalance = balance ?: 0.0
        val selectedColor = accountTypes.find { it.type == selectedType }
            ?.color?.toArgb()?.toLong() ?: 0xFF15803D

        if (existing != null) {
            viewModel.updateAccount(
                existing.copy(
                    name = name,
                    type = selectedType,
                    balance = resolvedBalance,
                    accountNumber = accountNumber,
                    creditLimit = creditLimit,
                    dueDay = dueDay,
                    colorArgb = selectedColor
                )
            )
            Toast.makeText(context, "Account updated", Toast.LENGTH_SHORT).show()
        } else {
            viewModel.addAccount(
                name = name,
                type = selectedType,
                balance = resolvedBalance,
                accountNumber = accountNumber,
                creditLimit = creditLimit,
                dueDay = dueDay,
                colorArgb = selectedColor
            )
            Toast.makeText(context, "Account added", Toast.LENGTH_SHORT).show()
        }
        navController.popBackStack()
    }

    Scaffold(
        containerColor = colors.background,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.background)
                    .statusBarsPadding()
                    .padding(top = 12.dp, bottom = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(colors.accentSoft)
                            .clickable { navController.popBackStack() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = colors.text,
                            modifier = Modifier.size(21.dp)
                        )
                    }

                    Text(
                        text = if (existing != null) "Edit Account" else "Add Account",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.accent
                    )

                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(colors.accentSoft),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.HelpOutline,
                            contentDescription = "Help",
                            tint = colors.accent,
                            modifier = Modifier.size(21.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Choose the type of account you want to add",
                    fontSize = 13.sp,
                    color = colors.textMuted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.background)
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Button(
                    onClick = { save() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1B6B37),
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = if (existing != null) "Save Changes" else "Add Account",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Select Account Type",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = colors.text
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column {
                    accountTypes.forEachIndexed { index, config ->
                        val isSelected = selectedType == config.type

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedType = config.type
                                    accountSubType = accountSubTypes[config.type]?.firstOrNull()
                                        ?: config.title
                                }
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(config.color),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = config.icon,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = config.title,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.text
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = config.subtitle,
                                    fontSize = 11.sp,
                                    color = colors.textMuted,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Icon(
                                imageVector = if (isSelected) {
                                    Icons.Default.CheckCircle
                                } else {
                                    Icons.AutoMirrored.Filled.ArrowForwardIos
                                },
                                contentDescription = null,
                                tint = if (isSelected) colors.accent else colors.textMuted,
                                modifier = Modifier.size(if (isSelected) 20.dp else 14.dp)
                            )
                        }

                        if (index < accountTypes.lastIndex) {
                            HorizontalDivider(
                                color = colors.textMuted.copy(alpha = 0.12f),
                                modifier = Modifier.padding(start = 76.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Account Details",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = colors.text
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    FieldLabel(text = "Account Name")
                    OutlinedTextField(
                        value = accountName,
                        onValueChange = { accountName = it },
                        placeholder = {
                            Text("e.g., SBI Savings Account", color = colors.textMuted)
                        },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.ContactPage,
                                contentDescription = null,
                                tint = colors.textMuted
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = fieldColors()
                    )

                    FieldLabel(text = "Account Type")
                    PickerField(
                        value = accountSubType,
                        options = accountSubTypes[selectedType].orEmpty(),
                        onSelect = { accountSubType = it }
                    )

                    FieldLabel(text = "Currency")
                    PickerField(
                        value = currency,
                        options = currencies,
                        onSelect = { currency = it }
                    )

                    Text(
                        text = "Opening Balance (Optional)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = colors.accent
                    )
                    OutlinedTextField(
                        value = balanceInput,
                        onValueChange = {
                            balanceInput = it.filter { ch -> ch.isDigit() || ch == '.' }
                        },
                        placeholder = { Text("0.00", color = colors.textMuted) },
                        prefix = {
                            Text(
                                text = "₹ ",
                                color = colors.text,
                                fontWeight = FontWeight.Medium
                            )
                        },
                        trailingIcon = {
                            Text(
                                text = ".00",
                                color = colors.textMuted,
                                modifier = Modifier.padding(end = 16.dp)
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = fieldColors(),
                        textStyle = TextStyle(textAlign = TextAlign.End)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(
        text = text,
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium,
        color = appColors().textMuted
    )
}

/** Read-only text field that drops a menu of [options] when tapped. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PickerField(
    value: String,
    options: List<String>,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val colors = appColors()

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = colors.textMuted
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = fieldColors()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}

@Composable
private fun fieldColors() = TextFieldDefaults.colors(
    focusedContainerColor = Color.Transparent,
    unfocusedContainerColor = Color.Transparent,
    focusedIndicatorColor = appColors().accent,
    unfocusedIndicatorColor = appColors().textMuted.copy(alpha = 0.3f),
    focusedTextColor = appColors().text,
    unfocusedTextColor = appColors().text
)

private fun formatAmountInput(value: Double): String =
    if (value == value.toLong().toDouble()) value.toLong().toString() else value.toString()
