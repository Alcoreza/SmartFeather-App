package com.example.smartfeather

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.filled.KeyboardArrowDown
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
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
import com.composables.icons.lucide.ClipboardList
import com.composables.icons.lucide.House
import com.composables.icons.lucide.LayoutDashboard
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.UserRound

private val NewBatchManrope = FontFamily(
    Font(R.font.manrope_extralight, FontWeight.ExtraLight),
    Font(R.font.manrope_light, FontWeight.Light),
    Font(R.font.manrope_regular, FontWeight.Normal),
    Font(R.font.manrope_medium, FontWeight.Medium),
    Font(R.font.manrope_semibold, FontWeight.SemiBold),
    Font(R.font.manrope_bold, FontWeight.Bold),
    Font(R.font.manrope_extrabold, FontWeight.ExtraBold),
    Font(R.font.manrope_variablefont_wght, FontWeight.Black)
)

private val NewBatchBackground = Color(0xFFF6F3EC)
private val NewBatchSurface = Color(0xFFFFFCF7)
private val NewBatchSurfaceAlt = Color(0xFFF3EFE7)
private val NewBatchAutoField = Color(0xFFE8E3DA)
private val NewBatchInk = Color(0xFF121A14)
private val NewBatchMuted = Color(0xFF677168)
private val NewBatchLine = Color(0xFFD8D0C3)
private val NewBatchGreen = Color(0xFF1F7A3A)
private val NewBatchDeepGreen = Color(0xFF062717)
private val NewBatchForest = Color(0xFF103C28)
private val NewBatchLeaf = Color(0xFF2F8F45)
private val NewBatchAmber = Color(0xFFD78A2B)
private val NewBatchCoral = Color(0xFFC76655)
private val NewBatchDanger = Color(0xFFC62828)

@Composable
fun NewBirdBatchScreen(
    employeeId: Int,
    onBackToFarm: () -> Unit,
    onNavigateToDashboard: () -> Unit,
    onNavigateToTasks: () -> Unit,
    onGoToBiosecurity: () -> Unit
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

    var pens by remember { mutableStateOf<List<NewBatchPenOption>>(emptyList()) }

    var selectedHouse by remember { mutableStateOf<NewBatchHouseOption?>(null) }
    var selectedPen by remember { mutableStateOf<NewBatchPenOption?>(null) }

    var penExpanded by remember { mutableStateOf(false) }

    var isContextLoading by remember { mutableStateOf(true) }
    var isLoading by remember { mutableStateOf(false) }
    var contentVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var showConflictDialog by remember { mutableStateOf(false) }
    var conflictMessage by remember { mutableStateOf("") }

    var showBlockedDialog by remember { mutableStateOf(false) }
    var blockedMessage by remember { mutableStateOf("") }

    var showRequiredDialog by remember { mutableStateOf(false) }
    var requiredDialogMessage by remember { mutableStateOf("") }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var successDialogMessage by remember { mutableStateOf("New bird batch added.") }

    fun showRequired(message: String) {
        requiredDialogMessage = message
        showRequiredDialog = true
    }

    LaunchedEffect(employeeId) {
        isContextLoading = true
        contentVisible = false
        errorMessage = null

        batchService.getNewBatchContext(employeeId)
            .onSuccess { context ->
                if (!context.accessAllowed) {
                    blockedMessage = context.message
                        ?: "Please complete personnel biosecurity before accessing add new batch."
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
                blockedMessage = it.message ?: "Failed to load add new batch context."
                showBlockedDialog = true
                house = ""
                selectedHouse = null
                pens = emptyList()
            }

        isContextLoading = false
        delay(120)
        contentVisible = true
    }

    if (showConflictDialog) {
        AlertDialog(
            onDismissRequest = { showConflictDialog = false },
            containerColor = NewBatchSurface,
            shape = RoundedCornerShape(28.dp),
            title = { NewBatchDialogTitle("Running Batch Found") },
            text = { NewBatchDialogBody(conflictMessage) },
            confirmButton = {
                TextButton(onClick = { showConflictDialog = false }) {
                    NewBatchDialogButtonText("OK", NewBatchGreen)
                }
            }
        )
    }

    if (showBlockedDialog) {
        AlertDialog(
            onDismissRequest = {},
            containerColor = NewBatchSurface,
            shape = RoundedCornerShape(28.dp),
            title = { NewBatchDialogTitle("Biosecurity Required") },
            text = { NewBatchDialogBody(blockedMessage) },
            dismissButton = {
                TextButton(onClick = onBackToFarm) {
                    NewBatchDialogButtonText("Back", NewBatchMuted)
                }
            },
            confirmButton = {
                TextButton(onClick = onGoToBiosecurity) {
                    NewBatchDialogButtonText("Go to Biosecurity", NewBatchGreen)
                }
            }
        )
    }

    if (showRequiredDialog) {
        AlertDialog(
            onDismissRequest = { showRequiredDialog = false },
            containerColor = NewBatchSurface,
            shape = RoundedCornerShape(28.dp),
            title = { NewBatchDialogTitle("Complete Required Fields") },
            text = { NewBatchDialogBody(requiredDialogMessage) },
            confirmButton = {
                TextButton(onClick = { showRequiredDialog = false }) {
                    NewBatchDialogButtonText("Got it", NewBatchGreen)
                }
            }
        )
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false },
            containerColor = NewBatchSurface,
            shape = RoundedCornerShape(28.dp),
            title = { NewBatchDialogTitle("Submitted") },
            text = { NewBatchDialogBody(successDialogMessage) },
            confirmButton = {
                TextButton(onClick = { showSuccessDialog = false }) {
                    NewBatchDialogButtonText("Done", NewBatchGreen)
                }
            }
        )
    }

    Scaffold(
        containerColor = NewBatchBackground,
        bottomBar = {
            NewBatchBottomNavBar(
                onDashboardClick = onNavigateToDashboard,
                onTasksClick = onNavigateToTasks,
                onFarmClick = onBackToFarm
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFFFBF8F1), NewBatchBackground, Color(0xFFEDE7DA))
                    )
                )
                .padding(padding)
                .pointerInput(Unit) {
                    detectTapGestures {
                        focusManager.clearFocus()
                    }
                }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .imePadding()
                    .padding(horizontal = 18.dp, vertical = 18.dp)
            ) {
                NewBatchHero(onBackToFarm = onBackToFarm)

                Spacer(modifier = Modifier.height(18.dp))

                if (isContextLoading) {
                    NewBatchSkeleton()
                    Spacer(modifier = Modifier.height(20.dp))
                    return@Column
                }

                errorMessage?.let {
                    NewBatchMessageBanner(
                        text = it,
                        backgroundColor = Color(0xFFFFECEA),
                        contentColor = NewBatchDanger
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                }

                AnimatedVisibility(
                    visible = contentVisible,
                    enter = fadeIn(animationSpec = tween(420)) + slideInVertically(
                        animationSpec = tween(420, easing = FastOutSlowInEasing),
                        initialOffsetY = { it / 12 }
                    )
                ) {
                    Column {
                        NewBatchSectionPanel {
                            NewBatchSectionHeader(
                                title = "Batch Details",
                                accentColor = NewBatchCoral
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            NewBatchLabel("Batch Code")
                            Spacer(modifier = Modifier.height(8.dp))
                            NewBatchInputField(
                                value = batchCode,
                                scrollState = scrollState
                            ) {
                                batchCode = it
                                errorMessage = null
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            NewBatchLabel("Initial Population")
                            Spacer(modifier = Modifier.height(8.dp))
                            NewBatchInputField(
                                value = initialPopulation,
                                scrollState = scrollState,
                                keyboardType = KeyboardType.Number
                            ) { newValue ->
                                initialPopulation = newValue.filter { it.isDigit() }
                                errorMessage = null
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        NewBatchSectionPanel {
                            NewBatchSectionHeader(
                                title = "Start Record",
                                accentColor = NewBatchAmber
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Column(modifier = Modifier.weight(1f)) {
                                    NewBatchLabel("Date")
                                    Spacer(modifier = Modifier.height(8.dp))
                                    NewBatchReadOnlyField(displayDate)
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    NewBatchLabel("Time")
                                    Spacer(modifier = Modifier.height(8.dp))
                                    NewBatchReadOnlyField(displayTime)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        NewBatchSectionPanel {
                            NewBatchSectionHeader(
                                title = "Placement",
                                accentColor = NewBatchGreen
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Column(modifier = Modifier.weight(1f)) {
                                    NewBatchLabel("House")
                                    Spacer(modifier = Modifier.height(8.dp))
                                    NewBatchReadOnlyField(house.ifBlank { "-" })
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    NewBatchLabel("Pen")
                                    Spacer(modifier = Modifier.height(8.dp))
                                    NewBatchDropdownField(
                                        value = pen,
                                        placeholder = "Select pen",
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
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Button(
                                onClick = {
                                    focusManager.clearFocus()
                                    errorMessage = null

                                    val currentHouse = selectedHouse
                                    val currentPen = selectedPen
                                    val initialPopulationValue = initialPopulation.toIntOrNull()

                                    when {
                                        batchCode.isBlank() -> {
                                            showRequired("Please enter a batch code.")
                                            return@Button
                                        }

                                        initialPopulationValue == null || initialPopulationValue <= 0 -> {
                                            showRequired("Please enter a valid initial population.")
                                            return@Button
                                        }

                                        currentHouse == null || house.isBlank() -> {
                                            showRequired("House is required. Please complete biosecurity first so the assigned house can be loaded.")
                                            return@Button
                                        }

                                        currentPen == null || pen.isBlank() -> {
                                            showRequired("Please select a pen before submitting.")
                                            return@Button
                                        }
                                    }

                                    coroutineScope.launch {
                                        isLoading = true

                                        batchService.submitNewBatch(
                                            employeeId = employeeId,
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
                                                successDialogMessage = result.message.ifBlank { "New bird batch added." }
                                                showSuccessDialog = true
                                                batchCode = ""
                                                initialPopulation = ""
                                                pen = ""
                                                selectedPen = null
                                            } else {
                                                errorMessage = result.message
                                            }
                                        }.onFailure {
                                            errorMessage = it.message ?: "Failed to add new batch."
                                        }

                                        isLoading = false
                                    }
                                },
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = NewBatchGreen,
                                    disabledContainerColor = Color(0xFF94A99A)
                                ),
                                modifier = Modifier.height(48.dp),
                                enabled = !isLoading && !showBlockedDialog
                            ) {
                                Text(
                                    text = if (isLoading) "Submitting..." else "Submit",
                                    color = Color.White,
                                    fontFamily = NewBatchManrope,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 14.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun NewBatchHero(
    onBackToFarm: () -> Unit
) {
    val transition = rememberInfiniteTransition(label = "newBatchHeroMotion")

    val pulse by transition.animateFloat(
        initialValue = 0.08f,
        targetValue = 0.18f,
        animationSpec = infiniteRepeatable(
            animation = tween(4200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "newBatchPulse"
    )

    val orbit by transition.animateFloat(
        initialValue = -10f,
        targetValue = 12f,
        animationSpec = infiniteRepeatable(
            animation = tween(5200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "newBatchOrbit"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF7A4A2A),
                        Color(0xFFB96B45),
                        Color(0xFFD89A54)
                    )
                )
            )
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .offset(x = (18 + orbit).dp)
                .size(86.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = pulse * 0.52f))
        )

        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .offset(x = (-28 - orbit * 0.35f).dp)
                .size(42.dp)
                .clip(CircleShape)
                .border(1.dp, Color.White.copy(alpha = pulse + 0.08f), CircleShape)
        )

        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .offset(x = (-86 + orbit * 0.22f).dp, y = 14.dp)
                .size(width = 52.dp, height = 10.dp)
                .graphicsLayer(rotationZ = -12f)
                .clip(RoundedCornerShape(999.dp))
                .background(Color.White.copy(alpha = pulse * 0.74f))
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.14f))
                    .border(1.dp, Color.White.copy(alpha = 0.16f), CircleShape)
                    .clickable { onBackToFarm() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White,
                    modifier = Modifier.size(19.dp)
                )
            }

            Spacer(modifier = Modifier.size(12.dp))

            Text(
                text = "New Bird Batch",
                fontFamily = NewBatchManrope,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 22.sp,
                color = Color.White,
                lineHeight = 26.sp,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun NewBatchSectionPanel(
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .background(NewBatchSurface)
            .border(1.dp, NewBatchLine.copy(alpha = 0.70f), RoundedCornerShape(26.dp))
            .padding(horizontal = 18.dp, vertical = 18.dp),
        content = content
    )
}

@Composable
private fun NewBatchSectionHeader(
    title: String,
    accentColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(width = 4.dp, height = 28.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(accentColor)
        )

        Spacer(modifier = Modifier.size(12.dp))

        Text(
            text = title,
            fontFamily = NewBatchManrope,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 18.sp,
            color = NewBatchInk
        )
    }
}

@Composable
private fun NewBatchLabel(text: String) {
    Text(
        text = text,
        fontSize = 13.sp,
        fontFamily = NewBatchManrope,
        fontWeight = FontWeight.ExtraBold,
        color = NewBatchInk
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
        textStyle = TextStyle(
            fontFamily = NewBatchManrope,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            color = NewBatchInk
        ),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape = RoundedCornerShape(18.dp),
        colors = newBatchFieldColors()
    )
}

@Composable
private fun NewBatchReadOnlyField(value: String) {
    OutlinedTextField(
        value = value,
        onValueChange = {},
        readOnly = true,
        singleLine = true,
        textStyle = TextStyle(
            fontFamily = NewBatchManrope,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            color = NewBatchMuted
        ),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = NewBatchAutoField,
            unfocusedContainerColor = NewBatchAutoField,
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent,
            focusedTextColor = NewBatchMuted,
            unfocusedTextColor = NewBatchMuted,
            cursorColor = NewBatchGreen
        )
    )
}

@Composable
private fun NewBatchDropdownField(
    value: String,
    placeholder: String,
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
                .clip(RoundedCornerShape(18.dp))
                .background(NewBatchSurfaceAlt)
                .clickable { onExpandedChange(true) }
                .padding(horizontal = 14.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (value.isBlank()) placeholder else value,
                    fontFamily = NewBatchManrope,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = if (value.isBlank()) NewBatchMuted else NewBatchInk,
                    modifier = Modifier.weight(1f)
                )

                Icon(
                    imageVector = Icons.Filled.KeyboardArrowDown,
                    contentDescription = placeholder,
                    tint = NewBatchGreen,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) },
            modifier = Modifier.background(NewBatchSurface)
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = option,
                            fontFamily = NewBatchManrope,
                            fontWeight = FontWeight.SemiBold,
                            color = NewBatchInk
                        )
                    },
                    onClick = { onValueSelected(option) }
                )
            }
        }
    }
}

@Composable
private fun newBatchFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = NewBatchSurfaceAlt,
    unfocusedContainerColor = NewBatchSurfaceAlt,
    focusedBorderColor = NewBatchGreen,
    unfocusedBorderColor = Color.Transparent,
    focusedTextColor = NewBatchInk,
    unfocusedTextColor = NewBatchInk,
    cursorColor = NewBatchGreen
)

@Composable
private fun NewBatchMessageBanner(
    text: String,
    backgroundColor: Color,
    contentColor: Color
) {
    Text(
        text = text,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .border(1.dp, contentColor.copy(alpha = 0.22f), RoundedCornerShape(16.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        fontFamily = NewBatchManrope,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        color = contentColor
    )
}

@Composable
private fun NewBatchDialogTitle(text: String) {
    Text(
        text = text,
        fontFamily = NewBatchManrope,
        fontWeight = FontWeight.ExtraBold,
        color = NewBatchInk
    )
}

@Composable
private fun NewBatchDialogBody(text: String) {
    Text(
        text = text,
        fontFamily = NewBatchManrope,
        fontWeight = FontWeight.Medium,
        color = NewBatchMuted,
        lineHeight = 21.sp
    )
}

@Composable
private fun NewBatchDialogButtonText(
    text: String,
    color: Color
) {
    Text(
        text = text,
        fontFamily = NewBatchManrope,
        fontWeight = FontWeight.ExtraBold,
        color = color
    )
}

@Composable
private fun NewBatchSkeleton() {
    val alpha = newBatchSkeletonAlpha()

    Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
        NewBatchSkeletonPanel(alpha = alpha, height = 202.dp)
        NewBatchSkeletonPanel(alpha = alpha, height = 146.dp)
        NewBatchSkeletonPanel(alpha = alpha, height = 146.dp)
    }
}

@Composable
private fun NewBatchSkeletonPanel(
    alpha: Float,
    height: androidx.compose.ui.unit.Dp
) {
    val showExtraField = height.value > 180f

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(26.dp))
            .background(NewBatchSurface)
            .border(1.dp, NewBatchLine.copy(alpha = 0.70f), RoundedCornerShape(26.dp))
            .padding(18.dp)
    ) {
        NewBatchSkeletonLine(0.36f, 18.dp, alpha)
        Spacer(modifier = Modifier.height(18.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            NewBatchSkeletonBox(
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                alpha = alpha
            )

            NewBatchSkeletonBox(
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                alpha = alpha
            )
        }

        if (showExtraField) {
            Spacer(modifier = Modifier.height(14.dp))
            NewBatchSkeletonBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                alpha = alpha
            )
        }
    }
}

@Composable
private fun newBatchSkeletonAlpha(): Float {
    val transition = rememberInfiniteTransition(label = "newBatchSkeleton")
    val alpha by transition.animateFloat(
        initialValue = 0.32f,
        targetValue = 0.72f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "newBatchSkeletonAlpha"
    )
    return alpha
}

@Composable
private fun NewBatchSkeletonLine(
    widthFraction: Float,
    height: androidx.compose.ui.unit.Dp,
    alpha: Float
) {
    Box(
        modifier = Modifier
            .fillMaxWidth(widthFraction)
            .height(height)
            .clip(RoundedCornerShape(999.dp))
            .background(Color(0xFFDAD4C8).copy(alpha = alpha))
    )
}

@Composable
private fun NewBatchSkeletonBox(
    modifier: Modifier,
    alpha: Float,
    shape: Shape = RoundedCornerShape(18.dp)
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(Color(0xFFDAD4C8).copy(alpha = alpha))
    )
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
                    colors = listOf(Color(0xFF07381F), Color(0xFF022716))
                )
            )
            .navigationBarsPadding()
            .padding(horizontal = 8.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        NewBatchBottomNavItem(Lucide.LayoutDashboard, "Dashboard", false, onDashboardClick)
        NewBatchBottomNavItem(Lucide.ClipboardList, "Tasks", false, onTasksClick)
        NewBatchBottomNavItem(Lucide.House, "Farm Management", true, onFarmClick)
        NewBatchBottomNavItem(Lucide.UserRound, "Profile", false, {})
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
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() }
            .padding(horizontal = 8.dp, vertical = 5.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (selected) Color.White else Color(0xFFCFE8D2),
            modifier = Modifier.size(22.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = label,
            fontFamily = NewBatchManrope,
            fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.SemiBold,
            fontSize = 9.sp,
            color = if (selected) Color.White else Color(0xFFCFE8D2),
            textAlign = TextAlign.Center,
            maxLines = 1,
            lineHeight = 10.sp
        )
    }
}