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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.example.focuslearn.ui.theme.FocusLearnTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc


class MainViewModel : ViewModel() {
    val imageBitmap = MutableLiveData<Bitmap?>()
    val eyeInfoData = MutableLiveData<eyeInfo?>()
}

private fun checkCameraPermission(context: Context): Boolean {
    return ActivityCompat.checkSelfPermission(
        context,
        Manifest.permission.CAMERA
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

data class eyeInfo(
    val eyes: List<Pair<Int, Int>>,
    val irises: List<Pair<Int, Int>>
)

private fun jsonToEyeInfo(json: String): eyeInfo {
    val jsonObject = JSONObject(json)
    val eyesArray = jsonObject.getJSONArray("eyes")
    val irisesArray = jsonObject.getJSONArray("irises")

    val eyesList = mutableListOf<Pair<Int, Int>>()
    val irisesList = mutableListOf<Pair<Int, Int>>()

    for (i in 0 until eyesArray.length()) {
        val eyeObject = eyesArray.getJSONObject(i)
        val xloc = eyeObject.getInt("xloc")
        val yloc = eyeObject.getInt("yloc")
        eyesList.add(Pair(xloc, yloc))
    }

    for (i in 0 until irisesArray.length()) {
        val irisObject = irisesArray.getJSONObject(i)
        val xloc = irisObject.getInt("xloc")
        val yloc = irisObject.getInt("yloc")
        irisesList.add(Pair(xloc, yloc))
    }

    return eyeInfo(eyes = eyesList, irises = irisesList)
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
                var hasPermission by remember { mutableStateOf(checkCameraPermission(context)) }
                val cameraPermissionRequest = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestPermission()
                ) { permissions ->
                    hasPermission = permissions
                    Log.d("Permissions", "Camera permission granted: $permissions")
                }
                val previewView = remember { PreviewView(context) }
                if (hasPermission) {
                    Log.d("Permissions", "Camera permission already granted")
                    AndroidView(
                        factory = {
                            previewView
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                    LaunchedEffect(previewView) {
                        startCamera(previewView, hasPermission)
                    }
                } else {
                    Log.d("Permissions", "Requesting camera permission")
                    LaunchedEffect(Unit) {
                        cameraPermissionRequest.launch(Manifest.permission.CAMERA)
                    }
                }
                val eyeInfo by viewModel.eyeInfoData.observeAsState()
                eyeInfo?.let {
                    if (it.eyes.isNotEmpty()) {
                        Log.d("eyeInfo", it.eyes[0].toString())
                    } else {
                        Log.d("eyeInfo", "No eye data available")
                    }
                }
            }
        }
    }

    private fun startCamera(previewView: PreviewView, hasPermission: Boolean) {
        Log.d("CameraX", "Starting camera")
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener(Runnable {
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(ContextCompat.getMainExecutor(this)) { imageProxy ->
                        CoroutineScope(Dispatchers.Default).launch {
                            processImageProxy(imageProxy, hasPermission)
                        }
                    }
                }

            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageAnalyzer
                )
                Log.d("CameraX", "Camera started successfully")
            } catch (exc: Exception) {
                Log.e("CameraX", "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private suspend fun processImageProxy(imageProxy: ImageProxy, hasPermission: Boolean) {
        Log.d("ImageAnalysis", "Processing image")
        try {
//            val bgrBytes = imageToNV21ByteArray(imageProxy)
            val nv21Bytes = withContext(Dispatchers.IO) {
                imageToBGRByteArray(imageProxy)
            }

            if (!Python.isStarted()) {
                Log.d("Python", "Starting Python interpreter")
                Python.start(AndroidPlatform(this))
            }

            Log.d("width", "${imageProxy.width}")
            Log.d("height", "${imageProxy.height}")

            val py = Python.getInstance()
            val pyModule = py.getModule("eyetracking_android")

            try {
                Log.d("Python", "Calling predict_img with hasPermission: $hasPermission")
                val resultJson: String = withContext(Dispatchers.IO) {
                    pyModule.callAttr(
                        "predict_img", nv21Bytes, imageProxy.width, imageProxy.height
//                    "predict_img", bgrBytes
                    ).toJava(String::class.java)
                }
                Log.d("Python", "predict_img called successfully")

                val eyeInfoObject = jsonToEyeInfo(resultJson)
                withContext(Dispatchers.Main) {
                    viewModel.eyeInfoData.postValue(eyeInfoObject)
                }
                Log.d("Python", "Eye info extracted successfully")

            } catch (e: Exception) {
                Log.e("Python", "Error calling Python function", e)
                Log.e("Python", "Exception message: ${e.message}")
                Log.e("Python", "Exception cause: ${e.cause}")
            } finally {
                imageProxy.close()
            }
        } catch (e: Exception) {
            Log.e("ImageAnalysis", "Error processing image", e)
            imageProxy.close()
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

