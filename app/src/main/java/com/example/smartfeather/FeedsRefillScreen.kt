package com.example.smartfeather

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val FarmPoppins = FontFamily(
    Font(R.font.poppins_regular, FontWeight.Normal),
    Font(R.font.poppins_medium, FontWeight.Medium),
    Font(R.font.poppins_semibold, FontWeight.SemiBold),
    Font(R.font.poppins_bold, FontWeight.Bold)
)

@Composable
fun FeedsRefillScreen(
    employeeId: Int,
    onBackToFarm: () -> Unit,
    onNavigateToDashboard: () -> Unit,
    onNavigateToTasks: () -> Unit,
    onGoToBiosecurity: () -> Unit
) {
    val feedsService = remember { FeedsRefillBackendService() }
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    val openedAt = remember { LocalDateTime.now() }
    val openedDate = remember(openedAt) {
        openedAt.format(DateTimeFormatter.ofPattern("M-d-yy", Locale.getDefault()))
    }
    val openedTime = remember(openedAt) {
        openedAt.format(DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault()))
    }
    val recordedAtValue = remember(openedAt) {
        openedAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"))
    }

    var house by remember { mutableStateOf("") }
    var pen by remember { mutableStateOf("") }
    var feedType by remember { mutableStateOf("") }
    var feederNumber by remember { mutableStateOf("") }
    var kilograms by remember { mutableStateOf("") }

    var selectedHouse by remember { mutableStateOf<FeedHouseOption?>(null) }
    var pens by remember { mutableStateOf<List<FeedPenOption>>(emptyList()) }
    var feedOptions by remember { mutableStateOf<List<FeedInventoryOption>>(emptyList()) }

    var selectedPen by remember { mutableStateOf<FeedPenOption?>(null) }
    var selectedFeed by remember { mutableStateOf<FeedInventoryOption?>(null) }

    var penExpanded by remember { mutableStateOf(false) }
    var feedExpanded by remember { mutableStateOf(false) }
    var feederExpanded by remember { mutableStateOf(false) }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    var showBlockedDialog by remember { mutableStateOf(false) }
    var blockedMessage by remember { mutableStateOf("") }

    val feederOptions = remember { feedsService.feederOptions() }

    LaunchedEffect(employeeId) {
        feedsService.getFeedsContext(employeeId)
            .onSuccess { context ->
                if (!context.accessAllowed) {
                    blockedMessage = context.message
                        ?: "Please complete personnel biosecurity before accessing feeds refill."
                    showBlockedDialog = true
                    house = ""
                    selectedHouse = null
                    pens = emptyList()
                } else {
                    selectedHouse = context.house
                    house = context.house?.houseNumber.orEmpty()
                    pens = context.pens
                }
            }
            .onFailure {
                blockedMessage = it.message ?: "Failed to load feeds refill context."
                showBlockedDialog = true
                house = ""
                selectedHouse = null
                pens = emptyList()
            }

        feedsService.getFeedInventoryOptions()
            .onSuccess { loadedFeeds ->
                feedOptions = loadedFeeds
            }
            .onFailure {
                errorMessage = it.message ?: "Failed to load feed inventory."
            }
    }

    if (showBlockedDialog) {
        AlertDialog(
            onDismissRequest = {},
            title = {
                Text(
                    text = "Biosecurity Required",
                    fontFamily = FarmPoppins,
                    fontWeight = FontWeight.SemiBold
                )
            },
            text = {
                Text(
                    text = blockedMessage,
                    fontFamily = FarmPoppins
                )
            },
            dismissButton = {
                TextButton(onClick = onBackToFarm) {
                    Text("Back", fontFamily = FarmPoppins)
                }
            },
            confirmButton = {
                TextButton(onClick = onGoToBiosecurity) {
                    Text("Go to Biosecurity", fontFamily = FarmPoppins)
                }
            }
        )
    }

    Scaffold(
        bottomBar = {
            FeedsBottomNavigationBar(
                onDashboardClick = onNavigateToDashboard,
                onTasksClick = onNavigateToTasks,
                onFarmClick = onBackToFarm
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF2F2F2))
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .pointerInput(Unit) {
                    detectTapGestures {
                        focusManager.clearFocus()
                    }
                }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFB37A3C))
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 12.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onBackToFarm() }
                )
                Text(
                    text = "Feeds Refill",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontFamily = FarmPoppins,
                    fontWeight = FontWeight.Bold
                )
            }

            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Spacer(modifier = Modifier.height(10.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        FeedsLabel("Date")
                        FeedsReadOnlyField(openedDate)
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        FeedsLabel("Time")
                        FeedsReadOnlyField(openedTime)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        FeedsLabel("House")
                        FeedsReadOnlyField(house)
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        FeedsLabel("Pen")
                        FeedsDropdownField(
                            value = pen,
                            options = pens.map { it.penName },
                            expanded = penExpanded,
                            onExpandedChange = {
                                if (selectedHouse != null) penExpanded = it
                            },
                            onValueSelected = { selectedValue ->
                                pen = selectedValue
                                selectedPen = pens.firstOrNull { it.penName == selectedValue }
                                penExpanded = false
                                errorMessage = null
                                successMessage = null
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                FeedsLabel("Type of Feed")
                FeedsDropdownField(
                    value = feedType,
                    options = feedOptions.map { it.itemName },
                    expanded = feedExpanded,
                    onExpandedChange = { feedExpanded = it },
                    onValueSelected = { selectedValue ->
                        feedType = selectedValue
                        selectedFeed = feedOptions.firstOrNull { it.itemName == selectedValue }
                        feedExpanded = false
                        errorMessage = null
                        successMessage = null
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                FeedsLabel("Feeder Number")
                FeedsDropdownField(
                    value = feederNumber,
                    options = feederOptions,
                    expanded = feederExpanded,
                    onExpandedChange = { feederExpanded = it },
                    onValueSelected = { selectedValue ->
                        feederNumber = selectedValue
                        feederExpanded = false
                        errorMessage = null
                        successMessage = null
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                FeedsLabel("Kilograms of Feeds Refilled")
                FeedsInputField(
                    value = kilograms,
                    keyboardType = KeyboardType.Number
                ) { newValue ->
                    kilograms = newValue.filter { ch -> ch.isDigit() }
                }

                Spacer(modifier = Modifier.height(12.dp))

                errorMessage?.let {
                    Text(
                        text = it,
                        color = Color(0xFFB00020),
                        fontFamily = FarmPoppins,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                successMessage?.let {
                    Text(
                        text = it,
                        color = Color(0xFF2E7D32),
                        fontFamily = FarmPoppins,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            errorMessage = null
                            successMessage = null

                            val currentHouse = selectedHouse
                            val currentPen = selectedPen
                            val currentFeed = selectedFeed
                            val feederValue = feederNumber.toIntOrNull()
                            val kilogramsValue = kilograms.toIntOrNull()

                            if (currentHouse == null) {
                                errorMessage = "No house is assigned from your biosecurity entry."
                                return@Button
                            }
                            if (currentPen == null) {
                                errorMessage = "Please select a pen."
                                return@Button
                            }
                            if (currentFeed == null) {
                                errorMessage = "Please select a type of feed."
                                return@Button
                            }
                            if (feederValue == null) {
                                errorMessage = "Please select a feeder number."
                                return@Button
                            }
                            if (kilogramsValue == null || kilogramsValue <= 0) {
                                errorMessage = "Please enter a valid kilograms value."
                                return@Button
                            }

                            coroutineScope.launch {
                                isLoading = true
                                feedsService.submitFeedRefill(
                                    employeeId = employeeId,
                                    inventoryId = currentFeed.id,
                                    houseId = currentHouse.id,
                                    penId = currentPen.id,
                                    feederNumber = feederValue,
                                    kilograms = kilogramsValue,
                                    recordedAt = recordedAtValue
                                ).onSuccess { success ->
                                    if (success) {
                                        successMessage = "Feeds refill submitted successfully."
                                        pen = ""
                                        feedType = ""
                                        feederNumber = ""
                                        kilograms = ""
                                        selectedPen = null
                                        selectedFeed = null

                                        feedsService.getFeedInventoryOptions()
                                            .onSuccess { refreshedFeeds ->
                                                feedOptions = refreshedFeeds
                                            }
                                    } else {
                                        errorMessage = "Failed to submit feeds refill."
                                    }
                                }.onFailure {
                                    errorMessage = it.message ?: "Failed to submit feeds refill."
                                }
                                isLoading = false
                            }
                        },
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                        modifier = Modifier.height(40.dp),
                        enabled = !isLoading && !showBlockedDialog
                    ) {
                        Text(
                            text = if (isLoading) "Submitting..." else "Submit",
                            color = Color.White,
                            fontFamily = FarmPoppins
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FeedsLabel(text: String) {
    Text(
        text = text,
        fontSize = 12.sp,
        fontFamily = FarmPoppins,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.padding(bottom = 4.dp)
    )
}

@Composable
fun FeedsInputField(
    value: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    onValueChange: (String) -> Unit
) {
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val coroutineScope = rememberCoroutineScope()

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .bringIntoViewRequester(bringIntoViewRequester)
            .onFocusEvent { focusState ->
                if (focusState.isFocused) {
                    coroutineScope.launch {
                        delay(250)
                        bringIntoViewRequester.bringIntoView()
                    }
                }
            },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape = RoundedCornerShape(20.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color(0xFFEDEDED),
            unfocusedContainerColor = Color(0xFFEDEDED),
            focusedBorderColor = Color(0xFFBDBDBD),
            unfocusedBorderColor = Color(0xFFBDBDBD),
            cursorColor = Color.Black
        )
    )
}

@Composable
fun FeedsReadOnlyField(value: String) {
    OutlinedTextField(
        value = value,
        onValueChange = {},
        readOnly = true,
        enabled = false,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        shape = RoundedCornerShape(20.dp),
        colors = OutlinedTextFieldDefaults.colors(
            disabledContainerColor = Color(0xFFE3E3E3),
            disabledBorderColor = Color(0xFFBDBDBD),
            disabledTextColor = Color(0xFF6E6E6E)
        )
    )
}

@Composable
fun FeedsDropdownField(
    value: String,
    options: List<String>,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onValueSelected: (String) -> Unit
) {
    Box {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(Color(0xFFEDEDED), RoundedCornerShape(20.dp))
                .border(1.dp, Color(0xFFBDBDBD), RoundedCornerShape(20.dp))
                .clickable { onExpandedChange(true) }
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (value.isBlank()) "Select" else value,
                    fontFamily = FarmPoppins,
                    fontSize = 14.sp,
                    color = Color.Black
                )
                Text(
                    text = "⌄",
                    fontFamily = FarmPoppins,
                    fontSize = 18.sp,
                    color = Color.Black
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option, fontFamily = FarmPoppins) },
                    onClick = { onValueSelected(option) }
                )
            }
        }
    }
}

@Composable
fun FeedsBottomNavigationBar(
    onDashboardClick: () -> Unit,
    onTasksClick: () -> Unit,
    onFarmClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF06331D), Color(0xFF022816))
                )
            )
            .navigationBarsPadding()
            .padding(horizontal = 10.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        FeedsBottomNavItem(Icons.Outlined.Home, "Dashboard", false, onDashboardClick)
        FeedsBottomNavItem(Icons.AutoMirrored.Outlined.List, "Tasks", false, onTasksClick)
        FeedsBottomNavItem(Icons.Outlined.Edit, "Farm Management", true, onFarmClick)
        FeedsBottomNavItem(Icons.Outlined.AccountCircle, "Profile", false, {})
    }
}

@Composable
fun FeedsBottomNavItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.clickable(
            interactionSource = interactionSource,
            indication = null
        ) { onClick() }
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (selected) Color.White else Color(0xFFD7ECD9),
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontFamily = FarmPoppins,
            fontWeight = FontWeight.Normal,
            fontSize = 10.sp,
            color = if (selected) Color.White else Color(0xFFD7ECD9),
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}