package com.example.focuslearn

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
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

class Conditions : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FocusLearnTheme {
                MainScreen()
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun MainScreen() {
        val scrollState = rememberScrollState()
        var allChecked by remember { mutableStateOf(false) }
        var term1Checked by remember { mutableStateOf(false) }
        var term2Checked by remember { mutableStateOf(false) }
        var term3Checked by remember { mutableStateOf(false) }
        var term4Checked by remember { mutableStateOf(false) }
        var showDialog by remember { mutableStateOf(false) }

        LaunchedEffect(term1Checked, term2Checked, term3Checked, term4Checked) {
            allChecked = term1Checked && term2Checked && term3Checked && term4Checked
        }

        fun onAllCheckedChange(checked: Boolean) {
            allChecked = checked
            term1Checked = checked
            term2Checked = checked
            term3Checked = checked
            term4Checked = checked
        }

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
                Modifier
                    .fillMaxSize()
                    .padding(12.dp)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Spacer(modifier = Modifier.height(100.dp))
                    Text(
                        text = "약관 동의",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1F41BB),
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                    Spacer(modifier = Modifier.height(60.dp))
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            Checkbox(
                                checked = allChecked,
                                onCheckedChange = { onAllCheckedChange(it) }
                            )
                            Text(text = "약관 전체 동의")
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            Checkbox(
                                checked = term1Checked,
                                onCheckedChange = { checked ->
                                    term1Checked = checked
                                    if (!checked) allChecked = false
                                }
                            )
                            Text(text = "이용 약관 동의 (필수)")
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            Checkbox(
                                checked = term2Checked,
                                onCheckedChange = { checked ->
                                    term2Checked = checked
                                    if (!checked) allChecked = false
                                }
                            )
                            Text(text = "개인정보 수집 및 이용 동의 (필수)")
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            Checkbox(
                                checked = term3Checked,
                                onCheckedChange = { checked ->
                                    term3Checked = checked
                                    if (!checked) allChecked = false
                                }
                            )
                            Text(text = "카메라 접근 및 사용자 동의 (필수)")
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            Checkbox(
                                checked = term4Checked,
                                onCheckedChange = { checked ->
                                    term4Checked = checked
                                    if (!checked) allChecked = false
                                }
                            )
                            Text(
                                text = "푸시 알림 수신 동의 (선택)",
                                color = Color.Red
                            )
                        }
                    }
                }
                //테스트
                val context = LocalContext.current
                Button(
                    onClick = {
                        if (term1Checked && term2Checked && term3Checked) {
                            val intent = Intent(context, EducationVideoScreen::class.java)
                            context.startActivity(intent)
                        } else {
                            showDialog = true
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
                ) {
                    Text(text = "동의하고 계속하기", color = Color.White)
                }

                if (showDialog) {
                    AlertDialog(
                        onDismissRequest = { showDialog = false },
                        title = { Text(text = "필수 동의 체크해주세요.") },
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
            }
        }
    }

    @Preview(showBackground = true)
    @Composable
    fun GreetingPreview() {
        FocusLearnTheme {
            MainScreen()
        }
    }
}