package com.piggylabs.piggyflow.features.settings.presentation

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.SetOptions
import com.piggylabs.piggyflow.core.database.AppEvents
import com.piggylabs.piggyflow.core.database.closeDatabase
import com.piggylabs.piggyflow.core.database.reopenDatabase
import com.piggylabs.piggyflow.core.domain.model.BusinessEntry
import com.piggylabs.piggyflow.core.domain.model.BusinessParty
import com.piggylabs.piggyflow.core.domain.model.Expense
import com.piggylabs.piggyflow.core.domain.model.Income
import com.piggylabs.piggyflow.core.domain.model.Subscription
import com.piggylabs.piggyflow.core.domain.model.Category
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.atomic.AtomicInteger
import com.piggylabs.piggyflow.core.di.backupRepository
import com.piggylabs.piggyflow.core.domain.repository.BackupSnapshot
import com.piggylabs.piggyflow.core.di.preferencesManager

/**
 * Sync/backup/account-lifecycle logic shared by the business [SettingScreen] and the
 * personal [com.piggylabs.piggyflow.features.profile.presentation.ProfileScreen]. Kept in
 * one place so both surfaces stay behaviourally identical.
 */
internal const val SETTINGS_TAG = "SettingsFlow"

internal fun clearLocalAppData(context: Context) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            closeDatabase(context)
            context.deleteDatabase("app_database")
            reopenDatabase(context)
            AppEvents.tryEmitDbRecreated()
        } catch (e: Exception) {
            Log.e(SETTINGS_TAG, "Failed to clear local data", e)
        }
    }
}

internal fun deleteAccount(
    context: Context,
    onProgress: (Boolean) -> Unit,
    onComplete: (message: String, deleted: Boolean) -> Unit
) {
    val user = FirebaseAuth.getInstance().currentUser
    if (user == null) {
        onComplete("No signed-in account found", false)
        return
    }

    val mainHandler = Handler(Looper.getMainLooper())
    mainHandler.post { onProgress(true) }

    val firestore = FirebaseFirestore.getInstance()
    val userDoc = firestore.collection("users").document(user.uid)
    val accountType = preferencesManager(context).snapshotBlocking().accountType.lowercase()
    val modeDoc = userDoc.collection("modes").document(accountType)

    fun finish(message: String, deleted: Boolean) {
        mainHandler.post {
            onProgress(false)
            if (deleted) {
                clearLocalAppData(context)
            }
            onComplete(message, deleted)
        }
    }

    fun deleteAuthUser(successMessage: String) {
        user.delete()
            .addOnSuccessListener {
                FirebaseAuth.getInstance().signOut()
                finish(successMessage, true)
            }
            .addOnFailureListener { e ->
                Log.e(SETTINGS_TAG, "Firebase auth delete failed", e)
                val message = if ((e.message ?: "").contains("recent", ignoreCase = true)) {
                    "For security, please log in again and then delete your account."
                } else {
                    e.message ?: "Account deletion failed"
                }
                finish(message, false)
            }
    }

    userDoc.get()
        .addOnSuccessListener { doc ->
            val allTypes = (doc.get("accountTypes") as? List<*>)
                ?.mapNotNull { it as? String }
                ?.map { it.lowercase() }
                ?.toMutableSet()
                ?: mutableSetOf()
            doc.getString("accountType")?.lowercase()?.let(allTypes::add)

            val remainingTypes = allTypes.filter { it != accountType }

            deleteModeBackupData(
                modeDoc = modeDoc,
                onSuccess = {
                    if (remainingTypes.isEmpty()) {
                        userDoc.delete()
                            .addOnSuccessListener {
                                deleteAuthUser("Account deleted successfully")
                            }
                            .addOnFailureListener { e ->
                                Log.e(SETTINGS_TAG, "Failed to delete main user doc", e)
                                finish(e.message ?: "Failed to delete account data", false)
                            }
                    } else {
                        val updates = mapOf(
                            "accountTypes" to remainingTypes,
                            "accountType" to remainingTypes.first(),
                            "userNames.$accountType" to FieldValue.delete(),
                            "updatedAt" to System.currentTimeMillis()
                        )

                        userDoc.set(updates, SetOptions.merge())
                            .addOnSuccessListener {
                                FirebaseAuth.getInstance().signOut()
                                finish(
                                    "${accountType.replaceFirstChar { it.uppercase() }} account deleted. Other account type is still available.",
                                    true
                                )
                            }
                            .addOnFailureListener { e ->
                                Log.e(SETTINGS_TAG, "Failed updating remaining account types", e)
                                finish(e.message ?: "Failed to update account data", false)
                            }
                    }
                },
                onFailure = { error ->
                    finish(error, false)
                }
            )
        }
        .addOnFailureListener { e ->
            Log.e(SETTINGS_TAG, "Failed reading user profile before delete", e)
            finish(e.message ?: "Failed to delete account", false)
        }
}

internal fun deleteModeBackupData(
    modeDoc: DocumentReference,
    onSuccess: () -> Unit,
    onFailure: (String) -> Unit
) {
    val firestore = FirebaseFirestore.getInstance()
    val collections = listOf("categories", "expenses", "incomes", "subscriptions", "businessParties", "businessEntries", "sync")
    val pending = AtomicInteger(collections.size)
    var failed = false

    fun markDone() {
        if (pending.decrementAndGet() == 0 && !failed) {
            modeDoc.delete()
                .addOnSuccessListener { onSuccess() }
                .addOnFailureListener { e ->
                    Log.e(SETTINGS_TAG, "Failed to delete mode doc", e)
                    onFailure(e.message ?: "Failed to delete account data")
                }
        }
    }

    collections.forEach { collectionName ->
        modeDoc.collection(collectionName).get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.isEmpty) {
                    markDone()
                    return@addOnSuccessListener
                }

                val batch = firestore.batch()
                snapshot.documents.forEach { doc ->
                    batch.delete(doc.reference)
                }
                batch.commit()
                    .addOnSuccessListener { markDone() }
                    .addOnFailureListener { e ->
                        failed = true
                        Log.e(SETTINGS_TAG, "Failed deleting subcollection=$collectionName", e)
                        onFailure(e.message ?: "Failed to delete account data")
                    }
            }
            .addOnFailureListener { e ->
                failed = true
                Log.e(SETTINGS_TAG, "Failed reading subcollection=$collectionName", e)
                onFailure(e.message ?: "Failed to delete account data")
            }
    }
}

internal fun syncLocalDataToFirebase(
    context: Context,
    uid: String,
    setSyncing: (Boolean) -> Unit,
    onMessage: (String) -> Unit
) {
    Log.d(SETTINGS_TAG, "syncLocalDataToFirebase started for uid=$uid")
    val mainHandler = Handler(Looper.getMainLooper())
    mainHandler.post { setSyncing(true) }
    val firestore = FirebaseFirestore.getInstance()
    val userDoc = firestore.collection("users").document(uid)
    val accountType = preferencesManager(context).snapshotBlocking().accountType.lowercase()
    val modeDoc = userDoc.collection("modes").document(accountType)

    CoroutineScope(Dispatchers.IO).launch {
        try {
            val backupRepo = backupRepository(context)
            val snapshot = backupRepo.snapshot()
            val categories = snapshot.categories
            val expenses = snapshot.expenses
            val incomes = snapshot.incomes
            val subscriptions = snapshot.subscriptions
            val businessParties = snapshot.businessParties
            val businessEntries = snapshot.businessEntries
            Log.d(SETTINGS_TAG, "Local data prepared for sync uid=$uid categories=${categories.size} expenses=${expenses.size} incomes=${incomes.size} subscriptions=${subscriptions.size} businessParties=${businessParties.size} businessEntries=${businessEntries.size}")

            val batch = firestore.batch()

            val syncMeta = mapOf(
                "updatedAt" to System.currentTimeMillis(),
                "categoryCount" to categories.size,
                "expenseCount" to expenses.size,
                "incomeCount" to incomes.size,
                "subscriptionCount" to subscriptions.size,
                "businessPartyCount" to businessParties.size,
                "businessEntryCount" to businessEntries.size
            )

            batch.set(
                modeDoc.collection("sync").document("meta"),
                syncMeta,
                SetOptions.merge()
            )

            categories.forEach { category ->
                val categoryData = mapOf(
                    "id" to category.id,
                    "name" to category.name,
                    "emoji" to category.emoji,
                    "updatedAt" to System.currentTimeMillis()
                )
                batch.set(
                    modeDoc.collection("categories").document(category.id.toString()),
                    categoryData,
                    SetOptions.merge()
                )
            }

            expenses.forEach { expense ->
                val expenseData = mapOf(
                    "id" to expense.id,
                    "categoryType" to expense.categoryType,
                    "amount" to expense.amount,
                    "note" to expense.note,
                    "date" to expense.date,
                    "categoryName" to expense.categoryName,
                    "categoryEmoji" to expense.categoryEmoji,
                    "updatedAt" to System.currentTimeMillis()
                )
                batch.set(
                    modeDoc.collection("expenses").document(expense.id.toString()),
                    expenseData,
                    SetOptions.merge()
                )
            }

            incomes.forEach { income ->
                val incomeData = mapOf(
                    "id" to income.id,
                    "categoryType" to income.categoryType,
                    "amount" to income.amount,
                    "note" to income.note,
                    "date" to income.date,
                    "categoryName" to income.categoryName,
                    "categoryEmoji" to income.categoryEmoji,
                    "updatedAt" to System.currentTimeMillis()
                )
                batch.set(
                    modeDoc.collection("incomes").document(income.id.toString()),
                    incomeData,
                    SetOptions.merge()
                )
            }

            subscriptions.forEach { subscription ->
                val subscriptionData = mapOf(
                    "id" to subscription.id,
                    "type" to subscription.type,
                    "name" to subscription.name,
                    "subType" to subscription.subType,
                    "amount" to subscription.amount,
                    "dueDate" to subscription.dueDate,
                    "logoUrl" to subscription.logoUrl,
                    "updatedAt" to System.currentTimeMillis()
                )
                batch.set(
                    modeDoc.collection("subscriptions").document(subscription.id.toString()),
                    subscriptionData,
                    SetOptions.merge()
                )
            }

            businessParties.forEach { party ->
                val partyData = mapOf(
                    "id" to party.id,
                    "name" to party.name,
                    "phone" to party.phone,
                    "address" to party.address,
                    "createdAt" to party.createdAt,
                    "updatedAt" to party.updatedAt
                )
                batch.set(
                    modeDoc.collection("businessParties").document(party.id.toString()),
                    partyData,
                    SetOptions.merge()
                )
            }

            businessEntries.forEach { entry ->
                val entryData = mapOf(
                    "id" to entry.id,
                    "partyId" to entry.partyId,
                    "type" to entry.type,
                    "amount" to entry.amount,
                    "note" to entry.note,
                    "createdAt" to entry.createdAt
                )
                batch.set(
                    modeDoc.collection("businessEntries").document(entry.id.toString()),
                    entryData,
                    SetOptions.merge()
                )
            }

            batch.commit()
                .addOnSuccessListener {
                    preferencesManager(context).apply {
                        setLastSyncedAtAsync(System.currentTimeMillis())
                        setActiveDataAsync(uid, accountType)
                    }
                    mainHandler.post {
                        setSyncing(false)
                        onMessage("Sync completed successfully")
                    }
                }
                .addOnFailureListener { e ->
                    val errorMessage = if (
                        e is FirebaseFirestoreException &&
                        e.code == FirebaseFirestoreException.Code.PERMISSION_DENIED
                    ) {
                        "Sync blocked by Firestore rules. Allow users/{uid}/... for signed-in user."
                    } else {
                        e.message ?: "Sync failed"
                    }
                    mainHandler.post {
                        setSyncing(false)
                        onMessage(errorMessage)
                    }
                }
        } catch (e: Exception) {
            mainHandler.post {
                setSyncing(false)
                onMessage(e.message ?: "Sync failed")
            }
        }
    }
}

internal fun restoreOrSyncAfterLogin(
    context: Context,
    uid: String,
    setSyncing: (Boolean) -> Unit,
    onMessage: (String) -> Unit
) {
    Log.d(SETTINGS_TAG, "restoreOrSyncAfterLogin started for uid=$uid")
    val mainHandler = Handler(Looper.getMainLooper())
    mainHandler.post { setSyncing(true) }

    val firestore = FirebaseFirestore.getInstance()
    val userDoc = firestore.collection("users").document(uid)
    val accountType = preferencesManager(context).snapshotBlocking().accountType.lowercase()
    val modeDoc = userDoc.collection("modes").document(accountType)

    prepareLocalDataForAccountSwitch(
        context = context,
        uid = uid,
        accountType = accountType,
        onSuccess = {
            modeDoc.collection("categories").get()
        .addOnSuccessListener { categorySnap ->
            modeDoc.collection("expenses").get()
                .addOnSuccessListener { expenseSnap ->
                    modeDoc.collection("incomes").get()
                        .addOnSuccessListener { incomeSnap ->
                            modeDoc.collection("subscriptions").get()
                                .addOnSuccessListener { subscriptionSnap ->
                                    modeDoc.collection("businessParties").get()
                                        .addOnSuccessListener { businessPartySnap ->
                                            modeDoc.collection("businessEntries").get()
                                                .addOnSuccessListener { businessEntrySnap ->
                            val hasBackupData =
                                categorySnap.documents.isNotEmpty() ||
                                    expenseSnap.documents.isNotEmpty() ||
                                    incomeSnap.documents.isNotEmpty() ||
                                    subscriptionSnap.documents.isNotEmpty() ||
                                    businessPartySnap.documents.isNotEmpty() ||
                                    businessEntrySnap.documents.isNotEmpty()

                            if (!hasBackupData) {
                                mainHandler.post { setSyncing(false) }
                                syncLocalDataToFirebase(context, uid, setSyncing, onMessage)
                                return@addOnSuccessListener
                            }

                            CoroutineScope(Dispatchers.IO).launch {
                                try {

                                    val categories = categorySnap.documents.map { doc ->
                                        Category(
                                            id = doc.getLong("id")?.toInt() ?: doc.id.toIntOrNull() ?: 0,
                                            name = doc.getString("name").orEmpty(),
                                            emoji = doc.getString("emoji").orEmpty()
                                        )
                                    }

                                    val expenses = expenseSnap.documents.map { doc ->
                                        Expense(
                                            id = doc.getLong("id")?.toInt() ?: doc.id.toIntOrNull() ?: 0,
                                            categoryType = doc.getString("categoryType").orEmpty(),
                                            amount = (doc.getDouble("amount")
                                                ?: doc.getLong("amount")?.toDouble()
                                                ?: 0.0),
                                            note = doc.getString("note").orEmpty(),
                                            date = doc.getString("date").orEmpty(),
                                            categoryName = doc.getString("categoryName").orEmpty(),
                                            categoryEmoji = doc.getString("categoryEmoji").orEmpty()
                                        )
                                    }

                                    val incomes = incomeSnap.documents.map { doc ->
                                        Income(
                                            id = doc.getLong("id")?.toInt() ?: doc.id.toIntOrNull() ?: 0,
                                            categoryType = doc.getString("categoryType").orEmpty(),
                                            amount = (doc.getDouble("amount")
                                                ?: doc.getLong("amount")?.toDouble()
                                                ?: 0.0),
                                            note = doc.getString("note").orEmpty(),
                                            date = doc.getString("date").orEmpty(),
                                            categoryName = doc.getString("categoryName").orEmpty(),
                                            categoryEmoji = doc.getString("categoryEmoji").orEmpty()
                                        )
                                    }

                                    val subscriptions = subscriptionSnap.documents.map { doc ->
                                        Subscription(
                                            id = doc.getLong("id")?.toInt() ?: doc.id.toIntOrNull() ?: 0,
                                            type = doc.getString("type").orEmpty(),
                                            name = doc.getString("name").orEmpty(),
                                            subType = doc.getString("subType").orEmpty(),
                                            amount = (doc.getDouble("amount") ?: doc.getLong("amount")?.toDouble() ?: 0.0),
                                            dueDate = doc.getString("dueDate").orEmpty(),
                                            logoUrl = doc.getString("logoUrl").orEmpty()
                                        )
                                    }

                                    val businessParties = businessPartySnap.documents.map { doc ->
                                        BusinessParty(
                                            id = doc.getLong("id")?.toInt() ?: doc.id.toIntOrNull() ?: 0,
                                            name = doc.getString("name").orEmpty(),
                                            phone = doc.getString("phone").orEmpty(),
                                            address = doc.getString("address").orEmpty(),
                                            createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis(),
                                            updatedAt = doc.getLong("updatedAt") ?: System.currentTimeMillis()
                                        )
                                    }

                                    val businessEntries = businessEntrySnap.documents.map { doc ->
                                        BusinessEntry(
                                            id = doc.getLong("id")?.toInt() ?: doc.id.toIntOrNull() ?: 0,
                                            partyId = doc.getLong("partyId")?.toInt() ?: 0,
                                            type = doc.getString("type").orEmpty(),
                                            amount = (doc.getDouble("amount") ?: doc.getLong("amount")?.toDouble() ?: 0.0),
                                            note = doc.getString("note").orEmpty(),
                                            createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                                        )
                                    }

                                    val backupRepo = backupRepository(context)
                                    val snapshot = backupRepo.snapshot()
                                    val localCategories = snapshot.categories
                                    val localExpenses = snapshot.expenses
                                    val localIncomes = snapshot.incomes
                                    val localSubscriptions = snapshot.subscriptions
                                    val localBusinessParties = snapshot.businessParties
                                    val localBusinessEntries = snapshot.businessEntries

                                    val localCategoryIds = localCategories.map { it.id }.toSet()
                                    val localExpenseIds = localExpenses.map { it.id }.toSet()
                                    val localIncomeIds = localIncomes.map { it.id }.toSet()
                                    val localSubscriptionIds = localSubscriptions.map { it.id }.toSet()
                                    val localBusinessPartyIds = localBusinessParties.map { it.id }.toSet()
                                    val localBusinessEntryIds = localBusinessEntries.map { it.id }.toSet()

                                    val categoriesToInsert = categories.filter { it.id !in localCategoryIds }
                                    val expensesToInsert = expenses.filter { it.id !in localExpenseIds }
                                    val incomesToInsert = incomes.filter { it.id !in localIncomeIds }
                                    val subscriptionsToInsert = subscriptions.filter { it.id !in localSubscriptionIds }
                                    val businessPartiesToInsert = businessParties.filter { it.id !in localBusinessPartyIds }
                                    val businessEntriesToInsert = businessEntries.filter { it.id !in localBusinessEntryIds }

                                    backupRepo.insertAll(
                                        BackupSnapshot(
                                            categories = categoriesToInsert,
                                            expenses = expensesToInsert,
                                            incomes = incomesToInsert,
                                            subscriptions = subscriptionsToInsert,
                                            businessParties = businessPartiesToInsert,
                                            businessEntries = businessEntriesToInsert
                                        )
                                    )
                                    preferencesManager(context).apply {
                                        setLastSyncedAtAsync(System.currentTimeMillis())
                                        setActiveDataAsync(uid, accountType)
                                    }

                                    mainHandler.post {
                                        setSyncing(false)
                                        onMessage(
                                            "Backup merged: +${categoriesToInsert.size} categories, +${expensesToInsert.size} expenses, +${incomesToInsert.size} incomes, +${subscriptionsToInsert.size} tracker items, +${businessPartiesToInsert.size} parties, +${businessEntriesToInsert.size} ledger entries"
                                        )
                                    }
                                } catch (e: Exception) {
                                    mainHandler.post {
                                        setSyncing(false)
                                        onMessage(e.message ?: "Restore failed")
                                    }
                                }
                            }
                                                }
                                                .addOnFailureListener { e ->
                                                    mainHandler.post {
                                                        setSyncing(false)
                                                        onMessage(e.message ?: "Restore failed")
                                                    }
                                                }
                                        }
                                        .addOnFailureListener { e ->
                                            mainHandler.post {
                                                setSyncing(false)
                                                onMessage(e.message ?: "Restore failed")
                                            }
                                        }
                                }
                                .addOnFailureListener { e ->
                                    mainHandler.post {
                                        setSyncing(false)
                                        onMessage(e.message ?: "Restore failed")
                                    }
                                }
                        }
                        .addOnFailureListener { e ->
                            mainHandler.post {
                                setSyncing(false)
                                onMessage(e.message ?: "Restore failed")
                            }
                        }
                }
                .addOnFailureListener { e ->
                    mainHandler.post {
                        setSyncing(false)
                        onMessage(e.message ?: "Restore failed")
                    }
                }
        }
        .addOnFailureListener { e ->
            val errorMessage = if (
                e is FirebaseFirestoreException &&
                e.code == FirebaseFirestoreException.Code.PERMISSION_DENIED
            ) {
                "Restore blocked by Firestore rules. Allow users/{uid}/... for signed-in user."
            } else {
                e.message ?: "Restore failed"
            }
            mainHandler.post {
                setSyncing(false)
                onMessage(errorMessage)
            }
        }},
        onError = { error ->
            mainHandler.post {
                setSyncing(false)
                onMessage(error)
            }
        }
    )
}

internal fun prepareLocalDataForAccountSwitch(
    context: Context,
    uid: String,
    accountType: String,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    val prefs = preferencesManager(context).snapshotBlocking()
    val previousUid = prefs.activeDataUid
    val previousType = prefs.activeDataAccountType
    val shouldResetLocal = previousUid != null &&
        (previousUid != uid || !previousType.equals(accountType, ignoreCase = true))

    if (!shouldResetLocal) {
        onSuccess()
        return
    }

    CoroutineScope(Dispatchers.IO).launch {
        try {
            backupRepository(context).clearAll()
            onSuccess()
        } catch (e: Exception) {
            Log.e(SETTINGS_TAG, "Failed clearing local data on account switch", e)
            onError(e.message ?: "Failed to prepare local data for account switch")
        }
    }
}

internal fun formatLastSynced(timestamp: Long): String {
    if (timestamp <= 0L) return "Last synced: Not synced yet"
    val formatter = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    return "Last synced: ${formatter.format(Date(timestamp))}"
}

internal fun saveSignedInUidForAccountType(
    context: Context,
    accountType: String,
    uid: String
) {
    preferencesManager(context).setSignedInUidForAccountTypeAsync(accountType, uid)
}

internal fun isSignedInForAccountType(
    context: Context,
    accountType: String,
    firebaseUid: String?
): Boolean = preferencesManager(context).isSignedInForAccountType(accountType, firebaseUid)
