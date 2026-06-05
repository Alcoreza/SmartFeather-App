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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

private val PendingVitaminManrope = FontFamily(
    Font(R.font.manrope_extralight, FontWeight.ExtraLight),
    Font(R.font.manrope_light, FontWeight.Light),
    Font(R.font.manrope_regular, FontWeight.Normal),
    Font(R.font.manrope_medium, FontWeight.Medium),
    Font(R.font.manrope_semibold, FontWeight.SemiBold),
    Font(R.font.manrope_bold, FontWeight.Bold),
    Font(R.font.manrope_extrabold, FontWeight.ExtraBold),
    Font(R.font.manrope_variablefont_wght, FontWeight.Black)
)

private val PendingVitaminSurface = Color(0xFFFFFCF7)
private val PendingVitaminSurfaceAlt = Color(0xFFF3EFE7)
private val PendingVitaminAutoField = Color(0xFFE8E3DA)
private val PendingVitaminInk = Color(0xFF121A14)
private val PendingVitaminMuted = Color(0xFF677168)
private val PendingVitaminLine = Color(0xFFD8D0C3)
private val PendingVitaminGreen = Color(0xFF1F7A3A)
private val PendingVitaminTeal = Color(0xFF2E7D6B)

fun isVitaminsSupplementationTask(title: String): Boolean {
    val normalized = title.trim().lowercase()
    return normalized == "vitamins supplementation" ||
            normalized == "vitamin supplementation" ||
            normalized == "vitamins refill" ||
            normalized == "vitamin refill" ||
            normalized.contains("vitamin") && normalized.contains("supplement")
}

@Composable
fun PendingVitaminsSupplementationSection(
    task: PendingTaskDetailUiState,
    vitaminType: String,
    vitaminOptions: List<VitaminInventoryOption>,
    bottlesUsed: String,
    vitaminExpanded: Boolean,
    recordedAt: String,
    onVitaminExpandedChange: (Boolean) -> Unit,
    onVitaminSelected: (VitaminInventoryOption) -> Unit,
    onBottlesUsedChange: (String) -> Unit
) {
    PendingVitaminSectionPanel {
        PendingVitaminSectionHeader(
            title = "Vitamins Supplementation",
            subtitle = "Record vitamin usage for the assigned pen",
            accentColor = PendingVitaminTeal
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                PendingVitaminLabel("House")
                Spacer(modifier = Modifier.height(8.dp))
                PendingVitaminReadOnlyField(task.houseLabel.ifBlank { "-" })
            }

            Column(modifier = Modifier.weight(1f)) {
                PendingVitaminLabel("Pen")
                Spacer(modifier = Modifier.height(8.dp))
                PendingVitaminReadOnlyField(task.penLabel.ifBlank { "-" })
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                PendingVitaminLabel("Date")
                Spacer(modifier = Modifier.height(8.dp))
                PendingVitaminReadOnlyField(formatPendingVitaminDate(recordedAt))
            }

            Column(modifier = Modifier.weight(1f)) {
                PendingVitaminLabel("Time")
                Spacer(modifier = Modifier.height(8.dp))
                PendingVitaminReadOnlyField(formatPendingVitaminTime(recordedAt))
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        PendingVitaminLabel("Type of Vitamins")
        Spacer(modifier = Modifier.height(8.dp))
        PendingVitaminDropdownField(
            value = vitaminType,
            placeholder = "Select vitamins",
            options = vitaminOptions.map { it.selectionKey },
            expanded = vitaminExpanded,
            onExpandedChange = onVitaminExpandedChange,
            optionLabel = { option -> option.substringAfter("|") },
            onValueSelected = { selectedValue ->
                vitaminOptions.firstOrNull { it.selectionKey == selectedValue }?.let(onVitaminSelected)
            }
        )

        Spacer(modifier = Modifier.height(14.dp))

        PendingVitaminLabel("Bottles Used")
        Spacer(modifier = Modifier.height(8.dp))
        PendingVitaminInputField(
            value = bottlesUsed,
            keyboardType = KeyboardType.Number,
            onValueChange = onBottlesUsedChange
        )
    }
}

@Composable
private fun PendingVitaminSectionPanel(
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
                        PendingVitaminSurface.copy(alpha = 0.98f)
                    )
                )
            )
            .border(1.dp, PendingVitaminLine.copy(alpha = 0.82f), RoundedCornerShape(26.dp))
            .padding(horizontal = 18.dp, vertical = 18.dp),
        content = content
    )
}

@Composable
private fun PendingVitaminSectionHeader(
    title: String,
    subtitle: String,
    accentColor: Color
) {
    Row(verticalAlignment = Alignment.Top) {
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
                fontFamily = PendingVitaminManrope,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp,
                color = PendingVitaminInk
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = subtitle,
                fontFamily = PendingVitaminManrope,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                color = PendingVitaminMuted,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
private fun PendingVitaminLabel(text: String) {
    Text(
        text = text,
        fontFamily = PendingVitaminManrope,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 13.sp,
        color = PendingVitaminInk
    )
}

@Composable
private fun PendingVitaminReadOnlyField(value: String) {
    OutlinedTextField(
        value = value,
        onValueChange = {},
        readOnly = true,
        enabled = false,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        textStyle = TextStyle(
            fontFamily = PendingVitaminManrope,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            color = PendingVitaminMuted
        ),
        shape = RoundedCornerShape(18.dp),
        colors = OutlinedTextFieldDefaults.colors(
            disabledContainerColor = PendingVitaminAutoField,
            disabledBorderColor = Color.Transparent,
            disabledTextColor = PendingVitaminMuted
        )
    )
}

@Composable
private fun PendingVitaminInputField(
    value: String,
    keyboardType: KeyboardType,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        textStyle = TextStyle(
            fontFamily = PendingVitaminManrope,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            color = PendingVitaminInk
        ),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = PendingVitaminSurfaceAlt,
            unfocusedContainerColor = PendingVitaminSurfaceAlt,
            focusedBorderColor = PendingVitaminGreen,
            unfocusedBorderColor = Color.Transparent,
            focusedTextColor = PendingVitaminInk,
            unfocusedTextColor = PendingVitaminInk,
            cursorColor = PendingVitaminGreen
        )
    )
}

@Composable
private fun PendingVitaminDropdownField(
    value: String,
    placeholder: String,
    options: List<String>,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    optionLabel: (String) -> String = { it },
    onValueSelected: (String) -> Unit
) {
    Box {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(PendingVitaminSurfaceAlt)
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
                    fontFamily = PendingVitaminManrope,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = if (value.isBlank()) PendingVitaminMuted else PendingVitaminInk,
                    modifier = Modifier.weight(1f)
                )

                Icon(
                    imageVector = Icons.Filled.KeyboardArrowDown,
                    contentDescription = placeholder,
                    tint = PendingVitaminGreen,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) },
            modifier = Modifier.background(PendingVitaminSurface)
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = optionLabel(option),
                            fontFamily = PendingVitaminManrope,
                            fontWeight = FontWeight.SemiBold,
                            color = PendingVitaminInk
                        )
                    },
                    onClick = { onValueSelected(option) }
                )
            }
        }
    }
}

private fun formatPendingVitaminDate(value: String): String {
    return runCatching {
        LocalDateTime.parse(value, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            .format(DateTimeFormatter.ofPattern("M-d-yy", Locale.getDefault()))
    }.getOrDefault("-")
}

private fun formatPendingVitaminTime(value: String): String {
    return runCatching {
        LocalDateTime.parse(value, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            .format(DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault()))
    }.getOrDefault("-")
}