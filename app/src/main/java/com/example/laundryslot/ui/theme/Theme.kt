package com.example.laundryslot.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary            = Blue40,
    onPrimary          = Color.White,
    primaryContainer   = Blue90,
    onPrimaryContainer = Blue10,
    secondary          = Cyan40,
    onSecondary        = Color.White,
    secondaryContainer = Cyan90,
    onSecondaryContainer = Color(0xFF001F24),
    background         = Grey99,
    onBackground       = Grey10,
    surface            = Grey99,
    onSurface          = Grey10,
    surfaceVariant     = Grey95,
    onSurfaceVariant   = Grey20,
    outline            = Color(0xFF73777F)
)

private val DarkColorScheme = darkColorScheme(
    primary            = Blue80,
    onPrimary          = Blue20,
    primaryContainer   = Blue30,
    onPrimaryContainer = Blue90,
    secondary          = Cyan80,
    onSecondary        = Color(0xFF00363D),
    secondaryContainer = Cyan40,
    onSecondaryContainer = Cyan90,
    background         = Grey10,
    onBackground       = Grey90,
    surface            = Grey10,
    onSurface          = Grey90,
    surfaceVariant     = Grey20,
    onSurfaceVariant   = Grey90
)

@Composable
fun LaundrySlotTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else      -> LightColorScheme
    }
    MaterialTheme(
        colorScheme = colorScheme,
        typography  = Typography,
        content     = content
    )
}

