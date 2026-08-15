package com.vishal2376.snaptick.presentation.calender_screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material.icons.filled.ViewWeek
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.WeekCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.compose.weekcalendar.rememberWeekCalendarState
import com.kizitonwose.calendar.core.firstDayOfWeekFromLocale
import com.vishal2376.snaptick.domain.model.Task
import com.vishal2376.snaptick.presentation.calender_screen.component.DaysOfWeekTitle
import com.vishal2376.snaptick.presentation.calender_screen.component.MonthDayComponent
import com.vishal2376.snaptick.presentation.calender_screen.component.WeekDayComponent
import com.vishal2376.snaptick.presentation.common.CalenderView
import com.vishal2376.snaptick.presentation.main.action.MainAction
import com.vishal2376.snaptick.presentation.main.state.MainState
import com.vishal2376.snaptick.presentation.task_list.action.TaskListAction
import com.vishal2376.snaptick.ui.theme.SnaptickTheme
import com.vishal2376.snaptick.util.DummyTasks
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CalenderScreen(
    tasks: List<Task>,
    appState: MainState,
    onTaskAction: (TaskListAction) -> Unit,
    onAction: (MainAction) -> Unit,
    onNavigate: (route: String) -> Unit,
    onBack: () -> Unit,
) {
    LuluCalendarDesignTheme {
        CalenderScreenContent(
            tasks = tasks,
            appState = appState,
            onTaskAction = onTaskAction,
            onAction = onAction,
            onNavigate = onNavigate,
            onBack = onBack,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun CalenderScreenContent(
    tasks: List<Task>,
    appState: MainState,
    onTaskAction: (TaskListAction) -> Unit,
    onAction: (MainAction) -> Unit,
    onNavigate: (route: String) -> Unit,
    onBack: () -> Unit,
) {
    val colors = LuluCalendarTheme.colors
    val scope = rememberCoroutineScope()
    var calenderView by remember(appState.calenderView) { mutableStateOf(appState.calenderView) }
    var agendaOnlyMode by remember { mutableStateOf(false) }
    var selectedDay by remember { mutableStateOf(LocalDate.now()) }
    var editorState by remember { mutableStateOf<CalendarEditorState?>(null) }
    var taskPendingDelete by remember { mutableStateOf<Task?>(null) }

    val firstDayOfWeek = remember { firstDayOfWeekFromLocale() }
    val currentMonth = remember { YearMonth.now() }
    val startMonth = remember { currentMonth.minusMonths(50) }
    val endMonth = remember { currentMonth.plusMonths(50) }
    val monthState = rememberCalendarState(
        startMonth = startMonth,
        endMonth = endMonth,
        firstVisibleMonth = currentMonth,
        firstDayOfWeek = firstDayOfWeek,
    )

    val currentDate = remember { LocalDate.now() }
    val startDate = remember { currentDate.minusDays(100) }
    val endDate = remember { currentDate.plusDays(100) }
    val weekState = rememberWeekCalendarState(
        startDate = startDate,
        endDate = endDate,
        firstDayOfWeek = firstDayOfWeek,
    )

    val currentMonthTitle = if (calenderView == CalenderView.WEEKLY) {
        weekState.firstVisibleWeek.days[0].date.month
    } else {
        monthState.lastVisibleMonth.yearMonth.month
    }

    val selectedDayTasks = remember(tasks, selectedDay) {
        tasks
            .asSequence()
            .filter { it.shouldOccurOn(selectedDay) }
            .map { task -> task.copy(isCompleted = task.isCompletedOn(selectedDay)) }
            .sortedWith(Comparator { a, b ->
                when {
                    a.isCompleted != b.isCompleted -> if (a.isCompleted) 1 else -1
                    a.isPinned != b.isPinned -> if (a.isPinned) -1 else 1
                    a.startTime != b.startTime -> a.startTime.compareTo(b.startTime)
                    else -> a.id.compareTo(b.id)
                }
            })
            .toList()
    }

    fun openCreate() {
        editorState = CalendarEditorState(
            startDate = selectedDay,
            colorHex = SudaColors.first(),
            quadrant = 4,
        )
    }

    fun openEdit(task: Task) {
        val anchor = LocalDateTime.of(task.date, task.startTime)
        val offsets = task.reminderOffsets().ifEmpty { if (task.reminder) listOf(0) else emptyList() }
        editorState = CalendarEditorState(
            id = task.id,
            uuid = task.uuid,
            title = task.title,
            description = task.description,
            startDate = task.date,
            startTime = task.startTime,
            allDay = task.isAllDayTaskEnabled(),
            endDate = task.safeEndDate().takeIf { it != task.date || !task.isAllDayTaskEnabled() },
            endTime = task.endTime.takeUnless { task.isAllDayTaskEnabled() },
            colorHex = task.colorHex.ifBlank { Quadrants.getOrNull(task.quadrant - 1)?.color ?: SudaColors.first() },
            quadrant = task.quadrant.coerceIn(1, 4),
            isPinned = task.isPinned,
            reminders = offsets.map { anchor.minusMinutes(it.toLong()) },
            subtasks = task.subtasks(),
            isCompleted = task.isCompleted,
            isRepeated = task.isRepeated,
            repeatWeekdays = task.repeatWeekdays,
            pomodoroTimer = task.pomodoroTimer,
            calendarEventId = task.calendarEventId,
            completedDatesCsv = task.completedDatesCsv,
        )
    }

    taskPendingDelete?.let { task ->
        AlertDialog(
            onDismissRequest = { taskPendingDelete = null },
            title = { Text("删除这项日程？", fontWeight = FontWeight.Bold) },
            text = { Text("“${task.title}”将从日历中移除，此操作无法撤销。", color = colors.secondaryInk) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onTaskAction(TaskListAction.DeleteTask(task.id))
                        taskPendingDelete = null
                    },
                ) {
                    Text("删除", color = colors.danger, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { taskPendingDelete = null }) {
                    Text("取消", color = colors.secondaryInk)
                }
            },
            containerColor = colors.surface,
            titleContentColor = colors.ink,
        )
    }

    editorState?.let { state ->
        CalendarTaskEditor(
            state = state,
            onStateChange = { editorState = it },
            onDismiss = { editorState = null },
            onSave = { edited ->
                val task = edited.toTask()
                onTaskAction(
                    TaskListAction.UpsertTask(
                        task = task,
                        reminderOffsets = edited.reminderOffsets(),
                    ),
                )
                selectedDay = edited.startDate
                editorState = null
            },
        )
    }

    val calendarTopBar: @Composable () -> Unit = {
        TopAppBar(
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = colors.canvas,
                titleContentColor = colors.ink,
                navigationIconContentColor = colors.secondaryInk,
                actionIconContentColor = colors.secondaryInk,
            ),
            title = {
                androidx.compose.foundation.layout.Column {
                    Text(
                        text = "日历",
                        color = colors.tertiaryInk,
                        fontSize = 10.sp,
                    )
                    Text(
                        text = currentMonthTitle.getDisplayName(TextStyle.FULL, Locale.CHINA),
                        color = colors.ink,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            },
            navigationIcon = {
                LuluIconButton(
                    icon = Icons.Rounded.ArrowBack,
                    contentDescription = "返回",
                    onClick = onBack,
                )
            },
            actions = {
                LuluIconButton(
                    icon = Icons.Default.Restore,
                    contentDescription = "回到今天",
                    onClick = {
                        scope.launch {
                            selectedDay = currentDate
                            if (calenderView == CalenderView.WEEKLY) {
                                weekState.animateScrollToWeek(currentDate)
                            } else {
                                monthState.animateScrollToMonth(currentMonth)
                            }
                        }
                    },
                    filled = true,
                )

                if (!agendaOnlyMode) {
                    LuluIconButton(
                        icon = if (calenderView == CalenderView.WEEKLY) Icons.Default.CalendarMonth else Icons.Default.ViewWeek,
                        contentDescription = "切换月/周视图",
                        onClick = {
                            calenderView = if (calenderView == CalenderView.WEEKLY) {
                                CalenderView.MONTHLY
                            } else {
                                CalenderView.WEEKLY
                            }
                            onAction(MainAction.UpdateCalenderView(calenderView))
                        },
                        filled = true,
                    )
                }

                LuluIconButton(
                    icon = if (agendaOnlyMode) Icons.Default.CalendarMonth else Icons.Default.ViewAgenda,
                    contentDescription = if (agendaOnlyMode) "返回日历" else "进入纯日程",
                    onClick = { agendaOnlyMode = !agendaOnlyMode },
                    brand = true,
                )
            },
        )
    }

    if (agendaOnlyMode) {
        Scaffold(
            topBar = calendarTopBar,
            containerColor = colors.canvas,
        ) { innerPadding ->
            AgendaPanel(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                selectedDay = selectedDay,
                tasks = selectedDayTasks,
                onCreate = ::openCreate,
                onEdit = ::openEdit,
                onToggleCompletion = { task ->
                    onTaskAction(
                        TaskListAction.ToggleCompletionForDate(
                            taskId = task.id,
                            date = selectedDay,
                            isCompleted = !task.isCompleted,
                        ),
                    )
                },
                onTogglePin = { task -> onTaskAction(TaskListAction.TogglePin(task.id)) },
                onDelete = { task -> taskPendingDelete = task },
                onToggleSubtask = { task, subtaskId ->
                    val next = task.subtasks().map {
                        if (it.id == subtaskId) it.copy(completed = !it.completed) else it
                    }
                    onTaskAction(TaskListAction.UpsertTask(task.withSubtasks(next)))
                },
            )
        }
        return
    }

    val bottomSheetScaffoldState = rememberBottomSheetScaffoldState()

    BottomSheetScaffold(
        scaffoldState = bottomSheetScaffoldState,
        sheetPeekHeight = 164.dp,
        sheetContainerColor = colors.canvas,
        sheetContent = {
            AgendaPanel(
                modifier = Modifier.fillMaxHeight(0.80f),
                selectedDay = selectedDay,
                tasks = selectedDayTasks,
                onCreate = ::openCreate,
                onEdit = ::openEdit,
                onToggleCompletion = { task ->
                    onTaskAction(
                        TaskListAction.ToggleCompletionForDate(
                            taskId = task.id,
                            date = selectedDay,
                            isCompleted = !task.isCompleted,
                        ),
                    )
                },
                onTogglePin = { task -> onTaskAction(TaskListAction.TogglePin(task.id)) },
                onDelete = { task -> taskPendingDelete = task },
                onToggleSubtask = { task, subtaskId ->
                    val next = task.subtasks().map {
                        if (it.id == subtaskId) it.copy(completed = !it.completed) else it
                    }
                    onTaskAction(TaskListAction.UpsertTask(task.withSubtasks(next)))
                },
                onHeaderClick = {
                    scope.launch {
                        if (bottomSheetScaffoldState.bottomSheetState.currentValue == SheetValue.Expanded) {
                            bottomSheetScaffoldState.bottomSheetState.partialExpand()
                        } else {
                            bottomSheetScaffoldState.bottomSheetState.expand()
                        }
                    }
                },
            )
        },
        topBar = calendarTopBar,
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            AnimatedVisibility(visible = calenderView == CalenderView.WEEKLY) {
                WeekCalendar(
                    modifier = Modifier.padding(horizontal = 12.dp),
                    state = weekState,
                    dayContent = { day ->
                        WeekDayComponent(
                            day,
                            selected = selectedDay == day.date,
                            indicator = tasks.any { it.shouldOccurOn(day.date) },
                        ) {
                            selectedDay = it
                            scope.launch { bottomSheetScaffoldState.bottomSheetState.partialExpand() }
                        }
                    },
                )
            }

            AnimatedVisibility(visible = calenderView == CalenderView.MONTHLY) {
                HorizontalCalendar(
                    modifier = Modifier.padding(horizontal = 12.dp),
                    state = monthState,
                    dayContent = { day ->
                        MonthDayComponent(
                            day,
                            selected = selectedDay == day.date,
                            indicator = tasks.any { it.shouldOccurOn(day.date) },
                        ) {
                            selectedDay = it
                            scope.launch { bottomSheetScaffoldState.bottomSheetState.partialExpand() }
                        }
                    },
                    monthHeader = { month ->
                        val daysOfWeek = month.weekDays.first().map { it.date.dayOfWeek }
                        DaysOfWeekTitle(daysOfWeek = daysOfWeek)
                    },
                )
            }
        }
    }
}

@Preview
@Composable
fun CalenderScreenPreview() {
    SnaptickTheme {
        CalenderScreen(
            tasks = DummyTasks.dummyTasks(),
            appState = MainState(),
            onTaskAction = {},
            onAction = {},
            onNavigate = {},
            onBack = {},
        )
    }
}
