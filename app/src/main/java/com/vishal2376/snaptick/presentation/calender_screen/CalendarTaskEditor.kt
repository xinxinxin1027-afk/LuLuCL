package com.vishal2376.snaptick.presentation.calender_screen

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vishal2376.snaptick.domain.model.TaskSubtask
import com.vishal2376.snaptick.presentation.common.h1TextStyle
import com.vishal2376.snaptick.presentation.common.infoDescTextStyle
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CalendarTaskEditor(
    state: CalendarEditorState,
    onStateChange: (CalendarEditorState) -> Unit,
    onDismiss: () -> Unit,
    onSave: (CalendarEditorState) -> Unit,
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var subtaskDraft by remember(state.id, state.uuid) { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (state.isEdit) "编辑安排" else "新的安排",
                        style = h1TextStyle,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = state.startDate.format(
                            DateTimeFormatter.ofPattern("M月d日 EEEE", Locale.getDefault())
                        ),
                        style = infoDescTextStyle,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
                Button(
                    onClick = {
                        if (state.title.isBlank()) {
                            Toast.makeText(context, "先写个标题", Toast.LENGTH_SHORT).show()
                        } else {
                            onSave(state)
                        }
                    },
                ) {
                    Text(if (state.isEdit) "保存" else "创建")
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "关闭")
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                EditorSection(title = "标题") {
                    OutlinedTextField(
                        value = state.title,
                        onValueChange = { onStateChange(state.copy(title = it)) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("写下要做的事") },
                        singleLine = true,
                    )
                }

                EditorSection(title = "描述") {
                    OutlinedTextField(
                        value = state.description,
                        onValueChange = { onStateChange(state.copy(description = it)) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("补充一点细节") },
                        minLines = 3,
                    )
                }

                DateTimeEditor(
                    state = state,
                    onStateChange = onStateChange,
                )

                EditorSection(title = "颜色") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        SudaColors.forEach { hex ->
                            val color = hex.toComposeColor(MaterialTheme.colorScheme.primary)
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .background(color, RoundedCornerShape(14.dp))
                                    .then(
                                        if (state.colorHex.equals(hex, ignoreCase = true)) {
                                            Modifier.border(3.dp, MaterialTheme.colorScheme.onBackground, RoundedCornerShape(14.dp))
                                        } else Modifier
                                    )
                                    .clickable { onStateChange(state.copy(colorHex = hex)) },
                            )
                        }
                    }
                }

                EditorSection(title = "四象限") {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Quadrants.chunked(2).forEach { rowItems ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                rowItems.forEach { option ->
                                    QuadrantCard(
                                        modifier = Modifier.weight(1f),
                                        option = option,
                                        selected = state.quadrant == option.id,
                                        onClick = {
                                            onStateChange(
                                                state.copy(
                                                    quadrant = option.id,
                                                    colorHex = if (state.isEdit) state.colorHex else option.color,
                                                )
                                            )
                                        },
                                    )
                                }
                            }
                        }
                    }
                }

                EditorSection(title = "置顶安排") {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onStateChange(state.copy(isPinned = !state.isPinned)) },
                        shape = RoundedCornerShape(18.dp),
                        color = if (state.isPinned) {
                            "#F59E0B".toComposeColor(MaterialTheme.colorScheme.primary).copy(alpha = 0.12f)
                        } else {
                            MaterialTheme.colorScheme.primaryContainer
                        },
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(Icons.Default.PushPin, contentDescription = null)
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("置顶安排", fontWeight = FontWeight.Bold)
                                Text(
                                    "显示时优先排在当天列表前面",
                                    style = infoDescTextStyle,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                )
                            }
                            Text(if (state.isPinned) "已开启" else "未开启")
                        }
                    }
                }

                EditorSection(
                    title = "提醒",
                    action = {
                        TextButton(
                            onClick = {
                                val default = LocalDateTime.now().plusMinutes(15)
                                onStateChange(state.copy(reminders = state.reminders + default))
                            },
                        ) { Text("添加") }
                    },
                ) {
                    if (state.reminders.isEmpty()) {
                        Text(
                            "还没有提醒",
                            style = infoDescTextStyle,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            state.reminders.forEachIndexed { index, reminder ->
                                ReminderRow(
                                    reminder = reminder,
                                    onChange = { next ->
                                        val list = state.reminders.toMutableList()
                                        list[index] = next
                                        onStateChange(state.copy(reminders = list))
                                    },
                                    onRemove = {
                                        val list = state.reminders.toMutableList()
                                        list.removeAt(index)
                                        onStateChange(state.copy(reminders = list))
                                    },
                                )
                            }
                        }
                    }
                }

                EditorSection(
                    title = "子任务",
                    trailingText = "${state.subtasks.size} 项",
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = subtaskDraft,
                            onValueChange = { subtaskDraft = it },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("添加子任务") },
                            singleLine = true,
                        )
                        Spacer(Modifier.width(8.dp))
                        FilledTonalButton(
                            onClick = {
                                val title = subtaskDraft.trim()
                                if (title.isNotEmpty()) {
                                    onStateChange(
                                        state.copy(subtasks = state.subtasks + TaskSubtask(title = title))
                                    )
                                    subtaskDraft = ""
                                }
                            },
                        ) { Text("添加") }
                    }

                    Column(
                        modifier = Modifier.padding(top = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        state.subtasks.forEach { item ->
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.primaryContainer,
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    IconButton(
                                        onClick = {
                                            onStateChange(
                                                state.copy(
                                                    subtasks = state.subtasks.map {
                                                        if (it.id == item.id) it.copy(completed = !it.completed) else it
                                                    }
                                                )
                                            )
                                        },
                                        modifier = Modifier.size(34.dp),
                                    ) {
                                        Icon(
                                            if (item.completed) Icons.Default.CheckCircle else Icons.Default.Check,
                                            contentDescription = null,
                                        )
                                    }
                                    Text(item.title, modifier = Modifier.weight(1f))
                                    TextButton(
                                        onClick = {
                                            onStateChange(
                                                state.copy(subtasks = state.subtasks.filterNot { it.id == item.id })
                                            )
                                        },
                                    ) { Text("移除") }
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

