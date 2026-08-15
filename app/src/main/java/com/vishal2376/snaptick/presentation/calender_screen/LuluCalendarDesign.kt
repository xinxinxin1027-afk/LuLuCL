package com.vishal2376.snaptick.presentation.calender_screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Immutable
data class LuluCalendarPalette(
    val canvas: Color,
    val surface: Color,
    val elevatedSurface: Color,
    val mutedSurface: Color,
    val mutedStrong: Color,
    val ink: Color,
    val secondaryInk: Color,
    val tertiaryInk: Color,
    val divider: Color,
    val outline: Color,
    val brand: Color,
    val brandStrong: Color,
    val brandSoft: Color,
    val brandFaint: Color,
    val danger: Color,
    val dangerSoft: Color,
    val warning: Color,
    val success: Color,
    val blue: Color,
)

private val LightLuluPalette = LuluCalendarPalette(
    canvas = Color(0xFFFCF9FB),
    surface = Color(0xFFFFFFFF),
    elevatedSurface = Color(0xFFFFFDFD),
    mutedSurface = Color(0xFFF8F1F5),
    mutedStrong = Color(0xFFF2E7ED),
    ink = Color(0xFF30272F),
    secondaryInk = Color(0xFF746A73),
    tertiaryInk = Color(0xFF9D929B),
    divider = Color(0xFFF0E5EB),
    outline = Color(0xFFE9DCE4),
    brand = Color(0xFFCF3E70),
    brandStrong = Color(0xFFB9295A),
    brandSoft = Color(0xFFFCE8F0),
    brandFaint = Color(0xFFFFF3F7),
    danger = Color(0xFFBE3B49),
    dangerSoft = Color(0xFFFCEAEC),
    warning = Color(0xFFC18A2E),
    success = Color(0xFF679979),
    blue = Color(0xFF6287C5),
)

private val DarkLuluPalette = LuluCalendarPalette(
    canvas = Color(0xFF171216),
    surface = Color(0xFF21191F),
    elevatedSurface = Color(0xFF251C22),
    mutedSurface = Color(0xFF2A2027),
    mutedStrong = Color(0xFF352832),
    ink = Color(0xFFFFF8FC),
    secondaryInk = Color(0xFFD3C3CC),
    tertiaryInk = Color(0xFF9E8C96),
    divider = Color(0xFF352832),
    outline = Color(0xFF44323D),
    brand = Color(0xFFCF3E70),
    brandStrong = Color(0xFFFF9BBB),
    brandSoft = Color(0xFF452433),
    brandFaint = Color(0xFF2C1D24),
    danger = Color(0xFFEE7582),
    dangerSoft = Color(0xFF42262D),
    warning = Color(0xFFE4B25A),
    success = Color(0xFF83B492),
    blue = Color(0xFF8BA8E0),
)

val LocalLuluCalendarPalette = compositionLocalOf { LightLuluPalette }

object LuluCalendarTheme {
    val colors: LuluCalendarPalette
        @Composable get() = LocalLuluCalendarPalette.current
}

object LuluCalendarDimens {
    val screenHorizontal = 16.dp
    val topBarHeight = 58.dp
    val touchTarget = 44.dp
    val cardRadius = 18.dp
    val largeCardRadius = 24.dp
    val compactRadius = 14.dp
    val sectionSpacing = 14.dp
}

@Composable
fun LuluCalendarDesignTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val palette = if (darkTheme) DarkLuluPalette else LightLuluPalette
    val scheme = if (darkTheme) {
        darkColorScheme(
            primary = palette.brand,
            onPrimary = Color.White,
            primaryContainer = palette.brandSoft,
            onPrimaryContainer = palette.brandStrong,
            background = palette.canvas,
            onBackground = palette.ink,
            surface = palette.surface,
            onSurface = palette.ink,
            outline = palette.outline,
            error = palette.danger,
        )
    } else {
        lightColorScheme(
            primary = palette.brand,
            onPrimary = Color.White,
            primaryContainer = palette.brandSoft,
            onPrimaryContainer = palette.brandStrong,
            background = palette.canvas,
            onBackground = palette.ink,
            surface = palette.surface,
            onSurface = palette.ink,
            outline = palette.outline,
            error = palette.danger,
        )
    }

    CompositionLocalProvider(LocalLuluCalendarPalette provides palette) {
        MaterialTheme(colorScheme = scheme, content = content)
    }
}

@Composable
fun LuluIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    filled: Boolean = false,
    brand: Boolean = false,
) {
    val colors = LuluCalendarTheme.colors
    val background = when {
        brand -> colors.brand
        filled -> colors.mutedSurface
        else -> Color.Transparent
    }
    val contentColor = when {
        brand -> Color.White
        else -> colors.secondaryInk
    }
    Surface(
        modifier = modifier.size(LuluCalendarDimens.touchTarget),
        shape = RoundedCornerShape(LuluCalendarDimens.compactRadius),
        color = background,
        contentColor = contentColor,
        onClick = onClick,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                modifier = Modifier.size(21.dp),
            )
        }
    }
}

@Composable
fun LuluSurfaceCard(
    modifier: Modifier = Modifier,
    radius: Dp = LuluCalendarDimens.largeCardRadius,
    border: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val colors = LuluCalendarTheme.colors
    if (onClick == null) {
        Surface(
            modifier = modifier,
            shape = RoundedCornerShape(radius),
            color = colors.surface,
            contentColor = colors.ink,
            border = if (border) BorderStroke(1.dp, colors.outline) else null,
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
        ) {
            Box(modifier = Modifier.padding(contentPadding)) { content() }
        }
    } else {
        Surface(
            modifier = modifier,
            shape = RoundedCornerShape(radius),
            color = colors.surface,
            contentColor = colors.ink,
            border = if (border) BorderStroke(1.dp, colors.outline) else null,
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
            onClick = onClick,
        ) {
            Box(modifier = Modifier.padding(contentPadding)) { content() }
        }
    }
}

@Composable
fun LuluSegmentedControl(
    selected: CalendarViewMode,
    onSelect: (CalendarViewMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LuluCalendarTheme.colors
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(40.dp),
        shape = RoundedCornerShape(14.dp),
        color = colors.mutedSurface,
        border = BorderStroke(1.dp, colors.outline),
    ) {
        Row(
            modifier = Modifier.padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            CalendarViewMode.values().forEach { mode ->
                val label = when (mode) {
                    CalendarViewMode.MONTH -> "月"
                    CalendarViewMode.WEEK -> "周"
                    CalendarViewMode.AGENDA -> "日程"
                }
                LuluSegmentItem(
                    selected = selected == mode,
                    label = label,
                    onClick = { onSelect(mode) },
                )
            }
        }
    }
}

@Composable
private fun RowScope.LuluSegmentItem(
    selected: Boolean,
    label: String,
    onClick: () -> Unit,
) {
    val colors = LuluCalendarTheme.colors
    Surface(
        modifier = Modifier
            .weight(1f)
            .height(32.dp),
        shape = RoundedCornerShape(10.dp),
        color = if (selected) colors.surface else Color.Transparent,
        contentColor = if (selected) colors.ink else colors.tertiaryInk,
        onClick = onClick,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
fun LuluSectionLabel(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        modifier = modifier.padding(start = 3.dp, bottom = 8.dp),
        color = LuluCalendarTheme.colors.tertiaryInk,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
    )
}

@Composable
fun LuluPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
) {
    val colors = LuluCalendarTheme.colors
    Button(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(15.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = colors.brand,
            contentColor = Color.White,
        ),
        contentPadding = PaddingValues(horizontal = 18.dp),
    ) {
        if (icon != null) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(7.dp))
        }
        Text(text = text, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun LuluSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    danger: Boolean = false,
    icon: ImageVector? = null,
) {
    val colors = LuluCalendarTheme.colors
    val container = if (danger) colors.dangerSoft else colors.surface
    val foreground = if (danger) colors.danger else colors.ink
    Button(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(15.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = container,
            contentColor = foreground,
        ),
        border = BorderStroke(1.dp, if (danger) colors.danger.copy(alpha = .22f) else colors.outline),
        contentPadding = PaddingValues(horizontal = 18.dp),
    ) {
        if (icon != null) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(7.dp))
        }
        Text(text = text, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun LuluSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LuluCalendarTheme.colors
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier,
        colors = SwitchDefaults.colors(
            checkedThumbColor = Color.White,
            checkedTrackColor = colors.brand,
            checkedBorderColor = Color.Transparent,
            uncheckedThumbColor = colors.surface,
            uncheckedTrackColor = colors.mutedStrong,
            uncheckedBorderColor = Color.Transparent,
        ),
    )
}

@Composable
fun LuluLabelValue(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            color = LuluCalendarTheme.colors.ink,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = value,
            color = LuluCalendarTheme.colors.secondaryInk,
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
fun LuluChip(
    text: String,
    selected: Boolean = false,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val colors = LuluCalendarTheme.colors
    val background = if (selected) colors.brandSoft else colors.surface
    val foreground = if (selected) colors.brandStrong else colors.secondaryInk
    val border = if (selected) colors.brand.copy(alpha = .28f) else colors.outline
    val source = remember { MutableInteractionSource() }
    Row(
        modifier = modifier
            .height(38.dp)
            .background(background, RoundedCornerShape(12.dp))
            .border(1.dp, border, RoundedCornerShape(12.dp))
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = source,
                        indication = null,
                        role = Role.Button,
                        onClick = onClick,
                    )
                } else Modifier,
            )
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(if (selected) colors.brand else border, CircleShape),
        )
        Spacer(modifier = Modifier.width(7.dp))
        Text(
            text = text,
            color = foreground,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

fun parseCalendarColor(hex: String): Color = runCatching {
    Color(android.graphics.Color.parseColor(normalizeHexColor(hex)))
}.getOrDefault(Color(0xFFCF3E70))
