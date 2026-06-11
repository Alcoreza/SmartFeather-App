package com.example.smartfeather

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.launch


enum class AppScreen {
    LOGIN,
    DASHBOARD,
    TASKS,
    TASK_DETAIL,
    COMPLETED_TASK_DETAIL,
    PERSONNEL_LOGS,
    VISITOR,
    PROFILE,
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SmartFeatherApp()
        }
    }
}

private val MainManrope = FontFamily(
    Font(R.font.manrope_extralight, FontWeight.ExtraLight),
    Font(R.font.manrope_light, FontWeight.Light),
    Font(R.font.manrope_regular, FontWeight.Normal),
    Font(R.font.manrope_medium, FontWeight.Medium),
    Font(R.font.manrope_semibold, FontWeight.SemiBold),
    Font(R.font.manrope_bold, FontWeight.Bold),
    Font(R.font.manrope_extrabold, FontWeight.ExtraBold),
    Font(R.font.manrope_variablefont_wght, FontWeight.Black)
)

@Composable
fun SmartFeatherApp() {
    val authService = remember { SupabaseAuthService() }
    val taskService = remember { TaskBackendService() }
    val dashboardService = remember { DashboardBackendService() }
    val deviceTokenService = remember { MobileDeviceTokenBackendService() }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = {}
    )

    var currentScreen by remember { mutableStateOf(AppScreen.LOGIN) }
    var loggedInEmployeeId by remember { mutableStateOf<Int?>(null) }

    var pendingTaskWaitingForBiosecurity by remember {
        mutableStateOf<PendingTaskDetailUiState?>(null)
    }

    var biosecurityRequiredMessage by remember {
        mutableStateOf("Complete the biosecurity log first before opening this task.")
    }

    var isCheckingTaskAccess by remember {
        mutableStateOf(false)
    }

    var isLoadingSubmittedTaskDetail by remember {
        mutableStateOf(false)
    }

    var showBiosecurityRequiredDialog by remember {
        mutableStateOf(false)
    }

    var selectedPendingTask by remember {
        mutableStateOf<PendingTaskDetailUiState?>(null)
    }

    var selectedCompletedTask by remember {
        mutableStateOf<CompletedTaskDetailUiState?>(null)
    }

    var dashboardUiState by remember { mutableStateOf(emptyDashboardState()) }
    var isDashboardLoading by remember { mutableStateOf(false) }
    var isDashboardRefreshing by remember { mutableStateOf(false) }
    var isEnvironmentRefreshing by remember { mutableStateOf(false) }
    var isResourceRefreshing by remember { mutableStateOf(false) }
    var hasLoadedDashboard by remember { mutableStateOf(false) }

    var selectedEnvironmentHouseId by remember { mutableStateOf<Int?>(null) }
    var selectedEnvironmentPenId by remember { mutableStateOf<Int?>(null) }
    var selectedResourceHouseId by remember { mutableStateOf<Int?>(null) }
    var selectedResourcePenId by remember { mutableStateOf<Int?>(null) }

    var dashboardRequestId by remember { mutableStateOf(0) }

    var mobileAccessToken by remember { mutableStateOf<String?>(null) }

    var showSessionExpiredDialog by remember { mutableStateOf(false) }

    fun refreshDashboard(
        environmentHouseId: Int? = selectedEnvironmentHouseId,
        environmentPenId: Int? = selectedEnvironmentPenId,
        resourceHouseId: Int? = selectedResourceHouseId,
        resourcePenId: Int? = selectedResourcePenId,
        showFullSkeleton: Boolean = false,
        showPullRefresh: Boolean = false,
        showEnvironmentRefresh: Boolean = false,
        showResourceRefresh: Boolean = false
    ) {
        val accessToken = mobileAccessToken ?: return
        val requestId = dashboardRequestId + 1
        dashboardRequestId = requestId

        coroutineScope.launch {
            if (showFullSkeleton && !hasLoadedDashboard) {
                isDashboardLoading = true
            }

            if (showPullRefresh) {
                isDashboardRefreshing = true
            }

            if (showEnvironmentRefresh) {
                isEnvironmentRefreshing = true
            }

            if (showResourceRefresh) {
                isResourceRefreshing = true
            }

            dashboardService.getDashboard(
                accessToken = accessToken,
                environmentHouseId = environmentHouseId,
                environmentPenId = environmentPenId,
                resourceHouseId = resourceHouseId,
                resourcePenId = resourcePenId
            ).onSuccess { state ->
                if (requestId != dashboardRequestId) {
                    return@launch
                }

                dashboardUiState = state

                selectedEnvironmentHouseId = state.environmentFilter.selectedHouseId
                selectedEnvironmentPenId = state.environmentFilter.selectedPenId
                selectedResourceHouseId = state.resourceFilter.selectedHouseId
                selectedResourcePenId = state.resourceFilter.selectedPenId

                hasLoadedDashboard = true
            }

            if (requestId == dashboardRequestId) {
                isDashboardLoading = false
                isDashboardRefreshing = false
                isEnvironmentRefreshing = false
                isResourceRefreshing = false
            }
        }
    }

    fun clearMobileSession() {
        loggedInEmployeeId = null
        selectedPendingTask = null
        selectedCompletedTask = null
        pendingTaskWaitingForBiosecurity = null
        showBiosecurityRequiredDialog = false
        isCheckingTaskAccess = false
        isLoadingSubmittedTaskDetail = false
        biosecurityRequiredMessage = "Complete the biosecurity log first before opening this task."
        dashboardUiState = emptyDashboardState()
        isDashboardLoading = false
        isDashboardRefreshing = false
        isEnvironmentRefreshing = false
        isResourceRefreshing = false
        hasLoadedDashboard = false
        dashboardRequestId = 0
        selectedEnvironmentHouseId = null
        selectedEnvironmentPenId = null
        selectedResourceHouseId = null
        selectedResourcePenId = null
        mobileAccessToken = null
        authService.clearSession()
        TaskBackendService.clearTaskCache()
        currentScreen = AppScreen.LOGIN
    }

    fun openCompletedTaskDetail(
        task: TaskItem,
        submittedFields: List<TaskSubmittedField>
    ) {
        selectedCompletedTask = CompletedTaskDetailUiState(
            id = task.id,
            title = task.title,
            description = task.description,
            timeAssigned = task.assignedLabel.replace("\n", " "),
            finishBy = task.finishByLabel
                .removePrefix("Finish by: ")
                .replace("\n", " "),
            timeCompleted = when (task.status) {
                TaskStatus.FOR_APPROVAL -> task.submittedLabel
                    .removePrefix("Submitted: ")
                    .replace("\n", " ")

                TaskStatus.COMPLETED -> task.completedLabel
                    .removePrefix("Completed: ")
                    .replace("\n", " ")

                else -> ""
            },
            timeCompletedLabel = if (task.status == TaskStatus.FOR_APPROVAL) {
                "Submitted"
            } else {
                "Time Completed"
            },
            statusLabel = if (task.status == TaskStatus.FOR_APPROVAL) {
                "For Approval"
            } else {
                "Completed"
            },
            priorityLabel = task.priority.name.lowercase()
                .replaceFirstChar { it.uppercase() },
            priority = task.priority,
            notes = task.notes,
            hasPhoto = task.hasPhoto,
            photoUrl = task.photoUrl,
            submittedFields = submittedFields
        )

        currentScreen = AppScreen.COMPLETED_TASK_DETAIL
    }

    LaunchedEffect(loggedInEmployeeId, mobileAccessToken) {
        val employeeId = loggedInEmployeeId ?: return@LaunchedEffect

        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
            val deviceName = "${Build.MANUFACTURER} ${Build.MODEL}"

            coroutineScope.launch {
                val accessToken = mobileAccessToken ?: return@launch

                deviceTokenService.saveDeviceToken(
                    accessToken = accessToken,
                    fcmToken = token,
                    deviceName = deviceName
                )
            }

            println("FCM TOKEN GENERATED: $token")
        }
    }

    LaunchedEffect(currentScreen, loggedInEmployeeId) {
        val employeeId = loggedInEmployeeId

        if (currentScreen == AppScreen.DASHBOARD && employeeId != null && !hasLoadedDashboard) {
            refreshDashboard(showFullSkeleton = true)
        }
    }

    if (showBiosecurityRequiredDialog) {
        AlertDialog(
            onDismissRequest = {
                showBiosecurityRequiredDialog = false
                biosecurityRequiredMessage = "Complete the biosecurity log first before opening this task."
            },
            containerColor = Color(0xFFFFFCF7),
            shape = RoundedCornerShape(28.dp),
            title = {
                Text(
                    text = "Biosecurity Required",
                    fontFamily = MainManrope,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF121A14)
                )
            },
            text = {
                Text(
                    text = biosecurityRequiredMessage,
                    fontFamily = MainManrope,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF677168),
                    lineHeight = 21.sp
                )
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showBiosecurityRequiredDialog = false
                        pendingTaskWaitingForBiosecurity = null
                        biosecurityRequiredMessage = "Complete the biosecurity log first before opening this task."
                    }
                ) {
                    Text(
                        text = "Cancel",
                        fontFamily = MainManrope,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF677168)
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showBiosecurityRequiredDialog = false
                        biosecurityRequiredMessage = "Complete the biosecurity log first before opening this task."
                        currentScreen = AppScreen.PERSONNEL_LOGS
                    }
                ) {
                    Text(
                        text = "Continue",
                        fontFamily = MainManrope,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF1F7A3A)
                    )
                }
            }
        )
    }

    if (isCheckingTaskAccess) {
        AlertDialog(
            onDismissRequest = {},
            containerColor = Color(0xFFFFFCF7),
            shape = RoundedCornerShape(28.dp),
            title = {
                Text(
                    text = "Checking Access",
                    fontFamily = MainManrope,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF121A14)
                )
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Verifying your biosecurity status for this task.",
                        fontFamily = MainManrope,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF677168),
                        lineHeight = 21.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.5.dp,
                        color = Color(0xFF1F7A3A)
                    )
                }
            },
            confirmButton = {}
        )
    }

    if (isLoadingSubmittedTaskDetail) {
        AlertDialog(
            onDismissRequest = {},
            containerColor = Color(0xFFFFFCF7),
            shape = RoundedCornerShape(28.dp),
            title = {
                Text(
                    text = "Loading Details",
                    fontFamily = MainManrope,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF121A14)
                )
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Preparing the submitted task record.",
                        fontFamily = MainManrope,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF677168),
                        lineHeight = 21.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.5.dp,
                        color = Color(0xFF1F7A3A)
                    )
                }
            },
            confirmButton = {}
        )
    }

    if (showSessionExpiredDialog) {
        AlertDialog(
            onDismissRequest = {},
            containerColor = Color(0xFFFFFCF7),
            shape = RoundedCornerShape(28.dp),
            title = {
                Text(
                    text = "Session Expired",
                    fontFamily = MainManrope,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF121A14)
                )
            },
            text = {
                Text(
                    text = "Please log in again to continue.",
                    fontFamily = MainManrope,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF677168),
                    lineHeight = 21.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSessionExpiredDialog = false
                        clearMobileSession()
                    }
                ) {
                    Text(
                        text = "Log In",
                        fontFamily = MainManrope,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF1F7A3A)
                    )
                }
            }
        )
    }

    when (currentScreen) {
        AppScreen.LOGIN -> LoginScreen(
            onLoginClick = { username, password ->
                authService.signInFlockman(username = username, password = password)
            },
            onLoginSuccess = { employeeId ->
                loggedInEmployeeId = employeeId
                mobileAccessToken = authService.currentAccessToken
                currentScreen = AppScreen.DASHBOARD
            }
        )

        AppScreen.DASHBOARD -> DashboardScreen(
            onNavigateToTasks = {
                currentScreen = AppScreen.TASKS
            },
            onNavigateToProfile = {
                currentScreen = AppScreen.PROFILE
            },
            onQuickAccessVisitor = {
                currentScreen = AppScreen.VISITOR
            },
            uiState = dashboardUiState,
            isLoading = isDashboardLoading,
            isRefreshing = isDashboardRefreshing,
            isEnvironmentRefreshing = isEnvironmentRefreshing,
            isResourceRefreshing = isResourceRefreshing,
            onRefresh = {
                refreshDashboard(showPullRefresh = true)
            },
            onEnvironmentRefresh = {
                refreshDashboard(showEnvironmentRefresh = true)
            },
            onResourceRefresh = {
                refreshDashboard(showResourceRefresh = true)
            },
            onEnvironmentFilterChange = { option: SensorFilterOption ->
                selectedEnvironmentHouseId = option.houseId
                selectedEnvironmentPenId = option.penId

                dashboardUiState = dashboardUiState.copy(
                    environmentFilter = dashboardUiState.environmentFilter.copy(
                        selectedHouseId = option.houseId,
                        selectedPenId = option.penId
                    )
                )

                refreshDashboard(
                    environmentHouseId = option.houseId,
                    environmentPenId = option.penId,
                    showEnvironmentRefresh = true
                )
            },
            onResourceFilterChange = { option: SensorFilterOption ->
                selectedResourceHouseId = option.houseId
                selectedResourcePenId = option.penId

                dashboardUiState = dashboardUiState.copy(
                    resourceFilter = dashboardUiState.resourceFilter.copy(
                        selectedHouseId = option.houseId,
                        selectedPenId = option.penId
                    )
                )

                refreshDashboard(
                    resourceHouseId = option.houseId,
                    resourcePenId = option.penId,
                    showResourceRefresh = true
                )
            }
        )

        AppScreen.TASKS -> TasksScreen(
            employeeId = loggedInEmployeeId ?: 0,
            accessToken = mobileAccessToken.orEmpty(),
            onNavigateToDashboard = {
                currentScreen = AppScreen.DASHBOARD
            },
            onNavigateToProfile = {
                currentScreen = AppScreen.PROFILE
            },
            onPendingTaskClick = { task ->
                val pendingTask = PendingTaskDetailUiState(
                    id = task.id,
                    title = task.title,
                    description = task.description,
                    timeAssigned = task.assignedLabel.replace("\n", " "),
                    finishBy = task.finishByLabel
                        .removePrefix("Finish by: ")
                        .replace("\n", " "),
                    priorityLabel = task.priority.name.lowercase()
                        .replaceFirstChar { it.uppercase() },
                    priority = task.priority,
                    houseId = task.houseId,
                    penNumber = task.penNumber,
                    houseLabel = task.houseLabel,
                    penLabel = task.penLabel
                )

                selectedPendingTask = pendingTask
                pendingTaskWaitingForBiosecurity = pendingTask

                val employeeId = loggedInEmployeeId

                if (employeeId == null) {
                    biosecurityRequiredMessage = "Missing employee session. Please sign in again."
                    showBiosecurityRequiredDialog = true
                    return@TasksScreen
                }

                coroutineScope.launch {
                    isCheckingTaskAccess = true

                    taskService.checkTaskAccess(
                        accessToken = mobileAccessToken.orEmpty(),
                        taskId = task.id
                    ).onSuccess {
                        pendingTaskWaitingForBiosecurity = null
                        currentScreen = AppScreen.TASK_DETAIL
                    }.onFailure {
                        if (it.isMobileSessionExpired()) {
                            showSessionExpiredDialog = true
                        } else {
                            biosecurityRequiredMessage = it.message
                                ?: "Complete the biosecurity log first before opening this task."
                            showBiosecurityRequiredDialog = true
                        }
                    }

                    isCheckingTaskAccess = false
                }
            },
            onCompletedTaskClick = { task ->
                val employeeId = loggedInEmployeeId

                if (employeeId == null) {
                    openCompletedTaskDetail(
                        task = task,
                        submittedFields = task.submittedFields
                    )
                    return@TasksScreen
                }

                coroutineScope.launch {
                    isLoadingSubmittedTaskDetail = true

                    val submittedFields = taskService.getSubmittedTaskFields(
                        accessToken = mobileAccessToken.orEmpty(),
                        taskId = task.id
                    ).getOrElse {
                        if (it.isMobileSessionExpired()) {
                            showSessionExpiredDialog = true
                        }

                        task.submittedFields
                    }

                    isLoadingSubmittedTaskDetail = false

                    openCompletedTaskDetail(
                        task = task,
                        submittedFields = submittedFields
                    )
                }
            }
        )

        AppScreen.TASK_DETAIL -> {
            selectedPendingTask?.let { task ->
                PendingTaskDetailScreen(
                    employeeId = loggedInEmployeeId ?: 0,
                    accessToken = mobileAccessToken.orEmpty(),
                    task = task,
                    onBackClick = {
                        currentScreen = AppScreen.TASKS
                    },
                    onNavigateToDashboard = {
                        currentScreen = AppScreen.DASHBOARD
                    },
                    onNavigateToTasks = {
                        currentScreen = AppScreen.TASKS
                    },
                    onNavigateToProfile = {
                        currentScreen = AppScreen.PROFILE
                    },
                    onGoToBiosecurity = {
                        pendingTaskWaitingForBiosecurity = selectedPendingTask
                        currentScreen = AppScreen.PERSONNEL_LOGS
                    },
                    onSubmit = { notes, photoUri ->
                        val employeeId = loggedInEmployeeId
                            ?: return@PendingTaskDetailScreen Result.failure(
                                IllegalStateException("Missing employee ID.")
                            )

                        val taskId = selectedPendingTask?.id
                            ?: return@PendingTaskDetailScreen Result.failure(
                                IllegalStateException("Missing task ID.")
                            )

                        val result = taskService.submitTaskForApproval(
                            context = context,
                            accessToken = mobileAccessToken.orEmpty(),
                            employeeId = employeeId,
                            taskId = taskId,
                            notes = notes,
                            photoUri = photoUri
                        )

                        result.onSuccess {
                            TaskBackendService.clearTaskCache(employeeId)
                        }.onFailure {
                            if (it.isMobileSessionExpired()) {
                                showSessionExpiredDialog = true
                            }
                        }.map { Unit }
                    }
                )
            }
        }

        AppScreen.COMPLETED_TASK_DETAIL -> {
            selectedCompletedTask?.let { task ->
                CompletedTaskDetailScreen(
                    task = task,
                    onBackClick = { currentScreen = AppScreen.TASKS },
                    onNavigateToDashboard = { currentScreen = AppScreen.DASHBOARD },
                    onNavigateToTasks = { currentScreen = AppScreen.TASKS },
                    onNavigateToProfile = { currentScreen = AppScreen.PROFILE }
                )
            }
        }

        AppScreen.PERSONNEL_LOGS -> PersonnelLogsScreen(
            employeeId = loggedInEmployeeId ?: 0,
            accessToken = mobileAccessToken.orEmpty(),
            lockedTaskId = pendingTaskWaitingForBiosecurity?.id,
            lockedHouseId = pendingTaskWaitingForBiosecurity?.houseId,
            lockedPenId = pendingTaskWaitingForBiosecurity?.penNumber,
            lockedHouseLabel = pendingTaskWaitingForBiosecurity?.houseLabel.orEmpty(),
            lockedPenLabel = pendingTaskWaitingForBiosecurity?.penLabel.orEmpty(),
            onBiosecuritySubmitted = {
                pendingTaskWaitingForBiosecurity?.let { task ->
                    selectedPendingTask = task
                    pendingTaskWaitingForBiosecurity = null
                    currentScreen = AppScreen.TASK_DETAIL
                }
            },
            onBackToBiosecurity = {
                pendingTaskWaitingForBiosecurity = null
                currentScreen = AppScreen.TASKS
            },
            onNavigateToDashboard = {
                pendingTaskWaitingForBiosecurity = null
                currentScreen = AppScreen.DASHBOARD
            },
            onNavigateToTasks = {
                pendingTaskWaitingForBiosecurity = null
                currentScreen = AppScreen.TASKS
            }
        )

        AppScreen.VISITOR -> VisitorScreen(
            employeeId = loggedInEmployeeId ?: 0,
            accessToken = mobileAccessToken.orEmpty(),
            onNavigateToDashboard = {
                currentScreen = AppScreen.DASHBOARD
            },
            onNavigateToTasks = {
                currentScreen = AppScreen.TASKS
            },
            onNavigateToProfile = {
                currentScreen = AppScreen.PROFILE
            }
        )

        AppScreen.PROFILE -> ProfileScreen(
            employeeId = loggedInEmployeeId ?: 0,
            accessToken = mobileAccessToken.orEmpty(),
            onNavigateToDashboard = {
                currentScreen = AppScreen.DASHBOARD
            },
            onNavigateToTasks = {
                currentScreen = AppScreen.TASKS
            },
            onLogout = {
                clearMobileSession()
            }
        )
    }
}