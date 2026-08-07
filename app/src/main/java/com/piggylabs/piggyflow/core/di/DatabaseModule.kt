package com.piggylabs.piggyflow.core.di

import android.content.Context
import com.piggylabs.piggyflow.core.database.dao.AccountDao
import com.piggylabs.piggyflow.core.database.dao.BudgetGoalDao
import com.piggylabs.piggyflow.core.database.dao.BusinessEntryDao
import com.piggylabs.piggyflow.core.database.dao.BusinessPartyDao
import com.piggylabs.piggyflow.core.database.dao.ExpenseDao
import com.piggylabs.piggyflow.core.database.dao.IncomeDao
import com.piggylabs.piggyflow.core.database.dao.SubscriptionDao
import com.piggylabs.piggyflow.core.database.dao.UserCategoryDao
import com.piggylabs.piggyflow.core.database.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

/**
 * Database bindings.
 *
 * Nothing here is `@Singleton`. Restoring a Drive backup closes the Room instance and
 * builds a new one ([com.piggylabs.piggyflow.core.database.closeDatabase] /
 * `reopenDatabase`), so a scoped binding would pin repositories to a closed database.
 * Providing unscoped and injecting `Provider<Dao>` in the repositories means every call
 * resolves against whichever instance is live right now.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        AppDatabase.getDatabase(context)

    @Provides
    fun provideUserCategoryDao(db: AppDatabase): UserCategoryDao = db.userCategoryDao()

    @Provides
    fun provideExpenseDao(db: AppDatabase): ExpenseDao = db.expenseDao()

    @Provides
    fun provideIncomeDao(db: AppDatabase): IncomeDao = db.incomeDao()

    @Provides
    fun provideSubscriptionDao(db: AppDatabase): SubscriptionDao = db.subscriptionDao()

    @Provides
    fun provideAccountDao(db: AppDatabase): AccountDao = db.accountDao()

    @Provides
    fun provideBudgetGoalDao(db: AppDatabase): BudgetGoalDao = db.budgetGoalDao()

    @Provides
    fun provideBusinessPartyDao(db: AppDatabase): BusinessPartyDao = db.businessPartyDao()

    @Provides
    fun provideBusinessEntryDao(db: AppDatabase): BusinessEntryDao = db.businessEntryDao()
}
