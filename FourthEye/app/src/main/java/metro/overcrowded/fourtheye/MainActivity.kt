package metro.overcrowded.fourtheye

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import metro.overcrowded.fourtheye.composables.CameraPreviewScreen
import metro.overcrowded.fourtheye.ui.theme.FourthEyeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FourthEyeTheme {
                HomeScreen()
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen(modifier: Modifier = Modifier) {
    val cameraPermissionState = rememberPermissionState(android.Manifest.permission.CAMERA)
    if (cameraPermissionState.status.isGranted) {
        CameraPreviewScreen()
    } else {
        Column(
            modifier = modifier.fillMaxSize().wrapContentSize().widthIn(max = 480.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val textToShow = "This app needs camera permissions to work ✨\n" +
                    "We cannot take pictures without the camera :)"
            Text(textToShow, textAlign = TextAlign.Center)
            Spacer(Modifier.height(16.dp))
            Button(onClick = { cameraPermissionState.launchPermissionRequest() }) {
                Text("Allow Camera")
            }
        }
    }
}
