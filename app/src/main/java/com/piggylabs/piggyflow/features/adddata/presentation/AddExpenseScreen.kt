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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.piggylabs.piggyflow.core.designsystem.theme.appColors
import com.piggylabs.piggyflow.core.domain.model.Account
import com.piggylabs.piggyflow.core.domain.model.Expense
import com.piggylabs.piggyflow.core.domain.model.LedgerScope
import com.piggylabs.piggyflow.core.utils.ReceiptStorage
import com.piggylabs.piggyflow.features.accounts.presentation.AccountsViewModel
import com.piggylabs.piggyflow.features.adddata.presentation.components.AmountCard
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
import com.piggylabs.piggyflow.features.home.presentation.Category as EnumCategory
import com.piggylabs.piggyflow.features.home.presentation.HomeViewModel
import com.piggylabs.piggyflow.core.domain.model.Category as UserCategory
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/** Built-in spend categories, in the order the picker lists them. */
private val expenseCategories = listOf(
    EnumCategory.FOOD,
    EnumCategory.HOME,
    EnumCategory.GROCERIES,
    EnumCategory.TRANSPORT,
    EnumCategory.ENTERTAINMENT,
    EnumCategory.DRINKS,
    EnumCategory.SHOPPING,
    EnumCategory.POWER_BILL,
    EnumCategory.PHONE,
    EnumCategory.INTERNET,
    EnumCategory.FUEL,
    EnumCategory.OTHERS
)

private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMM yyyy")
private val timeDisplayFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("hh:mm a")
private val timeStorageFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

/** Which picker sheet is open, if any. */
private enum class ExpenseSheet { CURRENCY, CATEGORY, PAYMENT_METHOD, ACCOUNT, TAGS, NEW_CATEGORY }

/**
 * Manual expense entry, built to the Add Expense mockup. Shares its chrome with
 * [AddIncomeScreen]; the differences are the category picker (which also carries the
 * user's own categories) and the merchant field.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(navController: NavHostController, viewModel: HomeViewModel) {
    val colors = appColors()
    val context = LocalContext.current

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val userCategories = uiState.categories

    val accountsViewModel: AccountsViewModel = hiltViewModel()
    val accountsState by accountsViewModel.uiState.collectAsStateWithLifecycle()
    val accounts = accountsState.accounts

    var amountInput by remember { mutableStateOf("") }
    var currency by remember { mutableStateOf(currencyOptions.first()) }
    var selectedEnumCategory by remember { mutableStateOf<EnumCategory?>(null) }
    var selectedUserCategory by remember { mutableStateOf<UserCategory?>(null) }
    var date by remember { mutableStateOf(LocalDate.now()) }
    var time by remember { mutableStateOf(LocalTime.now().withSecond(0).withNano(0)) }
    var selectedAccount by remember { mutableStateOf<Account?>(null) }
    var accountScope by remember { mutableStateOf(LedgerScope.PERSONAL) }
    var merchant by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf(listOf<String>()) }
    var receiptPath by remember { mutableStateOf("") }

    var openSheet by remember { mutableStateOf<ExpenseSheet?>(null) }
    var categoryPendingDelete by remember { mutableStateOf<UserCategory?>(null) }

    val categoryName = selectedEnumCategory?.categoryName ?: selectedUserCategory?.name
    val categoryEmoji = selectedEnumCategory?.emoji ?: selectedUserCategory?.emoji ?: ""

    val amount = amountInput.toDoubleOrNull()
    val canSave = amount != null && amount > 0 && categoryName != null

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
        selectedEnumCategory = null
        selectedUserCategory = null
        date = LocalDate.now()
        time = LocalTime.now().withSecond(0).withNano(0)
        merchant = ""
        description = ""
        tags = emptyList()
        receiptPath = ""
    }

    fun save(addAnother: Boolean) {
        val enteredAmount = amountInput.toDoubleOrNull()
        val name = categoryName

        if (enteredAmount == null || enteredAmount <= 0) {
            Toast.makeText(context, "Enter a valid amount", Toast.LENGTH_SHORT).show()
            return
        }
        if (name == null) {
            Toast.makeText(context, "Select a category", Toast.LENGTH_SHORT).show()
            return
        }

        viewModel.addExpense(
            Expense(
                categoryType = "Expense",
                amount = enteredAmount,
                note = description.trim(),
                date = date.toString(),
                categoryName = name,
                categoryEmoji = categoryEmoji,
                currency = currency.code,
                time = time.format(timeStorageFormatter),
                paymentMethod = selectedAccount?.let { accountLabel(it) }.orEmpty(),
                accountId = selectedAccount?.id,
                accountScope = accountScope,
                merchant = merchant.trim(),
                tags = tags.joinToString(", "),
                receiptPath = receiptPath
            )
        )

        Toast.makeText(context, "Expense Added", Toast.LENGTH_SHORT).show()
        if (addAnother) resetForm() else navController.popBackStack()
    }

    Scaffold(
        containerColor = colors.background,
        topBar = {
            EntryTopBar(
                title = "Add Expense",
                subtitle = "Track every expense to stay on top",
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
                saveLabel = "Save Expense",
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
                amountColor = colors.text,
                onCurrencyClick = { openSheet = ExpenseSheet.CURRENCY }
            )

            if (receiptPath.isNotBlank()) {
                ReceiptChip(onRemove = {
                    ReceiptStorage.delete(receiptPath)
                    receiptPath = ""
                })
            }

            FieldLabel("Category")
            FieldCard(
                icon = Icons.Default.Category,
                filled = categoryName != null,
                text = categoryName ?: "Select a category",
                isPlaceholder = categoryName == null,
                onClick = { openSheet = ExpenseSheet.CATEGORY }
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
                onClick = { openSheet = ExpenseSheet.PAYMENT_METHOD }
            )

            FieldLabel("Account")
            FieldCard(
                icon = Icons.Default.AccountBalanceWallet,
                filled = true,
                text = accountScope,
                isPlaceholder = false,
                onClick = { openSheet = ExpenseSheet.ACCOUNT }
            )

            FieldLabel("Merchant (Optional)")
            InputCard(
                icon = Icons.Default.Storefront,
                value = merchant,
                onValueChange = { merchant = it },
                placeholder = "e.g. Zomato, Reliance Fresh"
            )

            FieldLabel("Description (Optional)")
            DescriptionCard(
                value = description,
                placeholder = "What was this expense for?",
                onValueChange = { if (it.length <= DESCRIPTION_LIMIT) description = it }
            )

            FieldLabel("Add to")
            TagsCard(tags = tags, onClick = { openSheet = ExpenseSheet.TAGS })

            Spacer(modifier = Modifier.height(6.dp))

            InfoBanner(
                icon = Icons.Default.AutoAwesome,
                title = "Keep your expenses organised!",
                body = "Add categories, tags and notes to track better.",
                illustration = "📋🐷"
            )

            Spacer(modifier = Modifier.height(12.dp))
        }
    }

    when (openSheet) {
        ExpenseSheet.CURRENCY -> CurrencySheet(
            selected = currency,
            onSelect = { currency = it },
            onDismiss = { openSheet = null }
        )

        ExpenseSheet.CATEGORY -> OptionSheet(
            title = "Category",
            onDismiss = { openSheet = null }
        ) { dismiss ->
            OptionRow(
                leading = "➕",
                label = "Create new category",
                selected = false,
                onClick = { openSheet = ExpenseSheet.NEW_CATEGORY }
            )

            userCategories.forEach { category ->
                OptionRow(
                    leading = category.emoji.ifBlank { "🔖" },
                    label = category.name,
                    subtitle = "Your category - long press to delete",
                    selected = category.id == selectedUserCategory?.id,
                    onClick = {
                        selectedUserCategory = category
                        selectedEnumCategory = null
                        dismiss()
                    },
                    onLongClick = { categoryPendingDelete = category }
                )
            }

            expenseCategories.forEach { option ->
                OptionRow(
                    leading = option.emoji,
                    label = option.categoryName,
                    selected = option == selectedEnumCategory,
                    onClick = {
                        selectedEnumCategory = option
                        selectedUserCategory = null
                        dismiss()
                    }
                )
            }
        }

        ExpenseSheet.PAYMENT_METHOD -> OptionSheet(
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

        ExpenseSheet.ACCOUNT -> LedgerScopeSheet(
            selected = accountScope,
            onSelect = { accountScope = it },
            onDismiss = { openSheet = null }
        )

        ExpenseSheet.TAGS -> TagSheet(
            selected = tags,
            onToggle = { tag -> tags = if (tag in tags) tags - tag else tags + tag },
            onDismiss = { openSheet = null }
        )

        ExpenseSheet.NEW_CATEGORY -> NewCategorySheet(
            onCreate = { name, emoji ->
                viewModel.addCategory(name = name, emoji = emoji)
                Toast.makeText(context, "Category added", Toast.LENGTH_SHORT).show()
                openSheet = ExpenseSheet.CATEGORY
            },
            onDismiss = { openSheet = ExpenseSheet.CATEGORY }
        )

        null -> Unit
    }

    categoryPendingDelete?.let { category ->
        AlertDialog(
            onDismissRequest = { categoryPendingDelete = null },
            title = { Text("Delete Category", color = colors.text) },
            text = {
                Text(
                    "\"${category.name}\" will be permanently deleted. Expenses already saved with it are not affected.",
                    color = colors.text
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteCategoryById(category.id)
                        if (selectedUserCategory?.id == category.id) selectedUserCategory = null
                        categoryPendingDelete = null
                        Toast.makeText(context, "Category Deleted", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.negative,
                        contentColor = Color.White
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                Button(
                    onClick = { categoryPendingDelete = null },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.surfaceMuted,
                        contentColor = colors.text
                    )
                ) {
                    Text("Cancel")
                }
            },
            containerColor = colors.background
        )
    }
}

/** Name + emoji form for a user-created category, kept from the old Add Data screen. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NewCategorySheet(
    onCreate: (name: String, emoji: String) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = appColors()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var name by remember { mutableStateOf("") }
    var emoji by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = colors.background
    ) {
        Column(
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "New Category",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = colors.text
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                placeholder = { Text("Category name", color = colors.textMuted) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = sheetFieldColors()
            )

            OutlinedTextField(
                value = emoji,
                onValueChange = { emoji = it },
                placeholder = { Text("Emoji (optional)", color = colors.textMuted) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = sheetFieldColors()
            )

            Button(
                onClick = { onCreate(name.trim(), emoji.trim()) },
                enabled = name.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.accent,
                    contentColor = Color.White,
                    disabledContainerColor = colors.accent.copy(alpha = 0.4f),
                    disabledContentColor = Color.White.copy(alpha = 0.7f)
                )
            ) {
                Text(text = "Add Category", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun sheetFieldColors() = TextFieldDefaults.colors(
    focusedContainerColor = Color.Transparent,
    unfocusedContainerColor = Color.Transparent,
    focusedIndicatorColor = appColors().accent,
    unfocusedIndicatorColor = appColors().textMuted.copy(alpha = 0.3f),
    focusedTextColor = appColors().text,
    unfocusedTextColor = appColors().text
)
