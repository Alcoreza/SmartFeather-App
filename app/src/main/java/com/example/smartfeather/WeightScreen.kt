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
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.composables.icons.lucide.ClipboardList
import com.composables.icons.lucide.House
import com.composables.icons.lucide.LayoutDashboard
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.UserRound

private val WeightManrope = FontFamily(
    Font(R.font.manrope_extralight, FontWeight.ExtraLight),
    Font(R.font.manrope_light, FontWeight.Light),
    Font(R.font.manrope_regular, FontWeight.Normal),
    Font(R.font.manrope_medium, FontWeight.Medium),
    Font(R.font.manrope_semibold, FontWeight.SemiBold),
    Font(R.font.manrope_bold, FontWeight.Bold),
    Font(R.font.manrope_extrabold, FontWeight.ExtraBold),
    Font(R.font.manrope_variablefont_wght, FontWeight.Black)
)

private val WeightBackground = Color(0xFFF6F3EC)
private val WeightSurface = Color(0xFFFFFCF7)
private val WeightSurfaceAlt = Color(0xFFF3EFE7)
private val WeightAutoField = Color(0xFFE8E3DA)
private val WeightInk = Color(0xFF121A14)
private val WeightMuted = Color(0xFF677168)
private val WeightLine = Color(0xFFD8D0C3)
private val WeightGreen = Color(0xFF1F7A3A)
private val WeightBlue = Color(0xFF3F6F88)
private val WeightDeepBlue = Color(0xFF142C38)
private val WeightSteel = Color(0xFF49656F)
private val WeightAmber = Color(0xFFD78A2B)
private val WeightDanger = Color(0xFFC62828)

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
    var showSuccessDialog by remember { mutableStateOf(false) }

    var selectedHouse by remember { mutableStateOf<WeightHouseOption?>(null) }
    var selectedPen by remember { mutableStateOf<WeightPenOption?>(null) }
    var pens by remember { mutableStateOf<List<WeightPenOption>>(emptyList()) }

    var penExpanded by remember { mutableStateOf(false) }

    var isContextLoading by remember { mutableStateOf(true) }
    var isLoading by remember { mutableStateOf(false) }
    var contentVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    var showBlockedDialog by remember { mutableStateOf(false) }
    var blockedMessage by remember { mutableStateOf("") }

    var showRequiredDialog by remember { mutableStateOf(false) }
    var requiredDialogMessage by remember { mutableStateOf("") }

    fun showRequired(message: String) {
        requiredDialogMessage = message
        showRequiredDialog = true
    }

    LaunchedEffect(employeeId) {
        isContextLoading = true
        contentVisible = false
        errorMessage = null
        successMessage = null

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

        isContextLoading = false
        delay(120)
        contentVisible = true
    }

    if (showBlockedDialog) {
        AlertDialog(
            onDismissRequest = {},
            containerColor = WeightSurface,
            shape = RoundedCornerShape(28.dp),
            title = {
                DialogTitle("Biosecurity Required")
            },
            text = {
                DialogBody(blockedMessage)
            },
            dismissButton = {
                TextButton(onClick = onBackToFarm) {
                    DialogButtonText("Back", WeightMuted)
                }
            },
            confirmButton = {
                TextButton(onClick = onGoToBiosecurity) {
                    DialogButtonText("Go to Biosecurity", WeightGreen)
                }
            }
        )
    }

    if (showRequiredDialog) {
        AlertDialog(
            onDismissRequest = { showRequiredDialog = false },
            containerColor = WeightSurface,
            shape = RoundedCornerShape(28.dp),
            title = {
                DialogTitle("Complete Required Fields")
            },
            text = {
                DialogBody(requiredDialogMessage)
            },
            confirmButton = {
                TextButton(onClick = { showRequiredDialog = false }) {
                    DialogButtonText("Got it", WeightGreen)
                }
            }
        )
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false },
            containerColor = WeightSurface,
            shape = RoundedCornerShape(28.dp),
            title = {
                DialogTitle("Submitted")
            },
            text = {
                DialogBody("Weight sampling submitted.")
            },
            confirmButton = {
                TextButton(onClick = { showSuccessDialog = false }) {
                    DialogButtonText("Done", WeightGreen)
                }
            }
        )
    }

    Scaffold(
        containerColor = WeightBackground,
        bottomBar = {
            WeightBottomNavBar(
                onDashboardClick = onNavigateToDashboard,
                onTasksClick = onNavigateToTasks,
                onFarmClick = onBackToFarm,
                onProfileClick = onNavigateToProfile
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFFFBF8F1), WeightBackground, Color(0xFFEDE7DA))
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
                WeightHero(
                    openedDate = openedDate,
                    openedTime = openedTime,
                    onBackToFarm = onBackToFarm
                )

                Spacer(modifier = Modifier.height(18.dp))

                if (isContextLoading) {
                    WeightSkeleton()
                    Spacer(modifier = Modifier.height(20.dp))
                    return@Column
                }

                errorMessage?.let {
                    WeightMessageBanner(
                        text = it,
                        backgroundColor = Color(0xFFFFECEA),
                        contentColor = WeightDanger
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
                        WeightSectionPanel {
                            WeightSectionHeader(
                                title = "Location",
                                accentColor = WeightBlue
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Column(modifier = Modifier.weight(1f)) {
                                    WeightLabel("House")
                                    Spacer(modifier = Modifier.height(8.dp))
                                    WeightReadOnlyField(house.ifBlank { "-" })
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    WeightLabel("Pen")
                                    Spacer(modifier = Modifier.height(8.dp))
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

                                                age = if (parsedStartedAt == null) {
                                                    ""
                                                } else {
                                                    ChronoUnit.DAYS.between(
                                                        parsedStartedAt.toLocalDate(),
                                                        openedAt.toLocalDate()
                                                    ).toInt().toString()
                                                }
                                            }
                                        }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            WeightLabel("Age")
                            Spacer(modifier = Modifier.height(8.dp))
                            WeightReadOnlyField(if (age.isBlank()) "-" else "$age days")
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        WeightSectionPanel {
                            WeightSectionHeader(
                                title = "Sampling Setup",
                                accentColor = WeightAmber
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            WeightLabel("Number of Flocks")
                            Spacer(modifier = Modifier.height(8.dp))
                            WeightInputField(
                                value = flocks,
                                keyboardType = KeyboardType.Number,
                                scrollState = scrollState
                            ) { newValue ->
                                val digitsOnly = newValue.filter { it.isDigit() }
                                flocks = digitsOnly
                                errorMessage = null
                                successMessage = null

                                val count = digitsOnly.toIntOrNull() ?: 0
                                weights = if (count > 0) {
                                    List(count) { index -> weights.getOrNull(index) ?: "" }
                                } else {
                                    emptyList()
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Column(modifier = Modifier.weight(1f)) {
                                    WeightLabel("Cases")
                                    Spacer(modifier = Modifier.height(8.dp))
                                    WeightInputField(
                                        value = flocksWithCases,
                                        keyboardType = KeyboardType.Number,
                                        scrollState = scrollState
                                    ) { newValue ->
                                        flocksWithCases = newValue.filter { it.isDigit() }
                                        errorMessage = null
                                        successMessage = null
                                    }
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    WeightLabel("Target")
                                    Spacer(modifier = Modifier.height(8.dp))
                                    WeightInputField(
                                        value = targetWeight,
                                        keyboardType = KeyboardType.Decimal,
                                        scrollState = scrollState
                                    ) { newValue ->
                                        targetWeight = decimalOnly(newValue)
                                        errorMessage = null
                                        successMessage = null
                                    }
                                }
                            }
                        }

                        if (weights.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(18.dp))

                            WeightSectionPanel {
                                WeightSectionHeader(
                                    title = "Weight Samples",
                                    accentColor = WeightGreen
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                weights.chunked(2).forEachIndexed { rowIndex, rowItems ->
                                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        rowItems.forEachIndexed { itemIndex, sampleValue ->
                                            val actualIndex = rowIndex * 2 + itemIndex

                                            Column(modifier = Modifier.weight(1f)) {
                                                WeightLabel("Flock ${actualIndex + 1}")
                                                Spacer(modifier = Modifier.height(8.dp))
                                                WeightInputField(
                                                    value = sampleValue,
                                                    keyboardType = KeyboardType.Decimal,
                                                    scrollState = scrollState
                                                ) { newValue ->
                                                    weights = weights.toMutableList().also {
                                                        it[actualIndex] = decimalOnly(newValue)
                                                    }
                                                    errorMessage = null
                                                    successMessage = null
                                                }
                                            }
                                        }

                                        if (rowItems.size == 1) {
                                            Spacer(modifier = Modifier.weight(1f))
                                        }
                                    }

                                    if (rowIndex != weights.chunked(2).lastIndex) {
                                        Spacer(modifier = Modifier.height(14.dp))
                                    }
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
                                    successMessage = null

                                    val currentHouse = selectedHouse
                                    val currentPen = selectedPen
                                    val ageValue = age.toIntOrNull()
                                    val flockCount = flocks.toIntOrNull()
                                    val flocksWithCasesValue = flocksWithCases.toIntOrNull()
                                    val targetWeightValue = targetWeight.toDoubleOrNull()
                                    val weightValues = weights.map { it.toDoubleOrNull() }

                                    when {
                                        currentHouse == null || house.isBlank() -> {
                                            showRequired("House is required. Please complete biosecurity first so the assigned house can be loaded.")
                                            return@Button
                                        }

                                        currentPen == null || pen.isBlank() -> {
                                            showRequired("Please select a pen before submitting.")
                                            return@Button
                                        }

                                        ageValue == null -> {
                                            showRequired("Age is required. Please select a pen with an active batch.")
                                            return@Button
                                        }

                                        flockCount == null || flockCount <= 0 -> {
                                            showRequired("Please enter the number of flocks.")
                                            return@Button
                                        }

                                        flocksWithCases.isBlank() || flocksWithCasesValue == null -> {
                                            showRequired("Please enter the number of flocks with cases. Use 0 if there are none.")
                                            return@Button
                                        }

                                        flocksWithCasesValue > flockCount -> {
                                            showRequired("Flocks with cases cannot be greater than the number of flocks.")
                                            return@Button
                                        }

                                        targetWeight.isBlank() || targetWeightValue == null || targetWeightValue <= 0.0 -> {
                                            showRequired("Please enter a valid target weight.")
                                            return@Button
                                        }

                                        weights.size != flockCount || weights.any { it.isBlank() } -> {
                                            showRequired("Please enter a weight sample for every flock.")
                                            return@Button
                                        }

                                        weightValues.any { it == null || it <= 0.0 } -> {
                                            showRequired("Please enter valid weight values for all flocks.")
                                            return@Button
                                        }
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
                                                successMessage = null
                                                showSuccessDialog = true
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
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = WeightGreen,
                                    disabledContainerColor = Color(0xFF94A99A)
                                ),
                                modifier = Modifier.height(48.dp),
                                enabled = !isLoading && !showBlockedDialog
                            ) {
                                Text(
                                    text = if (isLoading) "Submitting..." else "Submit",
                                    color = Color.White,
                                    fontFamily = WeightManrope,
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

private fun decimalOnly(value: String): String {
    return buildString {
        var dotUsed = false
        value.forEach { ch ->
            if (ch.isDigit()) append(ch)
            else if (ch == '.' && !dotUsed) {
                append(ch)
                dotUsed = true
            }
        }
    }
}

@Composable
private fun WeightHero(
    openedDate: String,
    openedTime: String,
    onBackToFarm: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(30.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(WeightDeepBlue, WeightBlue, WeightSteel)
                )
            )
            .padding(18.dp)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(128.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.10f))
        )

        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.14f))
                        .border(1.dp, Color.White.copy(alpha = 0.14f), CircleShape)
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
                    text = "Weight Sampling",
                    fontFamily = WeightManrope,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 25.sp,
                    color = Color.White,
                    lineHeight = 29.sp,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                WeightHeroMetric(
                    label = "Date",
                    value = openedDate,
                    modifier = Modifier.weight(1f)
                )

                WeightHeroMetric(
                    label = "Time",
                    value = openedTime,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun WeightHeroMetric(
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
            fontFamily = WeightManrope,
            fontWeight = FontWeight.SemiBold,
            fontSize = 10.sp,
            color = Color.White.copy(alpha = 0.68f)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = value,
            fontFamily = WeightManrope,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 14.sp,
            color = Color.White
        )
    }
}

@Composable
private fun WeightSectionPanel(
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .background(WeightSurface)
            .border(1.dp, WeightLine.copy(alpha = 0.70f), RoundedCornerShape(26.dp))
            .padding(horizontal = 18.dp, vertical = 18.dp),
        content = content
    )
}

@Composable
private fun WeightSectionHeader(
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
            fontFamily = WeightManrope,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 18.sp,
            color = WeightInk
        )
    }
}

@Composable
private fun WeightLabel(text: String) {
    Text(
        text = text,
        fontSize = 13.sp,
        fontFamily = WeightManrope,
        fontWeight = FontWeight.ExtraBold,
        color = WeightInk
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
        textStyle = TextStyle(
            fontFamily = WeightManrope,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            color = WeightInk
        ),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape = RoundedCornerShape(18.dp),
        colors = weightFieldColors()
    )
}

@Composable
private fun WeightReadOnlyField(value: String) {
    OutlinedTextField(
        value = value,
        onValueChange = {},
        readOnly = true,
        singleLine = true,
        textStyle = TextStyle(
            fontFamily = WeightManrope,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            color = WeightMuted
        ),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = WeightAutoField,
            unfocusedContainerColor = WeightAutoField,
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent,
            focusedTextColor = WeightMuted,
            unfocusedTextColor = WeightMuted,
            cursorColor = WeightGreen
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
                .clip(RoundedCornerShape(18.dp))
                .background(WeightSurfaceAlt)
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
                    text = if (value.isBlank()) "Select pen" else value,
                    fontFamily = WeightManrope,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = if (value.isBlank()) WeightMuted else WeightInk
                )

                Icon(
                    imageVector = Icons.Filled.KeyboardArrowDown,
                    contentDescription = "Select pen",
                    tint = WeightBlue,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) },
            modifier = Modifier.background(WeightSurface)
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = option,
                            fontFamily = WeightManrope,
                            fontWeight = FontWeight.SemiBold,
                            color = WeightInk
                        )
                    },
                    onClick = { onValueSelected(option) }
                )
            }
        }
    }
}

@Composable
private fun weightFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = WeightSurfaceAlt,
    unfocusedContainerColor = WeightSurfaceAlt,
    focusedBorderColor = WeightBlue,
    unfocusedBorderColor = Color.Transparent,
    focusedTextColor = WeightInk,
    unfocusedTextColor = WeightInk,
    cursorColor = WeightBlue
)

@Composable
private fun WeightMessageBanner(
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
        fontFamily = WeightManrope,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        color = contentColor
    )
}

@Composable
private fun DialogTitle(text: String) {
    Text(
        text = text,
        fontFamily = WeightManrope,
        fontWeight = FontWeight.ExtraBold,
        color = WeightInk
    )
}

@Composable
private fun DialogBody(text: String) {
    Text(
        text = text,
        fontFamily = WeightManrope,
        fontWeight = FontWeight.Medium,
        color = WeightMuted,
        lineHeight = 21.sp
    )
}

@Composable
private fun DialogButtonText(
    text: String,
    color: Color
) {
    Text(
        text = text,
        fontFamily = WeightManrope,
        fontWeight = FontWeight.ExtraBold,
        color = color
    )
}

@Composable
private fun WeightSkeleton() {
    val alpha = weightSkeletonAlpha()

    Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
        WeightSkeletonPanel(alpha = alpha, height = 202.dp)
        WeightSkeletonPanel(alpha = alpha, height = 226.dp)
        WeightSkeletonPanel(alpha = alpha, height = 146.dp)
    }
}

@Composable
private fun WeightSkeletonPanel(
    alpha: Float,
    height: androidx.compose.ui.unit.Dp
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(26.dp))
            .background(WeightSurface)
            .border(1.dp, WeightLine.copy(alpha = 0.70f), RoundedCornerShape(26.dp))
            .padding(18.dp)
    ) {
        WeightSkeletonLine(0.34f, 18.dp, alpha)
        Spacer(modifier = Modifier.height(18.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            WeightSkeletonBox(
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                alpha = alpha
            )
            WeightSkeletonBox(
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                alpha = alpha
            )
        }
        Spacer(modifier = Modifier.height(14.dp))
        WeightSkeletonBox(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            alpha = alpha
        )
    }
}

@Composable
private fun weightSkeletonAlpha(): Float {
    val transition = rememberInfiniteTransition(label = "weightSkeleton")
    val alpha by transition.animateFloat(
        initialValue = 0.32f,
        targetValue = 0.72f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "weightSkeletonAlpha"
    )
    return alpha
}

@Composable
private fun WeightSkeletonLine(
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
private fun WeightSkeletonBox(
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
private fun WeightBottomNavBar(
    onDashboardClick: () -> Unit,
    onTasksClick: () -> Unit,
    onFarmClick: () -> Unit,
    onProfileClick: () -> Unit
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
        WeightBottomNavItem(Lucide.LayoutDashboard, "Dashboard", false, onDashboardClick)
        WeightBottomNavItem(Lucide.ClipboardList, "Tasks", false, onTasksClick)
        WeightBottomNavItem(Lucide.House, "Farm Management", true, onFarmClick)
        WeightBottomNavItem(Lucide.UserRound, "Profile", false, onProfileClick)
    }
}

@Composable
private fun WeightBottomNavItem(
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
            fontFamily = WeightManrope,
            fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.SemiBold,
            fontSize = 9.sp,
            color = if (selected) Color.White else Color(0xFFCFE8D2),
            textAlign = TextAlign.Center,
            maxLines = 1,
            lineHeight = 10.sp
        )
    }
}