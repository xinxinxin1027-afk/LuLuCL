package com.vishal2376.snaptick.presentation.onboarding.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.vishal2376.snaptick.R
import com.vishal2376.snaptick.data.calendar.CalendarInfo
import com.vishal2376.snaptick.presentation.common.h1TextStyle
import com.vishal2376.snaptick.presentation.common.infoDescTextStyle
import com.vishal2376.snaptick.presentation.common.taskTextStyle

@Suppress("UNUSED_PARAMETER")
@Composable
fun RestoreAndSyncPage(
	calendarSyncEnabled: Boolean,
	notificationsEnabled: Boolean,
	writableCalendars: List<CalendarInfo>,
	selectedCalendarId: Long?,
	calendarPermissionGranted: Boolean,
	onRestoreClick: () -> Unit,
	onPickIcsClick: () -> Unit,
	onCalendarSyncToggle: (Boolean) -> Unit,
	onSelectCalendar: (Long) -> Unit,
	onRequestCalendarPermission: () -> Unit,
	onRefreshCalendars: () -> Unit,
	onEnableNotifications: () -> Unit,
) {
	Column(
		modifier = Modifier
			.fillMaxSize()
			.padding(horizontal = 24.dp, vertical = 24.dp),
		horizontalAlignment = Alignment.CenterHorizontally,
		verticalArrangement = Arrangement.Center,
	) {
		Text(
			text = stringResource(R.string.onboarding_notifications_title),
			style = h1TextStyle,
			color = MaterialTheme.colorScheme.onBackground,
			textAlign = TextAlign.Center,
		)
		Spacer(Modifier.height(10.dp))
		Text(
			text = stringResource(R.string.onboarding_notifications_description),
			style = infoDescTextStyle,
			color = MaterialTheme.colorScheme.onPrimaryContainer,
			textAlign = TextAlign.Center,
			modifier = Modifier.fillMaxWidth(),
		)
		Spacer(Modifier.height(24.dp))

		if (notificationsEnabled) {
			Text(
				text = stringResource(R.string.notifications_enabled),
				style = taskTextStyle,
				color = MaterialTheme.colorScheme.primary,
			)
		} else {
			Button(onClick = onEnableNotifications) {
				Text(text = stringResource(R.string.enable_notifications))
			}
		}
	}
}
