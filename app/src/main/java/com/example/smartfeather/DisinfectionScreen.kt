package com.example.smartfeather

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.foundation.border


private val DisinfectionPoppins = FontFamily(
    Font(R.font.poppins_regular, FontWeight.Normal),
    Font(R.font.poppins_medium, FontWeight.Medium),
    Font(R.font.poppins_semibold, FontWeight.SemiBold),
    Font(R.font.poppins_bold, FontWeight.Bold)
)

@Composable
fun DisinfectionScreen(
    employeeId: Int,
    onBackToBiosecurity: () -> Unit,
    onNavigateToDashboard: () -> Unit,
    onNavigateToTasks: () -> Unit
) {
    val disinfectionService = remember { DisinfectionBackendService() }
    val coroutineScope = rememberCoroutineScope()

    var house by remember { mutableStateOf("") }
    var pen by remember { mutableStateOf("") }
    var activity by remember { mutableStateOf("") }
    var disinfectantUsed by remember { mutableStateOf("") }

    var houses by remember { mutableStateOf<List<DisinfectionHouseOption>>(emptyList()) }
    var pens by remember { mutableStateOf<List<DisinfectionPenOption>>(emptyList()) }

    var selectedHouse by remember { mutableStateOf<DisinfectionHouseOption?>(null) }
    var selectedPen by remember { mutableStateOf<DisinfectionPenOption?>(null) }

    var houseExpanded by remember { mutableStateOf(false) }
    var penExpanded by remember { mutableStateOf(false) }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        disinfectionService.getHouses()
            .onSuccess { houses = it }
            .onFailure { errorMessage = it.message ?: "Failed to load houses." }
    }

    Scaffold(
        bottomBar = {
            DisinfectionBottomNavBar(
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
                    text = "Disinfection",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontFamily = DisinfectionPoppins,
                    fontWeight = FontWeight.Bold
                )
            }

            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Spacer(modifier = Modifier.height(10.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        DisinfectionLabel("House")
                        DisinfectionDropdownField(
                            value = house,
                            options = houses.map { it.houseNumber },
                            expanded = houseExpanded,
                            onExpandedChange = { houseExpanded = it },
                            onValueSelected = { selectedValue ->
                                house = selectedValue
                                selectedHouse = houses.firstOrNull { it.houseNumber == selectedValue }
                                pen = ""
                                selectedPen = null
                                pens = emptyList()
                                houseExpanded = false
                                errorMessage = null
                                successMessage = null

                                val houseId = selectedHouse?.id ?: return@DisinfectionDropdownField
                                coroutineScope.launch {
                                    disinfectionService.getPensByHouse(houseId)
                                        .onSuccess { pens = it }
                                        .onFailure {
                                            errorMessage = it.message ?: "Failed to load pens."
                                        }
                                }
                            }
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        DisinfectionLabel("Pen")
                        DisinfectionDropdownField(
                            value = pen,
                            options = pens.map { it.penName },
                            expanded = penExpanded,
                            onExpandedChange = {
                                if (selectedHouse != null) penExpanded = it
                            },
                            onValueSelected = { selectedValue ->
                                pen = selectedValue
                                selectedPen = pens.firstOrNull { it.penName == selectedValue }
                                penExpanded = false
                                errorMessage = null
                                successMessage = null
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                DisinfectionLabel("Activity")
                DisinfectionInputField(activity) { activity = it }

                Spacer(modifier = Modifier.height(10.dp))

                DisinfectionLabel("Disinfectant Used")
                DisinfectionInputField(disinfectantUsed) { disinfectantUsed = it }

                Spacer(modifier = Modifier.height(12.dp))

                errorMessage?.let {
                    Text(
                        text = it,
                        color = Color(0xFFB00020),
                        fontFamily = DisinfectionPoppins,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                successMessage?.let {
                    Text(
                        text = it,
                        color = Color(0xFF2E7D32),
                        fontFamily = DisinfectionPoppins,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = {
                            errorMessage = null
                            successMessage = null

                            val currentHouse = selectedHouse
                            val currentPen = selectedPen

                            if (currentHouse == null) {
                                errorMessage = "Please select a house."
                                return@Button
                            }
                            if (currentPen == null) {
                                errorMessage = "Please select a pen."
                                return@Button
                            }
                            if (activity.isBlank()) {
                                errorMessage = "Please enter the activity."
                                return@Button
                            }
                            if (disinfectantUsed.isBlank()) {
                                errorMessage = "Please enter the disinfectant used."
                                return@Button
                            }

                            coroutineScope.launch {
                                isLoading = true
                                disinfectionService.submitDisinfection(
                                    employeeId = employeeId,
                                    houseId = currentHouse.id,
                                    penId = currentPen.id,
                                    activity = activity.trim(),
                                    disinfectantUsed = disinfectantUsed.trim()
                                ).onSuccess { success ->
                                    if (success) {
                                        successMessage = "Disinfection submitted successfully."
                                        house = ""
                                        pen = ""
                                        activity = ""
                                        disinfectantUsed = ""
                                        selectedHouse = null
                                        selectedPen = null
                                        pens = emptyList()
                                    } else {
                                        errorMessage = "Failed to submit disinfection."
                                    }
                                }.onFailure {
                                    errorMessage = it.message ?: "Failed to submit disinfection."
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
                            fontFamily = DisinfectionPoppins
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DisinfectionLabel(text: String) {
    Text(
        text = text,
        fontSize = 12.sp,
        fontFamily = DisinfectionPoppins,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.padding(bottom = 4.dp)
    )
}

@Composable
private fun DisinfectionInputField(value: String, onValueChange: (String) -> Unit) {
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
private fun DisinfectionDropdownField(
    value: String,
    options: List<String>,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onValueSelected: (String) -> Unit
) {
    Box {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(Color(0xFFEDEDED), RoundedCornerShape(20.dp))
                .border(1.dp, Color(0xFFBDBDBD), RoundedCornerShape(20.dp))
                .clickable { onExpandedChange(true) }
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (value.isBlank()) "Select" else value,
                    fontFamily = DisinfectionPoppins,
                    fontSize = 14.sp,
                    color = Color.Black
                )
                Text(
                    text = "⌄",
                    fontFamily = DisinfectionPoppins,
                    fontSize = 18.sp,
                    color = Color.Black
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option, fontFamily = DisinfectionPoppins) },
                    onClick = { onValueSelected(option) }
                )
            }
        }
    }
}


@Composable
private fun DisinfectionBottomNavBar(
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
        DisinfectionBottomNavItem(Icons.Outlined.Home, "Dashboard", false, onDashboardClick)
        DisinfectionBottomNavItem(Icons.AutoMirrored.Outlined.List, "Tasks", false, onTasksClick)
        DisinfectionBottomNavItem(Icons.Outlined.Edit, "Farm Management", true, onFarmClick)
        DisinfectionBottomNavItem(Icons.Outlined.AccountCircle, "Profile", false, {})
    }
}

@Composable
private fun DisinfectionBottomNavItem(
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
            fontFamily = DisinfectionPoppins,
            fontWeight = FontWeight.Normal,
            fontSize = 10.sp,
            color = if (selected) Color.White else Color(0xFFD7ECD9),
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}
