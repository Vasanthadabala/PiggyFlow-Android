package com.piggylabs.piggyflow.core.designsystem.theme

import android.content.Context
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import com.piggylabs.piggyflow.core.di.preferencesManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

enum class ThemeMode { SYSTEM, LIGHT, DARK }

/**
 * The Profile screen's Appearance row writes here; [PiggyFlowTheme] reads it. Backed
 * by a [androidx.compose.runtime.State] so flipping it recomposes the whole app
 * immediately, no activity recreation needed.
 *
 * The persisted value lives in
 * [com.piggylabs.piggyflow.core.datastore.PreferencesManager]. [init] reads it
 * synchronously because the very first composition needs a theme; writes are async.
 */
object ThemePreference {
    var mode by mutableStateOf(ThemeMode.SYSTEM)
        private set

    fun init(context: Context) {
        val saved = preferencesManager(context).snapshotBlocking().themeMode
        mode = runCatching { ThemeMode.valueOf(saved ?: ThemeMode.SYSTEM.name) }
            .getOrDefault(ThemeMode.SYSTEM)
    }

    fun set(context: Context, newMode: ThemeMode) {
        mode = newMode
        val prefs = preferencesManager(context)
        CoroutineScope(Dispatchers.IO).launch {
            prefs.setThemeMode(newMode.name)
        }
    }
}

private val LightColorScheme = lightColorScheme(
    background = LightAppColors.background,
    surface = LightAppColors.surface,
    surfaceVariant = LightAppColors.surfaceMuted,
    onBackground = LightAppColors.text,
    onSurface = LightAppColors.text,
    onSurfaceVariant = LightAppColors.textMuted,
    primary = LightAppColors.accent,
    onPrimary = Color.White,
    primaryContainer = LightAppColors.accentSoft,
    onPrimaryContainer = LightAppColors.onAccentSoft,
    outline = LightAppColors.textMuted,
    error = LightAppColors.negative
)

private val DarkColorScheme = darkColorScheme(
    background = DarkAppColors.background,
    surface = DarkAppColors.surface,
    surfaceVariant = DarkAppColors.surfaceMuted,
    onBackground = DarkAppColors.text,
    onSurface = DarkAppColors.text,
    onSurfaceVariant = DarkAppColors.textMuted,
    primary = DarkAppColors.accent,
    onPrimary = Color(0xFF04170F),
    primaryContainer = DarkAppColors.accentSoft,
    onPrimaryContainer = DarkAppColors.onAccentSoft,
    outline = DarkAppColors.textMuted,
    error = DarkAppColors.negative
)

@Composable
fun PiggyFlowTheme(
    darkTheme: Boolean = when (ThemePreference.mode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    },
    content: @Composable () -> Unit
) {
    val appColors = if (darkTheme) DarkAppColors else LightAppColors

    CompositionLocalProvider(LocalAppColors provides appColors) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
            typography = Typography,
            content = content
        )
    }
}

@Composable
fun appColors() = LocalAppColors.current
