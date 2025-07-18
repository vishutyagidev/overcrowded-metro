
package metro.overcrowded.fourtheye.composables

import android.content.Context
import android.graphics.Bitmap
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.content.getSystemService
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import metro.overcrowded.fourtheye.s3.S3Uploader
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@Composable
fun CameraPreviewScreen() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val lensFacing = CameraSelector.LENS_FACING_BACK
    val previewView = remember { PreviewView(context) }

    val preview = remember { Preview.Builder().build() }
    val cameraSelector = remember { CameraSelector.Builder().requireLensFacing(lensFacing).build() }

    val capturedBitmap = remember { mutableStateOf<Bitmap?>(null) }
    val isCapturing = capturedBitmap.value != null

    LaunchedEffect(isCapturing) {
        if (!isCapturing) {
            val cameraProvider = context.getCameraProvider()
            cameraProvider.unbindAll()
            preview.surfaceProvider = previewView.surfaceProvider
            cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (isCapturing && capturedBitmap.value != null) {
            Image(
                bitmap = capturedBitmap.value!!.asImageBitmap(),
                contentDescription = "Captured Frame",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 64.dp)
        ) {
            if (isCapturing) {
                Box(
                    modifier = Modifier
                        .background(Color.White, shape = CircleShape)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { capturedBitmap.value = null }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                modifier = Modifier.size(48.dp),
                                contentDescription = "Close",
                                tint = Color.Black
                            )
                        }

                        Spacer(modifier = Modifier.width(48.dp))

                        IconButton(
                            onClick = {
                                capturedBitmap.value?.let { bitmap ->
                                    processFrame(context, bitmap)
                                }
                                capturedBitmap.value = null
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Submit",
                                modifier = Modifier.size(48.dp),
                                tint = Color.Black
                            )
                        }
                    }
                }
            } else {
                ShutterButton(onClick = {
                    val bitmap = previewView.bitmap
                    if (bitmap != null) {
                        capturedBitmap.value = bitmap
                    }
                })
            }
        }
    }
}

@Composable
fun ShutterButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(80.dp)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        // Outer Circle (border)
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(Color.White.copy(alpha = 0.7f), shape = CircleShape)
        )
        // Inner Circle (filled)
        Box(
            modifier = Modifier
                .size(60.dp)
                .background(Color.White, shape = CircleShape)
        )
    }
}

private fun processFrame(context: Context, bitmap: Bitmap) {
    val filename = "image.png"

    CoroutineScope(Dispatchers.IO).launch {
        val file = saveBitmapToFile(context, bitmap, filename)
        S3Uploader.uploadFile(context, file, filename)
    }
}

private fun saveBitmapToFile(context: Context, bitmap: Bitmap, filename: String): File {
    val file = File(context.cacheDir, filename)
    file.outputStream().use {
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
    }
    return file
}

private suspend fun Context.getCameraProvider(): ProcessCameraProvider =
    suspendCoroutine { continuation ->
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            continuation.resume(cameraProviderFuture.get())
        }, ContextCompat.getMainExecutor(this))
    }