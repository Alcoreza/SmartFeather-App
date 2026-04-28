package com.example.smartfeather

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
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
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.ui.focus.onFocusEvent
import kotlinx.coroutines.delay





private val FarmPoppins = FontFamily(
    Font(R.font.poppins_regular, FontWeight.Normal),
    Font(R.font.poppins_medium, FontWeight.Medium),
    Font(R.font.poppins_semibold, FontWeight.SemiBold),
    Font(R.font.poppins_bold, FontWeight.Bold)
)

@Composable
fun PopulationScreen(
    onBackToFarm: () -> Unit,
    onNavigateToDashboard: () -> Unit,
    onNavigateToTasks: () -> Unit
) {
    val populationService = remember { PopulationBackendService() }
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current


    var batchId by remember { mutableStateOf("") }
    var house by remember { mutableStateOf("") }
    var pen by remember { mutableStateOf("") }
    var eggs by remember { mutableStateOf("") }
    var mortality by remember { mutableStateOf("") }

    var houses by remember { mutableStateOf<List<PopulationHouseOption>>(emptyList()) }
    var selectedHouse by remember { mutableStateOf<PopulationHouseOption?>(null) }
    var penOptions by remember { mutableStateOf<List<String>>(emptyList()) }

    var houseExpanded by remember { mutableStateOf(false) }
    var penExpanded by remember { mutableStateOf(false) }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        populationService.getHouses()
            .onSuccess { loadedHouses ->
                houses = loadedHouses
            }
            .onFailure {
                errorMessage = it.message ?: "Failed to load houses."
            }
    }

    Scaffold(
        bottomBar = {
            PopulationBottomNavBar(
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
                    .background(Color(0xFF8B0000))
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
                    text = "Population Data",
                    fontFamily = FarmPoppins,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }

            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                PopulationLabel("Batch ID")
                PopulationInputField(batchId) { batchId = it }

                Spacer(modifier = Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        PopulationLabel("House")
                        PopulationDropdownField(
                            value = house,
                            options = houses.map { it.houseNumber },
                            expanded = houseExpanded,
                            onExpandedChange = { houseExpanded = it },
                            onValueSelected = { selectedValue: String ->
                                house = selectedValue
                                selectedHouse = houses.firstOrNull { it.houseNumber == selectedValue }
                                pen = ""
                                penOptions = populationService.buildPenOptions(selectedHouse)
                                houseExpanded = false
                                penExpanded = false
                                errorMessage = null
                                successMessage = null
                            }
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        PopulationLabel("Pen")
                        PopulationDropdownField(
                            value = pen,
                            options = penOptions,
                            expanded = penExpanded,
                            onExpandedChange = {
                                if (selectedHouse != null) {
                                    penExpanded = it
                                }
                            },
                            onValueSelected = { selectedValue: String ->
                                pen = selectedValue
                                penExpanded = false
                                errorMessage = null
                                successMessage = null
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                PopulationLabel("Eggs Hatched")
                PopulationInputField(
                    value = eggs,
                    keyboardType = KeyboardType.Number
                ) { newValue ->
                    eggs = newValue.filter { ch -> ch.isDigit() }
                }

                Spacer(modifier = Modifier.height(12.dp))

                PopulationLabel("Mortalities")
                PopulationInputField(
                    value = mortality,
                    keyboardType = KeyboardType.Number
                ) { newValue ->
                    mortality = newValue.filter { ch -> ch.isDigit() }
                }


                Spacer(modifier = Modifier.height(12.dp))

                errorMessage?.let {
                    Text(
                        text = it,
                        color = Color(0xFFB00020),
                        fontFamily = FarmPoppins,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                successMessage?.let {
                    Text(
                        text = it,
                        color = Color(0xFF1E5D36),
                        fontFamily = FarmPoppins,
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
                            if (currentHouse == null) {
                                errorMessage = "Please select a house."
                                return@Button
                            }
                            if (pen.isBlank()) {
                                errorMessage = "Please select a pen."
                                return@Button
                            }
                            if (eggs.isBlank()) {
                                errorMessage = "Please enter eggs hatched."
                                return@Button
                            }
                            if (mortality.isBlank()) {
                                errorMessage = "Please enter mortalities."
                                return@Button
                            }

                            val eggsValue = eggs.toIntOrNull()
                            val mortalityValue = mortality.toIntOrNull()

                            if (eggsValue == null || mortalityValue == null) {
                                errorMessage = "Eggs hatched and mortalities must be valid numbers."
                                return@Button
                            }

                            coroutineScope.launch {
                                isLoading = true
                                populationService.submitPopulation(
                                    houseId = currentHouse.id,
                                    penNumber = pen,
                                    eggsHatched = eggsValue,
                                    mortality = mortalityValue
                                ).onSuccess { success ->
                                    if (success) {
                                        successMessage = "Population data submitted successfully."
                                        batchId = ""
                                        house = ""
                                        pen = ""
                                        eggs = ""
                                        mortality = ""
                                        selectedHouse = null
                                        penOptions = emptyList()
                                    } else {
                                        errorMessage = "No matching pen record was updated."
                                    }
                                }.onFailure {
                                    errorMessage = it.message ?: "Failed to submit population data."
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
                            fontFamily = FarmPoppins
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PopulationLabel(text: String) {
    Text(
        text = text,
        fontSize = 12.sp,
        fontFamily = FarmPoppins,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.padding(bottom = 4.dp)
    )
}

@Composable
private fun PopulationInputField(
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
private fun PopulationDropdownField(
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
                    text = if (value.isEmpty()) "Select" else value,
                    fontFamily = FarmPoppins,
                    fontSize = 14.sp
                )
                Text(
                    text = "⌄",
                    fontSize = 18.sp,
                    fontFamily = FarmPoppins
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = option,
                            fontFamily = FarmPoppins
                        )
                    },
                    onClick = {
                        onValueSelected(option)
                    }
                )
            }
        }
    }
}

@Composable
fun PopulationBottomNavBar(
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
        PopulationBottomNavItem(Icons.Outlined.Home, "Dashboard", false, onDashboardClick)
        PopulationBottomNavItem(Icons.AutoMirrored.Outlined.List, "Tasks", false, onTasksClick)
        PopulationBottomNavItem(Icons.Outlined.Edit, "Farm Management", true, onFarmClick)
        PopulationBottomNavItem(Icons.Outlined.AccountCircle, "Profile", false, {})
    }
}

@Composable
fun PopulationBottomNavItem(
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
            fontFamily = FarmPoppins,
            fontWeight = FontWeight.Normal,
            fontSize = 10.sp,
            color = if (selected) Color.White else Color(0xFFD7ECD9),
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}
