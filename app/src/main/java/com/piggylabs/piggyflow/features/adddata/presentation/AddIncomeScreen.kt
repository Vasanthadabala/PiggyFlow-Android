package com.piggylabs.piggyflow.features.adddata.presentation

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.piggylabs.piggyflow.core.designsystem.theme.appColors
import com.piggylabs.piggyflow.core.domain.model.Account
import com.piggylabs.piggyflow.core.domain.model.Income
import com.piggylabs.piggyflow.core.domain.model.LedgerScope
import com.piggylabs.piggyflow.core.utils.ReceiptStorage
import com.piggylabs.piggyflow.features.accounts.presentation.AccountsViewModel
import com.piggylabs.piggyflow.features.adddata.presentation.components.AmountCard
import com.piggylabs.piggyflow.features.adddata.presentation.components.CurrencyOption
import com.piggylabs.piggyflow.features.adddata.presentation.components.DESCRIPTION_LIMIT
import com.piggylabs.piggyflow.features.adddata.presentation.components.DateTimeCard
import com.piggylabs.piggyflow.features.adddata.presentation.components.DescriptionCard
import com.piggylabs.piggyflow.features.adddata.presentation.components.EntrySaveBar
import com.piggylabs.piggyflow.features.adddata.presentation.components.EntryTopBar
import com.piggylabs.piggyflow.features.adddata.presentation.components.FieldCard
import com.piggylabs.piggyflow.features.adddata.presentation.components.FieldLabel
import com.piggylabs.piggyflow.features.adddata.presentation.components.InfoBanner
import com.piggylabs.piggyflow.features.adddata.presentation.components.InputCard
import com.piggylabs.piggyflow.features.adddata.presentation.components.OptionRow
import com.piggylabs.piggyflow.features.adddata.presentation.components.OptionSheet
import com.piggylabs.piggyflow.features.adddata.presentation.components.ReceiptChip
import com.piggylabs.piggyflow.features.adddata.presentation.components.SheetEmptyState
import com.piggylabs.piggyflow.features.adddata.presentation.components.TagSheet
import com.piggylabs.piggyflow.features.adddata.presentation.components.TagsCard
import com.piggylabs.piggyflow.features.adddata.presentation.components.accountLabel
import com.piggylabs.piggyflow.features.adddata.presentation.components.currencyOptions
import com.piggylabs.piggyflow.features.adddata.presentation.components.sanitizeAmount
import com.piggylabs.piggyflow.features.home.presentation.Category
import com.piggylabs.piggyflow.features.home.presentation.HomeViewModel
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/** Sources offered in the "Source of Income" sheet, in the order the mockup lists them. */
private val incomeSources = listOf(
    Category.SALARY,
    Category.BUSINESS,
    Category.FREELANCE,
    Category.INVESTMENTS,
    Category.RENTAL,
    Category.INTEREST,
    Category.BONUS,
    Category.GIFTS,
    Category.REFUND,
    Category.OTHERS
)

/** Sub-types offered for a given source. Falls back to [genericIncomeTypes]. */
private val incomeTypesBySource = mapOf(
    Category.SALARY to listOf("Monthly Salary", "Arrears", "Incentive", "Overtime", "Reimbursement"),
    Category.BUSINESS to listOf("Sales Revenue", "Service Income", "Commission", "Profit Share"),
    Category.FREELANCE to listOf("Project Payment", "Retainer", "Milestone", "Consulting"),
    Category.INVESTMENTS to listOf("Dividend", "Capital Gain", "Mutual Fund Payout", "Maturity"),
    Category.RENTAL to listOf("Monthly Rent", "Advance", "Security Deposit"),
    Category.INTEREST to listOf("Savings Interest", "FD Interest", "Bond Interest"),
    Category.BONUS to listOf("Annual Bonus", "Festival Bonus", "Performance Bonus"),
    Category.GIFTS to listOf("Cash Gift", "Festival Gift"),
    Category.REFUND to listOf("Tax Refund", "Purchase Refund", "Cashback")
)

private val genericIncomeTypes = listOf("One-time", "Recurring", "Advance", "Reimbursement")

private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMM yyyy")
private val timeDisplayFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("hh:mm a")
private val timeStorageFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

/** Which picker sheet is open, if any. */
private enum class IncomeSheet { CURRENCY, SOURCE, PAYMENT_METHOD, ACCOUNT, TYPE, TAGS }

/**
 * Manual income entry, built to the Add Income mockup: an amount hero, the source and
 * placement of the money, and a set of optional details. Everything captured here is
 * persisted on [Income] - nothing on the form is decorative.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddIncomeScreen(navController: NavHostController, viewModel: HomeViewModel) {
    val colors = appColors()
    val context = LocalContext.current

    val accountsViewModel: AccountsViewModel = hiltViewModel()
    val accountsState by accountsViewModel.uiState.collectAsStateWithLifecycle()
    val accounts = accountsState.accounts

    var amountInput by remember { mutableStateOf("") }
    var currency by remember { mutableStateOf(currencyOptions.first()) }
    var source by remember { mutableStateOf<Category?>(null) }
    var date by remember { mutableStateOf(LocalDate.now()) }
    var time by remember { mutableStateOf(LocalTime.now().withSecond(0).withNano(0)) }
    var selectedAccount by remember { mutableStateOf<Account?>(null) }
    var accountScope by remember { mutableStateOf(LedgerScope.PERSONAL) }
    var payer by remember { mutableStateOf("") }
    var incomeType by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf(listOf<String>()) }
    var receiptPath by remember { mutableStateOf("") }

    var openSheet by remember { mutableStateOf<IncomeSheet?>(null) }

    val amount = amountInput.toDoubleOrNull()
    val canSave = amount != null && amount > 0 && source != null

    val receiptLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        val stored = ReceiptStorage.copyToInternal(context, uri)
        if (stored == null) {
            Toast.makeText(context, "Could not attach that receipt", Toast.LENGTH_SHORT).show()
        } else {
            ReceiptStorage.delete(receiptPath)
            receiptPath = stored
        }
    }

    val datePickerDialog = remember(date) {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth -> date = LocalDate.of(year, month + 1, dayOfMonth) },
            date.year,
            date.monthValue - 1,
            date.dayOfMonth
        )
    }

    val timePickerDialog = remember(time) {
        TimePickerDialog(
            context,
            { _, hour, minute -> time = LocalTime.of(hour, minute) },
            time.hour,
            time.minute,
            false
        )
    }

    fun resetForm() {
        amountInput = ""
        source = null
        date = LocalDate.now()
        time = LocalTime.now().withSecond(0).withNano(0)
        payer = ""
        incomeType = ""
        description = ""
        tags = emptyList()
        receiptPath = ""
    }

    fun save(addAnother: Boolean) {
        val enteredAmount = amountInput.toDoubleOrNull()
        val selectedSource = source

        if (enteredAmount == null || enteredAmount <= 0) {
            Toast.makeText(context, "Enter a valid amount", Toast.LENGTH_SHORT).show()
            return
        }
        if (selectedSource == null) {
            Toast.makeText(context, "Select a source of income", Toast.LENGTH_SHORT).show()
            return
        }

        viewModel.addIncome(
            Income(
                categoryType = "Income",
                amount = enteredAmount,
                note = description.trim(),
                date = date.toString(),
                categoryName = selectedSource.categoryName,
                categoryEmoji = selectedSource.emoji,
                currency = currency.code,
                time = time.format(timeStorageFormatter),
                paymentMethod = selectedAccount?.let { accountLabel(it) }.orEmpty(),
                accountId = selectedAccount?.id,
                accountScope = accountScope,
                payer = payer.trim(),
                incomeType = incomeType,
                tags = tags.joinToString(", "),
                receiptPath = receiptPath
            )
        )

        Toast.makeText(context, "Income Added", Toast.LENGTH_SHORT).show()
        if (addAnother) resetForm() else navController.popBackStack()
    }

    Scaffold(
        containerColor = colors.background,
        topBar = {
            EntryTopBar(
                title = "Add Income",
                subtitle = "Record your income and grow your finances",
                hasReceipt = receiptPath.isNotBlank(),
                onBack = { navController.popBackStack() },
                onReceipt = {
                    receiptLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }
            )
        },
        bottomBar = {
            EntrySaveBar(
                saveLabel = "Save Income",
                enabled = canSave,
                onSave = { save(addAnother = false) },
                onSaveAndAddAnother = { save(addAnother = true) }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            AmountCard(
                amountInput = amountInput,
                onAmountChange = { amountInput = sanitizeAmount(it) },
                currency = currency,
                amountColor = colors.accent,
                onCurrencyClick = { openSheet = IncomeSheet.CURRENCY }
            )

            if (receiptPath.isNotBlank()) {
                ReceiptChip(onRemove = {
                    ReceiptStorage.delete(receiptPath)
                    receiptPath = ""
                })
            }

            FieldLabel("Source of Income")
            FieldCard(
                icon = Icons.Default.Work,
                filled = source != null,
                text = source?.categoryName ?: "Select a source",
                isPlaceholder = source == null,
                onClick = { openSheet = IncomeSheet.SOURCE }
            )

            FieldLabel("Date & Time")
            DateTimeCard(
                dateText = date.format(dateFormatter),
                timeText = time.format(timeDisplayFormatter),
                onDateClick = { datePickerDialog.show() },
                onTimeClick = { timePickerDialog.show() }
            )

            FieldLabel("Payment Method")
            FieldCard(
                icon = Icons.Default.CreditCard,
                filled = selectedAccount != null,
                text = selectedAccount?.let { accountLabel(it) } ?: "Select an account",
                isPlaceholder = selectedAccount == null,
                onClick = { openSheet = IncomeSheet.PAYMENT_METHOD }
            )

            FieldLabel("Account")
            FieldCard(
                icon = Icons.Default.AccountBalanceWallet,
                filled = true,
                text = accountScope,
                isPlaceholder = false,
                onClick = { openSheet = IncomeSheet.ACCOUNT }
            )

            FieldLabel("Employer / Payer (Optional)")
            InputCard(
                icon = Icons.Default.Apartment,
                value = payer,
                onValueChange = { payer = it },
                placeholder = "e.g. Your Company Name"
            )

            FieldLabel("Type (Optional)")
            FieldCard(
                icon = Icons.Default.LocalOffer,
                filled = incomeType.isNotBlank(),
                text = incomeType.ifBlank { "Select a type" },
                isPlaceholder = incomeType.isBlank(),
                onClick = { openSheet = IncomeSheet.TYPE }
            )

            FieldLabel("Description (Optional)")
            DescriptionCard(
                value = description,
                placeholder = "Add a note (e.g. May salary, Freelance project)",
                onValueChange = { if (it.length <= DESCRIPTION_LIMIT) description = it }
            )

            FieldLabel("Add to")
            TagsCard(tags = tags, onClick = { openSheet = IncomeSheet.TAGS })

            Spacer(modifier = Modifier.height(6.dp))

            InfoBanner(
                icon = Icons.Default.ShowChart,
                title = "Track income to know your real growth",
                body = "Monitoring income helps you plan better and achieve your goals.",
                illustration = "💰📈"
            )

            Spacer(modifier = Modifier.height(12.dp))
        }
    }

    when (openSheet) {
        IncomeSheet.CURRENCY -> CurrencySheet(
            selected = currency,
            onSelect = { currency = it },
            onDismiss = { openSheet = null }
        )

        IncomeSheet.SOURCE -> OptionSheet(
            title = "Source of Income",
            onDismiss = { openSheet = null }
        ) { dismiss ->
            incomeSources.forEach { option ->
                OptionRow(
                    leading = option.emoji,
                    label = option.categoryName,
                    selected = option == source,
                    onClick = {
                        source = option
                        // The old sub-type no longer belongs to the new source.
                        if (incomeType !in typesFor(option)) incomeType = ""
                        dismiss()
                    }
                )
            }
        }

        IncomeSheet.PAYMENT_METHOD -> OptionSheet(
            title = "Payment Method",
            onDismiss = { openSheet = null }
        ) { dismiss ->
            if (accounts.isEmpty()) {
                SheetEmptyState("No accounts yet. Add one from the Accounts screen to pick it here.")
            }
            accounts.forEach { account ->
                OptionRow(
                    leading = "💳",
                    label = accountLabel(account),
                    subtitle = account.type,
                    selected = account.id == selectedAccount?.id,
                    onClick = {
                        selectedAccount = account
                        dismiss()
                    }
                )
            }
        }

        IncomeSheet.ACCOUNT -> LedgerScopeSheet(
            selected = accountScope,
            onSelect = { accountScope = it },
            onDismiss = { openSheet = null }
        )

        IncomeSheet.TYPE -> OptionSheet(
            title = "Type",
            onDismiss = { openSheet = null }
        ) { dismiss ->
            typesFor(source).forEach { option ->
                OptionRow(
                    leading = "🏷️",
                    label = option,
                    selected = option == incomeType,
                    onClick = {
                        incomeType = option
                        dismiss()
                    }
                )
            }
        }

        IncomeSheet.TAGS -> TagSheet(
            selected = tags,
            onToggle = { tag -> tags = if (tag in tags) tags - tag else tags + tag },
            onDismiss = { openSheet = null }
        )

        null -> Unit
    }
}

/** Currency and ledger pickers are identical on both forms, so they live next to each other. */
@Composable
internal fun CurrencySheet(
    selected: CurrencyOption,
    onSelect: (CurrencyOption) -> Unit,
    onDismiss: () -> Unit
) {
    OptionSheet(title = "Currency", onDismiss = onDismiss) { dismiss ->
        currencyOptions.forEach { option ->
            OptionRow(
                leading = option.symbol,
                label = option.label,
                selected = option.code == selected.code,
                onClick = {
                    onSelect(option)
                    dismiss()
                }
            )
        }
    }
}

@Composable
internal fun LedgerScopeSheet(
    selected: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    OptionSheet(title = "Account", onDismiss = onDismiss) { dismiss ->
        LedgerScope.all.forEach { option ->
            OptionRow(
                leading = if (option == LedgerScope.PERSONAL) "🙋" else "🏢",
                label = option,
                selected = option == selected,
                onClick = {
                    onSelect(option)
                    dismiss()
                }
            )
        }
    }
}

private fun typesFor(source: Category?): List<String> =
    incomeTypesBySource[source] ?: genericIncomeTypes
