package com.example.smartfeather

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import kotlinx.coroutines.launch
import java.util.Calendar
import androidx.compose.foundation.border


private val VisitorPoppins = FontFamily(
    Font(R.font.poppins_regular, FontWeight.Normal),
    Font(R.font.poppins_medium, FontWeight.Medium),
    Font(R.font.poppins_semibold, FontWeight.SemiBold),
    Font(R.font.poppins_bold, FontWeight.Bold)
)

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

    var date by remember { mutableStateOf("") }
    var timeIn by remember { mutableStateOf("") }
    var timeOut by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var purpose by remember { mutableStateOf("") }
    var footBath by remember { mutableStateOf(false) }
    var sanitation by remember { mutableStateOf(false) }
    var ppe by remember { mutableStateOf(false) }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

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

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        VisitorLabel("Date")
                        VisitorPickerField(
                            value = date,
                            placeholder = "Date",
                            onClick = { showDatePicker() }
                        )

                    }
                    Column(modifier = Modifier.weight(1f)) {
                        VisitorLabel("Time In")
                        VisitorPickerField(
                            value = timeIn,
                            placeholder = "Time in",
                            onClick = { showTimePicker { timeIn = it } }
                        )

                    }
                    Column(modifier = Modifier.weight(1f)) {
                        VisitorLabel("Time Out")
                        VisitorPickerField(
                            value = timeOut,
                            placeholder = "Time out",
                            onClick = { showTimePicker { timeOut = it } }
                        )

                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                VisitorLabel("Name")
                VisitorInputField(name) { name = it }

                Spacer(modifier = Modifier.height(10.dp))

                VisitorLabel("Purpose")
                VisitorInputField(purpose) { purpose = it }

                Spacer(modifier = Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    VisitorCheckbox(
                        label = "Foot Bath",
                        checked = footBath,
                        onCheckedChange = { footBath = it }
                    )
                    VisitorCheckbox(
                        label = "Sanitation",
                        checked = sanitation,
                        onCheckedChange = { sanitation = it }
                    )
                    VisitorCheckbox(
                        label = "PPE",
                        checked = ppe,
                        onCheckedChange = { ppe = it }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                errorMessage?.let {
                    Text(
                        text = it,
                        color = Color(0xFFB00020),
                        fontFamily = VisitorPoppins,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                successMessage?.let {
                    Text(
                        text = it,
                        color = Color(0xFF2E7D32),
                        fontFamily = VisitorPoppins,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = {
                            errorMessage = null
                            successMessage = null
                            focusManager.clearFocus()

                            if (date.isBlank()) {
                                errorMessage = "Please select a date."
                                return@Button
                            }
                            if (timeIn.isBlank()) {
                                errorMessage = "Please select time in."
                                return@Button
                            }
                            if (timeOut.isBlank()) {
                                errorMessage = "Please select time out."
                                return@Button
                            }
                            if (name.isBlank()) {
                                errorMessage = "Please enter the visitor name."
                                return@Button
                            }
                            if (purpose.isBlank()) {
                                errorMessage = "Please enter the purpose."
                                return@Button
                            }

                            coroutineScope.launch {
                                isLoading = true
                                visitorService.submitVisitorLog(
                                    employeeId = employeeId,
                                    date = date,
                                    timeIn = timeIn,
                                    timeOut = timeOut,
                                    name = name.trim(),
                                    purpose = purpose.trim(),
                                    footBath = footBath,
                                    sanitation = sanitation,
                                    ppe = ppe
                                ).onSuccess { success ->
                                    if (success) {
                                        successMessage = "Visitor log submitted successfully."
                                        date = ""
                                        timeIn = ""
                                        timeOut = ""
                                        name = ""
                                        purpose = ""
                                        footBath = false
                                        sanitation = false
                                        ppe = false
                                    } else {
                                        errorMessage = "Failed to submit visitor log."
                                    }
                                }.onFailure {
                                    errorMessage = it.message ?: "Failed to submit visitor log."
                                }
                                isLoading = false
                            }
                        },
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E5D36)),
                        modifier = Modifier.height(40.dp),
                        enabled = !isLoading
                    ) {
                        Text(
                            text = if (isLoading) "Submitting..." else "Submit",
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
private fun VisitorCheckbox(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Column {
        VisitorLabel(label)
        Box(
            modifier = Modifier
                .size(28.dp)
                .border(2.dp, Color(0xFF6A6A6A), RoundedCornerShape(4.dp))
                .background(
                    if (checked) Color(0xFF1E5D36) else Color.Transparent,
                    RoundedCornerShape(4.dp)
                )
                .clickable { onCheckedChange(!checked) }
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
private fun VisitorInputField(value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        shape = RoundedCornerShape(20.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color(0xFFEDEDED),
            unfocusedContainerColor = Color(0xFFEDEDED),
            focusedBorderColor = Color(0xFFBDBDBD),
            unfocusedBorderColor = Color(0xFFBDBDBD),
            cursorColor = Color.Black
        )
    )
}

@Composable
private fun VisitorPickerField(
    value: String,
    placeholder: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(Color(0xFFEDEDED), RoundedCornerShape(20.dp))
            .border(1.dp, Color(0xFFBDBDBD), RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = if (value.isBlank()) placeholder else value,
            fontFamily = VisitorPoppins,
            fontSize = 14.sp,
            color = if (value.isBlank()) Color(0xFF6E6E6E) else Color.Black,
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
