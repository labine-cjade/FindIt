package com.findit.dashboard

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color as AndroidColor
import android.os.Bundle
import android.widget.Toast
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview as ComposePreview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.findit.dashboard.ui.components.CardMode
import com.findit.dashboard.ui.components.ItemCardSheet
import com.findit.dashboard.ui.components.currentTimestamp
import com.findit.dashboard.ui.theme.CameraBackground
import com.findit.dashboard.ui.theme.CameraHeader
import com.findit.dashboard.ui.theme.DashboardFindItTheme
import com.findit.dashboard.ui.theme.Navy
import com.findit.dashboard.ui.theme.TabGray
import com.findit.dashboard.viewmodel.DashboardViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class TakePictureActivity : AppCompatActivity() {

    private val viewModel: DashboardViewModel by viewModels()

    private var previewView: PreviewView? = null
    private var imageCapture: ImageCapture? = null
    private lateinit var cameraExecutor: ExecutorService

    // Path of the photo that was just taken. While it is not null, the Add Item sheet is shown.
    private var capturedPhotoPath by mutableStateOf<String?>(null)

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            previewView?.let { startCamera(it) }
        } else {
            Toast.makeText(this, "Camera permission is required", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT)
        )

        cameraExecutor = Executors.newSingleThreadExecutor()

        setContent {
            DashboardFindItTheme {
                var selectedFloor by remember { mutableStateOf(Floors.cameraTabs.first()) }

                TakePictureScreen(
                    selectedFloor = selectedFloor,
                    onFloorSelected = { selectedFloor = it },
                    onCloseClick = { finish() },
                    onShutterClick = { takePhoto() },
                    onPreviewViewCreated = { view ->
                        previewView = view
                        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                            == PackageManager.PERMISSION_GRANTED) {
                            startCamera(view)
                        } else {
                            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    }
                )

                // After the photo is taken, the Add Item sheet opens on top of the camera.
                capturedPhotoPath?.let { path ->
                    ItemCardSheet(
                        initialMode = CardMode.ADD,
                        photoPath = path,
                        initialName = "",
                        initialLocation = "",
                        initialAudioPath = null,
                        dateText = "",
                        onDismiss = {
                            // Closed without saving: throw the photo away and go back to the camera.
                            File(path).delete()
                            capturedPhotoPath = null
                        },
                        onSave = { name, location, audioPath ->
                            viewModel.addItem(
                                DashboardItem(
                                    title = name,
                                    date = currentTimestamp(),
                                    photoPath = capturedPhotoPath,
                                    location = locationText(plain = location), // Keep only one clean location argument
                                    floor = selectedFloor,
                                    audioPath = audioPath
                                )
                            )
                            Toast.makeText(this, "$name saved!", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                    )
                }
            }
        }
    }

    private fun startCamera(viewFinder: PreviewView) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(viewFinder.surfaceProvider)
            }

            imageCapture = ImageCapture.Builder().build()

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture
                )
            } catch (exc: Exception) {
                Toast.makeText(this, "Camera failed to start", Toast.LENGTH_SHORT).show()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun takePhoto() {
        val capture = imageCapture ?: return
        if (capturedPhotoPath != null) return // already waiting for the form

        val fileName = "item_" + SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.US)
            .format(System.currentTimeMillis()) + ".jpg"
        // filesDir (not cacheDir) so Android does not delete the photo later
        val photoFile = File(filesDir, fileName)

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        capture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Toast.makeText(this@TakePictureActivity, "Capture failed", Toast.LENGTH_SHORT).show()
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    capturedPhotoPath = photoFile.absolutePath
                }
            }
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}

@Composable
fun TakePictureScreen(
    selectedFloor: String,
    onFloorSelected: (String) -> Unit,
    onCloseClick: () -> Unit,
    onShutterClick: () -> Unit,
    onPreviewViewCreated: (PreviewView) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CameraBackground)
    ) {
        // Top Action Bar Header (the color also fills the status bar area)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(CameraHeader)
                .statusBarsPadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = android.R.drawable.ic_menu_camera),
                    contentDescription = "FindIt Logo",
                    colorFilter = ColorFilter.tint(Color.White),
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "FindIt",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clickable(onClick = onCloseClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = android.R.drawable.ic_menu_close_clear_cancel),
                        contentDescription = "Close",
                        tint = Color.White,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }

        // Middle Camera Preview with Floor Selection Tabs overlay
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            // Camera Preview
            AndroidView(
                factory = { viewContext ->
                    PreviewView(viewContext).also { previewView ->
                        onPreviewViewCreated(previewView)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            // Top Horizontal Floor Selection Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Add Floor Button
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color(0xFFE0E0E0), RoundedCornerShape(6.dp))
                        .clickable {
                            Toast.makeText(context, "Adding new floors is coming soon", Toast.LENGTH_SHORT).show()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = android.R.drawable.ic_input_add),
                        contentDescription = "Add Floor",
                        tint = Color.Black
                    )
                }

                // 1st / 2nd / 3rd Floor tabs: the chosen one is saved with the item
                Floors.cameraTabs.forEach { floor ->
                    val isSelected = floor == selectedFloor
                    Box(
                        modifier = Modifier
                            .height(36.dp)
                            .background(if (isSelected) Navy else TabGray, RoundedCornerShape(4.dp))
                            .clickable { onFloorSelected(floor) }
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = floor,
                            color = if (isSelected) Color.White else Color.Black,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // Bottom Shutter Control Area
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(CameraBackground)
                .navigationBarsPadding()
                .height(120.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(Color.White, CircleShape)
                    .clickable(onClick = onShutterClick)
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = android.R.drawable.ic_menu_camera),
                    contentDescription = "Take Photo",
                    tint = CameraBackground,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Take photo",
                color = Color.White,
                fontSize = 12.sp
            )
        }
    }
}

@ComposePreview(showBackground = true)
@Composable
fun TakePictureScreenPreview() {
    DashboardFindItTheme {
        TakePictureScreen(
            selectedFloor = "1st Floor",
            onFloorSelected = {},
            onCloseClick = {},
            onShutterClick = {},
            onPreviewViewCreated = {}
        )
    }
}
