package com.i3dcor.scanbook.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.i3dcor.scanbook.ui.theme.ScanBookTheme

@Composable
fun PhotoCaptureScreen(
    modifier: Modifier = Modifier,
    onCaptureClick: () -> Unit = {},
    onFlashClick: (Boolean) -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    var isFlashOn by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        // Placeholder for CameraX Preview
        CameraPreviewPlaceholder(modifier = Modifier.fillMaxSize())

        // Top right flash button
        FlashToggleButton(
            isFlashOn = isFlashOn,
            onClick = {
                isFlashOn = !isFlashOn
                onFlashClick(isFlashOn)
            },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(24.dp)
        )

        // Bottom section with gradient and capture button
        BottomControlsOverlay(
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            CapturePhotoButton(
                onClick = onCaptureClick,
                modifier = Modifier.padding(bottom = 48.dp, start = 32.dp, end = 32.dp)
            )
        }
    }
}

@Composable
fun CameraPreviewPlaceholder(modifier: Modifier = Modifier) {
    // Represents the camera feed. For UI preview purposes, we use a solid peach color
    // to match the background in the provided image.
    Box(
        modifier = modifier
            .background(Color(0xFFF1D4C9)) // Peach background from image
    ) {
        // In a real implementation, CameraX's AndroidView would go here.
        // I won't draw the fake camera inside, as the requirement is just the UI overlay.
    }
}

@Composable
fun FlashToggleButton(
    isFlashOn: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.2f)) // Semi-transparent black
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = if (isFlashOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
            contentDescription = if (isFlashOn) "Turn flash off" else "Turn flash on",
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
fun BottomControlsOverlay(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        Color.Black.copy(alpha = 0.5f) // Dark gradient at the bottom
                    )
                )
            ),
        contentAlignment = Alignment.BottomCenter
    ) {
        content()
    }
}

@Composable
fun CapturePhotoButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(60.dp), // A bit taller than standard buttons
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF2962FF) // Primary Blue
        ),
        shape = RoundedCornerShape(30.dp) // Fully rounded
    ) {
        Icon(
            imageVector = Icons.Default.CameraAlt,
            contentDescription = null,
            modifier = Modifier.padding(end = 8.dp),
            tint = Color.White
        )
        Text(
            text = "Hacer Foto",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PhotoCaptureScreenPreview() {
    ScanBookTheme {
        PhotoCaptureScreen()
    }
}
