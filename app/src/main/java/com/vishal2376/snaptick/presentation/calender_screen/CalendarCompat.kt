package com.vishal2376.snaptick.presentation.calender_screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp

/**
 * Compatibility shim for the Material3 version pinned by this project.
 * The old BottomSheetScaffold has no floatingActionButton slot, while the
 * calendar screen intentionally keeps the existing FAB visual. We layer the
 * FAB above the scaffold without changing the calendar/grid appearance.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun BottomSheetScaffold(
    scaffoldState: BottomSheetScaffoldState,
    sheetPeekHeight: Dp,
    sheetContent: @Composable ColumnScope.() -> Unit,
    topBar: @Composable () -> Unit,
    floatingActionButton: @Composable () -> Unit,
    content: @Composable (PaddingValues) -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        androidx.compose.material3.BottomSheetScaffold(
            scaffoldState = scaffoldState,
            sheetPeekHeight = sheetPeekHeight,
            sheetContent = sheetContent,
            topBar = topBar,
            content = content,
        )
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomEnd,
        ) {
            floatingActionButton()
        }
    }
}

/**
 * CalendarTaskEditor uses Icons.Default.Check. The project's older icon
 * artifact requires an explicit extension import; providing the extension in
 * this package keeps that editor source simple while remaining equivalent.
 */
internal val Icons.Filled.Check: ImageVector
    get() = Icons.Default.CheckCircle
