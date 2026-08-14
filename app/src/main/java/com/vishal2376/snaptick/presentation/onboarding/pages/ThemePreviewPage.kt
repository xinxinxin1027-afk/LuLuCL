package com.vishal2376.snaptick.presentation.onboarding.pages

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.vishal2376.snaptick.domain.model.Task
import com.vishal2376.snaptick.presentation.common.AppTheme
import com.vishal2376.snaptick.presentation.common.animation.SnaptickMotion
import com.vishal2376.snaptick.presentation.common.components.ThemeSelector
import com.vishal2376.snaptick.presentation.common.h1TextStyle
import com.vishal2376.snaptick.presentation.common.infoDescTextStyle
import com.vishal2376.snaptick.presentation.home_screen.components.TaskComponent
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ThemePreviewPage(
	selectedTheme: AppTheme,
	onThemeSelected: (AppTheme) -> Unit,
) {
	val initialDemos = remember { demoTasks() }
	var demoOrder by remember { mutableStateOf(initialDemos) }

	var primed by remember { mutableStateOf(false) }
	LaunchedEffect(selectedTheme) {
		if (!primed) {
			primed = true
			return@LaunchedEffect
		}
		demoOrder = demoOrder.shuffled()
	}

	Column(
		modifier = Modifier
			.fillMaxSize()
			.padding(horizontal = 24.dp, vertical = 16.dp),
		horizontalAlignment = Alignment.CenterHorizontally
	) {
		Text(
			text = "选择你的主题",
			style = h1TextStyle,
			color = MaterialTheme.colorScheme.onBackground,
			textAlign = TextAlign.Center
		)
		Spacer(Modifier.height(6.dp))
		Text(
			modifier = Modifier
				.fillMaxWidth()
				.padding(horizontal = 8.dp),
			text = "点击下面的主题，预览 LuluCalendar 的效果。",
			style = infoDescTextStyle,
			color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f),
			textAlign = TextAlign.Center
		)
		Spacer(Modifier.height(20.dp))

		Box(modifier = Modifier.weight(1f)) {
			LazyColumn(
				modifier = Modifier.fillMaxSize(),
				contentPadding = PaddingValues(vertical = 16.dp),
				verticalArrangement = Arrangement.spacedBy(8.dp)
			) {
				itemsIndexed(demoOrder, key = { _, task -> task.uuid }) { index, task ->
					StaggeredEntry(
						index = index,
						modifier = Modifier.animateItemPlacement(
							tween(durationMillis = 500, easing = FastOutSlowInEasing)
						)
					) {
						TaskComponent(
							task = task,
							onEdit = {},
							onComplete = {},
							onPomodoro = {},
							onDelete = {},
							is24HourTimeFormat = false,
							animDelay = 0
						)
					}
				}
			}
		}

		Spacer(Modifier.height(24.dp))

		ThemeSelector(
			selected = selectedTheme,
			onSelect = onThemeSelected,
		)
	}
}

@Composable
private fun StaggeredEntry(
	index: Int,
	modifier: Modifier = Modifier,
	content: @Composable () -> Unit,
) {
	val alpha = remember { Animatable(0f) }
	val translate = remember { Animatable(28f) }
	val density = LocalDensity.current

	LaunchedEffect(Unit) {
		val delayMs = index.coerceAtMost(SnaptickMotion.MAX_STAGGERED_ITEMS) * 110L
		if (delayMs > 0) delay(delayMs)
		coroutineScope {
			launch { alpha.animateTo(1f, tween(520, easing = FastOutSlowInEasing)) }
			launch { translate.animateTo(0f, tween(560, easing = FastOutSlowInEasing)) }
		}
	}

	Box(
		modifier = modifier.graphicsLayer {
			this.alpha = alpha.value
			translationY = with(density) { translate.value.dp.toPx() }
		}
	) {
		content()
	}
}

private fun demoTasks(): List<Task> = listOf(
	Task(
		id = 1, uuid = "demo-1", title = "晨间跑步",
		startTime = LocalTime.of(6, 30), endTime = LocalTime.of(7, 15),
		reminder = true, date = LocalDate.now(), priority = 2,
		isRepeated = true, repeatWeekdays = "0,2,4"
	),
	Task(
		id = 2, uuid = "demo-2", title = "设计复盘",
		startTime = LocalTime.of(10, 0), endTime = LocalTime.of(11, 0),
		reminder = true, date = LocalDate.now(), priority = 1
	),
	Task(
		id = 3, uuid = "demo-3", title = "团队晨会",
		startTime = LocalTime.of(9, 30), endTime = LocalTime.of(9, 45),
		reminder = true, date = LocalDate.now(), priority = 1,
		isRepeated = true, repeatWeekdays = "0,1,2,3,4"
	),
	Task(
		id = 4, uuid = "demo-4", title = "阅读 30 页",
		startTime = LocalTime.of(21, 0), endTime = LocalTime.of(21, 45),
		date = LocalDate.now(), priority = 0
	),
	Task(
		id = 5, uuid = "demo-5", title = "周末计划",
		startTime = LocalTime.of(11, 0), endTime = LocalTime.of(11, 30),
		reminder = true, date = LocalDate.now(), priority = 0,
		isRepeated = true, repeatWeekdays = "5,6"
	),
)
