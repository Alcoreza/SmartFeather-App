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

private val ChickPlacementManrope = FontFamily(
    Font(R.font.manrope_extralight, FontWeight.ExtraLight),
    Font(R.font.manrope_light, FontWeight.Light),
    Font(R.font.manrope_regular, FontWeight.Normal),
    Font(R.font.manrope_medium, FontWeight.Medium),
    Font(R.font.manrope_semibold, FontWeight.SemiBold),
    Font(R.font.manrope_bold, FontWeight.Bold),
    Font(R.font.manrope_extrabold, FontWeight.ExtraBold),
    Font(R.font.manrope_variablefont_wght, FontWeight.Black)
)

private val ChickPlacementSurface = Color(0xFFFFFCF7)
private val ChickPlacementSurfaceAlt = Color(0xFFF3EFE7)
private val ChickPlacementAutoField = Color(0xFFE8E3DA)
private val ChickPlacementInk = Color(0xFF121A14)
private val ChickPlacementMuted = Color(0xFF677168)
private val ChickPlacementLine = Color(0xFFD8D0C3)
private val ChickPlacementBlue = Color(0xFF3D6F9F)

@Composable
fun PendingChickPlacementTaskForm(
    task: PendingTaskDetailUiState,
    batchCode: String,
    initialPopulation: String,
    recordedAt: String,
    onBatchCodeChange: (String) -> Unit,
    onInitialPopulationChange: (String) -> Unit
) {
    ChickPlacementSectionPanel {
        ChickPlacementSectionHeader(
            title = "Chick Placement",
            subtitle = "Start a new flock batch in the assigned pen",
            accentColor = ChickPlacementBlue
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                ChickPlacementLabel("House")
                Spacer(modifier = Modifier.height(8.dp))
                ChickPlacementReadOnlyField(task.houseLabel.ifBlank { "-" })
            }

            Column(modifier = Modifier.weight(1f)) {
                ChickPlacementLabel("Pen")
                Spacer(modifier = Modifier.height(8.dp))
                ChickPlacementReadOnlyField(task.penLabel.ifBlank { "-" })
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                ChickPlacementLabel("Date")
                Spacer(modifier = Modifier.height(8.dp))
                ChickPlacementReadOnlyField(formatChickPlacementDate(recordedAt))
            }

            Column(modifier = Modifier.weight(1f)) {
                ChickPlacementLabel("Time")
                Spacer(modifier = Modifier.height(8.dp))
                ChickPlacementReadOnlyField(formatChickPlacementTime(recordedAt))
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        ChickPlacementLabel("Batch Code")
        Spacer(modifier = Modifier.height(8.dp))
        ChickPlacementInputField(
            value = batchCode,
            placeholder = "Enter batch code",
            keyboardType = KeyboardType.Text,
            onValueChange = onBatchCodeChange
        )

        Spacer(modifier = Modifier.height(14.dp))

        ChickPlacementLabel("Initial Population")
        Spacer(modifier = Modifier.height(8.dp))
        ChickPlacementInputField(
            value = initialPopulation,
            placeholder = "Enter number of chicks",
            keyboardType = KeyboardType.Number,
            onValueChange = { value ->
                onInitialPopulationChange(value.filter { it.isDigit() })
            }
        )
    }
}

@Composable
private fun ChickPlacementSectionPanel(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.94f),
                        ChickPlacementSurface.copy(alpha = 0.98f)
                    )
                )
            )
            .border(1.dp, ChickPlacementLine.copy(alpha = 0.82f), RoundedCornerShape(26.dp))
            .padding(horizontal = 18.dp, vertical = 18.dp),
        content = content
    )
}

@Composable
private fun ChickPlacementSectionHeader(
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
                fontFamily = ChickPlacementManrope,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp,
                color = ChickPlacementInk
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = subtitle,
                fontFamily = ChickPlacementManrope,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                color = ChickPlacementMuted,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
private fun ChickPlacementLabel(text: String) {
    Text(
        text = text,
        fontFamily = ChickPlacementManrope,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 13.sp,
        color = ChickPlacementInk
    )
}

@Composable
private fun ChickPlacementReadOnlyField(value: String) {
    OutlinedTextField(
        value = value,
        onValueChange = {},
        readOnly = true,
        enabled = false,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        textStyle = TextStyle(
            fontFamily = ChickPlacementManrope,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            color = ChickPlacementMuted
        ),
        shape = RoundedCornerShape(18.dp),
        colors = OutlinedTextFieldDefaults.colors(
            disabledContainerColor = ChickPlacementAutoField,
            disabledBorderColor = Color.Transparent,
            disabledTextColor = ChickPlacementMuted
        )
    )
}

@Composable
private fun ChickPlacementInputField(
    value: String,
    placeholder: String,
    keyboardType: KeyboardType,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = Modifier.fillMaxWidth(),
        textStyle = TextStyle(
            fontFamily = ChickPlacementManrope,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            color = ChickPlacementInk
        ),
        placeholder = {
            Text(
                text = placeholder,
                fontFamily = ChickPlacementManrope,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = ChickPlacementMuted.copy(alpha = 0.72f)
            )
        },
        shape = RoundedCornerShape(18.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = ChickPlacementSurfaceAlt,
            unfocusedContainerColor = ChickPlacementSurfaceAlt,
            focusedBorderColor = ChickPlacementBlue,
            unfocusedBorderColor = Color.Transparent,
            focusedTextColor = ChickPlacementInk,
            unfocusedTextColor = ChickPlacementInk,
            cursorColor = ChickPlacementBlue
        )
    )
}

private fun formatChickPlacementDate(value: String): String {
    return runCatching {
        LocalDateTime.parse(value, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            .format(DateTimeFormatter.ofPattern("M-d-yy", Locale.getDefault()))
    }.getOrDefault("-")
}

private fun formatChickPlacementTime(value: String): String {
    return runCatching {
        LocalDateTime.parse(value, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            .format(DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault()))
    }.getOrDefault("-")
}