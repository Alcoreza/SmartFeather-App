package com.example.smartfeather

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext

enum class AppScreen {
    LOGIN,
    DASHBOARD,
    TASKS,
    FARM_MANAGEMENT,
    TASK_DETAIL,
    COMPLETED_TASK_DETAIL,
    POPULATION,
    WEIGHT,
    FEEDS_REFILL,
    VITAMINS_REFILL,
    BIOSECURITY,
    DISINFECTION,
    PERSONNEL_LOGS,
    VISITOR,
    NEW_BIRD_BATCH,
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

@Composable
fun SmartFeatherApp() {
    val authService = remember { SupabaseAuthService() }
    val taskService = remember { TaskBackendService() }
    val dashboardService = remember { DashboardBackendService() }
    val context = LocalContext.current

    var currentScreen by remember { mutableStateOf(AppScreen.LOGIN) }
    var loggedInEmployeeId by remember { mutableStateOf<Int?>(null) }

    var selectedPendingTask by remember {
        mutableStateOf<PendingTaskDetailUiState?>(null)
    }
    var selectedCompletedTask by remember {
        mutableStateOf<CompletedTaskDetailUiState?>(null)
    }

    var dashboardUiState by remember { mutableStateOf(placeholderDashboardState()) }

    var selectedEnvironmentHouseId by remember { mutableStateOf<Int?>(null) }
    var selectedEnvironmentPenId by remember { mutableStateOf<Int?>(null) }
    var selectedResourceHouseId by remember { mutableStateOf<Int?>(null) }
    var selectedResourcePenId by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(
        currentScreen,
        loggedInEmployeeId,
        selectedEnvironmentHouseId,
        selectedEnvironmentPenId,
        selectedResourceHouseId,
        selectedResourcePenId
    ) {
        val employeeId = loggedInEmployeeId

        if (currentScreen == AppScreen.DASHBOARD && employeeId != null) {
            dashboardService.getDashboard(
                employeeId = employeeId,
                environmentHouseId = selectedEnvironmentHouseId,
                environmentPenId = selectedEnvironmentPenId,
                resourceHouseId = selectedResourceHouseId,
                resourcePenId = selectedResourcePenId
            ).onSuccess { state ->
                dashboardUiState = state

                selectedEnvironmentHouseId = state.environmentFilter.selectedHouseId
                selectedEnvironmentPenId = state.environmentFilter.selectedPenId
                selectedResourceHouseId = state.resourceFilter.selectedHouseId
                selectedResourcePenId = state.resourceFilter.selectedPenId
            }
        }
    }

    when (currentScreen) {
        AppScreen.LOGIN -> LoginScreen(
            onLoginClick = { username, password ->
                authService.signInFlockman(username = username, password = password)
            },
            onLoginSuccess = { employeeId ->
                loggedInEmployeeId = employeeId
                currentScreen = AppScreen.DASHBOARD
            }
        )

        AppScreen.DASHBOARD -> DashboardScreen(
            onNavigateToTasks = {
                currentScreen = AppScreen.TASKS
            },
            onNavigateToFarmManagement = {
                currentScreen = AppScreen.FARM_MANAGEMENT
            },
            onNavigateToProfile = {
                currentScreen = AppScreen.PROFILE
            },
            onQuickAccessPopulation = {
                currentScreen = AppScreen.POPULATION
            },
            onQuickAccessFeedsRefill = {
                currentScreen = AppScreen.FEEDS_REFILL
            },
            onQuickAccessBiosecurity = {
                currentScreen = AppScreen.BIOSECURITY
            },
            uiState = dashboardUiState,
            onEnvironmentFilterChange = { option: SensorFilterOption ->
                selectedEnvironmentHouseId = option.houseId
                selectedEnvironmentPenId = option.penId

                dashboardUiState = dashboardUiState.copy(
                    environmentFilter = dashboardUiState.environmentFilter.copy(
                        selectedHouseId = option.houseId,
                        selectedPenId = option.penId
                    )
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
            }
        )

        AppScreen.TASKS -> TasksScreen(
            employeeId = loggedInEmployeeId ?: 0,
            onNavigateToDashboard = {
                currentScreen = AppScreen.DASHBOARD
            },
            onNavigateToFarmManagement = {
                currentScreen = AppScreen.FARM_MANAGEMENT
            },
            onNavigateToProfile = {
                currentScreen = AppScreen.PROFILE
            },
            onPendingTaskClick = { task ->
                selectedPendingTask = PendingTaskDetailUiState(
                    id = task.id,
                    title = task.title,
                    description = task.description,
                    timeAssigned = task.assignedLabel.replace("\n", " "),
                    finishBy = task.finishByLabel
                        .removePrefix("Finish by: ")
                        .replace("\n", " "),
                    priorityLabel = task.priority.name.lowercase()
                        .replaceFirstChar { it.uppercase() },
                    priority = task.priority
                )
                currentScreen = AppScreen.TASK_DETAIL
            },
            onCompletedTaskClick = { task ->
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
                    photoUrl = task.photoUrl
                )

                currentScreen = AppScreen.COMPLETED_TASK_DETAIL
            }
        )

        AppScreen.TASK_DETAIL -> {
            selectedPendingTask?.let { task ->
                PendingTaskDetailScreen(
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
                    onGoToBiosecurity = {
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
                            taskId = taskId,
                            employeeId = employeeId,
                            notes = notes,
                            photoUri = photoUri
                        )

                        result.onSuccess {
                            currentScreen = AppScreen.TASKS
                        }.map { Unit }
                    }
                )
            }
        }

        AppScreen.COMPLETED_TASK_DETAIL -> {
            selectedCompletedTask?.let { task ->
                CompletedTaskDetailScreen(
                    task = task,
                    onBackClick = {
                        currentScreen = AppScreen.TASKS
                    },
                    onNavigateToDashboard = {
                        currentScreen = AppScreen.DASHBOARD
                    },
                    onNavigateToTasks = {
                        currentScreen = AppScreen.TASKS
                    }
                )
            }
        }

        AppScreen.POPULATION -> PopulationScreen(
            employeeId = loggedInEmployeeId ?: 0,
            onBackToFarm = {
                currentScreen = AppScreen.FARM_MANAGEMENT
            },
            onNavigateToDashboard = {
                currentScreen = AppScreen.DASHBOARD
            },
            onNavigateToTasks = {
                currentScreen = AppScreen.TASKS
            },
            onGoToBiosecurity = {
                currentScreen = AppScreen.PERSONNEL_LOGS
            }
        )

        AppScreen.WEIGHT -> WeightScreen(
            employeeId = loggedInEmployeeId ?: 0,
            onBackToFarm = {
                currentScreen = AppScreen.FARM_MANAGEMENT
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
                currentScreen = AppScreen.PERSONNEL_LOGS
            }
        )

        AppScreen.FEEDS_REFILL -> FeedsRefillScreen(
            employeeId = loggedInEmployeeId ?: 0,
            onBackToFarm = {
                currentScreen = AppScreen.FARM_MANAGEMENT
            },
            onNavigateToDashboard = {
                currentScreen = AppScreen.DASHBOARD
            },
            onNavigateToTasks = {
                currentScreen = AppScreen.TASKS
            },
            onGoToBiosecurity = {
                currentScreen = AppScreen.PERSONNEL_LOGS
            }
        )

        AppScreen.VITAMINS_REFILL -> VitaminsRefillScreen(
            employeeId = loggedInEmployeeId ?: 0,
            onBackToFarm = {
                currentScreen = AppScreen.FARM_MANAGEMENT
            },
            onNavigateToDashboard = {
                currentScreen = AppScreen.DASHBOARD
            },
            onNavigateToTasks = {
                currentScreen = AppScreen.TASKS
            },
            onGoToBiosecurity = {
                currentScreen = AppScreen.PERSONNEL_LOGS
            }
        )

        AppScreen.BIOSECURITY -> BiosecurityScreen(
            onBackToFarm = {
                currentScreen = AppScreen.FARM_MANAGEMENT
            },
            onNavigateToDashboard = {
                currentScreen = AppScreen.DASHBOARD
            },
            onNavigateToTasks = {
                currentScreen = AppScreen.TASKS
            },
            onDisinfectionClick = {
                currentScreen = AppScreen.DISINFECTION
            },
            onPersonnelLogsClick = {
                currentScreen = AppScreen.PERSONNEL_LOGS
            },
            onVisitorClick = {
                currentScreen = AppScreen.VISITOR
            }
        )

        AppScreen.DISINFECTION -> DisinfectionScreen(
            employeeId = loggedInEmployeeId ?: 0,
            onBackToBiosecurity = {
                currentScreen = AppScreen.BIOSECURITY
            },
            onNavigateToDashboard = {
                currentScreen = AppScreen.DASHBOARD
            },
            onNavigateToTasks = {
                currentScreen = AppScreen.TASKS
            },
            onGoToBiosecurity = {
                currentScreen = AppScreen.PERSONNEL_LOGS
            }
        )

        AppScreen.PERSONNEL_LOGS -> PersonnelLogsScreen(
            employeeId = loggedInEmployeeId ?: 0,
            onBackToBiosecurity = {
                currentScreen = AppScreen.BIOSECURITY
            },
            onNavigateToDashboard = {
                currentScreen = AppScreen.DASHBOARD
            },
            onNavigateToTasks = {
                currentScreen = AppScreen.TASKS
            }
        )

        AppScreen.VISITOR -> VisitorScreen(
            employeeId = loggedInEmployeeId ?: 0,
            onBackToBiosecurity = {
                currentScreen = AppScreen.BIOSECURITY
            },
            onNavigateToDashboard = {
                currentScreen = AppScreen.DASHBOARD
            },
            onNavigateToTasks = {
                currentScreen = AppScreen.TASKS
            }
        )

        AppScreen.NEW_BIRD_BATCH -> NewBirdBatchScreen(
            employeeId = loggedInEmployeeId ?: 0,
            onBackToFarm = {
                currentScreen = AppScreen.FARM_MANAGEMENT
            },
            onNavigateToDashboard = {
                currentScreen = AppScreen.DASHBOARD
            },
            onNavigateToTasks = {
                currentScreen = AppScreen.TASKS
            },
            onGoToBiosecurity = {
                currentScreen = AppScreen.PERSONNEL_LOGS
            }
        )

        AppScreen.PROFILE -> ProfileScreen(
            employeeId = loggedInEmployeeId ?: 0,
            onNavigateToDashboard = {
                currentScreen = AppScreen.DASHBOARD
            },
            onNavigateToTasks = {
                currentScreen = AppScreen.TASKS
            },
            onNavigateToFarmManagement = {
                currentScreen = AppScreen.FARM_MANAGEMENT
            },
            onLogout = {
                loggedInEmployeeId = null
                selectedPendingTask = null
                selectedCompletedTask = null
                dashboardUiState = placeholderDashboardState()
                selectedEnvironmentHouseId = null
                selectedEnvironmentPenId = null
                selectedResourceHouseId = null
                selectedResourcePenId = null
                currentScreen = AppScreen.LOGIN
            }
        )

        AppScreen.FARM_MANAGEMENT -> FarmManagementScreen(
            onNavigateToDashboard = {
                currentScreen = AppScreen.DASHBOARD
            },
            onNavigateToTasks = {
                currentScreen = AppScreen.TASKS
            },
            onNavigateToProfile = {
                currentScreen = AppScreen.PROFILE
            },
            onPopulationClick = {
                currentScreen = AppScreen.POPULATION
            },
            onWeightClick = {
                currentScreen = AppScreen.WEIGHT
            },
            onFeedsRefillClick = {
                currentScreen = AppScreen.FEEDS_REFILL
            },
            onVitaminsRefillClick = {
                currentScreen = AppScreen.VITAMINS_REFILL
            },
            onBiosecurityClick = {
                currentScreen = AppScreen.BIOSECURITY
            },
            onNewBirdBatchClick = {
                currentScreen = AppScreen.NEW_BIRD_BATCH
            }
        )
    }
}