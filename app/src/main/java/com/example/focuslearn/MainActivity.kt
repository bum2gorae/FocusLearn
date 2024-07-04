package com.example.focuslearn

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import com.example.focuslearn.ui.theme.FocusLearnTheme


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
        enableEdgeToEdge()
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

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(100.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Button(onClick = {
                        val intent = Intent(contextAct, RegisterActivity::class.java)
                        contextAct?.startActivity(intent)
                    }) {
                        Text(text = "Regist")
                    }
                    Button(onClick = {
                        val intent = Intent(contextAct, VideoActivity::class.java)
                        contextAct?.startActivity(intent)
                    }) {
                        Text(text = "Start")
                    }
                }
            }
        }
    }
}


