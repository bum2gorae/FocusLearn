package com.example.focuslearn

import android.app.Activity
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import com.example.focuslearn.ui.theme.FocusLearnTheme
import com.example.focuslearn.R.color.gold
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

class RegisterActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FocusLearnTheme {
                val db = Firebase.firestore
                val context = LocalContext.current as Activity?
                var ID by remember { mutableStateOf("") }
                var Password by remember { mutableStateOf("") }
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Spacer(modifier = Modifier.size(200.dp))
                    TextField(
                        value = ID,
                        onValueChange = { newText -> ID = newText },
                        placeholder = { Text("ID") },
                        modifier = Modifier.size(width = 250.dp, height = 60.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = Color(0xffedeae4),
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedPlaceholderColor = Color.Black.copy(alpha = 0.5f),
                            disabledPlaceholderColor = Color.Black,
                            focusedPlaceholderColor = Color.Black
                        )
                    )
                    Spacer(modifier = Modifier.size(20.dp))
                    TextField(
                        value = Password,
                        onValueChange = { newText -> Password = newText },
                        placeholder = { Text("Password") },
                        modifier = Modifier.size(width = 250.dp, height = 60.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = Color(0xffedeae4),
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedPlaceholderColor = Color.Black.copy(alpha = 0.5f),
                            disabledPlaceholderColor = Color.Black,
                            focusedPlaceholderColor = Color.Black
                        )
                    )
                    Spacer(modifier = Modifier.size(50.dp))
                    Button(
                        onClick = {
                            val setPassword = mutableMapOf("PassWord" to Password)
                            db.collection("users").document(ID)
                                .set(setPassword)
                                .addOnSuccessListener { documentReference ->
                                    Log.d("Firestore Success", "DocumentSnapshot added with ID: ${documentReference.toString()}")
                                }
                                .addOnFailureListener { e ->
                                    Log.w("Firestore Failure", "Error adding document", e)
                                }
                            // firestore
                        },
                        modifier = Modifier.size(width = 100.dp, height = 45.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorResource(id = gold),
                            contentColor = Color.Black
                        )
                    ) {
                        Text(text = "등록")
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(100.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Button(
                        onClick = {
                            context?.finish()
                        },
                        modifier = Modifier.size(width = 120.dp, height = 45.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorResource(id = gold),
                            contentColor = Color.Black
                        )
                    ) {
                        Text(text = "돌아가기")
                    }
                }
            }
        }
    }
}

