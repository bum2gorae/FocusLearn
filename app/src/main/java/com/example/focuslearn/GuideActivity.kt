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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.focuslearn.ui.theme.FocusLearnTheme

class GuideScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FocusLearnTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFFF5F5F5)) {
                    GuideScreenContent()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuideScreenContent() {
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
            Spacer(modifier = Modifier.height(120.dp))

            Text(
                text = "사용 안내",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1F41BB)
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
                    GuideItem(number = 1, text = "카메라 관련 허용: 눈 인식을 위해 카메라 접근 권한을 허용해주세요.")
                    GuideItem(
                        number = 2,
                        text = "가이드 따라 하기: 화면에 나타나는 지시에 따라 시설을 이동해 주세요. \nex) \"왼쪽 상단 모서리를 보세요.\""
                    )
                    GuideItem(
                        number = 3,
                        text = "학습 시작: 모든 설정이 완료되면 교육 자료를 시청하면 됩니다. 눈 인식을 통해 학습 집중도를 높일 수 있습니다."
                    )
                }
            }

            Spacer(modifier = Modifier.height(50.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFFFE6E6), RoundedCornerShape(12.dp))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column {
                    Text(
                        text = "주의 사항",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Red
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "녹화된 영상은 일시적으로 저장되며, 학습 집중도와 관련된 문의가 있을 때만 활용됩니다. 고객님의 개인정보는 철저히 보호되며, 사용 목적 외에는 절대 사용되지 않습니다.",
                        fontSize = 14.sp,
                        color = Color.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(50.dp))

            // "확인 완료" 버튼
            val context = LocalContext.current
            Button(
                onClick = {
                    val intent = Intent(context, Focusing::class.java)
                    context.startActivity(intent)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1F41BB),
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "확인 완료",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}


@Composable
fun GuideItem(number: Int, text: String) {
    Row(
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        Text(
            text = "$number.",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            fontSize = 14.sp,
            color = Color.Black
        )
    }
}

@Preview(showBackground = true)
@Composable
fun GuideScreenPreview() {
    FocusLearnTheme {
        GuideScreenContent()
    }
}
