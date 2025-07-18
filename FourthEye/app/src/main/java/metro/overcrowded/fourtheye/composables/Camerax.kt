
package metro.overcrowded.fourtheye.composables

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
                .padding(24.dp)
        ) {
            if (isCapturing) {
                Row(horizontalArrangement = Arrangement.SpaceEvenly) {
                    Button(onClick = {
                        // Close -> discard image, return to preview
                        capturedBitmap.value = null
                    }) {
                        Text("Close")
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Button(onClick = {
                        // Submit -> process image and return to preview
                        capturedBitmap.value?.let { bitmap ->
                            processFrame(bitmap)
                        }
                        capturedBitmap.value = null
                    }) {
                        Text("Submit")
                    }
                }
            } else {
                Button(onClick = {
                    // Capture -> freeze current frame
                    val bitmap = previewView.bitmap
                    if (bitmap != null) {
                        capturedBitmap.value = bitmap
                    }
                }) {
                    Text("Capture")
                }
            }
        }
    }
}

private fun processFrame(bitmap: Bitmap) {
    CoroutineScope(Dispatchers.IO).launch {
        // Do your heavy processing here
    }
}

private suspend fun Context.getCameraProvider(): ProcessCameraProvider =
    suspendCoroutine { continuation ->
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            continuation.resume(cameraProviderFuture.get())
        }, ContextCompat.getMainExecutor(this))
    }