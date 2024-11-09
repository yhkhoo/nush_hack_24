import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography


// Light Blue Colors for the theme
val LightBlue = Color(0xFF03A9F4)        // Primary Color (Light Blue)
val LightBlueVariant = Color(0xFF0288D1) // Darker Blue for variants
val LightBlueBackground = Color(0xFFE1F5FE)  // Background Color (Light Blue background)
val White = Color(0xFFFFFFFF)             // White for background and surfaces
val Black = Color(0xFF000000)             // Black for text and icons

// Error color (default red)
val ErrorColor = Color(0xFFB00020)

// Define the light color scheme
val LightColorScheme = lightColorScheme(
    primary = LightBlue,
    primaryContainer = LightBlueVariant,
    secondary = LightBlueVariant,
    background = LightBlueBackground,
    surface = White,
    error = ErrorColor,
    onPrimary = White,
    onSecondary = Black,
    onBackground = Black,
    onSurface = Black,
    onError = White
)
val Typography = Typography()
@Composable
fun YourAppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme, // Apply the light blue color scheme
        typography = Typography,         // Use your custom typography (you can define this if needed)
        content = content
    )
}

