package com.piggylabs.piggyflow.data.local.repository

import android.content.Context
import com.piggylabs.piggyflow.data.local.db.AppDataBase
import com.piggylabs.piggyflow.data.local.entity.ExpenseEntity
import com.piggylabs.piggyflow.data.local.entity.IncomeEntity
import com.piggylabs.piggyflow.data.local.entity.UserCategoryEntity
import kotlinx.coroutines.flow.Flow

class UserRepository(context: Context) {

    private val db = AppDataBase.getDatabase(context)
    private val expenseDao = db.expenseDao()
    private val incomeDao = db.incomeDao()
    private val categoryDao = db.userCategoryDao()

    //Insert Data
    suspend fun addExpense(expense: ExpenseEntity) {
        expenseDao.insertExpense(expense)
    }

    suspend fun addIncome(income: IncomeEntity) =
        incomeDao.insertIncome(income)

    suspend fun addCategory(category: UserCategoryEntity) =
        categoryDao.insertUserCategory(category)

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

    // ✅ DELETE CATEGORY
    suspend fun deleteCategoryById(id: Int) =
        categoryDao.deleteCategoryById(id)

}
