package com.piggylabs.piggyflow.features.profile.domain.usecase

import com.piggylabs.piggyflow.core.domain.model.Account
import com.piggylabs.piggyflow.core.domain.model.AccountType
import com.piggylabs.piggyflow.core.domain.model.Expense
import com.piggylabs.piggyflow.core.domain.model.Income
import com.piggylabs.piggyflow.core.domain.model.Subscription
import com.piggylabs.piggyflow.core.domain.repository.AccountRepository
import com.piggylabs.piggyflow.core.domain.repository.ExpenseRepository
import com.piggylabs.piggyflow.core.domain.repository.IncomeRepository
import com.piggylabs.piggyflow.core.domain.repository.SubscriptionRepository
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

private val ISO_DATE: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE

private data class SampleExpense(
    val categoryName: String,
    val emoji: String,
    val amount: Double,
    val note: String,
    val daysAgo: Long
)

private data class SampleIncome(
    val categoryName: String,
    val emoji: String,
    val amount: Double,
    val note: String,
    val daysAgo: Long
)

private data class SampleSubscription(
    val type: String,
    val name: String,
    val subType: String,
    val amount: Double,
    val dueInDays: Long
)

/**
 * Seeds a representative demo dataset - a few accounts, a month of expenses/income
 * across common categories, and a handful of subscriptions/EMIs - so a fresh install
 * looks like the mockups instead of empty. Dates are generated relative to "today"
 * so the data reads as current whenever it's loaded. This is a deliberate, manual
 * action (a Profile button), so it always adds the sample expenses/income/
 * subscriptions - the only de-duplication is on accounts, matched by name, so
 * tapping it more than once doesn't create repeat SBI Bank / Cash / etc. entries.
 *
 * Throws if the write fails; the caller decides how to surface that.
 */
class SeedSampleDataUseCase @Inject constructor(
    private val accountRepository: AccountRepository,
    private val expenseRepository: ExpenseRepository,
    private val incomeRepository: IncomeRepository,
    private val subscriptionRepository: SubscriptionRepository
) {

    suspend operator fun invoke() {
        val existingAccountNames = accountRepository.observeAccounts().first()
            .map { it.name }
            .toSet()

        val today = LocalDate.now()
        // Sample dates below are authored for a ~26-day spread. Scale that
        // range down to however many days are actually available so far this
        // month, so every seeded expense/income lands in the current calendar
        // month - otherwise "This Month Overview" looks sparse whenever the
        // month is young (e.g. seeding on the 3rd would push most of the
        // original spread into last month).
        val daysIntoMonth = (today.dayOfMonth - 1).coerceAtLeast(0)
        val originalSpreadDays = 26.0
        fun scaledDaysAgo(daysAgo: Long): Long =
            (daysAgo * daysIntoMonth / originalSpreadDays).toLong()
                .coerceIn(0, daysIntoMonth.toLong())

        SAMPLE_ACCOUNTS
            .filter { it.name !in existingAccountNames }
            .forEach { accountRepository.addAccount(it) }

        SAMPLE_EXPENSES.forEach { sample ->
            expenseRepository.addExpense(
                Expense(
                    categoryType = "expense",
                    amount = sample.amount,
                    note = sample.note,
                    date = today.minusDays(scaledDaysAgo(sample.daysAgo)).format(ISO_DATE),
                    categoryName = sample.categoryName,
                    categoryEmoji = sample.emoji
                )
            )
        }

        sampleIncomes(daysIntoMonth.toLong()).forEach { sample ->
            incomeRepository.addIncome(
                Income(
                    categoryType = "income",
                    amount = sample.amount,
                    note = sample.note,
                    date = today.minusDays(scaledDaysAgo(sample.daysAgo)).format(ISO_DATE),
                    categoryName = sample.categoryName,
                    categoryEmoji = sample.emoji
                )
            )
        }

        SAMPLE_SUBSCRIPTIONS.forEach { sample ->
            subscriptionRepository.addSubscription(
                Subscription(
                    type = sample.type,
                    name = sample.name,
                    subType = sample.subType,
                    amount = sample.amount,
                    dueDate = today.plusDays(sample.dueInDays).format(ISO_DATE)
                )
            )
        }
    }

    private fun sampleIncomes(salaryDaysAgo: Long) = listOf(
        SampleIncome("Salary", "💼", 45000.0, "Monthly Salary", salaryDaysAgo),
        SampleIncome("Freelance", "🧑‍💻", 8000.0, "Freelance project", 10),
        SampleIncome("Gifts", "🎉", 1500.0, "Gift", 20)
    )

    private companion object {
        val SAMPLE_ACCOUNTS = listOf(
            Account(
                name = "SBI Bank",
                type = AccountType.BANK,
                balance = 48560.0,
                accountNumber = "1234",
                colorArgb = 0xFF15803D
            ),
            Account(
                name = "HDFC Bank",
                type = AccountType.BANK,
                balance = 57000.0,
                accountNumber = "5678",
                colorArgb = 0xFF0369A1
            ),
            Account(
                name = "HDFC Credit Card",
                type = AccountType.CREDIT_CARD,
                balance = -12340.0,
                accountNumber = "2345",
                creditLimit = 150000.0,
                dueDay = 25,
                colorArgb = 0xFFB91C1C
            ),
            Account(
                name = "PhonePe Wallet",
                type = AccountType.WALLET,
                balance = 5250.0,
                colorArgb = 0xFF6D28D9
            ),
            Account(
                name = "Cash",
                type = AccountType.CASH,
                balance = 8750.0,
                colorArgb = 0xFFD97706
            )
        )

        val SAMPLE_EXPENSES = listOf(
            SampleExpense("Shopping", "🛍️", 1245.0, "DMart", 0),
            SampleExpense("Food & Dining", "🍔", 1450.0, "Zomato", 1),
            SampleExpense("Fuel", "⛽", 1850.0, "HP Petrol Pump", 1),
            SampleExpense("Shopping", "🛍️", 2199.0, "Amazon", 2),
            SampleExpense("Utilities", "💡", 599.0, "Airtel Prepaid", 2),
            SampleExpense("Food & Dining", "🍔", 1290.0, "Swiggy", 3),
            SampleExpense("Utilities", "💡", 1245.0, "Electricity Bill", 4),
            SampleExpense("Shopping", "🛍️", 1875.0, "DMart", 5),
            SampleExpense("Fuel", "⛽", 2250.0, "Reliance BP", 6),
            SampleExpense("Entertainment", "🎉", 649.0, "Netflix top-up", 6),
            SampleExpense("Food & Dining", "🍔", 1820.0, "Zomato", 8),
            SampleExpense("Shopping", "🛍️", 3450.0, "Amazon", 9),
            SampleExpense("Transport", "🚌", 380.0, "Uber", 9),
            SampleExpense("Fuel", "⛽", 2100.0, "HP Petrol Pump", 11),
            SampleExpense("Utilities", "🌐", 799.0, "Jio Fiber", 12),
            SampleExpense("Food & Dining", "🍔", 640.0, "DMart Cafe", 13),
            SampleExpense("Shopping", "🛍️", 2890.0, "Myntra", 15),
            SampleExpense("Entertainment", "🎉", 1200.0, "BookMyShow", 16),
            SampleExpense("Fuel", "⛽", 1980.0, "HP Petrol Pump", 18),
            SampleExpense("Food & Dining", "🍔", 1610.0, "Zomato", 20),
            SampleExpense("Shopping", "🛍️", 1799.0, "Amazon", 22),
            SampleExpense("Utilities", "💡", 1100.0, "HP Gas", 23),
            SampleExpense("Fuel", "⛽", 2340.0, "Reliance BP", 25),
            SampleExpense("Transport", "🚌", 450.0, "Uber", 26),
            SampleExpense("Food & Dining", "🍔", 2340.0, "Swiggy", 24)
        )

        val SAMPLE_SUBSCRIPTIONS = listOf(
            SampleSubscription("subscription", "Netflix", "monthly", 649.0, 3),
            SampleSubscription("subscription", "Spotify Premium", "monthly", 119.0, 5),
            SampleSubscription("subscription", "Amazon Prime", "yearly", 1499.0, 12),
            SampleSubscription("subscription", "Swiggy One", "monthly", 149.0, 20),
            SampleSubscription("emi", "Home Loan EMI", "monthly", 28500.0, 9),
            SampleSubscription("emi", "Bike EMI", "monthly", 4500.0, 15)
        )
    }
}
