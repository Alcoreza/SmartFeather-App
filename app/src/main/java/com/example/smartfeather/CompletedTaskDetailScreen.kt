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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.composables.icons.lucide.ClipboardList
import com.composables.icons.lucide.House
import com.composables.icons.lucide.LayoutDashboard
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.UserRound
import kotlinx.coroutines.delay
import androidx.compose.foundation.layout.width

data class CompletedTaskDetailUiState(
    val id: Int,
    val title: String,
    val description: String,
    val timeAssigned: String,
    val statusLabel: String,
    val finishBy: String,
    val timeCompleted: String,
    val timeCompletedLabel: String = "Time Completed",
    val priorityLabel: String,
    val priority: TaskPriority,
    val notes: String,
    val hasPhoto: Boolean = true,
    val photoUrl: String? = null,
    val submittedFields: List<TaskSubmittedField> = emptyList()
)

private val CompletedPoppins = FontFamily(
    Font(R.font.manrope_regular, FontWeight.Normal),
    Font(R.font.manrope_medium, FontWeight.Medium),
    Font(R.font.manrope_semibold, FontWeight.SemiBold),
    Font(R.font.manrope_bold, FontWeight.Bold),
    Font(R.font.manrope_extrabold, FontWeight.ExtraBold)
)

private val CompletedBackground = Color(0xFFF6F3EC)
private val CompletedSurface = Color(0xFFFFFCF7)
private val CompletedInk = Color(0xFF121A14)
private val CompletedMuted = Color(0xFF677168)
private val CompletedLine = Color(0xFFD8D0C3)
private val CompletedGreen = Color(0xFF1F7A3A)
private val CompletedDeepGreen = Color(0xFF062717)
private val CompletedGreenTwo = Color(0xFF155C2D)
private val CompletedApproval = Color(0xFFE28622)

private fun completedPriorityChipColor(priority: TaskPriority): Color {
    return when (priority) {
        TaskPriority.LOW -> Color(0xFF2F7D46)
        TaskPriority.MID -> Color(0xFFE28622)
        TaskPriority.HIGH -> Color(0xFFC62B2B)
    }
}
@Composable
fun CompletedTaskDetailScreen(
    task: CompletedTaskDetailUiState,
    onBackClick: () -> Unit = {},
    onNavigateToDashboard: () -> Unit = {},
    onNavigateToTasks: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {}
) {
    var contentVisible by remember { mutableStateOf(false) }

    LaunchedEffect(task.id) {
        contentVisible = false
        delay(120)
        contentVisible = true
    }

    Scaffold(
        containerColor = CompletedBackground,
        bottomBar = {
            CompletedDetailBottomNavBar(
                onDashboardClick = onNavigateToDashboard,
                onTasksClick = onNavigateToTasks,
                onProfileClick = onNavigateToProfile
            )
        }
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFFFBF8F1), CompletedBackground, Color(0xFFEDE7DA))
                    )
                )
                .padding(innerPadding)
        ) {
            val isTablet = maxWidth >= 700.dp

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(
                        horizontal = if (isTablet) 28.dp else 18.dp,
                        vertical = 18.dp
                    )
            ) {
                CompletedTopHeader(
                    onBackClick = onBackClick,
                    title = "Task Details"
                )

                Spacer(modifier = Modifier.height(18.dp))

                if (!contentVisible) {
                    CompletedDetailSkeleton(isTablet = isTablet)
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
                        CompletedStatusSummaryCard(task = task)

                        Spacer(modifier = Modifier.height(22.dp))

                        CompletedBriefPanel(task = task, isTablet = isTablet)

                        if (task.submittedFields.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(22.dp))

                            SubmittedDataPanel(fields = task.submittedFields)
                        }

                        Spacer(modifier = Modifier.height(22.dp))

                        if (isTablet) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                PhotoPanel(
                                    hasPhoto = task.hasPhoto,
                                    photoUrl = task.photoUrl,
                                    modifier = Modifier.weight(1f)
                                )

                                NotesPanel(
                                    notes = task.notes,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        } else {
                            PhotoPanel(
                                hasPhoto = task.hasPhoto,
                                photoUrl = task.photoUrl,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(18.dp))

                            NotesPanel(
                                notes = task.notes,
                                modifier = Modifier.fillMaxWidth()
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
private fun CompletedTopHeader(
    onBackClick: () -> Unit,
    title: String
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
                .border(1.dp, CompletedLine, CircleShape)
                .clickable { onBackClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = CompletedInk,
                modifier = Modifier.size(21.dp)
            )
        }

        Text(
            text = title,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center,
            fontFamily = CompletedPoppins,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = CompletedInk
        )

        Spacer(modifier = Modifier.size(44.dp))
    }
}

@Composable
private fun CompletedStatusSummaryCard(task: CompletedTaskDetailUiState) {
    val statusColor = if (task.statusLabel == "For Approval") CompletedApproval else CompletedGreen

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(30.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(CompletedDeepGreen, Color(0xFF0E4025), CompletedGreenTwo)
                )
            )
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CompletedStatusLabel(
                text = task.statusLabel,
                color = statusColor,
                modifier = Modifier.weight(1f)
            )

            CompletedPriorityBadge(
                label = task.priorityLabel,
                color = completedPriorityChipColor(task.priority)
            )
        }

        Spacer(modifier = Modifier.height(22.dp))

        Text(
            text = task.title,
            fontFamily = CompletedPoppins,
            fontWeight = FontWeight.Bold,
            fontSize = 28.sp,
            color = Color.White,
            lineHeight = 32.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CompletedTimeBlock(
                label = "Assigned",
                value = task.timeAssigned,
                modifier = Modifier.weight(1f)
            )

            CompletedTimeBlock(
                label = "Finish by",
                value = task.finishBy,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        CompletedTimeBlock(
            label = task.timeCompletedLabel,
            value = task.timeCompleted,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun CompletedStatusLabel(
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
            fontFamily = CompletedPoppins,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = Color.White
        )
    }
}

@Composable
private fun CompletedPriorityBadge(
    label: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.End) {
        Text(
            text = "Priority",
            fontFamily = CompletedPoppins,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp,
            color = Color.White.copy(alpha = 0.58f)
        )

        Text(
            text = label,
            fontFamily = CompletedPoppins,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = color
        )
    }
}

@Composable
private fun CompletedTimeBlock(
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
            fontFamily = CompletedPoppins,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            color = Color.White.copy(alpha = 0.64f)
        )

        Spacer(modifier = Modifier.height(5.dp))

        Text(
            text = value.ifBlank { "-" },
            fontFamily = CompletedPoppins,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = Color.White,
            lineHeight = 19.sp
        )
    }
}

@Composable
private fun CompletedBriefPanel(
    task: CompletedTaskDetailUiState,
    isTablet: Boolean
) {
    CompletedSectionPanel {
        CompletedSectionHeaderRow(
            title = "Task Instructions",
            accentColor = CompletedGreen
        )

        Spacer(modifier = Modifier.height(14.dp))

        CompletedDivider()

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = task.description,
            fontFamily = CompletedPoppins,
            fontWeight = FontWeight.Normal,
            fontSize = if (isTablet) 15.sp else 14.sp,
            lineHeight = if (isTablet) 24.sp else 23.sp,
            color = Color(0xFF2E382F)
        )
    }
}

@Composable
private fun SubmittedDataPanel(
    fields: List<TaskSubmittedField>,
    modifier: Modifier = Modifier
) {
    CompletedSectionPanel(modifier = modifier) {
        CompletedSectionHeaderRow(
            title = "Submitted Data",
            accentColor = CompletedApproval
        )

        Spacer(modifier = Modifier.height(16.dp))

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            fields.forEach { field ->
                SubmittedDataRow(
                    label = field.label,
                    value = field.value
                )
            }
        }
    }
}

@Composable
private fun SubmittedDataRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFFF8F5EF))
            .border(1.dp, CompletedLine.copy(alpha = 0.70f), RoundedCornerShape(18.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            fontFamily = CompletedPoppins,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = CompletedInk,
            lineHeight = 17.sp
        )

        Text(
            text = value.ifBlank { "-" }.replace("\n", " "),
            fontFamily = CompletedPoppins,
            fontWeight = FontWeight.Medium,
            fontSize = 13.sp,
            color = CompletedMuted,
            textAlign = TextAlign.End,
            lineHeight = 17.sp
        )
    }
}

@Composable
private fun PhotoPanel(
    hasPhoto: Boolean,
    photoUrl: String?,
    modifier: Modifier = Modifier
) {
    CompletedSectionPanel(modifier = modifier) {
        CompletedSectionHeaderRow(
            title = "Proof Photo",
            accentColor = CompletedApproval
        )

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(190.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(Color(0xFFF3EFE7))
                .border(1.dp, CompletedLine, RoundedCornerShape(22.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (hasPhoto && !photoUrl.isNullOrBlank()) {
                AsyncImage(
                    model = photoUrl,
                    contentDescription = "Proof photo",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                CompletedEmptyState(
                    title = "No photo attached",
                    subtitle = "This task was submitted without visual proof."
                )
            }
        }
    }
}

@Composable
private fun NotesPanel(
    notes: String,
    modifier: Modifier = Modifier
) {
    CompletedSectionPanel(modifier = modifier) {
        CompletedSectionHeaderRow(
            title = "Worker Notes",
            accentColor = CompletedGreen
        )

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(Color(0xFFF8F5EF))
                .border(1.dp, CompletedLine.copy(alpha = 0.82f), RoundedCornerShape(22.dp))
                .padding(horizontal = 14.dp, vertical = 14.dp)
        ) {
            Text(
                text = notes.ifBlank { "No notes were added for this task." },
                fontFamily = CompletedPoppins,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                lineHeight = 21.sp,
                color = if (notes.isBlank()) CompletedMuted else Color(0xFF2E382F)
            )
        }
    }
}

@Composable
private fun CompletedSectionPanel(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.94f),
                        CompletedSurface.copy(alpha = 0.98f)
                    )
                )
            )
            .border(
                width = 1.dp,
                color = CompletedLine.copy(alpha = 0.82f),
                shape = RoundedCornerShape(26.dp)
            )
            .padding(horizontal = 18.dp, vertical = 18.dp),
        content = content
    )
}

@Composable
private fun CompletedSectionHeaderRow(
    title: String,
    accentColor: Color
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(width = 4.dp, height = 28.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(accentColor)
        )

        Spacer(modifier = Modifier.size(12.dp))

        Text(
            text = title,
            fontFamily = CompletedPoppins,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 17.sp,
            color = CompletedInk
        )
    }
}

@Composable
private fun CompletedDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        CompletedLine.copy(alpha = 0.92f),
                        Color.Transparent
                    )
                )
            )
    )
}

@Composable
private fun CompletedEmptyState(
    title: String,
    subtitle: String
) {
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
                contentDescription = null,
                tint = CompletedGreen,
                modifier = Modifier.size(23.dp)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = title,
            fontFamily = CompletedPoppins,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = CompletedInk
        )

        Spacer(modifier = Modifier.height(3.dp))

        Text(
            text = subtitle,
            fontFamily = CompletedPoppins,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            color = CompletedMuted,
            textAlign = TextAlign.Center,
            lineHeight = 16.sp,
            modifier = Modifier.padding(horizontal = 18.dp)
        )
    }
}

@Composable
private fun CompletedDetailSkeleton(isTablet: Boolean) {
    val alpha = completedSkeletonAlpha()

    Column(verticalArrangement = Arrangement.spacedBy(22.dp)) {
        CompletedSkeletonBox(
            modifier = Modifier
                .fillMaxWidth()
                .height(330.dp),
            alpha = alpha,
            color = CompletedDeepGreen,
            shape = RoundedCornerShape(30.dp)
        )

        CompletedSkeletonBox(
            modifier = Modifier
                .fillMaxWidth()
                .height(190.dp),
            alpha = alpha,
            color = Color(0xFFD9D2C6),
            shape = RoundedCornerShape(26.dp)
        )

        if (isTablet) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CompletedSkeletonBox(
                    modifier = Modifier
                        .weight(1f)
                        .height(290.dp),
                    alpha = alpha,
                    color = Color(0xFFD9D2C6),
                    shape = RoundedCornerShape(26.dp)
                )

                CompletedSkeletonBox(
                    modifier = Modifier
                        .weight(1f)
                        .height(220.dp),
                    alpha = alpha,
                    color = Color(0xFFD9D2C6),
                    shape = RoundedCornerShape(26.dp)
                )
            }
        } else {
            CompletedSkeletonBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(290.dp),
                alpha = alpha,
                color = Color(0xFFD9D2C6),
                shape = RoundedCornerShape(26.dp)
            )

            CompletedSkeletonBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                alpha = alpha,
                color = Color(0xFFD9D2C6),
                shape = RoundedCornerShape(26.dp)
            )
        }
    }
}

@Composable
private fun completedSkeletonAlpha(): Float {
    val transition = rememberInfiniteTransition(label = "completedDetailSkeleton")
    val alpha by transition.animateFloat(
        initialValue = 0.28f,
        targetValue = 0.62f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "completedDetailSkeletonAlpha"
    )
    return alpha
}

@Composable
private fun CompletedSkeletonBox(
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
private fun CompletedDetailBottomNavBar(
    onDashboardClick: () -> Unit,
    onTasksClick: () -> Unit,
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
        CompletedNavItem(Lucide.LayoutDashboard, "Dashboard", false, onDashboardClick)
        CompletedNavItem(Lucide.ClipboardList, "Tasks", true, onTasksClick)
        CompletedNavItem(Lucide.UserRound, "Profile", false, onProfileClick)
    }
}

@Composable
private fun CompletedNavItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(92.dp)
            .height(52.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
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
            fontFamily = CompletedPoppins,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            fontSize = 9.sp,
            color = if (selected) Color.White else Color(0xFFCFE8D2),
            textAlign = TextAlign.Center,
            lineHeight = 11.sp
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun CompletedTaskDetailScreenPreview() {
    CompletedTaskDetailScreen(
        task = CompletedTaskDetailUiState(
            id = 1,
            title = "Hatch and Mortality Check",
            description = "Record hatch and mortality data for the assigned pen.",
            timeAssigned = "6-5-26\n9:21 AM",
            statusLabel = "Completed",
            finishBy = "6-5-26\n5:00 PM",
            timeCompleted = "6-5-26\n2:53 PM",
            timeCompletedLabel = "Time Completed",
            priorityLabel = "Low",
            priority = TaskPriority.LOW,
            notes = "Recorded successfully.",
            photoUrl = null,
            submittedFields = listOf(
                TaskSubmittedField("Eggs Hatched", "25"),
                TaskSubmittedField("Mortality", "2")
            )
        )
    )
}