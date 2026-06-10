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
import java.util.Locale
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.offset
import com.composables.icons.lucide.ClipboardList
import com.composables.icons.lucide.House
import com.composables.icons.lucide.LayoutDashboard
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.UserRound

private val FeedsManrope = FontFamily(
    Font(R.font.manrope_extralight, FontWeight.ExtraLight),
    Font(R.font.manrope_light, FontWeight.Light),
    Font(R.font.manrope_regular, FontWeight.Normal),
    Font(R.font.manrope_medium, FontWeight.Medium),
    Font(R.font.manrope_semibold, FontWeight.SemiBold),
    Font(R.font.manrope_bold, FontWeight.Bold),
    Font(R.font.manrope_extrabold, FontWeight.ExtraBold),
    Font(R.font.manrope_variablefont_wght, FontWeight.Black)
)

private val FeedsBackground = Color(0xFFF6F3EC)
private val FeedsSurface = Color(0xFFFFFCF7)
private val FeedsSurfaceAlt = Color(0xFFF3EFE7)
private val FeedsAutoField = Color(0xFFE8E3DA)
private val FeedsInk = Color(0xFF121A14)
private val FeedsMuted = Color(0xFF677168)
private val FeedsLine = Color(0xFFD8D0C3)
private val FeedsGreen = Color(0xFF1F7A3A)
private val FeedsDeepGreen = Color(0xFF062717)
private val FeedsAmber = Color(0xFFD78A2B)
private val FeedsCopper = Color(0xFF9D6330)
private val FeedsDeepCopper = Color(0xFF4A2A16)
private val FeedsWheat = Color(0xFFE6B86E)
private val FeedsDanger = Color(0xFFC62828)


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

    var isContextLoading by remember { mutableStateOf(true) }
    var isLoading by remember { mutableStateOf(false) }
    var contentVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var showBlockedDialog by remember { mutableStateOf(false) }
    var blockedMessage by remember { mutableStateOf("") }

    var showRequiredDialog by remember { mutableStateOf(false) }
    var requiredDialogMessage by remember { mutableStateOf("") }
    var showSuccessDialog by remember { mutableStateOf(false) }

    val feederOptions = selectedPen?.feederOptions ?: emptyList()

    fun showRequired(message: String) {
        requiredDialogMessage = message
        showRequiredDialog = true
    }

    LaunchedEffect(employeeId) {
        isContextLoading = true
        contentVisible = false
        errorMessage = null

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

        isContextLoading = false
        delay(120)
        contentVisible = true
    }

    if (showBlockedDialog) {
        AlertDialog(
            onDismissRequest = {},
            containerColor = FeedsSurface,
            shape = RoundedCornerShape(28.dp),
            title = {
                FeedsDialogTitle("Biosecurity Required")
            },
            text = {
                FeedsDialogBody(blockedMessage)
            },
            dismissButton = {
                TextButton(onClick = onBackToFarm) {
                    FeedsDialogButtonText("Back", FeedsMuted)
                }
            },
            confirmButton = {
                TextButton(onClick = onGoToBiosecurity) {
                    FeedsDialogButtonText("Go to Biosecurity", FeedsGreen)
                }
            }
        )
    }

    if (showRequiredDialog) {
        AlertDialog(
            onDismissRequest = { showRequiredDialog = false },
            containerColor = FeedsSurface,
            shape = RoundedCornerShape(28.dp),
            title = {
                FeedsDialogTitle("Complete Required Fields")
            },
            text = {
                FeedsDialogBody(requiredDialogMessage)
            },
            confirmButton = {
                TextButton(onClick = { showRequiredDialog = false }) {
                    FeedsDialogButtonText("Got it", FeedsGreen)
                }
            }
        )
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false },
            containerColor = FeedsSurface,
            shape = RoundedCornerShape(28.dp),
            title = {
                FeedsDialogTitle("Submitted")
            },
            text = {
                FeedsDialogBody("Feeds refill submitted.")
            },
            confirmButton = {
                TextButton(onClick = { showSuccessDialog = false }) {
                    FeedsDialogButtonText("Done", FeedsGreen)
                }
            }
        )
    }

    Scaffold(
        containerColor = FeedsBackground,
        bottomBar = {
            FeedsBottomNavigationBar(
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
                        colors = listOf(Color(0xFFFBF8F1), FeedsBackground, Color(0xFFEDE7DA))
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
                FeedsHero(
                    openedDate = openedDate,
                    openedTime = openedTime,
                    onBackToFarm = onBackToFarm
                )

                Spacer(modifier = Modifier.height(18.dp))

                if (isContextLoading) {
                    FeedsSkeleton()
                    Spacer(modifier = Modifier.height(20.dp))
                    return@Column
                }

                errorMessage?.let {
                    FeedsMessageBanner(
                        text = it,
                        backgroundColor = Color(0xFFFFECEA),
                        contentColor = FeedsDanger
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
                        FeedsSectionPanel {
                            FeedsSectionHeader(
                                title = "Location",
                                accentColor = FeedsCopper
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Column(modifier = Modifier.weight(1f)) {
                                    FeedsLabel("House")
                                    Spacer(modifier = Modifier.height(8.dp))
                                    FeedsReadOnlyField(house.ifBlank { "-" })
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    FeedsLabel("Pen")
                                    Spacer(modifier = Modifier.height(8.dp))
                                    FeedsDropdownField(
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
                                            feederNumber = ""
                                            feederExpanded = false
                                            penExpanded = false
                                            errorMessage = null
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        FeedsSectionPanel {
                            FeedsSectionHeader(
                                title = "Feed Details",
                                accentColor = FeedsAmber
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            FeedsLabel("Type of Feed")
                            Spacer(modifier = Modifier.height(8.dp))
                            FeedsDropdownField(
                                value = feedType,
                                placeholder = "Select feed",
                                options = feedOptions.map { it.selectionKey },
                                expanded = feedExpanded,
                                onExpandedChange = { feedExpanded = it },
                                optionLabel = { option ->
                                    option.substringAfter("|")
                                },
                                onValueSelected = { selectedValue ->
                                    val selectedOption = feedOptions.firstOrNull {
                                        it.selectionKey == selectedValue
                                    }

                                    feedType = selectedOption?.itemName.orEmpty()
                                    selectedFeed = selectedOption
                                    feedExpanded = false
                                    errorMessage = null
                                }
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            FeedsLabel("Feeder Number")
                            Spacer(modifier = Modifier.height(8.dp))
                            FeedsDropdownField(
                                value = feederNumber,
                                placeholder = if (selectedPen == null) "Select pen first" else "Select feeder",
                                options = feederOptions,
                                expanded = feederExpanded,
                                onExpandedChange = {
                                    feederExpanded = it && selectedPen != null && feederOptions.isNotEmpty()
                                },
                                onValueSelected = { selectedValue ->
                                    feederNumber = selectedValue
                                    feederExpanded = false
                                    errorMessage = null
                                }
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            FeedsLabel("Kilograms Refilled")
                            Spacer(modifier = Modifier.height(8.dp))
                            FeedsInputField(
                                value = kilograms,
                                keyboardType = KeyboardType.Number,
                                scrollState = scrollState
                            ) { newValue ->
                                kilograms = newValue.filter { ch -> ch.isDigit() }
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
                                    val currentFeed = selectedFeed
                                    val feederValue = feederNumber.toIntOrNull()
                                    val kilogramsValue = kilograms.toIntOrNull()

                                    when {
                                        currentHouse == null || house.isBlank() -> {
                                            showRequired("House is required. Please complete biosecurity first so the assigned house can be loaded.")
                                            return@Button
                                        }

                                        currentPen == null || pen.isBlank() -> {
                                            showRequired("Please select a pen before submitting.")
                                            return@Button
                                        }

                                        currentFeed == null || feedType.isBlank() -> {
                                            showRequired("Please select a type of feed before submitting.")
                                            return@Button
                                        }

                                        feederValue == null -> {
                                            showRequired("Please select a feeder number before submitting.")
                                            return@Button
                                        }

                                        kilogramsValue == null || kilogramsValue <= 0 -> {
                                            showRequired("Please enter a valid kilograms value.")
                                            return@Button
                                        }
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
                                                showSuccessDialog = true
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
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = FeedsGreen,
                                    disabledContainerColor = Color(0xFF94A99A)
                                ),
                                modifier = Modifier.height(48.dp),
                                enabled = !isLoading && !showBlockedDialog
                            ) {
                                Text(
                                    text = if (isLoading) "Submitting..." else "Submit",
                                    color = Color.White,
                                    fontFamily = FeedsManrope,
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
private fun FeedsHero(
    openedDate: String,
    openedTime: String,
    onBackToFarm: () -> Unit
) {
    val transition = rememberInfiniteTransition(label = "feedsHeroMotion")

    val drift by transition.animateFloat(
        initialValue = 0f,
        targetValue = 34f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "feedsHeroDrift"
    )

    val pulse by transition.animateFloat(
        initialValue = 0.10f,
        targetValue = 0.22f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "feedsHeroPulse"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(30.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(FeedsDeepCopper, FeedsCopper, FeedsAmber)
                )
            )
            .padding(18.dp)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = (-8 - drift).dp, y = (4 + drift * 0.18f).dp)
                .size(132.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = pulse))
        )

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = (-96 + drift).dp, y = (-4 - drift * 0.12f).dp)
                .size(62.dp)
                .clip(CircleShape)
                .background(FeedsWheat.copy(alpha = pulse * 0.72f))
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
                    text = "Feeds Refill",
                    fontFamily = FeedsManrope,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 25.sp,
                    color = Color.White,
                    lineHeight = 29.sp,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FeedsHeroMetric(
                    label = "Date",
                    value = openedDate,
                    modifier = Modifier.weight(1f)
                )

                FeedsHeroMetric(
                    label = "Time",
                    value = openedTime,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun FeedsHeroMetric(
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
            fontFamily = FeedsManrope,
            fontWeight = FontWeight.SemiBold,
            fontSize = 10.sp,
            color = Color.White.copy(alpha = 0.70f)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = value,
            fontFamily = FeedsManrope,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 14.sp,
            color = Color.White
        )
    }
}

@Composable
private fun FeedsSectionPanel(
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .background(FeedsSurface)
            .border(1.dp, FeedsLine.copy(alpha = 0.70f), RoundedCornerShape(26.dp))
            .padding(horizontal = 18.dp, vertical = 18.dp),
        content = content
    )
}

@Composable
private fun FeedsSectionHeader(
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
            fontFamily = FeedsManrope,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 18.sp,
            color = FeedsInk
        )
    }
}

@Composable
private fun FeedsLabel(text: String) {
    Text(
        text = text,
        fontSize = 13.sp,
        fontFamily = FeedsManrope,
        fontWeight = FontWeight.ExtraBold,
        color = FeedsInk
    )
}

@Composable
private fun FeedsInputField(
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
            fontFamily = FeedsManrope,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            color = FeedsInk
        ),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape = RoundedCornerShape(18.dp),
        colors = feedsFieldColors()
    )
}

@Composable
private fun FeedsReadOnlyField(value: String) {
    OutlinedTextField(
        value = value,
        onValueChange = {},
        readOnly = true,
        singleLine = true,
        textStyle = TextStyle(
            fontFamily = FeedsManrope,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            color = FeedsMuted
        ),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = FeedsAutoField,
            unfocusedContainerColor = FeedsAutoField,
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent,
            focusedTextColor = FeedsMuted,
            unfocusedTextColor = FeedsMuted,
            cursorColor = FeedsGreen
        )
    )
}

@Composable
private fun FeedsDropdownField(
    value: String,
    placeholder: String,
    options: List<String>,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    optionLabel: (String) -> String = { it },
    onValueSelected: (String) -> Unit
) {
    Box {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(FeedsSurfaceAlt)
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
                    fontFamily = FeedsManrope,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = if (value.isBlank()) FeedsMuted else FeedsInk,
                    modifier = Modifier.weight(1f)
                )

                Icon(
                    imageVector = Icons.Filled.KeyboardArrowDown,
                    contentDescription = placeholder,
                    tint = FeedsCopper,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) },
            modifier = Modifier.background(FeedsSurface)
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = optionLabel(option),
                            fontFamily = FeedsManrope,
                            fontWeight = FontWeight.SemiBold,
                            color = FeedsInk
                        )
                    },
                    onClick = { onValueSelected(option) }
                )
            }
        }
    }
}

@Composable
private fun feedsFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = FeedsSurfaceAlt,
    unfocusedContainerColor = FeedsSurfaceAlt,
    focusedBorderColor = FeedsCopper,
    unfocusedBorderColor = Color.Transparent,
    focusedTextColor = FeedsInk,
    unfocusedTextColor = FeedsInk,
    cursorColor = FeedsCopper
)

@Composable
private fun FeedsMessageBanner(
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
        fontFamily = FeedsManrope,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        color = contentColor
    )
}

@Composable
private fun FeedsDialogTitle(text: String) {
    Text(
        text = text,
        fontFamily = FeedsManrope,
        fontWeight = FontWeight.ExtraBold,
        color = FeedsInk
    )
}

@Composable
private fun FeedsDialogBody(text: String) {
    Text(
        text = text,
        fontFamily = FeedsManrope,
        fontWeight = FontWeight.Medium,
        color = FeedsMuted,
        lineHeight = 21.sp
    )
}

@Composable
private fun FeedsDialogButtonText(
    text: String,
    color: Color
) {
    Text(
        text = text,
        fontFamily = FeedsManrope,
        fontWeight = FontWeight.ExtraBold,
        color = color
    )
}

@Composable
private fun FeedsSkeleton() {
    val alpha = feedsSkeletonAlpha()

    Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
        FeedsSkeletonPanel(alpha = alpha, height = 146.dp)
        FeedsSkeletonPanel(alpha = alpha, height = 284.dp)
    }
}

@Composable
private fun FeedsSkeletonPanel(
    alpha: Float,
    height: androidx.compose.ui.unit.Dp
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(26.dp))
            .background(FeedsSurface)
            .border(1.dp, FeedsLine.copy(alpha = 0.70f), RoundedCornerShape(26.dp))
            .padding(18.dp)
    ) {
        FeedsSkeletonLine(0.34f, 18.dp, alpha)
        Spacer(modifier = Modifier.height(18.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            FeedsSkeletonBox(
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                alpha = alpha
            )

            FeedsSkeletonBox(
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                alpha = alpha
            )
        }

        if (height > 180.dp) {
            Spacer(modifier = Modifier.height(14.dp))
            FeedsSkeletonBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                alpha = alpha
            )
            Spacer(modifier = Modifier.height(14.dp))
            FeedsSkeletonBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                alpha = alpha
            )
        }
    }
}

@Composable
private fun feedsSkeletonAlpha(): Float {
    val transition = rememberInfiniteTransition(label = "feedsSkeleton")
    val alpha by transition.animateFloat(
        initialValue = 0.32f,
        targetValue = 0.72f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "feedsSkeletonAlpha"
    )
    return alpha
}

@Composable
private fun FeedsSkeletonLine(
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
private fun FeedsSkeletonBox(
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
private fun FeedsBottomNavigationBar(
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
        FeedsBottomNavItem(Lucide.LayoutDashboard, "Dashboard", false, onDashboardClick)
        FeedsBottomNavItem(Lucide.ClipboardList, "Tasks", false, onTasksClick)
        FeedsBottomNavItem(Lucide.House, "Farm Management", true, onFarmClick)
        FeedsBottomNavItem(Lucide.UserRound, "Profile", false, {})
    }
}

@Composable
private fun FeedsBottomNavItem(
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
            fontFamily = FeedsManrope,
            fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.SemiBold,
            fontSize = 9.sp,
            color = if (selected) Color.White else Color(0xFFCFE8D2),
            textAlign = TextAlign.Center,
            maxLines = 1,
            lineHeight = 10.sp
        )
    }
}