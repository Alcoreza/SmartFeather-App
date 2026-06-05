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

private val HatchManrope = FontFamily(
    Font(R.font.manrope_extralight, FontWeight.ExtraLight),
    Font(R.font.manrope_light, FontWeight.Light),
    Font(R.font.manrope_regular, FontWeight.Normal),
    Font(R.font.manrope_medium, FontWeight.Medium),
    Font(R.font.manrope_semibold, FontWeight.SemiBold),
    Font(R.font.manrope_bold, FontWeight.Bold),
    Font(R.font.manrope_extrabold, FontWeight.ExtraBold),
    Font(R.font.manrope_variablefont_wght, FontWeight.Black)
)

private val HatchSurface = Color(0xFFFFFCF7)
private val HatchSurfaceAlt = Color(0xFFF3EFE7)
private val HatchAutoField = Color(0xFFE8E3DA)
private val HatchInk = Color(0xFF121A14)
private val HatchMuted = Color(0xFF677168)
private val HatchLine = Color(0xFFD8D0C3)
private val HatchGreen = Color(0xFF1F7A3A)
private val HatchRed = Color(0xFFB54A3C)

fun isHatchAndMortalityTask(title: String): Boolean {
    val normalized = title.trim().lowercase()
    return normalized == "hatch and mortality check" ||
            normalized.contains("hatch") && normalized.contains("mortality")
}

@Composable
fun PendingHatchMortalitySection(
    task: PendingTaskDetailUiState,
    eggsHatched: String,
    mortality: String,
    recordedAt: String,
    onEggsChange: (String) -> Unit,
    onMortalityChange: (String) -> Unit
) {
    HatchSectionPanel {
        HatchSectionHeader(
            title = "Hatch and Mortality",
            subtitle = "Record production changes for the assigned pen",
            accentColor = HatchRed
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                HatchLabel("House")
                Spacer(modifier = Modifier.height(8.dp))
                HatchReadOnlyField(task.houseLabel.ifBlank { "-" })
            }

            Column(modifier = Modifier.weight(1f)) {
                HatchLabel("Pen")
                Spacer(modifier = Modifier.height(8.dp))
                HatchReadOnlyField(task.penLabel.ifBlank { "-" })
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                HatchLabel("Date")
                Spacer(modifier = Modifier.height(8.dp))
                HatchReadOnlyField(formatHatchDate(recordedAt))
            }

            Column(modifier = Modifier.weight(1f)) {
                HatchLabel("Time")
                Spacer(modifier = Modifier.height(8.dp))
                HatchReadOnlyField(formatHatchTime(recordedAt))
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        HatchLabel("Eggs Hatched")
        Spacer(modifier = Modifier.height(8.dp))
        HatchInputField(
            value = eggsHatched,
            keyboardType = KeyboardType.Number,
            onValueChange = onEggsChange
        )

        Spacer(modifier = Modifier.height(14.dp))

        HatchLabel("Mortalities")
        Spacer(modifier = Modifier.height(8.dp))
        HatchInputField(
            value = mortality,
            keyboardType = KeyboardType.Number,
            onValueChange = onMortalityChange
        )
    }
}

@Composable
private fun HatchSectionPanel(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.94f),
                        HatchSurface.copy(alpha = 0.98f)
                    )
                )
            )
            .border(1.dp, HatchLine.copy(alpha = 0.82f), RoundedCornerShape(26.dp))
            .padding(horizontal = 18.dp, vertical = 18.dp),
        content = content
    )
}

@Composable
private fun HatchSectionHeader(
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
                fontFamily = HatchManrope,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp,
                color = HatchInk
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = subtitle,
                fontFamily = HatchManrope,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                color = HatchMuted,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
private fun HatchLabel(text: String) {
    Text(
        text = text,
        fontFamily = HatchManrope,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 13.sp,
        color = HatchInk
    )
}

@Composable
private fun HatchReadOnlyField(value: String) {
    OutlinedTextField(
        value = value,
        onValueChange = {},
        readOnly = true,
        enabled = false,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        textStyle = TextStyle(
            fontFamily = HatchManrope,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            color = HatchMuted
        ),
        shape = RoundedCornerShape(18.dp),
        colors = OutlinedTextFieldDefaults.colors(
            disabledContainerColor = HatchAutoField,
            disabledBorderColor = Color.Transparent,
            disabledTextColor = HatchMuted
        )
    )
}

@Composable
private fun HatchInputField(
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
            fontFamily = HatchManrope,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            color = HatchInk
        ),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = HatchSurfaceAlt,
            unfocusedContainerColor = HatchSurfaceAlt,
            focusedBorderColor = HatchGreen,
            unfocusedBorderColor = Color.Transparent,
            focusedTextColor = HatchInk,
            unfocusedTextColor = HatchInk,
            cursorColor = HatchGreen
        )
    )
}

private fun formatHatchDate(value: String): String {
    return runCatching {
        LocalDateTime.parse(value, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            .format(DateTimeFormatter.ofPattern("M-d-yy", Locale.getDefault()))
    }.getOrDefault("-")
}

private fun formatHatchTime(value: String): String {
    return runCatching {
        LocalDateTime.parse(value, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            .format(DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault()))
    }.getOrDefault("-")
}