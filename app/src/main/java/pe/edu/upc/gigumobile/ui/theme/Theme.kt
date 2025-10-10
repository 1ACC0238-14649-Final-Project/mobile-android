package pe.edu.upc.gigumobile.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary            = GigU_Primary,
    onPrimary          = GigU_White,
    primaryContainer   = GigU_PrimaryLight,
    onPrimaryContainer = GigU_White,

    secondary          = GigU_PrimaryLight,
    onSecondary        = GigU_White,

    error              = GigU_Red,
    onError            = GigU_White,

    background         = GigU_NeutralWhite, // fondo de screens
    onBackground       = GigU_TextBlack,

    surface            = GigU_White,        // cards, sheets, app surfaces
    onSurface          = GigU_TextBlack,

    surfaceVariant     = GigU_NeutralWhite, // para chips/controles suaves
    onSurfaceVariant   = GigU_TextDarkGray,

    outline            = GigU_EmptyGray     // bordes inputs / divisores
)

private val DarkColorScheme = darkColorScheme(
    primary            = GigU_PrimaryLight,
    onPrimary          = GigU_White,
    primaryContainer   = GigU_Primary,
    onPrimaryContainer = GigU_White,

    secondary          = GigU_PrimaryLight,
    onSecondary        = GigU_White,

    error              = GigU_Red,
    onError            = GigU_White,

    background         = Color(0xFF0E1524),
    onBackground       = GigU_White,

    surface            = Color(0xFF121826),
    onSurface          = GigU_White,

    surfaceVariant     = Color(0xFF1C2434),
    onSurfaceVariant   = Color(0xFFBFC7D5),

    outline            = Color(0xFF41506A)
)

@Composable
fun GiguMobileTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography  = AppTypography, // tu Poppins
        content     = content
    )
}
