package com.findit.dashboard

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview as ComposePreview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.findit.dashboard.ui.theme.DashboardFindItTheme
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class TakePictureActivity : AppCompatActivity() {

    private var previewView: PreviewView? = null
    private var imageCapture: ImageCapture? = null
    private lateinit var cameraExecutor: ExecutorService

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

        cameraExecutor = Executors.newSingleThreadExecutor()

        setContent {
            DashboardFindItTheme(dynamicColor = false) {
                TakePictureScreen(
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

        val fileName = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.US)
            .format(System.currentTimeMillis()) + ".jpg"
        val photoFile = File(cacheDir, fileName)

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        capture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Toast.makeText(this@TakePictureActivity, "Capture failed", Toast.LENGTH_SHORT).show()
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    // Photo is saved — now show the reusable Add Item card on top of the camera
                    ItemCardDialogFragment.forAdd(photoFile.absolutePath)
                        .show(supportFragmentManager, "item_card")
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
    onCloseClick: () -> Unit,
    onShutterClick: () -> Unit,
    onPreviewViewCreated: (PreviewView) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF1B262C))
    ) {
        // Top Action Bar Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(Color(0xFF3A6073))
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

        // Middle Camera Preview with Floor Selection Tabs overlay
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            // Camera Preview
            AndroidView(
                factory = { context ->
                    PreviewView(context).also { previewView ->
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
                        .background(Color(0xFFE0E0E0), RoundedCornerShape(6.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = android.R.drawable.ic_input_add),
                        contentDescription = "Add Floor",
                        tint = Color.Black
                    )
                }

                // 1st Floor Button
                Box(
                    modifier = Modifier
                        .height(36.dp)
                        .background(Color(0xFF1B4965), RoundedCornerShape(4.dp))
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "1st Floor",
                        color = Color.White,
                        fontSize = 12.sp
                    )
                }

                // 2nd Floor Button
                Box(
                    modifier = Modifier
                        .height(36.dp)
                        .background(Color(0xFFC4C4C4), RoundedCornerShape(4.dp))
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "2nd Floor",
                        color = Color.Black,
                        fontSize = 12.sp
                    )
                }

                // 3rd Floor Button
                Box(
                    modifier = Modifier
                        .height(36.dp)
                        .background(Color(0xFFC4C4C4), RoundedCornerShape(4.dp))
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "3rd Floor",
                        color = Color.Black,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Bottom Shutter Control Area
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .background(Color(0xFF1B262C)),
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
                    tint = Color(0xFF1B262C),
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
    DashboardFindItTheme(dynamicColor = false) {
        TakePictureScreen(
            onCloseClick = {},
            onShutterClick = {},
            onPreviewViewCreated = {}
        )
    }
}
