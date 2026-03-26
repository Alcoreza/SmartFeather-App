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
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.filled.KeyboardArrowDown
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
import androidx.compose.material.icons.automirrored.outlined.ArrowBack

private val PersonnelPoppins = FontFamily(
    Font(R.font.poppins_regular, FontWeight.Normal),
    Font(R.font.poppins_medium, FontWeight.Medium),
    Font(R.font.poppins_semibold, FontWeight.SemiBold),
    Font(R.font.poppins_bold, FontWeight.Bold)
)

@Composable
fun PersonnelLogsScreen(
    onBackToBiosecurity: () -> Unit,
    onNavigateToDashboard: () -> Unit,
    onNavigateToTasks: () -> Unit
) {
    var date by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var house by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("") }
    var footBath by remember { mutableStateOf(false) }
    var bootsChanged by remember { mutableStateOf(false) }
    var protectiveClothing by remember { mutableStateOf(false) }

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
            // HEADER
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
                        .clickable { onBackToBiosecurity() } // use each screen's own back lambda
                )
                Text(
                    text = "Personnel Logs",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontFamily = PersonnelPoppins,
                    fontWeight = FontWeight.Bold
                )
            }

            // CONTENT
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {

                Spacer(modifier = Modifier.height(10.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        PersonnelLabel("Date")
                        PersonnelInputField(date) { date = it }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        PersonnelLabel("Time")
                        PersonnelInputField(time) { time = it }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // House only (no Pen in this screen)
                Column(modifier = Modifier.fillMaxWidth(0.5f)) {
                    PersonnelLabel("House")
                    PersonnelDropdownField(house) { house = it }
                }

                Spacer(modifier = Modifier.height(10.dp))

                PersonnelLabel("Name")
                PersonnelInputField(name) { name = it }

                Spacer(modifier = Modifier.height(10.dp))

                PersonnelLabel("Role")
                PersonnelInputField(role) { role = it }

                Spacer(modifier = Modifier.height(16.dp))

                // Checkboxes row 1
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

                // Checkboxes row 2
                PersonnelCheckbox(
                    label = "Protective Clothing",
                    checked = protectiveClothing,
                    onCheckedChange = { protectiveClothing = it }
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = { },
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E5D36)),
                        modifier = Modifier.height(40.dp)
                    ) {
                        Text("Submit", color = Color.White, fontFamily = PersonnelPoppins)
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
private fun PersonnelInputField(value: String, onValueChange: (String) -> Unit) {
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
private fun PersonnelDropdownField(value: String, onValueSelected: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = {},
        readOnly = true,
        trailingIcon = { Icon(Icons.Default.KeyboardArrowDown, contentDescription = null) },
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