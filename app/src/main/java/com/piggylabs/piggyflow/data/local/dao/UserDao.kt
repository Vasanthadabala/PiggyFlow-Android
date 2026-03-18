package com.piggylabs.piggyflow.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.piggylabs.piggyflow.data.local.entity.ExpenseEntity
import com.piggylabs.piggyflow.data.local.entity.IncomeEntity
import com.piggylabs.piggyflow.data.local.entity.BusinessEntryEntity
import com.piggylabs.piggyflow.data.local.entity.BusinessPartyEntity
import com.piggylabs.piggyflow.data.local.entity.SubscriptionEntity
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

@Dao
interface SubscriptionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubscription(subscription: SubscriptionEntity): Long

    @Query("SELECT * FROM subscription ORDER BY id DESC")
    fun getAllSubscriptions(): Flow<List<SubscriptionEntity>>

    @Query("UPDATE subscription SET logoUrl = :logoUrl WHERE id = :id")
    suspend fun updateSubscriptionLogo(id: Int, logoUrl: String)

    @Query("""
        UPDATE subscription
        SET type = :type,
            name = :name,
            subType = :subType,
            amount = :amount,
            dueDate = :dueDate,
            logoUrl = :logoUrl
        WHERE id = :id
    """)
    suspend fun updateSubscription(
        id: Int,
        type: String,
        name: String,
        subType: String,
        amount: Double,
        dueDate: String,
        logoUrl: String
    )

    @Query("DELETE FROM subscription WHERE id = :id")
    suspend fun deleteSubscriptionById(id: Int)
}

@Dao
interface BusinessPartyDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBusinessParty(party: BusinessPartyEntity): Long

    @Query("SELECT * FROM business_party ORDER BY updatedAt DESC, id DESC")
    fun getAllBusinessParties(): Flow<List<BusinessPartyEntity>>

    @Query("SELECT * FROM business_party WHERE id = :partyId LIMIT 1")
    fun observeBusinessPartyById(partyId: Int): Flow<BusinessPartyEntity?>

    @Query("UPDATE business_party SET updatedAt = :updatedAt WHERE id = :partyId")
    suspend fun updateBusinessPartyTimestamp(partyId: Int, updatedAt: Long)

    @Query("DELETE FROM business_party WHERE id = :partyId")
    suspend fun deleteBusinessPartyById(partyId: Int)
}

@Dao
interface BusinessEntryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBusinessEntry(entry: BusinessEntryEntity): Long

    @Query("SELECT * FROM business_entry WHERE partyId = :partyId ORDER BY createdAt DESC, id DESC")
    fun getBusinessEntriesForParty(partyId: Int): Flow<List<BusinessEntryEntity>>

    @Query("SELECT * FROM business_entry ORDER BY createdAt DESC, id DESC")
    fun getAllBusinessEntries(): Flow<List<BusinessEntryEntity>>

    @Query("DELETE FROM business_entry WHERE id = :entryId")
    suspend fun deleteBusinessEntryById(entryId: Int)

    @Query("DELETE FROM business_entry WHERE partyId = :partyId")
    suspend fun deleteBusinessEntriesForParty(partyId: Int)
}
