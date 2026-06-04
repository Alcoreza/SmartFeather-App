package com.example.smartfeather

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.ColumnScope
import com.composables.icons.lucide.ClipboardList
import com.composables.icons.lucide.House
import com.composables.icons.lucide.LayoutDashboard
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.UserRound

data class PendingTaskDetailUiState(
    val id: Int,
    val title: String,
    val description: String,
    val timeAssigned: String,
    val finishBy: String,
    val priorityLabel: String,
    val priority: TaskPriority,
    val houseId: Int? = null,
    val penNumber: Int? = null,
    val houseLabel: String = "",
    val penLabel: String = ""
)

private val DetailPoppins = FontFamily(
    Font(R.font.manrope_regular, FontWeight.Normal),
    Font(R.font.manrope_medium, FontWeight.Medium),
    Font(R.font.manrope_semibold, FontWeight.SemiBold),
    Font(R.font.manrope_bold, FontWeight.Bold),
    Font(R.font.manrope_extrabold, FontWeight.ExtraBold)
)

private val DetailBackground = Color(0xFFF6F3EC)
private val DetailSurface = Color(0xFFFFFCF7)
private val DetailSurfaceMuted = Color(0xFFEDE7DA)
private val DetailInk = Color(0xFF121A14)
private val DetailMuted = Color(0xFF677168)
private val DetailLine = Color(0xFFD8D0C3)
private val DetailGreen = Color(0xFF1F7A3A)
private val DetailDeepGreen = Color(0xFF062717)
private val DetailGreenTwo = Color(0xFF155C2D)
private val PendingAccent = Color(0xFFE79A43)

private fun priorityChipColor(priority: TaskPriority): Color {
    return when (priority) {
        TaskPriority.LOW -> Color(0xFF2F7D46)
        TaskPriority.MID -> Color(0xFFE28622)
        TaskPriority.HIGH -> Color(0xFFC62B2B)
    }
}

@Composable
fun PendingTaskDetailScreen(
    task: PendingTaskDetailUiState,
    onBackClick: () -> Unit = {},
    onNavigateToDashboard: () -> Unit = {},
    onNavigateToTasks: () -> Unit = {},
    onGoToBiosecurity: () -> Unit = {},
    onSubmit: suspend (notes: String, photoUri: Uri?) -> Result<Unit> = { _, _ -> Result.success(Unit) }
) {
    var notes by remember { mutableStateOf("") }
    var selectedPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var submitError by remember { mutableStateOf<String?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }
    var showBiosecurityDialog by remember { mutableStateOf(false) }
    var biosecurityMessage by remember { mutableStateOf("") }
    var contentVisible by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(task.id) {
        contentVisible = false
        delay(120)
        contentVisible = true
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        selectedPhotoUri = uri
    }

    if (showBiosecurityDialog) {
        AlertDialog(
            onDismissRequest = { showBiosecurityDialog = false },
            containerColor = DetailSurface,
            title = {
                Text(
                    text = "Biosecurity Required",
                    fontFamily = DetailPoppins,
                    fontWeight = FontWeight.ExtraBold,
                    color = DetailInk
                )
            },
            text = {
                Text(
                    text = biosecurityMessage,
                    fontFamily = DetailPoppins,
                    fontWeight = FontWeight.Medium,
                    color = DetailMuted,
                    lineHeight = 21.sp
                )
            },
            dismissButton = {
                TextButton(onClick = { showBiosecurityDialog = false }) {
                    Text("Back", fontFamily = DetailPoppins, color = DetailMuted)
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showBiosecurityDialog = false
                        onGoToBiosecurity()
                    }
                ) {
                    Text(
                        text = "Go to Personnel Logs",
                        fontFamily = DetailPoppins,
                        fontWeight = FontWeight.ExtraBold,
                        color = DetailGreen
                    )
                }
            }
        )
    }

    Scaffold(
        containerColor = DetailBackground,
        bottomBar = {
            TaskDetailBottomNavBar(
                onDashboardClick = onNavigateToDashboard,
                onTasksClick = onNavigateToTasks
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFFFBF8F1), DetailBackground, Color(0xFFEDE7DA))
                    )
                )
                .pointerInput(Unit) {
                    detectTapGestures {
                        focusManager.clearFocus()
                    }
                }
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .imePadding()
                    .padding(horizontal = 18.dp, vertical = 18.dp)
            ) {
                DetailTopBar(onBackClick = onBackClick)

                Spacer(modifier = Modifier.height(18.dp))

                if (!contentVisible) {
                    PendingDetailSkeleton()
                    Spacer(modifier = Modifier.height(20.dp))
                    return@Column
                }

                AnimatedVisibility(
                    visible = contentVisible,
                    enter = fadeIn(animationSpec = tween(420)) + slideInVertically(
                        animationSpec = tween(420, easing = FastOutSlowInEasing),
                        initialOffsetY = { it / 18 }
                    )
                ) {
                    Column {
                        WorkOrderHeader(task = task)

                        Spacer(modifier = Modifier.height(22.dp))

                        WorkBriefSection(task = task)

                        Spacer(modifier = Modifier.height(24.dp))

                        SubmissionWorkspace(
                            selectedPhotoUri = selectedPhotoUri,
                            notes = notes,
                            onNotesChange = { notes = it },
                            onPhotoClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(
                                        ActivityResultContracts.PickVisualMedia.ImageOnly
                                    )
                                )
                            }
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        submitError?.let {
                            ErrorBanner(message = it)
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        Button(
                            onClick = {
                                focusManager.clearFocus()
                                submitError = null

                                coroutineScope.launch {
                                    isSubmitting = true
                                    onSubmit(notes, selectedPhotoUri)
                                        .onFailure {
                                            val message = it.message ?: "Failed to submit task."

                                            if (
                                                message.contains("personnel biosecurity", ignoreCase = true) ||
                                                message.contains("scan IN", ignoreCase = true) ||
                                                message.contains("different house", ignoreCase = true) ||
                                                message.contains("correct house", ignoreCase = true)
                                            ) {
                                                biosecurityMessage = message
                                                showBiosecurityDialog = true
                                            } else {
                                                submitError = message
                                            }
                                        }
                                    isSubmitting = false
                                }
                            },
                            shape = RoundedCornerShape(18.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = DetailGreen,
                                disabledContainerColor = Color(0xFF94A99A)
                            ),
                            enabled = !isSubmitting,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                        ) {
                            Text(
                                text = if (isSubmitting) "Submitting..." else "Submit Task",
                                fontFamily = DetailPoppins,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp,
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailTopBar(
    onBackClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.82f))
                .border(1.dp, DetailLine, CircleShape)
                .clickable { onBackClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = DetailInk,
                modifier = Modifier.size(21.dp)
            )
        }

        Text(
            text = "Work Order",
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center,
            fontFamily = DetailPoppins,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 20.sp,
            color = DetailInk
        )

        Spacer(modifier = Modifier.size(44.dp))
    }
}

@Composable
private fun WorkOrderHeader(task: PendingTaskDetailUiState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(30.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(DetailDeepGreen, Color(0xFF0E4025), DetailGreenTwo)
                )
            )
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatusLabel(
                text = "Pending task",
                color = PendingAccent,
                modifier = Modifier.weight(1f)
            )

            PriorityBadge(
                label = task.priorityLabel,
                color = priorityChipColor(task.priority)
            )
        }

        Spacer(modifier = Modifier.height(22.dp))

        Text(
            text = task.title,
            fontFamily = DetailPoppins,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 28.sp,
            color = Color.White,
            lineHeight = 32.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TimeBlock(
                label = "Assigned",
                value = task.timeAssigned,
                modifier = Modifier.weight(1f)
            )

            TimeBlock(
                label = "Finish by",
                value = task.finishBy,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun StatusLabel(
    text: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(9.dp)
                .clip(CircleShape)
                .background(color)
        )

        Spacer(modifier = Modifier.size(8.dp))

        Text(
            text = text,
            fontFamily = DetailPoppins,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 13.sp,
            color = Color.White
        )
    }
}

@Composable
private fun PriorityBadge(
    label: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.End) {
        Text(
            text = "Priority",
            fontFamily = DetailPoppins,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp,
            color = Color.White.copy(alpha = 0.58f)
        )

        Text(
            text = label,
            fontFamily = DetailPoppins,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 14.sp,
            color = color
        )
    }
}

@Composable
private fun TimeBlock(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White.copy(alpha = 0.10f))
            .border(1.dp, Color.White.copy(alpha = 0.13f), RoundedCornerShape(18.dp))
            .padding(horizontal = 13.dp, vertical = 12.dp)
    ) {
        Text(
            text = label,
            fontFamily = DetailPoppins,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            color = Color.White.copy(alpha = 0.64f)
        )

        Spacer(modifier = Modifier.height(5.dp))

        Text(
            text = value.ifBlank { "-" },
            fontFamily = DetailPoppins,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 15.sp,
            color = Color.White,
            lineHeight = 19.sp
        )
    }
}

@Composable
private fun WorkBriefSection(task: PendingTaskDetailUiState) {
    SectionPanel {
        SectionHeaderRow(
            title = "Work brief",
            subtitle = "Task instructions",
            accentColor = DetailGreen
        )

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = task.description,
            fontFamily = DetailPoppins,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            lineHeight = 23.sp,
            color = Color(0xFF2E382F)
        )
    }
}

@Composable
private fun SubmissionWorkspace(
    selectedPhotoUri: Uri?,
    notes: String,
    onNotesChange: (String) -> Unit,
    onPhotoClick: () -> Unit
) {
    SectionPanel {
        SectionHeaderRow(
            title = "Submission",
            subtitle = "Attach proof and add notes before sending",
            accentColor = PendingAccent
        )

        Spacer(modifier = Modifier.height(16.dp))

        DetailDivider()

        Spacer(modifier = Modifier.height(16.dp))

        PhotoUploadArea(
            selectedPhotoUri = selectedPhotoUri,
            onClick = onPhotoClick
        )

        Spacer(modifier = Modifier.height(14.dp))

        NotesInput(
            notes = notes,
            onNotesChange = onNotesChange
        )
    }
}

@Composable
private fun SectionPanel(
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.94f),
                        DetailSurface.copy(alpha = 0.98f)
                    )
                )
            )
            .border(
                width = 1.dp,
                color = DetailLine.copy(alpha = 0.82f),
                shape = RoundedCornerShape(26.dp)
            )
            .padding(horizontal = 18.dp, vertical = 18.dp),
        content = content
    )
}

@Composable
private fun SectionHeaderRow(
    title: String,
    subtitle: String,
    accentColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .padding(top = 4.dp)
                .size(width = 4.dp, height = 38.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(accentColor)
        )

        Spacer(modifier = Modifier.size(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontFamily = DetailPoppins,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp,
                color = DetailInk
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = subtitle,
                fontFamily = DetailPoppins,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                color = DetailMuted,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
private fun DetailDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        DetailLine.copy(alpha = 0.92f),
                        Color.Transparent
                    )
                )
            )
    )
}

@Composable
private fun SectionLabel(
    title: String,
    subtitle: String
) {
    Column {
        Text(
            text = title,
            fontFamily = DetailPoppins,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 18.sp,
            color = DetailInk
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = subtitle,
            fontFamily = DetailPoppins,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            color = DetailMuted
        )
    }
}

@Composable
private fun PhotoUploadArea(
    selectedPhotoUri: Uri?,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(184.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(DetailSurface)
            .border(1.dp, DetailLine, RoundedCornerShape(24.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (selectedPhotoUri != null) {
            AsyncImage(
                model = selectedPhotoUri,
                contentDescription = "Selected proof photo",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEAF3EC)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = "Upload proof photo",
                        tint = DetailGreen,
                        modifier = Modifier.size(23.dp)
                    )
                }

                Spacer(modifier = Modifier.height(11.dp))

                Text(
                    text = "Upload proof of work",
                    fontFamily = DetailPoppins,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp,
                    color = DetailInk
                )

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = "Tap to choose a photo",
                    fontFamily = DetailPoppins,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    color = DetailMuted
                )
            }
        }
    }
}

@Composable
private fun NotesInput(
    notes: String,
    onNotesChange: (String) -> Unit
) {
    OutlinedTextField(
        value = notes,
        onValueChange = onNotesChange,
        modifier = Modifier
            .fillMaxWidth()
            .height(136.dp),
        shape = RoundedCornerShape(22.dp),
        placeholder = {
            Text(
                text = "Add notes here",
                fontFamily = DetailPoppins,
                color = DetailMuted
            )
        },
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Sentences
        ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = DetailSurface,
            unfocusedContainerColor = DetailSurface,
            focusedBorderColor = DetailGreen,
            unfocusedBorderColor = DetailLine,
            focusedTextColor = DetailInk,
            unfocusedTextColor = DetailInk,
            cursorColor = DetailGreen
        )
    )
}

@Composable
private fun ErrorBanner(message: String) {
    Text(
        text = message,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFFFECEA))
            .border(1.dp, Color(0xFFF2B8B5), RoundedCornerShape(16.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        fontFamily = DetailPoppins,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        color = Color(0xFFB3261E)
    )
}

@Composable
private fun PendingDetailSkeleton() {
    val alpha = skeletonAlpha()

    Column(verticalArrangement = Arrangement.spacedBy(22.dp)) {
        SkeletonBox(
            modifier = Modifier
                .fillMaxWidth()
                .height(238.dp),
            alpha = alpha,
            color = DetailDeepGreen,
            shape = RoundedCornerShape(30.dp)
        )

        Column {
            SkeletonLine(widthFraction = 0.34f, height = 18.dp, alpha = alpha)
            Spacer(modifier = Modifier.height(12.dp))
            SkeletonLine(widthFraction = 0.96f, height = 13.dp, alpha = alpha)
            Spacer(modifier = Modifier.height(8.dp))
            SkeletonLine(widthFraction = 0.88f, height = 13.dp, alpha = alpha)
            Spacer(modifier = Modifier.height(8.dp))
            SkeletonLine(widthFraction = 0.62f, height = 13.dp, alpha = alpha)
        }

        SkeletonBox(
            modifier = Modifier
                .fillMaxWidth()
                .height(184.dp),
            alpha = alpha,
            color = Color(0xFFD9D2C6),
            shape = RoundedCornerShape(24.dp)
        )

        SkeletonBox(
            modifier = Modifier
                .fillMaxWidth()
                .height(136.dp),
            alpha = alpha,
            color = Color(0xFFD9D2C6),
            shape = RoundedCornerShape(22.dp)
        )
    }
}

@Composable
private fun skeletonAlpha(): Float {
    val transition = rememberInfiniteTransition(label = "pendingDetailSkeleton")
    val alpha by transition.animateFloat(
        initialValue = 0.28f,
        targetValue = 0.62f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pendingDetailSkeletonAlpha"
    )
    return alpha
}

@Composable
private fun SkeletonLine(
    widthFraction: Float,
    height: androidx.compose.ui.unit.Dp,
    alpha: Float,
    color: Color = Color(0xFFCFC5B5)
) {
    Box(
        modifier = Modifier
            .fillMaxWidth(widthFraction)
            .height(height)
            .clip(RoundedCornerShape(999.dp))
            .background(color.copy(alpha = alpha))
    )
}

@Composable
private fun SkeletonBox(
    modifier: Modifier,
    alpha: Float,
    color: Color = Color(0xFFCFC5B5),
    shape: Shape = RoundedCornerShape(16.dp)
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(color.copy(alpha = alpha))
    )
}

@Composable
private fun TaskDetailBottomNavBar(
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
        DetailNavItem(Lucide.LayoutDashboard, "Dashboard", false, onDashboardClick)
        DetailNavItem(Lucide.ClipboardList, "Tasks", true, onTasksClick)
        DetailNavItem(Lucide.House, "Farm Management", false, {})
        DetailNavItem(Lucide.UserRound, "Profile", false, {})
    }
}

@Composable
private fun DetailNavItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 5.dp),
        horizontalAlignment = Alignment.CenterHorizontally
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
            fontFamily = DetailPoppins,
            fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Medium,
            fontSize = 10.sp,
            color = if (selected) Color.White else Color(0xFFCFE8D2),
            textAlign = TextAlign.Center,
            lineHeight = 11.sp
        )
    }
}