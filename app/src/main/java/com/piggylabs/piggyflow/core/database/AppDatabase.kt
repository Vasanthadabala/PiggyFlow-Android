package com.piggylabs.piggyflow.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.piggylabs.piggyflow.core.database.dao.AccountDao
import com.piggylabs.piggyflow.core.database.dao.BudgetGoalDao
import com.piggylabs.piggyflow.core.database.dao.ExpenseDao
import com.piggylabs.piggyflow.core.database.dao.IncomeDao
import com.piggylabs.piggyflow.core.database.dao.BusinessEntryDao
import com.piggylabs.piggyflow.core.database.dao.BusinessPartyDao
import com.piggylabs.piggyflow.core.database.dao.SubscriptionDao
import com.piggylabs.piggyflow.core.database.dao.UserCategoryDao
import com.piggylabs.piggyflow.core.database.entities.AccountEntity
import com.piggylabs.piggyflow.core.database.entities.BudgetGoalEntity
import com.piggylabs.piggyflow.core.database.entities.BusinessEntryEntity
import com.piggylabs.piggyflow.core.database.entities.BusinessPartyEntity
import com.piggylabs.piggyflow.core.database.entities.ExpenseEntity
import com.piggylabs.piggyflow.core.database.entities.IncomeEntity
import com.piggylabs.piggyflow.core.database.entities.SubscriptionEntity
import com.piggylabs.piggyflow.core.database.entities.UserCategoryEntity
import kotlin.jvm.java

@Database(
    entities = [
        UserCategoryEntity::class,
        ExpenseEntity::class,
        IncomeEntity::class,
        SubscriptionEntity::class,
        BusinessPartyEntity::class,
        BusinessEntryEntity::class,
        AccountEntity::class,
        BudgetGoalEntity::class
    ],
    version = 12,
    exportSchema = false
)

abstract class AppDatabase : RoomDatabase() {
    abstract fun userCategoryDao(): UserCategoryDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun incomeDao(): IncomeDao
    abstract fun subscriptionDao(): SubscriptionDao
    abstract fun businessPartyDao(): BusinessPartyDao
    abstract fun businessEntryDao(): BusinessEntryDao
    abstract fun accountDao(): AccountDao
    abstract fun budgetGoalDao(): BudgetGoalDao

    companion object {
        @Volatile
        private var Instance: AppDatabase? = null

        /** Adds the account table without wiping the user's existing transactions. */
        private val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `account` (
                        `id` INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                        `name` TEXT NOT NULL,
                        `type` TEXT NOT NULL,
                        `balance` REAL NOT NULL,
                        `accountNumber` TEXT NOT NULL,
                        `creditLimit` REAL,
                        `dueDay` INTEGER,
                        `colorArgb` INTEGER NOT NULL,
                        `createdAt` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        /** Adds the richer income fields captured by the Add Income screen. */
        private val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `income` ADD COLUMN `currency` TEXT NOT NULL DEFAULT 'INR'")
                db.execSQL("ALTER TABLE `income` ADD COLUMN `time` TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE `income` ADD COLUMN `paymentMethod` TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE `income` ADD COLUMN `accountId` INTEGER")
                db.execSQL("ALTER TABLE `income` ADD COLUMN `accountScope` TEXT NOT NULL DEFAULT 'Personal Account'")
                db.execSQL("ALTER TABLE `income` ADD COLUMN `payer` TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE `income` ADD COLUMN `incomeType` TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE `income` ADD COLUMN `tags` TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE `income` ADD COLUMN `receiptPath` TEXT NOT NULL DEFAULT ''")
            }
        }

        /** Adds the budget goal table without wiping the user's existing transactions. */
        private val MIGRATION_10_11 = object : Migration(10, 11) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `budget_goal` (
                        `id` INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                        `categoryName` TEXT NOT NULL,
                        `categoryEmoji` TEXT NOT NULL,
                        `priority` TEXT NOT NULL,
                        `monthlyLimit` REAL NOT NULL,
                        `createdAt` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

        /** Mirrors the v10 income columns onto expenses for the Add Expense screen. */
        private val MIGRATION_11_12 = object : Migration(11, 12) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `expense` ADD COLUMN `currency` TEXT NOT NULL DEFAULT 'INR'")
                db.execSQL("ALTER TABLE `expense` ADD COLUMN `time` TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE `expense` ADD COLUMN `paymentMethod` TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE `expense` ADD COLUMN `accountId` INTEGER")
                db.execSQL("ALTER TABLE `expense` ADD COLUMN `accountScope` TEXT NOT NULL DEFAULT 'Personal Account'")
                db.execSQL("ALTER TABLE `expense` ADD COLUMN `merchant` TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE `expense` ADD COLUMN `tags` TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE `expense` ADD COLUMN `receiptPath` TEXT NOT NULL DEFAULT ''")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return Instance ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                )
                    .addMigrations(
                        MIGRATION_8_9,
                        MIGRATION_9_10,
                        MIGRATION_10_11,
                        MIGRATION_11_12
                    )
                    .fallbackToDestructiveMigration() // Resets database if no migration path exists
                    .build()
                Instance = instance
                instance
            }
        }

        fun getExistingInstance(): AppDatabase? = Instance

        fun clearInstance() {
            Instance = null
        }
    }
}
