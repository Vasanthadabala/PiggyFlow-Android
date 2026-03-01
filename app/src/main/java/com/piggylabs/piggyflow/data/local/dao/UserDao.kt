package com.piggylabs.piggyflow.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.piggylabs.piggyflow.data.local.entity.ExpenseEntity
import com.piggylabs.piggyflow.data.local.entity.IncomeEntity
import com.piggylabs.piggyflow.data.local.entity.UserCategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserCategoryDao{
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserCategory(category: UserCategoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllCategories(categories: List<UserCategoryEntity>)

    @Query("SELECT * FROM user_category ")
    fun getAllCategories(): Flow<List<UserCategoryEntity>>

    // ✅ DELETE CATEGORY
    @Query("DELETE FROM user_category WHERE id = :id")
    suspend fun deleteCategoryById(id: Int)

    @Query("DELETE FROM user_category")
    suspend fun clearAllCategories()

}

@Dao
interface ExpenseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllExpenses(expenses: List<ExpenseEntity>)

    @Query("SELECT * FROM expense ORDER BY id DESC")
    fun getAllExpenses(): Flow<List<ExpenseEntity>>

    // ✅ GET BY ID
//    @Query("SELECT * FROM expense WHERE id = :id LIMIT 1")
//    suspend fun getExpenseById(id: Int): ExpenseEntity?

    @Query("SELECT * FROM expense WHERE id = :id LIMIT 1")
    fun observeExpenseById(id: Int): Flow<ExpenseEntity?>


    // ✅ DELETE BY ID
    @Query("DELETE FROM expense WHERE id = :id")
    suspend fun deleteExpenseById(id: Int)

    @Query("DELETE FROM expense")
    suspend fun clearAllExpenses()

    @Query("""
    UPDATE expense 
    SET amount = :amount,
        note = :note,
        date = :date
    WHERE id = :id
""")
    suspend fun updateExpense(
        id: Int,
        amount: Double,
        note: String,
        date: String
    )
}


@Dao
interface IncomeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIncome(income: IncomeEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllIncome(incomes: List<IncomeEntity>)

    @Query("SELECT * FROM income ORDER BY id DESC")
    fun getAllIncome(): Flow<List<IncomeEntity>>

    // ✅ GET BY ID
//    @Query("SELECT * FROM income WHERE id = :id LIMIT 1")
//    suspend fun getIncomeById(id: Int): IncomeEntity?

    @Query("SELECT * FROM income WHERE id = :id LIMIT 1")
    fun observeIncomeById(id: Int): Flow<IncomeEntity?>

    // ✅ DELETE BY ID
    @Query("DELETE FROM income WHERE id = :id")
    suspend fun deleteIncomeById(id: Int)

    @Query("DELETE FROM income")
    suspend fun clearAllIncome()

    @Query("""
    UPDATE income 
    SET amount = :amount,
        note = :note,
        date = :date
    WHERE id = :id
""")
    suspend fun updateIncome(
        id: Int,
        amount: Double,
        note: String,
        date: String
    )
}
