package com.piggylabs.piggyflow.core.domain.model

/**
 * Pure domain models - no Room, no Android, no framework annotations.
 * These are the only shapes the domain and presentation layers deal with.
 */

data class Category(
    val id: Int = 0,
    val name: String = "",
    val emoji: String = ""
)

data class Expense(
    val id: Int = 0,
    val categoryType: String,
    val amount: Double,
    val note: String,
    val date: String,
    val categoryName: String,
    val categoryEmoji: String,
    /** ISO 4217 code the amount was entered in, e.g. "INR". */
    val currency: String = "INR",
    /** Time of day the money went out, stored 24h as "HH:mm". Blank when unknown. */
    val time: String = "",
    /** Display label of the account the money left, snapshotted so it survives account deletion. */
    val paymentMethod: String = "",
    /** [Account.id] behind [paymentMethod], when the user picked a saved account. */
    val accountId: Int? = null,
    /** Which ledger the entry belongs to - one of the values in [LedgerScope]. */
    val accountScope: String = LedgerScope.PERSONAL,
    /** Shop or payee the money went to. Optional. */
    val merchant: String = "",
    /** Comma separated free-form tags. */
    val tags: String = "",
    /** Absolute path of the receipt image copied into app storage. Blank when none. */
    val receiptPath: String = ""
)

data class Income(
    val id: Int = 0,
    val categoryType: String,
    val amount: Double,
    val note: String,
    val date: String,
    val categoryName: String,
    val categoryEmoji: String,
    /** ISO 4217 code the amount was entered in, e.g. "INR". */
    val currency: String = "INR",
    /** Time of day the income landed, stored 24h as "HH:mm". Blank when unknown. */
    val time: String = "",
    /** Display label of the account the money came into, snapshotted so it survives account deletion. */
    val paymentMethod: String = "",
    /** [Account.id] behind [paymentMethod], when the user picked a saved account. */
    val accountId: Int? = null,
    /** Which ledger the entry belongs to - one of the values in [LedgerScope]. */
    val accountScope: String = LedgerScope.PERSONAL,
    /** Employer or payer who sent the money. Optional. */
    val payer: String = "",
    /** Sub-type of the income, e.g. "Monthly Salary". Optional. */
    val incomeType: String = "",
    /** Comma separated free-form tags. */
    val tags: String = "",
    /** Absolute path of the receipt image copied into app storage. Blank when none. */
    val receiptPath: String = ""
)

/** The ledger an entry is recorded against, as stored in [Income.accountScope] / [Expense.accountScope]. */
object LedgerScope {
    const val PERSONAL = "Personal Account"
    const val BUSINESS = "Business Account"

    val all = listOf(PERSONAL, BUSINESS)
}

data class Subscription(
    val id: Int = 0,
    val type: String,
    val name: String,
    val subType: String,
    val amount: Double,
    val dueDate: String,
    val logoUrl: String = ""
)

data class Account(
    val id: Int = 0,
    val name: String,
    /** One of the values in [AccountType]. */
    val type: String,
    val balance: Double,
    val accountNumber: String = "",
    val creditLimit: Double? = null,
    /** Day of month a credit card payment is due, when [type] is a credit card. */
    val dueDay: Int? = null,
    val colorArgb: Long = 0xFF15803D,
    val createdAt: Long = System.currentTimeMillis()
)

object AccountType {
    const val BANK = "Bank Account"
    const val CREDIT_CARD = "Credit Card"
    const val WALLET = "E-Wallet"
    const val CASH = "Cash"
    const val BUSINESS = "Business"
    const val OTHER = "Other"

    val all = listOf(BANK, CREDIT_CARD, WALLET, CASH, BUSINESS, OTHER)
}

/**
 * A monthly spending limit for one category. Goals repeat every month rather than being
 * dated, so the same goal is measured against whichever month the user is looking at.
 */
data class BudgetGoal(
    val id: Int = 0,
    /** Matches [Expense.categoryName]; that is how spending is attributed to a goal. */
    val categoryName: String,
    val categoryEmoji: String = "",
    /** One of the values in [BudgetPriority]. */
    val priority: String = BudgetPriority.NEEDS,
    val monthlyLimit: Double,
    val createdAt: Long = System.currentTimeMillis()
)

/** How essential the spending is, as stored in [BudgetGoal.priority]. */
object BudgetPriority {
    const val NEEDS = "Needs"
    const val WANTS = "Wants"
    const val SAVINGS = "Savings"

    val all = listOf(NEEDS, WANTS, SAVINGS)
}

data class BusinessParty(
    val id: Int = 0,
    val name: String,
    val phone: String,
    val address: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class BusinessEntry(
    val id: Int = 0,
    val partyId: Int,
    val type: String,
    val amount: Double,
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

/** The two directions a ledger entry can run, as stored in [BusinessEntry.type]. */
object BusinessEntryType {
    const val GAVE = "gave"
    const val GOT = "got"
}
