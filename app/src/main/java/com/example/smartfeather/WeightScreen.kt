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
import java.time.temporal.ChronoUnit
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
fun WeightScreen(
    employeeId: Int,
    onBackToFarm: () -> Unit,
    onNavigateToDashboard: () -> Unit,
    onNavigateToTasks: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onGoToBiosecurity: () -> Unit
) {
    val weightService = remember { WeightBackendService() }
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    val openedAt = remember { LocalDateTime.now() }
    val openedDate = remember(openedAt) {
        openedAt.format(DateTimeFormatter.ofPattern("M-d-yy", Locale.getDefault()))
    }
    val openedTime = remember(openedAt) {
        openedAt.format(DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault()))
    }
    val recordedDateValue = remember(openedAt) {
        openedAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
    }
    val recordedTimeValue = remember(openedAt) {
        openedAt.format(DateTimeFormatter.ofPattern("HH:mm:ss"))
    }

    var house by remember { mutableStateOf("") }
    var pen by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var flocks by remember { mutableStateOf("") }
    var flocksWithCases by remember { mutableStateOf("") }
    var targetWeight by remember { mutableStateOf("") }
    var weights by remember { mutableStateOf<List<String>>(emptyList()) }

    var selectedHouse by remember { mutableStateOf<WeightHouseOption?>(null) }
    var selectedPen by remember { mutableStateOf<WeightPenOption?>(null) }
    var pens by remember { mutableStateOf<List<WeightPenOption>>(emptyList()) }

    var penExpanded by remember { mutableStateOf(false) }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    var showBlockedDialog by remember { mutableStateOf(false) }
    var blockedMessage by remember { mutableStateOf("") }

    LaunchedEffect(employeeId) {
        weightService.getWeightContext(employeeId)
            .onSuccess { context ->
                if (!context.accessAllowed) {
                    blockedMessage = context.message
                        ?: "Please complete personnel biosecurity before accessing weight sampling."
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
                blockedMessage = it.message ?: "Failed to load weight sampling context."
                showBlockedDialog = true
                house = ""
                selectedHouse = null
                pens = emptyList()
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
            BottomNavBar(
                onTasksClick = onNavigateToTasks,
                onFarmManagementClick = onBackToFarm,
                onProfileClick = onNavigateToProfile
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF1EFEC))
                .padding(padding)
                .verticalScroll(scrollState)
                .imePadding()
                .pointerInput(Unit) {
                    detectTapGestures {
                        focusManager.clearFocus()
                    }
                }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF6A1B9A))
                    .padding(vertical = 12.dp),
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
                    text = "Weight",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontFamily = FarmPoppins,
                    fontWeight = FontWeight.Bold
                )
            }

            Column(
                modifier = Modifier.padding(18.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Column(Modifier.weight(1f)) {
                        WeightLabel("Date")
                        WeightReadOnlyField(openedDate)
                    }

                    Column(Modifier.weight(1f)) {
                        WeightLabel("Time")
                        WeightReadOnlyField(openedTime)
                    }
                }

                Spacer(Modifier.height(10.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Column(Modifier.weight(1f)) {
                        WeightLabel("House")
                        WeightReadOnlyField(house)
                    }

                    Column(Modifier.weight(1f)) {
                        WeightLabel("Pen")
                        WeightDropdownField(
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

                                val startedAtText = selectedPen?.currentBatchStartedAt
                                if (startedAtText.isNullOrBlank()) {
                                    age = ""
                                } else {
                                    val parsedStartedAt = runCatching {
                                        LocalDateTime.parse(
                                            startedAtText,
                                            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                                        )
                                    }.getOrNull()

                                    if (parsedStartedAt == null) {
                                        age = ""
                                    } else {
                                        val days = ChronoUnit.DAYS.between(
                                            parsedStartedAt.toLocalDate(),
                                            openedAt.toLocalDate()
                                        ).toInt()

                                        age = days.toString()
                                    }
                                }
                            }
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))

                WeightLabel("Age (Days)")
                WeightReadOnlyField(age)

                Spacer(Modifier.height(10.dp))

                WeightLabel("Number of Flocks")
                WeightInputField(
                    value = flocks,
                    keyboardType = KeyboardType.Number,
                    scrollState = scrollState
                ) { newValue ->
                    val digitsOnly = newValue.filter { it.isDigit() }
                    flocks = digitsOnly

                    val count = digitsOnly.toIntOrNull() ?: 0
                    weights = if (count > 0) {
                        List(count) { index -> weights.getOrNull(index) ?: "" }
                    } else {
                        emptyList()
                    }
                }

                Spacer(Modifier.height(10.dp))

                WeightLabel("Flocks With Cases")
                WeightInputField(
                    value = flocksWithCases,
                    keyboardType = KeyboardType.Number,
                    scrollState = scrollState
                ) { newValue ->
                    flocksWithCases = newValue.filter { it.isDigit() }
                }

                Spacer(Modifier.height(10.dp))

                WeightLabel("Target Weight")
                WeightInputField(
                    value = targetWeight,
                    keyboardType = KeyboardType.Decimal,
                    scrollState = scrollState
                ) { newValue ->
                    val filtered = buildString {
                        var dotUsed = false
                        newValue.forEach { ch ->
                            if (ch.isDigit()) append(ch)
                            else if (ch == '.' && !dotUsed) {
                                append(ch)
                                dotUsed = true
                            }
                        }
                    }
                    targetWeight = filtered
                }

                weights.chunked(3).forEachIndexed { rowIndex, rowWeights ->
                    Spacer(Modifier.height(10.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        rowWeights.forEachIndexed { columnIndex, value ->
                            val actualIndex = rowIndex * 3 + columnIndex

                            Column(Modifier.weight(1f)) {
                                WeightLabel("Weight ${actualIndex + 1}")
                                WeightInputField(
                                    value = value,
                                    keyboardType = KeyboardType.Decimal,
                                    scrollState = scrollState
                                ) { newValue ->
                                    val filtered = buildString {
                                        var dotUsed = false
                                        newValue.forEach { ch ->
                                            if (ch.isDigit()) append(ch)
                                            else if (ch == '.' && !dotUsed) {
                                                append(ch)
                                                dotUsed = true
                                            }
                                        }
                                    }

                                    weights = weights.toMutableList().also {
                                        it[actualIndex] = filtered
                                    }
                                }
                            }
                        }

                        repeat(3 - rowWeights.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                errorMessage?.let {
                    Text(
                        text = it,
                        color = Color(0xFFB00020),
                        fontFamily = FarmPoppins,
                        fontSize = 13.sp
                    )
                    Spacer(Modifier.height(8.dp))
                }

                successMessage?.let {
                    Text(
                        text = it,
                        color = Color(0xFF1E5D36),
                        fontFamily = FarmPoppins,
                        fontSize = 13.sp
                    )
                    Spacer(Modifier.height(8.dp))
                }

                Spacer(Modifier.height(20.dp))

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
                            val ageValue = age.toIntOrNull()
                            val flockCount = flocks.toIntOrNull()
                            val flocksWithCasesValue = flocksWithCases.toIntOrNull() ?: 0
                            val targetWeightValue = targetWeight.toDoubleOrNull()
                            val weightValues = weights.map { it.toDoubleOrNull() }

                            if (currentHouse == null) {
                                errorMessage = "No house is assigned from your biosecurity entry."
                                return@Button
                            }
                            if (currentPen == null) {
                                errorMessage = "Please select a pen."
                                return@Button
                            }
                            if (ageValue == null) {
                                errorMessage = "Selected pen has no running batch."
                                return@Button
                            }
                            if (flockCount == null || flockCount <= 0) {
                                errorMessage = "Please enter a valid number of flocks."
                                return@Button
                            }
                            if (targetWeightValue == null || targetWeightValue <= 0.0) {
                                errorMessage = "Please enter a valid target weight."
                                return@Button
                            }
                            if (weightValues.size != flockCount || weightValues.any { it == null }) {
                                errorMessage = "Please enter valid weights for all flocks."
                                return@Button
                            }

                            coroutineScope.launch {
                                isLoading = true
                                weightService.submitWeightSampling(
                                    employeeId = employeeId,
                                    houseId = currentHouse.id,
                                    penId = currentPen.id,
                                    numberOfFlocks = flockCount,
                                    flocksWithCases = flocksWithCasesValue,
                                    targetWeight = targetWeightValue,
                                    weights = weightValues.filterNotNull(),
                                    recordedDate = recordedDateValue,
                                    recordedTime = recordedTimeValue
                                ).onSuccess { response ->
                                    if (response.success == true) {
                                        successMessage =
                                            "Average: ${response.averageWeight} | Target: ${response.target} | Status: ${response.status}"
                                        pen = ""
                                        age = ""
                                        flocks = ""
                                        flocksWithCases = ""
                                        targetWeight = ""
                                        weights = emptyList()
                                        selectedPen = null
                                    } else {
                                        errorMessage = response.message ?: "Failed to submit weight sampling."
                                    }
                                }.onFailure {
                                    errorMessage = it.message ?: "Failed to submit weight sampling."
                                }
                                isLoading = false
                            }
                        },
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1E5D36)
                        ),
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
private fun WeightLabel(text: String) {
    Text(
        text = text,
        fontSize = 12.sp,
        fontFamily = FarmPoppins,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.padding(bottom = 4.dp)
    )
}

@Composable
private fun WeightInputField(
    value: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    scrollState: ScrollState,
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
private fun WeightReadOnlyField(value: String) {
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
private fun WeightDropdownField(
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
                    fontSize = 18.sp,
                    fontFamily = FarmPoppins,
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