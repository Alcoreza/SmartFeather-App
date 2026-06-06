package com.example.smartfeather

import androidx.compose.foundation.background
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
import androidx.compose.foundation.border
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

private val PendingFeedManrope = FontFamily(
    Font(R.font.manrope_extralight, FontWeight.ExtraLight),
    Font(R.font.manrope_light, FontWeight.Light),
    Font(R.font.manrope_regular, FontWeight.Normal),
    Font(R.font.manrope_medium, FontWeight.Medium),
    Font(R.font.manrope_semibold, FontWeight.SemiBold),
    Font(R.font.manrope_bold, FontWeight.Bold),
    Font(R.font.manrope_extrabold, FontWeight.ExtraBold),
    Font(R.font.manrope_variablefont_wght, FontWeight.Black)
)

private val PendingFeedSurface = Color(0xFFFFFCF7)
private val PendingFeedSurfaceAlt = Color(0xFFF3EFE7)
private val PendingFeedAutoField = Color(0xFFE8E3DA)
private val PendingFeedInk = Color(0xFF121A14)
private val PendingFeedMuted = Color(0xFF677168)
private val PendingFeedLine = Color(0xFFD8D0C3)
private val PendingFeedGreen = Color(0xFF1F7A3A)
private val PendingFeedAmber = Color(0xFFE28622)

fun isFeedReplenishmentTask(title: String): Boolean {
    val normalized = title.trim().lowercase()
    return normalized == "feed replenishment" ||
            normalized == "feeds replenishment" ||
            normalized == "feeds refill" ||
            normalized == "feed refill" ||
            normalized.contains("feed") && normalized.contains("replenishment")
}

@Composable
fun PendingFeedReplenishmentSection(
    task: PendingTaskDetailUiState,
    feedType: String,
    feedOptions: List<FeedInventoryOption>,
    feederNumber: String,
    kilograms: String,
    feederOptions: List<String>,
    feedExpanded: Boolean,
    feederExpanded: Boolean,
    recordedAt: String,
    onFeedExpandedChange: (Boolean) -> Unit,
    onFeederExpandedChange: (Boolean) -> Unit,
    onFeedSelected: (FeedInventoryOption) -> Unit,
    onFeederSelected: (String) -> Unit,
    onKilogramsChange: (String) -> Unit
) {
    PendingFeedSectionPanel {
        PendingFeedSectionHeader(
            title = "Feed Replenishment",
            accentColor = PendingFeedAmber
        )
        Spacer(modifier = Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                PendingFeedLabel("House")
                Spacer(modifier = Modifier.height(8.dp))
                PendingFeedReadOnlyField(task.houseLabel.ifBlank { "-" })
            }

            Column(modifier = Modifier.weight(1f)) {
                PendingFeedLabel("Pen")
                Spacer(modifier = Modifier.height(8.dp))
                PendingFeedReadOnlyField(task.penLabel.ifBlank { "-" })
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                PendingFeedLabel("Date")
                Spacer(modifier = Modifier.height(8.dp))
                PendingFeedReadOnlyField(formatPendingFeedDate(recordedAt))
            }

            Column(modifier = Modifier.weight(1f)) {
                PendingFeedLabel("Time")
                Spacer(modifier = Modifier.height(8.dp))
                PendingFeedReadOnlyField(formatPendingFeedTime(recordedAt))
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        PendingFeedLabel("Type of Feed")
        Spacer(modifier = Modifier.height(8.dp))
        PendingFeedDropdownField(
            value = feedType,
            placeholder = "Select feed",
            options = feedOptions.map { it.selectionKey },
            expanded = feedExpanded,
            onExpandedChange = onFeedExpandedChange,
            optionLabel = { option -> option.substringAfter("|") },
            onValueSelected = { selectedValue ->
                feedOptions.firstOrNull { it.selectionKey == selectedValue }?.let(onFeedSelected)
            }
        )

        Spacer(modifier = Modifier.height(14.dp))

        PendingFeedLabel("Feeder Number")
        Spacer(modifier = Modifier.height(8.dp))
        PendingFeedDropdownField(
            value = feederNumber,
            placeholder = "Select feeder",
            options = feederOptions,
            expanded = feederExpanded,
            onExpandedChange = onFeederExpandedChange,
            onValueSelected = onFeederSelected
        )

        Spacer(modifier = Modifier.height(14.dp))

        PendingFeedLabel("Kilograms Refilled")
        Spacer(modifier = Modifier.height(8.dp))
        PendingFeedInputField(
            value = kilograms,
            keyboardType = KeyboardType.Number,
            onValueChange = onKilogramsChange
        )
    }
}

@Composable
private fun PendingFeedSectionPanel(
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
                        PendingFeedSurface.copy(alpha = 0.98f)
                    )
                )
            )
            .border(1.dp, PendingFeedLine.copy(alpha = 0.82f), RoundedCornerShape(26.dp))
            .padding(horizontal = 18.dp, vertical = 18.dp),
        content = content
    )
}

@Composable
private fun PendingFeedSectionHeader(
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
            fontFamily = PendingFeedManrope,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 18.sp,
            color = PendingFeedInk
        )
    }
}

@Composable
private fun PendingFeedLabel(text: String) {
    Text(
        text = text,
        fontFamily = PendingFeedManrope,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 13.sp,
        color = PendingFeedInk
    )
}

@Composable
private fun PendingFeedReadOnlyField(value: String) {
    OutlinedTextField(
        value = value,
        onValueChange = {},
        readOnly = true,
        enabled = false,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        textStyle = TextStyle(
            fontFamily = PendingFeedManrope,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            color = PendingFeedMuted
        ),
        shape = RoundedCornerShape(18.dp),
        colors = OutlinedTextFieldDefaults.colors(
            disabledContainerColor = PendingFeedAutoField,
            disabledBorderColor = Color.Transparent,
            disabledTextColor = PendingFeedMuted
        )
    )
}

@Composable
private fun PendingFeedInputField(
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
            fontFamily = PendingFeedManrope,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            color = PendingFeedInk
        ),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = PendingFeedSurfaceAlt,
            unfocusedContainerColor = PendingFeedSurfaceAlt,
            focusedBorderColor = PendingFeedGreen,
            unfocusedBorderColor = Color.Transparent,
            focusedTextColor = PendingFeedInk,
            unfocusedTextColor = PendingFeedInk,
            cursorColor = PendingFeedGreen
        )
    )
}

@Composable
private fun PendingFeedDropdownField(
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
                .background(PendingFeedSurfaceAlt)
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
                    fontFamily = PendingFeedManrope,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = if (value.isBlank()) PendingFeedMuted else PendingFeedInk,
                    modifier = Modifier.weight(1f)
                )

                Icon(
                    imageVector = Icons.Filled.KeyboardArrowDown,
                    contentDescription = placeholder,
                    tint = PendingFeedGreen,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) },
            modifier = Modifier.background(PendingFeedSurface)
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = optionLabel(option),
                            fontFamily = PendingFeedManrope,
                            fontWeight = FontWeight.SemiBold,
                            color = PendingFeedInk
                        )
                    },
                    onClick = { onValueSelected(option) }
                )
            }
        }
    }
}

private fun formatPendingFeedDate(value: String): String {
    return runCatching {
        LocalDateTime.parse(value, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            .format(DateTimeFormatter.ofPattern("M-d-yy", Locale.getDefault()))
    }.getOrDefault("-")
}

private fun formatPendingFeedTime(value: String): String {
    return runCatching {
        LocalDateTime.parse(value, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            .format(DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault()))
    }.getOrDefault("-")
}