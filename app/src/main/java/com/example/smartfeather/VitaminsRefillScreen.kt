package com.example.smartfeather

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.List
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
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val VitaminsPoppins = FontFamily(
    Font(R.font.poppins_regular, FontWeight.Normal),
    Font(R.font.poppins_medium, FontWeight.Medium),
    Font(R.font.poppins_semibold, FontWeight.SemiBold),
    Font(R.font.poppins_bold, FontWeight.Bold)
)

@Composable
fun VitaminsRefillScreen(
    onBackToFarm: () -> Unit,
    onNavigateToDashboard: () -> Unit,
    onNavigateToTasks: () -> Unit
) {
    val vitaminsService = remember { VitaminsRefillBackendService() }
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    var batchId by remember { mutableStateOf("") }
    var house by remember { mutableStateOf("") }
    var pen by remember { mutableStateOf("") }
    var typeOfVitamins by remember { mutableStateOf("") }
    var bottlesUsed by remember { mutableStateOf("") }

    var houses by remember { mutableStateOf<List<VitaminHouseOption>>(emptyList()) }
    var pens by remember { mutableStateOf<List<VitaminPenOption>>(emptyList()) }
    var vitaminOptions by remember { mutableStateOf<List<VitaminInventoryOption>>(emptyList()) }

    var selectedHouse by remember { mutableStateOf<VitaminHouseOption?>(null) }
    var selectedPen by remember { mutableStateOf<VitaminPenOption?>(null) }
    var selectedVitamin by remember { mutableStateOf<VitaminInventoryOption?>(null) }

    var houseExpanded by remember { mutableStateOf(false) }
    var penExpanded by remember { mutableStateOf(false) }
    var vitaminExpanded by remember { mutableStateOf(false) }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        vitaminsService.getHouses()
            .onSuccess { houses = it }
            .onFailure { errorMessage = it.message ?: "Failed to load houses." }

        vitaminsService.getVitaminInventoryOptions()
            .onSuccess { vitaminOptions = it }
            .onFailure { errorMessage = it.message ?: "Failed to load vitamin inventory." }
    }

    Scaffold(
        bottomBar = {
            VitaminsBottomNavBar(
                onDashboardClick = onNavigateToDashboard,
                onTasksClick = onNavigateToTasks,
                onFarmClick = onBackToFarm
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
                    .background(Color(0xFF2E7D6B))
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
                        .clickable { onBackToFarm() }
                )
                Text(
                    text = "Vitamins Refill",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontFamily = VitaminsPoppins,
                    fontWeight = FontWeight.Bold
                )
            }

            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                VitaminsLabel("Batch ID")
                VitaminsInputField(batchId) { batchId = it }

                Spacer(modifier = Modifier.height(10.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        VitaminsLabel("House")
                        VitaminsDropdownField(
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

                                val houseId = selectedHouse?.id ?: return@VitaminsDropdownField
                                coroutineScope.launch {
                                    vitaminsService.getPensByHouse(houseId)
                                        .onSuccess { pens = it }
                                        .onFailure {
                                            errorMessage = it.message ?: "Failed to load pens."
                                        }
                                }
                            }
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        VitaminsLabel("Pen")
                        VitaminsDropdownField(
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

                VitaminsLabel("Type of Vitamins")
                VitaminsDropdownField(
                    value = typeOfVitamins,
                    options = vitaminOptions.map { it.itemName },
                    expanded = vitaminExpanded,
                    onExpandedChange = { vitaminExpanded = it },
                    onValueSelected = { selectedValue ->
                        typeOfVitamins = selectedValue
                        selectedVitamin = vitaminOptions.firstOrNull { it.itemName == selectedValue }
                        vitaminExpanded = false
                        errorMessage = null
                        successMessage = null
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                VitaminsLabel("Bottles of Vitamins Used")
                VitaminsInputField(
                    value = bottlesUsed,
                    keyboardType = KeyboardType.Number
                ) { newValue ->
                    bottlesUsed = newValue.filter { ch -> ch.isDigit() }
                }

                Spacer(modifier = Modifier.height(12.dp))

                errorMessage?.let {
                    Text(
                        text = it,
                        color = Color(0xFFB00020),
                        fontFamily = VitaminsPoppins,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                successMessage?.let {
                    Text(
                        text = it,
                        color = Color(0xFF2E7D32),
                        fontFamily = VitaminsPoppins,
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
                            focusManager.clearFocus()
                            errorMessage = null
                            successMessage = null

                            val currentHouse = selectedHouse
                            val currentPen = selectedPen
                            val currentVitamin = selectedVitamin
                            val bottlesValue = bottlesUsed.toIntOrNull()

                            if (currentHouse == null) {
                                errorMessage = "Please select a house."
                                return@Button
                            }
                            if (currentPen == null) {
                                errorMessage = "Please select a pen."
                                return@Button
                            }
                            if (currentVitamin == null) {
                                errorMessage = "Please select a type of vitamins."
                                return@Button
                            }
                            if (bottlesValue == null || bottlesValue <= 0) {
                                errorMessage = "Please enter a valid bottle count."
                                return@Button
                            }

                            coroutineScope.launch {
                                isLoading = true
                                vitaminsService.submitVitaminRefill(
                                    inventoryId = currentVitamin.id,
                                    houseId = currentHouse.id,
                                    penId = currentPen.id,
                                    bottles = bottlesValue
                                ).onSuccess { success ->
                                    if (success) {
                                        successMessage = "Vitamins refill submitted successfully."
                                        batchId = ""
                                        house = ""
                                        pen = ""
                                        typeOfVitamins = ""
                                        bottlesUsed = ""
                                        selectedHouse = null
                                        selectedPen = null
                                        selectedVitamin = null
                                        pens = emptyList()

                                        vitaminsService.getVitaminInventoryOptions()
                                            .onSuccess { vitaminOptions = it }
                                    } else {
                                        errorMessage = "Failed to submit vitamins refill."
                                    }
                                }.onFailure {
                                    errorMessage = it.message ?: "Failed to submit vitamins refill."
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
                            fontFamily = VitaminsPoppins
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun VitaminsLabel(text: String) {
    Text(
        text = text,
        fontSize = 12.sp,
        fontFamily = VitaminsPoppins,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.padding(bottom = 4.dp)
    )
}

@Composable
private fun VitaminsInputField(
    value: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    onValueChange: (String) -> Unit
) {
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val coroutineScope = rememberCoroutineScope()

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
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
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
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
private fun VitaminsDropdownField(
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
                    fontFamily = VitaminsPoppins,
                    fontSize = 14.sp,
                    color = Color.Black
                )
                Text(
                    text = "⌄",
                    fontFamily = VitaminsPoppins,
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
                    text = { Text(option, fontFamily = VitaminsPoppins) },
                    onClick = { onValueSelected(option) }
                )
            }
        }
    }
}

@Composable
private fun VitaminsBottomNavBar(
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
        VitaminsBottomNavItem(Icons.Outlined.Home, "Dashboard", false, onDashboardClick)
        VitaminsBottomNavItem(Icons.AutoMirrored.Outlined.List, "Tasks", false, onTasksClick)
        VitaminsBottomNavItem(Icons.Outlined.Edit, "Farm Management", true, onFarmClick)
        VitaminsBottomNavItem(Icons.Outlined.AccountCircle, "Profile", false, {})
    }
}

@Composable
private fun VitaminsBottomNavItem(
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
            fontFamily = VitaminsPoppins,
            fontWeight = FontWeight.Normal,
            fontSize = 10.sp,
            color = if (selected) Color.White else Color(0xFFD7ECD9),
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}
