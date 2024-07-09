package com.example.focuslearn

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.focuslearn.questionset.Question
import com.example.focuslearn.questionset.QuestionData
import com.example.focuslearn.ui.theme.FocusLearnTheme
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

class Quiz : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FocusLearnTheme {
                AppContent(intent)
            }
        }
    }
}

@Composable
fun AppContent(intent: Intent) {
    var showResult by remember { mutableStateOf(false) }
    var score by remember { mutableStateOf(0) }

    if (showResult) {
        ResultScreen(score = score, totalQuestions = QuestionData.getQuestions().size,
            intent)
    } else {
        QuizScreen(
            questions = QuestionData.getQuestions(),
            onQuizCompleted = { finalScore ->
                score = finalScore
                showResult = true
            }
        )
    }
}

@Composable
fun QuizScreen(
    questions: List<Question>,
    onQuizCompleted: (Int) -> Unit,
) {
    var currentPosition by remember { mutableStateOf(0) }
    var selectedOption by remember { mutableStateOf(0) }
    var showDialog by remember { mutableStateOf(false) }
    var dialogMessage by remember { mutableStateOf("") }
    var score by remember { mutableStateOf(0) }
    val currentQuestion = questions[currentPosition]
    val context = LocalContext.current
    val scrollState = rememberScrollState()


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(60.dp))

        Text(
            text = "총 ${questions.size}문제 중 ${currentPosition + 1}번째 문제",
            fontSize = 20.sp
        )

        Spacer(modifier = Modifier.height(60.dp))

        LinearProgressIndicator(
            progress = (currentPosition + 1) / questions.size.toFloat(),
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
        )

        Spacer(modifier = Modifier.height(60.dp))

        //문제
        Text(
            text = currentQuestion.question,
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(50.dp))

        listOf(
            currentQuestion.option_one,
            currentQuestion.option_two,
            currentQuestion.option_three,
            currentQuestion.option_four
        ).forEachIndexed { index, option ->
            OptionItem(option = option, isSelected = selectedOption == index + 1) {
                selectedOption = index + 1
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            if (selectedOption != 0) {
                if (selectedOption == currentQuestion.correct_answer) {
                    dialogMessage = "정답입니다"
                    score++
                } else {
                    dialogMessage = "틀렸습니다"
                }
                showDialog = true
            } else {
                Toast.makeText(context, "답을 선택해 주세요.", Toast.LENGTH_SHORT).show()
            }
        }) {
            Text(text = if (currentPosition < questions.size - 1) "다음" else "Finish")
        }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text(text = dialogMessage) },
                confirmButton = {
                    TextButton(onClick = {
                        showDialog = false
                        selectedOption = 0
                        if (currentPosition < questions.size - 1) {
                            currentPosition++
                        } else {
                            onQuizCompleted(score)
                        }
                    }) {
                        Text(text = "OK")
                    }
                }
            )
        }
    }
}

@Composable
fun OptionItem(option: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .background(
                color = if (isSelected) Color.Gray else Color.White,
                shape = RoundedCornerShape(5.dp)
            )
            .clickable(onClick = onClick)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = option,
            fontSize = 20.sp
        )
    }
}

@Composable
fun ResultScreen(score: Int, totalQuestions: Int,
                 intent1: Intent) {
    val context = LocalContext.current
    val userID = intent1.getStringExtra("userID")
    val companyCode = intent1.getStringExtra("companyCode")
    val userName = intent1.getStringExtra("userName")
    val lectureName = intent1.getStringExtra("lectureName")
    val sharedPreferences = context.getSharedPreferences("FocusLearnPreference", Context.MODE_PRIVATE)
    val totConcentrateAvg =  sharedPreferences.getFloat("totalAvg", 0.00f)*100
    var result by remember {
        mutableStateOf(false)
    }
    if (totConcentrateAvg >= 60 && score >= 2) {
        result = true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "퀴즈 완료!",
            fontSize = 30.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "총 $totalQuestions 문제 중 $score 문제를 맞췄습니다.",
            fontSize = 20.sp
        )
        val fireDB = Firebase.firestore
        val userFireDoc = fireDB.collection("Company").document(companyCode.toString())
            .collection("Employee").document(userName.toString())
        userFireDoc.update("LectureStatus.$lectureName", result)

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = {
            val intent = Intent(context, CompletionScreen::class.java)
            intent.putExtra("userID", userID)
            intent.putExtra("companyCode", companyCode)
            intent.putExtra("userName", userName)
            intent.putExtra("lectureName", lectureName)
            intent.putExtra("result", result)
            context.startActivity(intent)
        }) {
            Text(text = "결과 확인")
        }
    }
}