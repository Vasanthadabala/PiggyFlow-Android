package com.piggylabs.piggyflow.core.domain.repository

import com.piggylabs.piggyflow.core.domain.model.Account
import com.piggylabs.piggyflow.core.domain.model.BudgetGoal
import com.piggylabs.piggyflow.core.domain.model.BusinessEntry
import com.piggylabs.piggyflow.core.domain.model.BusinessParty
import com.piggylabs.piggyflow.core.domain.model.Category
import com.piggylabs.piggyflow.core.domain.model.Expense
import com.piggylabs.piggyflow.core.domain.model.Income
import com.piggylabs.piggyflow.core.domain.model.Subscription
import kotlinx.coroutines.flow.Flow

/**
 * Repository contracts owned by the domain layer. Implementations live in
 * [com.piggylabs.piggyflow.core.data.repository] and are bound by Hilt, so nothing
 * above this layer knows Room exists.
 */

interface CategoryRepository {
    fun observeCategories(): Flow<List<Category>>
    suspend fun addCategory(category: Category)
    suspend fun deleteCategory(id: Int)
}

interface ExpenseRepository {
    fun observeExpenses(): Flow<List<Expense>>
    fun observeExpenseById(id: Int): Flow<Expense?>
    suspend fun addExpense(expense: Expense)
    suspend fun updateExpense(expense: Expense)
    suspend fun deleteExpense(id: Int)
}

interface IncomeRepository {
    fun observeIncome(): Flow<List<Income>>
    fun observeIncomeById(id: Int): Flow<Income?>
    suspend fun addIncome(income: Income)
    suspend fun updateIncome(income: Income)
    suspend fun deleteIncome(id: Int)
}

interface SubscriptionRepository {
    fun observeSubscriptions(): Flow<List<Subscription>>
    /** @return the row id of the inserted subscription. */
    suspend fun addSubscription(subscription: Subscription): Long
    suspend fun updateSubscription(subscription: Subscription)
    suspend fun updateSubscriptionLogo(id: Int, logoUrl: String)
    suspend fun deleteSubscription(id: Int)
}

interface AccountRepository {
    fun observeAccounts(): Flow<List<Account>>
    fun observeAccountById(id: Int): Flow<Account?>
    /** @return the row id of the inserted account. */
    suspend fun addAccount(account: Account): Long
    suspend fun updateAccount(account: Account)
    suspend fun deleteAccount(id: Int)
}

interface BudgetGoalRepository {
    fun observeBudgetGoals(): Flow<List<BudgetGoal>>
    fun observeBudgetGoalById(id: Int): Flow<BudgetGoal?>
    /** @return the row id of the inserted goal. */
    suspend fun addBudgetGoal(goal: BudgetGoal): Long
    suspend fun updateBudgetGoal(goal: BudgetGoal)
    suspend fun deleteBudgetGoal(id: Int)
}

interface BusinessRepository {
    fun observeParties(): Flow<List<BusinessParty>>
    fun observePartyById(partyId: Int): Flow<BusinessParty?>
    fun observeEntries(): Flow<List<BusinessEntry>>
    fun observeEntriesForParty(partyId: Int): Flow<List<BusinessEntry>>
    /** @return the row id of the inserted party. */
    suspend fun addParty(party: BusinessParty): Long
    /** @return the row id of the inserted entry. */
    suspend fun addEntry(entry: BusinessEntry): Long
    suspend fun touchParty(partyId: Int, updatedAt: Long)
    suspend fun deleteEntry(entryId: Int)
    /** Deletes the party together with every entry belonging to it. */
    suspend fun deleteParty(partyId: Int)
}
