package com.piggylabs.piggyflow.data.local.repository

import android.content.Context
import com.piggylabs.piggyflow.data.local.db.AppDataBase
import com.piggylabs.piggyflow.data.local.entity.BusinessEntryEntity
import com.piggylabs.piggyflow.data.local.entity.BusinessPartyEntity
import com.piggylabs.piggyflow.data.local.entity.ExpenseEntity
import com.piggylabs.piggyflow.data.local.entity.IncomeEntity
import com.piggylabs.piggyflow.data.local.entity.SubscriptionEntity
import com.piggylabs.piggyflow.data.local.entity.UserCategoryEntity
import kotlinx.coroutines.flow.Flow

class UserRepository(context: Context) {

    private val db = AppDataBase.getDatabase(context)
    private val expenseDao = db.expenseDao()
    private val incomeDao = db.incomeDao()
    private val categoryDao = db.userCategoryDao()
    private val subscriptionDao = db.subscriptionDao()
    private val businessPartyDao = db.businessPartyDao()
    private val businessEntryDao = db.businessEntryDao()

    //Insert Data
    suspend fun addExpense(expense: ExpenseEntity) {
        expenseDao.insertExpense(expense)
    }

    suspend fun addIncome(income: IncomeEntity) =
        incomeDao.insertIncome(income)

    suspend fun addCategory(category: UserCategoryEntity) =
        categoryDao.insertUserCategory(category)

    suspend fun addSubscription(subscription: SubscriptionEntity): Long =
        subscriptionDao.insertSubscription(subscription)

    suspend fun addBusinessParty(party: BusinessPartyEntity): Long =
        businessPartyDao.insertBusinessParty(party)

    suspend fun addBusinessEntry(entry: BusinessEntryEntity): Long =
        businessEntryDao.insertBusinessEntry(entry)

    suspend fun updateBusinessPartyTimestamp(partyId: Int, updatedAt: Long) =
        businessPartyDao.updateBusinessPartyTimestamp(partyId, updatedAt)

    suspend fun updateSubscriptionLogo(id: Int, logoUrl: String) =
        subscriptionDao.updateSubscriptionLogo(id, logoUrl)

    suspend fun updateSubscription(subscription: SubscriptionEntity) =
        subscriptionDao.updateSubscription(
            id = subscription.id,
            type = subscription.type,
            name = subscription.name,
            subType = subscription.subType,
            amount = subscription.amount,
            dueDate = subscription.dueDate,
            logoUrl = subscription.logoUrl
        )

    //Update Data
    suspend fun updateExpense(expense: ExpenseEntity) =
        expenseDao.updateExpense(
            expense.id,
            expense.amount,
            expense.note,
            expense.date
        )

    suspend fun updateIncome(income: IncomeEntity) =
        incomeDao.updateIncome(
            income.id,
            income.amount,
            income.note,
            income.date
        )



    //Get Data
    fun getAllCategories(): Flow<List<UserCategoryEntity>> =
        categoryDao.getAllCategories()

    fun getAllExpenses(): Flow<List<ExpenseEntity>> =
        expenseDao.getAllExpenses()

    fun getAllIncome(): Flow<List<IncomeEntity>> =
        incomeDao.getAllIncome()

    fun getAllSubscriptions(): Flow<List<SubscriptionEntity>> =
        subscriptionDao.getAllSubscriptions()

    fun getAllBusinessParties(): Flow<List<BusinessPartyEntity>> =
        businessPartyDao.getAllBusinessParties()

    fun observeBusinessPartyById(partyId: Int): Flow<BusinessPartyEntity?> =
        businessPartyDao.observeBusinessPartyById(partyId)

    fun getBusinessEntriesForParty(partyId: Int): Flow<List<BusinessEntryEntity>> =
        businessEntryDao.getBusinessEntriesForParty(partyId)

    fun getAllBusinessEntries(): Flow<List<BusinessEntryEntity>> =
        businessEntryDao.getAllBusinessEntries()

    // ✅ GET BY ID
//    suspend fun getExpenseById(id: Int): ExpenseEntity? =
//        expenseDao.getExpenseById(id)
//
//    suspend fun getIncomeById(id: Int): IncomeEntity? =
//        incomeDao.getIncomeById(id)

    fun observeExpenseById(id: Int): Flow<ExpenseEntity?> =
        expenseDao.observeExpenseById(id)

    fun observeIncomeById(id: Int): Flow<IncomeEntity?> =
        incomeDao.observeIncomeById(id)

    // ✅ DELETE BY ID
    suspend fun deleteExpenseById(id: Int) =
        expenseDao.deleteExpenseById(id)

    suspend fun deleteIncomeById(id: Int) =
        incomeDao.deleteIncomeById(id)

    suspend fun deleteSubscriptionById(id: Int) =
        subscriptionDao.deleteSubscriptionById(id)

    // ✅ DELETE CATEGORY
    suspend fun deleteCategoryById(id: Int) =
        categoryDao.deleteCategoryById(id)

    suspend fun deleteBusinessEntryById(id: Int) =
        businessEntryDao.deleteBusinessEntryById(id)

    suspend fun deleteBusinessPartyById(id: Int) {
        businessEntryDao.deleteBusinessEntriesForParty(id)
        businessPartyDao.deleteBusinessPartyById(id)
    }

}
