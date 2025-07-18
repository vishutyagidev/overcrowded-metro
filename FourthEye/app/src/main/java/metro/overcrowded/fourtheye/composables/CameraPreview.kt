
package metro.overcrowded.fourtheye.composables

import android.content.Context
import android.graphics.Bitmap
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import metro.overcrowded.fourtheye.s3.S3Uploader
import metro.overcrowded.fourtheye.utils.FileUtils
import metro.overcrowded.fourtheye.utils.SensorUtils
import org.json.JSONObject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import androidx.core.graphics.createBitmap

const val filePrefix = "image"
const val fileExtension = ".png"
const val fileMaskSuffix = "mask"
const val fileMetaName = "metadata"
const val canvasStrokeWidth = 64f

@Composable
fun CameraPreviewScreen() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val lensFacing = CameraSelector.LENS_FACING_BACK
    val previewView = remember { PreviewView(context) }

    val preview = remember { Preview.Builder().build() }
    val cameraSelector = remember { CameraSelector.Builder().requireLensFacing(lensFacing).build() }

    val strokes = remember { mutableStateListOf<Stroke>() }

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

            DrawingCanvas(strokes = strokes)
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
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                capturedBitmap.value = null
                                strokes.clear()
                            }
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
                                    processFrame(context, bitmap, strokes)
                                }
                                capturedBitmap.value = null
                                strokes.clear()
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
fun DrawingCanvas(strokes: MutableList<Stroke>) {
    val currentPath = remember { Path() }
    val isDrawing = remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = {
                        isDrawing.value = true
                        currentPath.moveTo(it.x, it.y)
                    },
                    onDrag = { change, dragAmount ->
                        if (isDrawing.value) {
                            currentPath.lineTo(change.position.x, change.position.y)
                        }
                    },
                    onDragEnd = {
                        if (isDrawing.value) {
                            strokes.add(Stroke(path = Path().apply { addPath(currentPath) }))
                            currentPath.reset()
                            isDrawing.value = false
                        }
                    }
                )
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            strokes.forEach { stroke ->
                drawPath(
                    path = stroke.path,
                    color = stroke.color,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(
                        width = stroke.strokeWidth
                    )
                )
            }

            // Draw current in-progress path
            drawPath(
                path = currentPath,
                color = Color.White,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = canvasStrokeWidth)
            )
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

data class Stroke(val path: Path, val color: Color = Color.White, val strokeWidth: Float = canvasStrokeWidth)

private fun generateMaskBitmap(width: Int, height: Int, strokes: List<Stroke>): Bitmap {
    val bitmap = createBitmap(width, height)
    val canvas = android.graphics.Canvas(bitmap)
    canvas.drawColor(android.graphics.Color.BLACK) // Black background

    val paint = android.graphics.Paint().apply {
        color = android.graphics.Color.WHITE
        style = android.graphics.Paint.Style.STROKE
        strokeWidth = canvasStrokeWidth
        isAntiAlias = true
    }

    strokes.forEach { stroke ->
        val androidPath = android.graphics.Path().apply {
            stroke.path.asAndroidPath().let { set(it) }
        }
        canvas.drawPath(androidPath, paint)
    }

    return bitmap
}

private fun processFrame(context: Context, rawImage: Bitmap, strokes: List<Stroke>) {
    val filename = "$filePrefix$fileExtension"
    val maskFilename = "$filePrefix-$fileMaskSuffix$fileExtension"

    // async
    CoroutineScope(Dispatchers.IO).launch {
        val rawFile = FileUtils.saveBitmapToFile(context, rawImage, filename)
        S3Uploader.uploadFile(context, rawFile, filename)

        val maskBitmap = generateMaskBitmap(rawImage.width, rawImage.height, strokes)
        val maskFile = FileUtils.saveBitmapToFile(context, maskBitmap, maskFilename)
        S3Uploader.uploadFile(context, maskFile, maskFilename)

        val jsonString = buildMetadataJson(context)
        val metadataFile = FileUtils.saveTextToFile(context, fileMetaName, jsonString)
        S3Uploader.uploadFile(context, metadataFile, fileMetaName)

        withContext(Dispatchers.Main) {
            Toast.makeText(context, "Uploaded the files successfully", Toast.LENGTH_SHORT).show()
        }
    }
}

suspend fun buildMetadataJson(context: Context): String {
    val location = SensorUtils.fetchCurrentLocation(context)
    val direction = SensorUtils.fetchDeviceDirection(context)

    val json = JSONObject().apply {
        put("latitude", location?.latitude ?: 0.0)
        put("longitude", location?.longitude ?: 0.0)
        put("direction", direction)
    }

    return json.toString()
}

private suspend fun Context.getCameraProvider(): ProcessCameraProvider =
    suspendCoroutine { continuation ->
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            continuation.resume(cameraProviderFuture.get())
        }, ContextCompat.getMainExecutor(this))
    }