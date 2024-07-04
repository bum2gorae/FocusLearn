package com.example.focuslearn


import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.example.focuslearn.ui.theme.FocusLearnTheme
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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

class VideoActivity: ComponentActivity()  {
    private val viewModel: MainViewModel by viewModels()

    init {
        System.loadLibrary("opencv_java4")
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FocusLearnTheme {
                LaunchedEffect(Unit) {
                    startCamera()
                }
                VideoPlayer()
                val bitmap = viewModel.imageBitmap.observeAsState()
                bitmap.value?.let {
                    DisplayImage(it)
                }
            }
        }
    }
    private fun startCamera() {
        Log.d("CameraX", "Starting camera")
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener(Runnable {
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

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


@Composable
fun VideoPlayer() {
    val context = LocalContext.current
    val player = remember {
        ExoPlayer.Builder(context).build().apply {
            val mediaItem = MediaItem.fromUri(
                "http://192.168.0.101:3700/uploads/[%EC%A7%81%EC%9E%A5%20%EB%82%B4%20%EA%B4%B4%EB%A1%AD%ED%9E%98%20%EC%98%88%EB%B0%A9%20%EA%B5%90%EC%9C%A1%EC%98%81%EC%83%81]%20%EB%88%84%EA%B5%AC%EB%82%98%20%EC%83%81%EC%B2%98%EB%B0%9B%EC%A7%80%20%EC%95%8A%EB%8A%94%20%EA%B1%B4%EC%A0%84%ED%95%9C%20%EC%A7%81%EC%9E%A5%EB%AC%B8%ED%99%94%20%EA%B0%9C%EC%84%A0%EC%9D%84%20%EC%9C%84%ED%95%B4.mp4"
            )
            setMediaItem(mediaItem)
            prepare()
            playWhenReady = true
        }
    }
    AndroidView(factory = {
        PlayerView(context).apply {
            this.player = player
            useController = true // 기본 컨트롤러 사용
        }
    }, modifier = Modifier.fillMaxSize())


    DisposableEffect(
        key1 = player, // Add this key to handle changes properly
        effect = {
            onDispose {
                player.release()
            }
        }
    )
}


@Composable
fun DisplayImage(bitmap: Bitmap) {
    Box(modifier = Modifier.fillMaxSize()) {
        Image(bitmap = bitmap.asImageBitmap(), contentDescription = null, modifier = Modifier.fillMaxSize())
    }
}


fun postDataToFlaskServer(json: String) {
    val client = OkHttpClient()

    val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
    val requestBody = json.toRequestBody(mediaType)

    val request = Request.Builder()
        .url("http://192.168.0.101:3700/eye_info") // Flask 서버의 엔드포인트 URL
        .post(requestBody)
        .build()
    Log.d("flask", "requestFinish")

    client.newCall(request).execute().use { response ->
        if (!response.isSuccessful) throw IOException("Unexpected code $response")

        println(response.body?.string())
    }
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
