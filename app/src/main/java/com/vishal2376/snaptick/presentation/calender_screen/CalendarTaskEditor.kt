package com.vishal2376.snaptick.presentation.calender_screen

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vishal2376.snaptick.domain.model.TaskSubtask
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CalendarTaskEditor(
    state: CalendarEditorState,
    onStateChange: (CalendarEditorState) -> Unit,
    onDismiss: () -> Unit,
    onSave: (CalendarEditorState) -> Unit,
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val colors = LuluCalendarTheme.colors
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var subtaskDraft by remember(state.id, state.uuid) { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.canvas,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 9.dp, bottom = 3.dp)
                    .width(38.dp)
                    .height(4.dp)
                    .background(colors.outline, RoundedCornerShape(50)),
            )
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(.94f),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .padding(start = 16.dp, end = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (state.isEdit) "编辑日程" else "新建日程",
                        color = colors.ink,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "${state.startDate.monthValue}月${state.startDate.dayOfMonth}日 ${state.startDate.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.CHINA)}",
                        color = colors.tertiaryInk,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }
                Surface(
                    modifier = Modifier
                        .width(58.dp)
                        .height(44.dp),
                    shape = RoundedCornerShape(13.dp),
                    color = colors.brand,
                    contentColor = Color.White,
                    onClick = {
                        if (state.title.isBlank()) {
                            Toast.makeText(context, "请输入日程标题", Toast.LENGTH_SHORT).show()
                        } else {
                            onSave(state)
                        }
                    },
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(if (state.isEdit) "保存" else "创建", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
                LuluIconButton(Icons.Default.Close, "关闭", onDismiss)
            }
            Divider(color = colors.divider)

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(13.dp),
            ) {
                EditorSurface {
                    OutlinedTextField(
                        value = state.title,
                        onValueChange = { onStateChange(state.copy(title = it.take(120))) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("标题") },
                        placeholder = { Text("写下要完成的事") },
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = state.description,
                        onValueChange = { onStateChange(state.copy(description = it.take(1000))) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("描述") },
                        placeholder = { Text("补充一点细节（可选）") },
                        minLines = 2,
                        maxLines = 4,
                    )
                }

                LuluSectionLabel("安排")
                EditorSurface { DateTimeEditor(state = state, onStateChange = onStateChange) }

                LuluSectionLabel("颜色与象限")
                EditorSurface {
                    Text("颜色", color = colors.ink, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(top = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        SudaColors.forEach { hex ->
                            val color = hex.toComposeColor(colors.brand)
                            Surface(
                                modifier = Modifier.size(44.dp),
                                shape = CircleShape,
                                color = Color.Transparent,
                                onClick = { onStateChange(state.copy(colorHex = hex)) },
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Surface(
                                        modifier = Modifier.size(32.dp),
                                        shape = CircleShape,
                                        color = color,
                                        border = if (state.colorHex.equals(hex, true)) BorderStroke(3.dp, colors.surface) else null,
                                    ) {
                                        if (state.colorHex.equals(hex, true)) {
                                            Icon(Icons.Default.Check, "已选择", tint = Color.White, modifier = Modifier.padding(8.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Divider(color = colors.divider, modifier = Modifier.padding(vertical = 12.dp))
                    Text("四象限", color = colors.ink, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Column(
                        modifier = Modifier.padding(top = 9.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Quadrants.chunked(2).forEach { items ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                items.forEach { option ->
                                    val accent = option.color.toComposeColor(colors.brand)
                                    Surface(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(66.dp),
                                        shape = RoundedCornerShape(15.dp),
                                        color = if (state.quadrant == option.id) accent.copy(alpha = .10f) else colors.surface,
                                        border = BorderStroke(1.dp, if (state.quadrant == option.id) accent else colors.outline),
                                        onClick = { onStateChange(state.copy(quadrant = option.id)) },
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(Modifier.size(8.dp).background(accent, CircleShape))
                                                Text(
                                                    option.title,
                                                    color = colors.ink,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(start = 7.dp),
                                                )
                                            }
                                            Text(
                                                option.description,
                                                color = colors.tertiaryInk,
                                                fontSize = 9.sp,
                                                modifier = Modifier.padding(top = 6.dp),
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                LuluSectionLabel("提醒与优先级")
                EditorSurface {
                    EditorToggleRow(
                        title = "置顶",
                        description = "优先显示在当天日程顶部",
                        checked = state.isPinned,
                        onToggle = { onStateChange(state.copy(isPinned = !state.isPinned)) },
                    )
                    Divider(color = colors.divider)
                    Text(
                        text = "提醒",
                        color = colors.ink,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 12.dp),
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(top = 9.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        listOf(
                            "准时" to 0L,
                            "10 分钟前" to 10L,
                            "30 分钟前" to 30L,
                            "1 小时前" to 60L,
                            "1 天前" to 1440L,
                        ).forEach { (label, offset) ->
                            val anchor = LocalDateTime.of(state.startDate, if (state.allDay) LocalTime.MIDNIGHT else state.startTime)
                            val reminder = anchor.minusMinutes(offset)
                            LuluChip(
                                text = label,
                                selected = reminder in state.reminders,
                                onClick = {
                                    val next = if (reminder in state.reminders) state.reminders - reminder else state.reminders + reminder
                                    onStateChange(state.copy(reminders = next.distinct().sorted()))
                                },
                            )
                        }
                    }
                }

                LuluSectionLabel("子任务")
                EditorSurface {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = subtaskDraft,
                            onValueChange = { subtaskDraft = it },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("添加子任务") },
                            singleLine = true,
                        )
                        Spacer(Modifier.width(8.dp))
                        LuluIconButton(
                            icon = Icons.Default.Add,
                            contentDescription = "添加子任务",
                            onClick = {
                                val title = subtaskDraft.trim()
                                if (title.isNotEmpty()) {
                                    onStateChange(state.copy(subtasks = state.subtasks + TaskSubtask(title = title)))
                                    subtaskDraft = ""
                                }
                            },
                            brand = true,
                        )
                    }
                    if (state.subtasks.isNotEmpty()) {
                        Column(modifier = Modifier.padding(top = 8.dp)) {
                            state.subtasks.forEachIndexed { index, item ->
                                if (index > 0) Divider(color = colors.divider)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Surface(
                                        modifier = Modifier.size(30.dp),
                                        shape = RoundedCornerShape(9.dp),
                                        color = if (item.completed) colors.brand else Color.Transparent,
                                        contentColor = Color.White,
                                        border = BorderStroke(1.5.dp, colors.brand),
                                        onClick = {
                                            onStateChange(
                                                state.copy(
                                                    subtasks = state.subtasks.map {
                                                        if (it.id == item.id) it.copy(completed = !it.completed) else it
                                                    },
                                                ),
                                            )
                                        },
                                    ) {
                                        if (item.completed) Icon(Icons.Default.Check, null, modifier = Modifier.padding(6.dp))
                                    }
                                    Text(
                                        item.title,
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(horizontal = 10.dp),
                                        color = colors.ink,
                                        fontSize = 12.sp,
                                    )
                                    LuluIconButton(
                                        icon = Icons.Default.Delete,
                                        contentDescription = "删除子任务",
                                        onClick = { onStateChange(state.copy(subtasks = state.subtasks.filterNot { it.id == item.id })) },
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun EditorSurface(content: @Composable () -> Unit) {
    val colors = LuluCalendarTheme.colors
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = colors.surface,
        border = BorderStroke(1.dp, colors.outline),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            content = { content() },
        )
    }
}

@Composable
private fun EditorToggleRow(
    title: String,
    description: String,
    checked: Boolean,
    onToggle: () -> Unit,
) {
    val colors = LuluCalendarTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .clickable(onClick = onToggle),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Default.PushPin, null, tint = colors.brand, modifier = Modifier.size(19.dp))
        Column(modifier = Modifier.weight(1f).padding(horizontal = 10.dp)) {
            Text(title, color = colors.ink, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Text(description, color = colors.tertiaryInk, fontSize = 10.sp, modifier = Modifier.padding(top = 2.dp))
        }
        LuluSwitch(checked = checked, onCheckedChange = { onToggle() })
    }
}
