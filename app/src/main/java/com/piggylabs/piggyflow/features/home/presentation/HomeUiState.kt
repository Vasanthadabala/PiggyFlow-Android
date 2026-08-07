package com.piggylabs.piggyflow.features.home.presentation

import com.piggylabs.piggyflow.core.common.UiState
import com.piggylabs.piggyflow.core.domain.model.Category
import com.piggylabs.piggyflow.core.domain.model.Expense
import com.piggylabs.piggyflow.core.domain.model.Income

/** Everything the personal-ledger screens render, as one immutable snapshot. */
data class HomeUiState(
    val categories: List<Category> = emptyList(),
    val expenses: List<Expense> = emptyList(),
    val income: List<Income> = emptyList(),
    override val isLoading: Boolean = true,
    override val errorMessage: String? = null
) : UiState
