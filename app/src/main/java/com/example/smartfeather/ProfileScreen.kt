package com.example.smartfeather

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val ProfilePoppins = FontFamily(
    Font(R.font.poppins_regular, FontWeight.Normal),
    Font(R.font.poppins_medium, FontWeight.Medium),
    Font(R.font.poppins_semibold, FontWeight.SemiBold),
    Font(R.font.poppins_bold, FontWeight.Bold)
)

@Composable
fun ProfileScreen(
    employeeId: Int,
    onNavigateToDashboard: () -> Unit,
    onNavigateToTasks: () -> Unit,
    onNavigateToFarmManagement: () -> Unit,
    onLogout: () -> Unit
) {
    val profileService = remember { ProfileBackendService() }
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    var profile by remember { mutableStateOf<FlockmanProfileUiState?>(null) }
    var editablePhoneNumber by remember { mutableStateOf("") }
    var editableAddress by remember { mutableStateOf("") }

    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }
    var isEditing by remember { mutableStateOf(false) }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    var showSaveConfirmDialog by remember { mutableStateOf(false) }
    var showSaveSuccessDialog by remember { mutableStateOf(false) }
    var confirmDialogMessage by remember { mutableStateOf("") }
    var successDialogMessage by remember { mutableStateOf("") }

    LaunchedEffect(employeeId) {
        isLoading = true
        errorMessage = null

        profileService.getFlockmanProfile(employeeId)
            .onSuccess {
                profile = it
                editablePhoneNumber = it.phoneNumber
                editableAddress = it.address
            }
            .onFailure {
                errorMessage = it.message ?: "Failed to load profile."
            }

        isLoading = false
    }

    if (showSaveConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showSaveConfirmDialog = false },
            title = {
                Text(
                    text = "Confirm Changes",
                    fontFamily = ProfilePoppins,
                    fontWeight = FontWeight.SemiBold
                )
            },
            text = {
                Text(
                    text = confirmDialogMessage,
                    fontFamily = ProfilePoppins
                )
            },
            dismissButton = {
                TextButton(onClick = { showSaveConfirmDialog = false }) {
                    Text("Cancel", fontFamily = ProfilePoppins)
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSaveConfirmDialog = false
                        errorMessage = null

                        coroutineScope.launch {
                            isSaving = true
                            profileService.updateFlockmanProfile(
                                employeeId = employeeId,
                                phoneNumber = editablePhoneNumber.trim(),
                                address = editableAddress.trim()
                            ).onSuccess { result ->
                                successDialogMessage = result.first
                                profile = result.second
                                editablePhoneNumber = result.second.phoneNumber
                                editableAddress = result.second.address
                                isEditing = false
                                showSaveSuccessDialog = true
                            }.onFailure {
                                errorMessage = it.message ?: "Failed to update profile."
                            }
                            isSaving = false
                        }
                    }
                ) {
                    Text("Save", fontFamily = ProfilePoppins)
                }
            }
        )
    }

    if (showSaveSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSaveSuccessDialog = false },
            title = {
                Text(
                    text = "Profile Updated",
                    fontFamily = ProfilePoppins,
                    fontWeight = FontWeight.SemiBold
                )
            },
            text = {
                Text(
                    text = successDialogMessage,
                    fontFamily = ProfilePoppins
                )
            },
            confirmButton = {
                TextButton(onClick = { showSaveSuccessDialog = false }) {
                    Text("OK", fontFamily = ProfilePoppins)
                }
            }
        )
    }

    Scaffold(
        containerColor = Color(0xFFF4F2EF),
        bottomBar = {
            ProfileBottomNavBar(
                onDashboardClick = onNavigateToDashboard,
                onTasksClick = onNavigateToTasks,
                onFarmManagementClick = onNavigateToFarmManagement
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFFF6F4F1), Color(0xFFEFECE8))
                    )
                )
                .pointerInput(Unit) {
                    detectTapGestures {
                        focusManager.clearFocus()
                    }
                }
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = 22.dp, vertical = 16.dp)
        ) {
            Text(
                text = "Profile",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                fontFamily = ProfilePoppins,
                fontWeight = FontWeight.SemiBold,
                fontSize = 22.sp,
                color = Color(0xFF111111)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(Color(0xFF246B33), RoundedCornerShape(999.dp))
            )

            Spacer(modifier = Modifier.height(14.dp))

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AccountCircle,
                        contentDescription = "Profile Icon",
                        tint = Color.White,
                        modifier = Modifier.size(56.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (isLoading) {
                InfoCardText("Loading profile...")
                Spacer(modifier = Modifier.height(18.dp))
            }

            errorMessage?.let {
                InfoCardText(it, Color(0xFFC51E1E))
                Spacer(modifier = Modifier.height(18.dp))
            }

            profile?.let { user ->
                ProfileFieldRow(
                    leftLabel = "First Name",
                    leftValue = user.firstName,
                    rightLabel = "Middle Name",
                    rightValue = user.middleName,
                    grayOut = isEditing
                )

                Spacer(modifier = Modifier.height(14.dp))

                ProfileFieldRow(
                    leftLabel = "Last Name",
                    leftValue = user.lastName,
                    rightLabel = "Suffix",
                    rightValue = user.suffix,
                    grayOut = isEditing
                )

                Spacer(modifier = Modifier.height(14.dp))

                ProfileFieldRow(
                    leftLabel = "ID",
                    leftValue = user.employeeId,
                    rightLabel = "Role",
                    rightValue = user.role,
                    grayOut = isEditing
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        ProfileLabel("Birthday")
                        Spacer(modifier = Modifier.height(8.dp))
                        ProfileValueBox(
                            value = user.birthday,
                            grayOut = isEditing
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        ProfileLabel("Phone Number")
                        Spacer(modifier = Modifier.height(8.dp))
                        if (isEditing) {
                            ProfileEditableValueBox(
                                value = editablePhoneNumber,
                                onValueChange = { editablePhoneNumber = it },
                                keyboardType = KeyboardType.Phone
                            )
                        } else {
                            ProfileValueBox(
                                value = user.phoneNumber,
                                grayOut = false
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                ProfileSingleField(
                    label = "Gender",
                    value = user.gender,
                    widthFraction = 0.42f,
                    grayOut = isEditing
                )

                Spacer(modifier = Modifier.height(14.dp))

                Column(modifier = Modifier.fillMaxWidth()) {
                    ProfileLabel("Address")
                    Spacer(modifier = Modifier.height(8.dp))
                    if (isEditing) {
                        ProfileEditableValueBox(
                            value = editableAddress,
                            onValueChange = { editableAddress = it },
                            keyboardType = KeyboardType.Text,
                            singleLine = false
                        )
                    } else {
                        ProfileValueBox(
                            value = user.address,
                            grayOut = false
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!isEditing) {
                        Button(
                            onClick = {
                                errorMessage = null
                                editablePhoneNumber = user.phoneNumber
                                editableAddress = user.address
                                isEditing = true
                            },
                            enabled = !isSaving,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF1E5D36)
                            ),
                            shape = RoundedCornerShape(999.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Edit,
                                contentDescription = "Edit",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Edit",
                                fontFamily = ProfilePoppins,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                    }

                    if (isEditing) {
                        Button(
                            onClick = {
                                editablePhoneNumber = user.phoneNumber
                                editableAddress = user.address
                                errorMessage = null
                                isEditing = false
                            },
                            enabled = !isSaving,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF8A8A8A)
                            ),
                            shape = RoundedCornerShape(999.dp)
                        ) {
                            Text(
                                text = "Cancel",
                                fontFamily = ProfilePoppins,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }

                        Button(
                            onClick = {
                                val phone = editablePhoneNumber.trim()
                                val address = editableAddress.trim()
                                val changedFields = buildList {
                                    if (phone != user.phoneNumber.trim()) add("Phone Number")
                                    if (address != user.address.trim()) add("Address")
                                }

                                when {
                                    changedFields.isEmpty() -> {
                                        errorMessage = "No changes were made."
                                    }
                                    phone.isBlank() -> {
                                        errorMessage = "Phone number is required."
                                    }
                                    !Regex("^(\\+639\\d{9}|09\\d{9})$").matches(phone) -> {
                                        errorMessage = "Phone number must be in 09XXXXXXXXX or +639XXXXXXXXX format."
                                    }
                                    address.length > 200 -> {
                                        errorMessage = "Address must not exceed 200 characters."
                                    }
                                    else -> {
                                        errorMessage = null
                                        confirmDialogMessage = if (changedFields.size == 1) {
                                            "Save changes to ${changedFields.first()}?"
                                        } else {
                                            "Save changes to ${changedFields.joinToString(" and ")}?"
                                        }
                                        showSaveConfirmDialog = true
                                    }
                                }
                            },
                            enabled = !isSaving,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF246B33)
                            ),
                            shape = RoundedCornerShape(999.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Save,
                                contentDescription = "Save",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isSaving) "Saving..." else "Save",
                                fontFamily = ProfilePoppins,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                    }

                    Button(
                        onClick = onLogout,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFC62828)
                        ),
                        shape = RoundedCornerShape(999.dp)
                    ) {
                        Text(
                            text = "Logout",
                            fontFamily = ProfilePoppins,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun InfoCardText(
    text: String,
    color: Color = Color(0xFF5E5E5E)
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(16.dp),
            fontFamily = ProfilePoppins,
            color = color,
            fontSize = 14.sp
        )
    }
}

@Composable
private fun ProfileFieldRow(
    leftLabel: String,
    leftValue: String,
    rightLabel: String,
    rightValue: String,
    grayOut: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            ProfileLabel(leftLabel)
            Spacer(modifier = Modifier.height(8.dp))
            ProfileValueBox(leftValue, grayOut)
        }

        Column(modifier = Modifier.weight(1f)) {
            ProfileLabel(rightLabel)
            Spacer(modifier = Modifier.height(8.dp))
            ProfileValueBox(rightValue, grayOut)
        }
    }
}

@Composable
private fun ProfileSingleField(
    label: String,
    value: String,
    widthFraction: Float,
    grayOut: Boolean
) {
    Column(
        modifier = Modifier.fillMaxWidth(widthFraction)
    ) {
        ProfileLabel(label)
        Spacer(modifier = Modifier.height(8.dp))
        ProfileValueBox(value, grayOut)
    }
}

@Composable
private fun ProfileLabel(text: String) {
    Text(
        text = text,
        fontFamily = ProfilePoppins,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        color = Color(0xFF111111)
    )
}

@Composable
private fun ProfileValueBox(
    value: String,
    grayOut: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(999.dp))
            .background(if (grayOut) Color(0xFFE7E7E7) else Color.White)
            .border(
                1.dp,
                if (grayOut) Color(0xFF9A9A9A) else Color(0xFF6E6E6E),
                RoundedCornerShape(999.dp)
            )
            .padding(horizontal = 18.dp, vertical = 12.dp)
    ) {
        Text(
            text = value.ifBlank { "-" },
            fontFamily = ProfilePoppins,
            fontSize = 14.sp,
            color = if (grayOut) Color(0xFF4A4A4A) else Color(0xFF1A1A1A)
        )
    }
}

@Composable
private fun ProfileEditableValueBox(
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType,
    singleLine: Boolean = true
) {
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val coroutineScope = rememberCoroutineScope()

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = singleLine,
        modifier = Modifier
            .fillMaxWidth()
            .bringIntoViewRequester(bringIntoViewRequester)
            .onFocusEvent { focusState ->
                if (focusState.isFocused) {
                    coroutineScope.launch {
                        delay(250)
                        bringIntoViewRequester.bringIntoView()
                    }
                }
            },
        shape = RoundedCornerShape(999.dp),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            focusedBorderColor = Color(0xFF246B33),
            unfocusedBorderColor = Color(0xFF6E6E6E),
            focusedTextColor = Color(0xFF1A1A1A),
            unfocusedTextColor = Color(0xFF1A1A1A),
            cursorColor = Color(0xFF246B33)
        ),
        textStyle = TextStyle(
            fontFamily = ProfilePoppins,
            fontSize = 14.sp,
            color = Color(0xFF1A1A1A)
        ),
        placeholder = {
            Text(
                text = "-",
                fontFamily = ProfilePoppins,
                fontSize = 14.sp,
                color = Color(0xFF8A8A8A)
            )
        }
    )
}

@Composable
private fun ProfileBottomNavBar(
    onDashboardClick: () -> Unit,
    onTasksClick: () -> Unit,
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
        ProfileBottomNavItem(
            icon = Icons.Outlined.Home,
            label = "Dashboard",
            selected = false,
            onClick = onDashboardClick
        )

        ProfileBottomNavItem(
            icon = Icons.AutoMirrored.Outlined.List,
            label = "Tasks",
            selected = false,
            onClick = onTasksClick
        )

        ProfileBottomNavItem(
            icon = Icons.Outlined.Edit,
            label = "Farm Management",
            selected = false,
            onClick = onFarmManagementClick
        )

        ProfileBottomNavItem(
            icon = Icons.Outlined.AccountCircle,
            label = "Profile",
            selected = true,
            onClick = {}
        )
    }
}

@Composable
private fun ProfileBottomNavItem(
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
            fontFamily = ProfilePoppins,
            fontSize = 10.sp,
            color = if (selected) Color.White else Color(0xFFD7ECD9),
            textAlign = TextAlign.Center,
            lineHeight = 11.sp
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ProfileScreenPreview() {
    ProfileScreen(
        employeeId = 2,
        onNavigateToDashboard = {},
        onNavigateToTasks = {},
        onNavigateToFarmManagement = {},
        onLogout = {}
    )
}