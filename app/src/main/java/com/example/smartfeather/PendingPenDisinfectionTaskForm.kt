package com.example.smartfeather

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

private val PenDisinfectionManrope = FontFamily(
    Font(R.font.manrope_extralight, FontWeight.ExtraLight),
    Font(R.font.manrope_light, FontWeight.Light),
    Font(R.font.manrope_regular, FontWeight.Normal),
    Font(R.font.manrope_medium, FontWeight.Medium),
    Font(R.font.manrope_semibold, FontWeight.SemiBold),
    Font(R.font.manrope_bold, FontWeight.Bold),
    Font(R.font.manrope_extrabold, FontWeight.ExtraBold),
    Font(R.font.manrope_variablefont_wght, FontWeight.Black)
)

private val PenDisinfectionSurface = Color(0xFFFFFCF7)
private val PenDisinfectionSurfaceAlt = Color(0xFFF3EFE7)
private val PenDisinfectionAutoField = Color(0xFFE8E3DA)
private val PenDisinfectionInk = Color(0xFF121A14)
private val PenDisinfectionMuted = Color(0xFF677168)
private val PenDisinfectionLine = Color(0xFFD8D0C3)
private val PenDisinfectionGreen = Color(0xFF1F7A3A)

private val PenDisinfectionOptions = listOf(
    "Quaternary ammonium compound",
    "Iodophor disinfectant",
    "Chlorine solution",
    "Virkon S",
    "Glutaraldehyde disinfectant",
    "Hydrogen peroxide disinfectant",
    "Phenolic disinfectant",
    "Lime wash",
    "Others"
)

@Composable
fun PendingPenDisinfectionTaskForm(
    task: PendingTaskDetailUiState,
    activity: String,
    disinfectantUsed: String,
    recordedAt: String,
    onActivityChange: (String) -> Unit,
    onDisinfectantUsedChange: (String) -> Unit
) {
    var disinfectantExpanded by remember { mutableStateOf(false) }
    var isOtherSelected by remember { mutableStateOf(false) }

    val fixedOptions = PenDisinfectionOptions.filterNot { it == "Others" }
    val isCustomDisinfectant = disinfectantUsed.isNotBlank() && fixedOptions.none { it == disinfectantUsed }
    val showOtherField = isOtherSelected || isCustomDisinfectant
    val selectedDisinfectantLabel = when {
        disinfectantUsed.isBlank() && showOtherField -> "Others"
        disinfectantUsed.isBlank() -> ""
        isCustomDisinfectant -> "Others"
        else -> disinfectantUsed
    }

    PenDisinfectionSectionPanel {
        PenDisinfectionSectionHeader(
            title = "Pen Disinfection",
            accentColor = PenDisinfectionGreen
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                PenDisinfectionLabel("House")
                Spacer(modifier = Modifier.height(8.dp))
                PenDisinfectionReadOnlyField(task.houseLabel.ifBlank { "-" })
            }

            Column(modifier = Modifier.weight(1f)) {
                PenDisinfectionLabel("Pen")
                Spacer(modifier = Modifier.height(8.dp))
                PenDisinfectionReadOnlyField(task.penLabel.ifBlank { "-" })
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                PenDisinfectionLabel("Date")
                Spacer(modifier = Modifier.height(8.dp))
                PenDisinfectionReadOnlyField(formatPenDisinfectionDate(recordedAt))
            }

            Column(modifier = Modifier.weight(1f)) {
                PenDisinfectionLabel("Time")
                Spacer(modifier = Modifier.height(8.dp))
                PenDisinfectionReadOnlyField(formatPenDisinfectionTime(recordedAt))
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        PenDisinfectionLabel("Disinfectant Used")
        Spacer(modifier = Modifier.height(8.dp))
        PenDisinfectionDropdownField(
            value = selectedDisinfectantLabel,
            placeholder = "Select disinfectant",
            options = PenDisinfectionOptions,
            expanded = disinfectantExpanded,
            onExpandedChange = { disinfectantExpanded = it },
            onValueSelected = { selected ->
                disinfectantExpanded = false

                if (selected == "Others") {
                    isOtherSelected = true
                    onDisinfectantUsedChange("")
                } else {
                    isOtherSelected = false
                    onDisinfectantUsedChange(selected)
                }
            }
        )

        if (showOtherField) {
            Spacer(modifier = Modifier.height(14.dp))

            PenDisinfectionLabel("Other Disinfectant")
            Spacer(modifier = Modifier.height(8.dp))
            PenDisinfectionInputField(
                value = disinfectantUsed,
                placeholder = "Enter disinfectant used",
                onValueChange = onDisinfectantUsedChange
            )
        }
    }
}

@Composable
private fun PenDisinfectionSectionPanel(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.94f),
                        PenDisinfectionSurface.copy(alpha = 0.98f)
                    )
                )
            )
            .border(1.dp, PenDisinfectionLine.copy(alpha = 0.82f), RoundedCornerShape(26.dp))
            .padding(horizontal = 18.dp, vertical = 18.dp),
        content = content
    )
}

@Composable
private fun PenDisinfectionSectionHeader(
    title: String,
    accentColor: Color
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(width = 4.dp, height = 28.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(accentColor)
        )

        Spacer(modifier = Modifier.size(12.dp))

        Text(
            text = title,
            fontFamily = PenDisinfectionManrope,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 18.sp,
            color = PenDisinfectionInk
        )
    }
}

@Composable
private fun PenDisinfectionLabel(text: String) {
    Text(
        text = text,
        fontFamily = PenDisinfectionManrope,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 13.sp,
        color = PenDisinfectionInk
    )
}

@Composable
private fun PenDisinfectionReadOnlyField(value: String) {
    OutlinedTextField(
        value = value,
        onValueChange = {},
        readOnly = true,
        enabled = false,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        textStyle = TextStyle(
            fontFamily = PenDisinfectionManrope,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            color = PenDisinfectionMuted
        ),
        shape = RoundedCornerShape(18.dp),
        colors = OutlinedTextFieldDefaults.colors(
            disabledContainerColor = PenDisinfectionAutoField,
            disabledBorderColor = Color.Transparent,
            disabledTextColor = PenDisinfectionMuted
        )
    )
}

@Composable
private fun PenDisinfectionDropdownField(
    value: String,
    placeholder: String,
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
                .clip(RoundedCornerShape(18.dp))
                .background(PenDisinfectionSurfaceAlt)
                .clickable { onExpandedChange(true) }
                .padding(horizontal = 14.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (value.isBlank()) placeholder else value,
                    fontFamily = PenDisinfectionManrope,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = if (value.isBlank()) PenDisinfectionMuted else PenDisinfectionInk,
                    modifier = Modifier.weight(1f)
                )

                Icon(
                    imageVector = Icons.Filled.KeyboardArrowDown,
                    contentDescription = placeholder,
                    tint = PenDisinfectionGreen,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) },
            modifier = Modifier.background(PenDisinfectionSurface)
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = option,
                            fontFamily = PenDisinfectionManrope,
                            fontWeight = FontWeight.SemiBold,
                            color = PenDisinfectionInk
                        )
                    },
                    onClick = { onValueSelected(option) }
                )
            }
        }
    }
}

@Composable
private fun PenDisinfectionInputField(
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Words
        ),
        modifier = Modifier.fillMaxWidth(),
        textStyle = TextStyle(
            fontFamily = PenDisinfectionManrope,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            color = PenDisinfectionInk
        ),
        placeholder = {
            Text(
                text = placeholder,
                fontFamily = PenDisinfectionManrope,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = PenDisinfectionMuted.copy(alpha = 0.72f)
            )
        },
        shape = RoundedCornerShape(18.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = PenDisinfectionSurfaceAlt,
            unfocusedContainerColor = PenDisinfectionSurfaceAlt,
            focusedBorderColor = PenDisinfectionGreen,
            unfocusedBorderColor = Color.Transparent,
            focusedTextColor = PenDisinfectionInk,
            unfocusedTextColor = PenDisinfectionInk,
            cursorColor = PenDisinfectionGreen
        )
    )
}

private fun formatPenDisinfectionDate(value: String): String {
    return runCatching {
        LocalDateTime.parse(value, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            .format(DateTimeFormatter.ofPattern("M-d-yy", Locale.getDefault()))
    }.getOrDefault("-")
}

private fun formatPenDisinfectionTime(value: String): String {
    return runCatching {
        LocalDateTime.parse(value, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            .format(DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault()))
    }.getOrDefault("-")
}