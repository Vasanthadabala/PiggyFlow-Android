package com.piggylabs.piggyflow.core.database.mapper

import com.piggylabs.piggyflow.core.database.entities.AccountEntity
import com.piggylabs.piggyflow.core.database.entities.BusinessEntryEntity
import com.piggylabs.piggyflow.core.database.entities.BudgetGoalEntity
import com.piggylabs.piggyflow.core.database.entities.BusinessPartyEntity
import com.piggylabs.piggyflow.core.database.entities.ExpenseEntity
import com.piggylabs.piggyflow.core.database.entities.IncomeEntity
import com.piggylabs.piggyflow.core.database.entities.SubscriptionEntity
import com.piggylabs.piggyflow.core.database.entities.UserCategoryEntity
import com.piggylabs.piggyflow.core.domain.model.Account
import com.piggylabs.piggyflow.core.domain.model.BusinessEntry
import com.piggylabs.piggyflow.core.domain.model.BudgetGoal
import com.piggylabs.piggyflow.core.domain.model.BusinessParty
import com.piggylabs.piggyflow.core.domain.model.Category
import com.piggylabs.piggyflow.core.domain.model.Expense
import com.piggylabs.piggyflow.core.domain.model.Income
import com.piggylabs.piggyflow.core.domain.model.Subscription

/** Room entity <-> domain model translation. Kept in one place so the boundary is easy to audit. */

fun UserCategoryEntity.toDomain() = Category(id = id, name = name, emoji = emoji)

fun Category.toEntity() = UserCategoryEntity(id = id, name = name, emoji = emoji)

fun ExpenseEntity.toDomain() = Expense(
    id = id,
    categoryType = categoryType,
    amount = amount,
    note = note,
    date = date,
    categoryName = categoryName,
    categoryEmoji = categoryEmoji,
    currency = currency,
    time = time,
    paymentMethod = paymentMethod,
    accountId = accountId,
    accountScope = accountScope,
    merchant = merchant,
    tags = tags,
    receiptPath = receiptPath
)

fun Expense.toEntity() = ExpenseEntity(
    id = id,
    categoryType = categoryType,
    amount = amount,
    note = note,
    date = date,
    categoryName = categoryName,
    categoryEmoji = categoryEmoji,
    currency = currency,
    time = time,
    paymentMethod = paymentMethod,
    accountId = accountId,
    accountScope = accountScope,
    merchant = merchant,
    tags = tags,
    receiptPath = receiptPath
)

fun IncomeEntity.toDomain() = Income(
    id = id,
    categoryType = categoryType,
    amount = amount,
    note = note,
    date = date,
    categoryName = categoryName,
    categoryEmoji = categoryEmoji,
    currency = currency,
    time = time,
    paymentMethod = paymentMethod,
    accountId = accountId,
    accountScope = accountScope,
    payer = payer,
    incomeType = incomeType,
    tags = tags,
    receiptPath = receiptPath
)

fun Income.toEntity() = IncomeEntity(
    id = id,
    categoryType = categoryType,
    amount = amount,
    note = note,
    date = date,
    categoryName = categoryName,
    categoryEmoji = categoryEmoji,
    currency = currency,
    time = time,
    paymentMethod = paymentMethod,
    accountId = accountId,
    accountScope = accountScope,
    payer = payer,
    incomeType = incomeType,
    tags = tags,
    receiptPath = receiptPath
)

fun SubscriptionEntity.toDomain() = Subscription(
    id = id,
    type = type,
    name = name,
    subType = subType,
    amount = amount,
    dueDate = dueDate,
    logoUrl = logoUrl
)

fun Subscription.toEntity() = SubscriptionEntity(
    id = id,
    type = type,
    name = name,
    subType = subType,
    amount = amount,
    dueDate = dueDate,
    logoUrl = logoUrl
)

fun AccountEntity.toDomain() = Account(
    id = id,
    name = name,
    type = type,
    balance = balance,
    accountNumber = accountNumber,
    creditLimit = creditLimit,
    dueDay = dueDay,
    colorArgb = colorArgb,
    createdAt = createdAt
)

fun Account.toEntity() = AccountEntity(
    id = id,
    name = name,
    type = type,
    balance = balance,
    accountNumber = accountNumber,
    creditLimit = creditLimit,
    dueDay = dueDay,
    colorArgb = colorArgb,
    createdAt = createdAt
)

fun BudgetGoalEntity.toDomain() = BudgetGoal(
    id = id,
    categoryName = categoryName,
    categoryEmoji = categoryEmoji,
    priority = priority,
    monthlyLimit = monthlyLimit,
    createdAt = createdAt
)

fun BudgetGoal.toEntity() = BudgetGoalEntity(
    id = id,
    categoryName = categoryName,
    categoryEmoji = categoryEmoji,
    priority = priority,
    monthlyLimit = monthlyLimit,
    createdAt = createdAt
)

fun BusinessPartyEntity.toDomain() = BusinessParty(
    id = id,
    name = name,
    phone = phone,
    address = address,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun BusinessParty.toEntity() = BusinessPartyEntity(
    id = id,
    name = name,
    phone = phone,
    address = address,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun BusinessEntryEntity.toDomain() = BusinessEntry(
    id = id,
    partyId = partyId,
    type = type,
    amount = amount,
    note = note,
    createdAt = createdAt
)

fun BusinessEntry.toEntity() = BusinessEntryEntity(
    id = id,
    partyId = partyId,
    type = type,
    amount = amount,
    note = note,
    createdAt = createdAt
)
