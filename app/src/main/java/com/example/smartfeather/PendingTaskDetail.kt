package com.example.smartfeather

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlinx.coroutines.launch

data class PendingTaskDetailUiState(
    val id: Int,
    val title: String,
    val description: String,
    val timeAssigned: String,
    val finishBy: String,
    val priorityLabel: String,
    val priority: TaskPriority
)

private val DetailPoppins = FontFamily(
    Font(R.font.poppins_regular, FontWeight.Normal),
    Font(R.font.poppins_medium, FontWeight.Medium),
    Font(R.font.poppins_semibold, FontWeight.SemiBold),
    Font(R.font.poppins_bold, FontWeight.Bold)
)

private fun priorityChipColor(priority: TaskPriority): Color {
    return when (priority) {
        TaskPriority.LOW -> Color(0xFFFA7A1F)
        TaskPriority.MID -> Color(0xFFC4420B)
        TaskPriority.HIGH -> Color(0xFFC92222)
    }
}

@Composable
fun PendingTaskDetailScreen(
    task: PendingTaskDetailUiState,
    onBackClick: () -> Unit = {},
    onNavigateToDashboard: () -> Unit = {},
    onNavigateToTasks: () -> Unit = {},
    onSubmit: suspend (notes: String, photoUri: Uri?) -> Result<Unit> = { _, _ -> Result.success(Unit) }
) {
    var notes by remember { mutableStateOf("") }
    var selectedPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var submitError by remember { mutableStateOf<String?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        selectedPhotoUri = uri
    }

    Scaffold(
        containerColor = Color(0xFFF5F2EE),
        bottomBar = {
            TaskDetailBottomNavBar(
                onDashboardClick = onNavigateToDashboard,
                onTasksClick = onNavigateToTasks
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFFF8F6F2), Color(0xFFF1EEEA))
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
                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .clip(CircleShape)
                            .clickable { onBackClick() }
                            .padding(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF171717),
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Text(
                        text = "Tasks",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        fontFamily = DetailPoppins,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 22.sp,
                        color = Color(0xFF171717)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DetailSectionChip(
                        text = "Pending",
                        containerColor = Color(0xFFF4A46E)
                    )

                    DetailInfoChip(
                        title = "Time Assigned",
                        value = task.timeAssigned,
                        containerColor = Color(0xFF103824)
                    )

                    DetailInfoChip(
                        title = "Finish By",
                        value = task.finishBy,
                        containerColor = Color(0xFFD88913)
                    )

                    DetailInfoChip(
                        title = "Priority",
                        value = task.priorityLabel,
                        containerColor = priorityChipColor(task.priority)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 18.dp)) {
                        Text(
                            text = task.title,
                            fontFamily = DetailPoppins,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = Color(0xFF1B6A23)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = task.description,
                            fontFamily = DetailPoppins,
                            fontWeight = FontWeight.Normal,
                            fontSize = 14.sp,
                            lineHeight = 21.sp,
                            color = Color(0xFF3F3F3F)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 18.dp)) {
                        Text(
                            text = "Add Photo:",
                            fontFamily = DetailPoppins,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 18.sp,
                            color = Color(0xFF6B6B6B)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(Color(0xFFF7F7F7))
                                .clickable {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(
                                            ActivityResultContracts.PickVisualMedia.ImageOnly
                                        )
                                    )
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (selectedPhotoUri != null) {
                                AsyncImage(
                                    model = selectedPhotoUri,
                                    contentDescription = "Selected proof photo",
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Edit,
                                        contentDescription = "Upload proof photo",
                                        tint = Color(0xFF7A7A7A),
                                        modifier = Modifier.size(34.dp)
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text(
                                        text = "Tap to upload proof of work",
                                        fontFamily = DetailPoppins,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 13.sp,
                                        color = Color(0xFF8A8A8A)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 18.dp)) {
                        Text(
                            text = "Notes:",
                            fontFamily = DetailPoppins,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 18.sp,
                            color = Color(0xFF6B6B6B)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp),
                            shape = RoundedCornerShape(18.dp),
                            placeholder = {
                                Text(
                                    text = "Add notes here",
                                    fontFamily = DetailPoppins,
                                    color = Color(0xFF9A9A9A)
                                )
                            },
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Sentences
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFFFDFDFD),
                                unfocusedContainerColor = Color(0xFFFDFDFD),
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                submitError?.let {
                    Text(
                        text = it,
                        fontFamily = DetailPoppins,
                        fontSize = 13.sp,
                        color = Color(0xFFC92222)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            submitError = null

                            coroutineScope.launch {
                                isSubmitting = true
                                onSubmit(notes, selectedPhotoUri)
                                    .onFailure {
                                        submitError = it.message ?: "Failed to submit task."
                                    }
                                isSubmitting = false
                            }
                        },
                        shape = RoundedCornerShape(999.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1F7A2E)
                        ),
                        enabled = !isSubmitting
                    ) {
                        Text(
                            text = if (isSubmitting) "Submitting..." else "Submit",
                            fontFamily = DetailPoppins,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 18.sp,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun DetailSectionChip(
    text: String,
    containerColor: Color
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(containerColor)
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            fontFamily = DetailPoppins,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            color = Color.White
        )
    }
}

@Composable
private fun DetailInfoChip(
    title: String,
    value: String,
    containerColor: Color
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(containerColor)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = title,
            fontFamily = DetailPoppins,
            fontSize = 9.sp,
            color = Color.White.copy(alpha = 0.9f)
        )
        Text(
            text = value,
            fontFamily = DetailPoppins,
            fontWeight = FontWeight.SemiBold,
            fontSize = 10.sp,
            color = Color.White
        )
    }
}

@Composable
private fun TaskDetailBottomNavBar(
    onDashboardClick: () -> Unit,
    onTasksClick: () -> Unit
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
        DetailNavItem(Icons.Outlined.Home, "Dashboard", false, onDashboardClick)
        DetailNavItem(Icons.AutoMirrored.Outlined.List, "Tasks", true, onTasksClick)
        DetailNavItem(Icons.Outlined.Edit, "Farm Management", false, {})
        DetailNavItem(Icons.Outlined.AccountCircle, "Profile", false, {})
    }
}

@Composable
private fun DetailNavItem(
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
            fontFamily = DetailPoppins,
            fontSize = 10.sp,
            color = if (selected) Color.White else Color(0xFFD7ECD9),
            textAlign = TextAlign.Center,
            lineHeight = 11.sp
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PendingTaskDetailScreenPreview() {
    PendingTaskDetailScreen(
        task = PendingTaskDetailUiState(
            id = 1,
            title = "Cleaning",
            description = "Clean the assigned pen thoroughly, focusing especially on the area near the opening where dirt, moisture, and waste are more likely to accumulate. Remove visible manure, spilled feed, feathers, and other debris from the floor and surrounding surfaces.\n\nEnsure that the feeding and watering areas inside the pen are clean and unobstructed. After cleaning, visually check the pen to confirm that it is clean, dry, and safe for the birds.",
            timeAssigned = "11:58 AM",
            finishBy = "1:30 PM",
            priorityLabel = "High",
            priority = TaskPriority.HIGH
        )
    )
}
