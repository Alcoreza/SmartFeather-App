package com.example.smartfeather

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
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
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

private val PenCleaningManrope = FontFamily(
    Font(R.font.manrope_extralight, FontWeight.ExtraLight),
    Font(R.font.manrope_light, FontWeight.Light),
    Font(R.font.manrope_regular, FontWeight.Normal),
    Font(R.font.manrope_medium, FontWeight.Medium),
    Font(R.font.manrope_semibold, FontWeight.SemiBold),
    Font(R.font.manrope_bold, FontWeight.Bold),
    Font(R.font.manrope_extrabold, FontWeight.ExtraBold),
    Font(R.font.manrope_variablefont_wght, FontWeight.Black)
)

private val PenCleaningSurface = Color(0xFFFFFCF7)
private val PenCleaningSurfaceAlt = Color(0xFFF3EFE7)
private val PenCleaningAutoField = Color(0xFFE8E3DA)
private val PenCleaningInk = Color(0xFF121A14)
private val PenCleaningMuted = Color(0xFF677168)
private val PenCleaningLine = Color(0xFFD8D0C3)
private val PenCleaningGreen = Color(0xFF1F7A3A)

@Composable
fun PendingPenCleaningTaskForm(
    task: PendingTaskDetailUiState,
    materialsUsed: String,
    recordedAt: String,
    onMaterialsUsedChange: (String) -> Unit
) {
    PenCleaningSectionPanel {
        PenCleaningSectionHeader(
            title = "Pen Cleaning",
            subtitle = "Record the assigned pen cleaning work",
            accentColor = PenCleaningGreen
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                PenCleaningLabel("House")
                Spacer(modifier = Modifier.height(8.dp))
                PenCleaningReadOnlyField(task.houseLabel.ifBlank { "-" })
            }

            Column(modifier = Modifier.weight(1f)) {
                PenCleaningLabel("Pen")
                Spacer(modifier = Modifier.height(8.dp))
                PenCleaningReadOnlyField(task.penLabel.ifBlank { "-" })
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                PenCleaningLabel("Date")
                Spacer(modifier = Modifier.height(8.dp))
                PenCleaningReadOnlyField(formatPenCleaningDate(recordedAt))
            }

            Column(modifier = Modifier.weight(1f)) {
                PenCleaningLabel("Time")
                Spacer(modifier = Modifier.height(8.dp))
                PenCleaningReadOnlyField(formatPenCleaningTime(recordedAt))
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        PenCleaningLabel("Cleaning Materials Used")
        Spacer(modifier = Modifier.height(8.dp))
        PenCleaningTextArea(
            value = materialsUsed,
            placeholder = "Enter materials used",
            onValueChange = onMaterialsUsedChange
        )
    }
}

@Composable
private fun PenCleaningSectionPanel(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.94f),
                        PenCleaningSurface.copy(alpha = 0.98f)
                    )
                )
            )
            .border(1.dp, PenCleaningLine.copy(alpha = 0.82f), RoundedCornerShape(26.dp))
            .padding(horizontal = 18.dp, vertical = 18.dp),
        content = content
    )
}

@Composable
private fun PenCleaningSectionHeader(
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
                fontFamily = PenCleaningManrope,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp,
                color = PenCleaningInk
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = subtitle,
                fontFamily = PenCleaningManrope,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                color = PenCleaningMuted,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
private fun PenCleaningLabel(text: String) {
    Text(
        text = text,
        fontFamily = PenCleaningManrope,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 13.sp,
        color = PenCleaningInk
    )
}

@Composable
private fun PenCleaningReadOnlyField(value: String) {
    OutlinedTextField(
        value = value,
        onValueChange = {},
        readOnly = true,
        enabled = false,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        textStyle = TextStyle(
            fontFamily = PenCleaningManrope,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            color = PenCleaningMuted
        ),
        shape = RoundedCornerShape(18.dp),
        colors = OutlinedTextFieldDefaults.colors(
            disabledContainerColor = PenCleaningAutoField,
            disabledBorderColor = Color.Transparent,
            disabledTextColor = PenCleaningMuted
        )
    )
}

@Composable
private fun PenCleaningTextArea(
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        minLines = 3,
        maxLines = 5,
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Sentences
        ),
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 112.dp),
        textStyle = TextStyle(
            fontFamily = PenCleaningManrope,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            color = PenCleaningInk
        ),
        placeholder = {
            Text(
                text = placeholder,
                fontFamily = PenCleaningManrope,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = PenCleaningMuted.copy(alpha = 0.72f)
            )
        },
        shape = RoundedCornerShape(18.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = PenCleaningSurfaceAlt,
            unfocusedContainerColor = PenCleaningSurfaceAlt,
            focusedBorderColor = PenCleaningGreen,
            unfocusedBorderColor = Color.Transparent,
            focusedTextColor = PenCleaningInk,
            unfocusedTextColor = PenCleaningInk,
            cursorColor = PenCleaningGreen
        )
    )
}

private fun formatPenCleaningDate(value: String): String {
    return runCatching {
        LocalDateTime.parse(value, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            .format(DateTimeFormatter.ofPattern("M-d-yy", Locale.getDefault()))
    }.getOrDefault("-")
}

private fun formatPenCleaningTime(value: String): String {
    return runCatching {
        LocalDateTime.parse(value, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            .format(DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault()))
    }.getOrDefault("-")
}