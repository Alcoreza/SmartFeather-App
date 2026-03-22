package com.example.smartfeather

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
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

// Moved to top so it's accessible everywhere in this file
private val FarmPoppins = FontFamily(
    Font(R.font.poppins_regular, FontWeight.Normal),
    Font(R.font.poppins_medium, FontWeight.Medium),
    Font(R.font.poppins_semibold, FontWeight.SemiBold),
    Font(R.font.poppins_bold, FontWeight.Bold)
)

@Composable
fun FeedsRefillScreen(
    onBackToFarm: () -> Unit,
    onNavigateToDashboard: () -> Unit,
    onNavigateToTasks: () -> Unit
) {
    var batchId by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var house by remember { mutableStateOf("") }
    var pen by remember { mutableStateOf("") }
    var feederNumber by remember { mutableStateOf("") }
    var kilograms by remember { mutableStateOf("") }

    Scaffold(
        bottomBar = {
            FeedsBottomNavigationBar(
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
        ) {
            // HEADER
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFB37A3C))
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
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onBackToFarm() }
                )
                Text(
                    text = "Feeds Refill",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontFamily = FarmPoppins,
                    fontWeight = FontWeight.Bold
                )
            }

            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {

                Spacer(modifier = Modifier.height(10.dp))

                FeedsLabel("Batch ID")
                FeedsInputField(batchId) { batchId = it }

                Spacer(modifier = Modifier.height(10.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        FeedsLabel("Date")
                        FeedsInputField(date) { date = it }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        FeedsLabel("Time")
                        FeedsInputField(time) { time = it }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        FeedsLabel("House")
                        FeedsDropdownField(house) { house = it }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        FeedsLabel("Pen")
                        FeedsDropdownField(pen) { pen = it }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                FeedsLabel("Feeder Number")
                FeedsDropdownField(feederNumber) { feederNumber = it }

                Spacer(modifier = Modifier.height(10.dp))

                FeedsLabel("Kilograms of Feeds Refilled")
                FeedsInputField(kilograms) { kilograms = it }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = { },
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                        modifier = Modifier.height(40.dp)
                    ) {
                        Text("Submit", color = Color.White, fontFamily = FarmPoppins)
                    }
                }
            }
        }
    }
}

@Composable
fun FeedsLabel(text: String) {
    Text(
        text = text,
        fontSize = 12.sp,
        fontFamily = FarmPoppins,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.padding(bottom = 4.dp)
    )
}

@Composable
fun FeedsInputField(value: String, onValueChange: (String) -> Unit) {
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
fun FeedsDropdownField(value: String, onValueSelected: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = {},
        readOnly = true,
        trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth(),
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
fun FeedsBottomNavigationBar(
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
        FeedsBottomNavItem(Icons.Outlined.Home, "Dashboard", false, onDashboardClick)
        FeedsBottomNavItem(Icons.AutoMirrored.Outlined.List, "Tasks", false, onTasksClick)
        FeedsBottomNavItem(Icons.Outlined.Edit, "Farm Management", true, onFarmClick)
        FeedsBottomNavItem(Icons.Outlined.AccountCircle, "Profile", false, {})
    }
}

@Composable
fun FeedsBottomNavItem(
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