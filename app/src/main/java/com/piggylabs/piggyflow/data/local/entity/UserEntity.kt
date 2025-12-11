package com.piggylabs.piggyflow.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_category")
data class UserCategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    var name: String = "",
    var emoji: String = ""
)

@Entity(tableName = "expense")
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val categoryType: String,
    val amount: Double,
    val note: String,
    val date: String,
    val categoryName: String,
    val categoryEmoji: String
)

@Entity(tableName = "income")
data class IncomeEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val categoryType: String,
    val amount: Double,
    val note: String,
    val date: String,
    val categoryName: String,
    val categoryEmoji: String
)