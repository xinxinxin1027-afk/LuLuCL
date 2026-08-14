package com.vishal2376.snaptick.presentation.calender_screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.vishal2376.snaptick.presentation.common.infoDescTextStyle
import com.vishal2376.snaptick.presentation.common.taskDescTextStyle
import com.vishal2376.snaptick.presentation.common.taskTextStyle
import java.time.LocalDateTime
import java.time.LocalTime

@Composable
internal fun EditorSection(
    title: String,
    trailingText: String? = null,
    action: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                modifier = Modifier.weight(1f),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            trailingText?.let {
                Text(it, style = infoDescTextStyle, color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
            action?.invoke()
        }
        content()
    }
}

@Composable
internal fun DateTimeEditor(
    state: CalendarEditorState,
    onStateChange: (CalendarEditorState) -> Unit,
) {
    val context = androidx.compose.ui.platform.LocalContext.current

    EditorSection(title = "时间") {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            DateValueButton(
                modifier = Modifier.weight(1f),
                label = "开始日期",
                value = state.startDate.toString(),
                onClick = {
                    pickDate(context, state.startDate) { date ->
                        val adjustedEnd = state.endDate?.takeIf { !it.isBefore(date) }
                        onStateChange(state.copy(startDate = date, endDate = adjustedEnd))
                    }
                },
            )
            DateValueButton(
                modifier = Modifier.weight(1f),
                label = "开始时间",
                value = if (state.allDay) "全天" else state.startTime.toString(),
                onClick = {
                    pickTime(context, state.startTime) { time ->
                        onStateChange(state.copy(startTime = time, allDay = false))
                    }
                },
            )
        }
        if (!state.allDay) {
            TextButton(onClick = { onStateChange(state.copy(allDay = true)) }) {
                Text("改为全天")
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            DateValueButton(
                modifier = Modifier.weight(1f),
                label = "截止日期",
                value = state.endDate?.toString() ?: "不设置",
                onClick = {
                    pickDate(context, state.endDate ?: state.startDate) { date ->
                        onStateChange(state.copy(endDate = if (date < state.startDate) state.startDate else date))
                    }
                },
            )
            DateValueButton(
                modifier = Modifier.weight(1f),
                label = "截止时间",
                value = state.endTime?.toString() ?: "默认 23:59",
                onClick = {
                    pickTime(context, state.endTime ?: LocalTime.of(23, 59)) { time ->
                        onStateChange(state.copy(endTime = time, allDay = false))
                    }
                },
            )
        }
        if (state.endDate != null) {
            TextButton(onClick = { onStateChange(state.copy(endDate = null, endTime = null)) }) {
                Text("清除截止时间")
            }
        }
    }
}

@Composable
internal fun DateValueButton(
    modifier: Modifier,
    label: String,
    value: String,
    onClick: () -> Unit,
) {
    OutlinedButton(onClick = onClick, modifier = modifier) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(label, style = infoDescTextStyle)
            Text(value, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
internal fun QuadrantCard(
    modifier: Modifier,
    option: QuadrantOption,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val accent = option.color.toComposeColor(MaterialTheme.colorScheme.primary)
    Surface(
        modifier = modifier
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) accent else MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(16.dp),
            )
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = if (selected) accent.copy(alpha = 0.08f) else MaterialTheme.colorScheme.background,
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.size(14.dp).background(accent, CircleShape))
            Spacer(Modifier.width(8.dp))
            Column {
                Text(option.title, fontWeight = FontWeight.Bold, style = taskDescTextStyle)
                Text(option.description, style = infoDescTextStyle)
            }
        }
    }
}

@Composable
internal fun ReminderRow(
    reminder: LocalDateTime,
    onChange: (LocalDateTime) -> Unit,
    onRemove: () -> Unit,
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        OutlinedButton(
            onClick = {
                pickDate(context, reminder.toLocalDate()) {
                    onChange(LocalDateTime.of(it, reminder.toLocalTime()))
                }
            },
            modifier = Modifier.weight(1f),
        ) { Text(reminder.toLocalDate().toString()) }
        OutlinedButton(
            onClick = {
                pickTime(context, reminder.toLocalTime()) {
                    onChange(LocalDateTime.of(reminder.toLocalDate(), it))
                }
            },
            modifier = Modifier.weight(0.7f),
        ) { Text(reminder.toLocalTime().toString()) }
        IconButton(onClick = onRemove) {
            Icon(Icons.Default.Delete, contentDescription = "移除")
        }
    }
}

