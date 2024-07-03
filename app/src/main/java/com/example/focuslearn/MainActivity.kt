package com.example.focuslearn

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.focuslearn.ui.theme.FocusLearnTheme
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc
import java.io.IOException

class MainViewModel : ViewModel() {
    val imageBitmap = MutableLiveData<Bitmap?>()
}

fun postDataToFlaskServer(json: String) {
    val client = OkHttpClient()

    val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
    val requestBody = json.toRequestBody(mediaType)

    val request = Request.Builder()
        .url("http://192.168.0.101:3700/eye_info") // Flask 서버의 엔드포인트 URL
        .post(requestBody)
        .build()
//    val request1 = Request.Builder()
//        .url("http://192.168.45.12:5000/test1") // Flask 서버의 엔드포인트 URL
//        .post(requestBody)
//        .build()
    Log.d("flask", "requestFinish")

    client.newCall(request).execute().use { response ->
        if (!response.isSuccessful) throw IOException("Unexpected code $response")

        println(response.body?.string())
    }
//    client.newCall(request1).execute().use { response ->
//        if (!response.isSuccessful) throw IOException("Unexpected code $response")
//
//        println(response.body?.string())
//    }
}


private fun checkCameraPermission(context: Context): Boolean {
    return ActivityCompat.checkSelfPermission(
        context,
        Manifest.permission.CAMERA
    ) == PackageManager.PERMISSION_GRANTED
}

private fun checkInternetPermission(context: Context): Boolean {
    return ActivityCompat.checkSelfPermission(
        context,
        Manifest.permission.INTERNET
    ) == PackageManager.PERMISSION_GRANTED
}

private fun imageToIntArray(imageProxy: ImageProxy): Array<Array<IntArray>> {
    val yBuffer = imageProxy.planes[0].buffer
    val uBuffer = imageProxy.planes[1].buffer
    val vBuffer = imageProxy.planes[2].buffer

    val ySize = yBuffer.remaining()
    val uSize = uBuffer.remaining()
    val vSize = vBuffer.remaining()

    val yBytes = ByteArray(ySize)
    val uBytes = ByteArray(uSize)
    val vBytes = ByteArray(vSize)

    yBuffer.get(yBytes)
    uBuffer.get(uBytes)
    vBuffer.get(vBytes)

    val nv21Bytes = ByteArray(ySize + uSize + vSize)
    System.arraycopy(yBytes, 0, nv21Bytes, 0, ySize)
    for (i in 0 until vSize) {
        nv21Bytes[ySize + i * 2] = vBytes[i]
        nv21Bytes[ySize + i * 2 + 1] = uBytes[i]
    }

    val yuvImage = Mat(imageProxy.height + imageProxy.height / 2, imageProxy.width, CvType.CV_8UC1)
    yuvImage.put(0, 0, nv21Bytes)

    val bgrImage = Mat()
    Imgproc.cvtColor(yuvImage, bgrImage, Imgproc.COLOR_YUV2BGR_NV21)

    // 3차원 배열 생성
    val height = bgrImage.rows()
    val width = bgrImage.cols()
    val bgrArray = Array(height) { Array(width) { IntArray(3) } }

    val bgrBytes = ByteArray(bgrImage.total().toInt() * bgrImage.elemSize().toInt())
    bgrImage.get(0, 0, bgrBytes)

    // ByteArray를 3차원 IntArray로 변환
    var index = 0
    for (i in 0 until height) {
        for (j in 0 until width) {
            bgrArray[i][j][0] = bgrBytes[index].toInt() and 0xFF  // Blue
            bgrArray[i][j][1] = bgrBytes[index + 1].toInt() and 0xFF  // Green
            bgrArray[i][j][2] = bgrBytes[index + 2].toInt() and 0xFF  // Red
            index += 3
        }
    }

    return bgrArray
}


class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    init {
        System.loadLibrary("opencv_java4")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FocusLearnTheme {
                val context = LocalContext.current
                var hasCameraPermission by remember { mutableStateOf(checkCameraPermission(context)) }
                var hasInternetPermission by remember { mutableStateOf(checkInternetPermission(context)) }
                var isButtonClicked  by remember {
                    mutableStateOf(false)
                }
                val cameraPermissionRequest = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestPermission()
                ) { permissions ->
                    hasCameraPermission = permissions
                    Log.d("Permissions", "Camera permission granted: $permissions")
                }
                val internetPermissionRequest = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestPermission()
                ) { permissions ->
                    hasInternetPermission = permissions
                    Log.d("Permissions", "Internet permission granted: $permissions")
                }
                val previewView = remember { PreviewView(context) }
                if (hasCameraPermission&&hasInternetPermission) {
                    Log.d("Permissions", "Camera permission already granted")
//                    AndroidView(
//                        factory = {
//                            previewView
//                        },
//                        modifier = Modifier.fillMaxSize()
//                    )
                    LaunchedEffect(isButtonClicked) {
                        startCamera()
                    }
                } else if (!hasCameraPermission) {
                    Log.d("Permissions", "Requesting camera permission")
                    LaunchedEffect(Unit) {
                        cameraPermissionRequest.launch(Manifest.permission.CAMERA)
                    }
                }
                val bitmap = viewModel.imageBitmap.observeAsState()
                bitmap.value?.let {
                    DisplayImage(it)
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(100.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Button(onClick = {
                        isButtonClicked = !isButtonClicked
                    }) {
                        Text(text = "Start")
                    }
                }
            }
        }
    }

    private fun startCamera() {
        Log.d("CameraX", "Starting camera")
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener(Runnable {
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

//            val preview = Preview.Builder().build().also {
//                it.setSurfaceProvider(previewView.surfaceProvider)
//            }

            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(ContextCompat.getMainExecutor(this)) { imageProxy ->
                        processImageProxy(imageProxy)
                    }
                }

            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, imageAnalyzer
                )
                Log.d("CameraX", "Camera started successfully")
            } catch (exc: Exception) {
                Log.e("CameraX", "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun processImageProxy(imageProxy: ImageProxy) {
        Log.d("ImageAnalysis", "Processing image")
        val gson = Gson()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val bgrBytes = imageToIntArray(imageProxy)
                val jsonString = gson.toJson(bgrBytes)
                postDataToFlaskServer(jsonString)
            } catch (e: Exception) {
                Log.e("ImageAnalysis", "Error processing image", e)
            } finally {
                imageProxy.close()
            }
        }
    }
}

private fun imageToBGRByteArray(imageProxy: ImageProxy): ByteArray {
    val yBuffer = imageProxy.planes[0].buffer
    val uBuffer = imageProxy.planes[1].buffer
    val vBuffer = imageProxy.planes[2].buffer

    val ySize = yBuffer.remaining()
    val uSize = uBuffer.remaining()
    val vSize = vBuffer.remaining()

    Log.d("ySize", "$ySize")
    Log.d("uSize", "$uSize")
    Log.d("vSize", "$vSize")

    val yBytes = ByteArray(ySize)
    val uBytes = ByteArray(uSize)
    val vBytes = ByteArray(vSize)

    yBuffer.get(yBytes)
    uBuffer.get(uBytes, 0, uSize)
    vBuffer.get(vBytes, 0, vSize)

    // NV21 포맷의 데이터를 BGR로 변환
    val nv21Bytes = ByteArray(ySize + uSize + vSize)
    System.arraycopy(yBytes, 0, nv21Bytes, 0, ySize)
    for (i in 0 until uSize) {
        nv21Bytes[ySize + i * 2] = vBytes[i]
        nv21Bytes[ySize + i * 2 + 1] = uBytes[i]
    }

    val yuvImage = Mat(imageProxy.height + imageProxy.height / 2, imageProxy.width, CvType.CV_8UC1)
    yuvImage.put(0, 0, nv21Bytes)

    val bgrImage = Mat()
    Imgproc.cvtColor(yuvImage, bgrImage, Imgproc.COLOR_YUV2BGR_NV21)

    // BGR 이미지를 ByteArray로 변환
    val size = bgrImage.total() * bgrImage.elemSize()
    val bgrBytes = ByteArray(size.toInt())
    bgrImage.get(0, 0, bgrBytes)

    return bgrBytes
}

@Composable
fun DisplayImage(bitmap: Bitmap) {
    Box(modifier = Modifier.fillMaxSize()) {
        Image(bitmap = bitmap.asImageBitmap(), contentDescription = null, modifier = Modifier.fillMaxSize())
    }
}