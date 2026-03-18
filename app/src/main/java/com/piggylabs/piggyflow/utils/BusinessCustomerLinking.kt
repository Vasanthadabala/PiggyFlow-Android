package com.piggylabs.piggyflow.utils

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions

data class PendingCustomerRequest(
    val id: String,
    val ownerUid: String,
    val partyId: Int,
    val partyName: String,
    val partyPhone: String,
    val type: String,
    val amount: Double,
    val note: String,
    val createdAt: Long
)

data class LinkedOwnerParty(
    val ownerUid: String,
    val partyId: Int,
    val partyName: String,
    val partyPhone: String,
    val customerCode: String
)

private fun readableFirestoreError(error: Exception?, fallback: String): String {
    return if (error is FirebaseFirestoreException && error.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
        "Permission denied. Please update Firestore rules for customer linking."
    } else {
        error?.message ?: fallback
    }
}

fun customerCodeFor(ownerUid: String, partyId: Int): String {
    return "PF-${partyId.toString().padStart(4, '0')}"
}

fun createOrUpdateCustomerLink(
    ownerUid: String,
    partyId: Int,
    partyName: String,
    partyPhone: String
) {
    if (ownerUid.isBlank() || partyId == 0) return
    val firestore = FirebaseFirestore.getInstance()
    val code = customerCodeFor(ownerUid, partyId)
    firestore.collection("customer_links")
        .document(code)
        .set(
            mapOf(
                "customerCode" to code,
                "ownerUid" to ownerUid,
                "partyId" to partyId,
                "partyName" to partyName,
                "partyPhone" to partyPhone,
                "updatedAt" to System.currentTimeMillis()
            ),
            SetOptions.merge()
        )
}

fun linkCustomerWithCode(
    codeInput: String,
    onSuccess: (partyName: String) -> Unit,
    onError: (String) -> Unit
) {
    val user = FirebaseAuth.getInstance().currentUser ?: run {
        onError("Please sign in first")
        return
    }
    val code = codeInput.trim().uppercase()
    if (code.isBlank()) {
        onError("Enter customer ID")
        return
    }

    val firestore = FirebaseFirestore.getInstance()
    val linkRef = firestore.collection("customer_links").document(code)
    linkRef.get()
        .addOnSuccessListener { linkDoc ->
            if (!linkDoc.exists()) {
                onError("Customer ID not found")
                return@addOnSuccessListener
            }

            val partyName = linkDoc.getString("partyName").orEmpty()
            linkRef.set(
                mapOf(
                    "customerUid" to user.uid,
                    "linkedAt" to System.currentTimeMillis(),
                    "updatedAt" to System.currentTimeMillis()
                ),
                SetOptions.merge()
            ).addOnSuccessListener {
                enqueueExistingOwnerEntriesForCustomer(
                    customerUid = user.uid,
                    ownerUid = linkDoc.getString("ownerUid").orEmpty(),
                    partyId = linkDoc.getLong("partyId")?.toInt() ?: 0,
                    partyName = partyName.ifBlank { "Customer" },
                    partyPhone = linkDoc.getString("partyPhone").orEmpty(),
                    onDone = {
                        onSuccess(partyName.ifBlank { "Customer" })
                    },
                    onError = { backfillError ->
                        onError(backfillError)
                    }
                )
            }.addOnFailureListener { e ->
                onError(readableFirestoreError(e, "Failed to link customer"))
            }
        }
        .addOnFailureListener { e ->
            onError(readableFirestoreError(e, "Failed to check customer ID"))
        }
}

private fun enqueueExistingOwnerEntriesForCustomer(
    customerUid: String,
    ownerUid: String,
    partyId: Int,
    partyName: String,
    partyPhone: String,
    onDone: () -> Unit,
    onError: (String) -> Unit
) {
    if (ownerUid.isBlank() || partyId == 0) {
        onError("Invalid owner link")
        return
    }

    val firestore = FirebaseFirestore.getInstance()
    val requestsRef = firestore.collection("users")
        .document(customerUid)
        .collection("modes")
        .document("business")
        .collection("customerRequests")

    val ownerEntriesRef = firestore.collection("users")
        .document(ownerUid)
        .collection("modes")
        .document("business")
        .collection("businessEntries")

    requestsRef
        .whereEqualTo("ownerUid", ownerUid)
        .whereEqualTo("partyId", partyId)
        .get()
        .addOnSuccessListener { existingReqSnap ->
            val existingSourceEntryIds = existingReqSnap.documents
                .mapNotNull { it.getLong("sourceEntryId")?.toInt() }
                .toSet()

            ownerEntriesRef
                .whereEqualTo("partyId", partyId)
                .get()
                .addOnSuccessListener { ownerEntrySnap ->
                    val batch = firestore.batch()
                    ownerEntrySnap.documents.forEach { entryDoc ->
                        val sourceEntryId = entryDoc.getLong("id")?.toInt() ?: return@forEach
                        if (sourceEntryId in existingSourceEntryIds) return@forEach

                        val requestDoc = requestsRef.document("${ownerUid}_${partyId}_$sourceEntryId")
                        val type = entryDoc.getString("type").orEmpty()
                        val amount = entryDoc.getDouble("amount")
                            ?: entryDoc.getLong("amount")?.toDouble()
                            ?: 0.0
                        val note = entryDoc.getString("note").orEmpty()
                        val createdAt = entryDoc.getLong("createdAt") ?: System.currentTimeMillis()

                        batch.set(
                            requestDoc,
                            mapOf(
                                "ownerUid" to ownerUid,
                                "partyId" to partyId,
                                "partyName" to partyName,
                                "partyPhone" to partyPhone,
                                "type" to type,
                                "amount" to amount,
                                "note" to note,
                                "sourceEntryId" to sourceEntryId,
                                "status" to "pending",
                                "createdAt" to createdAt,
                                "syncedAt" to System.currentTimeMillis()
                            ),
                            SetOptions.merge()
                        )
                    }

                    batch.commit()
                        .addOnSuccessListener { onDone() }
                        .addOnFailureListener { e ->
                            onError(readableFirestoreError(e, "Failed to fetch owner data"))
                        }
                }
                .addOnFailureListener { e ->
                    onError(readableFirestoreError(e, "Failed to fetch owner data"))
                }
        }
        .addOnFailureListener { e ->
            onError(readableFirestoreError(e, "Failed to load customer requests"))
        }
}

fun sendRequestToLinkedCustomer(
    ownerUid: String,
    partyId: Int,
    sourceEntryId: Int,
    partyName: String,
    partyPhone: String,
    type: String,
    amount: Double,
    note: String
) {
    if (ownerUid.isBlank() || sourceEntryId == 0) return
    val firestore = FirebaseFirestore.getInstance()
    val code = customerCodeFor(ownerUid, partyId)
    firestore.collection("customer_links")
        .document(code)
        .get()
        .addOnSuccessListener { linkDoc ->
            val customerUid = linkDoc.getString("customerUid").orEmpty()
            if (customerUid.isBlank()) return@addOnSuccessListener

            val requestRef = firestore.collection("users")
                .document(customerUid)
                .collection("modes")
                .document("business")
                .collection("customerRequests")
                .document("${ownerUid}_${partyId}_$sourceEntryId")

            requestRef.set(
                mapOf(
                    "ownerUid" to ownerUid,
                    "partyId" to partyId,
                    "sourceEntryId" to sourceEntryId,
                    "partyName" to partyName,
                    "partyPhone" to partyPhone,
                    "type" to type,
                    "amount" to amount,
                    "note" to note,
                    "status" to "pending",
                    "createdAt" to System.currentTimeMillis()
                ),
                SetOptions.merge()
            )
        }
}

fun observePendingBusinessCustomerRequests(
    customerUid: String,
    onUpdate: (List<PendingCustomerRequest>) -> Unit
): ListenerRegistration {
    return FirebaseFirestore.getInstance()
        .collection("users")
        .document(customerUid)
        .collection("modes")
        .document("business")
        .collection("customerRequests")
        .whereEqualTo("status", "pending")
        .addSnapshotListener { snapshot, _ ->
            if (snapshot == null) {
                onUpdate(emptyList())
                return@addSnapshotListener
            }
            val list = snapshot.documents.map { doc ->
                PendingCustomerRequest(
                    id = doc.id,
                    ownerUid = doc.getString("ownerUid").orEmpty(),
                    partyId = doc.getLong("partyId")?.toInt() ?: 0,
                    partyName = doc.getString("partyName").orEmpty(),
                    partyPhone = doc.getString("partyPhone").orEmpty(),
                    type = doc.getString("type").orEmpty(),
                    amount = doc.getDouble("amount")
                        ?: doc.getLong("amount")?.toDouble()
                        ?: 0.0,
                    note = doc.getString("note").orEmpty(),
                    createdAt = doc.getLong("createdAt") ?: 0L
                )
            }.sortedByDescending { it.createdAt }
            onUpdate(list)
        }
}

fun observeLinkedOwnersForCustomer(
    customerUid: String,
    onUpdate: (List<LinkedOwnerParty>) -> Unit
): ListenerRegistration {
    return FirebaseFirestore.getInstance()
        .collection("customer_links")
        .whereEqualTo("customerUid", customerUid)
        .addSnapshotListener { snapshot, _ ->
            if (snapshot == null) {
                onUpdate(emptyList())
                return@addSnapshotListener
            }

            val list = snapshot.documents.mapNotNull { doc ->
                val ownerUid = doc.getString("ownerUid").orEmpty()
                val partyId = doc.getLong("partyId")?.toInt() ?: 0
                if (ownerUid.isBlank() || partyId == 0) return@mapNotNull null
                LinkedOwnerParty(
                    ownerUid = ownerUid,
                    partyId = partyId,
                    partyName = doc.getString("partyName").orEmpty(),
                    partyPhone = doc.getString("partyPhone").orEmpty(),
                    customerCode = doc.getString("customerCode").orEmpty()
                )
            }.sortedBy { it.partyName }

            onUpdate(list)
        }
}

fun updateCustomerRequestStatus(
    customerUid: String,
    requestId: String,
    status: String,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    FirebaseFirestore.getInstance()
        .collection("users")
        .document(customerUid)
        .collection("modes")
        .document("business")
        .collection("customerRequests")
        .document(requestId)
        .set(
            mapOf(
                "status" to status,
                "actedAt" to System.currentTimeMillis()
            ),
            SetOptions.merge()
        )
        .addOnSuccessListener { onSuccess() }
        .addOnFailureListener { e -> onError(readableFirestoreError(e, "Failed to update request")) }
}
