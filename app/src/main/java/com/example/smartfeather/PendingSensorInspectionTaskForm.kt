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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
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

private val SensorInspectionManrope = FontFamily(
    Font(R.font.manrope_extralight, FontWeight.ExtraLight),
    Font(R.font.manrope_light, FontWeight.Light),
    Font(R.font.manrope_regular, FontWeight.Normal),
    Font(R.font.manrope_medium, FontWeight.Medium),
    Font(R.font.manrope_semibold, FontWeight.SemiBold),
    Font(R.font.manrope_bold, FontWeight.Bold),
    Font(R.font.manrope_extrabold, FontWeight.ExtraBold),
    Font(R.font.manrope_variablefont_wght, FontWeight.Black)
)

private val SensorInspectionSurface = Color(0xFFFFFCF7)
private val SensorInspectionSurfaceAlt = Color(0xFFF3EFE7)
private val SensorInspectionAutoField = Color(0xFFE8E3DA)
private val SensorInspectionInk = Color(0xFF121A14)
private val SensorInspectionMuted = Color(0xFF677168)
private val SensorInspectionLine = Color(0xFFD8D0C3)
private val SensorInspectionTeal = Color(0xFF2F8F88)

data class SensorInspectionChecklistState(
    val sensorPresent: Boolean = false,
    val sensorCleanUnblocked: Boolean = false,
    val noVisibleDamageOrLooseWiring: Boolean = false,
    val powerStatusOn: Boolean = false,
    val placementSecure: Boolean = false
)

@Composable
fun PendingSensorInspectionTaskForm(
    task: PendingTaskDetailUiState,
    checklist: SensorInspectionChecklistState,
    recordedAt: String,
    onChecklistChange: (SensorInspectionChecklistState) -> Unit
) {
    SensorInspectionSectionPanel {
        SensorInspectionSectionHeader(
            title = "Sensor Inspection",
            accentColor = SensorInspectionTeal
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                SensorInspectionLabel("House")
                Spacer(modifier = Modifier.height(8.dp))
                SensorInspectionReadOnlyField(task.houseLabel.ifBlank { "-" })
            }

            Column(modifier = Modifier.weight(1f)) {
                SensorInspectionLabel("Pen")
                Spacer(modifier = Modifier.height(8.dp))
                SensorInspectionReadOnlyField(task.penLabel.ifBlank { "-" })
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                SensorInspectionLabel("Date")
                Spacer(modifier = Modifier.height(8.dp))
                SensorInspectionReadOnlyField(formatSensorInspectionDate(recordedAt))
            }

            Column(modifier = Modifier.weight(1f)) {
                SensorInspectionLabel("Time")
                Spacer(modifier = Modifier.height(8.dp))
                SensorInspectionReadOnlyField(formatSensorInspectionTime(recordedAt))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        SensorInspectionChecklistItem(
            text = "Sensor is physically present in the assigned pen",
            checked = checklist.sensorPresent,
            onCheckedChange = {
                onChecklistChange(checklist.copy(sensorPresent = it))
            }
        )

        Spacer(modifier = Modifier.height(10.dp))

        SensorInspectionChecklistItem(
            text = "Sensor is clean and not blocked by dust, feathers, feed, or equipment",
            checked = checklist.sensorCleanUnblocked,
            onCheckedChange = {
                onChecklistChange(checklist.copy(sensorCleanUnblocked = it))
            }
        )

        Spacer(modifier = Modifier.height(10.dp))

        SensorInspectionChecklistItem(
            text = "Sensor has no visible damage or loose wiring",
            checked = checklist.noVisibleDamageOrLooseWiring,
            onCheckedChange = {
                onChecklistChange(checklist.copy(noVisibleDamageOrLooseWiring = it))
            }
        )

        Spacer(modifier = Modifier.height(10.dp))

        SensorInspectionChecklistItem(
            text = "Power/status indicator is on",
            checked = checklist.powerStatusOn,
            onCheckedChange = {
                onChecklistChange(checklist.copy(powerStatusOn = it))
            }
        )

        Spacer(modifier = Modifier.height(10.dp))

        SensorInspectionChecklistItem(
            text = "Sensor placement is secure and not tilted, fallen, or moved",
            checked = checklist.placementSecure,
            onCheckedChange = {
                onChecklistChange(checklist.copy(placementSecure = it))
            }
        )
    }
}

@Composable
private fun SensorInspectionSectionPanel(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.94f),
                        SensorInspectionSurface.copy(alpha = 0.98f)
                    )
                )
            )
            .border(1.dp, SensorInspectionLine.copy(alpha = 0.82f), RoundedCornerShape(26.dp))
            .padding(horizontal = 18.dp, vertical = 18.dp),
        content = content
    )
}

@Composable
private fun SensorInspectionSectionHeader(
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
            fontFamily = SensorInspectionManrope,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 18.sp,
            color = SensorInspectionInk
        )
    }
}

@Composable
private fun SensorInspectionLabel(text: String) {
    Text(
        text = text,
        fontFamily = SensorInspectionManrope,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 13.sp,
        color = SensorInspectionInk
    )
}

@Composable
private fun SensorInspectionReadOnlyField(value: String) {
    OutlinedTextField(
        value = value,
        onValueChange = {},
        readOnly = true,
        enabled = false,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
        textStyle = TextStyle(
            fontFamily = SensorInspectionManrope,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            color = SensorInspectionMuted
        ),
        shape = RoundedCornerShape(18.dp),
        colors = OutlinedTextFieldDefaults.colors(
            disabledContainerColor = SensorInspectionAutoField,
            disabledBorderColor = Color.Transparent,
            disabledTextColor = SensorInspectionMuted
        )
    )
}

@Composable
private fun SensorInspectionChecklistItem(
    text: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(SensorInspectionSurfaceAlt.copy(alpha = 0.72f))
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 12.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                checkedColor = SensorInspectionTeal,
                uncheckedColor = SensorInspectionMuted.copy(alpha = 0.55f),
                checkmarkColor = Color.White
            )
        )

        Spacer(modifier = Modifier.size(8.dp))

        Text(
            text = text,
            modifier = Modifier.weight(1f),
            fontFamily = SensorInspectionManrope,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
            color = SensorInspectionInk,
            lineHeight = 18.sp
        )
    }
}

private fun formatSensorInspectionDate(value: String): String {
    return runCatching {
        LocalDateTime.parse(value, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            .format(DateTimeFormatter.ofPattern("M-d-yy", Locale.getDefault()))
    }.getOrDefault("-")
}

private fun formatSensorInspectionTime(value: String): String {
    return runCatching {
        LocalDateTime.parse(value, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            .format(DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault()))
    }.getOrDefault("-")
}