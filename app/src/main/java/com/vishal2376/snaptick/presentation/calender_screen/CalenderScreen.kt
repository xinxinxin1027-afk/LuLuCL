package com.vishal2376.snaptick.presentation.calender_screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material.icons.filled.ViewWeek
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
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
import com.vishal2376.snaptick.presentation.common.SnackbarController.showCustomSnackbar
import com.vishal2376.snaptick.presentation.common.animation.SnaptickMotion
import com.vishal2376.snaptick.presentation.common.filterTasksByDate
import com.vishal2376.snaptick.presentation.common.h1TextStyle
import com.vishal2376.snaptick.presentation.common.infoDescTextStyle
import com.vishal2376.snaptick.presentation.home_screen.components.EmptyTaskComponent
import com.vishal2376.snaptick.presentation.home_screen.components.TaskComponent
import com.vishal2376.snaptick.presentation.main.action.MainAction
import com.vishal2376.snaptick.presentation.main.state.MainState
import com.vishal2376.snaptick.presentation.navigation.Routes
import com.vishal2376.snaptick.presentation.task_list.action.TaskListAction
import com.vishal2376.snaptick.ui.theme.SnaptickTheme
import com.vishal2376.snaptick.util.DummyTasks
import com.vishal2376.snaptick.util.SoundEvent
import com.vishal2376.snaptick.util.playSound
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
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
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var calenderView by remember(appState.calenderView) { mutableStateOf(appState.calenderView) }
    var agendaOnlyMode by remember { mutableStateOf(false) }
    var showAgendaSheet by remember { mutableStateOf(true) }
    val agendaSheetState = rememberModalBottomSheetState()
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

    var selectedDay by remember { mutableStateOf(currentDate) }
    val currentMonthTitle = if (calenderView == CalenderView.WEEKLY) {
        weekState.firstVisibleWeek.days[0].date.month
    } else {
        monthState.lastVisibleMonth.yearMonth.month
    }

    val selectedDayTasks = remember(tasks, selectedDay) {
        filterTasksByDate(tasks, selectedDay)
    }

    if (!agendaOnlyMode && showAgendaSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAgendaSheet = false },
            sheetState = agendaSheetState,
            containerColor = MaterialTheme.colorScheme.background,
        ) {
            AgendaPanel(
                modifier = Modifier.fillMaxHeight(0.78f),
                selectedDay = selectedDay,
                tasks = selectedDayTasks,
                appState = appState,
                onTaskAction = onTaskAction,
                onNavigate = onNavigate,
                onAction = onAction,
                onDeleteMessage = {
                    showCustomSnackbar(
                        msg = "任务已删除",
                        actionText = "撤销",
                        onClickAction = { onTaskAction(TaskListAction.UndoDelete) },
                    )
                },
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
                title = {
                    Text(
                        text = currentMonthTitle.getDisplayName(
                            TextStyle.FULL,
                            Locale.getDefault(),
                        ),
                        style = h1TextStyle,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowBack,
                            contentDescription = null,
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            scope.launch {
                                selectedDay = currentDate
                                if (calenderView == CalenderView.WEEKLY) {
                                    weekState.animateScrollToWeek(currentDate)
                                } else {
                                    monthState.animateScrollToMonth(currentMonth)
                                }
                                if (!agendaOnlyMode) showAgendaSheet = true
                            }
                        },
                    ) {
                        Icon(imageVector = Icons.Default.Restore, contentDescription = null)
                    }

                    if (!agendaOnlyMode) {
                        IconButton(
                            onClick = {
                                calenderView = if (calenderView == CalenderView.WEEKLY) {
                                    CalenderView.MONTHLY
                                } else {
                                    CalenderView.WEEKLY
                                }
                                onAction(MainAction.UpdateCalenderView(calenderView))
                            },
                        ) {
                            val currentIcon = if (calenderView == CalenderView.WEEKLY) {
                                Icons.Default.CalendarMonth
                            } else {
                                Icons.Default.ViewWeek
                            }
                            Icon(imageVector = currentIcon, contentDescription = null)
                        }
                    }

                    // Calendar is the default mode. The far-right button switches
                    // into the pure agenda mode; tapping again returns to calendar.
                    IconButton(
                        onClick = {
                            agendaOnlyMode = !agendaOnlyMode
                            showAgendaSheet = !agendaOnlyMode
                        },
                    ) {
                        Icon(
                            imageVector = if (agendaOnlyMode) {
                                Icons.Default.CalendarMonth
                            } else {
                                Icons.Default.ViewAgenda
                            },
                            contentDescription = null,
                        )
                    }
                },
            )
        },
        floatingActionButton = {
            if (!agendaOnlyMode && selectedDay >= LocalDate.now()) {
                FloatingActionButton(
                    onClick = {
                        onAction(MainAction.UpdateCalenderDate(selectedDay))
                        onNavigate(Routes.AddTaskScreen.name)
                    },
                    modifier = Modifier.padding(end = 4.dp, bottom = 4.dp),
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.primaryContainer,
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null)
                }
            }
        },
    ) { innerPadding ->
        if (agendaOnlyMode) {
            AgendaPanel(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                selectedDay = selectedDay,
                tasks = selectedDayTasks,
                appState = appState,
                onTaskAction = onTaskAction,
                onNavigate = onNavigate,
                onAction = onAction,
                onDeleteMessage = {
                    showCustomSnackbar(
                        msg = "任务已删除",
                        actionText = "撤销",
                        onClickAction = { onTaskAction(TaskListAction.UndoDelete) },
                    )
                },
            )
        } else {
            Column(modifier = Modifier.padding(innerPadding)) {
                AnimatedVisibility(visible = calenderView == CalenderView.WEEKLY) {
                    WeekCalendar(
                        modifier = Modifier.padding(horizontal = 10.dp),
                        state = weekState,
                        dayContent = { day ->
                            WeekDayComponent(
                                day,
                                selected = selectedDay == day.date,
                                indicator = filterTasksByDate(tasks, day.date).isNotEmpty(),
                            ) {
                                selectedDay = it
                                showAgendaSheet = true
                            }
                        },
                    )
                }

                AnimatedVisibility(visible = calenderView == CalenderView.MONTHLY) {
                    HorizontalCalendar(
                        modifier = Modifier.padding(horizontal = 10.dp),
                        state = monthState,
                        dayContent = { day ->
                            MonthDayComponent(
                                day,
                                selected = selectedDay == day.date,
                                indicator = filterTasksByDate(tasks, day.date).isNotEmpty(),
                            ) {
                                selectedDay = it
                                showAgendaSheet = true
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
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AgendaPanel(
    modifier: Modifier,
    selectedDay: LocalDate,
    tasks: List<Task>,
    appState: MainState,
    onTaskAction: (TaskListAction) -> Unit,
    onNavigate: (String) -> Unit,
    onAction: (MainAction) -> Unit,
    onDeleteMessage: () -> Unit,
) {
    val context = LocalContext.current
    val pendingCount = tasks.count { !it.isCompleted }
    val completedCount = tasks.count { it.isCompleted }
    var firstPaintDone by remember(selectedDay) { mutableStateOf(false) }

    LaunchedEffect(selectedDay) {
        delay(700)
        firstPaintDone = true
    }

    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
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
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$pendingCount 个待完成 · $completedCount 个已完成",
                    style = infoDescTextStyle,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }

            if (selectedDay >= LocalDate.now()) {
                Button(
                    onClick = {
                        onAction(MainAction.UpdateCalenderDate(selectedDay))
                        onNavigate(Routes.AddTaskScreen.name)
                    },
                ) {
                    Text(text = "新建")
                }
            }
        }

        Divider(
            modifier = Modifier.padding(horizontal = 16.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
        )

        if (tasks.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                EmptyTaskComponent()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 12.dp),
            ) {
                itemsIndexed(
                    items = tasks,
                    key = { _, task -> task.id },
                ) { index, task ->
                    Box(
                        modifier = Modifier.animateItemPlacement(
                            spring(
                                dampingRatio = 0.6f,
                                stiffness = Spring.StiffnessMediumLow,
                                visibilityThreshold = IntOffset.VisibilityThreshold,
                            )
                        )
                    ) {
                        TaskComponent(
                            task = task,
                            is24HourTimeFormat = appState.is24hourTimeFormat,
                            onEdit = { taskId ->
                                onNavigate("${Routes.EditTaskScreen.name}/$taskId")
                            },
                            onComplete = { taskId ->
                                playSound(
                                    context,
                                    SoundEvent.TASK_COMPLETED,
                                    appState.soundEnabled,
                                )
                                onTaskAction(
                                    TaskListAction.ToggleCompletionForDate(
                                        taskId = taskId,
                                        date = selectedDay,
                                        isCompleted = !task.isCompleted,
                                    )
                                )
                            },
                            onPomodoro = { taskId ->
                                onNavigate("${Routes.PomodoroScreen.name}/$taskId")
                            },
                            onDelete = { taskId ->
                                onTaskAction(TaskListAction.DeleteTask(taskId))
                                onDeleteMessage()
                            },
                            animDelay = if (firstPaintDone) -1
                            else index.coerceAtMost(SnaptickMotion.MAX_STAGGERED_ITEMS) * 110,
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    }
}

@Preview
@Composable
fun CalenderScreenPreview() {
    SnaptickTheme {
        val tasks = DummyTasks.dummyTasks()
        CalenderScreen(tasks, MainState(), {}, {}, {}, {})
    }
}
