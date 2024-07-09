package com.example.focuslearn

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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


class CompletionScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize context and shared preferences
        val context = this // Use the Activity context
        val sharedPreferences = context.getSharedPreferences("FocusLearnPreference", Context.MODE_PRIVATE)

        // Set certificationStatus to "미수료" on app start
        with(sharedPreferences.edit()) {
            putString("certificationStatus", "미수료")
            apply()
        }

        setContent {
            FocusLearnTheme {
                val result = intent.getBooleanExtra("result", false)
                val totConcentrateAvg = sharedPreferences.getFloat("totalAvg", 0.00f) * 100

                // Save certification status to SharedPreferences
                val certificationStatus = if (result) "수료" else "미수료"
                with(sharedPreferences.edit()) {
                    putString("certificationStatus", certificationStatus)
                    apply()
                }

                Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFFF5F5F5)) {
                    CompletionScreenContent(
                        completionDate = "2024-07-10",
                        progressRate = "100%",
                        concentrationRate = "${totConcentrateAvg.toInt()}%",
                        certificationStatus = certificationStatus,
                        intent
                    )
                }
            }
        }
    }
}




@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompletionScreenContent(
    completionDate: String,
    progressRate: String,
    concentrationRate: String,
    certificationStatus: String,
    intent1: Intent
) {
    val context = LocalContext.current
    val userID = intent1.getStringExtra("userID")
    val companyCode = intent1.getStringExtra("companyCode")
    val userName = intent1.getStringExtra("userName")

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
                text = "수강 완료",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1F41BB)
            )

            Spacer(modifier = Modifier.height(150.dp))

            Text(
                text = "축하합니다! 모든 교육 동영상을 성공적으로 수강하셨습니다!",
                fontSize = 16.sp,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(20.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Text(text = "• 수강 완료 날짜: [$completionDate]", fontSize = 14.sp, color = Color.Black)
                Text(text = "• 수료증은 이메일로 발송되었습니다.", fontSize = 14.sp, color = Color.Black)
                Text(
                    text = "• 다음 교육 일정은 [10월]입니다. 잊지 말고 참여해 주세요!",
                    fontSize = 14.sp,
                    color = Color.Black
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "산업 재해 방지를 위한 중요한 교육에 참여해 주셔서 감사합니다.\n앞으로도 안전한 근무 환경을 유지하는 데 기여해 주시길 바랍니다.",
                fontSize = 14.sp,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(80.dp))

            InfoBox(label = "진도율", value = progressRate)
            Spacer(modifier = Modifier.height(20.dp))
            InfoBox(label = "집중도", value = concentrationRate)
            Spacer(modifier = Modifier.height(20.dp))
            InfoBox(label = "수료 여부", value = certificationStatus)

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            )  {
                Button(onClick = {
                    val intent = Intent(context, EducationVideoScreen::class.java)
                    intent.putExtra("userID", userID)
                    intent.putExtra("companyCode", companyCode)
                    intent.putExtra("userName", userName)
                    context.startActivity(intent)
                }) {
                    Text(text = "홈으로")
                }
                Spacer(modifier = Modifier.width(20.dp))
                Button(onClick = {
                    shareViaEmail(context, completionDate, progressRate, concentrationRate, certificationStatus)
                }) {
                    Text(text = "이메일로 공유하기")
                }
            }
        }
    }
}

fun shareViaEmail(context: android.content.Context, completionDate: String, progressRate: String, concentrationRate: String, certificationStatus: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "message/rfc822"
        putExtra(Intent.EXTRA_SUBJECT, "교육 수료 결과")
        putExtra(Intent.EXTRA_TEXT, """
            수강 완료 날짜: $completionDate
            진도율: $progressRate
            집중도: $concentrationRate
            수료 여부: $certificationStatus
        """.trimIndent())
    }
    context.startActivity(Intent.createChooser(intent, "이메일로 공유하기"))
}

@Composable
fun InfoBox(label: String, value: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .background(Color.White, RoundedCornerShape(8.dp))
            .border(width = 1.dp, color = Color.Black, shape = RoundedCornerShape(8.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, fontSize = 16.sp, color = Color.Black)
            Text(
                text = value,
                fontSize = 16.sp,
                color = if (label == "수료 여부") Color(0xFF1F41BB) else Color.Black
            )
        }
    }
}

