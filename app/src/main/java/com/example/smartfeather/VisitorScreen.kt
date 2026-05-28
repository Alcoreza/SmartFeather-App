package com.example.smartfeather

import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.List
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import java.io.File
import java.util.Calendar

private val VisitorPoppins = FontFamily(
    Font(R.font.poppins_regular, FontWeight.Normal),
    Font(R.font.poppins_medium, FontWeight.Medium),
    Font(R.font.poppins_semibold, FontWeight.SemiBold),
    Font(R.font.poppins_bold, FontWeight.Bold)
)

private enum class VisitorMode {
    TIME_IN,
    TIME_OUT
}

@Composable
fun VisitorScreen(
    employeeId: Int,
    onBackToBiosecurity: () -> Unit,
    onNavigateToDashboard: () -> Unit,
    onNavigateToTasks: () -> Unit
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
    var showDialog by remember { mutableStateOf(false) }
    var dialogTitle by remember { mutableStateOf("") }
    var dialogMessage by remember { mutableStateOf("") }

    var openVisitors by remember { mutableStateOf<List<OpenVisitorUiState>>(emptyList()) }
    var selectedVisitorId by remember { mutableStateOf<Int?>(null) }
    var showVisitorPicker by remember { mutableStateOf(false) }

    var cameraImageUri by remember { mutableStateOf<Uri?>(null) }

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
            dialogTitle = "Camera Permission Required"
            dialogMessage = "Please allow camera access to take a visitor photo."
            showDialog = true
        }
    }

    val selectedOpenVisitor = openVisitors.firstOrNull { it.id == selectedVisitorId }

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
                    dialogTitle = "Load Failed"
                    dialogMessage = it.message ?: "Failed to load open visitors."
                    showDialog = true
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
            title = {
                Text(
                    text = dialogTitle,
                    fontFamily = VisitorPoppins,
                    fontWeight = FontWeight.SemiBold
                )
            },
            text = {
                Text(
                    text = dialogMessage,
                    fontFamily = VisitorPoppins
                )
            },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("OK", fontFamily = VisitorPoppins)
                }
            }
        )
    }

    if (showVisitorPicker) {
        AlertDialog(
            onDismissRequest = { showVisitorPicker = false },
            title = {
                Text(
                    text = "Select Visitor",
                    fontFamily = VisitorPoppins,
                    fontWeight = FontWeight.SemiBold
                )
            },
            text = {
                Column {
                    openVisitors.forEach { visitor ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
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
                                .padding(vertical = 10.dp)
                        ) {
                            Text(
                                text = "${visitor.name} | ${visitor.date} | ${visitor.timeIn}",
                                fontFamily = VisitorPoppins,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showVisitorPicker = false }) {
                    Text("Close", fontFamily = VisitorPoppins)
                }
            }
        )
    }

    Scaffold(
        bottomBar = {
            VisitorBottomNavBar(
                onDashboardClick = onNavigateToDashboard,
                onTasksClick = onNavigateToTasks,
                onFarmClick = onBackToBiosecurity
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF2F2F2))
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .pointerInput(Unit) {
                    detectTapGestures {
                        focusManager.clearFocus()
                    }
                }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1E5D36))
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 12.dp)
                        .clickable { onBackToBiosecurity() }
                )
                Text(
                    text = "Visitors",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontFamily = VisitorPoppins,
                    fontWeight = FontWeight.Bold
                )
            }

            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Spacer(modifier = Modifier.height(10.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    VisitorModeButton(
                        label = "Time In",
                        selected = mode == VisitorMode.TIME_IN,
                        onClick = { mode = VisitorMode.TIME_IN }
                    )
                    VisitorModeButton(
                        label = "Time Out",
                        selected = mode == VisitorMode.TIME_OUT,
                        onClick = { mode = VisitorMode.TIME_OUT }
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                VisitorLabel(if (mode == VisitorMode.TIME_IN) "Name" else "Visitor")
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

                Spacer(modifier = Modifier.height(10.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        VisitorLabel("Date")
                        VisitorPickerField(
                            value = date,
                            placeholder = "Date",
                            onClick = { if (mode == VisitorMode.TIME_IN) showDatePicker() },
                            enabled = mode == VisitorMode.TIME_IN,
                            readOnlyStyle = mode == VisitorMode.TIME_OUT
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        VisitorLabel("Time In")
                        VisitorPickerField(
                            value = timeIn,
                            placeholder = "Time in",
                            onClick = { if (mode == VisitorMode.TIME_IN) showTimePicker { timeIn = it } },
                            enabled = mode == VisitorMode.TIME_IN,
                            readOnlyStyle = mode == VisitorMode.TIME_OUT
                        )
                    }

                    if (mode == VisitorMode.TIME_OUT) {
                        Column(modifier = Modifier.weight(1f)) {
                            VisitorLabel("Time Out")
                            VisitorPickerField(
                                value = timeOut,
                                placeholder = "Time out",
                                onClick = { showTimePicker { timeOut = it } },
                                enabled = true,
                                readOnlyStyle = false
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                VisitorLabel("Purpose")
                VisitorInputField(
                    value = purpose,
                    onValueChange = { purpose = it },
                    enabled = mode == VisitorMode.TIME_IN,
                    readOnlyStyle = mode == VisitorMode.TIME_OUT
                )

                Spacer(modifier = Modifier.height(10.dp))

                VisitorLabel("Visitor Photo")
                if (mode == VisitorMode.TIME_IN) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = {
                                galleryLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF455A64))
                        ) {
                            Icon(Icons.Outlined.Upload, contentDescription = "Upload")
                            Spacer(modifier = Modifier.size(6.dp))
                            Text("Upload", fontFamily = VisitorPoppins, color = Color.White)
                        }

                        Button(
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
                            },
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF546E7A))
                        ) {
                            Icon(Icons.Outlined.PhotoCamera, contentDescription = "Camera")
                            Spacer(modifier = Modifier.size(6.dp))
                            Text("Camera", fontFamily = VisitorPoppins, color = Color.White)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .background(Color(0xFFEDEDED), RoundedCornerShape(20.dp))
                        .border(1.dp, Color(0xFFBDBDBD), RoundedCornerShape(20.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        selectedPhotoUri != null -> {
                            AsyncImage(
                                model = selectedPhotoUri,
                                contentDescription = "Visitor photo preview",
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        !selectedPhotoUrl.isNullOrBlank() -> {
                            AsyncImage(
                                model = selectedPhotoUrl,
                                contentDescription = "Visitor photo preview",
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        else -> {
                            Text(
                                text = "No photo selected",
                                fontFamily = VisitorPoppins,
                                fontSize = 14.sp,
                                color = Color(0xFF6E6E6E)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    VisitorCheckbox(
                        label = "Foot Bath",
                        checked = footBath,
                        onCheckedChange = { footBath = it },
                        enabled = mode == VisitorMode.TIME_IN,
                        readOnlyStyle = mode == VisitorMode.TIME_OUT
                    )
                    VisitorCheckbox(
                        label = "Sanitation",
                        checked = sanitation,
                        onCheckedChange = { sanitation = it },
                        enabled = mode == VisitorMode.TIME_IN,
                        readOnlyStyle = mode == VisitorMode.TIME_OUT
                    )
                    VisitorCheckbox(
                        label = "PPE",
                        checked = ppe,
                        onCheckedChange = { ppe = it },
                        enabled = mode == VisitorMode.TIME_IN,
                        readOnlyStyle = mode == VisitorMode.TIME_OUT
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

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
                                        dialogTitle = "Missing Date"
                                        dialogMessage = "Please select a date."
                                        showDialog = true
                                        return@Button
                                    }
                                    timeIn.isBlank() -> {
                                        dialogTitle = "Missing Time In"
                                        dialogMessage = "Please select time in."
                                        showDialog = true
                                        return@Button
                                    }
                                    name.isBlank() -> {
                                        dialogTitle = "Missing Name"
                                        dialogMessage = "Please enter the visitor name."
                                        showDialog = true
                                        return@Button
                                    }
                                    purpose.isBlank() -> {
                                        dialogTitle = "Missing Purpose"
                                        dialogMessage = "Please enter the purpose."
                                        showDialog = true
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
                                            dialogTitle = "Submitted"
                                            dialogMessage = "Visitor timed in successfully."
                                            showDialog = true
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
                                            dialogTitle = "Submission Failed"
                                            dialogMessage = "Failed to submit visitor time in."
                                            showDialog = true
                                        }
                                    }.onFailure {
                                        dialogTitle = "Submission Failed"
                                        dialogMessage = it.message ?: "Failed to submit visitor time in."
                                        showDialog = true
                                    }
                                    isLoading = false
                                }
                            } else {
                                when {
                                    selectedVisitorId == null -> {
                                        dialogTitle = "Missing Visitor"
                                        dialogMessage = "Please select a visitor to time out."
                                        showDialog = true
                                        return@Button
                                    }
                                    timeOut.isBlank() -> {
                                        dialogTitle = "Missing Time Out"
                                        dialogMessage = "Please select time out."
                                        showDialog = true
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
                                            dialogTitle = "Submitted"
                                            dialogMessage = "Visitor timed out successfully."
                                            showDialog = true
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
                                            dialogTitle = "Submission Failed"
                                            dialogMessage = "Failed to submit visitor time out."
                                            showDialog = true
                                        }
                                    }.onFailure {
                                        dialogTitle = "Submission Failed"
                                        dialogMessage = it.message ?: "Failed to submit visitor time out."
                                        showDialog = true
                                    }
                                    isLoading = false
                                }
                            }
                        },
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E5D36)),
                        modifier = Modifier.height(40.dp),
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
                            fontFamily = VisitorPoppins
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun VisitorModeButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) Color(0xFF1E5D36) else Color(0xFFDCE6DE),
            contentColor = if (selected) Color.White else Color(0xFF1E5D36)
        )
    ) {
        Text(
            text = label,
            fontFamily = VisitorPoppins,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun VisitorCheckbox(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean,
    readOnlyStyle: Boolean
) {
    Column {
        VisitorLabel(label)
        Box(
            modifier = Modifier
                .size(28.dp)
                .border(
                    2.dp,
                    if (readOnlyStyle) Color(0xFFB0B0B0) else Color(0xFF6A6A6A),
                    RoundedCornerShape(4.dp)
                )
                .background(
                    when {
                        checked && readOnlyStyle -> Color(0xFF9AA59C)
                        checked -> Color(0xFF1E5D36)
                        readOnlyStyle -> Color(0xFFE0E0E0)
                        else -> Color.Transparent
                    },
                    RoundedCornerShape(4.dp)
                )
                .clickable(enabled = enabled) { onCheckedChange(!checked) }
        )
    }
}

@Composable
private fun VisitorLabel(text: String) {
    Text(
        text = text,
        fontSize = 12.sp,
        fontFamily = VisitorPoppins,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.padding(bottom = 4.dp)
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
        shape = RoundedCornerShape(20.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = if (readOnlyStyle) Color(0xFFE2E2E2) else Color(0xFFEDEDED),
            unfocusedContainerColor = if (readOnlyStyle) Color(0xFFE2E2E2) else Color(0xFFEDEDED),
            disabledContainerColor = Color(0xFFE2E2E2),
            focusedBorderColor = if (readOnlyStyle) Color(0xFFB8B8B8) else Color(0xFFBDBDBD),
            unfocusedBorderColor = if (readOnlyStyle) Color(0xFFB8B8B8) else Color(0xFFBDBDBD),
            disabledBorderColor = Color(0xFFB8B8B8),
            cursorColor = Color.Black,
            disabledTextColor = Color(0xFF666666)
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
            .background(
                if (readOnlyStyle) Color(0xFFE2E2E2) else Color(0xFFEDEDED),
                RoundedCornerShape(20.dp)
            )
            .border(
                1.dp,
                if (readOnlyStyle) Color(0xFFB8B8B8) else Color(0xFFBDBDBD),
                RoundedCornerShape(20.dp)
            )
            .clickable(enabled = enabled) { onClick() }
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = if (value.isBlank()) placeholder else value,
            fontFamily = VisitorPoppins,
            fontSize = 14.sp,
            color = if (value.isBlank()) Color(0xFF6E6E6E) else if (readOnlyStyle) Color(0xFF666666) else Color.Black,
            maxLines = 1
        )
    }
}

@Composable
private fun VisitorBottomNavBar(
    onDashboardClick: () -> Unit,
    onTasksClick: () -> Unit,
    onFarmClick: () -> Unit
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
        VisitorBottomNavItem(Icons.Outlined.Home, "Dashboard", false, onDashboardClick)
        VisitorBottomNavItem(Icons.AutoMirrored.Outlined.List, "Tasks", false, onTasksClick)
        VisitorBottomNavItem(Icons.Outlined.Edit, "Farm Management", true, onFarmClick)
        VisitorBottomNavItem(Icons.Outlined.AccountCircle, "Profile", false, {})
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
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.clickable(
            interactionSource = interactionSource,
            indication = null
        ) { onClick() }
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
            fontFamily = VisitorPoppins,
            fontWeight = FontWeight.Normal,
            fontSize = 10.sp,
            color = if (selected) Color.White else Color(0xFFD7ECD9),
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}