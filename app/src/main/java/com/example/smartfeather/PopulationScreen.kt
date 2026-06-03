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

private val PopulationManrope = FontFamily(
    Font(R.font.manrope_extralight, FontWeight.ExtraLight),
    Font(R.font.manrope_light, FontWeight.Light),
    Font(R.font.manrope_regular, FontWeight.Normal),
    Font(R.font.manrope_medium, FontWeight.Medium),
    Font(R.font.manrope_semibold, FontWeight.SemiBold),
    Font(R.font.manrope_bold, FontWeight.Bold),
    Font(R.font.manrope_extrabold, FontWeight.ExtraBold),
    Font(R.font.manrope_variablefont_wght, FontWeight.Black)
)

private val PopulationBackground = Color(0xFFF6F3EC)
private val PopulationSurface = Color(0xFFFFFCF7)
private val PopulationSurfaceAlt = Color(0xFFF3EFE7)
private val PopulationAutoField = Color(0xFFE8E3DA)
private val PopulationInk = Color(0xFF121A14)
private val PopulationMuted = Color(0xFF677168)
private val PopulationLine = Color(0xFFD8D0C3)
private val PopulationGreen = Color(0xFF1F7A3A)
private val PopulationDeepGreen = Color(0xFF062717)
private val PopulationAccent = Color(0xFFB54A3C)
private val PopulationAccentDeep = Color(0xFF5A1F19)
private val PopulationAmber = Color(0xFFD78A2B)
private val PopulationDanger = Color(0xFFC62828)

@Composable
fun PopulationScreen(
    employeeId: Int,
    onBackToFarm: () -> Unit,
    onNavigateToDashboard: () -> Unit,
    onNavigateToTasks: () -> Unit,
    onGoToBiosecurity: () -> Unit
) {
    val populationService = remember { PopulationBackendService() }
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
    var eggs by remember { mutableStateOf("") }
    var mortality by remember { mutableStateOf("") }

    var selectedHouse by remember { mutableStateOf<PopulationHouseOption?>(null) }
    var penOptions by remember { mutableStateOf<List<String>>(emptyList()) }

    var penExpanded by remember { mutableStateOf(false) }

    var isContextLoading by remember { mutableStateOf(true) }
    var isLoading by remember { mutableStateOf(false) }
    var contentVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    var showBlockedDialog by remember { mutableStateOf(false) }
    var blockedMessage by remember { mutableStateOf("") }

    LaunchedEffect(employeeId) {
        isContextLoading = true
        contentVisible = false
        errorMessage = null
        successMessage = null

        populationService.getPopulationContext(employeeId)
            .onSuccess { context ->
                if (!context.accessAllowed) {
                    blockedMessage = context.message
                        ?: "Please complete personnel biosecurity entry before accessing this page."
                    showBlockedDialog = true
                    house = ""
                    selectedHouse = null
                    penOptions = emptyList()
                } else {
                    selectedHouse = context.house
                    house = context.house?.houseNumber.orEmpty()
                    penOptions = context.penOptions
                }
            }
            .onFailure {
                errorMessage = it.message ?: "Failed to load access context."
            }

        isContextLoading = false
        delay(120)
        contentVisible = true
    }

    if (showBlockedDialog) {
        AlertDialog(
            onDismissRequest = {},
            containerColor = PopulationSurface,
            shape = RoundedCornerShape(28.dp),
            title = {
                Text(
                    text = "Biosecurity Required",
                    fontFamily = PopulationManrope,
                    fontWeight = FontWeight.ExtraBold,
                    color = PopulationInk
                )
            },
            text = {
                Text(
                    text = blockedMessage,
                    fontFamily = PopulationManrope,
                    fontWeight = FontWeight.Medium,
                    color = PopulationMuted,
                    lineHeight = 21.sp
                )
            },
            dismissButton = {
                TextButton(onClick = onBackToFarm) {
                    Text(
                        text = "Back",
                        fontFamily = PopulationManrope,
                        fontWeight = FontWeight.ExtraBold,
                        color = PopulationMuted
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = onGoToBiosecurity) {
                    Text(
                        text = "Go to Biosecurity",
                        fontFamily = PopulationManrope,
                        fontWeight = FontWeight.ExtraBold,
                        color = PopulationGreen
                    )
                }
            }
        )
    }

    Scaffold(
        containerColor = PopulationBackground,
        bottomBar = {
            PopulationBottomNavBar(
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
                        colors = listOf(Color(0xFFFBF8F1), PopulationBackground, Color(0xFFEDE7DA))
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
                    .verticalScroll(rememberScrollState())
                    .imePadding()
                    .padding(horizontal = 18.dp, vertical = 18.dp)
            ) {
                PopulationHero(
                    openedDate = openedDate,
                    openedTime = openedTime,
                    onBackToFarm = onBackToFarm
                )

                Spacer(modifier = Modifier.height(18.dp))

                if (isContextLoading) {
                    PopulationSkeleton()
                    Spacer(modifier = Modifier.height(20.dp))
                    return@Column
                }

                errorMessage?.let {
                    PopulationMessageBanner(
                        text = it,
                        backgroundColor = Color(0xFFFFECEA),
                        contentColor = PopulationDanger
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                }

                successMessage?.let {
                    PopulationMessageBanner(
                        text = it,
                        backgroundColor = Color(0xFFEAF3EC),
                        contentColor = PopulationGreen
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
                        PopulationSectionPanel {
                            PopulationSectionHeader(
                                title = "Location",
                                accentColor = PopulationAccent
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Column(modifier = Modifier.weight(1f)) {
                                    PopulationLabel("House")
                                    Spacer(modifier = Modifier.height(8.dp))
                                    PopulationReadOnlyField(house.ifBlank { "-" })
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    PopulationLabel("Pen")
                                    Spacer(modifier = Modifier.height(8.dp))
                                    PopulationDropdownField(
                                        value = pen,
                                        options = penOptions,
                                        expanded = penExpanded,
                                        onExpandedChange = {
                                            if (selectedHouse != null) {
                                                penExpanded = it
                                            }
                                        },
                                        onValueSelected = { selectedValue: String ->
                                            pen = selectedValue
                                            penExpanded = false
                                            errorMessage = null
                                            successMessage = null
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        PopulationSectionPanel {
                            PopulationSectionHeader(
                                title = "Entry",
                                accentColor = PopulationAmber
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            PopulationLabel("Eggs Hatched")
                            Spacer(modifier = Modifier.height(8.dp))
                            PopulationInputField(
                                value = eggs,
                                keyboardType = KeyboardType.Number
                            ) { newValue ->
                                eggs = newValue.filter { ch -> ch.isDigit() }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            PopulationLabel("Mortalities")
                            Spacer(modifier = Modifier.height(8.dp))
                            PopulationInputField(
                                value = mortality,
                                keyboardType = KeyboardType.Number
                            ) { newValue ->
                                mortality = newValue.filter { ch -> ch.isDigit() }
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
                                    if (currentHouse == null) {
                                        errorMessage = "No house is assigned from your biosecurity entry."
                                        return@Button
                                    }
                                    if (pen.isBlank()) {
                                        errorMessage = "Please select a pen."
                                        return@Button
                                    }
                                    if (eggs.isBlank()) {
                                        errorMessage = "Please enter eggs hatched."
                                        return@Button
                                    }
                                    if (mortality.isBlank()) {
                                        errorMessage = "Please enter mortalities."
                                        return@Button
                                    }

                                    val eggsValue = eggs.toIntOrNull()
                                    val mortalityValue = mortality.toIntOrNull()

                                    if (eggsValue == null || mortalityValue == null) {
                                        errorMessage = "Eggs hatched and mortalities must be valid numbers."
                                        return@Button
                                    }

                                    coroutineScope.launch {
                                        isLoading = true
                                        populationService.submitPopulation(
                                            employeeId = employeeId,
                                            houseId = currentHouse.id,
                                            penNumber = pen,
                                            eggsHatched = eggsValue,
                                            mortality = mortalityValue,
                                            recordedAt = recordedAtValue
                                        ).onSuccess { success ->
                                            if (success) {
                                                successMessage = "Population data submitted successfully."
                                                pen = ""
                                                eggs = ""
                                                mortality = ""
                                            } else {
                                                errorMessage = "No matching pen record was updated."
                                            }
                                        }.onFailure {
                                            errorMessage = it.message ?: "Failed to submit population data."
                                        }
                                        isLoading = false
                                    }
                                },
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = PopulationGreen,
                                    disabledContainerColor = Color(0xFF94A99A)
                                ),
                                modifier = Modifier.height(48.dp),
                                enabled = !isLoading && !showBlockedDialog
                            ) {
                                Text(
                                    text = if (isLoading) "Submitting..." else "Submit",
                                    color = Color.White,
                                    fontFamily = PopulationManrope,
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
private fun PopulationHero(
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
                    colors = listOf(PopulationAccentDeep, PopulationAccent, Color(0xFFC76655))
                )
            )
            .padding(18.dp)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(118.dp)
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
                    text = "Population Data",
                    fontFamily = PopulationManrope,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 25.sp,
                    color = Color.White,
                    lineHeight = 29.sp,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                PopulationHeroMetric(
                    label = "Date",
                    value = openedDate,
                    modifier = Modifier.weight(1f)
                )

                PopulationHeroMetric(
                    label = "Time",
                    value = openedTime,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun PopulationHeroMetric(
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
            fontFamily = PopulationManrope,
            fontWeight = FontWeight.SemiBold,
            fontSize = 10.sp,
            color = Color.White.copy(alpha = 0.68f)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = value,
            fontFamily = PopulationManrope,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 14.sp,
            color = Color.White
        )
    }
}

@Composable
private fun PopulationSectionPanel(
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .background(PopulationSurface)
            .border(1.dp, PopulationLine.copy(alpha = 0.70f), RoundedCornerShape(26.dp))
            .padding(horizontal = 18.dp, vertical = 18.dp),
        content = content
    )
}

@Composable
private fun PopulationSectionHeader(
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
            fontFamily = PopulationManrope,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 18.sp,
            color = PopulationInk
        )
    }
}

@Composable
private fun PopulationLabel(text: String) {
    Text(
        text = text,
        fontSize = 13.sp,
        fontFamily = PopulationManrope,
        fontWeight = FontWeight.ExtraBold,
        color = PopulationInk
    )
}

@Composable
private fun PopulationInputField(
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
        shape = RoundedCornerShape(18.dp),
        colors = populationFieldColors()
    )
}

@Composable
private fun PopulationReadOnlyField(value: String) {
    OutlinedTextField(
        value = value,
        onValueChange = {},
        readOnly = true,
        enabled = false,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        shape = RoundedCornerShape(18.dp),
        colors = OutlinedTextFieldDefaults.colors(
            disabledContainerColor = PopulationAutoField,
            disabledBorderColor = Color.Transparent,
            disabledTextColor = PopulationMuted
        )
    )
}

@Composable
private fun PopulationDropdownField(
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
                .background(PopulationSurfaceAlt)
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
                    text = if (value.isEmpty()) "Select pen" else value,
                    fontFamily = PopulationManrope,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = if (value.isEmpty()) PopulationMuted else PopulationInk
                )

                Icon(
                    imageVector = Icons.Filled.KeyboardArrowDown,
                    contentDescription = "Select pen",
                    tint = PopulationGreen,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) },
            modifier = Modifier.background(PopulationSurface)
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = option,
                            fontFamily = PopulationManrope,
                            fontWeight = FontWeight.SemiBold,
                            color = PopulationInk
                        )
                    },
                    onClick = {
                        onValueSelected(option)
                    }
                )
            }
        }
    }
}

@Composable
private fun populationFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = PopulationSurfaceAlt,
    unfocusedContainerColor = PopulationSurfaceAlt,
    focusedBorderColor = PopulationGreen,
    unfocusedBorderColor = Color.Transparent,
    focusedTextColor = PopulationInk,
    unfocusedTextColor = PopulationInk,
    cursorColor = PopulationGreen
)

@Composable
private fun PopulationMessageBanner(
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
        fontFamily = PopulationManrope,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        color = contentColor
    )
}

@Composable
private fun PopulationSkeleton() {
    val alpha = populationSkeletonAlpha()

    Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
        PopulationSkeletonPanel(alpha = alpha, height = 132.dp)
        PopulationSkeletonPanel(alpha = alpha, height = 226.dp)
    }
}

@Composable
private fun PopulationSkeletonPanel(
    alpha: Float,
    height: androidx.compose.ui.unit.Dp
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(26.dp))
            .background(PopulationSurface)
            .border(1.dp, PopulationLine.copy(alpha = 0.70f), RoundedCornerShape(26.dp))
            .padding(18.dp)
    ) {
        PopulationSkeletonLine(0.28f, 18.dp, alpha)
        Spacer(modifier = Modifier.height(18.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            PopulationSkeletonBox(
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                alpha = alpha
            )
            PopulationSkeletonBox(
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                alpha = alpha
            )
        }
    }
}

@Composable
private fun populationSkeletonAlpha(): Float {
    val transition = rememberInfiniteTransition(label = "populationSkeleton")
    val alpha by transition.animateFloat(
        initialValue = 0.32f,
        targetValue = 0.72f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "populationSkeletonAlpha"
    )
    return alpha
}

@Composable
private fun PopulationSkeletonLine(
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
private fun PopulationSkeletonBox(
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
fun PopulationBottomNavBar(
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
        PopulationBottomNavItem(Icons.Outlined.Home, "Dashboard", false, onDashboardClick)
        PopulationBottomNavItem(Icons.AutoMirrored.Outlined.List, "Tasks", false, onTasksClick)
        PopulationBottomNavItem(Icons.Outlined.Edit, "Farm Management", true, onFarmClick)
        PopulationBottomNavItem(Icons.Outlined.AccountCircle, "Profile", false, {})
    }
}

@Composable
fun PopulationBottomNavItem(
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
            fontFamily = PopulationManrope,
            fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.SemiBold,
            fontSize = 9.sp,
            color = if (selected) Color.White else Color(0xFFCFE8D2),
            textAlign = TextAlign.Center,
            maxLines = 1,
            lineHeight = 10.sp
        )
    }
}