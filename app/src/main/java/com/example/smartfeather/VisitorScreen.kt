package com.example.smartfeather

import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.pm.PackageManager
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.Upload
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.util.Calendar
import com.composables.icons.lucide.ClipboardList
import com.composables.icons.lucide.House
import com.composables.icons.lucide.LayoutDashboard
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.UserRound

private val VisitorManrope = FontFamily(
    Font(R.font.manrope_extralight, FontWeight.ExtraLight),
    Font(R.font.manrope_light, FontWeight.Light),
    Font(R.font.manrope_regular, FontWeight.Normal),
    Font(R.font.manrope_medium, FontWeight.Medium),
    Font(R.font.manrope_semibold, FontWeight.SemiBold),
    Font(R.font.manrope_bold, FontWeight.Bold),
    Font(R.font.manrope_extrabold, FontWeight.ExtraBold),
    Font(R.font.manrope_variablefont_wght, FontWeight.Black)
)

private val VisitorBackground = Color(0xFFF6F3EC)
private val VisitorSurface = Color(0xFFFFFCF7)
private val VisitorSurfaceAlt = Color(0xFFF3EFE7)
private val VisitorAutoField = Color(0xFFE8E3DA)
private val VisitorInk = Color(0xFF121A14)
private val VisitorMuted = Color(0xFF677168)
private val VisitorLine = Color(0xFFD8D0C3)
private val VisitorGreen = Color(0xFF1F7A3A)
private val VisitorDeepGreen = Color(0xFF062717)
private val VisitorForest = Color(0xFF103C28)
private val VisitorTeal = Color(0xFF2E7D6B)
private val VisitorBlue = Color(0xFF3F6F88)
private val VisitorSlate = Color(0xFF49656F)
private val VisitorDanger = Color(0xFFC62828)

private enum class VisitorMode {
    TIME_IN,
    TIME_OUT
}

@Composable
fun VisitorScreen(
    employeeId: Int,
    onNavigateToDashboard: () -> Unit,
    onNavigateToTasks: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    val visitorService = remember { VisitorBackendService() }
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current

    var mode by remember { mutableStateOf(VisitorMode.TIME_IN) }

    var date by remember { mutableStateOf("") }
    var timeIn by remember { mutableStateOf("") }
    var timeOut by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var purpose by remember { mutableStateOf("") }
    var footBath by remember { mutableStateOf(false) }
    var sanitation by remember { mutableStateOf(false) }
    var ppe by remember { mutableStateOf(false) }
    var selectedPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var selectedPhotoUrl by remember { mutableStateOf<String?>(null) }

    var isLoading by remember { mutableStateOf(false) }
    var isLoadingOpenVisitors by remember { mutableStateOf(false) }
    var contentVisible by remember { mutableStateOf(false) }

    var showDialog by remember { mutableStateOf(false) }
    var dialogTitle by remember { mutableStateOf("") }
    var dialogMessage by remember { mutableStateOf("") }

    var openVisitors by remember { mutableStateOf<List<OpenVisitorUiState>>(emptyList()) }
    var selectedVisitorId by remember { mutableStateOf<Int?>(null) }
    var showVisitorPicker by remember { mutableStateOf(false) }

    var cameraImageUri by remember { mutableStateOf<Uri?>(null) }

    fun showModal(title: String, message: String) {
        dialogTitle = title
        dialogMessage = message
        showDialog = true
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            selectedPhotoUri = uri
            selectedPhotoUrl = null
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            selectedPhotoUri = cameraImageUri
            selectedPhotoUrl = null
        }
    }

    val launchCamera = {
        val imageFile = File.createTempFile(
            "visitor_${System.currentTimeMillis()}",
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
                message = "Please allow camera access to take a visitor photo."
            )
        }
    }

    val selectedOpenVisitor = openVisitors.firstOrNull { it.id == selectedVisitorId }

    LaunchedEffect(Unit) {
        delay(120)
        contentVisible = true
    }

    LaunchedEffect(mode) {
        if (mode == VisitorMode.TIME_IN) {
            date = ""
            timeIn = ""
            timeOut = ""
            name = ""
            purpose = ""
            footBath = false
            sanitation = false
            ppe = false
            selectedVisitorId = null
            selectedPhotoUri = null
            selectedPhotoUrl = null
        } else {
            isLoadingOpenVisitors = true
            visitorService.getOpenVisitors(employeeId)
                .onSuccess { visitors ->
                    openVisitors = visitors
                    selectedVisitorId = null
                    date = ""
                    timeIn = ""
                    timeOut = ""
                    name = ""
                    purpose = ""
                    footBath = false
                    sanitation = false
                    ppe = false
                    selectedPhotoUri = null
                    selectedPhotoUrl = null
                }
                .onFailure {
                    showModal(
                        title = "Load Failed",
                        message = it.message ?: "Failed to load open visitors."
                    )
                }
            isLoadingOpenVisitors = false
        }
    }

    fun showDatePicker() {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                date = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    fun showTimePicker(onTimeSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                onTimeSelected(String.format("%02d:%02d", hourOfDay, minute))
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            false
        ).show()
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            containerColor = VisitorSurface,
            shape = RoundedCornerShape(28.dp),
            title = { VisitorDialogTitle(dialogTitle) },
            text = { VisitorDialogBody(dialogMessage) },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    VisitorDialogButtonText("OK", VisitorGreen)
                }
            }
        )
    }

    if (showVisitorPicker) {
        AlertDialog(
            onDismissRequest = { showVisitorPicker = false },
            containerColor = VisitorSurface,
            shape = RoundedCornerShape(28.dp),
            title = { VisitorDialogTitle("Select Visitor") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (openVisitors.isEmpty()) {
                        VisitorDialogBody("No open visitors found.")
                    } else {
                        openVisitors.forEach { visitor ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(VisitorSurfaceAlt)
                                    .clickable {
                                        selectedVisitorId = visitor.id
                                        date = visitor.date
                                        timeIn = visitor.timeIn
                                        name = visitor.name
                                        purpose = visitor.purpose
                                        footBath = visitor.footBath
                                        sanitation = visitor.sanitation
                                        ppe = visitor.ppe
                                        selectedPhotoUri = null
                                        selectedPhotoUrl = visitor.photoUrl
                                        showVisitorPicker = false
                                    }
                                    .padding(horizontal = 14.dp, vertical = 12.dp)
                            ) {
                                Text(
                                    text = visitor.name.ifBlank { "Visitor" },
                                    fontFamily = VisitorManrope,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 14.sp,
                                    color = VisitorInk
                                )

                                Spacer(modifier = Modifier.height(3.dp))

                                Text(
                                    text = "${visitor.date} | ${visitor.timeIn}",
                                    fontFamily = VisitorManrope,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 12.sp,
                                    color = VisitorMuted
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showVisitorPicker = false }) {
                    VisitorDialogButtonText("Close", VisitorGreen)
                }
            }
        )
    }

    Scaffold(
        containerColor = VisitorBackground,
        bottomBar = {
            VisitorBottomNavBar(
                onDashboardClick = onNavigateToDashboard,
                onTasksClick = onNavigateToTasks,
                onProfileClick = onNavigateToProfile
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFFFBF8F1), VisitorBackground, Color(0xFFEDE7DA))
                    )
                )
                .padding(padding)
                .pointerInput(Unit) {
                    detectTapGestures {
                        focusManager.clearFocus()
                    }
                }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .imePadding()
                    .padding(horizontal = 18.dp, vertical = 18.dp)
            ) {
                VisitorHero(onBackClick = onNavigateToDashboard)

                Spacer(modifier = Modifier.height(18.dp))

                AnimatedVisibility(
                    visible = contentVisible,
                    enter = fadeIn(animationSpec = tween(420)) + slideInVertically(
                        animationSpec = tween(420, easing = FastOutSlowInEasing),
                        initialOffsetY = { it / 12 }
                    )
                ) {
                    Column {
                        VisitorSectionPanel {
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                VisitorModeButton(
                                    label = "Time In",
                                    selected = mode == VisitorMode.TIME_IN,
                                    onClick = { mode = VisitorMode.TIME_IN },
                                    modifier = Modifier.weight(1f)
                                )

                                VisitorModeButton(
                                    label = "Time Out",
                                    selected = mode == VisitorMode.TIME_OUT,
                                    onClick = { mode = VisitorMode.TIME_OUT },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        VisitorSectionPanel {
                            VisitorSectionHeader(
                                title = if (mode == VisitorMode.TIME_IN) "Visitor Details" else "Open Visitor",
                                accentColor = VisitorTeal
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            VisitorLabel(if (mode == VisitorMode.TIME_IN) "Name" else "Visitor")
                            Spacer(modifier = Modifier.height(8.dp))

                            if (mode == VisitorMode.TIME_IN) {
                                VisitorInputField(
                                    value = name,
                                    onValueChange = { name = it },
                                    enabled = true,
                                    readOnlyStyle = false
                                )
                            } else {
                                VisitorPickerField(
                                    value = selectedOpenVisitor?.name ?: "",
                                    placeholder = if (isLoadingOpenVisitors) "Loading visitors..." else "Select visitor",
                                    onClick = {
                                        if (openVisitors.isNotEmpty()) {
                                            showVisitorPicker = true
                                        }
                                    },
                                    enabled = openVisitors.isNotEmpty(),
                                    readOnlyStyle = false
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            VisitorLabel("Purpose")
                            Spacer(modifier = Modifier.height(8.dp))
                            VisitorInputField(
                                value = purpose,
                                onValueChange = { purpose = it },
                                enabled = mode == VisitorMode.TIME_IN,
                                readOnlyStyle = mode == VisitorMode.TIME_OUT
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        VisitorSectionPanel {
                            VisitorSectionHeader(
                                title = "Visit Schedule",
                                accentColor = VisitorBlue
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Column(modifier = Modifier.weight(1f)) {
                                    VisitorLabel("Date")
                                    Spacer(modifier = Modifier.height(8.dp))
                                    VisitorPickerField(
                                        value = date,
                                        placeholder = "Pick date",
                                        onClick = { if (mode == VisitorMode.TIME_IN) showDatePicker() },
                                        enabled = mode == VisitorMode.TIME_IN,
                                        readOnlyStyle = mode == VisitorMode.TIME_OUT
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    VisitorLabel("Time In")
                                    Spacer(modifier = Modifier.height(8.dp))
                                    VisitorPickerField(
                                        value = timeIn,
                                        placeholder = "Pick time",
                                        onClick = { if (mode == VisitorMode.TIME_IN) showTimePicker { timeIn = it } },
                                        enabled = mode == VisitorMode.TIME_IN,
                                        readOnlyStyle = mode == VisitorMode.TIME_OUT
                                    )
                                }
                            }

                            if (mode == VisitorMode.TIME_OUT) {
                                Spacer(modifier = Modifier.height(14.dp))

                                VisitorLabel("Time Out")
                                Spacer(modifier = Modifier.height(8.dp))
                                VisitorPickerField(
                                    value = timeOut,
                                    placeholder = "Pick time out",
                                    onClick = { showTimePicker { timeOut = it } },
                                    enabled = true,
                                    readOnlyStyle = false
                                )
                            }
                        }

                        if (mode == VisitorMode.TIME_IN) {
                            Spacer(modifier = Modifier.height(18.dp))

                            VisitorSectionPanel {
                                VisitorSectionHeader(
                                    title = "Visitor Photo",
                                    accentColor = VisitorSlate
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    VisitorActionButton(
                                        label = "Upload",
                                        icon = Icons.Outlined.Upload,
                                        color = VisitorBlue,
                                        modifier = Modifier.weight(1f),
                                        onClick = {
                                            galleryLauncher.launch(
                                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                            )
                                        }
                                    )

                                    VisitorActionButton(
                                        label = "Camera",
                                        icon = Icons.Outlined.PhotoCamera,
                                        color = VisitorSlate,
                                        modifier = Modifier.weight(1f),
                                        onClick = {
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
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                VisitorPhotoPreview(
                                    selectedPhotoUri = selectedPhotoUri,
                                    selectedPhotoUrl = selectedPhotoUrl
                                )
                            }

                            Spacer(modifier = Modifier.height(18.dp))

                            VisitorSectionPanel {
                                VisitorSectionHeader(
                                    title = "Biosecurity Checklist",
                                    accentColor = VisitorGreen
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                VisitorChecklistItem(
                                    label = "Foot Bath",
                                    checked = footBath,
                                    enabled = true,
                                    readOnlyStyle = false,
                                    accentColor = VisitorGreen,
                                    onCheckedChange = { footBath = it }
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                VisitorChecklistItem(
                                    label = "Sanitation",
                                    checked = sanitation,
                                    enabled = true,
                                    readOnlyStyle = false,
                                    accentColor = VisitorTeal,
                                    onCheckedChange = { sanitation = it }
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                VisitorChecklistItem(
                                    label = "PPE",
                                    checked = ppe,
                                    enabled = true,
                                    readOnlyStyle = false,
                                    accentColor = VisitorBlue,
                                    onCheckedChange = { ppe = it }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Button(
                                onClick = {
                                    focusManager.clearFocus()

                                    if (mode == VisitorMode.TIME_IN) {
                                        when {
                                            date.isBlank() -> {
                                                showModal("Missing Date", "Please select a date.")
                                                return@Button
                                            }

                                            timeIn.isBlank() -> {
                                                showModal("Missing Time In", "Please select time in.")
                                                return@Button
                                            }

                                            name.isBlank() -> {
                                                showModal("Missing Name", "Please enter the visitor name.")
                                                return@Button
                                            }

                                            purpose.isBlank() -> {
                                                showModal("Missing Purpose", "Please enter the purpose.")
                                                return@Button
                                            }

                                            !footBath || !sanitation || !ppe -> {
                                                val missingItems = mutableListOf<String>()
                                                if (!footBath) missingItems.add("Foot Bath")
                                                if (!sanitation) missingItems.add("Sanitation")
                                                if (!ppe) missingItems.add("PPE")
                                                showModal(
                                                    "Incomplete Biosecurity",
                                                    "Please complete: ${missingItems.joinToString(", ")}."
                                                )
                                                return@Button
                                            }
                                        }

                                        coroutineScope.launch {
                                            isLoading = true

                                            visitorService.submitVisitorTimeIn(
                                                context = context,
                                                employeeId = employeeId,
                                                date = date,
                                                timeIn = timeIn,
                                                name = name.trim(),
                                                purpose = purpose.trim(),
                                                footBath = footBath,
                                                sanitation = sanitation,
                                                ppe = ppe,
                                                photoUri = selectedPhotoUri
                                            ).onSuccess { success ->
                                                if (success) {
                                                    showModal("Submitted", "Visitor timed in.")
                                                    date = ""
                                                    timeIn = ""
                                                    timeOut = ""
                                                    name = ""
                                                    purpose = ""
                                                    footBath = false
                                                    sanitation = false
                                                    ppe = false
                                                    selectedPhotoUri = null
                                                    selectedPhotoUrl = null
                                                } else {
                                                    showModal("Submission Failed", "Failed to submit visitor time in.")
                                                }
                                            }.onFailure {
                                                showModal(
                                                    "Submission Failed",
                                                    it.message ?: "Failed to submit visitor time in."
                                                )
                                            }

                                            isLoading = false
                                        }
                                    } else {
                                        when {
                                            selectedVisitorId == null -> {
                                                showModal("Missing Visitor", "Please select a visitor to time out.")
                                                return@Button
                                            }

                                            timeOut.isBlank() -> {
                                                showModal("Missing Time Out", "Please select time out.")
                                                return@Button
                                            }
                                        }

                                        coroutineScope.launch {
                                            isLoading = true

                                            visitorService.submitVisitorTimeOut(
                                                employeeId = employeeId,
                                                visitorLogId = selectedVisitorId!!,
                                                timeOut = timeOut
                                            ).onSuccess { success ->
                                                if (success) {
                                                    showModal("Submitted", "Visitor timed out.")
                                                    mode = VisitorMode.TIME_IN
                                                    openVisitors = emptyList()
                                                    selectedVisitorId = null
                                                    date = ""
                                                    timeIn = ""
                                                    timeOut = ""
                                                    name = ""
                                                    purpose = ""
                                                    footBath = false
                                                    sanitation = false
                                                    ppe = false
                                                    selectedPhotoUri = null
                                                    selectedPhotoUrl = null
                                                } else {
                                                    showModal("Submission Failed", "Failed to submit visitor time out.")
                                                }
                                            }.onFailure {
                                                showModal(
                                                    "Submission Failed",
                                                    it.message ?: "Failed to submit visitor time out."
                                                )
                                            }

                                            isLoading = false
                                        }
                                    }
                                },
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = VisitorGreen,
                                    disabledContainerColor = Color(0xFF94A99A)
                                ),
                                modifier = Modifier.height(48.dp),
                                enabled = !isLoading
                            ) {
                                Text(
                                    text = if (isLoading) {
                                        "Submitting..."
                                    } else if (mode == VisitorMode.TIME_IN) {
                                        "Time In"
                                    } else {
                                        "Time Out"
                                    },
                                    color = Color.White,
                                    fontFamily = VisitorManrope,
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
private fun VisitorHero(
    onBackClick: () -> Unit
) {
    val transition = rememberInfiniteTransition(label = "visitorHeroMotion")

    val pulse by transition.animateFloat(
        initialValue = 0.08f,
        targetValue = 0.16f,
        animationSpec = infiniteRepeatable(
            animation = tween(3600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "visitorPulse"
    )

    val drift by transition.animateFloat(
        initialValue = -8f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(4200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "visitorDrift"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(VisitorDeepGreen, VisitorForest, VisitorTeal)
                )
            )
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .offset(x = (18 + drift).dp)
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
                    .clickable { onBackClick() },
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
                text = "Visitors",
                fontFamily = VisitorManrope,
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
private fun VisitorModeButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(if (selected) VisitorGreen else VisitorSurfaceAlt)
            .clickable { onClick() }
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontFamily = VisitorManrope,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 13.sp,
            color = if (selected) Color.White else VisitorGreen
        )
    }
}

@Composable
private fun VisitorSectionPanel(
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .background(VisitorSurface)
            .border(1.dp, VisitorLine.copy(alpha = 0.70f), RoundedCornerShape(26.dp))
            .padding(horizontal = 18.dp, vertical = 18.dp),
        content = content
    )
}

@Composable
private fun VisitorSectionHeader(
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
            fontFamily = VisitorManrope,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 18.sp,
            color = VisitorInk
        )
    }
}

@Composable
private fun VisitorChecklistItem(
    label: String,
    checked: Boolean,
    enabled: Boolean,
    readOnlyStyle: Boolean,
    accentColor: Color,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(
                when {
                    checked && readOnlyStyle -> VisitorAutoField
                    checked -> accentColor.copy(alpha = 0.10f)
                    readOnlyStyle -> VisitorAutoField
                    else -> VisitorSurfaceAlt
                }
            )
            .clickable(enabled = enabled) { onCheckedChange(!checked) }
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
                    if (checked) accentColor else VisitorLine,
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
            fontFamily = VisitorManrope,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 14.sp,
            color = if (readOnlyStyle) VisitorMuted else VisitorInk
        )
    }
}

@Composable
private fun VisitorLabel(text: String) {
    Text(
        text = text,
        fontSize = 13.sp,
        fontFamily = VisitorManrope,
        fontWeight = FontWeight.ExtraBold,
        color = VisitorInk
    )
}

@Composable
private fun VisitorInputField(
    value: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean,
    readOnlyStyle: Boolean
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        textStyle = TextStyle(
            fontFamily = VisitorManrope,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            color = if (readOnlyStyle) VisitorMuted else VisitorInk
        ),
        shape = RoundedCornerShape(18.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = if (readOnlyStyle) VisitorAutoField else VisitorSurfaceAlt,
            unfocusedContainerColor = if (readOnlyStyle) VisitorAutoField else VisitorSurfaceAlt,
            disabledContainerColor = VisitorAutoField,
            focusedBorderColor = if (readOnlyStyle) Color.Transparent else VisitorTeal,
            unfocusedBorderColor = Color.Transparent,
            disabledBorderColor = Color.Transparent,
            focusedTextColor = VisitorInk,
            unfocusedTextColor = VisitorInk,
            disabledTextColor = VisitorMuted,
            cursorColor = VisitorTeal
        )
    )
}

@Composable
private fun VisitorPickerField(
    value: String,
    placeholder: String,
    onClick: () -> Unit,
    enabled: Boolean,
    readOnlyStyle: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(if (readOnlyStyle) VisitorAutoField else VisitorSurfaceAlt)
            .clickable(enabled = enabled) { onClick() }
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
                fontFamily = VisitorManrope,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = when {
                    value.isBlank() -> VisitorMuted
                    readOnlyStyle -> VisitorMuted
                    else -> VisitorInk
                },
                maxLines = 1,
                modifier = Modifier.weight(1f)
            )

            if (enabled && !readOnlyStyle) {
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowDown,
                    contentDescription = placeholder,
                    tint = VisitorTeal,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
private fun VisitorActionButton(
    label: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color),
        modifier = modifier.height(48.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = Color.White,
            modifier = Modifier.size(18.dp)
        )

        Spacer(modifier = Modifier.width(7.dp))

        Text(
            text = label,
            fontFamily = VisitorManrope,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 13.sp,
            color = Color.White
        )
    }
}

@Composable
private fun VisitorPhotoPreview(
    selectedPhotoUri: Uri?,
    selectedPhotoUrl: String?
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(178.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(VisitorSurfaceAlt)
            .border(1.dp, VisitorLine.copy(alpha = 0.70f), RoundedCornerShape(22.dp)),
        contentAlignment = Alignment.Center
    ) {
        when {
            selectedPhotoUri != null -> {
                AsyncImage(
                    model = selectedPhotoUri,
                    contentDescription = "Visitor photo preview",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
            }

            !selectedPhotoUrl.isNullOrBlank() -> {
                AsyncImage(
                    model = selectedPhotoUrl,
                    contentDescription = "Visitor photo preview",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
            }

            else -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Outlined.PhotoCamera,
                        contentDescription = null,
                        tint = VisitorMuted.copy(alpha = 0.72f),
                        modifier = Modifier.size(28.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "No photo selected",
                        fontFamily = VisitorManrope,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = VisitorMuted
                    )
                }
            }
        }
    }
}

@Composable
private fun VisitorDialogTitle(text: String) {
    Text(
        text = text,
        fontFamily = VisitorManrope,
        fontWeight = FontWeight.ExtraBold,
        color = VisitorInk
    )
}

@Composable
private fun VisitorDialogBody(text: String) {
    Text(
        text = text,
        fontFamily = VisitorManrope,
        fontWeight = FontWeight.Medium,
        color = VisitorMuted,
        lineHeight = 21.sp
    )
}

@Composable
private fun VisitorDialogButtonText(
    text: String,
    color: Color
) {
    Text(
        text = text,
        fontFamily = VisitorManrope,
        fontWeight = FontWeight.ExtraBold,
        color = color
    )
}

@Composable
private fun VisitorBottomNavBar(
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
        VisitorBottomNavItem(Lucide.LayoutDashboard, "Dashboard", false, onDashboardClick)
        VisitorBottomNavItem(Lucide.ClipboardList, "Tasks", false, onTasksClick)
        VisitorBottomNavItem(Lucide.UserRound, "Profile", false, onProfileClick)
    }
}

@Composable
private fun VisitorBottomNavItem(
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
            fontFamily = VisitorManrope,
            fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.SemiBold,
            fontSize = 9.sp,
            color = if (selected) Color.White else Color(0xFFCFE8D2),
            textAlign = TextAlign.Center,
            maxLines = 1,
            lineHeight = 10.sp
        )
    }
}