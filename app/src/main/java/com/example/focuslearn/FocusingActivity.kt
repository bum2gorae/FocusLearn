package com.example.focuslearn

import com.example.focuslearn.ui.theme.FocusLearnTheme
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

class Focusing : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FocusLearnTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFFE0E0E0)) { // 차분한 배경색
                    EyeRecognitionContent()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EyeRecognitionContent() {
    var position by remember { mutableStateOf(Position.TopRight) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        while (true) {
            delay(5000)
            position = when (position) {
                Position.TopRight -> Position.BottomLeft
                Position.BottomLeft -> {
                    val intent = Intent(context, EyeRecognitionScreen::class.java)
                    context.startActivity(intent)
                    break
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        RedDotWithMessage(position = position)
    }
}

@Composable
fun RedDotWithMessage(position: Position) {
    when (position) {
        Position.TopRight -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(Color.Red, CircleShape)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "빨간 점을 5초 동안 응시해 주세요",
                    fontSize = 18.sp,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Position.BottomLeft -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Bottom
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(Color.Red, CircleShape)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "빨간 점을 5초 동안 응시해 주세요",
                    fontSize = 18.sp,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

enum class Position {
    TopRight, BottomLeft
}

@Preview(showBackground = true)
@Composable
fun EyeRecognitionPreview() {
    FocusLearnTheme {
        EyeRecognitionContent()
    }
}