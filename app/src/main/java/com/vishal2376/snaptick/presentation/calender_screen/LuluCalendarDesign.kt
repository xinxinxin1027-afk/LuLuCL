package com.vishal2376.snaptick.presentation.calender_screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Immutable
internal data class LuluCalendarPalette(
    val canvas: Color,
    val surface: Color,
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
)

private val LightPalette = LuluCalendarPalette(
    canvas = Color(0xFFFCF9FB), surface = Color.White,
    mutedSurface = Color(0xFFF8F1F5), mutedStrong = Color(0xFFF2E7ED),
    ink = Color(0xFF30272F), secondaryInk = Color(0xFF746A73), tertiaryInk = Color(0xFF9D929B),
    divider = Color(0xFFF0E5EB), outline = Color(0xFFE9DCE4),
    brand = Color(0xFFCF3E70), brandStrong = Color(0xFFB9295A), brandSoft = Color(0xFFFCE8F0), brandFaint = Color(0xFFFFF3F7),
    danger = Color(0xFFBE3B49), dangerSoft = Color(0xFFFCEAEC),
)

private val DarkPalette = LuluCalendarPalette(
    canvas = Color(0xFF171216), surface = Color(0xFF21191F),
    mutedSurface = Color(0xFF2A2027), mutedStrong = Color(0xFF352832),
    ink = Color(0xFFFFF8FC), secondaryInk = Color(0xFFD3C3CC), tertiaryInk = Color(0xFF9E8C96),
    divider = Color(0xFF352832), outline = Color(0xFF44323D),
    brand = Color(0xFFCF3E70), brandStrong = Color(0xFFFF9BBB), brandSoft = Color(0xFF452433), brandFaint = Color(0xFF2C1D24),
    danger = Color(0xFFEE7582), dangerSoft = Color(0xFF42262D),
)

private val LocalPalette = compositionLocalOf { LightPalette }

internal object LuluCalendarTheme {
    val colors: LuluCalendarPalette
        @Composable get() = LocalPalette.current
}

@Composable
internal fun LuluCalendarDesignTheme(
    darkTheme: Boolean = androidx.compose.foundation.isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val p = if (darkTheme) DarkPalette else LightPalette
    val scheme = if (darkTheme) darkColorScheme(
        primary = p.brand, onPrimary = Color.White, background = p.canvas, onBackground = p.ink,
        surface = p.surface, onSurface = p.ink, outline = p.outline, error = p.danger,
    ) else lightColorScheme(
        primary = p.brand, onPrimary = Color.White, background = p.canvas, onBackground = p.ink,
        surface = p.surface, onSurface = p.ink, outline = p.outline, error = p.danger,
    )
    CompositionLocalProvider(LocalPalette provides p) {
        MaterialTheme(colorScheme = scheme, content = content)
    }
}

@Composable
internal fun LuluIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    filled: Boolean = false,
    brand: Boolean = false,
) {
    val p = LuluCalendarTheme.colors
    Surface(
        modifier = modifier.size(44.dp),
        shape = RoundedCornerShape(14.dp),
        color = when { brand -> p.brand; filled -> p.mutedSurface; else -> Color.Transparent },
        contentColor = if (brand) Color.White else p.secondaryInk,
        onClick = onClick,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
internal fun LuluSectionLabel(text: String) {
    Text(
        text = text,
        color = LuluCalendarTheme.colors.tertiaryInk,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(start = 3.dp, bottom = 1.dp),
    )
}

@Composable
internal fun LuluChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val p = LuluCalendarTheme.colors
    Surface(
        modifier = Modifier.height(38.dp),
        shape = RoundedCornerShape(12.dp),
        color = if (selected) p.brandSoft else p.surface,
        contentColor = if (selected) p.brandStrong else p.secondaryInk,
        border = BorderStroke(1.dp, if (selected) p.brand.copy(alpha = .28f) else p.outline),
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(if (selected) p.brand else p.outline, CircleShape),
            )
            Spacer(Modifier.width(7.dp))
            Text(text, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
internal fun LuluSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    val p = LuluCalendarTheme.colors
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        colors = SwitchDefaults.colors(
            checkedThumbColor = Color.White,
            checkedTrackColor = p.brand,
            uncheckedThumbColor = p.surface,
            uncheckedTrackColor = p.mutedStrong,
            checkedBorderColor = Color.Transparent,
            uncheckedBorderColor = Color.Transparent,
        ),
    )
}
