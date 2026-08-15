package com.vishal2376.snaptick.presentation.calender_screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vishal2376.snaptick.domain.model.Task
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
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
    val colors = LuluCalendarTheme.colors
    val pending = tasks.filterNot { it.isCompleted }
    val completed = tasks.filter { it.isCompleted }
    Column(modifier = modifier.background(colors.canvas)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = onHeaderClick != null) { onHeaderClick?.invoke() }
                .padding(start = 18.dp, end = 12.dp, top = 10.dp, bottom = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${selectedDay.monthValue}月${selectedDay.dayOfMonth}日 ${selectedDay.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.CHINA)}",
                    color = colors.ink,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = if (tasks.isEmpty()) "暂无安排" else "${pending.size} 项待完成 · ${completed.size} 项已完成",
                    color = colors.tertiaryInk,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 3.dp),
                )
            }
            LuluIconButton(
                icon = Icons.Default.Add,
                contentDescription = "新建日程",
                onClick = onCreate,
                brand = true,
            )
        }
        Divider(color = colors.divider)
        if (tasks.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 28.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Surface(
                    modifier = Modifier.size(66.dp),
                    shape = RoundedCornerShape(22.dp),
                    color = colors.brandSoft,
                    contentColor = colors.brand,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(30.dp))
                    }
                }
                Text(
                    text = "今天还没有安排",
                    color = colors.ink,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 12.dp),
                )
                Text(
                    text = "为这一天添加一件想完成的事",
                    color = colors.tertiaryInk,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(9.dp),
            ) {
                items(tasks, key = { it.id }) { task ->
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
    val colors = LuluCalendarTheme.colors
    val accent = taskAccent(task, colors.brand)
    val subtasks = task.subtasks()
    val completedSubtasks = subtasks.count { it.completed }
    val progress = if (subtasks.isEmpty()) null else completedSubtasks.toFloat() / subtasks.size
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = colors.surface,
        contentColor = colors.ink,
        border = BorderStroke(1.dp, colors.outline),
        onClick = onEdit,
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(accent),
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 10.dp, end = 6.dp, top = 11.dp, bottom = 10.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Column(
                    modifier = Modifier.width(47.dp),
                    horizontalAlignment = Alignment.End,
                ) {
                    Text(
                        text = if (task.isAllDayTaskEnabled()) "全天" else task.startTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                        color = colors.secondaryInk,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    if (!task.isAllDayTaskEnabled()) {
                        Text(
                            text = task.endTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                            color = colors.tertiaryInk,
                            fontSize = 9.sp,
                            modifier = Modifier.padding(top = 2.dp),
                        )
                    }
                }
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = task.title,
                            color = if (task.isCompleted) colors.secondaryInk else colors.ink,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f),
                        )
                        if (task.isPinned) {
                            Icon(
                                imageVector = Icons.Default.PushPin,
                                contentDescription = "已置顶",
                                tint = colors.brand,
                                modifier = Modifier
                                    .padding(start = 5.dp)
                                    .size(13.dp),
                            )
                        }
                    }
                    if (task.description.isNotBlank()) {
                        Text(
                            text = task.description,
                            color = colors.secondaryInk,
                            fontSize = 11.sp,
                            lineHeight = 15.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = 3.dp),
                        )
                    }
                    Row(
                        modifier = Modifier.padding(top = 7.dp),
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                    ) {
                        MetaPill(quadrantLabel(task.quadrant), quadrantColor(task.quadrant))
                        if (task.reminderOffsets().isNotEmpty()) MetaPill("提醒", colors.secondaryInk)
                        if (subtasks.isNotEmpty()) MetaPill("$completedSubtasks/${subtasks.size} 子任务", colors.secondaryInk)
                    }
                    progress?.let {
                        LinearProgressIndicator(
                            progress = it,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                                .height(4.dp),
                            color = accent,
                            trackColor = colors.mutedStrong,
                        )
                    }
                    if (subtasks.isNotEmpty()) {
                        Column(modifier = Modifier.padding(top = 5.dp)) {
                            subtasks.take(3).forEach { subtask ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(40.dp)
                                        .clickable { onToggleSubtask(subtask.id) },
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Surface(
                                        modifier = Modifier.size(22.dp),
                                        shape = RoundedCornerShape(7.dp),
                                        color = if (subtask.completed) accent else Color.Transparent,
                                        contentColor = Color.White,
                                        border = BorderStroke(1.5.dp, accent),
                                    ) {
                                        if (subtask.completed) Icon(Icons.Default.Check, null, modifier = Modifier.padding(4.dp))
                                    }
                                    Text(
                                        text = subtask.title,
                                        color = if (subtask.completed) colors.tertiaryInk else colors.secondaryInk,
                                        fontSize = 11.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.padding(start = 8.dp),
                                    )
                                }
                            }
                        }
                    }
                    Row(
                        modifier = Modifier.padding(top = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        SmallActionButton(
                            text = if (task.isPinned) "取消置顶" else "置顶",
                            onClick = onTogglePin,
                            modifier = Modifier.weight(1f),
                        )
                        SmallActionButton(
                            text = "删除",
                            onClick = onDelete,
                            modifier = Modifier.weight(1f),
                            danger = true,
                        )
                    }
                }
                Spacer(Modifier.width(3.dp))
                Surface(
                    modifier = Modifier.size(44.dp),
                    shape = RoundedCornerShape(14.dp),
                    color = Color.Transparent,
                    onClick = onToggleCompletion,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Surface(
                            modifier = Modifier.size(24.dp),
                            shape = CircleShape,
                            color = if (task.isCompleted) accent else Color.Transparent,
                            contentColor = Color.White,
                            border = BorderStroke(2.dp, accent),
                        ) {
                            if (task.isCompleted) Icon(Icons.Default.Check, null, modifier = Modifier.padding(4.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SmallActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    danger: Boolean = false,
) {
    val colors = LuluCalendarTheme.colors
    Surface(
        modifier = modifier.height(40.dp),
        shape = RoundedCornerShape(12.dp),
        color = if (danger) colors.dangerSoft else colors.mutedSurface,
        contentColor = if (danger) colors.danger else colors.secondaryInk,
        onClick = onClick,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(text, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun MetaPill(text: String, accent: Color) {
    val colors = LuluCalendarTheme.colors
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (accent == colors.secondaryInk) colors.mutedSurface else accent.copy(alpha = .10f),
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
            color = accent,
            fontSize = 9.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
        )
    }
}

internal fun quadrantLabel(quadrant: Int): String = when (quadrant) {
    1 -> "重要紧急"
    2 -> "重要不急"
    3 -> "紧急不重"
    else -> "轻松安排"
}

internal fun quadrantColor(quadrant: Int): Color = when (quadrant) {
    1 -> Color(0xFFD95F61)
    2 -> Color(0xFFD49A3B)
    3 -> Color(0xFF6287C5)
    else -> Color(0xFFCF3E70)
}

private fun taskAccent(task: Task, fallback: Color): Color =
    task.colorHex.toComposeColor(
        Quadrants.getOrNull(task.quadrant - 1)?.color?.toComposeColor(fallback) ?: fallback,
    )
