package com.example.smartfeather

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
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

private val LoginPoppins = FontFamily(
    Font(R.font.poppins_regular, FontWeight.Normal),
    Font(R.font.poppins_medium, FontWeight.Medium),
    Font(R.font.poppins_semibold, FontWeight.SemiBold),
    Font(R.font.poppins_bold, FontWeight.Bold)
)

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
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    var currentScreen by remember { mutableStateOf(AppScreen.LOGIN) }
    var loggedInEmployeeId by remember { mutableStateOf<Int?>(null) }
    var selectedPendingTask by remember {
        mutableStateOf<PendingTaskDetailUiState?>(null)
    }
    var selectedCompletedTask by remember {
        mutableStateOf<CompletedTaskDetailUiState?>(null)
    }



    when (currentScreen) {
        AppScreen.LOGIN -> LoginScreen(
            onLoginClick = { employeeId, password ->
                authService.signInFlockman(employeeId = employeeId, password = password)
            },
            onLoginSuccess = { employeeId ->
                loggedInEmployeeId = employeeId.toIntOrNull()
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
            uiState = placeholderDashboardState()
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
                    onSubmit = { notes, photoUri ->
                        val employeeId = loggedInEmployeeId
                            ?: return@PendingTaskDetailScreen Result.failure(IllegalStateException("Missing employee ID."))

                        val taskId = selectedPendingTask?.id
                            ?: return@PendingTaskDetailScreen Result.failure(IllegalStateException("Missing task ID."))

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
            onBackToFarm = {
                currentScreen = AppScreen.FARM_MANAGEMENT
            },
            onNavigateToDashboard = {
                currentScreen = AppScreen.DASHBOARD
            },
            onNavigateToTasks = {
                currentScreen = AppScreen.TASKS
            }
        )

        AppScreen.WEIGHT -> WeightScreen(
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
            }
        )



        AppScreen.FEEDS_REFILL -> FeedsRefillScreen(
            onBackToFarm = {
                currentScreen = AppScreen.FARM_MANAGEMENT
            },
            onNavigateToDashboard = {
                currentScreen = AppScreen.DASHBOARD
            },
            onNavigateToTasks = {
                currentScreen = AppScreen.TASKS
            }
        )

        AppScreen.VITAMINS_REFILL -> VitaminsRefillScreen(
            onBackToFarm = {
                currentScreen = AppScreen.FARM_MANAGEMENT
            },
            onNavigateToDashboard = {
                currentScreen = AppScreen.DASHBOARD
            },
            onNavigateToTasks = {
                currentScreen = AppScreen.TASKS
            }
        )

        AppScreen.BIOSECURITY -> BiosecurityScreen(
            onBackToFarm = { currentScreen = AppScreen.FARM_MANAGEMENT },
            onNavigateToDashboard = { currentScreen = AppScreen.DASHBOARD },
            onNavigateToTasks = { currentScreen = AppScreen.TASKS },
            onDisinfectionClick = { currentScreen = AppScreen.DISINFECTION },
            onPersonnelLogsClick = { currentScreen = AppScreen.PERSONNEL_LOGS },
            onVisitorClick = { currentScreen = AppScreen.VISITOR },
        )

        AppScreen.DISINFECTION -> DisinfectionScreen(
            employeeId = loggedInEmployeeId ?: 0,
            onBackToBiosecurity = { currentScreen = AppScreen.BIOSECURITY },
            onNavigateToDashboard = { currentScreen = AppScreen.DASHBOARD },
            onNavigateToTasks = { currentScreen = AppScreen.TASKS }
        )



        AppScreen.PERSONNEL_LOGS -> PersonnelLogsScreen(
            onBackToBiosecurity = { currentScreen = AppScreen.BIOSECURITY },
            onNavigateToDashboard = { currentScreen = AppScreen.DASHBOARD },
            onNavigateToTasks = { currentScreen = AppScreen.TASKS }
        )

        AppScreen.VISITOR -> VisitorScreen(
            employeeId = loggedInEmployeeId ?: 0,
            onBackToBiosecurity = { currentScreen = AppScreen.BIOSECURITY },
            onNavigateToDashboard = { currentScreen = AppScreen.DASHBOARD },
            onNavigateToTasks = { currentScreen = AppScreen.TASKS }
        )

        AppScreen.NEW_BIRD_BATCH -> NewBirdBatchScreen(
            onBackToFarm = { currentScreen = AppScreen.FARM_MANAGEMENT },
            onNavigateToDashboard = { currentScreen = AppScreen.DASHBOARD },
            onNavigateToTasks = { currentScreen = AppScreen.TASKS }
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

@Composable
fun LoginScreen(
    onLoginClick: suspend (String, String) -> Result<Unit>,
    onLoginSuccess: (String) -> Unit
) {
    var userId by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    val pageBackground = Color(0xFF012B18)
    val deepBackground = Color(0xFF001B10)
    val borderGreen = Color(0xFF55D56E)
    val softGreen = Color(0xFF2F8F45)
    val softGreenDark = Color(0xFF1F6F32)
    val white = Color(0xFFF8F8F6)
    val mutedWhite = Color(0xFFEAF6ED)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(deepBackground, pageBackground)
                )
            )
            .pointerInput(Unit) {
                detectTapGestures {
                    focusManager.clearFocus()
                }
            }
    ) {
        Box(
            modifier = Modifier
                .size(220.dp)
                .align(Alignment.TopStart)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0x223BE46A), Color.Transparent)
                    ),
                    shape = CircleShape
                )
        )

        Box(
            modifier = Modifier
                .size(260.dp)
                .align(Alignment.BottomEnd)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0x163BE46A), Color.Transparent)
                    ),
                    shape = CircleShape
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(34.dp))
                    .background(Color(0x12000000))
                    .border(
                        width = 1.dp,
                        color = borderGreen.copy(alpha = 0.9f),
                        shape = RoundedCornerShape(34.dp)
                    )
                    .padding(horizontal = 28.dp, vertical = 42.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Log In",
                        fontFamily = LoginPoppins,
                        color = white,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(38.dp))

                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Employee ID",
                            fontFamily = LoginPoppins,
                            color = mutedWhite,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = userId,
                            onValueChange = {
                                userId = it
                                errorMessage = null
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions.Default.copy(
                                keyboardType = KeyboardType.Number
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(50.dp),
                            placeholder = {
                                Text(
                                    text = "Enter your employee ID",
                                    fontFamily = LoginPoppins,
                                    color = Color(0xFF8C8C8C),
                                    fontSize = 16.sp
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = white,
                                unfocusedContainerColor = white,
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                focusedTextColor = Color.Black,
                                unfocusedTextColor = Color.Black,
                                cursorColor = softGreen
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Password",
                            fontFamily = LoginPoppins,
                            color = mutedWhite,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = password,
                            onValueChange = {
                                password = it
                                errorMessage = null
                            },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(50.dp),
                            placeholder = {
                                Text(
                                    text = "Enter your password",
                                    fontFamily = LoginPoppins,
                                    color = Color(0xFF8C8C8C),
                                    fontSize = 16.sp
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = white,
                                unfocusedContainerColor = white,
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                focusedTextColor = Color.Black,
                                unfocusedTextColor = Color.Black,
                                cursorColor = softGreen
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(36.dp))

                    errorMessage?.let {
                        Text(
                            text = it,
                            fontFamily = LoginPoppins,
                            color = Color(0xFFFF8B8B),
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            if (userId.isBlank() || password.isBlank()) {
                                errorMessage = "Please provide both employee ID and password."
                                return@Button
                            }
                            if (userId.any { !it.isDigit() }) {
                                errorMessage = "Employee ID should contain numbers only."
                                return@Button
                            }

                            coroutineScope.launch {
                                isLoading = true
                                val result = onLoginClick(userId.trim(), password)
                                isLoading = false
                                result
                                    .onSuccess {
                                        errorMessage = null
                                        onLoginSuccess(userId.trim())
                                    }
                                    .onFailure {
                                        errorMessage = it.message ?: "Unable to sign in."
                                    }
                            }
                        },
                        shape = RoundedCornerShape(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        enabled = !isLoading,
                        modifier = Modifier.width(140.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(50.dp))
                                .background(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(softGreen, softGreenDark)
                                    )
                                )
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isLoading) "Signing In..." else "Go",
                                fontFamily = LoginPoppins,
                                color = white,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LoginScreenPreview() {
    LoginScreen(
        onLoginClick = { _, _ -> Result.success(Unit) },
        onLoginSuccess = { }
    )
}
