package com.example.focuslearn

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.focuslearn.ui.theme.FocusLearnTheme
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

class EducationVideoScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FocusLearnTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFFF5F5F5)) {
                    var refreshTrigger by remember { mutableStateOf(false) }
                    val userID = intent.getStringExtra("userID")
                    val companyCode = intent.getStringExtra("companyCode")
                    val userName = intent.getStringExtra("userName")
                    val lectureCodeMutable = remember { mutableListOf<Boolean>() }
                    val lectureStatusMutable = remember { mutableListOf<Boolean>() }

                    val fireDB = Firebase.firestore
                    fireDB.collection("Company").document(companyCode.toString())
                        .collection("Employee")
                        .document(userName.toString()).get().addOnSuccessListener { Docs ->
                            val lectureCodeMap = Docs["LectureCode"] as Map<String?, Boolean?>
                            val lectureStatusMap = Docs["LectureStatus"] as Map<String?, Boolean?>
                            for (i in 0..3) {
                                lectureCodeMutable.add(lectureCodeMap[lectureList[i]] as Boolean)
                            }
                            for (i in 0..3) {
                                lectureStatusMutable.add(lectureStatusMap[lectureList[i]] as Boolean)
                            }
                            refreshTrigger = true
                        }
                    if (refreshTrigger) {
                        EducationVideoScreenContent(
                            userID.toString(),
                            companyCode.toString(),
                            userName.toString(),
                            lectureCodeMutable,
                            lectureStatusMutable
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EducationVideoScreenContent(userID : String,
                                companyCode : String,
                                userName : String,
                                lectureCodeMutable : MutableList<Boolean>,
                                lectureStatusMutable : MutableList<Boolean>) {
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }


    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(text = "법적 필수 교육이란?") },
            text = { Text(text = "5인 이상 사업장의 모든 근로자가\n매 반기 6~12시간 반드시 이수해야 하는 법정의무교육 입니다.\n\n“산업안전보건법” 제 175조에 따라 \n매 반기 이수하지 않을 시 최대 500만 이하의 과태료가 부과됩니다.") },
            confirmButton = {
                Button(
                    onClick = { showDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1F41BB))
                ) {
                    Text("확인")
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {

        Image(
            painter = painterResource(id = R.drawable.focuslearn_background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(150.dp))

            Text(
                text = "교육 영상",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1F41BB)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "안녕하세요 ${userName}님,\n오늘 수강하실 법적 의무 교육 안전 보건 교육 영상입니다.",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                textAlign = TextAlign.Center
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "법적필수 교육이란?",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.clickable { showDialog = true }
                )
            }

            Spacer(modifier = Modifier.height(30.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
            ) {

                val intent = Intent(context, PreVideoScreen::class.java)
                intent.putExtra("userID", userID)
                intent.putExtra("companyCode", companyCode)
                intent.putExtra("userName", userName)
                Log.d("size", lectureCodeMutable.size.toString())


                if (lectureStatusMutable.size>0 && lectureCodeMutable[0]) {
                    VideoListItem(
                        title = "개인정보보호",
                        imageRes = R.drawable.thumbnail_1,
                        certificationStatus = lectureStatusMutable[0],
                        onClick = {
                            OkHttpClientInstance.deleteData { response, exception ->
                                if (exception != null) {
                                    // 요청 실패 처리
                                    Log.d("test", "1")
                                    exception.printStackTrace()
                                } else if (response != null && response.isSuccessful) {
                                    // DELETE 요청이 성공하면 VideoActivity로 이동
                                    Log.d("test", "2")
                                    context.startActivity(intent)
                                } else {
                                    Log.d("test", "3")
                                    // 요청 실패 처리
                                }
                            }
                        },
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
                if (lectureStatusMutable.size>0 && lectureCodeMutable[1]) {
                    VideoListItem(
                        title = "산업안전법",
                        imageRes = R.drawable.thumbnail_2,
                        certificationStatus = lectureStatusMutable[1],
                        onClick = {
                            OkHttpClientInstance.deleteData { response, exception ->
                                if (exception != null) {
                                    // 요청 실패 처리
                                    Log.d("test", "1")
                                    exception.printStackTrace()
                                } else if (response != null && response.isSuccessful) {
                                    // DELETE 요청이 성공하면 VideoActivity로 이동
                                    Log.d("test", "2")
                                    context.startActivity(intent)
                                } else {
                                    Log.d("test", "3")
                                    // 요청 실패 처리
                                }
                            }
                        },
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
                if (lectureStatusMutable.size>0 && lectureCodeMutable[2]) {
                    VideoListItem(
                        title = "장애인인식개선",
                        imageRes = R.drawable.thumbnail_2,
                        certificationStatus = lectureStatusMutable[2],
                        onClick = {
                            OkHttpClientInstance.deleteData { response, exception ->
                                if (exception != null) {
                                    // 요청 실패 처리
                                    Log.d("test", "1")
                                    exception.printStackTrace()
                                } else if (response != null && response.isSuccessful) {
                                    // DELETE 요청이 성공하면 VideoActivity로 이동
                                    Log.d("test", "2")
                                    context.startActivity(intent)
                                } else {
                                    Log.d("test", "3")
                                    // 요청 실패 처리
                                }
                            }
                        },
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
                if (lectureStatusMutable.size>0 && lectureCodeMutable[3]) {
                    VideoListItem(
                        title = "직장내성희롱",
                        imageRes = R.drawable.thumbnail_1,
                        certificationStatus = lectureStatusMutable[3],
                        onClick = {
                            OkHttpClientInstance.deleteData { response, exception ->
                                if (exception != null) {
                                    // 요청 실패 처리
                                    Log.d("test", "1")
                                    exception.printStackTrace()
                                } else if (response != null && response.isSuccessful) {
                                    // DELETE 요청이 성공하면 VideoActivity로 이동
                                    Log.d("test", "2")
                                    intent.putExtra("lectureName", "직장내성희롱")
                                    context.startActivity(intent)
                                } else {
                                    Log.d("test", "3")
                                    // 요청 실패 처리
                                }
                            }
                        },
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}


@Composable
fun VideoListItem(title: String, imageRes: Int, certificationStatus: Boolean, onClick: () -> Unit) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(8.dp))
            .padding(16.dp)
            .clickable(onClick = onClick, enabled = !certificationStatus)
    ) {
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = null,
            modifier = Modifier
                .size(100.dp)
                .clip(RoundedCornerShape(8.dp))
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            fontSize = 16.sp,
            color = Color.Black,
            modifier = Modifier.align(Alignment.CenterVertically)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = if (certificationStatus) "수료" else "미수료",
            fontSize = 12.sp,
            color = if (certificationStatus) {
                Color(0xFF1F41BB) // Blue color for "수료"
            } else {Color.Red // Default color
            },
            modifier = Modifier.align(Alignment.CenterVertically)
        )
    }
}

object OkHttpClientInstance {
    val client = OkHttpClient()

    fun deleteData(callback: (Response?, IOException?) -> Unit) {
        val request = Request.Builder()
            .url("http://192.168.0.101:3700/test") // Flask 서버 주소와 포트로 변경
            .delete()
            .build()

        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                Log.e("OkHttpClientInstance", "DELETE request failed", e)
                callback(null, e)
            }

            override fun onResponse(call: okhttp3.Call, response: Response) {
                if (response.isSuccessful) {
                    Log.d("OkHttpClientInstance", "DELETE request successful")
                } else {
                    Log.e("OkHttpClientInstance", "DELETE request failed: ${response.code}")
                }
                callback(response, null)
            }
        })
    }
}
