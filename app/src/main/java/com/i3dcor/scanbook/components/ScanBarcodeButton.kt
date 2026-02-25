package com.i3dcor.scanbook.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun ScanBarcodeButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ActionButton(
        text = "Scanbook",
        icon = Icons.Default.QrCodeScanner,
        onClick = onClick,
        modifier = modifier
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF1C1C1E)
@Composable
private fun ScanBarcodeButtonPreview() {
    ScanBarcodeButton(onClick = {})
}
