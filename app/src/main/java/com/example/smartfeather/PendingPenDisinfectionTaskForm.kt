package com.example.smartfeather

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
    Font(R.font.manrope_regular, FontWeight.Normal),
    Font(R.font.manrope_medium, FontWeight.Medium),
    Font(R.font.manrope_semibold, FontWeight.SemiBold),
    Font(R.font.manrope_bold, FontWeight.Bold),
    Font(R.font.manrope_extrabold, FontWeight.ExtraBold)
)

private val PenDisinfectionSurface = Color(0xFFFFFCF7)
private val PenDisinfectionInk = Color(0xFF121A14)
private val PenDisinfectionMuted = Color(0xFF677168)
private val PenDisinfectionLine = Color(0xFFD8D0C3)
private val PenDisinfectionGreen = Color(0xFF1F7A3A)
private val PenDisinfectionField = Color(0xFFF3EFE7)

@Composable
fun PendingPenDisinfectionTaskForm(
    task: PendingTaskDetailUiState,
    activity: String,
    disinfectantUsed: String,
    recordedAt: String,
    onActivityChange: (String) -> Unit,
    onDisinfectantUsedChange: (String) -> Unit
) {
    PenDisinfectionPanel {
        PenDisinfectionHeader(
            title = "Pen Disinfection",
            subtitle = "Complete the cleaning log for the assigned pen."
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(1f)) {
                PenDisinfectionLabel("House")
                Spacer(modifier = Modifier.height(8.dp))
                PenDisinfectionReadOnlyField(task.houseLabel.ifBlank { "-" })
            }

            Spacer(modifier = Modifier.height(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                PenDisinfectionLabel("Pen")
                Spacer(modifier = Modifier.height(8.dp))
                PenDisinfectionReadOnlyField(task.penLabel.ifBlank { "-" })
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(1f)) {
                PenDisinfectionLabel("Date")
                Spacer(modifier = Modifier.height(8.dp))
                PenDisinfectionReadOnlyField(formatDisinfectionDate(recordedAt))
            }

            Spacer(modifier = Modifier.height(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                PenDisinfectionLabel("Time")
                Spacer(modifier = Modifier.height(8.dp))
                PenDisinfectionReadOnlyField(formatDisinfectionTime(recordedAt))
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        PenDisinfectionLabel("Activity")
        Spacer(modifier = Modifier.height(8.dp))
        PenDisinfectionTextField(
            value = activity,
            placeholder = "Pen Disinfection",
            onValueChange = onActivityChange
        )

        Spacer(modifier = Modifier.height(14.dp))

        PenDisinfectionLabel("Disinfectant Used")
        Spacer(modifier = Modifier.height(8.dp))
        PenDisinfectionTextField(
            value = disinfectantUsed,
            placeholder = "Enter disinfectant used",
            onValueChange = onDisinfectantUsedChange
        )
    }
}

@Composable
private fun PenDisinfectionPanel(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.96f),
                        PenDisinfectionSurface
                    )
                )
            )
            .border(1.dp, PenDisinfectionLine.copy(alpha = 0.82f), RoundedCornerShape(26.dp))
            .padding(horizontal = 18.dp, vertical = 18.dp),
        content = content
    )
}

@Composable
private fun PenDisinfectionHeader(
    title: String,
    subtitle: String
) {
    Row {
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .padding(top = 4.dp)
                .height(38.dp)
                .fillMaxWidth(0.012f)
                .clip(RoundedCornerShape(999.dp))
                .background(PenDisinfectionGreen)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Column(modifier = Modifier.padding(start = 12.dp)) {
            Text(
                text = title,
                fontFamily = PenDisinfectionManrope,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp,
                color = PenDisinfectionInk
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = subtitle,
                fontFamily = PenDisinfectionManrope,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                color = PenDisinfectionMuted,
                lineHeight = 16.sp
            )
        }
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
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        textStyle = TextStyle(
            fontFamily = PenDisinfectionManrope,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            color = PenDisinfectionMuted
        ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = PenDisinfectionField,
            unfocusedContainerColor = PenDisinfectionField,
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent,
            focusedTextColor = PenDisinfectionMuted,
            unfocusedTextColor = PenDisinfectionMuted,
            cursorColor = PenDisinfectionGreen
        )
    )
}

@Composable
private fun PenDisinfectionTextField(
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Words
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
        textStyle = TextStyle(
            fontFamily = PenDisinfectionManrope,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            color = PenDisinfectionInk
        ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = PenDisinfectionField,
            unfocusedContainerColor = PenDisinfectionField,
            focusedBorderColor = PenDisinfectionGreen,
            unfocusedBorderColor = Color.Transparent,
            focusedTextColor = PenDisinfectionInk,
            unfocusedTextColor = PenDisinfectionInk,
            cursorColor = PenDisinfectionGreen
        )
    )
}

private fun formatDisinfectionDate(value: String): String {
    return runCatching {
        LocalDateTime.parse(value, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            .format(DateTimeFormatter.ofPattern("M-d-yy", Locale.getDefault()))
    }.getOrDefault("-")
}

private fun formatDisinfectionTime(value: String): String {
    return runCatching {
        LocalDateTime.parse(value, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            .format(DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault()))
    }.getOrDefault("-")
}