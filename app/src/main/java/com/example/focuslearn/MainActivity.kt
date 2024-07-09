package com.example.focuslearn

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import com.example.focuslearn.ui.theme.FocusLearnTheme
import com.google.common.reflect.TypeToken
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.gson.Gson
import java.io.Serializable


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

class MainActivity : ComponentActivity() {
    init {
        System.loadLibrary("opencv_java4")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FocusLearnTheme {
                val context = LocalContext.current
                val contextAct = LocalContext.current as Activity?
                var hasCameraPermission by remember { mutableStateOf(checkCameraPermission(context)) }
                val hasInternetPermission by remember {
                    mutableStateOf(
                        checkInternetPermission(
                            context
                        )
                    )
                }
                val cameraPermissionRequest = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestPermission()
                ) { permissions ->
                    hasCameraPermission = permissions
                    Log.d("Permissions", "Camera permission granted: $permissions")
                }
                if (hasCameraPermission && hasInternetPermission) {
                    Log.d("Permissions", "Camera permission already granted")
                } else if (!hasCameraPermission) {
                    Log.d("Permissions", "Requesting camera permission")
                    LaunchedEffect(Unit) {
                        cameraPermissionRequest.launch(Manifest.permission.CAMERA)
                    }
                }
                val sharedPreferences: SharedPreferences =
                    getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)


                Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFFF5F5F5)) {
                    LoginScreen(context, sharedPreferences)
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(context: Context, sharedPreferences: SharedPreferences) {
    var id by remember { mutableStateOf("") }
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
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(120.dp))

            Text(
                text = "필수 이수 교육 통합 플랫폼",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Text(
                text = "FOCUS LEARN",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 16.dp),
                color = Color(0xFF1F41BB)
            )

            Spacer(modifier = Modifier.height(100.dp))

            OutlinedTextField(
                value = id,
                onValueChange = { id = it },
                label = { Text("ID 입력") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 40.dp),
                shape = RoundedCornerShape(8.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    containerColor = Color(0xFFF5F8FF),
                    focusedBorderColor = Color(0xFF3F51B5),
                    unfocusedBorderColor = Color(0xFF3F51B5)
                )
            )

            Spacer(modifier = Modifier.height(20.dp))


            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 40.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = "ID는 담당직원에게 문의하세요.",
                    fontSize = 12.sp,
                    color = Color(0xFF1F41BB)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))


            Button(
                onClick = {
                    val fireDB = Firebase.firestore
                    fireDB.collection("Company").get()
                        .addOnSuccessListener { companyDocs ->
                            Log.d("onSuccess", "success")
                            for (companyDoc in companyDocs) {
                                val companyCode = companyDoc.id
                                fireDB.collection("Company").document(companyCode)
                                    .collection("Employee")
                                    .whereEqualTo("ID", id).get().addOnSuccessListener { Docs ->
                                        for (Doc in Docs) {
                                            val intent = Intent(context, Conditions::class.java)
                                            intent.putExtra("userID", id)
                                            intent.putExtra("companyCode", companyCode)
                                            intent.putExtra("userName", Doc.id)
                                            val lectureCodeMap = Doc["LectureCode"] as Map<String?, Boolean?>
                                            val lectureStatusMap = Doc["LectureStatus"] as Map<String?, Boolean?>
                                            val lectureCodeList = booleanArrayOf(false, false, false, false)
                                            val lectureStatusList = booleanArrayOf(false, false, false, false)
                                            for (i in 0..3) {
                                                lectureCodeList[i] = lectureCodeMap[lectureList[i]] as Boolean
                                            }
                                            intent.putExtra("lectureCode", lectureCodeList)
                                            for (i in 0..3) {
                                                lectureStatusList[i] = lectureStatusMap[lectureList[i]] as Boolean
                                            }
                                            intent.putExtra("lectureStatus", lectureCodeList)
                                            Log.d("Login data check", "$id, $companyCode, ${Doc.id}, ${lectureCodeMap["산업안전법"]}, ${lectureStatusMap["산업안전법"]}")
                                            context.startActivity(intent)
                                        }
                                    }

                            }
                        }




                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 40.dp)
                    .height(56.dp)
                    .shadow(8.dp, RoundedCornerShape(8.dp)),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1F41BB))
            ) {
                Text(text = "로그인", color = Color.White)
            }

            Spacer(modifier = Modifier.height(250.dp))

            Text(
                text = "© Intel",
                fontSize = 12.sp,
                color = Color(0xFF1F41BB)
            )
        }
    }
}

val lectureList = listOf(
    "개인정보보호",
    "산업안전법",
    "장애인인식개선",
    "직장내성희롱"
)
