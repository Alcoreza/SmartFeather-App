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

private val DisinfectionManrope = FontFamily(
    Font(R.font.manrope_extralight, FontWeight.ExtraLight),
    Font(R.font.manrope_light, FontWeight.Light),
    Font(R.font.manrope_regular, FontWeight.Normal),
    Font(R.font.manrope_medium, FontWeight.Medium),
    Font(R.font.manrope_semibold, FontWeight.SemiBold),
    Font(R.font.manrope_bold, FontWeight.Bold),
    Font(R.font.manrope_extrabold, FontWeight.ExtraBold),
    Font(R.font.manrope_variablefont_wght, FontWeight.Black)
)

private val DisinfectionBackground = Color(0xFFF6F3EC)
private val DisinfectionSurface = Color(0xFFFFFCF7)
private val DisinfectionSurfaceAlt = Color(0xFFF3EFE7)
private val DisinfectionAutoField = Color(0xFFE8E3DA)
private val DisinfectionInk = Color(0xFF121A14)
private val DisinfectionMuted = Color(0xFF677168)
private val DisinfectionLine = Color(0xFFD8D0C3)
private val DisinfectionGreen = Color(0xFF1F7A3A)
private val DisinfectionDeepGreen = Color(0xFF062717)
private val DisinfectionForest = Color(0xFF123B2A)
private val DisinfectionTeal = Color(0xFF2E7D6B)
private val DisinfectionBlue = Color(0xFF3F6F88)
private val DisinfectionMist = Color(0xFFCFE8D2)
private val DisinfectionDanger = Color(0xFFC62828)

@Composable
fun DisinfectionScreen(
    employeeId: Int,
    onBackToBiosecurity: () -> Unit,
    onNavigateToDashboard: () -> Unit,
    onNavigateToTasks: () -> Unit,
    onGoToBiosecurity: () -> Unit
) {
    val disinfectionService = remember { DisinfectionBackendService() }
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
    var activity by remember { mutableStateOf("") }
    var disinfectantUsed by remember { mutableStateOf("") }

    var pens by remember { mutableStateOf<List<DisinfectionPenOption>>(emptyList()) }

    var selectedHouse by remember { mutableStateOf<DisinfectionHouseOption?>(null) }
    var selectedPen by remember { mutableStateOf<DisinfectionPenOption?>(null) }

    var penExpanded by remember { mutableStateOf(false) }

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

        disinfectionService.getDisinfectionContext(employeeId)
            .onSuccess { context ->
                if (!context.accessAllowed) {
                    blockedMessage = context.message
                        ?: "Please complete personnel biosecurity before accessing disinfection."
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
                blockedMessage = it.message ?: "Failed to load disinfection context."
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
            containerColor = DisinfectionSurface,
            shape = RoundedCornerShape(28.dp),
            title = { DisinfectionDialogTitle("Biosecurity Required") },
            text = { DisinfectionDialogBody(blockedMessage) },
            dismissButton = {
                TextButton(onClick = onBackToBiosecurity) {
                    DisinfectionDialogButtonText("Back", DisinfectionMuted)
                }
            },
            confirmButton = {
                TextButton(onClick = onGoToBiosecurity) {
                    DisinfectionDialogButtonText("Go to Biosecurity", DisinfectionGreen)
                }
            }
        )
    }

    if (showRequiredDialog) {
        AlertDialog(
            onDismissRequest = { showRequiredDialog = false },
            containerColor = DisinfectionSurface,
            shape = RoundedCornerShape(28.dp),
            title = { DisinfectionDialogTitle("Complete Required Fields") },
            text = { DisinfectionDialogBody(requiredDialogMessage) },
            confirmButton = {
                TextButton(onClick = { showRequiredDialog = false }) {
                    DisinfectionDialogButtonText("Got it", DisinfectionGreen)
                }
            }
        )
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false },
            containerColor = DisinfectionSurface,
            shape = RoundedCornerShape(28.dp),
            title = { DisinfectionDialogTitle("Submitted") },
            text = { DisinfectionDialogBody("Disinfection submitted.") },
            confirmButton = {
                TextButton(onClick = { showSuccessDialog = false }) {
                    DisinfectionDialogButtonText("Done", DisinfectionGreen)
                }
            }
        )
    }

    Scaffold(
        containerColor = DisinfectionBackground,
        bottomBar = {
            DisinfectionBottomNavBar(
                onDashboardClick = onNavigateToDashboard,
                onTasksClick = onNavigateToTasks,
                onFarmClick = onBackToBiosecurity
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFFFBF8F1), DisinfectionBackground, Color(0xFFEDE7DA))
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
                DisinfectionHero(
                    openedDate = openedDate,
                    openedTime = openedTime,
                    onBackToBiosecurity = onBackToBiosecurity
                )

                Spacer(modifier = Modifier.height(18.dp))

                if (isContextLoading) {
                    DisinfectionSkeleton()
                    Spacer(modifier = Modifier.height(20.dp))
                    return@Column
                }

                errorMessage?.let {
                    DisinfectionMessageBanner(
                        text = it,
                        backgroundColor = Color(0xFFFFECEA),
                        contentColor = DisinfectionDanger
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
                        DisinfectionSectionPanel {
                            DisinfectionSectionHeader(
                                title = "Location",
                                accentColor = DisinfectionTeal
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Column(modifier = Modifier.weight(1f)) {
                                    DisinfectionLabel("House")
                                    Spacer(modifier = Modifier.height(8.dp))
                                    DisinfectionReadOnlyField(house.ifBlank { "-" })
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    DisinfectionLabel("Pen")
                                    Spacer(modifier = Modifier.height(8.dp))
                                    DisinfectionDropdownField(
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

                        DisinfectionSectionPanel {
                            DisinfectionSectionHeader(
                                title = "Disinfection Details",
                                accentColor = DisinfectionBlue
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            DisinfectionLabel("Activity")
                            Spacer(modifier = Modifier.height(8.dp))
                            DisinfectionInputField(
                                value = activity,
                                scrollState = scrollState
                            ) {
                                activity = it
                                errorMessage = null
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            DisinfectionLabel("Disinfectant Used")
                            Spacer(modifier = Modifier.height(8.dp))
                            DisinfectionInputField(
                                value = disinfectantUsed,
                                scrollState = scrollState
                            ) {
                                disinfectantUsed = it
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

                                    when {
                                        currentHouse == null || house.isBlank() -> {
                                            showRequired("House is required. Please complete biosecurity first so the assigned house can be loaded.")
                                            return@Button
                                        }

                                        currentPen == null || pen.isBlank() -> {
                                            showRequired("Please select a pen before submitting.")
                                            return@Button
                                        }

                                        activity.isBlank() -> {
                                            showRequired("Please enter the disinfection activity.")
                                            return@Button
                                        }

                                        disinfectantUsed.isBlank() -> {
                                            showRequired("Please enter the disinfectant used.")
                                            return@Button
                                        }
                                    }

                                    coroutineScope.launch {
                                        isLoading = true

                                        disinfectionService.submitDisinfection(
                                            employeeId = employeeId,
                                            houseId = currentHouse.id,
                                            penId = currentPen.id,
                                            activity = activity.trim(),
                                            disinfectantUsed = disinfectantUsed.trim(),
                                            recordedDate = recordedDateValue,
                                            recordedTime = recordedTimeValue
                                        ).onSuccess { success ->
                                            if (success) {
                                                showSuccessDialog = true
                                                pen = ""
                                                activity = ""
                                                disinfectantUsed = ""
                                                selectedPen = null
                                            } else {
                                                errorMessage = "Failed to submit disinfection."
                                            }
                                        }.onFailure {
                                            errorMessage = it.message ?: "Failed to submit disinfection."
                                        }

                                        isLoading = false
                                    }
                                },
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = DisinfectionGreen,
                                    disabledContainerColor = Color(0xFF94A99A)
                                ),
                                modifier = Modifier.height(48.dp),
                                enabled = !isLoading && !showBlockedDialog
                            ) {
                                Text(
                                    text = if (isLoading) "Submitting..." else "Submit",
                                    color = Color.White,
                                    fontFamily = DisinfectionManrope,
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
private fun DisinfectionHero(
    openedDate: String,
    openedTime: String,
    onBackToBiosecurity: () -> Unit
) {
    val transition = rememberInfiniteTransition(label = "disinfectionHeroMotion")

    val mistRise by transition.animateFloat(
        initialValue = 34f,
        targetValue = -28f,
        animationSpec = infiniteRepeatable(
            animation = tween(5200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "disinfectionMistRise"
    )

    val mistDrift by transition.animateFloat(
        initialValue = -18f,
        targetValue = 26f,
        animationSpec = infiniteRepeatable(
            animation = tween(6100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "disinfectionMistDrift"
    )

    val mistAlpha by transition.animateFloat(
        initialValue = 0.08f,
        targetValue = 0.18f,
        animationSpec = infiniteRepeatable(
            animation = tween(4600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "disinfectionMistAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(30.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(DisinfectionDeepGreen, DisinfectionForest, DisinfectionTeal)
                )
            )
            .padding(18.dp)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = mistDrift.dp, y = mistRise.dp)
                .size(width = 172.dp, height = 34.dp)
                .graphicsLayer(rotationZ = -12f)
                .clip(RoundedCornerShape(999.dp))
                .background(Color.White.copy(alpha = mistAlpha))
        )

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = (-74 - mistDrift * 0.34f).dp, y = (12 + mistRise * 0.42f).dp)
                .size(width = 118.dp, height = 22.dp)
                .graphicsLayer(rotationZ = -12f)
                .clip(RoundedCornerShape(999.dp))
                .background(DisinfectionMist.copy(alpha = mistAlpha * 0.78f))
        )

        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = (-14 + mistDrift * 0.22f).dp, y = 2.dp)
                .size(62.dp)
                .clip(CircleShape)
                .border(1.dp, Color.White.copy(alpha = mistAlpha + 0.08f), CircleShape)
        )

        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.14f))
                        .border(1.dp, Color.White.copy(alpha = 0.16f), CircleShape)
                        .clickable { onBackToBiosecurity() },
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
                    text = "Disinfection",
                    fontFamily = DisinfectionManrope,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 25.sp,
                    color = Color.White,
                    lineHeight = 29.sp,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                DisinfectionHeroMetric(
                    label = "Date",
                    value = openedDate,
                    modifier = Modifier.weight(1f)
                )

                DisinfectionHeroMetric(
                    label = "Time",
                    value = openedTime,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun DisinfectionHeroMetric(
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
            fontFamily = DisinfectionManrope,
            fontWeight = FontWeight.SemiBold,
            fontSize = 10.sp,
            color = Color.White.copy(alpha = 0.70f)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = value,
            fontFamily = DisinfectionManrope,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 14.sp,
            color = Color.White
        )
    }
}

@Composable
private fun DisinfectionSectionPanel(
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .background(DisinfectionSurface)
            .border(1.dp, DisinfectionLine.copy(alpha = 0.70f), RoundedCornerShape(26.dp))
            .padding(horizontal = 18.dp, vertical = 18.dp),
        content = content
    )
}

@Composable
private fun DisinfectionSectionHeader(
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
            fontFamily = DisinfectionManrope,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 18.sp,
            color = DisinfectionInk
        )
    }
}

@Composable
private fun DisinfectionLabel(text: String) {
    Text(
        text = text,
        fontSize = 13.sp,
        fontFamily = DisinfectionManrope,
        fontWeight = FontWeight.ExtraBold,
        color = DisinfectionInk
    )
}

@Composable
private fun DisinfectionInputField(
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
            fontFamily = DisinfectionManrope,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            color = DisinfectionInk
        ),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape = RoundedCornerShape(18.dp),
        colors = disinfectionFieldColors()
    )
}

@Composable
private fun DisinfectionReadOnlyField(value: String) {
    OutlinedTextField(
        value = value,
        onValueChange = {},
        readOnly = true,
        singleLine = true,
        textStyle = TextStyle(
            fontFamily = DisinfectionManrope,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            color = DisinfectionMuted
        ),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = DisinfectionAutoField,
            unfocusedContainerColor = DisinfectionAutoField,
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent,
            focusedTextColor = DisinfectionMuted,
            unfocusedTextColor = DisinfectionMuted,
            cursorColor = DisinfectionTeal
        )
    )
}

@Composable
private fun DisinfectionDropdownField(
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
                .background(DisinfectionSurfaceAlt)
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
                    fontFamily = DisinfectionManrope,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = if (value.isBlank()) DisinfectionMuted else DisinfectionInk,
                    modifier = Modifier.weight(1f)
                )

                Icon(
                    imageVector = Icons.Filled.KeyboardArrowDown,
                    contentDescription = placeholder,
                    tint = DisinfectionTeal,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) },
            modifier = Modifier.background(DisinfectionSurface)
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = option,
                            fontFamily = DisinfectionManrope,
                            fontWeight = FontWeight.SemiBold,
                            color = DisinfectionInk
                        )
                    },
                    onClick = { onValueSelected(option) }
                )
            }
        }
    }
}

@Composable
private fun disinfectionFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = DisinfectionSurfaceAlt,
    unfocusedContainerColor = DisinfectionSurfaceAlt,
    focusedBorderColor = DisinfectionTeal,
    unfocusedBorderColor = Color.Transparent,
    focusedTextColor = DisinfectionInk,
    unfocusedTextColor = DisinfectionInk,
    cursorColor = DisinfectionTeal
)

@Composable
private fun DisinfectionMessageBanner(
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
        fontFamily = DisinfectionManrope,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        color = contentColor
    )
}

@Composable
private fun DisinfectionDialogTitle(text: String) {
    Text(
        text = text,
        fontFamily = DisinfectionManrope,
        fontWeight = FontWeight.ExtraBold,
        color = DisinfectionInk
    )
}

@Composable
private fun DisinfectionDialogBody(text: String) {
    Text(
        text = text,
        fontFamily = DisinfectionManrope,
        fontWeight = FontWeight.Medium,
        color = DisinfectionMuted,
        lineHeight = 21.sp
    )
}

@Composable
private fun DisinfectionDialogButtonText(
    text: String,
    color: Color
) {
    Text(
        text = text,
        fontFamily = DisinfectionManrope,
        fontWeight = FontWeight.ExtraBold,
        color = color
    )
}

@Composable
private fun DisinfectionSkeleton() {
    val alpha = disinfectionSkeletonAlpha()

    Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
        DisinfectionSkeletonPanel(alpha = alpha, height = 146.dp, expanded = false)
        DisinfectionSkeletonPanel(alpha = alpha, height = 214.dp, expanded = true)
    }
}

@Composable
private fun DisinfectionSkeletonPanel(
    alpha: Float,
    height: androidx.compose.ui.unit.Dp,
    expanded: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(26.dp))
            .background(DisinfectionSurface)
            .border(1.dp, DisinfectionLine.copy(alpha = 0.70f), RoundedCornerShape(26.dp))
            .padding(18.dp)
    ) {
        DisinfectionSkeletonLine(0.34f, 18.dp, alpha)
        Spacer(modifier = Modifier.height(18.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            DisinfectionSkeletonBox(
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                alpha = alpha
            )

            DisinfectionSkeletonBox(
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                alpha = alpha
            )
        }

        if (expanded) {
            Spacer(modifier = Modifier.height(14.dp))
            DisinfectionSkeletonBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                alpha = alpha
            )
        }
    }
}

@Composable
private fun disinfectionSkeletonAlpha(): Float {
    val transition = rememberInfiniteTransition(label = "disinfectionSkeleton")
    val alpha by transition.animateFloat(
        initialValue = 0.32f,
        targetValue = 0.72f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "disinfectionSkeletonAlpha"
    )
    return alpha
}

@Composable
private fun DisinfectionSkeletonLine(
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
private fun DisinfectionSkeletonBox(
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
private fun DisinfectionBottomNavBar(
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
        DisinfectionBottomNavItem(Lucide.LayoutDashboard, "Dashboard", false, onDashboardClick)
        DisinfectionBottomNavItem(Lucide.ClipboardList, "Tasks", false, onTasksClick)
        DisinfectionBottomNavItem(Lucide.House, "Farm Management", true, onFarmClick)
        DisinfectionBottomNavItem(Lucide.UserRound, "Profile", false, {})
    }
}

@Composable
private fun DisinfectionBottomNavItem(
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
            fontFamily = DisinfectionManrope,
            fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.SemiBold,
            fontSize = 9.sp,
            color = if (selected) Color.White else Color(0xFFCFE8D2),
            textAlign = TextAlign.Center,
            maxLines = 1,
            lineHeight = 10.sp
        )
    }
}