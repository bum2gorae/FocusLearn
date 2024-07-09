package com.example.focuslearn

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.focuslearn.ui.theme.FocusLearnTheme

class EyeRecognitionScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FocusLearnTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFFF5F5F5)) {
                    EyeRecognitionScreenContent(intent)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EyeRecognitionScreenContent(intent1: Intent) {
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        val userID = intent1.getStringExtra("userID")
        val companyCode = intent1.getStringExtra("companyCode")
        val userName = intent1.getStringExtra("userName")
        val lectureName = intent1.getStringExtra("lectureName")
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
            Spacer(modifier = Modifier.height(250.dp))

            Text(
                text = "눈 인식 설정이 완료되었습니다!",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "이제 교육을 시작할 수 있습니다.",
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(50.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(12.dp))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column {
                    Text(
                        text = "교육 중 집중도 측정을 위해 눈 인식이 지속적으로 수행됩니다. 교육에 집중해 주세요.",
                        fontSize = 16.sp,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "알림: 교육 후에 3문제 중 2문제 이상을 맞춰야 통과할 수 있습니다. 집중해서 시청해 주세요.",
                        fontSize = 16.sp,
                        color = Color.Black
                    )
                }
            }




            //테스트
            val context = LocalContext.current
            Button(
                onClick = {
                    val intent = Intent(context, VideoActivity::class.java)
                    intent.putExtra("userID", userID)
                    intent.putExtra("companyCode", companyCode)
                    intent.putExtra("userName", userName)
                    intent.putExtra("lectureName", lectureName)
                    context.startActivity(intent)
                },
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .height(40.dp),
                shape = RoundedCornerShape(4.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1F41BB))
            ) {
                Text(
                    text = "강의 시작",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }



        }
    }
}
