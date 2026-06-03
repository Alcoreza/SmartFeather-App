package com.example.smartfeather

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
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
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
import com.composables.icons.lucide.ClipboardList
import com.composables.icons.lucide.House
import com.composables.icons.lucide.LayoutDashboard
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.UserRound

private val ProfilePoppins = FontFamily(
    Font(R.font.manrope_extralight, FontWeight.ExtraLight),
    Font(R.font.manrope_light, FontWeight.Light),
    Font(R.font.manrope_regular, FontWeight.Normal),
    Font(R.font.manrope_medium, FontWeight.Medium),
    Font(R.font.manrope_semibold, FontWeight.SemiBold),
    Font(R.font.manrope_bold, FontWeight.Bold),
    Font(R.font.manrope_extrabold, FontWeight.ExtraBold)
)

private val ProfileBackground = Color(0xFFF6F3EC)
private val ProfileSurface = Color(0xFFFFFCF7)
private val ProfileInk = Color(0xFF121A14)
private val ProfileMuted = Color(0xFF677168)
private val ProfileLine = Color(0xFFD8D0C3)
private val ProfileGreen = Color(0xFF1F7A3A)
private val ProfileDeepGreen = Color(0xFF062717)
private val ProfileGreenTwo = Color(0xFF155C2D)
private val ProfileDanger = Color(0xFFC62828)

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
    var isLoggingOut by remember { mutableStateOf(false) }
    var contentVisible by remember { mutableStateOf(false) }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    var showSaveConfirmDialog by remember { mutableStateOf(false) }
    var showSaveSuccessDialog by remember { mutableStateOf(false) }
    var showLogoutConfirmDialog by remember { mutableStateOf(false) }
    var confirmDialogMessage by remember { mutableStateOf("") }
    var successDialogMessage by remember { mutableStateOf("") }

    LaunchedEffect(employeeId) {
        isLoading = true
        contentVisible = false
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
        delay(120)
        contentVisible = true
    }

    if (showSaveConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showSaveConfirmDialog = false },
            containerColor = ProfileSurface,
            shape = RoundedCornerShape(28.dp),
            title = {
                Text(
                    text = "Save Changes",
                    fontFamily = ProfilePoppins,
                    fontWeight = FontWeight.ExtraBold,
                    color = ProfileInk
                )
            },
            text = {
                Text(
                    text = confirmDialogMessage,
                    fontFamily = ProfilePoppins,
                    fontWeight = FontWeight.Medium,
                    color = ProfileMuted,
                    lineHeight = 21.sp
                )
            },
            dismissButton = {
                TextButton(onClick = { showSaveConfirmDialog = false }) {
                    Text(
                        text = "Cancel",
                        fontFamily = ProfilePoppins,
                        fontWeight = FontWeight.ExtraBold,
                        color = ProfileMuted
                    )
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
                    Text(
                        text = "Save",
                        fontFamily = ProfilePoppins,
                        fontWeight = FontWeight.ExtraBold,
                        color = ProfileGreen
                    )
                }
            }
        )
    }

    if (showSaveSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSaveSuccessDialog = false },
            containerColor = ProfileSurface,
            shape = RoundedCornerShape(28.dp),
            title = {
                Text(
                    text = "Profile Updated",
                    fontFamily = ProfilePoppins,
                    fontWeight = FontWeight.ExtraBold,
                    color = ProfileInk
                )
            },
            text = {
                Text(
                    text = successDialogMessage,
                    fontFamily = ProfilePoppins,
                    fontWeight = FontWeight.Medium,
                    color = ProfileMuted,
                    lineHeight = 21.sp
                )
            },
            confirmButton = {
                TextButton(onClick = { showSaveSuccessDialog = false }) {
                    Text(
                        text = "OK",
                        fontFamily = ProfilePoppins,
                        fontWeight = FontWeight.ExtraBold,
                        color = ProfileGreen
                    )
                }
            }
        )
    }

    if (errorMessage != null) {
        AlertDialog(
            onDismissRequest = { errorMessage = null },
            containerColor = ProfileSurface,
            shape = RoundedCornerShape(28.dp),
            title = {
                Text(
                    text = "Something Went Wrong",
                    fontFamily = ProfilePoppins,
                    fontWeight = FontWeight.ExtraBold,
                    color = ProfileInk
                )
            },
            text = {
                Text(
                    text = errorMessage.orEmpty(),
                    fontFamily = ProfilePoppins,
                    fontWeight = FontWeight.Medium,
                    color = ProfileMuted,
                    lineHeight = 21.sp
                )
            },
            confirmButton = {
                TextButton(onClick = { errorMessage = null }) {
                    Text(
                        text = "Got it",
                        fontFamily = ProfilePoppins,
                        fontWeight = FontWeight.ExtraBold,
                        color = ProfileGreen
                    )
                }
            }
        )
    }

    if (showLogoutConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirmDialog = false },
            containerColor = ProfileSurface,
            shape = RoundedCornerShape(28.dp),
            title = {
                Text(
                    text = "Sign Out",
                    fontFamily = ProfilePoppins,
                    fontWeight = FontWeight.ExtraBold,
                    color = ProfileInk
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to sign out of this account?",
                    fontFamily = ProfilePoppins,
                    fontWeight = FontWeight.Medium,
                    color = ProfileMuted,
                    lineHeight = 21.sp
                )
            },
            dismissButton = {
                TextButton(onClick = { showLogoutConfirmDialog = false }) {
                    Text(
                        text = "Cancel",
                        fontFamily = ProfilePoppins,
                        fontWeight = FontWeight.ExtraBold,
                        color = ProfileMuted
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutConfirmDialog = false
                        isLoggingOut = true

                        coroutineScope.launch {
                            delay(650)
                            onLogout()
                        }
                    }
                ) {
                    Text(
                        text = "Sign Out",
                        fontFamily = ProfilePoppins,
                        fontWeight = FontWeight.ExtraBold,
                        color = ProfileDanger
                    )
                }
            }
        )
    }

    Scaffold(
        containerColor = ProfileBackground,
        bottomBar = {
            ProfileBottomNavBar(
                onDashboardClick = onNavigateToDashboard,
                onTasksClick = onNavigateToTasks,
                onFarmManagementClick = onNavigateToFarmManagement
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFFFBF8F1), ProfileBackground, Color(0xFFEDE7DA))
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
                ProfileHero(isEditing = isEditing)

                Spacer(modifier = Modifier.height(20.dp))

                if (isLoading) {
                    ProfileSkeleton()
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
                        profile?.let { user ->
                            ProfileSectionPanel {
                                ProfileSectionHeader(
                                    title = "Personal Information",
                                    subtitle = if (isEditing) {
                                        "Identity details are locked while editing contact info."
                                    } else {
                                        "Registered flockman details"
                                    },
                                    accentColor = ProfileGreen
                                )

                                Spacer(modifier = Modifier.height(16.dp))

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
                                    leftLabel = "Username",
                                    leftValue = user.username,
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
                                        ProfileLabel("Gender")
                                        Spacer(modifier = Modifier.height(8.dp))
                                        ProfileValueBox(
                                            value = user.gender,
                                            grayOut = isEditing
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(18.dp))

                            ProfileSectionPanel {
                                ProfileSectionHeader(
                                    title = "Contact Details",
                                    subtitle = if (isEditing) {
                                        "Update verified contact information."
                                    } else {
                                        "Phone and address used for farm records"
                                    },
                                    accentColor = if (isEditing) Color(0xFFD78A2B) else ProfileGreen
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                Column(modifier = Modifier.fillMaxWidth()) {
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
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            ProfileActionRow(
                                isEditing = isEditing,
                                isSaving = isSaving,
                                isLoggingOut = isLoggingOut,
                                onEdit = {
                                    errorMessage = null
                                    editablePhoneNumber = user.phoneNumber
                                    editableAddress = user.address
                                    isEditing = true
                                },
                                onCancel = {
                                    editablePhoneNumber = user.phoneNumber
                                    editableAddress = user.address
                                    errorMessage = null
                                    isEditing = false
                                },
                                onSave = {
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
                                onLogout = {
                                    showLogoutConfirmDialog = true
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }
            }

            if (isLoggingOut) {
                ProfileLogoutOverlay()
            }
        }
    }
}

@Composable
private fun ProfileHero(isEditing: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(30.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(ProfileDeepGreen, Color(0xFF0E4025), ProfileGreenTwo)
                )
            )
            .padding(horizontal = 20.dp, vertical = 22.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.12f))
                    .border(1.dp, Color.White.copy(alpha = 0.14f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.AccountCircle,
                    contentDescription = "Profile",
                    tint = Color.White,
                    modifier = Modifier.size(31.dp)
                )
            }

            Spacer(modifier = Modifier.size(13.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Profile",
                    fontFamily = ProfilePoppins,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 25.sp,
                    color = Color.White,
                    lineHeight = 29.sp
                )

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = if (isEditing) "Editing contact details" else "Flockman account details",
                    fontFamily = ProfilePoppins,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.72f),
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
private fun ProfileSectionPanel(
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.94f),
                        ProfileSurface.copy(alpha = 0.98f)
                    )
                )
            )
            .border(
                width = 1.dp,
                color = ProfileLine.copy(alpha = 0.82f),
                shape = RoundedCornerShape(26.dp)
            )
            .padding(horizontal = 18.dp, vertical = 18.dp),
        content = content
    )
}

@Composable
private fun ProfileSectionHeader(
    title: String,
    subtitle: String,
    accentColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .padding(top = 4.dp)
                .size(width = 4.dp, height = 38.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(accentColor)
        )

        Spacer(modifier = Modifier.size(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontFamily = ProfilePoppins,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp,
                color = ProfileInk
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = subtitle,
                fontFamily = ProfilePoppins,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                color = ProfileMuted,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
private fun ProfileActionRow(
    isEditing: Boolean,
    isSaving: Boolean,
    isLoggingOut: Boolean,
    onEdit: () -> Unit,
    onCancel: () -> Unit,
    onSave: () -> Unit,
    onLogout: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (!isEditing) {
            ProfileCompactButton(
                text = "Edit Profile",
                icon = Icons.Outlined.Edit,
                backgroundColor = Color(0xFF236B3A),
                contentColor = Color.White,
                borderColor = Color.Transparent,
                enabled = !isSaving && !isLoggingOut,
                onClick = onEdit
            )

            Spacer(modifier = Modifier.width(10.dp))

            ProfileCompactButton(
                text = "Sign Out",
                icon = null,
                backgroundColor = Color(0xFFFFF3F0),
                contentColor = ProfileDanger.copy(alpha = 0.94f),
                borderColor = ProfileDanger.copy(alpha = 0.48f),
                enabled = !isSaving && !isLoggingOut,
                onClick = onLogout
            )
        }

        if (isEditing) {
            ProfileCompactButton(
                text = "Cancel",
                icon = null,
                backgroundColor = Color(0xFFE8E3DA),
                contentColor = ProfileInk,
                borderColor = ProfileLine.copy(alpha = 0.85f),
                enabled = !isSaving && !isLoggingOut,
                onClick = onCancel
            )

            Spacer(modifier = Modifier.width(10.dp))

            ProfileCompactButton(
                text = if (isSaving) "Saving..." else "Save",
                icon = Icons.Outlined.Save,
                backgroundColor = Color(0xFF236B3A),
                contentColor = Color.White,
                borderColor = Color.Transparent,
                enabled = !isSaving && !isLoggingOut,
                onClick = onSave
            )
        }
    }
}

@Composable
private fun ProfileCompactButton(
    text: String,
    icon: ImageVector?,
    backgroundColor: Color,
    contentColor: Color,
    borderColor: Color,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .height(42.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(if (enabled) backgroundColor else Color(0xFFE0DCD4))
            .border(
                width = 1.dp,
                color = if (enabled) borderColor else Color.Transparent,
                shape = RoundedCornerShape(999.dp)
            )
            .clickable(enabled = enabled) { onClick() }
            .padding(horizontal = if (icon == null) 18.dp else 15.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                modifier = Modifier.size(15.dp),
                tint = contentColor
            )

            Spacer(modifier = Modifier.width(7.dp))
        }

        Text(
            text = text,
            fontFamily = ProfilePoppins,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 13.sp,
            color = contentColor
        )
    }
}

@Composable
private fun ProfileLogoutOverlay() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ProfileInk.copy(alpha = 0.34f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(26.dp))
                .background(ProfileSurface)
                .border(1.dp, ProfileLine.copy(alpha = 0.80f), RoundedCornerShape(26.dp))
                .padding(horizontal = 28.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                color = ProfileGreen,
                strokeWidth = 3.dp,
                modifier = Modifier.size(34.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Signing out",
                fontFamily = ProfilePoppins,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 15.sp,
                color = ProfileInk
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Please wait a moment.",
                fontFamily = ProfilePoppins,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                color = ProfileMuted
            )
        }
    }
}

@Composable
private fun ProfileMessageBanner(
    text: String,
    backgroundColor: Color,
    contentColor: Color
) {
    Text(
        text = text,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .border(1.dp, contentColor.copy(alpha = 0.22f), RoundedCornerShape(16.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        fontFamily = ProfilePoppins,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        color = contentColor
    )
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
private fun ProfileLabel(text: String) {
    Text(
        text = text,
        fontFamily = ProfilePoppins,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 13.sp,
        color = ProfileInk
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
            .clip(RoundedCornerShape(18.dp))
            .background(if (grayOut) Color(0xFFE9E5DC) else Color(0xFFF8F5EF))
            .border(
                1.dp,
                if (grayOut) ProfileLine.copy(alpha = 0.9f) else ProfileLine,
                RoundedCornerShape(18.dp)
            )
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Text(
            text = value.ifBlank { "-" },
            fontFamily = ProfilePoppins,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            lineHeight = 19.sp,
            color = if (grayOut) ProfileMuted else ProfileInk
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
        shape = RoundedCornerShape(18.dp),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color(0xFFF8F5EF),
            unfocusedContainerColor = Color(0xFFF8F5EF),
            focusedBorderColor = ProfileGreen,
            unfocusedBorderColor = ProfileLine,
            focusedTextColor = ProfileInk,
            unfocusedTextColor = ProfileInk,
            cursorColor = ProfileGreen
        ),
        textStyle = TextStyle(
            fontFamily = ProfilePoppins,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            color = ProfileInk
        ),
        placeholder = {
            Text(
                text = "-",
                fontFamily = ProfilePoppins,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = ProfileMuted
            )
        }
    )
}

@Composable
private fun ProfileSkeleton() {
    val alpha = profileSkeletonAlpha()

    Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
        ProfileSkeletonBox(
            modifier = Modifier
                .fillMaxWidth()
                .height(255.dp),
            alpha = alpha,
            shape = RoundedCornerShape(26.dp)
        )

        ProfileSkeletonBox(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp),
            alpha = alpha,
            shape = RoundedCornerShape(26.dp)
        )

        ProfileSkeletonBox(
            modifier = Modifier
                .fillMaxWidth()
                .height(102.dp),
            alpha = alpha,
            shape = RoundedCornerShape(18.dp)
        )
    }
}

@Composable
private fun profileSkeletonAlpha(): Float {
    val transition = rememberInfiniteTransition(label = "profileSkeleton")
    val alpha by transition.animateFloat(
        initialValue = 0.28f,
        targetValue = 0.62f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "profileSkeletonAlpha"
    )
    return alpha
}

@Composable
private fun ProfileSkeletonBox(
    modifier: Modifier,
    alpha: Float,
    color: Color = Color(0xFFD9D2C6),
    shape: Shape = RoundedCornerShape(16.dp)
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(color.copy(alpha = alpha))
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
                    colors = listOf(Color(0xFF07381F), Color(0xFF022716))
                )
            )
            .navigationBarsPadding()
            .padding(horizontal = 8.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        ProfileBottomNavItem(
            icon = Lucide.LayoutDashboard,
            label = "Dashboard",
            selected = false,
            onClick = onDashboardClick
        )

        ProfileBottomNavItem(
            icon = Lucide.ClipboardList,
            label = "Tasks",
            selected = false,
            onClick = onTasksClick
        )

        ProfileBottomNavItem(
            icon = Lucide.House,
            label = "Farm Management",
            selected = false,
            onClick = onFarmManagementClick
        )

        ProfileBottomNavItem(
            icon = Lucide.UserRound,
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
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 5.dp),
        horizontalAlignment = Alignment.CenterHorizontally
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
            fontFamily = ProfilePoppins,
            fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.SemiBold,
            fontSize = 9.sp,
            color = if (selected) Color.White else Color(0xFFCFE8D2),
            textAlign = TextAlign.Center,
            lineHeight = 10.sp
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