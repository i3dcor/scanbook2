package com.i3dcor.scanbook.components

import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.ToneGenerator
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview as ComposePreview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

@androidx.annotation.OptIn(ExperimentalGetImage::class)
private fun processImageProxy(
    scanner: com.google.mlkit.vision.barcode.BarcodeScanner,
    imageProxy: ImageProxy,
    onIsbnFound: (String) -> Unit
) {
    val mediaImage = imageProxy.image
    if (mediaImage != null) {
        val inputImage = InputImage.fromMediaImage(
            mediaImage,
            imageProxy.imageInfo.rotationDegrees
        )

        scanner.process(inputImage)
            .addOnSuccessListener { barcodes ->
                for (barcode in barcodes) {
                    // ISBN usa formato EAN-13
                    if (barcode.format == Barcode.FORMAT_EAN_13) {
                        barcode.rawValue?.let { value ->
                            // ISBN-13 empieza con 978 o 979
                            if (value.startsWith("978") || value.startsWith("979")) {
                                onIsbnFound(value)
                                return@addOnSuccessListener
                            }
                        }
                    }
                }
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    } else {
        imageProxy.close()
    }
}

@androidx.annotation.OptIn(ExperimentalGetImage::class)
@Composable
private fun CameraPreviewView(
    isFlashEnabled: Boolean,
    onBarcodeDetected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Estado para evitar múltiples detecciones del mismo código
    var lastScannedIsbn by remember { mutableStateOf<String?>(null) }
    
    // Referencia a la cámara para controlar el flash
    var camera by remember { mutableStateOf<Camera?>(null) }
    
    // Controlar el flash cuando cambie el estado
    LaunchedEffect(isFlashEnabled) {
        camera?.cameraControl?.enableTorch(isFlashEnabled)
    }
    
    // Apagar el flash al salir de la pantalla
    DisposableEffect(Unit) {
        onDispose {
            camera?.cameraControl?.enableTorch(false)
        }
    }

    AndroidView(
        factory = { ctx ->
            PreviewView(ctx).apply {
                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                scaleType = PreviewView.ScaleType.FILL_CENTER
            }
        },
        modifier = modifier,
        update = { previewView ->
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder().build().also {
                    it.surfaceProvider = previewView.surfaceProvider
                }

                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                // ImageAnalysis para escanear códigos de barras
                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()

                val scanner = BarcodeScanning.getClient()
                val executor = Executors.newSingleThreadExecutor()

                imageAnalysis.setAnalyzer(executor) { imageProxy ->
                    processImageProxy(scanner, imageProxy) { isbn ->
                        if (isbn != lastScannedIsbn) {
                            lastScannedIsbn = isbn
                            // Reproducir pitido de confirmación
                            try {
                                val toneGen = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100)
                                toneGen.startTone(ToneGenerator.TONE_PROP_BEEP, 150)
                                toneGen.release()
                            } catch (e: Exception) {
                                Log.e("CameraPreview", "Error playing beep", e)
                            }
                            // Notificar ISBN detectado
                            onBarcodeDetected(isbn)
                        }
                    }
                }

                try {
                    cameraProvider.unbindAll()
                    camera = cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageAnalysis
                    )
                    // Aplicar estado inicial del flash
                    camera?.cameraControl?.enableTorch(isFlashEnabled)
                } catch (e: Exception) {
                    Log.e("CameraPreview", "Camera binding failed", e)
                }
            }, ContextCompat.getMainExecutor(context))
        }
    )
}

/**
 * Overlay que oscurece la pantalla excepto el área de escaneo
 */
@Composable
private fun ScannerOverlay(
    scanFrameWidth: Float,
    scanFrameHeight: Float,
    cornerRadius: Float,
    modifier: Modifier = Modifier
) {
    val overlayColor = Color.Black.copy(alpha = 0.5f)
    val frameColor = Color(0xFF4285F4)
    val strokeWidth = 4.dp

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer { 
                // Necesario para que BlendMode.Clear funcione
                alpha = 0.99f 
            }
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        // Posición del recuadro centrado
        val frameLeft = (canvasWidth - scanFrameWidth) / 2
        val frameTop = (canvasHeight - scanFrameHeight) / 2

        // Dibujar overlay oscuro en toda la pantalla
        drawRect(color = overlayColor)

        // Recortar el área del recuadro (hacerla transparente)
        drawRoundRect(
            color = Color.Transparent,
            topLeft = Offset(frameLeft, frameTop),
            size = Size(scanFrameWidth, scanFrameHeight),
            cornerRadius = CornerRadius(cornerRadius, cornerRadius),
            blendMode = BlendMode.Clear
        )

        // Dibujar el borde del recuadro
        drawRoundRect(
            color = frameColor,
            topLeft = Offset(frameLeft, frameTop),
            size = Size(scanFrameWidth, scanFrameHeight),
            cornerRadius = CornerRadius(cornerRadius, cornerRadius),
            style = Stroke(width = strokeWidth.toPx())
        )
    }
}

@Composable
fun CameraScreen(
    onBackClick: () -> Unit,
    onManualInputClick: () -> Unit,
    onIsbnDetected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    
    // Estado del flash
    var isFlashEnabled by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }
    
    // Manejar botón atrás del sistema Android
    BackHandler {
        onBackClick()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Camera Preview
        if (hasCameraPermission) {
            CameraPreviewView(
                isFlashEnabled = isFlashEnabled,
                onBarcodeDetected = onIsbnDetected,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Camera permission required",
                    color = Color.White
                )
            }
        }

        // Top Bar (Back and Flash)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            IconButton(
                onClick = { isFlashEnabled = !isFlashEnabled },
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(
                    imageVector = if (isFlashEnabled) Icons.Default.FlashOn else Icons.Default.FlashOff,
                    contentDescription = if (isFlashEnabled) "Flash On" else "Flash Off",
                    tint = Color.White
                )
            }
        }

        // Scanner overlay con oscurecimiento y recuadro de escaneo
        val density = LocalDensity.current
        val scanFrameWidthPx = with(density) { 300.dp.toPx() }
        val scanFrameHeightPx = with(density) { 200.dp.toPx() }
        val cornerRadiusPx = with(density) { 16.dp.toPx() }
        
        ScannerOverlay(
            scanFrameWidth = scanFrameWidthPx,
            scanFrameHeight = scanFrameHeightPx,
            cornerRadius = cornerRadiusPx
        )

        // Bottom Section (Text and Button)
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp, start = 16.dp, end = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Align the barcode within the frame to scan",
                    color = Color.White,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onManualInputClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                shape = RoundedCornerShape(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Keyboard,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = "Enter ISBN Manually",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}

@ComposePreview(showBackground = true)
@Composable
fun CameraScreenPreview() {
    CameraScreen(
        onBackClick = {},
        onManualInputClick = {},
        onIsbnDetected = {}
    )
}
