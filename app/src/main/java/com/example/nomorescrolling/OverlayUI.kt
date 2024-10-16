import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Alignment
import androidx.compose.material3.Text
import androidx.compose.ui.unit.sp

@Composable
fun OverlayUI(onDismissRequest: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f)) // Semi-transparent background
            .clickable { onDismissRequest() }, // Click to dismiss the overlayz
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "You've been scrolling too much!",
            color = Color.White,
            fontSize = 20.sp
        )
    }
}