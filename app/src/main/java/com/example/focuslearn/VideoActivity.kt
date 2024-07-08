package com.example.focuslearn


import android.content.Context
import android.content.Intent
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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.example.focuslearn.ui.theme.FocusLearnTheme
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
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
    val responseLiveData = MutableLiveData<String>()  // 서버 응답을 저장할 LiveData

    private val _dataList = MutableLiveData<MutableMap<Int, Any?>>(mutableMapOf())
    val dataList: LiveData<MutableMap<Int, Any?>> get() = _dataList

    private val viewModelJob = Job()
    private val viewModelScope = CoroutineScope(Dispatchers.Main + viewModelJob)
    private var isPaused = false
    private var currentImageProxy: ImageProxy? = null  // 현재 처리 중인 ImageProxy를 저장


    fun addDataToList(requestId: Int, data: Any?) {
        _dataList.value?.let {
            it[requestId] = data
            _dataList.postValue(it)
        }
    }

    fun processImageProxy(imageProxy: ImageProxy) {
        Log.d("ImageAnalysis", "Processing image")
        val gson = Gson()

        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (isPaused) {
                    Log.d("ImageAnalysis", "Paused, waiting...")
                    kotlinx.coroutines.delay(Long.MAX_VALUE)
                }
                val bgrBytes = imageToIntArray(imageProxy)
                val jsonString = gson.toJson(bgrBytes)
                postDataToFlaskServer(jsonString, this@MainViewModel)
            } catch (e: Exception) {
                Log.e("ImageAnalysis", "Error processing image", e)
            } finally {
                imageProxy.close()
                currentImageProxy = null
            }
        }
    }

    fun pauseProcessing() {
        isPaused = true
    }

    fun resumeProcessing() {
        isPaused = false
        currentImageProxy?.let {
            processImageProxy(it)
        }
    }

    public override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    fun calculateAverageOfLastTenValues(): Double {
        val dataSnapshot = _dataList.value?.toList() ?: emptyList()
        val size = dataSnapshot.size
        val sublist = if (size > 10) dataSnapshot.subList(size - 10, size) else dataSnapshot

        val values = sublist.map { it.second as? Double ?: 0.0 }
        return if (values.isNotEmpty()) values.average() else 0.0
    }

    private val firestore = Firebase.firestore

    fun saveDataToFirestore(
        id: String,
        requestId: Int,
        result: Any?,
        concentrateNow: Int,
        label: String
    ) {
        val safeResult = result ?: 0 // result가 null이면 0으로 설정

        val data = hashMapOf(
            "result" to safeResult,
            "concentrateNow" to concentrateNow
        )

        firestore.collection("Company")
            .document("#0001")
            .collection("Employee")
            .whereEqualTo("ID", id)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    firestore.collection("Company")
                        .document("#0001")
                        .collection("Employee")
                        .document(document.id)
                        .collection(label)
                        .document(requestId.toString())
                        .set(data)
                        .addOnSuccessListener {
                            Log.d("Firestore", "DocumentSnapshot successfully written!")
                        }
                        .addOnFailureListener { e ->
                            Log.w("Firestore", "Error writing document", e)
                        }
                }
            }
            .addOnFailureListener { exception ->
                Log.w("Firestore", "Error getting documents: ", exception)
            }
    }

    fun fetchResultsFromFirestore(id: String, label: String, onComplete: (Double) -> Unit) {
        firestore.collection("Company")
            .document("#0001")
            .collection("Employee")
            .whereEqualTo("ID", id)
            .get()
            .addOnSuccessListener { documents ->
                var totalResult = 0.0
                var count = 0

                for (document in documents) {
                    val employeeDocId = document.id
                    firestore.collection("Company")
                        .document("#0001")
                        .collection("Employee")
                        .document(employeeDocId)
                        .collection(label)
                        .get()
                        .addOnSuccessListener { results ->
                            for (result in results) {
                                val resultValue = result.getDouble("result") ?: 0.0
                                totalResult += resultValue
                                count++
                            }
                            if (count > 0) {
                                val totalAvg = totalResult / count
                                onComplete(totalAvg)
                            } else {
                                onComplete(0.0)
                            }
                        }
                        .addOnFailureListener { e ->
                            Log.w("Firestore", "Error getting results: ", e)
                            onComplete(0.0)
                        }
                }
            }
            .addOnFailureListener { exception ->
                Log.w("Firestore", "Error getting documents: ", exception)
                onComplete(0.0)
            }
    }
}



class VideoActivity : ComponentActivity() {
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
                val ID = "ss01"
                val label = "직장내성희롱"
                var concentrateNow by remember {
                    mutableIntStateOf(0)
                }
                val response = viewModel.responseLiveData.observeAsState()
                val dataListState = viewModel.dataList.observeAsState()
                val dataList: List<Map<Int, Double?>> =
                    dataListState.value?.values?.mapNotNull { it as? Map<Int, Double?> }
                        ?: emptyList()
                val average = viewModel.calculateAverageOfLastTenValues() * 100
                Log.d("avgTest", average.toString())
                concentrateNow = average.toInt()

                VideoPlayer(onPlaybackEnded = {
                    // 재생이 끝나면 MainActivity로 이동
                    val intent = Intent(this@VideoActivity, TestStartScreen::class.java)
                    startActivity(intent)
                    finish()
                },
                    onPlaybackPaused = {
                        viewModel.pauseProcessing()
                    },
                    onPlaybackResumed = {
                        viewModel.resumeProcessing()
                    })
                Box(
                    modifier = Modifier
                        .size(150.dp)
                        .padding(30.dp)
                ) {
                    Text(
                        text = "$concentrateNow%",
                        color = Color.Red
                    )

                    dataListState.value?.let {
//                            DisplayDataList(it)
                    }
                    response.value?.let {
//                            // 서버 응답 처리 로직 추가
//                            Log.d("ServerResponse", it)
//                            // 예시: 응답 내용을 화면에 표시하는 Composable 함수 호출
//                            DisplayServerResponse(it)
                    }
                }

                val bitmap = viewModel.imageBitmap.observeAsState()
                bitmap.value?.let {
                    DisplayImage(it)
                }


//


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
                        viewModel.processImageProxy(imageProxy)
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

    override fun onDestroy() {
        super.onDestroy()
        val sharedPreferences = getSharedPreferences("FocusLearnPreference", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        viewModel.fetchResultsFromFirestore("ss01", "직장내성희롱") { totalAvg ->
            editor.putFloat("totalAvg", totalAvg.toFloat())
            editor.apply()
            Log.d("SharedPreferences", "totalAvg saved: $totalAvg")
        }

        viewModel.onCleared() // This will cancel all coroutines in the ViewModel
    }
}


data class ServerResponse(
    val result: Any?,
    val request_id: Int
)

@Composable
fun DisplayServerResponse(response: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(30.dp)
    ) {
        Text(text = response)
    }
}

@Composable
fun VideoPlayer(
    onPlaybackEnded: () -> Unit,
    onPlaybackPaused: () -> Unit,
    onPlaybackResumed: () -> Unit
) {
    val context = LocalContext.current
    val player = remember {
        ExoPlayer.Builder(context).build().apply {
            val mediaItem = MediaItem.fromUri(
                "http://192.168.0.101:3700/uploads/Workplace_harassment_1.mp4"
//                "http://192.168.45.55:3700/uploads/Workplace_harassment_1.mp4"

            )
            setMediaItem(mediaItem)
            prepare()
            playWhenReady = true

            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == Player.STATE_ENDED) {
                        onPlaybackEnded()
                    } else if (playbackState == Player.STATE_READY && !playWhenReady) {
                        onPlaybackPaused()
                    } else if (playbackState == Player.STATE_READY && playWhenReady) {
                        onPlaybackResumed()
                    }
                }
            })
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
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = null,
            modifier = Modifier.fillMaxSize()
        )
    }
}


fun postDataToFlaskServer(json: String, viewModel: MainViewModel) {
    val client = OkHttpClient()

    val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
    val requestBody = json.toRequestBody(mediaType)

    val request = Request.Builder()
        // Flask 서버의 엔드포인트 URL
        .url("http://192.168.0.101:3700/test")
//        .url("http://192.168.45.79:5000/test")
        .post(requestBody)
        .build()
    Log.d("flask", "requestFinish")

    CoroutineScope(Dispatchers.IO).launch {
        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IOException("Unexpected code $response")
                val responseBody = response.body?.string()
                Log.d("Response", responseBody ?: "No response body")

                responseBody?.let {
                    val serverResponse = Gson().fromJson(it, ServerResponse::class.java)
                    viewModel.addDataToList(serverResponse.request_id, serverResponse.result)
                    viewModel.responseLiveData.postValue("Request ID: ${serverResponse.request_id}, Data: ${serverResponse.result}")

                    // Firestore에 데이터 저장
                    val id = "ss01"  // ID는 예시로 사용된 값, 실제 값으로 변경 필요
                    val label = "직장내성희롱"
                    val concentrateNow = viewModel.calculateAverageOfLastTenValues().toInt()
                    viewModel.saveDataToFirestore(
                        id,
                        serverResponse.request_id,
                        serverResponse.result,
                        concentrateNow,
                        label
                    )
                }
            }
        } catch (e: Exception) {
            Log.e("HTTP Request", "Error in postDataToFlaskServer", e)
        }
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

