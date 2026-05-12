package com.example.smartfeather

import androidx.compose.foundation.ScrollState
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
import androidx.compose.foundation.layout.imePadding
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

private val NewBatchPoppins = FontFamily(
    Font(R.font.poppins_regular, FontWeight.Normal),
    Font(R.font.poppins_medium, FontWeight.Medium),
    Font(R.font.poppins_semibold, FontWeight.SemiBold),
    Font(R.font.poppins_bold, FontWeight.Bold)
)

@Composable
fun NewBirdBatchScreen(
    onBackToFarm: () -> Unit,
    onNavigateToDashboard: () -> Unit,
    onNavigateToTasks: () -> Unit
) {
    val batchService = remember { NewBatchBackendService() }
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    val openedAt = remember { LocalDateTime.now() }
    val displayDate = remember(openedAt) {
        openedAt.format(DateTimeFormatter.ofPattern("M-d-yy", Locale.getDefault()))
    }
    val displayTime = remember(openedAt) {
        openedAt.format(DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault()))
    }
    val submittedDate = remember(openedAt) {
        openedAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
    }
    val submittedTime = remember(openedAt) {
        openedAt.format(DateTimeFormatter.ofPattern("HH:mm"))
    }

    var batchCode by remember { mutableStateOf("") }
    var initialPopulation by remember { mutableStateOf("") }
    var house by remember { mutableStateOf("") }
    var pen by remember { mutableStateOf("") }

    var houses by remember { mutableStateOf<List<NewBatchHouseOption>>(emptyList()) }
    var pens by remember { mutableStateOf<List<NewBatchPenOption>>(emptyList()) }

    var selectedHouse by remember { mutableStateOf<NewBatchHouseOption?>(null) }
    var selectedPen by remember { mutableStateOf<NewBatchPenOption?>(null) }

    var houseExpanded by remember { mutableStateOf(false) }
    var penExpanded by remember { mutableStateOf(false) }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    var showConflictDialog by remember { mutableStateOf(false) }
    var conflictMessage by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        batchService.getHouses()
            .onSuccess { houses = it }
            .onFailure { errorMessage = it.message ?: "Failed to load houses." }
    }

    if (showConflictDialog) {
        AlertDialog(
            onDismissRequest = { showConflictDialog = false },
            title = {
                Text(
                    text = "Running Batch Found",
                    fontFamily = NewBatchPoppins,
                    fontWeight = FontWeight.SemiBold
                )
            },
            text = {
                Text(
                    text = conflictMessage,
                    fontFamily = NewBatchPoppins
                )
            },
            confirmButton = {
                TextButton(onClick = { showConflictDialog = false }) {
                    Text("OK", fontFamily = NewBatchPoppins)
                }
            }
        )
    }

    Scaffold(
        bottomBar = {
            NewBatchBottomNavBar(
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
                .verticalScroll(scrollState)
                .imePadding()
                .pointerInput(Unit) {
                    detectTapGestures {
                        focusManager.clearFocus()
                    }
                }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
            ) {
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color(0xFFD32F2F))
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Back",
                        tint = Color(0xFFD32F2F),
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(start = 12.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { onBackToFarm() }
                    )
                    Text(
                        text = "Add New Batch",
                        color = Color(0xFFD32F2F),
                        fontSize = 20.sp,
                        fontFamily = NewBatchPoppins,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color(0xFFD32F2F))
                )
            }

            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                NewBatchLabel("Batch Code")
                NewBatchInputField(
                    value = batchCode,
                    scrollState = scrollState
                ) { batchCode = it }

                Spacer(modifier = Modifier.height(10.dp))

                NewBatchLabel("Initial Population")
                NewBatchInputField(
                    value = initialPopulation,
                    scrollState = scrollState,
                    keyboardType = KeyboardType.Number
                ) { newValue ->
                    initialPopulation = newValue.filter { it.isDigit() }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        NewBatchLabel("Date")
                        NewBatchReadOnlyField(displayDate)
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        NewBatchLabel("Time")
                        NewBatchReadOnlyField(displayTime)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        NewBatchLabel("House")
                        NewBatchDropdownField(
                            value = house,
                            options = houses.map { it.houseNumber },
                            expanded = houseExpanded,
                            onExpandedChange = { houseExpanded = it },
                            onValueSelected = { selectedValue ->
                                house = selectedValue
                                selectedHouse = houses.firstOrNull { it.houseNumber == selectedValue }
                                pen = ""
                                selectedPen = null
                                pens = emptyList()
                                houseExpanded = false
                                errorMessage = null
                                successMessage = null

                                val houseId = selectedHouse?.id ?: return@NewBatchDropdownField
                                coroutineScope.launch {
                                    batchService.getPensByHouse(houseId)
                                        .onSuccess { pens = it }
                                        .onFailure {
                                            errorMessage = it.message ?: "Failed to load pens."
                                        }
                                }
                            }
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        NewBatchLabel("Pen")
                        NewBatchDropdownField(
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

                Spacer(modifier = Modifier.height(12.dp))

                errorMessage?.let {
                    Text(
                        text = it,
                        color = Color(0xFFB00020),
                        fontFamily = NewBatchPoppins,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                successMessage?.let {
                    Text(
                        text = it,
                        color = Color(0xFF2E7D32),
                        fontFamily = NewBatchPoppins,
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
                            val initialPopulationValue = initialPopulation.toIntOrNull()

                            if (batchCode.isBlank()) {
                                errorMessage = "Please enter a batch code."
                                return@Button
                            }
                            if (initialPopulationValue == null || initialPopulationValue <= 0) {
                                errorMessage = "Please enter a valid initial population."
                                return@Button
                            }
                            if (currentHouse == null) {
                                errorMessage = "Please select a house."
                                return@Button
                            }
                            if (currentPen == null) {
                                errorMessage = "Please select a pen."
                                return@Button
                            }

                            coroutineScope.launch {
                                isLoading = true
                                batchService.submitNewBatch(
                                    batchCode = batchCode.trim(),
                                    houseId = currentHouse.id,
                                    penId = currentPen.id,
                                    initialPopulation = initialPopulationValue,
                                    date = submittedDate,
                                    time = submittedTime
                                ).onSuccess { result ->
                                    if (result.isConflict) {
                                        conflictMessage = buildString {
                                            append(result.message)
                                            if (!result.existingBatchCode.isNullOrBlank()) {
                                                append("\n\nCurrent Batch: ${result.existingBatchCode}")
                                            }
                                            if (!result.existingStartedAt.isNullOrBlank()) {
                                                append("\nStarted At: ${result.existingStartedAt}")
                                            }
                                        }
                                        showConflictDialog = true
                                    } else if (result.success) {
                                        successMessage = result.message
                                        batchCode = ""
                                        initialPopulation = ""
                                        house = ""
                                        pen = ""
                                        selectedHouse = null
                                        selectedPen = null
                                        pens = emptyList()
                                    } else {
                                        errorMessage = result.message
                                    }
                                }.onFailure {
                                    errorMessage = it.message ?: "Failed to add new batch."
                                }
                                isLoading = false
                            }
                        },
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E5D36)),
                        modifier = Modifier.height(40.dp),
                        enabled = !isLoading
                    ) {
                        Text(
                            text = if (isLoading) "Submitting..." else "Submit",
                            color = Color.White,
                            fontFamily = NewBatchPoppins
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NewBatchLabel(text: String) {
    Text(
        text = text,
        fontSize = 12.sp,
        fontFamily = NewBatchPoppins,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.padding(bottom = 4.dp)
    )
}

@Composable
private fun NewBatchInputField(
    value: String,
    scrollState: ScrollState,
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
                        delay(350)
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
private fun NewBatchReadOnlyField(value: String) {
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
private fun NewBatchDropdownField(
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
                    fontFamily = NewBatchPoppins,
                    fontSize = 14.sp,
                    color = Color.Black
                )
                Text(
                    text = "⌄",
                    fontFamily = NewBatchPoppins,
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
                    text = { Text(option, fontFamily = NewBatchPoppins) },
                    onClick = { onValueSelected(option) }
                )
            }
        }
    }
}

@Composable
private fun NewBatchBottomNavBar(
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
        NewBatchBottomNavItem(Icons.Outlined.Home, "Dashboard", false, onDashboardClick)
        NewBatchBottomNavItem(Icons.AutoMirrored.Outlined.List, "Tasks", false, onTasksClick)
        NewBatchBottomNavItem(Icons.Outlined.Edit, "Farm Management", true, onFarmClick)
        NewBatchBottomNavItem(Icons.Outlined.AccountCircle, "Profile", false, {})
    }
}

@Composable
private fun NewBatchBottomNavItem(
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
            fontFamily = NewBatchPoppins,
            fontWeight = FontWeight.Normal,
            fontSize = 10.sp,
            color = if (selected) Color.White else Color(0xFFD7ECD9),
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}
