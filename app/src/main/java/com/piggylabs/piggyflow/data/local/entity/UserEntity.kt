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

@Entity(tableName = "subscription")
data class SubscriptionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: String,
    val name: String,
    val subType: String,
    val amount: Double,
    val dueDate: String,
    val logoUrl: String = ""
)

@Entity(tableName = "business_party")
data class BusinessPartyEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val phone: String,
    val address: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "business_entry")
data class BusinessEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val partyId: Int,
    val type: String,
    val amount: Double,
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
