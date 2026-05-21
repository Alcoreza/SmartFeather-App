package com.example.smartfeather

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

private val PersonnelPoppins = FontFamily(
    Font(R.font.poppins_regular, FontWeight.Normal),
    Font(R.font.poppins_medium, FontWeight.Medium),
    Font(R.font.poppins_semibold, FontWeight.SemiBold),
    Font(R.font.poppins_bold, FontWeight.Bold)
)

@Composable
fun PersonnelLogsScreen(
    employeeId: Int,
    onBackToBiosecurity: () -> Unit,
    onNavigateToDashboard: () -> Unit,
    onNavigateToTasks: () -> Unit
) {
    val personnelService = remember { PersonnelLogsBackendService() }
    val coroutineScope = rememberCoroutineScope()

    var date by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var house by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("") }
    var footBath by remember { mutableStateOf(false) }
    var bootsChanged by remember { mutableStateOf(false) }
    var protectiveClothing by remember { mutableStateOf(false) }

    var personnelEntryLogId by remember { mutableStateOf<Long?>(null) }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    var showIncompleteDialog by remember { mutableStateOf(false) }
    var incompleteMessage by remember { mutableStateOf("") }

    LaunchedEffect(employeeId) {
        personnelService.getContext(employeeId)
            .onSuccess { context ->
                date = context.date
                time = context.time
                house = context.house
                name = context.name
                role = context.role
                personnelEntryLogId = context.personnelEntryLogId
                errorMessage = null
            }
            .onFailure {
                date = ""
                time = ""
                house = ""
                name = ""
                role = ""
                personnelEntryLogId = null
                errorMessage = it.message ?: "Failed to load personnel biosecurity context."
            }
    }

    if (showIncompleteDialog) {
        AlertDialog(
            onDismissRequest = { showIncompleteDialog = false },
            title = {
                Text(
                    text = "Incomplete Biosecurity",
                    fontFamily = PersonnelPoppins,
                    fontWeight = FontWeight.SemiBold
                )
            },
            text = {
                Text(
                    text = incompleteMessage,
                    fontFamily = PersonnelPoppins
                )
            },
            confirmButton = {
                TextButton(onClick = { showIncompleteDialog = false }) {
                    Text("OK", fontFamily = PersonnelPoppins)
                }
            }
        )
    }

    Scaffold(
        bottomBar = {
            PersonnelBottomNavBar(
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
                    text = "Personnel Logs",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontFamily = PersonnelPoppins,
                    fontWeight = FontWeight.Bold
                )
            }

            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Spacer(modifier = Modifier.height(10.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        PersonnelLabel("Date")
                        PersonnelReadOnlyField(date)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        PersonnelLabel("Time")
                        PersonnelReadOnlyField(time)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Column(modifier = Modifier.fillMaxWidth(0.5f)) {
                    PersonnelLabel("House")
                    PersonnelReadOnlyField(house)
                }

                Spacer(modifier = Modifier.height(10.dp))

                PersonnelLabel("Name")
                PersonnelReadOnlyField(name)

                Spacer(modifier = Modifier.height(10.dp))

                PersonnelLabel("Role")
                PersonnelReadOnlyField(role)

                Spacer(modifier = Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    PersonnelCheckbox(
                        label = "Foot Bath",
                        checked = footBath,
                        onCheckedChange = { footBath = it }
                    )
                    PersonnelCheckbox(
                        label = "Boots Changed",
                        checked = bootsChanged,
                        onCheckedChange = { bootsChanged = it }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                PersonnelCheckbox(
                    label = "Protective Clothing",
                    checked = protectiveClothing,
                    onCheckedChange = { protectiveClothing = it }
                )

                Spacer(modifier = Modifier.height(12.dp))

                errorMessage?.let {
                    Text(
                        text = it,
                        color = Color(0xFFB00020),
                        fontFamily = PersonnelPoppins,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                successMessage?.let {
                    Text(
                        text = it,
                        color = Color(0xFF2E7D32),
                        fontFamily = PersonnelPoppins,
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

                            val currentEntryLogId = personnelEntryLogId
                            if (currentEntryLogId == null) {
                                errorMessage = "Missing personnel entry log context."
                                return@Button
                            }

                            val uncheckedItems = buildList {
                                if (!footBath) add("Foot Bath")
                                if (!bootsChanged) add("Boots Changed")
                                if (!protectiveClothing) add("Protective Clothing")
                            }

                            if (uncheckedItems.isNotEmpty()) {
                                incompleteMessage = "Please complete: ${uncheckedItems.joinToString(", ")}."
                                showIncompleteDialog = true
                                return@Button
                            }

                            coroutineScope.launch {
                                isLoading = true
                                personnelService.submit(
                                    employeeId = employeeId,
                                    personnelEntryLogId = currentEntryLogId,
                                    footBath = footBath,
                                    bootsChanged = bootsChanged,
                                    protectiveClothing = protectiveClothing
                                ).onSuccess { success ->
                                    if (success) {
                                        successMessage = "Personnel biosecurity log submitted successfully."
                                    } else {
                                        errorMessage = "Failed to submit personnel biosecurity log."
                                    }
                                }.onFailure {
                                    errorMessage = it.message ?: "Failed to submit personnel biosecurity log."
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
                            fontFamily = PersonnelPoppins
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PersonnelCheckbox(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Column {
        PersonnelLabel(label)
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
private fun PersonnelLabel(text: String) {
    Text(
        text = text,
        fontSize = 12.sp,
        fontFamily = PersonnelPoppins,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.padding(bottom = 4.dp)
    )
}

@Composable
private fun PersonnelReadOnlyField(value: String) {
    OutlinedTextField(
        value = value,
        onValueChange = {},
        readOnly = true,
        enabled = false,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        shape = RoundedCornerShape(20.dp),
        colors = OutlinedTextFieldDefaults.colors(
            disabledContainerColor = Color(0xFFE3E3E3),
            disabledBorderColor = Color(0xFFBDBDBD),
            disabledTextColor = Color(0xFF6E6E6E)
        )
    )
}

@Composable
private fun PersonnelBottomNavBar(
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
        PersonnelBottomNavItem(Icons.Outlined.Home, "Dashboard", false, onDashboardClick)
        PersonnelBottomNavItem(Icons.AutoMirrored.Outlined.List, "Tasks", false, onTasksClick)
        PersonnelBottomNavItem(Icons.Outlined.Edit, "Farm Management", true, onFarmClick)
        PersonnelBottomNavItem(Icons.Outlined.AccountCircle, "Profile", false, {})
    }
}

@Composable
private fun PersonnelBottomNavItem(
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
            fontFamily = PersonnelPoppins,
            fontWeight = FontWeight.Normal,
            fontSize = 10.sp,
            color = if (selected) Color.White else Color(0xFFD7ECD9),
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}