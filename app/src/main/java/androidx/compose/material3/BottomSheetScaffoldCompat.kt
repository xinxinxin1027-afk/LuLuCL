package androidx.compose.material3

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp

/** Compatibility overload for the older Material3 pinned by LuluCalendar. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheetScaffold(
    scaffoldState: BottomSheetScaffoldState,
    sheetPeekHeight: Dp,
    sheetContainerColor: Color,
    sheetContent: @Composable ColumnScope.() -> Unit,
    topBar: @Composable () -> Unit,
    floatingActionButton: @Composable () -> Unit,
    content: @Composable (PaddingValues) -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        BottomSheetScaffold(
            scaffoldState = scaffoldState,
            sheetPeekHeight = sheetPeekHeight,
            sheetContainerColor = sheetContainerColor,
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
