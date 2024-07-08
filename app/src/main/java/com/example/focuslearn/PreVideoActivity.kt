package com.example.focuslearn

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.focuslearn.ui.theme.FocusLearnTheme

class PreVideoScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FocusLearnTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFFF5F5F5)) {
                    VideoScreenContent(
                        title = "안전한 일터를 위한 중대재해처벌법",
                        description = "안전 보건 교육 영상",
                        duration = "34분",
                        deadline = "마감기한 8월 26일",
                        requirements = listOf(
                            "작업장의 안전보건 지식 습득",
                            "대처능력을 키워 산업재해 예방",
                            "근로자의 생명과 신체 안전 도모"
                        )
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoScreenContent(
    title: String,
    description: String,
    duration: String,
    deadline: String,
    requirements: List<String>,
) {
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = R.drawable.focuslearn_background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize()
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = "동영상 페이지",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1F41BB),
                modifier = Modifier.padding(vertical = 16.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(Color.LightGray, RoundedCornerShape(16.dp))
                    .clickable {
                        val intent = Intent(context, GuideScreen::class.java)
                        context.startActivity(intent)
                    }
            ) {
                Image(
                    painter = painterResource(id = R.drawable.thumbnail_1),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = description,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Text(
                text = "$duration | $deadline",
                fontSize = 14.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "교육을 완료하려면 강의 수강 중 60% 이상 집중하고, 시험에서 3문제 중 2문제 이상을 맞춰야 합니다.\n동영상 시청에 집중해 주시기 바랍니다.",
                fontSize = 14.sp,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "안전보건교육 왜 필수적일까요?",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(40.dp))

            requirements.forEach { requirement ->
                RequirementItem(requirement)
            }

            Spacer(modifier = Modifier.height(40.dp))

        }
    }
}

@Composable
fun RequirementItem(requirement: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(Color(0xFFFB6340), RoundedCornerShape(8.dp))
            .padding(16.dp)
    ) {
        Text(
            text = requirement,
            fontSize = 14.sp,
            color = Color.White
        )
    }
}

@Preview(showBackground = true)
@Composable
fun VideoScreenPreview() {
    FocusLearnTheme {
        VideoScreenContent(
            title = "안전한 일터를 위한 중대재해처벌법",
            description = "안전 보건 교육 영상",
            duration = "34분",
            deadline = "마감기한 8월 26일",
            requirements = listOf(
                "작업장의 안전보건 지식 습득",
                "대처능력을 키워 산업재해 예방",
                "근로자의 생명과 신체 안전 도모"
            )
        )
    }
}