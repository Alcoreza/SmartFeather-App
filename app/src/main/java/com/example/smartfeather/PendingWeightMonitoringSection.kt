package com.example.smartfeather

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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

private val WeightTaskManrope = FontFamily(
    Font(R.font.manrope_extralight, FontWeight.ExtraLight),
    Font(R.font.manrope_light, FontWeight.Light),
    Font(R.font.manrope_regular, FontWeight.Normal),
    Font(R.font.manrope_medium, FontWeight.Medium),
    Font(R.font.manrope_semibold, FontWeight.SemiBold),
    Font(R.font.manrope_bold, FontWeight.Bold),
    Font(R.font.manrope_extrabold, FontWeight.ExtraBold),
    Font(R.font.manrope_variablefont_wght, FontWeight.Black)
)

private val WeightTaskSurface = Color(0xFFFFFCF7)
private val WeightTaskSurfaceAlt = Color(0xFFF3EFE7)
private val WeightTaskAutoField = Color(0xFFE8E3DA)
private val WeightTaskInk = Color(0xFF121A14)
private val WeightTaskMuted = Color(0xFF677168)
private val WeightTaskLine = Color(0xFFD8D0C3)
private val WeightTaskGreen = Color(0xFF1F7A3A)
private val WeightTaskBlue = Color(0xFF3F6F88)

fun isWeightMonitoringTask(title: String): Boolean {
    val normalized = title.trim().lowercase()
    return normalized == "weight monitoring" ||
            normalized.contains("weight") && normalized.contains("monitoring")
}

private fun averageWeightDisplay(weights: List<String>): String {
    val values = weights.mapNotNull { it.toDoubleOrNull() }
    if (values.isEmpty() || values.size != weights.size) return "-"
    return String.format("%.2f", values.sum() / values.size)
}

private fun weightStatusDisplay(averageText: String, targetText: String): String {
    val average = averageText.toDoubleOrNull() ?: return "-"
    val target = targetText.toDoubleOrNull() ?: return "-"
    return when {
        average < target -> "Underweight"
        average > target -> "Overweight"
        else -> "Normal"
    }
}

@Composable
fun PendingWeightMonitoringSection(
    task: PendingTaskDetailUiState,
    numberOfFlocks: String,
    flocksWithCases: String,
    targetWeight: String,
    weightSamples: List<String>,
    recordedAt: String,
    onNumberOfFlocksChange: (String) -> Unit,
    onFlocksWithCasesChange: (String) -> Unit,
    onTargetWeightChange: (String) -> Unit,
    onWeightSampleChange: (Int, String) -> Unit
) {
    val averageText = averageWeightDisplay(weightSamples)
    val statusText = weightStatusDisplay(averageText, targetWeight)

    WeightTaskSectionPanel {
        WeightTaskSectionHeader(
            title = "Weight Monitoring",
            accentColor = WeightTaskBlue
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                WeightTaskLabel("House")
                Spacer(modifier = Modifier.height(8.dp))
                WeightTaskReadOnlyField(task.houseLabel.ifBlank { "-" })
            }

            Column(modifier = Modifier.weight(1f)) {
                WeightTaskLabel("Pen")
                Spacer(modifier = Modifier.height(8.dp))
                WeightTaskReadOnlyField(task.penLabel.ifBlank { "-" })
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                WeightTaskLabel("Date")
                Spacer(modifier = Modifier.height(8.dp))
                WeightTaskReadOnlyField(formatWeightTaskDate(recordedAt))
            }

            Column(modifier = Modifier.weight(1f)) {
                WeightTaskLabel("Time")
                Spacer(modifier = Modifier.height(8.dp))
                WeightTaskReadOnlyField(formatWeightTaskTime(recordedAt))
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        WeightTaskLabel("Number of Flocks")
        Spacer(modifier = Modifier.height(8.dp))
        WeightTaskInputField(
            value = numberOfFlocks,
            keyboardType = KeyboardType.Number,
            onValueChange = onNumberOfFlocksChange
        )

        Spacer(modifier = Modifier.height(14.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                WeightTaskLabel("Cases")
                Spacer(modifier = Modifier.height(8.dp))
                WeightTaskInputField(
                    value = flocksWithCases,
                    keyboardType = KeyboardType.Number,
                    onValueChange = onFlocksWithCasesChange
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                WeightTaskLabel("Target Weight")
                Spacer(modifier = Modifier.height(8.dp))
                WeightTaskInputField(
                    value = targetWeight,
                    keyboardType = KeyboardType.Decimal,
                    onValueChange = onTargetWeightChange
                )
            }
        }

        if (weightSamples.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            WeightTaskDivider()
            Spacer(modifier = Modifier.height(16.dp))

            weightSamples.chunked(2).forEachIndexed { rowIndex, rowItems ->
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    rowItems.forEachIndexed { itemIndex, sample ->
                        val sampleIndex = rowIndex * 2 + itemIndex

                        Column(modifier = Modifier.weight(1f)) {
                            WeightTaskLabel("Flock ${sampleIndex + 1} Weight")
                            Spacer(modifier = Modifier.height(8.dp))
                            WeightTaskInputField(
                                value = sample,
                                keyboardType = KeyboardType.Decimal,
                                onValueChange = { onWeightSampleChange(sampleIndex, it) }
                            )
                        }
                    }

                    if (rowItems.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }

                if (rowIndex != weightSamples.chunked(2).lastIndex) {
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                WeightSummaryBox("Average", averageText, Modifier.weight(1f))
                WeightSummaryBox("Status", statusText, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun WeightTaskSectionPanel(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.94f),
                        WeightTaskSurface.copy(alpha = 0.98f)
                    )
                )
            )
            .border(1.dp, WeightTaskLine.copy(alpha = 0.82f), RoundedCornerShape(26.dp))
            .padding(horizontal = 18.dp, vertical = 18.dp),
        content = content
    )
}

@Composable
private fun WeightTaskSectionHeader(
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
            fontFamily = WeightTaskManrope,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 18.sp,
            color = WeightTaskInk
        )
    }
}

@Composable
private fun WeightTaskLabel(text: String) {
    Text(
        text = text,
        fontFamily = WeightTaskManrope,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 13.sp,
        color = WeightTaskInk
    )
}

@Composable
private fun WeightTaskReadOnlyField(value: String) {
    OutlinedTextField(
        value = value,
        onValueChange = {},
        readOnly = true,
        enabled = false,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        textStyle = TextStyle(
            fontFamily = WeightTaskManrope,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            color = WeightTaskMuted
        ),
        shape = RoundedCornerShape(18.dp),
        colors = OutlinedTextFieldDefaults.colors(
            disabledContainerColor = WeightTaskAutoField,
            disabledBorderColor = Color.Transparent,
            disabledTextColor = WeightTaskMuted
        )
    )
}

@Composable
private fun WeightTaskInputField(
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
            fontFamily = WeightTaskManrope,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            color = WeightTaskInk
        ),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = WeightTaskSurfaceAlt,
            unfocusedContainerColor = WeightTaskSurfaceAlt,
            focusedBorderColor = WeightTaskGreen,
            unfocusedBorderColor = Color.Transparent,
            focusedTextColor = WeightTaskInk,
            unfocusedTextColor = WeightTaskInk,
            cursorColor = WeightTaskGreen
        )
    )
}

@Composable
private fun WeightTaskDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        Color.Transparent,
                        WeightTaskLine.copy(alpha = 0.92f),
                        Color.Transparent
                    )
                )
            )
    )
}

@Composable
private fun WeightSummaryBox(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(WeightTaskSurfaceAlt)
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Text(
            text = label,
            fontFamily = WeightTaskManrope,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            color = WeightTaskMuted
        )

        Spacer(modifier = Modifier.height(5.dp))

        Text(
            text = value,
            fontFamily = WeightTaskManrope,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 15.sp,
            color = WeightTaskInk,
            lineHeight = 18.sp
        )
    }
}

private fun formatWeightTaskDate(value: String): String {
    return runCatching {
        LocalDateTime.parse(value, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            .format(DateTimeFormatter.ofPattern("M-d-yy", Locale.getDefault()))
    }.getOrDefault("-")
}

private fun formatWeightTaskTime(value: String): String {
    return runCatching {
        LocalDateTime.parse(value, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            .format(DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault()))
    }.getOrDefault("-")
}