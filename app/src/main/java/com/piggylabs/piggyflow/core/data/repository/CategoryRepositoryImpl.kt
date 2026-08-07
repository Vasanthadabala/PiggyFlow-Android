package com.piggylabs.piggyflow.core.data.repository

import com.piggylabs.piggyflow.core.database.dao.UserCategoryDao
import com.piggylabs.piggyflow.core.database.mapper.toDomain
import com.piggylabs.piggyflow.core.database.mapper.toEntity
import com.piggylabs.piggyflow.core.domain.model.Category
import com.piggylabs.piggyflow.core.domain.repository.CategoryRepository
import com.piggylabs.piggyflow.core.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Provider

class CategoryRepositoryImpl @Inject constructor(
    private val daoProvider: Provider<UserCategoryDao>,
    @IoDispatcher private val io: CoroutineDispatcher
) : CategoryRepository {

    /** Resolved per call so a restored database swap is picked up. */
    private val dao get() = daoProvider.get()

    override fun observeCategories(): Flow<List<Category>> =
        dao.getAllCategories().map { list -> list.map { it.toDomain() } }.flowOn(io)

    override suspend fun addCategory(category: Category) = withContext(io) {
        dao.insertUserCategory(category.toEntity())
    }

    override suspend fun deleteCategory(id: Int) = withContext(io) {
        dao.deleteCategoryById(id)
    }
}
