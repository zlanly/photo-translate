package com.example.phototranslate.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.light
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/**
 * Material 3 Color Scheme for Photo Translate App.
 * Light theme: Clean, bright background for camera preview.
 * Dark theme: Reduced glare for low-light camera use.
 */
object PhotoTranslateTheme {

    @Composable
    fun getSystemColorScheme(): ColorScheme {
        val isDark = isSystemInDarkTheme()
        val context = LocalContext.current

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Use dynamic colors on Android 12+
            return if (isDark) {
                dynamicDarkColorScheme(context)
            } else {
                dynamicLightColorScheme(context)
            }
        } else {
            // Fallback to static schemes on older versions
            return if (isDark) {
                DarkColorScheme
            } else {
                LightColorScheme
            }
        }
    }

    /**
     * Light theme color scheme optimized for camera UI.
     * High contrast for text overlays, clean background.
     */
    val LightColorScheme = light(
    // Primary brand color (for buttons, highlights)
    primary = colorRGB(0x0066CC),      // Google-like blue
    onPrimary = colorRGB(0xFFFFFFFF),

    // Secondary (for secondary actions)
    secondary = colorRGB(0x42A5F5),
    onSecondary = colorRGB(0xFFFFFFFF),

    // Tertiary (for accent elements)
    tertiary = colorRGB(0x00D54A),     // Google green

    // Background (camera preview area)
    background = colorRGB(0xFFFFFFFF),
    onBackground = colorRGB(0x000000),

    // Surface (cards, modals)
    surface = colorRGB(0xF5F5F5),
    onSurface = colorRGB(0x000000),

    // Error (for validation/error states)
    error = colorRGB(0xD32F2F),
    onError = colorRGB(0xFFFFFFFF),

    // Variant colors for UI elements
    variantPrimary = colorRGB(0x0066CC),
    variantSecondary = colorRGB(0x42A5F5),
    variantTertiary = colorRGB(0x00D54A),

    // Overlay colors for text on camera preview
    overlayDark = colorRGB(0xCC000000),  // 80% black
    overlayLight = colorRGB(0xCCFFFFFF), // 80% white

    // Transparent for overlays
    transparent = colorRGB(0x00000000),
    semiTransparent = colorRGB(0x80000000) // 50% black
    )

    /**
     * Dark theme color scheme optimized for low-light camera use.
     * Reduced brightness to prevent eye strain in dark environments.
     */
    val DarkColorScheme = dark(
    primary = colorRGB(0x64B5F6),
    onPrimary = colorRGB(0x000000),
    secondary = colorRGB(0x90CAF9),
    onSecondary = colorRGB(0x000000),
    tertiary = colorRGB(0x81C784),
    onTertiary = colorRGB(0x000000),
    background = colorRGB(0x121212),
    onBackground = colorRGB(0xFFFFFFFF),
    surface = colorRGB(0x1E1E1E),
    onSurface = colorRGB(0xFFFFFFFF),
    error = colorRGB(0xFFB6B6B6),
    onError = colorRGB(0x000000),
    variantPrimary = colorRGB(0x64B5F6),
    variantSecondary = colorRGB(0x90CAF9),
    variantTertiary = colorRGB(0x81C784),
    overlayDark = colorRGB(0xCC000000),
    overlayLight = colorRGB(0xCCFFFFFF),
    transparent = colorRGB(0x00000000),
    semiTransparent = colorRGB(0x80000000)
    )
}

/**
 * Helper to create a ColorInt from RGB hex value.
 */
@Composable
fun colorRGB(hex: Int): androidx.compose.ui.graphics.Color =
    androidx.compose.ui.graphics.Color(hex)
