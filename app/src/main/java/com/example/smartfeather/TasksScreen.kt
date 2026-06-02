package com.example.smartfeather

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Shape
import androidx.compose.foundation.layout.fillMaxHeight

enum class TaskStatus {
    PENDING,
    FOR_APPROVAL,
    COMPLETED
}

enum class TaskPriority {
    LOW,
    MID,
    HIGH
}

data class TaskItem(
    val id: Int,
    val title: String,
    val description: String,
    val houseLabel: String,
    val penLabel: String,
    val assignedLabel: String = "",
    val finishByLabel: String = "",
    val submittedLabel: String = "",
    val completedLabel: String = "",
    val priority: TaskPriority,
    val status: TaskStatus,
    val notes: String = "",
    val hasPhoto: Boolean = false,
    val photoUrl: String? = null
)

private val AppPoppins = FontFamily(
    Font(R.font.manrope_regular, FontWeight.Normal),
    Font(R.font.manrope_medium, FontWeight.Medium),
    Font(R.font.manrope_semibold, FontWeight.SemiBold),
    Font(R.font.manrope_bold, FontWeight.Bold),
    Font(R.font.manrope_extrabold, FontWeight.ExtraBold)
)

private val TaskCream = Color(0xFFF1ECE2)
private val TaskSurface = Color(0xFFFFFCF6)
private val TaskSurfaceAlt = Color(0xFFE9E1D4)
private val TaskInk = Color(0xFF101A13)
private val TaskMuted = Color(0xFF6A7068)
private val TaskLine = Color(0xFFD7CFC1)
private val TaskGreen = Color(0xFF1F7A3A)
private val TaskDeepGreen = Color(0xFF032416)
private val TaskPanelDark = Color(0xFF10271A)
private val PendingColor = Color(0xFFE79A43)
private val ApprovalColor = Color(0xFFC98213)
private val CompletedColor = Color(0xFF3C9A52)

@Composable
fun TasksScreen(
    employeeId: Int,
    onNavigateToDashboard: () -> Unit,
    onNavigateToFarmManagement: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onPendingTaskClick: (TaskItem) -> Unit,
    onCompletedTaskClick: (TaskItem) -> Unit
) {
    val taskService = remember { TaskBackendService() }

    val tasks = remember { mutableStateListOf<TaskItem>() }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var contentVisible by remember { mutableStateOf(false) }

    var showPendingTasks by remember { mutableStateOf(true) }
    var showForApprovalTasks by remember { mutableStateOf(true) }
    var showCompletedTasks by remember { mutableStateOf(true) }

    LaunchedEffect(employeeId) {
        isLoading = true
        contentVisible = false
        errorMessage = null

        taskService.getTasksForFlockman(employeeId)
            .onSuccess { items ->
                tasks.clear()
                tasks.addAll(items)
            }
            .onFailure { throwable ->
                tasks.clear()
                errorMessage = throwable.message ?: "Failed to load tasks."
            }

        isLoading = false
        contentVisible = true
    }

    val pendingTasks = tasks.filter { it.status == TaskStatus.PENDING }
    val forApprovalTasks = tasks.filter { it.status == TaskStatus.FOR_APPROVAL }
    val completedTasks = tasks.filter { it.status == TaskStatus.COMPLETED }

    Scaffold(
        containerColor = TaskCream,
        bottomBar = {
            TasksBottomNavBar(
                onDashboardClick = onNavigateToDashboard,
                onFarmManagementClick = onNavigateToFarmManagement,
                onProfileClick = onNavigateToProfile
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFFFFFCF7), TaskCream, Color(0xFFF2EEE5))
                    )
                )
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 18.dp, vertical = 18.dp)
            ) {
                TaskHeader(
                    pendingCount = pendingTasks.size,
                    approvalCount = forApprovalTasks.size,
                    completedCount = completedTasks.size
                )

                Spacer(modifier = Modifier.height(18.dp))

                if (isLoading) {
                    TaskLoadingSkeleton()
                    Spacer(modifier = Modifier.height(20.dp))
                    return@Column
                }

                errorMessage?.let {
                    ErrorCard(message = it)
                    Spacer(modifier = Modifier.height(18.dp))
                }

                AnimatedVisibility(
                    visible = contentVisible,
                    enter = fadeIn(animationSpec = tween(420)) + slideInVertically(
                        animationSpec = tween(420, easing = FastOutSlowInEasing),
                        initialOffsetY = { it / 10 }
                    )
                ) {
                    Column {
                        TaskStatusSection(
                            title = "Pending",
                            subtitle = "Tasks waiting to be completed",
                            count = pendingTasks.size,
                            color = PendingColor,
                            isExpanded = showPendingTasks,
                            onToggleClick = { showPendingTasks = !showPendingTasks },
                            visible = showPendingTasks,
                            items = pendingTasks,
                            emptyText = "No pending tasks right now.",
                            onTaskClick = onPendingTaskClick
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        TaskStatusSection(
                            title = "For Approval",
                            subtitle = "Submitted tasks waiting for verification",
                            count = forApprovalTasks.size,
                            color = ApprovalColor,
                            isExpanded = showForApprovalTasks,
                            onToggleClick = { showForApprovalTasks = !showForApprovalTasks },
                            visible = showForApprovalTasks,
                            items = forApprovalTasks,
                            emptyText = "No tasks waiting for verification.",
                            onTaskClick = onCompletedTaskClick
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        TaskStatusSection(
                            title = "Completed",
                            subtitle = "Verified and finished work",
                            count = completedTasks.size,
                            color = CompletedColor,
                            isExpanded = showCompletedTasks,
                            onToggleClick = { showCompletedTasks = !showCompletedTasks },
                            visible = showCompletedTasks,
                            items = completedTasks,
                            emptyText = "No completed tasks yet.",
                            onTaskClick = onCompletedTaskClick
                        )

                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun TaskHeader(
    pendingCount: Int,
    approvalCount: Int,
    completedCount: Int
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Tasks",
            fontFamily = AppPoppins,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 30.sp,
            color = TaskInk
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Assigned work, submissions, and completed tasks.",
            fontFamily = AppPoppins,
            fontWeight = FontWeight.Medium,
            fontSize = 13.sp,
            color = TaskMuted,
            lineHeight = 18.sp
        )

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(Color(0xFFF4F0E8))
                .border(1.dp, TaskLine, RoundedCornerShape(22.dp))
                .padding(horizontal = 10.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            QuietMetric(
                label = "Pending",
                value = pendingCount.toString(),
                color = PendingColor,
                modifier = Modifier.weight(1f)
            )

            QuietMetric(
                label = "Approval",
                value = approvalCount.toString(),
                color = ApprovalColor,
                modifier = Modifier.weight(1f)
            )

            QuietMetric(
                label = "Done",
                value = completedCount.toString(),
                color = CompletedColor,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun QuietMetric(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.62f))
            .padding(horizontal = 10.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            fontFamily = AppPoppins,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 18.sp,
            color = color
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = label,
            fontFamily = AppPoppins,
            fontWeight = FontWeight.SemiBold,
            fontSize = 10.sp,
            color = TaskMuted,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun HeaderMetric(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White.copy(alpha = 0.13f))
            .border(1.dp, Color.White.copy(alpha = 0.16f), RoundedCornerShape(18.dp))
            .padding(horizontal = 10.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )

        Spacer(modifier = Modifier.width(7.dp))

        Column {
            Text(
                text = value,
                fontFamily = AppPoppins,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 16.sp,
                color = Color.White
            )

            Text(
                text = label,
                fontFamily = AppPoppins,
                fontWeight = FontWeight.SemiBold,
                fontSize = 10.sp,
                color = Color.White.copy(alpha = 0.76f),
                lineHeight = 11.sp
            )
        }
    }
}

@Composable
private fun TaskStatusSection(
    title: String,
    subtitle: String,
    count: Int,
    color: Color,
    isExpanded: Boolean,
    onToggleClick: () -> Unit,
    visible: Boolean,
    items: List<TaskItem>,
    emptyText: String,
    onTaskClick: (TaskItem) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = TaskPanelDark),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            SectionHeader(
                text = title,
                subtitle = subtitle,
                count = count,
                containerColor = color,
                isExpanded = isExpanded,
                onToggleClick = onToggleClick
            )

            Spacer(modifier = Modifier.height(12.dp))

            AnimatedTaskListSection(
                visible = visible,
                items = items,
                emptyText = emptyText,
                onTaskClick = onTaskClick
            )
        }
    }
}

@Composable
private fun SectionHeader(
    text: String,
    subtitle: String,
    count: Int,
    containerColor: Color,
    isExpanded: Boolean,
    onToggleClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(containerColor.copy(alpha = 0.18f))
                .border(1.dp, containerColor.copy(alpha = 0.45f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = count.toString(),
                fontFamily = AppPoppins,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 15.sp,
                color = containerColor
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = text,
                fontFamily = AppPoppins,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = subtitle,
                fontFamily = AppPoppins,
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.68f),
                lineHeight = 13.sp
            )
        }

        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.10f))
                .border(1.dp, Color.White.copy(alpha = 0.16f), CircleShape)
                .clickable { onToggleClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isExpanded) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                contentDescription = if (isExpanded) "Hide $text tasks" else "Show $text tasks",
                tint = Color.White.copy(alpha = 0.82f),
                modifier = Modifier.size(15.dp)
            )
        }
    }
}

private fun statusShortLabel(text: String): String {
    return when (text) {
        "For Approval" -> "Review"
        "Completed" -> "Done"
        else -> text
    }
}

@Composable
private fun AnimatedTaskListSection(
    visible: Boolean,
    items: List<TaskItem>,
    emptyText: String,
    onTaskClick: (TaskItem) -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(220)) + expandVertically(
            animationSpec = tween(260, easing = FastOutSlowInEasing),
            expandFrom = Alignment.Top
        ),
        exit = fadeOut(animationSpec = tween(140)) + shrinkVertically(
            animationSpec = tween(210, easing = FastOutSlowInEasing),
            shrinkTowards = Alignment.Top
        )
    ) {
        TaskListSection(
            items = items,
            emptyText = emptyText,
            onTaskClick = onTaskClick
        )
    }
}

@Composable
private fun TaskListSection(
    items: List<TaskItem>,
    emptyText: String,
    onTaskClick: (TaskItem) -> Unit
) {
    if (items.isEmpty()) {
        EmptyTaskCard(emptyText)
        return
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items.forEachIndexed { index, item ->
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(animationSpec = tween(240 + index * 35)) + slideInVertically(
                    animationSpec = tween(260 + index * 35, easing = FastOutSlowInEasing),
                    initialOffsetY = { it / 5 }
                )
            ) {
                TaskRow(
                    item = item,
                    onTaskClick = onTaskClick
                )
            }
        }
    }
}

@Composable
private fun EmptyTaskCard(emptyText: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(Color.White.copy(alpha = 0.08f))
            .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(22.dp))
            .padding(horizontal = 16.dp, vertical = 18.dp)
    ) {
        Text(
            text = emptyText,
            fontFamily = AppPoppins,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            color = Color.White.copy(alpha = 0.72f)
        )
    }
}

@Composable
private fun TaskRow(
    item: TaskItem,
    onTaskClick: (TaskItem) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onTaskClick(item) },
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFCF6)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .width(5.dp)
                    .fillMaxHeight()
                    .background(priorityColor(item.priority))
            )

            Column(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp)
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.title,
                            fontFamily = AppPoppins,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            color = TaskInk,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(5.dp))

                        Text(
                            text = item.description,
                            fontFamily = AppPoppins,
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp,
                            color = TaskMuted,
                            lineHeight = 17.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(Color(0xFFE8F3E8))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "${item.houseLabel} | ${item.penLabel}",
                            fontFamily = AppPoppins,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = TaskGreen,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    TaskTimeLabel(
                        item = item,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}


@Composable
private fun LocationChip(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(Color(0xFFEAF6EC))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            fontFamily = AppPoppins,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            color = TaskGreen,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun TaskTimeLabel(
    item: TaskItem,
    modifier: Modifier = Modifier
) {
    val label = when (item.status) {
        TaskStatus.PENDING -> item.finishByLabel
        TaskStatus.FOR_APPROVAL -> item.submittedLabel
        TaskStatus.COMPLETED -> item.completedLabel
    }

    val color = when (item.status) {
        TaskStatus.PENDING -> Color(0xFFD17A17)
        TaskStatus.FOR_APPROVAL -> Color(0xFFC27A11)
        TaskStatus.COMPLETED -> Color(0xFF4E8D39)
    }

    if (label.isBlank()) {
        Spacer(modifier = modifier)
        return
    }

    Text(
        text = label.replace("\n", " "),
        modifier = modifier,
        fontFamily = AppPoppins,
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp,
        lineHeight = 13.sp,
        textAlign = TextAlign.End,
        color = color,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
private fun TaskLoadingSkeleton() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SkeletonHeaderBlock()
        SkeletonSection()
        SkeletonSection()
        SkeletonSection()
    }
}

@Composable
private fun SkeletonHeaderBlock() {
    val alpha = skeletonAlpha()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(154.dp)
            .clip(RoundedCornerShape(30.dp))
            .background(TaskDeepGreen.copy(alpha = 0.92f))
            .padding(20.dp)
    ) {
        Column {
            SkeletonLine(widthFraction = 0.42f, height = 26.dp, alpha = alpha, color = Color.White)
            Spacer(modifier = Modifier.height(10.dp))
            SkeletonLine(widthFraction = 0.72f, height = 14.dp, alpha = alpha, color = Color.White)
            Spacer(modifier = Modifier.height(22.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                SkeletonBox(modifier = Modifier.weight(1f).height(48.dp), alpha = alpha, color = Color.White)
                SkeletonBox(modifier = Modifier.weight(1f).height(48.dp), alpha = alpha, color = Color.White)
                SkeletonBox(modifier = Modifier.weight(1f).height(48.dp), alpha = alpha, color = Color.White)
            }
        }
    }
}

@Composable
private fun SkeletonSection() {
    val alpha = skeletonAlpha()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = TaskSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                SkeletonBox(
                    modifier = Modifier.size(42.dp),
                    alpha = alpha,
                    shape = CircleShape
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    SkeletonLine(widthFraction = 0.42f, height = 18.dp, alpha = alpha)
                    Spacer(modifier = Modifier.height(6.dp))
                    SkeletonLine(widthFraction = 0.72f, height = 11.dp, alpha = alpha)
                }

                SkeletonBox(
                    modifier = Modifier.size(32.dp),
                    alpha = alpha,
                    shape = CircleShape
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            repeat(2) {
                SkeletonTaskCard(alpha = alpha)
                if (it == 0) Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}

@Composable
private fun SkeletonTaskCard(alpha: Float) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(116.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFFFFFEFB))
            .border(1.dp, Color(0xFFEDE8DE), RoundedCornerShape(24.dp))
            .padding(15.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.Top) {
                SkeletonBox(
                    modifier = Modifier.size(10.dp),
                    alpha = alpha,
                    shape = CircleShape
                )

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    SkeletonLine(widthFraction = 0.46f, height = 17.dp, alpha = alpha)
                    Spacer(modifier = Modifier.height(8.dp))
                    SkeletonLine(widthFraction = 0.95f, height = 12.dp, alpha = alpha)
                    Spacer(modifier = Modifier.height(6.dp))
                    SkeletonLine(widthFraction = 0.70f, height = 12.dp, alpha = alpha)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(verticalAlignment = Alignment.Bottom) {
                SkeletonLine(widthFraction = 0.36f, height = 22.dp, alpha = alpha)
                Spacer(modifier = Modifier.weight(1f))
                SkeletonLine(widthFraction = 0.30f, height = 12.dp, alpha = alpha)
            }
        }
    }
}

@Composable
private fun skeletonAlpha(): Float {
    val transition = rememberInfiniteTransition(label = "taskSkeleton")
    val alpha by transition.animateFloat(
        initialValue = 0.34f,
        targetValue = 0.82f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "taskSkeletonAlpha"
    )
    return alpha
}

@Composable
private fun SkeletonLine(
    widthFraction: Float,
    height: androidx.compose.ui.unit.Dp,
    alpha: Float,
    color: Color = Color(0xFFDAD4C8)
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
    color: Color = Color(0xFFDAD4C8),
    shape: Shape = RoundedCornerShape(16.dp)
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(color.copy(alpha = alpha))
    )
}

@Composable
private fun ErrorCard(message: String) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF7F5)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(18.dp),
            fontFamily = AppPoppins,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            color = Color(0xFFC51E1E)
        )
    }
}

private fun priorityColor(priority: TaskPriority): Color {
    return when (priority) {
        TaskPriority.LOW -> Color(0xFFFA7A1F)
        TaskPriority.MID -> Color(0xFFC4420B)
        TaskPriority.HIGH -> Color(0xFFC51E1E)
    }
}

@Composable
private fun TasksBottomNavBar(
    onDashboardClick: () -> Unit,
    onFarmManagementClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF06331D), Color(0xFF022816))
                )
            )
            .navigationBarsPadding()
            .padding(horizontal = 10.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        BottomNavItem(Icons.Outlined.Home, "Dashboard", false, onDashboardClick)
        BottomNavItem(Icons.AutoMirrored.Outlined.List, "Tasks", true, {})
        BottomNavItem(Icons.Outlined.Edit, "Farm Management", false, onFarmManagementClick)
        BottomNavItem(Icons.Outlined.AccountCircle, "Profile", false, onProfileClick)
    }
}

@Composable
private fun BottomNavItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (selected) Color.White else Color(0xFFD7ECD9),
            modifier = Modifier.size(22.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = label,
            fontFamily = AppPoppins,
            fontSize = 10.sp,
            color = if (selected) Color.White else Color(0xFFD7ECD9),
            textAlign = TextAlign.Center,
            lineHeight = 11.sp
        )
    }
}