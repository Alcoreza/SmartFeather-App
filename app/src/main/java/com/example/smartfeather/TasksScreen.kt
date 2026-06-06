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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.ClipboardList
import com.composables.icons.lucide.LayoutDashboard
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.UserRound
import kotlinx.coroutines.launch
import androidx.compose.material3.pulltorefresh.PullToRefreshBox

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

data class TaskSubmittedField(
    val label: String,
    val value: String
)

data class TaskItem(
    val id: Int,
    val title: String,
    val description: String,
    val houseId: Int? = null,
    val penNumber: Int? = null,
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
    val photoUrl: String? = null,
    val submittedFields: List<TaskSubmittedField> = emptyList(),
    val biosecurityCleared: Boolean = false
)

private val TaskManrope = FontFamily(
    Font(R.font.manrope_regular, FontWeight.Normal),
    Font(R.font.manrope_medium, FontWeight.Medium),
    Font(R.font.manrope_semibold, FontWeight.SemiBold),
    Font(R.font.manrope_bold, FontWeight.Bold),
    Font(R.font.manrope_extrabold, FontWeight.ExtraBold)
)

private val TaskCream = Color(0xFFF6F3EC)
private val TaskSectionSurface = Color(0xFFF4EFE6)
private val TaskInk = Color(0xFF121A14)
private val TaskMuted = Color(0xFF677168)
private val TaskLine = Color(0xFFD2C8B8)
private val TaskGreen = Color(0xFF1F7A3A)
private val TaskDeepGreen = Color(0xFF062717)

private val PendingColor = Color(0xFF8A7A2E)
private val ApprovalColor = Color(0xFF2F6F68)
private val CompletedColor = Color(0xFF2F7D46)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    employeeId: Int,
    onNavigateToDashboard: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onPendingTaskClick: (TaskItem) -> Unit,
    onCompletedTaskClick: (TaskItem) -> Unit
) {
    val taskService = remember { TaskBackendService() }
    val coroutineScope = rememberCoroutineScope()

    val pendingTasks = remember { mutableStateListOf<TaskItem>() }
    val forApprovalTasks = remember { mutableStateListOf<TaskItem>() }
    val completedTasks = remember { mutableStateListOf<TaskItem>() }

    var taskCounts by remember { mutableStateOf(TaskCounts()) }

    var pendingPage by remember { mutableStateOf(1) }
    var forApprovalPage by remember { mutableStateOf(1) }
    var completedPage by remember { mutableStateOf(1) }

    var pendingHasMore by remember { mutableStateOf(false) }
    var forApprovalHasMore by remember { mutableStateOf(false) }
    var completedHasMore by remember { mutableStateOf(false) }

    var isLoading by remember { mutableStateOf(true) }
    var isRefreshing by remember { mutableStateOf(false) }
    var isPendingLoadingMore by remember { mutableStateOf(false) }
    var isForApprovalLoadingMore by remember { mutableStateOf(false) }
    var isCompletedLoadingMore by remember { mutableStateOf(false) }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var contentVisible by remember { mutableStateOf(false) }
    var showPendingTasks by remember { mutableStateOf(true) }
    var showForApprovalTasks by remember { mutableStateOf(true) }
    var showCompletedTasks by remember { mutableStateOf(true) }

    fun applyCachedPage(
        status: TaskStatus,
        cachedPage: CachedTaskPage
    ) {
        taskCounts = cachedPage.counts

        when (status) {
            TaskStatus.PENDING -> {
                pendingTasks.clear()
                pendingTasks.addAll(cachedPage.tasks)
                pendingPage = cachedPage.page
                pendingHasMore = cachedPage.hasMore
            }

            TaskStatus.FOR_APPROVAL -> {
                forApprovalTasks.clear()
                forApprovalTasks.addAll(cachedPage.tasks)
                forApprovalPage = cachedPage.page
                forApprovalHasMore = cachedPage.hasMore
            }

            TaskStatus.COMPLETED -> {
                completedTasks.clear()
                completedTasks.addAll(cachedPage.tasks)
                completedPage = cachedPage.page
                completedHasMore = cachedPage.hasMore
            }
        }
    }

    fun applyPageResult(
        status: TaskStatus,
        page: Int,
        result: TaskPageResult
    ) {
        taskCounts = result.counts

        when (status) {
            TaskStatus.PENDING -> {
                if (page == 1) pendingTasks.clear()
                pendingTasks.addAll(result.tasks)
                pendingPage = page
                pendingHasMore = result.hasMore
            }

            TaskStatus.FOR_APPROVAL -> {
                if (page == 1) forApprovalTasks.clear()
                forApprovalTasks.addAll(result.tasks)
                forApprovalPage = page
                forApprovalHasMore = result.hasMore
            }

            TaskStatus.COMPLETED -> {
                if (page == 1) completedTasks.clear()
                completedTasks.addAll(result.tasks)
                completedPage = page
                completedHasMore = result.hasMore
            }
        }
    }

    suspend fun refreshFirstPages(
        forceRefresh: Boolean,
        showSkeleton: Boolean
    ) {
        if (showSkeleton) {
            isLoading = true
            contentVisible = false
        } else {
            isRefreshing = true
        }

        errorMessage = null

        if (forceRefresh) {
            TaskBackendService.clearTaskCache(employeeId)
        }

        pendingPage = 1
        forApprovalPage = 1
        completedPage = 1

        val pendingResult = taskService.getTasksForFlockman(
            employeeId = employeeId,
            status = TaskStatus.PENDING,
            page = 1,
            perPage = 10,
            forceRefresh = forceRefresh
        )

        val forApprovalResult = taskService.getTasksForFlockman(
            employeeId = employeeId,
            status = TaskStatus.FOR_APPROVAL,
            page = 1,
            perPage = 10,
            forceRefresh = forceRefresh
        )

        val completedResult = taskService.getTasksForFlockman(
            employeeId = employeeId,
            status = TaskStatus.COMPLETED,
            page = 1,
            perPage = 10,
            forceRefresh = forceRefresh
        )

        pendingResult.onSuccess { applyPageResult(TaskStatus.PENDING, 1, it) }
            .onFailure { errorMessage = it.message ?: "Unable to load pending tasks." }

        forApprovalResult.onSuccess { applyPageResult(TaskStatus.FOR_APPROVAL, 1, it) }
            .onFailure { errorMessage = it.message ?: "Unable to load for approval tasks." }

        completedResult.onSuccess { applyPageResult(TaskStatus.COMPLETED, 1, it) }
            .onFailure { errorMessage = it.message ?: "Unable to load completed tasks." }

        isLoading = false
        isRefreshing = false
        contentVisible = true
    }

    fun loadMoreTasks(status: TaskStatus) {
        val nextPage = when (status) {
            TaskStatus.PENDING -> pendingPage + 1
            TaskStatus.FOR_APPROVAL -> forApprovalPage + 1
            TaskStatus.COMPLETED -> completedPage + 1
        }

        coroutineScope.launch {
            when (status) {
                TaskStatus.PENDING -> isPendingLoadingMore = true
                TaskStatus.FOR_APPROVAL -> isForApprovalLoadingMore = true
                TaskStatus.COMPLETED -> isCompletedLoadingMore = true
            }

            taskService.getTasksForFlockman(
                employeeId = employeeId,
                status = status,
                page = nextPage,
                perPage = 10
            ).onSuccess { result ->
                applyPageResult(status, nextPage, result)
            }.onFailure { throwable ->
                errorMessage = throwable.message ?: "Unable to load more tasks."
            }

            when (status) {
                TaskStatus.PENDING -> isPendingLoadingMore = false
                TaskStatus.FOR_APPROVAL -> isForApprovalLoadingMore = false
                TaskStatus.COMPLETED -> isCompletedLoadingMore = false
            }
        }
    }

    LaunchedEffect(employeeId) {
        val cachedPending = taskService.getCachedTaskPage(employeeId, TaskStatus.PENDING)
        val cachedForApproval = taskService.getCachedTaskPage(employeeId, TaskStatus.FOR_APPROVAL)
        val cachedCompleted = taskService.getCachedTaskPage(employeeId, TaskStatus.COMPLETED)

        val hasCache = cachedPending != null || cachedForApproval != null || cachedCompleted != null

        if (cachedPending != null) {
            applyCachedPage(TaskStatus.PENDING, cachedPending)
        }

        if (cachedForApproval != null) {
            applyCachedPage(TaskStatus.FOR_APPROVAL, cachedForApproval)
        }

        if (cachedCompleted != null) {
            applyCachedPage(TaskStatus.COMPLETED, cachedCompleted)
        }

        if (hasCache) {
            isLoading = false
            contentVisible = true
        }

        refreshFirstPages(
            forceRefresh = false,
            showSkeleton = !hasCache
        )
    }

    Scaffold(
        containerColor = TaskCream,
        bottomBar = {
            TasksBottomNavBar(
                onDashboardClick = onNavigateToDashboard,
                onProfileClick = onNavigateToProfile
            )
        }
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                coroutineScope.launch {
                    refreshFirstPages(
                        forceRefresh = true,
                        showSkeleton = false
                    )
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFFFBF8F1), TaskCream, Color(0xFFEDE7DA))
                        )
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 18.dp, vertical = 18.dp)
                ) {
                    TaskHeader(
                        pendingCount = taskCounts.pending,
                        approvalCount = taskCounts.forApproval,
                        completedCount = taskCounts.completed
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
                                count = taskCounts.pending,
                                color = PendingColor,
                                isExpanded = showPendingTasks,
                                onToggleClick = { showPendingTasks = !showPendingTasks },
                                visible = showPendingTasks,
                                items = pendingTasks,
                                emptyText = "No pending tasks right now.",
                                hasMore = pendingHasMore,
                                isLoadingMore = isPendingLoadingMore,
                                onLoadMore = { loadMoreTasks(TaskStatus.PENDING) },
                                onTaskClick = onPendingTaskClick
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            TaskStatusSection(
                                title = "For Approval",
                                count = taskCounts.forApproval,
                                color = ApprovalColor,
                                isExpanded = showForApprovalTasks,
                                onToggleClick = { showForApprovalTasks = !showForApprovalTasks },
                                visible = showForApprovalTasks,
                                items = forApprovalTasks,
                                emptyText = "No tasks waiting for verification.",
                                hasMore = forApprovalHasMore,
                                isLoadingMore = isForApprovalLoadingMore,
                                onLoadMore = { loadMoreTasks(TaskStatus.FOR_APPROVAL) },
                                onTaskClick = onCompletedTaskClick
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            TaskStatusSection(
                                title = "Completed",
                                count = taskCounts.completed,
                                color = CompletedColor,
                                isExpanded = showCompletedTasks,
                                onToggleClick = { showCompletedTasks = !showCompletedTasks },
                                visible = showCompletedTasks,
                                items = completedTasks,
                                emptyText = "No completed tasks yet.",
                                hasMore = completedHasMore,
                                isLoadingMore = isCompletedLoadingMore,
                                onLoadMore = { loadMoreTasks(TaskStatus.COMPLETED) },
                                onTaskClick = onCompletedTaskClick
                            )

                            Spacer(modifier = Modifier.height(20.dp))
                        }
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
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(30.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(TaskDeepGreen, Color(0xFF0E4025), Color(0xFF155C2D))
                )
            )
            .padding(horizontal = 20.dp, vertical = 22.dp)
    ) {
        Text(
            text = "Tasks",
            fontFamily = TaskManrope,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 29.sp,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Assigned work, submissions, and completed tasks.",
            fontFamily = TaskManrope,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
            color = Color.White.copy(alpha = 0.72f),
            lineHeight = 18.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            QuietMetric("Pending", pendingCount.toString(), PendingColor, Modifier.weight(1f))
            QuietMetric("Approval", approvalCount.toString(), ApprovalColor, Modifier.weight(1f))
            QuietMetric("Done", completedCount.toString(), CompletedColor, Modifier.weight(1f))
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
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White.copy(alpha = 0.12f))
            .padding(horizontal = 10.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            fontFamily = TaskManrope,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 18.sp,
            color = color
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = label,
            fontFamily = TaskManrope,
            fontWeight = FontWeight.SemiBold,
            fontSize = 10.sp,
            color = Color.White.copy(alpha = 0.76f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun TaskStatusSection(
    title: String,
    count: Int,
    color: Color,
    isExpanded: Boolean,
    onToggleClick: () -> Unit,
    visible: Boolean,
    items: List<TaskItem>,
    emptyText: String,
    hasMore: Boolean,
    isLoadingMore: Boolean,
    onLoadMore: () -> Unit,
    onTaskClick: (TaskItem) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(TaskSectionSurface)
            .border(1.dp, TaskLine.copy(alpha = 0.58f), RoundedCornerShape(28.dp))
            .padding(14.dp)
    ) {
        SectionHeader(
            text = title,
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
            hasMore = hasMore,
            isLoadingMore = isLoadingMore,
            onLoadMore = onLoadMore,
            onTaskClick = onTaskClick
        )
    }
}

@Composable
private fun SectionHeader(
    text: String,
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
                .background(containerColor.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = count.toString(),
                fontFamily = TaskManrope,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 15.sp,
                color = containerColor
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = text,
            modifier = Modifier.weight(1f),
            fontFamily = TaskManrope,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 18.sp,
            color = TaskInk
        )

        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(Color(0xFFEDE7DA))
                .clickable { onToggleClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isExpanded) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                contentDescription = if (isExpanded) "Hide $text tasks" else "Show $text tasks",
                tint = TaskInk.copy(alpha = 0.68f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun AnimatedTaskListSection(
    visible: Boolean,
    items: List<TaskItem>,
    emptyText: String,
    hasMore: Boolean,
    isLoadingMore: Boolean,
    onLoadMore: () -> Unit,
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
            hasMore = hasMore,
            isLoadingMore = isLoadingMore,
            onLoadMore = onLoadMore,
            onTaskClick = onTaskClick
        )
    }
}

@Composable
private fun TaskListSection(
    items: List<TaskItem>,
    emptyText: String,
    hasMore: Boolean,
    isLoadingMore: Boolean,
    onLoadMore: () -> Unit,
    onTaskClick: (TaskItem) -> Unit
) {
    if (items.isEmpty()) {
        EmptyTaskCard(emptyText)
        return
    }

    Column(verticalArrangement = Arrangement.spacedBy(13.dp)) {
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

        if (hasMore) {
            LoadMoreTasksButton(
                isLoading = isLoadingMore,
                onClick = onLoadMore
            )
        }
    }
}

@Composable
private fun EmptyTaskCard(emptyText: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(Color(0xFFF1EAE0))
            .padding(horizontal = 16.dp, vertical = 18.dp)
    ) {
        Text(
            text = emptyText,
            fontFamily = TaskManrope,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            color = TaskMuted
        )
    }
}

@Composable
private fun LoadMoreTasksButton(
    isLoading: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(999.dp))
                .background(TaskGreen.copy(alpha = 0.09f))
                .clickable(enabled = !isLoading) { onClick() }
                .padding(horizontal = 18.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(15.dp),
                    strokeWidth = 2.dp,
                    color = TaskGreen
                )
            } else {
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowDown,
                    contentDescription = "Load more tasks",
                    tint = TaskGreen,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = if (isLoading) "Loading" else "Load more",
                fontFamily = TaskManrope,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 12.sp,
                color = TaskGreen
            )
        }
    }
}

@Composable
private fun TaskRow(
    item: TaskItem,
    onTaskClick: (TaskItem) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(132.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(taskRowColor(item.status))
            .clickable { onTaskClick(item) }
            .padding(horizontal = 15.dp, vertical = 15.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    PriorityDot(priority = item.priority)

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = item.title,
                        fontFamily = TaskManrope,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        color = TaskInk,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = item.description,
                    fontFamily = TaskManrope,
                    fontWeight = FontWeight.Medium,
                    fontSize = 11.sp,
                    color = TaskMuted,
                    lineHeight = 15.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            PriorityPill(priority = item.priority)
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            QuietLocationLabel("${item.houseLabel} | ${item.penLabel}")

            Spacer(modifier = Modifier.width(8.dp))

            TaskTimeLabel(
                item = item,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun PriorityDot(priority: TaskPriority) {
    Box(
        modifier = Modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(priorityColor(priority).copy(alpha = 0.86f))
    )
}

@Composable
private fun PriorityPill(priority: TaskPriority) {
    val color = priorityColor(priority)

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(color.copy(alpha = 0.08f))
            .padding(horizontal = 9.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = priorityLabel(priority),
            fontFamily = TaskManrope,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 10.sp,
            color = color.copy(alpha = 0.92f),
            maxLines = 1
        )
    }
}

@Composable
private fun QuietLocationLabel(text: String) {
    Text(
        text = text,
        fontFamily = TaskManrope,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 11.sp,
        color = TaskGreen,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
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
        TaskStatus.PENDING -> Color(0xFFB96E18)
        TaskStatus.FOR_APPROVAL -> Color(0xFFA96613)
        TaskStatus.COMPLETED -> Color(0xFF467C36)
    }

    if (label.isBlank()) {
        Spacer(modifier = modifier)
        return
    }

    Text(
        text = label.replace("\n", " "),
        modifier = modifier,
        fontFamily = TaskManrope,
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp,
        lineHeight = 13.sp,
        textAlign = TextAlign.End,
        color = color.copy(alpha = 0.88f),
        maxLines = 2,
        overflow = TextOverflow.Ellipsis
    )
}

private fun taskRowColor(status: TaskStatus): Color {
    return when (status) {
        TaskStatus.PENDING -> Color(0xFFFFFCF7)
        TaskStatus.FOR_APPROVAL -> Color(0xFFFFFCF7)
        TaskStatus.COMPLETED -> Color(0xFFFAFFF8)
    }
}

private fun priorityLabel(priority: TaskPriority): String {
    return when (priority) {
        TaskPriority.LOW -> "Low"
        TaskPriority.MID -> "Medium"
        TaskPriority.HIGH -> "High"
    }
}

private fun priorityColor(priority: TaskPriority): Color {
    return when (priority) {
        TaskPriority.LOW -> Color(0xFF6D7E3A)
        TaskPriority.MID -> Color(0xFF8A7A2E)
        TaskPriority.HIGH -> Color(0xFFB8483A)
    }
}

@Composable
private fun TaskLoadingSkeleton() {
    Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
        SkeletonSection()
        SkeletonSection()
        SkeletonSection()
    }
}

@Composable
private fun SkeletonSection() {
    val alpha = skeletonAlpha()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(TaskSectionSurface)
            .border(1.dp, TaskLine.copy(alpha = 0.58f), RoundedCornerShape(28.dp))
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SkeletonBox(
                modifier = Modifier.size(42.dp),
                alpha = alpha,
                color = Color(0xFFDAD4C8),
                shape = CircleShape
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                SkeletonLine(widthFraction = 0.42f, height = 18.dp, alpha = alpha)
                Spacer(modifier = Modifier.height(7.dp))
                SkeletonLine(widthFraction = 0.78f, height = 11.dp, alpha = alpha)
            }

            SkeletonBox(
                modifier = Modifier.size(34.dp),
                alpha = alpha,
                color = Color(0xFFDAD4C8),
                shape = CircleShape
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        repeat(2) {
            SkeletonTaskCard(alpha = alpha, index = it)
            if (it == 0) Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
private fun SkeletonTaskCard(
    alpha: Float,
    index: Int
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(132.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(if (index % 2 == 0) Color(0xFFFFF6EA) else Color(0xFFF7ECDA))
            .padding(horizontal = 15.dp, vertical = 15.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                SkeletonBox(
                    modifier = Modifier.size(8.dp),
                    alpha = alpha,
                    color = Color(0xFFC27A16),
                    shape = CircleShape
                )

                Spacer(modifier = Modifier.width(8.dp))

                SkeletonLine(widthFraction = 0.42f, height = 20.dp, alpha = alpha)

                Spacer(modifier = Modifier.weight(1f))

                SkeletonLine(widthFraction = 0.18f, height = 20.dp, alpha = alpha)
            }

            Spacer(modifier = Modifier.height(10.dp))

            SkeletonLine(widthFraction = 0.92f, height = 10.dp, alpha = alpha)

            Spacer(modifier = Modifier.height(7.dp))

            SkeletonLine(widthFraction = 0.68f, height = 10.dp, alpha = alpha)

            Spacer(modifier = Modifier.weight(1f))

            Row(verticalAlignment = Alignment.CenterVertically) {
                SkeletonLine(widthFraction = 0.34f, height = 13.dp, alpha = alpha)
                Spacer(modifier = Modifier.weight(1f))
                SkeletonLine(widthFraction = 0.28f, height = 13.dp, alpha = alpha)
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
            fontFamily = TaskManrope,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            color = Color(0xFFC51E1E)
        )
    }
}

@Composable
private fun TasksBottomNavBar(
    onDashboardClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF07381F), Color(0xFF022716))
                )
            )
            .navigationBarsPadding()
            .padding(horizontal = 8.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        BottomNavItem(Lucide.LayoutDashboard, "Dashboard", false, onDashboardClick)
        BottomNavItem(Lucide.ClipboardList, "Tasks", true, {})
        BottomNavItem(Lucide.UserRound, "Profile", false, onProfileClick)
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
            fontFamily = TaskManrope,
            fontSize = 10.sp,
            fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.SemiBold,
            color = if (selected) Color.White else Color(0xFFD7ECD9),
            textAlign = TextAlign.Center,
            lineHeight = 11.sp
        )
    }
}