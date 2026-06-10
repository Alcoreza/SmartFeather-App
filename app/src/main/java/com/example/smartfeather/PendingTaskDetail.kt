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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Edit
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.composables.icons.lucide.ClipboardList
import com.composables.icons.lucide.LayoutDashboard
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.UserRound
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.Upload
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File
import androidx.compose.foundation.layout.width

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

private val DetailManrope = FontFamily(
    Font(R.font.manrope_extralight, FontWeight.ExtraLight),
    Font(R.font.manrope_light, FontWeight.Light),
    Font(R.font.manrope_regular, FontWeight.Normal),
    Font(R.font.manrope_medium, FontWeight.Medium),
    Font(R.font.manrope_semibold, FontWeight.SemiBold),
    Font(R.font.manrope_bold, FontWeight.Bold),
    Font(R.font.manrope_extrabold, FontWeight.ExtraBold),
    Font(R.font.manrope_variablefont_wght, FontWeight.Black)
)

private val DetailBackground = Color(0xFFF6F3EC)
private val DetailSurface = Color(0xFFFFFCF7)
private val DetailInk = Color(0xFF121A14)
private val DetailMuted = Color(0xFF677168)
private val DetailLine = Color(0xFFD8D0C3)
private val DetailGreen = Color(0xFF1F7A3A)
private val DetailDeepGreen = Color(0xFF062717)
private val DetailGreenTwo = Color(0xFF155C2D)
private val DetailAmber = Color(0xFFE28622)

private fun priorityChipColor(priority: TaskPriority): Color {
    return when (priority) {
        TaskPriority.LOW -> Color(0xFF2F7D46)
        TaskPriority.MID -> DetailAmber
        TaskPriority.HIGH -> Color(0xFFC62B2B)
    }
}

private fun decimalOnly(value: String): String {
    return buildString {
        var dotUsed = false
        value.forEach { char ->
            if (char.isDigit()) {
                append(char)
            } else if (char == '.' && !dotUsed) {
                append(char)
                dotUsed = true
            }
        }
    }
}

@Composable
fun PendingTaskDetailScreen(
    employeeId: Int,
    task: PendingTaskDetailUiState,
    onBackClick: () -> Unit = {},
    onNavigateToDashboard: () -> Unit = {},
    onNavigateToTasks: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onGoToBiosecurity: () -> Unit = {},
    onSubmit: suspend (notes: String, photoUri: Uri?) -> Result<Unit> = { _, _ -> Result.success(Unit) }
) {
    val populationService = remember { PopulationBackendService() }
    val weightService = remember { WeightBackendService() }
    val feedsService = remember { FeedsRefillBackendService() }
    val vitaminsService = remember { VitaminsRefillBackendService() }
    val disinfectionService = remember { DisinfectionBackendService() }
    val penCleaningService = remember { PenCleaningBackendService() }
    val sensorInspectionService = remember { SensorInspectionBackendService() }
    val newBatchService = remember { NewBatchBackendService() }

    val focusManager = LocalFocusManager.current
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val isHatchTask = remember(task.title) { isHatchAndMortalityTask(task.title) }
    val isWeightTask = remember(task.title) { isWeightMonitoringTask(task.title) }
    val isFeedTask = remember(task.title) { isFeedReplenishmentTask(task.title) }
    val isVitaminTask = remember(task.title) { isVitaminsSupplementationTask(task.title) }
    var feedPens by remember(task.id) { mutableStateOf<List<FeedPenOption>>(emptyList()) }

    val feederOptions = remember(feedPens, task.penNumber) {
        feedPens.firstOrNull { it.id == task.penNumber?.toLong() }?.feederOptions ?: emptyList()
    }
    val isPenDisinfectionTaskType = remember(task.title) {
        isPendingPenDisinfectionTaskType(task.title)
    }

    val isPenCleaningTaskType = remember(task.title) {
        isPendingPenCleaningTaskType(task.title)
    }

    val isSensorInspectionTaskType = remember(task.title) {
        isPendingSensorInspectionTaskType(task.title)
    }
    val isChickPlacementTaskType = remember(task.title) {
        isPendingChickPlacementTaskType(task.title)
    }

    val recordedAt = remember(task.id) {
        LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
    }

    var vitaminOptions by remember(task.id) { mutableStateOf<List<VitaminInventoryOption>>(emptyList()) }
    var selectedVitamin by remember(task.id) { mutableStateOf<VitaminInventoryOption?>(null) }
    var vitaminType by remember(task.id) { mutableStateOf("") }
    var bottlesUsed by remember(task.id) { mutableStateOf("") }
    var vitaminExpanded by remember(task.id) { mutableStateOf(false) }

    var eggsHatched by remember(task.id) { mutableStateOf("") }
    var mortality by remember(task.id) { mutableStateOf("") }

    var numberOfFlocks by remember(task.id) { mutableStateOf("") }
    var flocksWithCases by remember(task.id) { mutableStateOf("") }
    var targetWeight by remember(task.id) { mutableStateOf("") }
    var weightSamples by remember(task.id) { mutableStateOf<List<String>>(emptyList()) }

    var feedOptions by remember(task.id) { mutableStateOf<List<FeedInventoryOption>>(emptyList()) }
    var selectedFeed by remember(task.id) { mutableStateOf<FeedInventoryOption?>(null) }
    var feedType by remember(task.id) { mutableStateOf("") }
    var feederNumber by remember(task.id) { mutableStateOf("") }
    var kilograms by remember(task.id) { mutableStateOf("") }
    var feedExpanded by remember(task.id) { mutableStateOf(false) }
    var feederExpanded by remember(task.id) { mutableStateOf(false) }

    var disinfectionActivity by remember(task.id) { mutableStateOf("Pen Disinfection") }
    var disinfectantUsed by remember(task.id) { mutableStateOf("") }

    var cleaningMaterialsUsed by remember(task.id) { mutableStateOf("") }

    var sensorInspectionChecklist by remember(task.id) {
        mutableStateOf(SensorInspectionChecklistState())
    }
    var chickPlacementBatchCode by remember(task.id) { mutableStateOf("") }
    var chickPlacementInitialPopulation by remember(task.id) { mutableStateOf("") }

    var notes by remember(task.id) { mutableStateOf("") }
    var selectedPhotoUri by remember(task.id) { mutableStateOf<Uri?>(null) }
    var cameraImageUri by remember(task.id) { mutableStateOf<Uri?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }
    var contentVisible by remember { mutableStateOf(false) }

    var showDialog by remember { mutableStateOf(false) }
    var dialogTitle by remember { mutableStateOf("") }
    var dialogMessage by remember { mutableStateOf("") }
    var dialogAction by remember { mutableStateOf<(() -> Unit)?>(null) }

    fun showModal(title: String, message: String, action: (() -> Unit)? = null) {
        dialogTitle = title
        dialogMessage = message
        dialogAction = action
        showDialog = true
    }

    LaunchedEffect(task.id) {
        contentVisible = false
        delay(120)
        contentVisible = true
    }

    LaunchedEffect(task.id, isFeedTask) {
        if (isFeedTask) {
            feedsService.getFeedInventoryOptions()
                .onSuccess { feedOptions = it }
                .onFailure {
                    showModal(
                        title = "Feed Inventory Error",
                        message = it.message ?: "Failed to load active feed options."
                    )
                }

            feedsService.getFeedsContext(employeeId)
                .onSuccess { context ->
                    feedPens = context.pens
                }
                .onFailure {
                    showModal(
                        title = "Feeder Options Error",
                        message = it.message ?: "Failed to load feeder options."
                    )
                }
        }
    }

    LaunchedEffect(task.id, isVitaminTask) {
        if (isVitaminTask) {
            vitaminsService.getVitaminInventoryOptions()
                .onSuccess { vitaminOptions = it }
                .onFailure {
                    showModal(
                        title = "Vitamin Inventory Error",
                        message = it.message ?: "Failed to load active vitamin options."
                    )
                }
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            selectedPhotoUri = uri
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            selectedPhotoUri = cameraImageUri
        }
    }

    val launchCamera = {
        val imageFile = File.createTempFile(
            "task_${task.id}_${System.currentTimeMillis()}",
            ".jpg",
            context.cacheDir
        )

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            imageFile
        )

        cameraImageUri = uri
        cameraLauncher.launch(uri)
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            launchCamera()
        } else {
            showModal(
                title = "Camera Permission Required",
                message = "Please allow camera access to take a proof photo."
            )
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = {
                showDialog = false
                dialogAction = null
            },
            containerColor = DetailSurface,
            shape = RoundedCornerShape(28.dp),
            title = {
                Text(
                    text = dialogTitle,
                    fontFamily = DetailManrope,
                    fontWeight = FontWeight.ExtraBold,
                    color = DetailInk
                )
            },
            text = {
                Text(
                    text = dialogMessage,
                    fontFamily = DetailManrope,
                    fontWeight = FontWeight.Medium,
                    color = DetailMuted,
                    lineHeight = 21.sp
                )
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
                    Text(
                        text = "OK",
                        fontFamily = DetailManrope,
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
                onTasksClick = onNavigateToTasks,
                onProfileClick = onNavigateToProfile
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

                        Spacer(modifier = Modifier.height(18.dp))

                        if (isHatchTask) {
                            PendingHatchMortalitySection(
                                task = task,
                                eggsHatched = eggsHatched,
                                mortality = mortality,
                                recordedAt = recordedAt,
                                onEggsChange = { value ->
                                    eggsHatched = value.filter { it.isDigit() }
                                },
                                onMortalityChange = { value ->
                                    mortality = value.filter { it.isDigit() }
                                }
                            )

                            Spacer(modifier = Modifier.height(18.dp))
                        }

                        if (isWeightTask) {
                            PendingWeightMonitoringSection(
                                task = task,
                                numberOfFlocks = numberOfFlocks,
                                flocksWithCases = flocksWithCases,
                                targetWeight = targetWeight,
                                weightSamples = weightSamples,
                                recordedAt = recordedAt,
                                onNumberOfFlocksChange = { value ->
                                    val digitsOnly = value.filter { it.isDigit() }
                                    numberOfFlocks = digitsOnly
                                    val count = digitsOnly.toIntOrNull() ?: 0
                                    weightSamples = if (count > 0) {
                                        List(count) { index -> weightSamples.getOrNull(index) ?: "" }
                                    } else {
                                        emptyList()
                                    }
                                },
                                onFlocksWithCasesChange = { value ->
                                    flocksWithCases = value.filter { it.isDigit() }
                                },
                                onTargetWeightChange = { value ->
                                    targetWeight = decimalOnly(value)
                                },
                                onWeightSampleChange = { index, value ->
                                    weightSamples = weightSamples.mapIndexed { sampleIndex, oldValue ->
                                        if (sampleIndex == index) decimalOnly(value) else oldValue
                                    }
                                }
                            )

                            Spacer(modifier = Modifier.height(18.dp))
                        }

                        if (isFeedTask) {
                            PendingFeedReplenishmentSection(
                                task = task,
                                feedType = feedType,
                                feedOptions = feedOptions,
                                feederNumber = feederNumber,
                                kilograms = kilograms,
                                feederOptions = feederOptions,
                                feedExpanded = feedExpanded,
                                feederExpanded = feederExpanded,
                                recordedAt = recordedAt,
                                onFeedExpandedChange = { feedExpanded = it },
                                onFeederExpandedChange = { feederExpanded = it },
                                onFeedSelected = { option ->
                                    selectedFeed = option
                                    feedType = option.itemName
                                    feedExpanded = false
                                },
                                onFeederSelected = { selected ->
                                    feederNumber = selected
                                    feederExpanded = false
                                },
                                onKilogramsChange = { value ->
                                    kilograms = value.filter { it.isDigit() }
                                }
                            )

                            Spacer(modifier = Modifier.height(18.dp))
                        }

                        if (isVitaminTask) {
                            PendingVitaminsSupplementationSection(
                                task = task,
                                vitaminType = vitaminType,
                                vitaminOptions = vitaminOptions,
                                bottlesUsed = bottlesUsed,
                                vitaminExpanded = vitaminExpanded,
                                recordedAt = recordedAt,
                                onVitaminExpandedChange = { vitaminExpanded = it },
                                onVitaminSelected = { option ->
                                    selectedVitamin = option
                                    vitaminType = option.itemName
                                    vitaminExpanded = false
                                },
                                onBottlesUsedChange = { value ->
                                    bottlesUsed = value.filter { it.isDigit() }
                                }
                            )

                            Spacer(modifier = Modifier.height(18.dp))
                        }

                        if (isPenDisinfectionTaskType) {
                            PendingPenDisinfectionTaskForm(
                                task = task,
                                activity = disinfectionActivity,
                                disinfectantUsed = disinfectantUsed,
                                recordedAt = recordedAt,
                                onActivityChange = { disinfectionActivity = it },
                                onDisinfectantUsedChange = { disinfectantUsed = it }
                            )

                            Spacer(modifier = Modifier.height(18.dp))
                        }

                        if (isPenCleaningTaskType) {
                            PendingPenCleaningTaskForm(
                                task = task,
                                materialsUsed = cleaningMaterialsUsed,
                                recordedAt = recordedAt,
                                onMaterialsUsedChange = { cleaningMaterialsUsed = it }
                            )

                            Spacer(modifier = Modifier.height(18.dp))
                        }

                        if (isSensorInspectionTaskType) {
                            PendingSensorInspectionTaskForm(
                                task = task,
                                checklist = sensorInspectionChecklist,
                                recordedAt = recordedAt,
                                onChecklistChange = { sensorInspectionChecklist = it }
                            )

                            Spacer(modifier = Modifier.height(18.dp))
                        }

                        if (isChickPlacementTaskType) {
                            PendingChickPlacementTaskForm(
                                task = task,
                                batchCode = chickPlacementBatchCode,
                                initialPopulation = chickPlacementInitialPopulation,
                                recordedAt = recordedAt,
                                onBatchCodeChange = { chickPlacementBatchCode = it },
                                onInitialPopulationChange = { chickPlacementInitialPopulation = it }
                            )

                            Spacer(modifier = Modifier.height(18.dp))
                        }

                        SubmissionWorkspace(
                            selectedPhotoUri = selectedPhotoUri,
                            notes = notes,
                            onNotesChange = { notes = it },
                            onUploadClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(
                                        ActivityResultContracts.PickVisualMedia.ImageOnly
                                    )
                                )
                            },
                            onCameraClick = {
                                if (
                                    ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.CAMERA
                                    ) == PackageManager.PERMISSION_GRANTED
                                ) {
                                    launchCamera()
                                } else {
                                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                }
                            }
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        Button(
                            onClick = {
                                focusManager.clearFocus()

                                if (isHatchTask) {
                                    if (task.houseId == null || task.penNumber == null) {
                                        showModal("Missing Assignment", "This task is missing its assigned house or pen.")
                                        return@Button
                                    }

                                    if (eggsHatched.isBlank()) {
                                        showModal("Eggs Hatched Required", "Please enter eggs hatched. Enter 0 if there are no newly hatched eggs.")
                                        return@Button
                                    }

                                    if (mortality.isBlank()) {
                                        showModal("Mortality Required", "Please enter mortality. Enter 0 if there are no mortalities.")
                                        return@Button
                                    }

                                    val eggsValue = eggsHatched.toIntOrNull()
                                    if (eggsValue == null || eggsValue < 0) {
                                        showModal("Invalid Eggs Hatched", "Eggs hatched must be 0 or higher.")
                                        return@Button
                                    }

                                    val mortalityValue = mortality.toIntOrNull()
                                    if (mortalityValue == null || mortalityValue < 0) {
                                        showModal("Invalid Mortality", "Mortality must be 0 or higher.")
                                        return@Button
                                    }
                                }

                                if (isWeightTask) {
                                    if (task.houseId == null || task.penNumber == null) {
                                        showModal("Missing Assignment", "This task is missing its assigned house or pen.")
                                        return@Button
                                    }

                                    val flockCount = numberOfFlocks.toIntOrNull()
                                    val casesCount = flocksWithCases.toIntOrNull()
                                    val targetValue = targetWeight.toDoubleOrNull()
                                    val weightValues = weightSamples.map { it.toDoubleOrNull() }

                                    when {
                                        flockCount == null || flockCount <= 0 -> {
                                            showModal("Number of Flocks Required", "Please enter the number of flocks sampled.")
                                            return@Button
                                        }

                                        casesCount == null || casesCount < 0 -> {
                                            showModal("Cases Required", "Please enter the number of flocks with cases. Use 0 if there are none.")
                                            return@Button
                                        }

                                        casesCount > flockCount -> {
                                            showModal("Invalid Cases", "Flocks with cases cannot be greater than the number of flocks sampled.")
                                            return@Button
                                        }

                                        targetValue == null || targetValue <= 0.0 -> {
                                            showModal("Target Weight Required", "Please enter a valid target weight.")
                                            return@Button
                                        }

                                        weightSamples.size != flockCount || weightSamples.any { it.isBlank() } -> {
                                            showModal("Weight Samples Required", "Please enter one weight sample for every flock.")
                                            return@Button
                                        }

                                        weightValues.any { it == null || it <= 0.0 } -> {
                                            showModal("Invalid Weight Sample", "Please enter valid weight values for all flocks.")
                                            return@Button
                                        }
                                    }
                                }

                                if (isFeedTask) {
                                    if (task.houseId == null || task.penNumber == null) {
                                        showModal("Missing Assignment", "This task is missing its assigned house or pen.")
                                        return@Button
                                    }

                                    if (selectedFeed == null) {
                                        showModal("Feed Required", "Please select the feed type.")
                                        return@Button
                                    }

                                    val feederValue = feederNumber.toIntOrNull()
                                    if (feederValue == null || feederValue <= 0) {
                                        showModal("Feeder Required", "Please select a valid feeder number.")
                                        return@Button
                                    }

                                    val kilogramsValue = kilograms.toIntOrNull()
                                    if (kilogramsValue == null || kilogramsValue <= 0) {
                                        showModal("Kilograms Required", "Please enter the kilograms refilled.")
                                        return@Button
                                    }
                                }

                                if (isVitaminTask) {
                                    if (task.houseId == null || task.penNumber == null) {
                                        showModal("Missing Assignment", "This task is missing its assigned house or pen.")
                                        return@Button
                                    }

                                    if (selectedVitamin == null) {
                                        showModal("Vitamins Required", "Please select the type of vitamins.")
                                        return@Button
                                    }

                                    val bottlesValue = bottlesUsed.toIntOrNull()
                                    if (bottlesValue == null || bottlesValue <= 0) {
                                        showModal("Bottles Required", "Please enter a valid bottle count.")
                                        return@Button
                                    }
                                }

                                if (isPenDisinfectionTaskType) {
                                    if (task.houseId == null || task.penNumber == null) {
                                        showModal("Missing Assignment", "This task is missing its assigned house or pen.")
                                        return@Button
                                    }

                                    if (disinfectionActivity.isBlank()) {
                                        showModal("Activity Required", "Please enter the disinfection activity.")
                                        return@Button
                                    }

                                    if (disinfectantUsed.isBlank()) {
                                        showModal("Disinfectant Required", "Please enter the disinfectant used.")
                                        return@Button
                                    }
                                }

                                if (isPenCleaningTaskType) {
                                    if (task.houseId == null || task.penNumber == null) {
                                        showModal("Missing Assignment", "This task is missing its assigned house or pen.")
                                        return@Button
                                    }

                                    if (cleaningMaterialsUsed.isBlank()) {
                                        showModal("Materials Required", "Please enter the cleaning materials used.")
                                        return@Button
                                    }
                                }

                                if (isSensorInspectionTaskType) {
                                    if (task.houseId == null || task.penNumber == null) {
                                        showModal("Missing Assignment", "This task is missing its assigned house or pen.")
                                        return@Button
                                    }
                                }

                                if (isChickPlacementTaskType) {
                                    if (task.houseId == null || task.penNumber == null) {
                                        showModal("Missing Assignment", "This task is missing its assigned house or pen.")
                                        return@Button
                                    }

                                    if (chickPlacementBatchCode.isBlank()) {
                                        showModal("Batch Code Required", "Please enter the batch code.")
                                        return@Button
                                    }

                                    val populationValue = chickPlacementInitialPopulation.toIntOrNull()
                                    if (populationValue == null || populationValue <= 0) {
                                        showModal("Population Required", "Please enter a valid initial population.")
                                        return@Button
                                    }
                                }

                                if (selectedPhotoUri == null) {
                                    showModal("Proof Photo Required", "Please upload a proof photo before submitting this task.")
                                    return@Button
                                }

                                coroutineScope.launch {
                                    isSubmitting = true

                                    val formResult = when {
                                        isHatchTask -> {
                                            populationService.submitPopulation(
                                                employeeId = employeeId,
                                                houseId = task.houseId?.toLong()
                                                    ?: return@launch showModal("Missing Assignment", "This task is missing its assigned house."),
                                                penNumber = null,
                                                eggsHatched = eggsHatched.toInt(),
                                                mortality = mortality.toInt(),
                                                recordedAt = recordedAt,
                                                taskId = task.id,
                                                penId = task.penNumber?.toLong()
                                                    ?: return@launch showModal("Missing Assignment", "This task is missing its assigned pen.")
                                            )
                                        }

                                        isWeightTask -> {
                                            weightService.submitWeightSamplingTask(
                                                employeeId = employeeId,
                                                taskId = task.id,
                                                houseId = task.houseId?.toLong()
                                                    ?: return@launch showModal("Missing Assignment", "This task is missing its assigned house."),
                                                penId = task.penNumber?.toLong()
                                                    ?: return@launch showModal("Missing Assignment", "This task is missing its assigned pen."),
                                                numberOfFlocks = numberOfFlocks.toInt(),
                                                flocksWithCases = flocksWithCases.toInt(),
                                                targetWeight = targetWeight.toDouble(),
                                                weights = weightSamples.mapNotNull { it.toDoubleOrNull() },
                                                recordedAt = recordedAt
                                            ).map { it.success == true }
                                        }

                                        isFeedTask -> {
                                            feedsService.submitFeedReplenishmentTask(
                                                employeeId = employeeId,
                                                taskId = task.id,
                                                inventoryId = selectedFeed?.id
                                                    ?: return@launch showModal("Feed Required", "Please select the feed type."),
                                                houseId = task.houseId?.toLong()
                                                    ?: return@launch showModal("Missing Assignment", "This task is missing its assigned house."),
                                                penId = task.penNumber?.toLong()
                                                    ?: return@launch showModal("Missing Assignment", "This task is missing its assigned pen."),
                                                feederNumber = feederNumber.toInt(),
                                                kilograms = kilograms.toInt(),
                                                recordedAt = recordedAt
                                            )
                                        }

                                        isVitaminTask -> {
                                            vitaminsService.submitVitaminsSupplementationTask(
                                                employeeId = employeeId,
                                                taskId = task.id,
                                                inventoryId = selectedVitamin?.id
                                                    ?: return@launch showModal("Vitamins Required", "Please select the type of vitamins."),
                                                houseId = task.houseId?.toLong()
                                                    ?: return@launch showModal("Missing Assignment", "This task is missing its assigned house."),
                                                penId = task.penNumber?.toLong()
                                                    ?: return@launch showModal("Missing Assignment", "This task is missing its assigned pen."),
                                                bottles = bottlesUsed.toInt(),
                                                recordedAt = recordedAt
                                            )
                                        }

                                        isPenDisinfectionTaskType -> {
                                            disinfectionService.submitPenDisinfectionTask(
                                                employeeId = employeeId,
                                                taskId = task.id,
                                                houseId = task.houseId?.toLong()
                                                    ?: return@launch showModal("Missing Assignment", "This task is missing its assigned house."),
                                                penId = task.penNumber?.toLong()
                                                    ?: return@launch showModal("Missing Assignment", "This task is missing its assigned pen."),
                                                activity = disinfectionActivity.trim(),
                                                disinfectantUsed = disinfectantUsed.trim(),
                                                recordedAt = recordedAt
                                            )
                                        }

                                        isPenCleaningTaskType -> {
                                            penCleaningService.submitPenCleaningTask(
                                                employeeId = employeeId,
                                                taskId = task.id,
                                                houseId = task.houseId?.toLong()
                                                    ?: return@launch showModal("Missing Assignment", "This task is missing its assigned house."),
                                                penId = task.penNumber?.toLong()
                                                    ?: return@launch showModal("Missing Assignment", "This task is missing its assigned pen."),
                                                materialsUsed = cleaningMaterialsUsed.trim(),
                                                recordedAt = recordedAt
                                            )
                                        }

                                        isSensorInspectionTaskType -> {
                                            sensorInspectionService.submitSensorInspectionTask(
                                                employeeId = employeeId,
                                                taskId = task.id,
                                                houseId = task.houseId?.toLong()
                                                    ?: return@launch showModal("Missing Assignment", "This task is missing its assigned house."),
                                                penId = task.penNumber?.toLong()
                                                    ?: return@launch showModal("Missing Assignment", "This task is missing its assigned pen."),
                                                checklist = sensorInspectionChecklist,
                                                recordedAt = recordedAt
                                            )
                                        }

                                        isChickPlacementTaskType -> {
                                            newBatchService.submitChickPlacementTask(
                                                employeeId = employeeId,
                                                taskId = task.id,
                                                batchCode = chickPlacementBatchCode.trim(),
                                                houseId = task.houseId?.toLong()
                                                    ?: return@launch showModal("Missing Assignment", "This task is missing its assigned house."),
                                                penId = task.penNumber?.toLong()
                                                    ?: return@launch showModal("Missing Assignment", "This task is missing its assigned pen."),
                                                initialPopulation = chickPlacementInitialPopulation.toInt(),
                                                recordedAt = recordedAt
                                            )
                                        }

                                        else -> Result.success(true)
                                    }

                                    formResult.onSuccess { saved ->
                                        if (!saved) {
                                            showModal("Submission Failed", "The task data was not saved.")
                                            isSubmitting = false
                                            return@launch
                                        }

                                        onSubmit(notes, selectedPhotoUri)
                                            .onSuccess {
                                                showModal(
                                                    title = "Task Submitted",
                                                    message = "Task submitted for approval successfully.",
                                                    action = onNavigateToTasks
                                                )
                                            }
                                            .onFailure {
                                                val message = it.message ?: "Failed to submit task."

                                                if (
                                                    message.contains("personnel biosecurity", ignoreCase = true) ||
                                                    message.contains("scan IN", ignoreCase = true) ||
                                                    message.contains("biosecurity", ignoreCase = true)
                                                ) {
                                                    showModal("Biosecurity Required", message, onGoToBiosecurity)
                                                } else {
                                                    showModal("Submission Failed", message)
                                                }
                                            }
                                    }.onFailure {
                                        showModal("Submission Failed", it.message ?: "Failed to save task data.")
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
                                fontFamily = DetailManrope,
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
private fun DetailTopBar(onBackClick: () -> Unit) {
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
            fontFamily = DetailManrope,
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
        Row(verticalAlignment = Alignment.CenterVertically) {
            StatusLabel("Pending Task", DetailAmber, Modifier.weight(1f))
            PriorityBadge(task.priorityLabel, priorityChipColor(task.priority))
        }

        Spacer(modifier = Modifier.height(22.dp))

        Text(
            text = task.title,
            fontFamily = DetailManrope,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 28.sp,
            color = Color.White,
            lineHeight = 32.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            TimeBlock("Assigned", task.timeAssigned, Modifier.weight(1f))
            TimeBlock("Finish by", task.finishBy, Modifier.weight(1f))
        }
    }
}

@Composable
private fun StatusLabel(text: String, color: Color, modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(9.dp)
                .clip(CircleShape)
                .background(color)
        )

        Spacer(modifier = Modifier.size(8.dp))

        Text(
            text = text,
            fontFamily = DetailManrope,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 13.sp,
            color = Color.White
        )
    }
}

@Composable
private fun PriorityBadge(label: String, color: Color) {
    Column(horizontalAlignment = Alignment.End) {
        Text(
            text = "Priority",
            fontFamily = DetailManrope,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp,
            color = Color.White.copy(alpha = 0.58f)
        )

        Text(
            text = label,
            fontFamily = DetailManrope,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 14.sp,
            color = color
        )
    }
}

private fun isPendingPenCleaningTaskType(title: String): Boolean {
    val normalized = title.trim().lowercase()
    return normalized == "pen cleaning"
}

private fun isPendingSensorInspectionTaskType(title: String): Boolean {
    val normalized = title.trim().lowercase()
    return normalized == "sensor inspection"
}

private fun isPendingChickPlacementTaskType(title: String): Boolean {
    val normalized = title.trim().lowercase()
    return normalized == "chick placement"
}

@Composable
private fun TimeBlock(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White.copy(alpha = 0.10f))
            .border(1.dp, Color.White.copy(alpha = 0.13f), RoundedCornerShape(18.dp))
            .padding(horizontal = 13.dp, vertical = 12.dp)
    ) {
        Text(
            text = label,
            fontFamily = DetailManrope,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            color = Color.White.copy(alpha = 0.64f)
        )

        Spacer(modifier = Modifier.height(5.dp))

        Text(
            text = value.ifBlank { "-" },
            fontFamily = DetailManrope,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 15.sp,
            color = Color.White,
            lineHeight = 19.sp
        )
    }
}

@Composable
private fun WorkBriefSection(task: PendingTaskDetailUiState) {
    DetailSectionPanel {
        DetailSectionHeaderRow("Task Instructions", DetailGreen)

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = task.description,
            fontFamily = DetailManrope,
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
    onUploadClick: () -> Unit,
    onCameraClick: () -> Unit
) {
    DetailSectionPanel {
        DetailSectionHeaderRow("Submission", DetailAmber)

        Spacer(modifier = Modifier.height(16.dp))

        DetailDivider()

        Spacer(modifier = Modifier.height(16.dp))

        PhotoUploadActions(
            onUploadClick = onUploadClick,
            onCameraClick = onCameraClick
        )

        Spacer(modifier = Modifier.height(14.dp))

        PhotoUploadArea(selectedPhotoUri)

        Spacer(modifier = Modifier.height(14.dp))

        NotesInput(notes, onNotesChange)
    }
}

@Composable
private fun PhotoUploadActions(
    onUploadClick: () -> Unit,
    onCameraClick: () -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        PhotoActionButton(
            label = "Upload",
            icon = Icons.Outlined.Upload,
            color = DetailGreen,
            modifier = Modifier.weight(1f),
            onClick = onUploadClick
        )

        PhotoActionButton(
            label = "Camera",
            icon = Icons.Outlined.PhotoCamera,
            color = DetailAmber,
            modifier = Modifier.weight(1f),
            onClick = onCameraClick
        )
    }
}

@Composable
private fun PhotoActionButton(
    label: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(color.copy(alpha = 0.12f))
            .border(1.dp, color.copy(alpha = 0.22f), RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(19.dp)
        )

        Spacer(modifier = Modifier.size(8.dp))

        Text(
            text = label,
            fontFamily = DetailManrope,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 13.sp,
            color = color
        )
    }
}

@Composable
private fun DetailSectionPanel(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.94f),
                        DetailSurface.copy(alpha = 0.98f)
                    )
                )
            )
            .border(1.dp, DetailLine.copy(alpha = 0.82f), RoundedCornerShape(26.dp))
            .padding(horizontal = 18.dp, vertical = 18.dp),
        content = content
    )
}

@Composable
private fun DetailSectionHeaderRow(title: String, accentColor: Color) {
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
            fontFamily = DetailManrope,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 18.sp,
            color = DetailInk
        )
    }
}

@Composable
private fun DetailDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(
                Brush.horizontalGradient(
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
private fun PhotoUploadArea(selectedPhotoUri: Uri?) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(184.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(DetailSurface)
            .border(1.dp, DetailLine, RoundedCornerShape(24.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (selectedPhotoUri != null) {
            AsyncImage(
                model = selectedPhotoUri,
                contentDescription = "Selected proof photo",
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(DetailGreen.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Upload,
                        contentDescription = "Proof photo",
                        tint = DetailGreen,
                        modifier = Modifier.size(23.dp)
                    )
                }

                Spacer(modifier = Modifier.height(11.dp))

                Text(
                    text = "Proof photo required",
                    fontFamily = DetailManrope,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp,
                    color = DetailInk
                )

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = "Upload from gallery or take a photo",
                    fontFamily = DetailManrope,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    color = DetailMuted
                )
            }
        }
    }
}

@Composable
private fun NotesInput(notes: String, onNotesChange: (String) -> Unit) {
    OutlinedTextField(
        value = notes,
        onValueChange = onNotesChange,
        modifier = Modifier
            .fillMaxWidth()
            .height(136.dp),
        shape = RoundedCornerShape(22.dp),
        textStyle = TextStyle(
            fontFamily = DetailManrope,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            color = DetailInk
        ),
        placeholder = {
            Text(
                text = "Add optional notes",
                fontFamily = DetailManrope,
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
private fun PendingDetailSkeleton() {
    val alpha = skeletonAlpha()

    Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
        SkeletonBox(
            modifier = Modifier
                .fillMaxWidth()
                .height(238.dp),
            alpha = alpha,
            color = DetailDeepGreen,
            shape = RoundedCornerShape(30.dp)
        )

        SkeletonBox(
            modifier = Modifier
                .fillMaxWidth()
                .height(144.dp),
            alpha = alpha,
            color = Color(0xFFD9D2C6),
            shape = RoundedCornerShape(26.dp)
        )

        SkeletonBox(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            alpha = alpha,
            color = Color(0xFFD9D2C6),
            shape = RoundedCornerShape(26.dp)
        )

        SkeletonBox(
            modifier = Modifier
                .fillMaxWidth()
                .height(356.dp),
            alpha = alpha,
            color = Color(0xFFD9D2C6),
            shape = RoundedCornerShape(26.dp)
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
    onTasksClick: () -> Unit,
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
        DetailNavItem(Lucide.LayoutDashboard, "Dashboard", false, onDashboardClick)
        DetailNavItem(Lucide.ClipboardList, "Tasks", true, onTasksClick)
        DetailNavItem(Lucide.UserRound, "Profile", false, onProfileClick)
    }
}

private fun isPendingPenDisinfectionTaskType(title: String): Boolean {
    val normalized = title.trim().lowercase()
    return normalized == "pen disinfection"
}

@Composable
private fun DetailNavItem(
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
            fontFamily = DetailManrope,
            fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.SemiBold,
            fontSize = 9.sp,
            color = if (selected) Color.White else Color(0xFFCFE8D2),
            textAlign = TextAlign.Center,
            lineHeight = 11.sp
        )
    }
}