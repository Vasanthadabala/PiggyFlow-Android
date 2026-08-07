package com.piggylabs.piggyflow.core.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room persistence models. These are storage details and never leave the data layer -
 * everything above it works with the pure models in
 * [com.piggylabs.piggyflow.core.domain.model], via the mappers in
 * [com.piggylabs.piggyflow.core.database.mapper].
 */

@Entity(tableName = "user_category")
data class UserCategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    var name: String = "",
    var emoji: String = ""
)

@Entity(tableName = "expense")
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val categoryType: String,
    val amount: Double,
    val note: String,
    val date: String,
    val categoryName: String,
    val categoryEmoji: String,
    /** Columns below were added in schema v12 - see AppDatabase.MIGRATION_11_12. */
    val currency: String = "INR",
    val time: String = "",
    val paymentMethod: String = "",
    val accountId: Int? = null,
    val accountScope: String = "Personal Account",
    val merchant: String = "",
    val tags: String = "",
    val receiptPath: String = ""
)

@Entity(tableName = "income")
data class IncomeEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val categoryType: String,
    val amount: Double,
    val note: String,
    val date: String,
    val categoryName: String,
    val categoryEmoji: String,
    /** Columns below were added in schema v10 - see AppDatabase.MIGRATION_9_10. */
    val currency: String = "INR",
    val time: String = "",
    val paymentMethod: String = "",
    val accountId: Int? = null,
    val accountScope: String = "Personal Account",
    val payer: String = "",
    val incomeType: String = "",
    val tags: String = "",
    val receiptPath: String = ""
)

@Entity(tableName = "subscription")
data class SubscriptionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: String,
    val name: String,
    val subType: String,
    val amount: Double,
    val dueDate: String,
    val logoUrl: String = ""
)

@Entity(tableName = "account")
data class AccountEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    /** One of the values in [com.piggylabs.piggyflow.core.domain.model.AccountType]. */
    val type: String,
    val balance: Double,
    val accountNumber: String = "",
    val creditLimit: Double? = null,
    /** Day of month a credit card payment is due, when [type] is a credit card. */
    val dueDay: Int? = null,
    val colorArgb: Long = 0xFF15803D,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "budget_goal")
data class BudgetGoalEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    /** Matches [ExpenseEntity.categoryName]; that is how spending is attributed to a goal. */
    val categoryName: String,
    val categoryEmoji: String = "",
    /** One of the values in [com.piggylabs.piggyflow.core.domain.model.BudgetPriority]. */
    val priority: String,
    /** The limit for a single month. Goals repeat every month rather than being dated. */
    val monthlyLimit: Double,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "business_party")
data class BusinessPartyEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val phone: String,
    val address: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "business_entry")
data class BusinessEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val partyId: Int,
    val type: String,
    val amount: Double,
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
