package com.piggylabs.piggyflow.ui.screens.home.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.piggylabs.piggyflow.data.local.db.AppEvents
import com.piggylabs.piggyflow.data.local.entity.ExpenseEntity
import com.piggylabs.piggyflow.data.local.entity.IncomeEntity
import com.piggylabs.piggyflow.data.local.entity.UserCategoryEntity
import com.piggylabs.piggyflow.data.local.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class HomeViewModel(application: Application): AndroidViewModel(application){

    private var repo = UserRepository(application)

    var categories by mutableStateOf<List<UserCategoryEntity>>(emptyList())
        private set

    var expenses by mutableStateOf<List<ExpenseEntity>>(emptyList())
        private set

    var income by mutableStateOf<List<IncomeEntity>>(emptyList())
        private set

    init {
        observeCategories()
        observeExpenses()
        observeIncome()

        // Listen for DB recreated event and refresh
        viewModelScope.launch {
            AppEvents.dbRecreated.collect {
                onDbRecreated()
            }
        }
    }

    private fun onDbRecreated() {
        viewModelScope.launch {
            // 1) Clear in-memory state
            categories = emptyList()
            expenses = emptyList()
            income = emptyList()

            // 2) Recreate repository (so any DAO references inside repo use new Room instance)
            repo = UserRepository(getApplication())

            // 3) Re-subscribe / re-fetch data
            // If your repo returns cold flows from DAOs, re-subscribing is enough.
            // If repo caches results internally, ensure repo.clearCache() is implemented (optional).
            observeCategories()
            observeExpenses()
            observeIncome()
        }
    }


    //Observe All Data
    private fun observeCategories() {
        viewModelScope.launch {
            repo.getAllCategories().collect { list ->
                categories = list   // ✅ instant UI update
            }
        }
    }

    private fun observeExpenses() {
        viewModelScope.launch {
            repo.getAllExpenses().collect { list ->
                expenses = list
            }
        }
    }

    private fun observeIncome() {
        viewModelScope.launch {
            repo.getAllIncome().collect { list ->
                income = list
            }
        }
    }

    //Observe Data By Id
    // direct Flow returns
    fun observeExpenseById(id: Int): Flow<ExpenseEntity?> {
        return repo.observeExpenseById(id)
    }

    fun observeIncomeById(id: Int): Flow<IncomeEntity?> {
        return repo.observeIncomeById(id)
    }


    //Add Data
    fun addExpense(
        categoryType: String,
        amount: Double,
        note: String,
        date: String,
        categoryName: String,
        categoryEmoji: String
    ) = viewModelScope.launch {
        repo.addExpense(
            ExpenseEntity(
                categoryType = categoryType,
                amount  = amount,
                note = note,
                date = date,
                categoryName = categoryName,
                categoryEmoji = categoryEmoji
            )
        )
    }

    fun addIncome(
        categoryType: String,
        categoryName: String,
        categoryEmoji: String,
        amount: Double,
        note:String,
        date: String
    ) = viewModelScope.launch {
        repo.addIncome(
            IncomeEntity(
                categoryType = categoryType,
                amount = amount,
                note = note,
                date = date,
                categoryName = categoryName,
                categoryEmoji = categoryEmoji
            )
        )
    }

    fun addCategory(
        name: String,
        emoji: String
    )  = viewModelScope.launch {
        repo.addCategory(
            UserCategoryEntity(
                name = name,
                emoji = emoji
            )
        )
    }

    //Update Data
    fun updateExpense(expense: ExpenseEntity) = viewModelScope.launch {
        repo.updateExpense(expense)
    }

    fun updateIncome(income: IncomeEntity) = viewModelScope.launch {
        repo.updateIncome(income)
    }



    //Get Data by ID
//    fun getExpenseById(id: Int) = viewModelScope.launch {
//        selectedExpense = repo.getExpenseById(id)
//    }
//
//    fun getIncomeById(id: Int) = viewModelScope.launch {
//        selectedIncome = repo.getIncomeById(id)
//    }


    //Delete Data by ID
    fun deleteExpenseById(id: Int) = viewModelScope.launch {
        repo.deleteExpenseById(id)
    }

    fun deleteIncomeById(id: Int) = viewModelScope.launch {
        repo.deleteIncomeById(id)
    }

    // ✅ DELETE CATEGORY
    fun deleteCategoryById(id: Int) = viewModelScope.launch {
        repo.deleteCategoryById(id)
    }
}