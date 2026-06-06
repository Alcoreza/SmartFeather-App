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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.ClipboardList
import com.composables.icons.lucide.House
import com.composables.icons.lucide.LayoutDashboard
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.UserRound
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.width

private val PersonnelManrope = FontFamily(
    Font(R.font.manrope_extralight, FontWeight.ExtraLight),
    Font(R.font.manrope_light, FontWeight.Light),
    Font(R.font.manrope_regular, FontWeight.Normal),
    Font(R.font.manrope_medium, FontWeight.Medium),
    Font(R.font.manrope_semibold, FontWeight.SemiBold),
    Font(R.font.manrope_bold, FontWeight.Bold),
    Font(R.font.manrope_extrabold, FontWeight.ExtraBold),
    Font(R.font.manrope_variablefont_wght, FontWeight.Black)
)

private val PersonnelBackground = Color(0xFFF6F3EC)
private val PersonnelSurface = Color(0xFFFFFCF7)
private val PersonnelSurfaceAlt = Color(0xFFF3EFE7)
private val PersonnelAutoField = Color(0xFFE8E3DA)
private val PersonnelInk = Color(0xFF121A14)
private val PersonnelMuted = Color(0xFF677168)
private val PersonnelLine = Color(0xFFD8D0C3)
private val PersonnelGreen = Color(0xFF1F7A3A)
private val PersonnelDeepGreen = Color(0xFF062717)
private val PersonnelForest = Color(0xFF103C28)
private val PersonnelTeal = Color(0xFF2E7D6B)
private val PersonnelBlue = Color(0xFF3F6F88)

@Composable
fun PersonnelLogsScreen(
    employeeId: Int,
    lockedTaskId: Int? = null,
    lockedHouseId: Int? = null,
    lockedPenId: Int? = null,
    lockedHouseLabel: String = "",
    lockedPenLabel: String = "",
    onBiosecuritySubmitted: () -> Unit = {},
    onBackToBiosecurity: () -> Unit,
    onNavigateToDashboard: () -> Unit,
    onNavigateToTasks: () -> Unit
) {
    val personnelService = remember { PersonnelLogsBackendService() }
    val coroutineScope = rememberCoroutineScope()

    var date by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("") }

    var houses by remember { mutableStateOf<List<PersonnelHouseOption>>(emptyList()) }
    var selectedHouse by remember { mutableStateOf<PersonnelHouseOption?>(null) }
    var houseExpanded by remember { mutableStateOf(false) }

    var footBath by remember { mutableStateOf(false) }
    var bootsChanged by remember { mutableStateOf(false) }
    var protectiveClothing by remember { mutableStateOf(false) }

    var personnelEntryLogId by remember { mutableStateOf<Long?>(null) }
    var isContextLoading by remember { mutableStateOf(true) }
    var isLoading by remember { mutableStateOf(false) }
    var contentVisible by remember { mutableStateOf(false) }

    var showDialog by remember { mutableStateOf(false) }
    var dialogTitle by remember { mutableStateOf("") }
    var dialogMessage by remember { mutableStateOf("") }
    var dialogAction by remember { mutableStateOf<(() -> Unit)?>(null) }

    val isTaskLocked = lockedTaskId != null
    val assignedHouseText = lockedHouseLabel.ifBlank { lockedHouseId?.let { "House $it" }.orEmpty() }
    val assignedPenText = lockedPenLabel.ifBlank { lockedPenId?.let { "Pen $it" }.orEmpty() }

    fun showModal(title: String, message: String, action: (() -> Unit)? = null) {
        dialogTitle = title
        dialogMessage = message
        dialogAction = action
        showDialog = true
    }

    LaunchedEffect(employeeId, lockedHouseId, lockedTaskId) {
        isContextLoading = true
        contentVisible = false

        personnelService.getContext(employeeId)
            .onSuccess { context ->
                date = context.date
                time = context.time
                name = context.name
                role = context.role
                houses = context.houses
                selectedHouse = if (lockedHouseId != null) {
                    context.houses.firstOrNull { it.id == lockedHouseId.toLong() }
                        ?: PersonnelHouseOption(
                            id = lockedHouseId.toLong(),
                            houseNumber = assignedHouseText.ifBlank { "House $lockedHouseId" }
                        )
                } else {
                    null
                }
                personnelEntryLogId = context.personnelEntryLogId
            }
            .onFailure {
                date = ""
                time = ""
                name = ""
                role = ""
                houses = emptyList()
                selectedHouse = null
                personnelEntryLogId = null
            }

        isContextLoading = false
        delay(120)
        contentVisible = true
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = {
                showDialog = false
                dialogAction = null
            },
            containerColor = PersonnelSurface,
            shape = RoundedCornerShape(28.dp),
            title = {
                PersonnelDialogTitle(dialogTitle)
            },
            text = {
                PersonnelDialogBody(dialogMessage)
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val action = dialogAction
                        showDialog = false
                        dialogAction = null
                        action?.invoke()
                    }
                ) {
                    PersonnelDialogButtonText("OK", PersonnelGreen)
                }
            }
        )
    }

    Scaffold(
        containerColor = PersonnelBackground,
        bottomBar = {
            PersonnelBottomNavBar(
                onDashboardClick = onNavigateToDashboard,
                onTasksClick = onNavigateToTasks
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFFFBF8F1), PersonnelBackground, Color(0xFFEDE7DA))
                    )
                )
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 18.dp, vertical = 18.dp)
            ) {
                PersonnelHero(
                    onBackToBiosecurity = onBackToBiosecurity
                )

                Spacer(modifier = Modifier.height(18.dp))

                if (isContextLoading) {
                    PersonnelSkeleton()
                    Spacer(modifier = Modifier.height(20.dp))
                    return@Column
                }

                AnimatedVisibility(
                    visible = contentVisible,
                    enter = fadeIn(animationSpec = tween(420)) + slideInVertically(
                        animationSpec = tween(420, easing = FastOutSlowInEasing),
                        initialOffsetY = { it / 12 }
                    )
                ) {
                    Column {
                        PersonnelSectionPanel {
                            PersonnelSectionHeader(
                                title = "Personnel Details",
                                accentColor = PersonnelTeal
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Column(modifier = Modifier.weight(1f)) {
                                    PersonnelLabel("Date")
                                    Spacer(modifier = Modifier.height(8.dp))
                                    PersonnelReadOnlyField(date.ifBlank { "-" })
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    PersonnelLabel("Time")
                                    Spacer(modifier = Modifier.height(8.dp))
                                    PersonnelReadOnlyField(time.ifBlank { "-" })
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            PersonnelLabel("Name")
                            Spacer(modifier = Modifier.height(8.dp))
                            PersonnelReadOnlyField(name.ifBlank { "-" })

                            Spacer(modifier = Modifier.height(14.dp))

                            PersonnelLabel("Role")
                            Spacer(modifier = Modifier.height(8.dp))
                            PersonnelReadOnlyField(role.ifBlank { "-" })
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        PersonnelSectionPanel {
                            PersonnelSectionHeader(
                                title = "Assigned Area",
                                accentColor = PersonnelBlue
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            PersonnelLabel("House")
                            Spacer(modifier = Modifier.height(8.dp))

                            if (isTaskLocked) {
                                PersonnelReadOnlyField(assignedHouseText.ifBlank { "-" })

                                Spacer(modifier = Modifier.height(14.dp))

                                PersonnelLabel("Pen")
                                Spacer(modifier = Modifier.height(8.dp))
                                PersonnelReadOnlyField(assignedPenText.ifBlank { "-" })
                            } else {
                                PersonnelDropdownField(
                                    value = selectedHouse?.houseNumber.orEmpty(),
                                    placeholder = "Select house",
                                    options = houses.map { it.houseNumber },
                                    expanded = houseExpanded,
                                    onExpandedChange = { houseExpanded = it },
                                    onValueSelected = { selectedValue ->
                                        selectedHouse = houses.firstOrNull { it.houseNumber == selectedValue }
                                        houseExpanded = false
                                    }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        PersonnelSectionPanel {
                            PersonnelSectionHeader(
                                title = "Biosecurity Checklist",
                                accentColor = PersonnelGreen
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            PersonnelChecklistItem(
                                label = "Foot Bath",
                                checked = footBath,
                                accentColor = PersonnelGreen,
                                onCheckedChange = { footBath = it }
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            PersonnelChecklistItem(
                                label = "Boots Changed",
                                checked = bootsChanged,
                                accentColor = PersonnelTeal,
                                onCheckedChange = { bootsChanged = it }
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            PersonnelChecklistItem(
                                label = "Protective Clothing",
                                checked = protectiveClothing,
                                accentColor = PersonnelBlue,
                                onCheckedChange = { protectiveClothing = it }
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Button(
                                onClick = {
                                    val currentEntryLogId = personnelEntryLogId
                                    val currentHouseId = lockedHouseId?.toLong() ?: selectedHouse?.id
                                    val currentPenId = lockedPenId?.toLong()

                                    if (currentEntryLogId == null) {
                                        showModal(
                                            title = "Missing Context",
                                            message = "Missing personnel entry log context."
                                        )
                                        return@Button
                                    }

                                    if (currentHouseId == null) {
                                        showModal(
                                            title = "House Required",
                                            message = "Please select the house you will go to."
                                        )
                                        return@Button
                                    }

                                    if (isTaskLocked && currentPenId == null) {
                                        showModal(
                                            title = "Missing Assigned Pen",
                                            message = "This task does not have a valid assigned pen."
                                        )
                                        return@Button
                                    }

                                    val uncheckedItems = mutableListOf<String>()
                                    if (!footBath) uncheckedItems.add("Foot Bath")
                                    if (!bootsChanged) uncheckedItems.add("Boots Changed")
                                    if (!protectiveClothing) uncheckedItems.add("Protective Clothing")

                                    if (uncheckedItems.isNotEmpty()) {
                                        showModal(
                                            title = "Incomplete Biosecurity",
                                            message = "Please complete: ${uncheckedItems.joinToString(", ")}."
                                        )
                                        return@Button
                                    }

                                    coroutineScope.launch {
                                        isLoading = true

                                        personnelService.submit(
                                            employeeId = employeeId,
                                            personnelEntryLogId = currentEntryLogId,
                                            taskId = lockedTaskId,
                                            houseId = currentHouseId,
                                            penId = currentPenId,
                                            footBath = footBath,
                                            bootsChanged = bootsChanged,
                                            protectiveClothing = protectiveClothing
                                        ).onSuccess {
                                            if (isTaskLocked) {
                                                showModal(
                                                    title = "Biosecurity Submitted",
                                                    message = "You can now continue with the assigned task.",
                                                    action = onBiosecuritySubmitted
                                                )
                                            } else {
                                                showModal(
                                                    title = "Submitted",
                                                    message = "Personnel biosecurity log submitted."
                                                )
                                            }
                                        }.onFailure {
                                            showModal(
                                                title = "Submission Failed",
                                                message = it.message ?: "Failed to submit personnel biosecurity log."
                                            )
                                        }

                                        isLoading = false
                                    }
                                },
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = PersonnelGreen,
                                    disabledContainerColor = Color(0xFF94A99A)
                                ),
                                modifier = Modifier.height(48.dp),
                                enabled = !isLoading
                            ) {
                                Text(
                                    text = if (isLoading) "Submitting..." else "Submit",
                                    color = Color.White,
                                    fontFamily = PersonnelManrope,
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
private fun PersonnelHero(
    onBackToBiosecurity: () -> Unit
) {
    val transition = rememberInfiniteTransition(label = "personnelHeroMotion")

    val pulse by transition.animateFloat(
        initialValue = 0.08f,
        targetValue = 0.16f,
        animationSpec = infiniteRepeatable(
            animation = tween(3600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "personnelPulse"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(PersonnelDeepGreen, PersonnelForest, PersonnelTeal)
                )
            )
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .offset(x = 18.dp)
                .size(82.dp)
                .clip(CircleShape)
                .border(1.dp, Color.White.copy(alpha = pulse + 0.08f), CircleShape)
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(38.dp)
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
                    modifier = Modifier.size(19.dp)
                )
            }

            Spacer(modifier = Modifier.size(12.dp))

            Text(
                text = "Personnel Logs",
                fontFamily = PersonnelManrope,
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
private fun PersonnelSectionPanel(
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .background(PersonnelSurface)
            .border(1.dp, PersonnelLine.copy(alpha = 0.70f), RoundedCornerShape(26.dp))
            .padding(horizontal = 18.dp, vertical = 18.dp),
        content = content
    )
}

@Composable
private fun PersonnelSectionHeader(
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
            fontFamily = PersonnelManrope,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 18.sp,
            color = PersonnelInk
        )
    }
}

@Composable
private fun PersonnelChecklistItem(
    label: String,
    checked: Boolean,
    accentColor: Color,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(if (checked) accentColor.copy(alpha = 0.10f) else PersonnelSurfaceAlt)
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 14.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(if (checked) accentColor else Color.Transparent)
                .border(
                    2.dp,
                    if (checked) accentColor else PersonnelLine,
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (checked) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                )
            }
        }

        Spacer(modifier = Modifier.size(12.dp))

        Text(
            text = label,
            fontFamily = PersonnelManrope,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 14.sp,
            color = PersonnelInk
        )
    }
}

@Composable
private fun PersonnelLabel(text: String) {
    Text(
        text = text,
        fontSize = 13.sp,
        fontFamily = PersonnelManrope,
        fontWeight = FontWeight.ExtraBold,
        color = PersonnelInk
    )
}

@Composable
private fun PersonnelReadOnlyField(value: String) {
    OutlinedTextField(
        value = value,
        onValueChange = {},
        readOnly = true,
        singleLine = true,
        textStyle = TextStyle(
            fontFamily = PersonnelManrope,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            color = PersonnelMuted
        ),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = PersonnelAutoField,
            unfocusedContainerColor = PersonnelAutoField,
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent,
            focusedTextColor = PersonnelMuted,
            unfocusedTextColor = PersonnelMuted,
            cursorColor = PersonnelTeal
        )
    )
}

@Composable
private fun PersonnelDropdownField(
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
                .background(PersonnelSurfaceAlt)
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
                    fontFamily = PersonnelManrope,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = if (value.isBlank()) PersonnelMuted else PersonnelInk,
                    modifier = Modifier.weight(1f)
                )

                Icon(
                    imageVector = Icons.Filled.KeyboardArrowDown,
                    contentDescription = placeholder,
                    tint = PersonnelTeal,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) },
            modifier = Modifier.background(PersonnelSurface)
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = option,
                            fontFamily = PersonnelManrope,
                            fontWeight = FontWeight.SemiBold,
                            color = PersonnelInk
                        )
                    },
                    onClick = { onValueSelected(option) }
                )
            }
        }
    }
}

@Composable
private fun PersonnelDialogTitle(text: String) {
    Text(
        text = text,
        fontFamily = PersonnelManrope,
        fontWeight = FontWeight.ExtraBold,
        color = PersonnelInk
    )
}

@Composable
private fun PersonnelDialogBody(text: String) {
    Text(
        text = text,
        fontFamily = PersonnelManrope,
        fontWeight = FontWeight.Medium,
        color = PersonnelMuted,
        lineHeight = 21.sp
    )
}

@Composable
private fun PersonnelDialogButtonText(
    text: String,
    color: Color
) {
    Text(
        text = text,
        fontFamily = PersonnelManrope,
        fontWeight = FontWeight.ExtraBold,
        color = color
    )
}

@Composable
private fun PersonnelSkeleton() {
    val alpha = personnelSkeletonAlpha()

    Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
        PersonnelSkeletonPanel(alpha = alpha, height = 258.dp, expanded = true)
        PersonnelSkeletonPanel(alpha = alpha, height = 146.dp, expanded = false)
        PersonnelSkeletonPanel(alpha = alpha, height = 202.dp, expanded = true)
    }
}

@Composable
private fun PersonnelSkeletonPanel(
    alpha: Float,
    height: androidx.compose.ui.unit.Dp,
    expanded: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(26.dp))
            .background(PersonnelSurface)
            .border(1.dp, PersonnelLine.copy(alpha = 0.70f), RoundedCornerShape(26.dp))
            .padding(horizontal = 18.dp, vertical = 14.dp)
    ) {
        PersonnelSkeletonLine(0.38f, 18.dp, alpha)
        Spacer(modifier = Modifier.height(18.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            PersonnelSkeletonBox(
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                alpha = alpha
            )

            PersonnelSkeletonBox(
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                alpha = alpha
            )
        }

        if (expanded) {
            Spacer(modifier = Modifier.height(14.dp))
            PersonnelSkeletonBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                alpha = alpha
            )
            Spacer(modifier = Modifier.height(14.dp))
            PersonnelSkeletonBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                alpha = alpha
            )
        }
    }
}

@Composable
private fun personnelSkeletonAlpha(): Float {
    val transition = rememberInfiniteTransition(label = "personnelSkeleton")
    val alpha by transition.animateFloat(
        initialValue = 0.32f,
        targetValue = 0.72f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "personnelSkeletonAlpha"
    )
    return alpha
}

@Composable
private fun PersonnelSkeletonLine(
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
private fun PersonnelSkeletonBox(
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
private fun PersonnelBottomNavBar(
    onDashboardClick: () -> Unit,
    onTasksClick: () -> Unit
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
        PersonnelBottomNavItem(Lucide.LayoutDashboard, "Dashboard", false, onDashboardClick)
        PersonnelBottomNavItem(Lucide.ClipboardList, "Tasks", true, onTasksClick)
        PersonnelBottomNavItem(Lucide.UserRound, "Profile", false, {})
    }
}

@Composable
private fun PersonnelBottomNavItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    Column(
        modifier = Modifier
            .width(92.dp)
            .height(52.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() }
            .padding(horizontal = 4.dp, vertical = 5.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
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
            fontFamily = PersonnelManrope,
            fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.SemiBold,
            fontSize = 9.sp,
            color = if (selected) Color.White else Color(0xFFCFE8D2),
            textAlign = TextAlign.Center,
            maxLines = 1,
            lineHeight = 10.sp
        )
    }
}