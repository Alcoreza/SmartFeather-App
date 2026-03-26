package com.example.smartfeather

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource

private val FarmPoppins = FontFamily(
    Font(R.font.poppins_regular, FontWeight.Normal),
    Font(R.font.poppins_medium, FontWeight.Medium),
    Font(R.font.poppins_semibold, FontWeight.SemiBold),
    Font(R.font.poppins_bold, FontWeight.Bold)
)

@Composable
fun WeightScreen(
    onBackToFarm: () -> Unit,
    onNavigateToDashboard: () -> Unit,
    onNavigateToTasks: () -> Unit
) {

    var batchId by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var house by remember { mutableStateOf("") }
    var pen by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("") }
    var flocks by remember { mutableStateOf("3") }
    var weight1 by remember { mutableStateOf("") }
    var weight2 by remember { mutableStateOf("") }
    var weight3 by remember { mutableStateOf("") }
    var sickCount by remember { mutableStateOf("") }

    Scaffold(
        bottomBar = {
            BottomNavBar(
                onTasksClick = onNavigateToTasks,
                onFarmManagementClick = onBackToFarm
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF1EFEC))
                .padding(padding)
        ) {

            // 🔴 HEADER
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF6A1B9A))
                    .padding(vertical = 12.dp),
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
                    text = "Weight",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontFamily = FarmPoppins,
                    fontWeight = FontWeight.Bold
                )
            }

            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {

                Label("Batch ID")
                InputField(batchId) { batchId = it }

                Spacer(Modifier.height(10.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Column(Modifier.weight(1f)) {
                        Label("Date")
                        InputField(date) { date = it }
                    }
                    Column(Modifier.weight(1f)) {
                        Label("Time")
                        InputField(time) { time = it }
                    }
                }

                Spacer(Modifier.height(10.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Column(Modifier.weight(1f)) {
                        Label("House")
                        DropdownField(house) { house = it }
                    }
                    Column(Modifier.weight(1f)) {
                        Label("Pen")
                        DropdownField(pen) { pen = it }
                    }
                }

                Spacer(Modifier.height(10.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Column(Modifier.weight(1f)) {
                        Label("Age")
                        InputField(age) { age = it }
                    }
                    Column(Modifier.weight(1f)) {
                        Label("Status")
                        DropdownField(status) { status = it }
                    }
                }

                Spacer(Modifier.height(10.dp))

                Label("Number of Flocks")
                InputField(flocks) { flocks = it }

                Spacer(Modifier.height(10.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Column(Modifier.weight(1f)) {
                        Label("Weight 1")
                        InputField(weight1) { weight1 = it }
                    }
                    Column(Modifier.weight(1f)) {
                        Label("Weight 2")
                        InputField(weight2) { weight2 = it }
                    }
                    Column(Modifier.weight(1f)) {
                        Label("Weight 3")
                        InputField(weight3) { weight3 = it }
                    }
                }

                Spacer(Modifier.height(10.dp))

                Label("Sick Count")
                InputField(sickCount) { sickCount = it }

                Spacer(Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = { },
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1E5D36)
                        ),
                        modifier = Modifier.height(40.dp)
                    ) {
                        Text(
                            "Submit",
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
private fun WeightLabel(text: String) {
    Text(
        text = text,
        fontSize = 12.sp,
        fontFamily = FarmPoppins,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.padding(bottom = 4.dp)
    )
}

@Composable
private fun WeightInputField(value: String, onValueChange: (String) -> Unit) {
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
private fun WeightDropdownField(value: String, onValueSelected: (String) -> Unit) {
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