package com.piggylabs.piggyflow.core.di

import com.piggylabs.piggyflow.core.data.repository.AccountRepositoryImpl
import com.piggylabs.piggyflow.core.data.repository.BudgetGoalRepositoryImpl
import com.piggylabs.piggyflow.core.data.repository.BusinessRepositoryImpl
import com.piggylabs.piggyflow.core.data.repository.CategoryRepositoryImpl
import com.piggylabs.piggyflow.core.data.repository.ExpenseRepositoryImpl
import com.piggylabs.piggyflow.core.data.repository.IncomeRepositoryImpl
import com.piggylabs.piggyflow.core.data.repository.SubscriptionRepositoryImpl
import com.piggylabs.piggyflow.core.domain.repository.AccountRepository
import com.piggylabs.piggyflow.core.domain.repository.BudgetGoalRepository
import com.piggylabs.piggyflow.core.domain.repository.BusinessRepository
import com.piggylabs.piggyflow.core.domain.repository.CategoryRepository
import com.piggylabs.piggyflow.core.domain.repository.ExpenseRepository
import com.piggylabs.piggyflow.core.domain.repository.IncomeRepository
import com.piggylabs.piggyflow.core.domain.repository.SubscriptionRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import com.piggylabs.piggyflow.core.data.repository.BackupRepositoryImpl
import com.piggylabs.piggyflow.core.domain.repository.BackupRepository

/** Binds the domain-owned repository contracts to their data-layer implementations. */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindCategoryRepository(impl: CategoryRepositoryImpl): CategoryRepository

    @Binds
    @Singleton
    abstract fun bindExpenseRepository(impl: ExpenseRepositoryImpl): ExpenseRepository

    @Binds
    @Singleton
    abstract fun bindIncomeRepository(impl: IncomeRepositoryImpl): IncomeRepository

    @Binds
    @Singleton
    abstract fun bindSubscriptionRepository(impl: SubscriptionRepositoryImpl): SubscriptionRepository

    @Binds
    @Singleton
    abstract fun bindAccountRepository(impl: AccountRepositoryImpl): AccountRepository

    @Binds
    @Singleton
    abstract fun bindBudgetGoalRepository(impl: BudgetGoalRepositoryImpl): BudgetGoalRepository

    @Binds
    @Singleton
    abstract fun bindBusinessRepository(impl: BusinessRepositoryImpl): BusinessRepository

    @Binds
    @Singleton
    abstract fun bindBackupRepository(impl: BackupRepositoryImpl): BackupRepository
}
