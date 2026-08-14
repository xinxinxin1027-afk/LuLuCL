package com.vishal2376.snaptick.presentation.calender_screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.vishal2376.snaptick.domain.model.Task
import com.vishal2376.snaptick.presentation.common.h1TextStyle
import com.vishal2376.snaptick.presentation.common.infoDescTextStyle
import com.vishal2376.snaptick.presentation.common.taskDescTextStyle
import com.vishal2376.snaptick.presentation.common.taskTextStyle
import com.vishal2376.snaptick.presentation.home_screen.components.EmptyTaskComponent
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
internal fun AgendaPanel(
    modifier: Modifier,
    selectedDay: LocalDate,
    tasks: List<Task>,
    onCreate: () -> Unit,
    onEdit: (Task) -> Unit,
    onToggleCompletion: (Task) -> Unit,
    onTogglePin: (Task) -> Unit,
    onDelete: (Task) -> Unit,
    onToggleSubtask: (Task, String) -> Unit,
    onHeaderClick: (() -> Unit)? = null,
) {
    val pendingCount = tasks.count { !it.isCompleted }
    val completedCount = tasks.count { it.isCompleted }

    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = onHeaderClick != null) { onHeaderClick?.invoke() }
                .padding(horizontal = 20.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = selectedDay.format(
                        DateTimeFormatter.ofPattern("M月d日 EEEE", Locale.getDefault())
                    ),
                    style = h1TextStyle,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$pendingCount 个待完成 · $completedCount 个已完成",
                    style = infoDescTextStyle,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
            FilledTonalButton(onClick = onCreate) { Text("新建") }
        }

        Divider(color = MaterialTheme.colorScheme.primaryContainer)

        if (tasks.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                EmptyTaskComponent()
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                itemsIndexed(tasks, key = { _, task -> task.id }) { _, task ->
                    CalendarTodoCard(
                        task = task,
                        selectedDay = selectedDay,
                        onEdit = { onEdit(task) },
                        onToggleCompletion = { onToggleCompletion(task) },
                        onTogglePin = { onTogglePin(task) },
                        onDelete = { onDelete(task) },
                        onToggleSubtask = { id -> onToggleSubtask(task, id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun CalendarTodoCard(
    task: Task,
    selectedDay: LocalDate,
    onEdit: () -> Unit,
    onToggleCompletion: () -> Unit,
    onTogglePin: () -> Unit,
    onDelete: () -> Unit,
    onToggleSubtask: (String) -> Unit,
) {
    val accent = taskAccent(task, MaterialTheme.colorScheme.primary)
    val subtasks = task.subtasks()
    val completedSubtasks = subtasks.count { it.completed }
    val progress = if (subtasks.isEmpty()) 0f else completedSubtasks.toFloat() / subtasks.size

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .width(7.dp)
                    .height(if (subtasks.isEmpty()) 144.dp else 196.dp)
                    .background(accent),
            )
            Column(modifier = Modifier.weight(1f)) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onEdit)
                        .padding(start = 14.dp, top = 14.dp, end = 14.dp, bottom = 8.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onToggleCompletion, modifier = Modifier.size(36.dp)) {
                            Icon(
                                imageVector = if (task.isCompleted) Icons.Default.CheckCircle else Icons.Default.Check,
                                contentDescription = "完成",
                                tint = if (task.isCompleted) accent else MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                        }
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = task.title,
                            style = taskTextStyle,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f),
                        )
                        if (task.isPinned) {
                            Surface(
                                shape = RoundedCornerShape(999.dp),
                                color = accent.copy(alpha = 0.13f),
                            ) {
                                Text(
                                    text = "置顶",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = taskDescTextStyle,
                                    color = accent,
                                )
                            }
                        }
                    }

                    val timeText = taskTimeText(task)
                    Row(
                        modifier = Modifier.padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        MetaPill(text = timeText)
                        firstReminderText(task)?.let { MetaPill(text = it, accent = accent) }
                        if (subtasks.isNotEmpty()) {
                            MetaPill(text = "$completedSubtasks/${subtasks.size} 子任务")
                        }
                    }

                    if (task.description.isNotBlank()) {
                        Text(
                            text = task.description,
                            modifier = Modifier.padding(top = 8.dp),
                            style = infoDescTextStyle,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }

                    if (subtasks.isNotEmpty()) {
                        LinearProgressIndicator(
                            progress = progress,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp)
                                .height(4.dp),
                            color = accent,
                        )
                        Column(modifier = Modifier.padding(top = 6.dp)) {
                            subtasks.take(4).forEach { subtask ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onToggleSubtask(subtask.id) }
                                        .padding(vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(18.dp)
                                            .border(1.dp, accent, CircleShape)
                                            .background(
                                                if (subtask.completed) accent else Color.Transparent,
                                                CircleShape,
                                            ),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        if (subtask.completed) {
                                            Icon(
                                                Icons.Default.Check,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(13.dp),
                                            )
                                        }
                                    }
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = subtask.title,
                                        style = taskDescTextStyle,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    )
                                }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedButton(onClick = onTogglePin, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.PushPin, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(if (task.isPinned) "取消置顶" else "置顶")
                    }
                    OutlinedButton(onClick = onDelete, modifier = Modifier.weight(1f)) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.error,
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("删除", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

@Composable
private fun MetaPill(text: String, accent: Color? = null) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = (accent ?: MaterialTheme.colorScheme.onPrimaryContainer).copy(alpha = 0.08f),
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = taskDescTextStyle,
            color = accent ?: MaterialTheme.colorScheme.onPrimaryContainer,
            maxLines = 1,
        )
    }
}

private fun taskTimeText(task: Task): String {
    val startDate = task.date
    val endDate = task.safeEndDate()
    val dateText = if (endDate != startDate) "$startDate 至 $endDate" else startDate.toString()
    if (task.isAllDayTaskEnabled()) return dateText
    return "$dateText ${task.startTime}-${task.endTime}"
}

private fun firstReminderText(task: Task): String? {
    val firstOffset = task.reminderOffsets().firstOrNull() ?: return null
    val anchor = LocalDateTime.of(task.date, task.startTime)
    val reminder = anchor.minusMinutes(firstOffset.toLong())
    return "提醒 ${reminder.format(DateTimeFormatter.ofPattern("MM-dd HH:mm"))}"
}

private fun taskAccent(task: Task, fallback: Color): Color =
    task.colorHex.toComposeColor(
        Quadrants.getOrNull(task.quadrant - 1)?.color?.toComposeColor(fallback) ?: fallback
    )
