package com.piggylabs.piggyflow.ui.screens.business.business_party_details

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.piggylabs.piggyflow.data.local.entity.BusinessEntryEntity
import com.piggylabs.piggyflow.navigation.components.TopBar
import com.piggylabs.piggyflow.ui.screens.business.business_home.formatCurrency
import com.piggylabs.piggyflow.ui.screens.business.business_home.formatDateTime
import com.piggylabs.piggyflow.ui.screens.business.viewmodel.BusinessLedgerViewModel
import com.piggylabs.piggyflow.ui.theme.appColors
import com.piggylabs.piggyflow.utils.customerCodeFor
import com.piggylabs.piggyflow.utils.sendRequestToLinkedCustomer
import com.piggylabs.piggyflow.utils.generateBusinessLedgerPdf
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusinessPartyDetailScreen(
    navController: NavHostController,
    viewModel: BusinessLedgerViewModel,
    partyId: Int
) {

    Scaffold(
        topBar = { TopBar(name = "Back", navController = navController) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(appColors().background)
                .padding(innerPadding)
        ) {
            BusinessPartyDetailScreenComponent(navController, viewModel, partyId)
        }
    }
}

@Composable
fun BusinessPartyDetailScreenComponent(
    navController: NavHostController,
    viewModel: BusinessLedgerViewModel,
    partyId: Int
){
    val context = LocalContext.current
    val ownerUid = context.getSharedPreferences("MY_PRE", Context.MODE_PRIVATE)
        .getString("uid", "")
        .orEmpty()
    var waitingCount by remember { mutableStateOf(0) }
    var waitingStatusText by remember { mutableStateOf("Checking link status...") }
    var waitingListener by remember { mutableStateOf<ListenerRegistration?>(null) }

    LaunchedEffect(ownerUid, partyId) {
        waitingListener?.remove()
        waitingListener = null
        waitingCount = 0

        if (ownerUid.isBlank()) {
            waitingStatusText = "Owner not signed in"
            return@LaunchedEffect
        }

        val code = customerCodeFor(ownerUid = ownerUid, partyId = partyId)
        FirebaseFirestore.getInstance()
            .collection("customer_links")
            .document(code)
            .get()
            .addOnSuccessListener { linkDoc ->
                val customerUid = linkDoc.getString("customerUid").orEmpty()
                if (customerUid.isBlank()) {
                    waitingStatusText = "Customer not connected yet"
                    waitingCount = 0
                    return@addOnSuccessListener
                }

                waitingStatusText = "Connected"
                waitingListener = FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(customerUid)
                    .collection("modes")
                    .document("business")
                    .collection("customerRequests")
                    .whereEqualTo("ownerUid", ownerUid)
                    .whereEqualTo("partyId", partyId)
                    .whereEqualTo("status", "pending")
                    .addSnapshotListener { snap, _ ->
                        waitingCount = snap?.size() ?: 0
                    }
            }
            .addOnFailureListener {
                waitingStatusText = "Failed to check customer link"
            }
    }

    DisposableEffect(Unit) {
        onDispose {
            waitingListener?.remove()
            waitingListener = null
        }
    }
    val party by viewModel.observeParty(partyId).collectAsState(initial = null)
    val entries by viewModel.observeEntries(partyId).collectAsState(initial = emptyList())
    var showSheet by remember { mutableStateOf(false) }
    var sheetType by remember { mutableStateOf("gave") }

    val balance = entries.sumOf { if (it.type == "gave") it.amount else -it.amount }

    fun openDialer(phone: String) {
        runCatching {
            context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone")))
        }.onFailure {
            Toast.makeText(context, "Unable to open dialer", Toast.LENGTH_SHORT).show()
        }
    }

    fun shareReminder(name: String, phone: String, balance: Double) {
        val statusLine = when {
            balance > 0 -> "Please pay ${formatCurrency(balance)}."
            balance < 0 -> "I will pay ${formatCurrency(abs(balance))}."
            else -> "Your account is fully settled."
        }
        val message = "Hi $name, this is your PiggyFlow Flow reminder. $statusLine"
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "$message\nPhone: $phone")
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share reminder"))
    }

    fun exportLedger(name: String, phone: String, balance: Double, entries: List<BusinessEntryEntity>) {
        val result = generateBusinessLedgerPdf(
            context = context,
            partyName = name,
            phone = phone,
            balance = balance,
            entries = entries
        )
        Toast.makeText(context, result, Toast.LENGTH_LONG).show()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        if (party == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Customer not found", color = appColors().text)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = appColors().container),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    party!!.name,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = appColors().text
                                )
                                Text(party!!.phone, color = Color.Gray)
                            }
                            IconButton(
                                onClick = {
                                    viewModel.deleteParty(partyId)
                                    navController.popBackStack()
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "Delete customer",
                                    tint = appColors().red.copy(alpha = 0.8f)
                                )
                            }
                        }

                        Text(
                            text = when {
                                balance > 0 -> "You will collect ${formatCurrency(balance)}"
                                balance < 0 -> "You need to pay ${formatCurrency(abs(balance))}"
                                else -> "All settled"
                            },
                            color = when {
                                balance > 0 -> appColors().green.copy(alpha = 0.6f)
                                balance < 0 -> appColors().red.copy(alpha = 0.6f)
                                else -> appColors().text
                            },
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 18.sp
                        )

                        Text(
                            text = if (waitingCount > 0) {
                                "Waiting for customer accept: $waitingCount"
                            } else {
                                waitingStatusText
                            },
                            color = if (waitingCount > 0) Color(0xFFE67E22) else Color.Gray,
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp
                        )

                        if (party!!.address.isNotBlank()) {
                            Text(party!!.address, color = Color.Gray)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { openDialer(party!!.phone) },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = appColors().green,
                                    contentColor = Color.White
                                )
                            ) {
                                Icon(Icons.Default.Call, contentDescription = null)
                                Spacer(modifier = Modifier.padding(2.dp))
                                Text(
                                    text = "Call",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    textAlign = TextAlign.Center
                                )
                            }
                            Button(
                                onClick = { shareReminder(party!!.name, party!!.phone, balance) },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF457B9D),
                                    contentColor = Color.White
                                )
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null)
                                Spacer(modifier = Modifier.padding(2.dp))
                                Text(
                                    text = "Remind",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { exportLedger(party!!.name, party!!.phone, balance, entries) },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF6D597A),
                                    contentColor = Color.White
                                )
                            ) {
                                Icon(Icons.Default.PictureAsPdf, contentDescription = null)
                                Spacer(modifier = Modifier.padding(2.dp))
                                Text(
                                    text = "Export",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    textAlign = TextAlign.Center
                                )
                            }
                            Button(
                                onClick = {
                                    viewModel.settleBalance(partyId, balance)
                                    Toast.makeText(context, "Settlement recorded", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.weight(1f),
                                enabled = balance != 0.0,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF2A9D8F),
                                    contentColor = Color.White
                                )
                            ) {
                                Text(
                                    text = "Settle Up",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    "Ledger entries",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = appColors().text
                )
                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (entries.isEmpty()) {
                        item {
                            Card(
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(containerColor = appColors().container),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "No entries yet. Use the buttons below to record the first transaction.",
                                    modifier = Modifier.padding(18.dp),
                                    color = Color.Gray
                                )
                            }
                        }
                    } else {
                        items(entries, key = { it.id }) { entry ->
                            BusinessEntryRow(
                                entry = entry,
                                onDelete = { viewModel.deleteEntry(entry.id) }
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            sheetType = "gave"
                            showSheet = true
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = appColors().red,
                            contentColor = Color.White
                        )
                    ) {
                        Text(
                            text = "You Gave",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Button(
                        onClick = {
                            sheetType = "got"
                            showSheet = true
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = appColors().green,
                            contentColor = Color.White
                        )
                    ) {
                        Text(
                            text = "You Got",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }

    if (showSheet && party != null) {
        AddBusinessEntrySheet(
            type = sheetType,
            onDismiss = { showSheet = false },
            onSubmit = { amount, note ->
                viewModel.addEntry(
                    partyId = partyId,
                    type = sheetType,
                    amount = amount,
                    note = note
                ) { entryId ->
                    sendRequestToLinkedCustomer(
                        ownerUid = ownerUid,
                        partyId = partyId,
                        sourceEntryId = entryId,
                        partyName = party!!.name,
                        partyPhone = party!!.phone,
                        type = sheetType,
                        amount = amount,
                        note = note
                    )
                }
                showSheet = false
            }
        )
    }
}

@Composable
private fun BusinessEntryRow(
    entry: BusinessEntryEntity,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = appColors().container),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (entry.type == "gave") "You gave" else "You got",
                    color = appColors().text,
                    fontWeight = FontWeight.SemiBold
                )

                if (entry.note.isNotBlank()) {
                    Text(entry.note, color = Color.Gray, fontSize = 13.sp)
                }

                Text(formatDateTime(entry.createdAt), color = Color.Gray, fontSize = 12.sp)
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = formatCurrency(entry.amount),
                    color = if (entry.type == "gave") appColors().red.copy(alpha = 0.5f) else appColors().green.copy(alpha = 0.5f),
                    fontWeight = FontWeight.Medium,
                    fontSize = 18.sp
                )
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete entry",
                        tint = appColors().red.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddBusinessEntrySheet(
    type: String,
    onDismiss: () -> Unit,
    onSubmit: (Double, String) -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = appColors().background
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = if (type == "gave") "Record you gave" else "Record you got",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = appColors().text
            )

            Column {

                Text(
                    text = "Amount",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Normal,
                    color = appColors().text
                )

                Spacer(modifier = Modifier.height(5.dp))

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = amount,
                        singleLine = true,
                        onValueChange = { amount = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        placeholder = {
                            Text(
                                text = "Amount",
                                style = TextStyle(
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.W500,
                                    color = Color.Gray,
                                    textAlign = TextAlign.Start
                                )
                            )
                        },
                        shape = RoundedCornerShape(20),
                        colors = TextFieldDefaults.colors(
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedContainerColor = appColors().container,
                            unfocusedContainerColor = appColors().container,
                            cursorColor = appColors().text
                        ),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done // Use Done for the last field
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                keyboardController?.hide()
                            }
                        ),
                        leadingIcon = {
                            Box(
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CurrencyRupee,
                                    contentDescription = "",
                                    modifier = Modifier.size(20.dp),
                                    tint = Color.Gray
                                )
                            }
                        },
                        textStyle = TextStyle(
                            fontWeight = FontWeight.W500,
                            fontSize = 16.sp,
                            color = appColors().text
                        )
                    )
                }
            }

            Column {

                Text(
                    text = "Note",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Normal,
                    color = appColors().text
                )

                Spacer(modifier = Modifier.height(5.dp))

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = note,
                        singleLine = true,
                        onValueChange = { note = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        placeholder = {
                            Text(
                                text = "Note (optional)",
                                style = TextStyle(
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.W500,
                                    color = Color.Gray,
                                    textAlign = TextAlign.Start
                                )
                            )
                        },
                        shape = RoundedCornerShape(20),
                        colors = TextFieldDefaults.colors(
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedContainerColor = appColors().container,
                            unfocusedContainerColor = appColors().container,
                            cursorColor = appColors().text
                        ),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done // Use Done for the last field
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                keyboardController?.hide()
                            }
                        ),
                        leadingIcon = {
                            Box(
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Description,
                                    contentDescription = "",
                                    modifier = Modifier.size(20.dp),
                                    tint = Color.Gray
                                )
                            }
                        },
                        textStyle = TextStyle(
                            fontWeight = FontWeight.W500,
                            fontSize = 16.sp,
                            color = appColors().text
                        )
                    )
                }
            }

            Button(
                onClick = {
                    amount.toDoubleOrNull()?.takeIf { it > 0 }?.let { onSubmit(it, note) }
                },
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 0.dp,
                    pressedElevation = 2.dp,
                ),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = appColors().green,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "Save entry",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
