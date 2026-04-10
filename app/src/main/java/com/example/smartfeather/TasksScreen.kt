package com.example.smartfeather

import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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
    val timeLabel: String,
    val priority: TaskPriority,
    val status: TaskStatus,
    val notes: String = "",
    val hasPhoto: Boolean = false
)

private val AppPoppins = FontFamily(
    Font(R.font.poppins_regular, FontWeight.Normal),
    Font(R.font.poppins_medium, FontWeight.Medium),
    Font(R.font.poppins_semibold, FontWeight.SemiBold),
    Font(R.font.poppins_bold, FontWeight.Bold)
)

@Composable
fun TasksScreen(
    employeeId: Int,
    onNavigateToDashboard: () -> Unit,
    onNavigateToFarmManagement: () -> Unit,
    onPendingTaskClick: (TaskItem) -> Unit,
    onCompletedTaskClick: (TaskItem) -> Unit
) {
    val taskService = remember { TaskBackendService() }

    val tasks = remember { mutableStateListOf<TaskItem>() }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(employeeId) {
        isLoading = true
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
    }

    val pendingTasks = tasks.filter { it.status == TaskStatus.PENDING }
    val forApprovalTasks = tasks.filter { it.status == TaskStatus.FOR_APPROVAL }
    val completedTasks = tasks.filter { it.status == TaskStatus.COMPLETED }

    Scaffold(
        containerColor = Color(0xFFF5F2EE),
        bottomBar = {
            TasksBottomNavBar(
                onDashboardClick = onNavigateToDashboard,
                onFarmManagementClick = onNavigateToFarmManagement
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFFF8F6F2), Color(0xFFF1EEEA))
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
                Text(
                    text = "Tasks",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    fontFamily = AppPoppins,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 22.sp,
                    color = Color(0xFF171717)
                )

                Spacer(modifier = Modifier.height(18.dp))

                if (isLoading) {
                    Card(
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Loading tasks...",
                            modifier = Modifier.padding(18.dp),
                            fontFamily = AppPoppins,
                            fontSize = 14.sp,
                            color = Color(0xFF6A6A6A)
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))
                }

                if (errorMessage != null) {
                    Card(
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = errorMessage ?: "Something went wrong.",
                            modifier = Modifier.padding(18.dp),
                            fontFamily = AppPoppins,
                            fontSize = 14.sp,
                            color = Color(0xFFC51E1E)
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))
                }

                SectionChip(
                    text = "Pending",
                    containerColor = Color(0xFFF4A46E)
                )

                Spacer(modifier = Modifier.height(14.dp))

                TaskListSection(
                    items = pendingTasks,
                    emptyText = "No pending tasks right now.",
                    onTaskClick = onPendingTaskClick
                )

                Spacer(modifier = Modifier.height(26.dp))

                SectionChip(
                    text = "For Approval",
                    containerColor = Color(0xFFD88913)
                )

                Spacer(modifier = Modifier.height(14.dp))

                TaskListSection(
                    items = forApprovalTasks,
                    emptyText = "No tasks waiting for verification.",
                    onTaskClick = onCompletedTaskClick
                )

                Spacer(modifier = Modifier.height(26.dp))

                SectionChip(
                    text = "Completed",
                    containerColor = Color(0xFF266F33)
                )

                Spacer(modifier = Modifier.height(14.dp))

                TaskListSection(
                    items = completedTasks,
                    emptyText = "No completed tasks yet.",
                    onTaskClick = onCompletedTaskClick
                )

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun SectionChip(
    text: String,
    containerColor: Color
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(containerColor)
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            fontFamily = AppPoppins,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            color = Color.White
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
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = emptyText,
                modifier = Modifier.padding(18.dp),
                fontFamily = AppPoppins,
                fontSize = 14.sp,
                color = Color(0xFF6A6A6A)
            )
        }
        return
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(10.dp, RoundedCornerShape(28.dp)),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(modifier = Modifier.padding(vertical = 4.dp)) {
            items.forEachIndexed { index, item ->
                TaskRow(
                    item = item,
                    onTaskClick = onTaskClick
                )

                if (index != items.lastIndex) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(Color(0xFFEDE8E2))
                    )
                }
            }
        }
    }
}

@Composable
private fun TaskRow(
    item: TaskItem,
    onTaskClick: (TaskItem) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onTaskClick(item) }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = item.title,
                fontFamily = AppPoppins,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color(0xFF1B6A23)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = item.description,
                fontFamily = AppPoppins,
                fontWeight = FontWeight.Normal,
                fontSize = 13.sp,
                color = Color(0xFF6A6A6A),
                lineHeight = 17.sp
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column(
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = "${item.houseLabel}  ${item.penLabel}",
                fontFamily = AppPoppins,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                color = Color(0xFF7FA9A2)
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = item.timeLabel,
                fontFamily = AppPoppins,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                lineHeight = 13.sp,
                textAlign = TextAlign.End,
                color = when (item.status) {
                    TaskStatus.PENDING -> Color(0xFFD17A17)
                    TaskStatus.FOR_APPROVAL -> Color(0xFFC27A11)
                    TaskStatus.COMPLETED -> Color(0xFF4E8D39)
                }
            )

            Spacer(modifier = Modifier.height(14.dp))

            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(priorityColor(item.priority))
            )
        }
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
    onFarmManagementClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF06331D), Color(0xFF022816))
                )
            )
            .navigationBarsPadding()
            .padding(horizontal = 10.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        BottomNavItem(
            icon = Icons.Outlined.Home,
            label = "Dashboard",
            selected = false,
            onClick = onDashboardClick
        )

        BottomNavItem(
            icon = Icons.AutoMirrored.Outlined.List,
            label = "Tasks",
            selected = true,
            onClick = {}
        )

        BottomNavItem(
            icon = Icons.Outlined.Edit,
            label = "Farm Management",
            selected = false,
            onClick = onFarmManagementClick
        )

        BottomNavItem(
            icon = Icons.Outlined.AccountCircle,
            label = "Profile",
            selected = false,
            onClick = {}
        )
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
        modifier = Modifier.clickable { onClick() },
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

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun TasksScreenPreview() {
    TasksScreen(
        employeeId = 2,
        onNavigateToDashboard = {},
        onNavigateToFarmManagement = {},
        onPendingTaskClick = {},
        onCompletedTaskClick = {}
    )
}
