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

private val VitaminsManrope = FontFamily(
    Font(R.font.manrope_extralight, FontWeight.ExtraLight),
    Font(R.font.manrope_light, FontWeight.Light),
    Font(R.font.manrope_regular, FontWeight.Normal),
    Font(R.font.manrope_medium, FontWeight.Medium),
    Font(R.font.manrope_semibold, FontWeight.SemiBold),
    Font(R.font.manrope_bold, FontWeight.Bold),
    Font(R.font.manrope_extrabold, FontWeight.ExtraBold),
    Font(R.font.manrope_variablefont_wght, FontWeight.Black)
)

private val VitaminsBackground = Color(0xFFF6F3EC)
private val VitaminsSurface = Color(0xFFFFFCF7)
private val VitaminsSurfaceAlt = Color(0xFFF3EFE7)
private val VitaminsAutoField = Color(0xFFE8E3DA)
private val VitaminsInk = Color(0xFF121A14)
private val VitaminsMuted = Color(0xFF677168)
private val VitaminsLine = Color(0xFFD8D0C3)
private val VitaminsGreen = Color(0xFF1F7A3A)
private val VitaminsTeal = Color(0xFF2E7D6B)
private val VitaminsDeepTeal = Color(0xFF123B34)
private val VitaminsMint = Color(0xFF8FD8B6)
private val VitaminsBlueGreen = Color(0xFF3C8C82)
private val VitaminsDanger = Color(0xFFC62828)

@Composable
fun VitaminsRefillScreen(
    employeeId: Int,
    onBackToFarm: () -> Unit,
    onNavigateToDashboard: () -> Unit,
    onNavigateToTasks: () -> Unit,
    onGoToBiosecurity: () -> Unit
) {
    val vitaminsService = remember { VitaminsRefillBackendService() }
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
    val recordedAtValue = remember(openedAt) {
        openedAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"))
    }

    var house by remember { mutableStateOf("") }
    var pen by remember { mutableStateOf("") }
    var typeOfVitamins by remember { mutableStateOf("") }
    var bottlesUsed by remember { mutableStateOf("") }

    var selectedHouse by remember { mutableStateOf<VitaminHouseOption?>(null) }
    var pens by remember { mutableStateOf<List<VitaminPenOption>>(emptyList()) }
    var vitaminOptions by remember { mutableStateOf<List<VitaminInventoryOption>>(emptyList()) }

    var selectedPen by remember { mutableStateOf<VitaminPenOption?>(null) }
    var selectedVitamin by remember { mutableStateOf<VitaminInventoryOption?>(null) }

    var penExpanded by remember { mutableStateOf(false) }
    var vitaminExpanded by remember { mutableStateOf(false) }

    var isContextLoading by remember { mutableStateOf(true) }
    var isLoading by remember { mutableStateOf(false) }
    var contentVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var showBlockedDialog by remember { mutableStateOf(false) }
    var blockedMessage by remember { mutableStateOf("") }

    var showRequiredDialog by remember { mutableStateOf(false) }
    var requiredDialogMessage by remember { mutableStateOf("") }
    var showSuccessDialog by remember { mutableStateOf(false) }

    fun showRequired(message: String) {
        requiredDialogMessage = message
        showRequiredDialog = true
    }

    LaunchedEffect(employeeId) {
        isContextLoading = true
        contentVisible = false
        errorMessage = null

        vitaminsService.getVitaminsContext(employeeId)
            .onSuccess { context ->
                if (!context.accessAllowed) {
                    blockedMessage = context.message
                        ?: "Please complete personnel biosecurity before accessing vitamins refill."
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
                blockedMessage = it.message ?: "Failed to load vitamins refill context."
                showBlockedDialog = true
                house = ""
                selectedHouse = null
                pens = emptyList()
            }

        vitaminsService.getVitaminInventoryOptions()
            .onSuccess { vitaminOptions = it }
            .onFailure { errorMessage = it.message ?: "Failed to load vitamin inventory." }

        isContextLoading = false
        delay(120)
        contentVisible = true
    }

    if (showBlockedDialog) {
        AlertDialog(
            onDismissRequest = {},
            containerColor = VitaminsSurface,
            shape = RoundedCornerShape(28.dp),
            title = { VitaminsDialogTitle("Biosecurity Required") },
            text = { VitaminsDialogBody(blockedMessage) },
            dismissButton = {
                TextButton(onClick = onBackToFarm) {
                    VitaminsDialogButtonText("Back", VitaminsMuted)
                }
            },
            confirmButton = {
                TextButton(onClick = onGoToBiosecurity) {
                    VitaminsDialogButtonText("Go to Biosecurity", VitaminsGreen)
                }
            }
        )
    }

    if (showRequiredDialog) {
        AlertDialog(
            onDismissRequest = { showRequiredDialog = false },
            containerColor = VitaminsSurface,
            shape = RoundedCornerShape(28.dp),
            title = { VitaminsDialogTitle("Complete Required Fields") },
            text = { VitaminsDialogBody(requiredDialogMessage) },
            confirmButton = {
                TextButton(onClick = { showRequiredDialog = false }) {
                    VitaminsDialogButtonText("Got it", VitaminsGreen)
                }
            }
        )
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false },
            containerColor = VitaminsSurface,
            shape = RoundedCornerShape(28.dp),
            title = { VitaminsDialogTitle("Submitted") },
            text = { VitaminsDialogBody("Vitamins refill submitted.") },
            confirmButton = {
                TextButton(onClick = { showSuccessDialog = false }) {
                    VitaminsDialogButtonText("Done", VitaminsGreen)
                }
            }
        )
    }

    Scaffold(
        containerColor = VitaminsBackground,
        bottomBar = {
            VitaminsBottomNavBar(
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
                        colors = listOf(Color(0xFFFBF8F1), VitaminsBackground, Color(0xFFEDE7DA))
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
                VitaminsHero(
                    openedDate = openedDate,
                    openedTime = openedTime,
                    onBackToFarm = onBackToFarm
                )

                Spacer(modifier = Modifier.height(18.dp))

                if (isContextLoading) {
                    VitaminsSkeleton()
                    Spacer(modifier = Modifier.height(20.dp))
                    return@Column
                }

                errorMessage?.let {
                    VitaminsMessageBanner(
                        text = it,
                        backgroundColor = Color(0xFFFFECEA),
                        contentColor = VitaminsDanger
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
                        VitaminsSectionPanel {
                            VitaminsSectionHeader(
                                title = "Location",
                                accentColor = VitaminsTeal
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Column(modifier = Modifier.weight(1f)) {
                                    VitaminsLabel("House")
                                    Spacer(modifier = Modifier.height(8.dp))
                                    VitaminsReadOnlyField(house.ifBlank { "-" })
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    VitaminsLabel("Pen")
                                    Spacer(modifier = Modifier.height(8.dp))
                                    VitaminsDropdownField(
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

                        VitaminsSectionPanel {
                            VitaminsSectionHeader(
                                title = "Vitamin Details",
                                accentColor = VitaminsBlueGreen
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            VitaminsLabel("Type of Vitamins")
                            Spacer(modifier = Modifier.height(8.dp))
                            VitaminsDropdownField(
                                value = typeOfVitamins,
                                placeholder = "Select vitamins",
                                options = vitaminOptions.map { it.itemName },
                                expanded = vitaminExpanded,
                                onExpandedChange = { vitaminExpanded = it },
                                onValueSelected = { selectedValue ->
                                    typeOfVitamins = selectedValue
                                    selectedVitamin = vitaminOptions.firstOrNull { it.itemName == selectedValue }
                                    vitaminExpanded = false
                                    errorMessage = null
                                }
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            VitaminsLabel("Bottles Used")
                            Spacer(modifier = Modifier.height(8.dp))
                            VitaminsInputField(
                                value = bottlesUsed,
                                keyboardType = KeyboardType.Number,
                                scrollState = scrollState
                            ) { newValue ->
                                bottlesUsed = newValue.filter { ch -> ch.isDigit() }
                                errorMessage = null
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
                                    val currentVitamin = selectedVitamin
                                    val bottlesValue = bottlesUsed.toIntOrNull()

                                    when {
                                        currentHouse == null || house.isBlank() -> {
                                            showRequired("House is required. Please complete biosecurity first so the assigned house can be loaded.")
                                            return@Button
                                        }

                                        currentPen == null || pen.isBlank() -> {
                                            showRequired("Please select a pen before submitting.")
                                            return@Button
                                        }

                                        currentVitamin == null || typeOfVitamins.isBlank() -> {
                                            showRequired("Please select a type of vitamins before submitting.")
                                            return@Button
                                        }

                                        bottlesValue == null || bottlesValue <= 0 -> {
                                            showRequired("Please enter a valid bottle count.")
                                            return@Button
                                        }
                                    }

                                    coroutineScope.launch {
                                        isLoading = true

                                        vitaminsService.submitVitaminRefill(
                                            employeeId = employeeId,
                                            inventoryId = currentVitamin.id,
                                            houseId = currentHouse.id,
                                            penId = currentPen.id,
                                            bottles = bottlesValue,
                                            recordedAt = recordedAtValue
                                        ).onSuccess { success ->
                                            if (success) {
                                                showSuccessDialog = true
                                                pen = ""
                                                typeOfVitamins = ""
                                                bottlesUsed = ""
                                                selectedPen = null
                                                selectedVitamin = null

                                                vitaminsService.getVitaminInventoryOptions()
                                                    .onSuccess { vitaminOptions = it }
                                            } else {
                                                errorMessage = "Failed to submit vitamins refill."
                                            }
                                        }.onFailure {
                                            errorMessage = it.message ?: "Failed to submit vitamins refill."
                                        }

                                        isLoading = false
                                    }
                                },
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = VitaminsGreen,
                                    disabledContainerColor = Color(0xFF94A99A)
                                ),
                                modifier = Modifier.height(48.dp),
                                enabled = !isLoading && !showBlockedDialog
                            ) {
                                Text(
                                    text = if (isLoading) "Submitting..." else "Submit",
                                    color = Color.White,
                                    fontFamily = VitaminsManrope,
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
private fun VitaminsHero(
    openedDate: String,
    openedTime: String,
    onBackToFarm: () -> Unit
) {
    val transition = rememberInfiniteTransition(label = "vitaminsHeroMotion")

    val bandShift by transition.animateFloat(
        initialValue = -44f,
        targetValue = 48f,
        animationSpec = infiniteRepeatable(
            animation = tween(4200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "vitaminsBandShift"
    )

    val glimmer by transition.animateFloat(
        initialValue = 0.08f,
        targetValue = 0.18f,
        animationSpec = infiniteRepeatable(
            animation = tween(3200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "vitaminsGlimmer"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(30.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(VitaminsDeepTeal, VitaminsTeal, VitaminsBlueGreen)
                )
            )
            .padding(18.dp)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .offset(x = bandShift.dp, y = (-18).dp)
                .size(width = 176.dp, height = 42.dp)
                .graphicsLayer(rotationZ = -22f)
                .clip(RoundedCornerShape(999.dp))
                .background(Color.White.copy(alpha = glimmer))
        )

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = (-72 - bandShift * 0.28f).dp, y = 6.dp)
                .size(width = 118.dp, height = 24.dp)
                .graphicsLayer(rotationZ = -22f)
                .clip(RoundedCornerShape(999.dp))
                .background(VitaminsMint.copy(alpha = glimmer * 0.82f))
        )

        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = (-18 + bandShift * 0.18f).dp, y = (-6).dp)
                .size(58.dp)
                .clip(CircleShape)
                .border(1.dp, Color.White.copy(alpha = glimmer + 0.08f), CircleShape)
        )

        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
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
                        modifier = Modifier.size(21.dp)
                    )
                }

                Spacer(modifier = Modifier.size(12.dp))

                Text(
                    text = "Vitamins Refill",
                    fontFamily = VitaminsManrope,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 25.sp,
                    color = Color.White,
                    lineHeight = 29.sp,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                VitaminsHeroMetric(
                    label = "Date",
                    value = openedDate,
                    modifier = Modifier.weight(1f)
                )

                VitaminsHeroMetric(
                    label = "Time",
                    value = openedTime,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun VitaminsHeroMetric(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White.copy(alpha = 0.14f))
            .padding(horizontal = 13.dp, vertical = 11.dp)
    ) {
        Text(
            text = label,
            fontFamily = VitaminsManrope,
            fontWeight = FontWeight.SemiBold,
            fontSize = 10.sp,
            color = Color.White.copy(alpha = 0.70f)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = value,
            fontFamily = VitaminsManrope,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 14.sp,
            color = Color.White
        )
    }
}

@Composable
private fun VitaminsSectionPanel(
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .background(VitaminsSurface)
            .border(1.dp, VitaminsLine.copy(alpha = 0.70f), RoundedCornerShape(26.dp))
            .padding(horizontal = 18.dp, vertical = 18.dp),
        content = content
    )
}

@Composable
private fun VitaminsSectionHeader(
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
            fontFamily = VitaminsManrope,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 18.sp,
            color = VitaminsInk
        )
    }
}

@Composable
private fun VitaminsLabel(text: String) {
    Text(
        text = text,
        fontSize = 13.sp,
        fontFamily = VitaminsManrope,
        fontWeight = FontWeight.ExtraBold,
        color = VitaminsInk
    )
}

@Composable
private fun VitaminsInputField(
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
        textStyle = TextStyle(
            fontFamily = VitaminsManrope,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            color = VitaminsInk
        ),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape = RoundedCornerShape(18.dp),
        colors = vitaminsFieldColors()
    )
}

@Composable
private fun VitaminsReadOnlyField(value: String) {
    OutlinedTextField(
        value = value,
        onValueChange = {},
        readOnly = true,
        singleLine = true,
        textStyle = TextStyle(
            fontFamily = VitaminsManrope,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            color = VitaminsMuted
        ),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = VitaminsAutoField,
            unfocusedContainerColor = VitaminsAutoField,
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent,
            focusedTextColor = VitaminsMuted,
            unfocusedTextColor = VitaminsMuted,
            cursorColor = VitaminsTeal
        )
    )
}

@Composable
private fun VitaminsDropdownField(
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
                .background(VitaminsSurfaceAlt)
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
                    fontFamily = VitaminsManrope,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = if (value.isBlank()) VitaminsMuted else VitaminsInk,
                    modifier = Modifier.weight(1f)
                )

                Icon(
                    imageVector = Icons.Filled.KeyboardArrowDown,
                    contentDescription = placeholder,
                    tint = VitaminsTeal,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) },
            modifier = Modifier.background(VitaminsSurface)
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = option,
                            fontFamily = VitaminsManrope,
                            fontWeight = FontWeight.SemiBold,
                            color = VitaminsInk
                        )
                    },
                    onClick = { onValueSelected(option) }
                )
            }
        }
    }
}

@Composable
private fun vitaminsFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = VitaminsSurfaceAlt,
    unfocusedContainerColor = VitaminsSurfaceAlt,
    focusedBorderColor = VitaminsTeal,
    unfocusedBorderColor = Color.Transparent,
    focusedTextColor = VitaminsInk,
    unfocusedTextColor = VitaminsInk,
    cursorColor = VitaminsTeal
)

@Composable
private fun VitaminsMessageBanner(
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
        fontFamily = VitaminsManrope,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        color = contentColor
    )
}

@Composable
private fun VitaminsDialogTitle(text: String) {
    Text(
        text = text,
        fontFamily = VitaminsManrope,
        fontWeight = FontWeight.ExtraBold,
        color = VitaminsInk
    )
}

@Composable
private fun VitaminsDialogBody(text: String) {
    Text(
        text = text,
        fontFamily = VitaminsManrope,
        fontWeight = FontWeight.Medium,
        color = VitaminsMuted,
        lineHeight = 21.sp
    )
}

@Composable
private fun VitaminsDialogButtonText(
    text: String,
    color: Color
) {
    Text(
        text = text,
        fontFamily = VitaminsManrope,
        fontWeight = FontWeight.ExtraBold,
        color = color
    )
}

@Composable
private fun VitaminsSkeleton() {
    val alpha = vitaminsSkeletonAlpha()

    Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
        VitaminsSkeletonPanel(alpha = alpha, height = 146.dp, expanded = false)
        VitaminsSkeletonPanel(alpha = alpha, height = 214.dp, expanded = true)
    }
}

@Composable
private fun VitaminsSkeletonPanel(
    alpha: Float,
    height: androidx.compose.ui.unit.Dp,
    expanded: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(26.dp))
            .background(VitaminsSurface)
            .border(1.dp, VitaminsLine.copy(alpha = 0.70f), RoundedCornerShape(26.dp))
            .padding(18.dp)
    ) {
        VitaminsSkeletonLine(0.34f, 18.dp, alpha)
        Spacer(modifier = Modifier.height(18.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            VitaminsSkeletonBox(
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                alpha = alpha
            )

            VitaminsSkeletonBox(
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                alpha = alpha
            )
        }

        if (expanded) {
            Spacer(modifier = Modifier.height(14.dp))
            VitaminsSkeletonBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                alpha = alpha
            )
        }
    }
}

@Composable
private fun vitaminsSkeletonAlpha(): Float {
    val transition = rememberInfiniteTransition(label = "vitaminsSkeleton")
    val alpha by transition.animateFloat(
        initialValue = 0.32f,
        targetValue = 0.72f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "vitaminsSkeletonAlpha"
    )
    return alpha
}

@Composable
private fun VitaminsSkeletonLine(
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
private fun VitaminsSkeletonBox(
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
private fun VitaminsBottomNavBar(
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
        VitaminsBottomNavItem(Icons.Outlined.Home, "Dashboard", false, onDashboardClick)
        VitaminsBottomNavItem(Icons.AutoMirrored.Outlined.List, "Tasks", false, onTasksClick)
        VitaminsBottomNavItem(Icons.Outlined.Edit, "Farm Management", true, onFarmClick)
        VitaminsBottomNavItem(Icons.Outlined.AccountCircle, "Profile", false, {})
    }
}

@Composable
private fun VitaminsBottomNavItem(
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
            fontFamily = VitaminsManrope,
            fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.SemiBold,
            fontSize = 9.sp,
            color = if (selected) Color.White else Color(0xFFCFE8D2),
            textAlign = TextAlign.Center,
            maxLines = 1,
            lineHeight = 10.sp
        )
    }
}