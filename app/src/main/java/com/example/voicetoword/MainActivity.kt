package com.example.voicetoword

import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.RecognitionListener
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.util.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request RECORD_AUDIO permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 1)
        }

        setContent {
            VoiceToTextApp()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceToTextApp() {
    val context = LocalContext.current
    var spokenText by remember { mutableStateOf("Tap the button and speak") }
    val speechRecognizer by remember { mutableStateOf(SpeechRecognizer.createSpeechRecognizer(context)) }

    val recognitionListener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {}
        override fun onBeginningOfSpeech() {}
        override fun onRmsChanged(rmsdB: Float) {}
        override fun onBufferReceived(buffer: ByteArray?) {}
        override fun onEndOfSpeech() {}

        override fun onError(error: Int) {
            spokenText = when (error) {
                SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout, try again!"
                SpeechRecognizer.ERROR_NETWORK -> "Network error, check your internet!"
                SpeechRecognizer.ERROR_AUDIO -> "Audio error, please retry!"
                SpeechRecognizer.ERROR_NO_MATCH -> "No speech detected, please try again!"
                SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognizer is busy, try later!"
                else -> "Unknown error occurred, try again!"
            }
        }

        override fun onResults(results: Bundle?) {
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            if (!matches.isNullOrEmpty()) {
                spokenText = matches[0]
            }
        }

        override fun onPartialResults(partialResults: Bundle?) {}
        override fun onEvent(eventType: Int, params: Bundle?) {}
    }

    DisposableEffect(Unit) {
        speechRecognizer.setRecognitionListener(recognitionListener)
        onDispose { speechRecognizer.destroy() }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { TopAppBar(title = { Text("Voice to Text App") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = spokenText, style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(20.dp))
            Button(onClick = {
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                    putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now")
                }
                speechRecognizer.startListening(intent)
            }) {
                Text("Start Listening")
            }
        }
    }
}
