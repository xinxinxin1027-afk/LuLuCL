package com.vishal2376.snaptick.presentation.calender_screen

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun CalendarAgendaHeader(
    date: LocalDate,
    tasks: List<CalendarTaskItem>,
    filterActive: Boolean,
    onFilter: () -> Unit,
    onCreate: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val incomplete = tasks.count { !it.completedOn(date) }
    val completed = tasks.size - incomplete
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(60.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "${date.monthValue}月${date.dayOfMonth}日 ${date.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.CHINA)}",
                color = LuluCalendarTheme.colors.ink,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = when {
                    tasks.isEmpty() -> "暂无安排"
                    else -> "$incomplete 项待完成 · $completed 项已完成"
                },
                color = LuluCalendarTheme.colors.tertiaryInk,
                fontSize = 11.sp,
                modifier = Modifier.padding(top = 3.dp),
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            LuluIconButton(
                icon = Icons.Default.FilterList,
                contentDescription = "筛选日程",
                onClick = onFilter,
                filled = true,
                modifier = if (filterActive) Modifier.background(
                    LuluCalendarTheme.colors.brandSoft,
                    RoundedCornerShape(LuluCalendarDimens.compactRadius),
                ) else Modifier,
            )
            LuluIconButton(
                icon = Icons.Default.Add,
                contentDescription = "新建日程",
                onClick = onCreate,
                brand = true,
            )
        }
    }
}

@Composable
fun AgendaPanel(
    selectedDate: LocalDate,
    tasks: List<CalendarTaskItem>,
    filter: CalendarAgendaFilter,
    onFilterChange: (CalendarAgendaFilter) -> Unit,
    onFilterClick: () -> Unit,
    onCreate: () -> Unit,
    onOpen: (CalendarTaskItem) -> Unit,
    onToggleCompletion: (CalendarTaskItem, Boolean) -> Unit,
    onTogglePin: (CalendarTaskItem) -> Unit,
    modifier: Modifier = Modifier,
    showQuickFilters: Boolean = false,
) {
    val visible = remember(tasks, selectedDate, filter) {
        tasks.applyAgendaFilter(selectedDate, filter)
    }
    Column(modifier = modifier.fillMaxSize()) {
        CalendarAgendaHeader(
            date = selectedDate,
            tasks = tasks.forCalendarDate(selectedDate),
            filterActive = filter.isActive,
            onFilter = onFilterClick,
            onCreate = onCreate,
        )
        AnimatedVisibility(visible = showQuickFilters) {
            AgendaQuickFilters(
                selected = filter.status,
                onSelected = { onFilterChange(filter.copy(status = it)) },
                modifier = Modifier.padding(bottom = 12.dp),
            )
        }
        if (visible.isEmpty()) {
            CalendarEmptyState(
                filtered = filter.isActive,
                onCreate = onCreate,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            var completedExpanded by remember { mutableStateOf(true) }
            val pending = visible.filterNot { it.completedOn(selectedDate) }
            val completed = visible.filter { it.completedOn(selectedDate) }
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(9.dp),
            ) {
                items(pending, key = { "pending-${it.uuid}" }) { task ->
                    CalendarTaskCard(
                        task = task,
                        date = selectedDate,
                        onClick = { onOpen(task) },
                        onToggleCompletion = { onToggleCompletion(task, it) },
                        onTogglePin = { onTogglePin(task) },
                    )
                }
                if (completed.isNotEmpty()) {
                    item(key = "completed-header") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { completedExpanded = !completedExpanded }
                                .padding(top = 7.dp, bottom = 1.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                text = "已完成 · ${completed.size}",
                                color = LuluCalendarTheme.colors.tertiaryInk,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(
                                text = if (completedExpanded) "收起" else "展开",
                                color = LuluCalendarTheme.colors.brandStrong,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                    if (completedExpanded) {
                        items(completed, key = { "completed-${it.uuid}" }) { task ->
                            CalendarTaskCard(
                                task = task,
                                date = selectedDate,
                                onClick = { onOpen(task) },
                                onToggleCompletion = { onToggleCompletion(task, it) },
                                onTogglePin = { onTogglePin(task) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AgendaQuickFilters(
    selected: AgendaStatusFilter,
    onSelected: (AgendaStatusFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    val labels = listOf(
        AgendaStatusFilter.ALL to "全部",
        AgendaStatusFilter.INCOMPLETE to "未完成",
        AgendaStatusFilter.PINNED to "置顶",
        AgendaStatusFilter.COMPLETED to "已完成",
    )
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        labels.forEach { (status, label) ->
            LuluChip(
                text = label,
                selected = selected == status,
                onClick = { onSelected(status) },
            )
        }
    }
}

@Composable
fun CalendarTaskCard(
    task: CalendarTaskItem,
    date: LocalDate,
    onClick: () -> Unit,
    onToggleCompletion: (Boolean) -> Unit,
    onTogglePin: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LuluCalendarTheme.colors
    val accent = parseCalendarColor(task.colorHex)
    val completed = task.completedOn(date)
    val progress = if (task.subtasks.isEmpty()) null else task.completedSubtasks.toFloat() / task.subtasks.size
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (completed) .68f else 1f),
        shape = RoundedCornerShape(LuluCalendarDimens.cardRadius),
        color = colors.surface,
        contentColor = colors.ink,
        border = BorderStroke(1.dp, colors.outline),
        onClick = onClick,
    ) {
        Row(modifier = Modifier.heightIn(min = if (task.description.isBlank()) 76.dp else 96.dp)) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(accent),
            )
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 11.dp, end = 8.dp, top = 11.dp, bottom = 10.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Column(
                    modifier = Modifier.width(48.dp),
                    horizontalAlignment = Alignment.End,
                ) {
                    Text(
                        text = if (task.allDay) "全天" else task.start.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")),
                        color = colors.secondaryInk,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    if (!task.allDay) {
                        Text(
                            text = task.end.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")),
                            color = colors.tertiaryInk,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(top = 2.dp),
                        )
                    }
                }
                Spacer(modifier = Modifier.width(11.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = task.title,
                            modifier = Modifier.weight(1f, fill = false),
                            color = if (completed) colors.secondaryInk else colors.ink,
                            fontSize = 14.sp,
                            lineHeight = 18.sp,
                            fontWeight = FontWeight.Bold,
                            textDecoration = if (completed) TextDecoration.LineThrough else TextDecoration.None,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
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
                        modifier = Modifier.padding(top = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                    ) {
                        CalendarMetaChip(
                            text = quadrantLabel(task.quadrant),
                            color = quadrantColor(task.quadrant),
                        )
                        if (task.subtasks.isNotEmpty()) {
                            CalendarMetaChip(
                                text = "${task.subtasks.size} 个子任务",
                                color = colors.secondaryInk,
                            )
                        }
                        if (task.reminders.isNotEmpty()) {
                            CalendarMetaChip(
                                text = "${task.reminders.size} 个提醒",
                                color = colors.secondaryInk,
                            )
                        }
                    }
                    if (progress != null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            LinearProgressIndicator(
                                progress = progress,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(4.dp),
                                color = accent,
                                trackColor = colors.mutedStrong,
                            )
                            Text(
                                text = "${(progress * 100).toInt()}%",
                                color = colors.tertiaryInk,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(start = 7.dp),
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.width(4.dp))
                Surface(
                    modifier = Modifier.size(44.dp),
                    shape = RoundedCornerShape(14.dp),
                    color = Color.Transparent,
                    onClick = { onToggleCompletion(!completed) },
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Surface(
                            modifier = Modifier.size(24.dp),
                            shape = CircleShape,
                            color = if (completed) accent else Color.Transparent,
                            border = BorderStroke(2.dp, accent),
                        ) {
                            if (completed) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.padding(4.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarMetaChip(
    text: String,
    color: Color,
) {
    val palette = LuluCalendarTheme.colors
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (color == palette.secondaryInk) palette.mutedSurface else color.copy(alpha = .11f),
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
        )
    }
}

@Composable
fun CalendarEmptyState(
    filtered: Boolean,
    onCreate: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LuluCalendarTheme.colors
    Column(
        modifier = modifier.padding(horizontal = 18.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Surface(
            modifier = Modifier.size(68.dp),
            shape = RoundedCornerShape(24.dp),
            color = colors.brandSoft,
            contentColor = colors.brand,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = if (filtered) Icons.Default.FilterList else Icons.Default.Event,
                    contentDescription = null,
                    modifier = Modifier.size(34.dp),
                )
            }
        }
        Text(
            text = if (filtered) "没有符合条件的安排" else "今天还没有安排",
            color = colors.ink,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 13.dp),
        )
        Text(
            text = if (filtered) "调整筛选条件后再试试" else "轻点添加按钮，为今天留下一件重要的事",
            color = colors.tertiaryInk,
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 5.dp),
        )
        if (!filtered) {
            LuluPrimaryButton(
                text = "新建日程",
                onClick = onCreate,
                icon = Icons.Default.Add,
                modifier = Modifier.padding(top = 16.dp),
            )
        }
    }
}

fun quadrantLabel(quadrant: Int): String = when (quadrant) {
    1 -> "重要紧急"
    2 -> "重要不急"
    3 -> "紧急不重"
    else -> "轻松安排"
}

fun quadrantColor(quadrant: Int): Color = when (quadrant) {
    1 -> Color(0xFFD95F61)
    2 -> Color(0xFFD49A3B)
    3 -> Color(0xFF6287C5)
    else -> Color(0xFFCF3E70)
}
