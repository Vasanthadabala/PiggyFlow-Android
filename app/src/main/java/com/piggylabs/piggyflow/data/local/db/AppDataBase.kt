package com.piggylabs.piggyflow.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.piggylabs.piggyflow.data.local.dao.ExpenseDao
import com.piggylabs.piggyflow.data.local.dao.IncomeDao
import com.piggylabs.piggyflow.data.local.dao.BusinessEntryDao
import com.piggylabs.piggyflow.data.local.dao.BusinessPartyDao
import com.piggylabs.piggyflow.data.local.dao.SubscriptionDao
import com.piggylabs.piggyflow.data.local.dao.UserCategoryDao
import com.piggylabs.piggyflow.data.local.entity.BusinessEntryEntity
import com.piggylabs.piggyflow.data.local.entity.BusinessPartyEntity
import com.piggylabs.piggyflow.data.local.entity.ExpenseEntity
import com.piggylabs.piggyflow.data.local.entity.IncomeEntity
import com.piggylabs.piggyflow.data.local.entity.SubscriptionEntity
import com.piggylabs.piggyflow.data.local.entity.UserCategoryEntity
import kotlin.jvm.java

@Database(
    entities = [
        UserCategoryEntity::class,
        ExpenseEntity::class,
        IncomeEntity::class,
        SubscriptionEntity::class,
        BusinessPartyEntity::class,
        BusinessEntryEntity::class
    ],
    version = 8,
    exportSchema = false
)

abstract class AppDataBase : RoomDatabase() {
    abstract fun userCategoryDao(): UserCategoryDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun incomeDao(): IncomeDao
    abstract fun subscriptionDao(): SubscriptionDao
    abstract fun businessPartyDao(): BusinessPartyDao
    abstract fun businessEntryDao(): BusinessEntryDao

    companion object {
        @Volatile
        private var Instance: AppDataBase? = null

        fun getDatabase(context: Context): AppDataBase {
            return Instance ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDataBase::class.java,
                    "app_database"
                )
                    .fallbackToDestructiveMigration() // Resets database on version change
                    .build()
                Instance = instance
                instance
            }
        }

        fun getExistingInstance(): AppDataBase? = Instance

        fun clearInstance() {
            Instance = null
        }
    }
}
