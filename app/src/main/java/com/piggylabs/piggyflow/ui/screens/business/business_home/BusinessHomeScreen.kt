package com.piggylabs.piggyflow.ui.screens.business.business_home

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.piggylabs.piggyflow.navigation.BusinessPartyDetail
import com.piggylabs.piggyflow.navigation.Notification
import com.piggylabs.piggyflow.navigation.Profile
import com.piggylabs.piggyflow.navigation.components.BottomBar
import com.piggylabs.piggyflow.ui.screens.personal.home.getGreetingByTime
import com.piggylabs.piggyflow.ui.screens.common.notification.calculateTrackerReminderCount
import com.piggylabs.piggyflow.ui.screens.common.notification.getClearedTrackerNotificationKeys
import com.piggylabs.piggyflow.ui.screens.business.viewmodel.BusinessLedgerViewModel
import com.piggylabs.piggyflow.ui.screens.business.viewmodel.BusinessPartySummary
import com.piggylabs.piggyflow.ui.screens.common.tracker.viewmodel.TrackerViewModel
import com.piggylabs.piggyflow.ui.theme.appColors
import com.piggylabs.piggyflow.utils.LinkedOwnerParty
import com.piggylabs.piggyflow.utils.PendingCustomerRequest
import com.piggylabs.piggyflow.utils.createOrUpdateCustomerLink
import com.piggylabs.piggyflow.utils.customerCodeFor
import com.piggylabs.piggyflow.utils.linkCustomerWithCode
import com.piggylabs.piggyflow.utils.observeLinkedOwnersForCustomer
import com.piggylabs.piggyflow.utils.observePendingBusinessCustomerRequests
import com.piggylabs.piggyflow.utils.updateCustomerRequestStatus
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

private enum class BusinessFilter(val label: String) {
    ALL("All parties"),
    COLLECT("Need to collect"),
    PAY("Need to pay")
}

private enum class BusinessUserMode(val label: String) {
    OWNER("Owner"),
    CUSTOMER("Customer")
}

@ExperimentalMaterial3Api
@Composable
fun BusinessHomeScreen(navController: NavHostController, viewModel: BusinessLedgerViewModel) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val trackerViewModel: TrackerViewModel = viewModel()
    val sharedPreferences = context.getSharedPreferences("MY_PRE", Context.MODE_PRIVATE)
    val userName = sharedPreferences.getString("userName", "Guest")
    val ownerUid = sharedPreferences.getString("uid", "").orEmpty()
    val greetings by remember { mutableStateOf(getGreetingByTime()) }
    val reminderCount = calculateTrackerReminderCount(
        trackerViewModel.subscriptions,
        getClearedTrackerNotificationKeys(context)
    )
    val partySummaries by viewModel.partySummaries.collectAsState(initial = emptyList())
    var search by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf(BusinessFilter.ALL) }
    var selectedUserMode by remember { mutableStateOf(BusinessUserMode.OWNER) }
    var showAddPartySheet by remember { mutableStateOf(false) }
    var customerCodeInput by remember { mutableStateOf("") }
    var customerConnectStatus by remember { mutableStateOf("") }
    var pendingRequests by remember { mutableStateOf<List<PendingCustomerRequest>>(emptyList()) }
    var linkedOwners by remember { mutableStateOf<List<LinkedOwnerParty>>(emptyList()) }
    var processingRequestIds by remember { mutableStateOf(setOf<String>()) }

    DisposableEffect(ownerUid, selectedUserMode) {
        if (ownerUid.isBlank() || selectedUserMode != BusinessUserMode.CUSTOMER) {
            pendingRequests = emptyList()
            linkedOwners = emptyList()
            onDispose { }
        } else {
            val reqRegistration = observePendingBusinessCustomerRequests(ownerUid) { list ->
                pendingRequests = list
            }
            val linkRegistration = observeLinkedOwnersForCustomer(ownerUid) { list ->
                linkedOwners = list
            }
            onDispose {
                reqRegistration.remove()
                linkRegistration.remove()
            }
        }
    }

    val filteredParties = remember(partySummaries, search, selectedFilter) {
        partySummaries.filter { summary ->
            val matchesSearch = search.isBlank() ||
                summary.party.name.contains(search, ignoreCase = true) ||
                summary.party.phone.contains(search, ignoreCase = true)
            val matchesFilter = when (selectedFilter) {
                BusinessFilter.ALL -> true
                BusinessFilter.COLLECT -> summary.balance > 0
                BusinessFilter.PAY -> summary.balance < 0
            }
            matchesSearch && matchesFilter
        }
    }

    Scaffold(
        bottomBar = { BottomBar(navController = navController) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(appColors().background)
                .padding(innerPadding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(appColors().green)
                                    .clickable {
                                        navController.navigate(Profile.route) {
                                            launchSingleTop = true
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = userName?.trim()?.firstOrNull()?.uppercase() ?: "",
                                    color = Color.White,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.width(4.dp))

                            Column(
                                modifier = Modifier.padding(horizontal = 12.dp),
                                verticalArrangement = Arrangement.spacedBy((-4).dp)
                            ) {
                                Text(
                                    text = greetings,
                                    textAlign = TextAlign.Center,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = appColors().text
                                )
                                Text(
                                    text = "$userName",
                                    textAlign = TextAlign.Center,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.W500,
                                    color = appColors().text
                                )
                            }
                        }

                        Box(
                            modifier = Modifier.size(52.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(appColors().container)
                                    .clickable {
                                        navController.navigate(Notification.route) {
                                            launchSingleTop = true
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsNone,
                                    contentDescription = "",
                                    modifier = Modifier.size(24.dp),
                                    tint = appColors().text
                                )
                            }

                            if (reminderCount > 0) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(top = 2.dp, end = 2.dp)
                                        .size(18.dp)
                                        .clip(CircleShape)
                                        .background(appColors().red),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = reminderCount.coerceAtMost(99).toString(),
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center,
                                        style = TextStyle(
                                            platformStyle = PlatformTextStyle(
                                                includeFontPadding = false
                                            )
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    BusinessSummaryHeader(partySummaries = partySummaries)
                }

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(appColors().container, RoundedCornerShape(16.dp))
                            .padding(6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        BusinessUserMode.entries.forEach { mode ->
                            Button(
                                onClick = { selectedUserMode = mode },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (selectedUserMode == mode) appColors().green else Color.Transparent,
                                    contentColor = if (selectedUserMode == mode) Color.White else appColors().text
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(mode.label)
                            }
                        }
                    }
                }

                if (selectedUserMode == BusinessUserMode.OWNER) {
                    item {
                        OutlinedTextField(
                        value = search,
                        singleLine = true,
                        onValueChange = { search = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        placeholder = {
                            Text(
                                text = "Search category or note",
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
                                    imageVector = Icons.Default.Search,
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

                    item {
                        FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        BusinessFilter.entries.forEach { filter ->
                            AssistChip(
                                onClick = { selectedFilter = filter },
                                label = { Text(filter.label) },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = if (filter == selectedFilter) {
                                        appColors().green.copy(alpha = 0.5f)
                                    } else {
                                        appColors().container
                                    },
                                    labelColor = appColors().text
                                )
                            )
                        }
                    }
                    }

                    item {
                        Text(
                        text = "Customers",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = appColors().text
                    )
                    }

                    if (filteredParties.isEmpty()) {
                        item {
                            EmptyBusinessState(onAddParty = { showAddPartySheet = true })
                        }
                    } else {
                        items(filteredParties, key = { it.party.id }) { summary ->
                            BusinessPartyCard(
                                summary = summary,
                                customerCode = customerCodeFor(ownerUid = ownerUid, partyId = summary.party.id),
                                onClick = {
                                    navController.navigate("${BusinessPartyDetail.route}/${summary.party.id}")
                                }
                            )
                        }
                    }
                } else {
                    item {
                        CustomerModeCard(
                            code = customerCodeInput,
                            onCodeChange = { customerCodeInput = it },
                            statusMessage = customerConnectStatus,
                            onConnect = {
                                if (customerCodeInput.isBlank()) {
                                    customerConnectStatus = "Enter customer ID first"
                                    Toast.makeText(context, customerConnectStatus, Toast.LENGTH_SHORT).show()
                                    return@CustomerModeCard
                                }
                                customerConnectStatus = "Connecting..."
                                linkCustomerWithCode(
                                    codeInput = customerCodeInput,
                                    onSuccess = {
                                        customerConnectStatus = "Linked with $it"
                                        Toast.makeText(context, customerConnectStatus, Toast.LENGTH_SHORT).show()
                                        customerCodeInput = ""
                                    },
                                    onError = { err ->
                                        customerConnectStatus = err
                                        Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        )
                    }

                    item {
                        Text(
                            text = "Linked Owners",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = appColors().text
                        )
                    }

                    if (linkedOwners.isEmpty()) {
                        item {
                            Card(
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = appColors().container),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "No linked owner yet. Connect using Customer ID.",
                                    color = Color.Gray,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }
                    } else {
                        items(linkedOwners, key = { "${it.ownerUid}_${it.partyId}" }) { owner ->
                            val requestsForOwner = pendingRequests.filter {
                                it.ownerUid == owner.ownerUid && it.partyId == owner.partyId
                            }
                            CustomerOwnerCard(
                                owner = owner,
                                pendingRequests = requestsForOwner,
                                processingRequestIds = processingRequestIds,
                                onAccept = { request ->
                                    processingRequestIds = processingRequestIds + request.id
                                    updateCustomerRequestStatus(
                                        customerUid = ownerUid,
                                        requestId = request.id,
                                        status = "accepted",
                                        onSuccess = {
                                            viewModel.saveAcceptedCustomerRequest(
                                                partyName = request.partyName,
                                                partyPhone = request.partyPhone,
                                                type = request.type,
                                                amount = request.amount,
                                                note = request.note
                                            )
                                            Toast.makeText(context, "Accepted and saved", Toast.LENGTH_SHORT).show()
                                            processingRequestIds = processingRequestIds - request.id
                                        },
                                        onError = { err ->
                                            Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                                            processingRequestIds = processingRequestIds - request.id
                                        }
                                    )
                                },
                                onReject = { request ->
                                    processingRequestIds = processingRequestIds + request.id
                                    updateCustomerRequestStatus(
                                        customerUid = ownerUid,
                                        requestId = request.id,
                                        status = "rejected",
                                        onSuccess = {
                                            Toast.makeText(context, "Rejected", Toast.LENGTH_SHORT).show()
                                            processingRequestIds = processingRequestIds - request.id
                                        },
                                        onError = { err ->
                                            Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                                            processingRequestIds = processingRequestIds - request.id
                                        }
                                    )
                                }
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(88.dp))
                }
            }

            if (selectedUserMode == BusinessUserMode.OWNER) {
                FloatingActionButton(
                    onClick = { showAddPartySheet = true },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 20.dp, bottom = 20.dp),
                    containerColor = appColors().green,
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add customer",
                        tint = Color.White
                    )
                }
            }
        }
    }

    if (showAddPartySheet) {
        AddPartySheet(
            onDismiss = { showAddPartySheet = false },
            onSubmit = { name, phone, address ->
                viewModel.addParty(name, phone, address) { partyId ->
                    createOrUpdateCustomerLink(
                        ownerUid = ownerUid,
                        partyId = partyId,
                        partyName = name,
                        partyPhone = phone
                    )
                }
                showAddPartySheet = false
            }
        )
    }
}

@Composable
private fun BusinessSummaryHeader(partySummaries: List<BusinessPartySummary>) {
    val collectAmount = partySummaries.filter { it.balance > 0 }.sumOf { it.balance }
    val payAmount = partySummaries.filter { it.balance < 0 }.sumOf { abs(it.balance) }

    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFF0B6E4F), Color(0xFF1B4332))
                    ),
                    shape = RoundedCornerShape(28.dp)
                )
                .padding(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    text = "Business Flow",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Track dues, customer balances, and daily entries in one place.",
                    color = Color.White.copy(alpha = 0.9f),
                    style = MaterialTheme.typography.bodyMedium
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(modifier = Modifier.weight(1f)) {
                        BusinessMetricCard("To collect", collectAmount, Color(0xFFD8F3DC))
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        BusinessMetricCard("To pay", payAmount, Color(0xFFFFE5D9))
                    }
                }
                Text(
                    text = "${partySummaries.size} active parties",
                    color = Color.White.copy(alpha = 0.88f),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun BusinessMetricCard(title: String, amount: Double, accent: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.12f))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(text = title, color = Color.White.copy(alpha = 0.82f), fontSize = 13.sp)
            Text(
                text = formatCurrency(amount),
                color = accent,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }
    }
}

@Composable
private fun BusinessPartyCard(
    summary: BusinessPartySummary,
    customerCode: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = appColors().container)
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .background(appColors().green.copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = summary.party.name.take(2).uppercase(),
                        color = appColors().green,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = summary.party.name,
                        color = appColors().text,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = summary.party.phone, color = Color.Gray, fontSize = 13.sp)
                    }
                    Text(
                        text = "Customer ID: $customerCode",
                        color = appColors().green,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Icon(
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = null,
                    tint = Color.Gray
                )
            }

            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.35f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = when {
                            summary.balance > 0 -> "Customer has to pay"
                            summary.balance < 0 -> "You have to pay"
                            else -> "Settled"
                        },
                        color = Color.Gray,
                        fontSize = 13.sp
                    )
                    Text(
                        text = formatSignedBalance(summary.balance),
                        color = when {
                            summary.balance > 0 -> Color(0xFF2D6A4F)
                            summary.balance < 0 -> Color(0xFFBC4749)
                            else -> appColors().text
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }
                Text(
                    text = summary.lastEntryAt?.let(::formatDateTime) ?: "No entries yet",
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun EmptyBusinessState(onAddParty: () -> Unit) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = appColors().container),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "No customers yet",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = appColors().text
            )
            Text(
                text = "Add your first customer and start recording you gave / you got entries.",
                color = Color.Gray
            )
            Button(
                onClick = onAddParty,
                colors = ButtonDefaults.buttonColors(
                    containerColor = appColors().green,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "Add customer",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun CustomerModeCard(
    code: String,
    onCodeChange: (String) -> Unit,
    statusMessage: String,
    onConnect: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = appColors().container),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Connect with Owner",
                color = appColors().text,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
            OutlinedTextField(
                value = code,
                onValueChange = { onCodeChange(it.uppercase()) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Enter Customer ID (PF-0001)") },
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedContainerColor = appColors().background,
                    unfocusedContainerColor = appColors().background,
                    cursorColor = appColors().text
                )
            )
            Button(
                onClick = onConnect,
                colors = ButtonDefaults.buttonColors(
                    containerColor = appColors().green,
                    contentColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Connect")
            }
            if (statusMessage.isNotBlank()) {
                Text(
                    text = statusMessage,
                    color = if (statusMessage.startsWith("Linked")) appColors().green else Color.Gray,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun CustomerOwnerCard(
    owner: LinkedOwnerParty,
    pendingRequests: List<PendingCustomerRequest>,
    processingRequestIds: Set<String>,
    onAccept: (PendingCustomerRequest) -> Unit,
    onReject: (PendingCustomerRequest) -> Unit
) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = appColors().container),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .background(appColors().green.copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = owner.partyName.take(2).uppercase(),
                        color = appColors().green,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = owner.partyName,
                        color = appColors().text,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(text = owner.partyPhone, color = Color.Gray, fontSize = 13.sp)
                    Text(
                        text = "Customer ID: ${owner.customerCode}",
                        color = appColors().green,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.35f))

            if (pendingRequests.isEmpty()) {
                Text(
                    text = "No pending requests",
                    color = Color.Gray
                )
            } else {
                pendingRequests.forEach { request ->
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = appColors().background)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "${if (request.type == "gave") "Owner gave" else "Owner got"} ₹${"%.2f".format(request.amount)}",
                                color = Color.Gray
                            )
                            Text(
                                text = "Status: Waiting for accept",
                                color = appColors().red,
                                fontSize = 12.sp
                            )
                            if (request.note.isNotBlank()) {
                                Text(text = request.note, color = Color.Gray, fontSize = 12.sp)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                val isProcessing = request.id in processingRequestIds
                                Button(
                                    onClick = { onAccept(request) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = appColors().green,
                                        contentColor = Color.White
                                    ),
                                    modifier = Modifier.weight(1f),
                                    enabled = !isProcessing
                                ) {
                                    Text(if (isProcessing) "Please wait..." else "Accept")
                                }
                                Button(
                                    onClick = { onReject(request) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = appColors().red,
                                        contentColor = Color.White
                                    ),
                                    modifier = Modifier.weight(1f),
                                    enabled = !isProcessing
                                ) {
                                    Text("Reject")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@ExperimentalMaterial3Api
@Composable
private fun AddPartySheet(
    onDismiss: () -> Unit,
    onSubmit: (String, String, String) -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }

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
            Text("Add customer", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = appColors().text)

            Column {

                Text(
                    text = "Name",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Normal,
                    color = appColors().text
                )

                Spacer(modifier = Modifier.height(5.dp))

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = name,
                        singleLine = true,
                        onValueChange = { name = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        placeholder = {
                            Text(
                                text = "Customer name",
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
                                    imageVector = Icons.Default.Person,
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
                    text = "Number",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Normal,
                    color = appColors().text
                )

                Spacer(modifier = Modifier.height(5.dp))

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = phone,
                        singleLine = true,
                        onValueChange = { phone = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        placeholder = {
                            Text(
                                text = "Phone number",
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
                                    imageVector = Icons.Default.Phone,
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
                    text = "Address",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Normal,
                    color = appColors().text
                )

                Spacer(modifier = Modifier.height(5.dp))

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = address,
                        singleLine = true,
                        onValueChange = { address = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        placeholder = {
                            Text(
                                text = "Address (optional)",
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
                                    imageVector = Icons.Default.Home,
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
                    if (name.isNotBlank() && phone.isNotBlank()) {
                        onSubmit(name, phone, address)
                    }
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
                    text = "Save customer",
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

internal fun formatCurrency(amount: Double): String {
    return "₹${DecimalFormat("#,##0.##").format(amount)}"
}

private fun formatSignedBalance(amount: Double): String {
    return when {
        amount > 0 -> "+ ${formatCurrency(amount)}"
        amount < 0 -> "- ${formatCurrency(abs(amount))}"
        else -> formatCurrency(0.0)
    }
}

internal fun formatDateTime(timestamp: Long): String {
    return SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(timestamp))
}
