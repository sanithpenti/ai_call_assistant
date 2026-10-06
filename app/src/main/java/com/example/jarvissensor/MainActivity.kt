package com.example.jarvissensor

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.telecom.TelecomManager
import android.telephony.TelephonyCallback
import android.telephony.TelephonyManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import com.example.jarvissensor.ui.theme.JarvisSensorTheme
import java.util.Locale


class MainActivity : ComponentActivity() {

    // ==================================================
    // Android ML model
    // ==================================================

    private val jarvisML = JarvisML()

    private lateinit var taskExecutor: TaskExecutor


    // ==================================================
    // Motion variables
    // ==================================================

    private var variation by mutableStateOf(0.0)

    private var motionStatus by mutableStateOf("Starting...")

    private lateinit var motionManager: MotionManager


    // ==================================================
    // Call variables
    // ==================================================

    private lateinit var telephonyManager: TelephonyManager

    private lateinit var telecomManager: TelecomManager

    private var callState by mutableStateOf("IDLE")

    private var telephonyCallback: TelephonyCallback? = null

    private lateinit var callManager: CallManager


    // ==================================================
    // Python command server
    // ==================================================

    private lateinit var commandServer: CommandServer


    // ==================================================
    // Speech recognition
    // ==================================================

    private lateinit var speechRecognizer: SpeechRecognizer

    private var recognizedText by mutableStateOf("")


    // ==================================================
    // Permission launcher
    // ==================================================

    private val permissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) {
            println("Jarvis: Permissions checked.")
        }


    // ==================================================
    // ON CREATE
    // ==================================================

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)


        // ----------------------------------------------
        // Task executor
        // ----------------------------------------------

        taskExecutor = TaskExecutor(
            onAnswerCall = {
                checkAnswerCallDecision()
            }
        )


        // ----------------------------------------------
        // Call manager
        // ----------------------------------------------

        callManager = CallManager(this)


        // ----------------------------------------------
        // Motion manager
        // ----------------------------------------------

        motionManager = MotionManager(
            this
        ) { status, variation ->

            runOnUiThread {

                motionStatus = status

                this.variation = variation

                println(
                    "Jarvis: Motion status = $status"
                )

                println(
                    "Jarvis: Motion variation = $variation"
                )
            }
        }

        // Start motion detection
        motionManager.start()


        // ----------------------------------------------
        // Telephony setup
        // ----------------------------------------------

        telephonyManager =
            getSystemService(TELEPHONY_SERVICE)
                    as TelephonyManager

        telecomManager =
            getSystemService(TELECOM_SERVICE)
                    as TelecomManager


        // ----------------------------------------------
        // Request permissions
        // ----------------------------------------------

        requestRequiredPermissions()


        // ----------------------------------------------
        // Call-state listener
        // ----------------------------------------------

        setupCallStateListener()


        // ----------------------------------------------
        // Python command server
        // ----------------------------------------------

        commandServer =
            CommandServer { command ->

                runOnUiThread {

                    println(
                        "Jarvis: Processing command: $command"
                    )

                    when (command) {

                        "ANSWER_CALL" -> {

                            checkAnswerCallDecision()
                        }

                        else -> {

                            println(
                                "Jarvis: Unknown command: $command"
                            )
                        }
                    }
                }
            }

        commandServer.start()


        // ----------------------------------------------
        // Speech recognition
        // ----------------------------------------------

        setupSpeechRecognizer()


        // ----------------------------------------------
        // Compose UI
        // ----------------------------------------------

        setContent {

            JarvisSensorTheme {

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                ) {

                    Text(
                        text = "Jarvis Assistant"
                    )

                    Spacer(
                        modifier = Modifier.height(20.dp)
                    )


                    // ----------------------------------
                    // Motion
                    // ----------------------------------

                    Text(
                        text = "Motion Detection"
                    )

                    Text(
                        text = "Variation: %.3f"
                            .format(variation)
                    )

                    Text(
                        text = "Motion: $motionStatus"
                    )

                    Spacer(
                        modifier = Modifier.height(10.dp)
                    )


                    // ----------------------------------
                    // Call state
                    // ----------------------------------

                    Text(
                        text = "Call State: $callState"
                    )

                    Spacer(
                        modifier = Modifier.height(20.dp)
                    )


                    // ----------------------------------
                    // Speak button
                    // ----------------------------------

                    Button(
                        onClick = {

                            startListening()
                        }
                    ) {

                        Text(
                            text = "Speak to Jarvis"
                        )
                    }

                    Spacer(
                        modifier = Modifier.height(10.dp)
                    )


                    // ----------------------------------
                    // Recognized speech
                    // ----------------------------------

                    Text(
                        text = "Recognized: $recognizedText"
                    )

                    Spacer(
                        modifier = Modifier.height(20.dp)
                    )


                    // ----------------------------------
                    // Android ML test
                    // ----------------------------------

                    Button(
                        onClick = {

                            val testText =
                                "please answer my call"

                            val intent =
                                jarvisML.predict(
                                    testText
                                )

                            println(
                                "Jarvis ML test:"
                            )

                            println(
                                "Input: $testText"
                            )

                            println(
                                "Predicted intent: $intent"
                            )
                        }
                    ) {

                        Text(
                            text = "Test Android ML"
                        )
                    }


                    Spacer(
                        modifier = Modifier.height(10.dp)
                    )


                    // ----------------------------------
                    // Direct ANSWER_CALL test
                    // ----------------------------------

                    Button(
                        onClick = {

                            println(
                                "Jarvis: Manual ANSWER_CALL test."
                            )

                            checkAnswerCallDecision()
                        }
                    ) {

                        Text(
                            text = "Test ANSWER_CALL"
                        )
                    }
                }
            }
        }
    }


    // ==================================================
    // PERMISSIONS
    // ==================================================

    private fun requestRequiredPermissions() {

        val permissions =
            mutableListOf(

                Manifest.permission.READ_PHONE_STATE,

                Manifest.permission.ANSWER_PHONE_CALLS,

                Manifest.permission.RECORD_AUDIO
            )

        permissionLauncher.launch(
            permissions.toTypedArray()
        )
    }


    // ==================================================
    // SPEECH RECOGNIZER SETUP
    // ==================================================

    private fun setupSpeechRecognizer() {

        if (
            !SpeechRecognizer.isRecognitionAvailable(this)
        ) {

            println(
                "Jarvis: Speech recognition is not available."
            )

            return
        }


        speechRecognizer =
            SpeechRecognizer.createSpeechRecognizer(this)


        speechRecognizer.setRecognitionListener(

            object : RecognitionListener {

                override fun onReadyForSpeech(
                    params: Bundle?
                ) {

                    println(
                        "Jarvis: Listening..."
                    )
                }


                override fun onBeginningOfSpeech() {

                    println(
                        "Jarvis: Speech detected."
                    )
                }


                override fun onRmsChanged(
                    rmsdB: Float
                ) {
                }


                override fun onBufferReceived(
                    buffer: ByteArray?
                ) {
                }


                override fun onEndOfSpeech() {

                    println(
                        "Jarvis: Finished listening."
                    )
                }


                override fun onError(
                    error: Int
                ) {

                    println(
                        "Jarvis: Speech recognition error: $error"
                    )
                }


                // ======================================
                // Speech → ML → Task
                // ======================================

                override fun onResults(
                    results: Bundle?
                ) {

                    val matches =
                        results?.getStringArrayList(
                            SpeechRecognizer.RESULTS_RECOGNITION
                        )


                    if (!matches.isNullOrEmpty()) {

                        recognizedText =
                            matches[0]


                        println(
                            "Jarvis heard: $recognizedText"
                        )


                        // ----------------------------------
                        // Send speech to ML model
                        // ----------------------------------

                        val predictedIntent =
                            jarvisML.predict(
                                recognizedText
                            )


                        println(
                            "Jarvis predicted intent: $predictedIntent"
                        )


                        // ----------------------------------
                        // Execute predicted task
                        // ----------------------------------

                        taskExecutor.execute(
                            predictedIntent
                        )
                    }
                }


                override fun onPartialResults(
                    partialResults: Bundle?
                ) {
                }


                override fun onEvent(
                    eventType: Int,
                    params: Bundle?
                ) {
                }
            }
        )
    }


    // ==================================================
    // START LISTENING
    // ==================================================

    private fun startListening() {

        if (
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.RECORD_AUDIO
                ),
                102
            )

            println(
                "Jarvis: Microphone permission required."
            )

            return
        }


        val intent =
            Intent(
                RecognizerIntent.ACTION_RECOGNIZE_SPEECH
            ).apply {

                putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                )

                putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE,
                    Locale.getDefault()
                )

                putExtra(
                    RecognizerIntent.EXTRA_PARTIAL_RESULTS,
                    false
                )
            }


        speechRecognizer.startListening(
            intent
        )


        println(
            "Jarvis: Started listening..."
        )
    }


    // ==================================================
    // CALL STATE LISTENER
    // ==================================================

    private fun setupCallStateListener() {

        if (
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_PHONE_STATE
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.READ_PHONE_STATE
                ),
                100
            )

            return
        }


        if (
            Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.S
        ) {

            val callback =
                object :
                    TelephonyCallback(),
                    TelephonyCallback.CallStateListener {

                    override fun onCallStateChanged(
                        state: Int
                    ) {

                        runOnUiThread {

                            when (state) {

                                TelephonyManager.CALL_STATE_IDLE -> {

                                    callState =
                                        "IDLE"

                                    println(
                                        "Jarvis: Call state = IDLE"
                                    )
                                }


                                TelephonyManager.CALL_STATE_RINGING -> {

                                    callState = "RINGING"

                                    println(
                                        "Jarvis: Call state = RINGING"
                                    )

                                    println(
                                        "Jarvis: Incoming call detected."
                                    )

                                    println(
                                        "Jarvis: Starting Call Assistant Service..."
                                    )

                                    val serviceIntent =
                                        Intent(
                                            this@MainActivity,
                                            CallAssistantService::class.java
                                        )

                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                                        startForegroundService(
                                            serviceIntent
                                        )

                                    } else {

                                        startService(
                                            serviceIntent
                                        )
                                    }

                                    println(
                                        "Jarvis: Call Assistant Service start requested."
                                    )

                                    // Keep the existing working test for now.
                                    startListening()
                                }


                                TelephonyManager.CALL_STATE_OFFHOOK -> {

                                    callState =
                                        "OFFHOOK"

                                    println(
                                        "Jarvis: Call state = OFFHOOK"
                                    )
                                }
                            }
                        }
                    }
                }


            telephonyCallback =
                callback


            telephonyManager.registerTelephonyCallback(
                mainExecutor,
                callback
            )
        }
    }


    // ==================================================
    // CALL DECISION
    // ==================================================

    private fun checkAnswerCallDecision() {

        println(
            "Jarvis: Checking whether call should be answered."
        )


        if (callState != "RINGING") {

            println(
                "Jarvis: No incoming call."
            )

            return
        }


        if (motionStatus == "STATIONARY") {

            println(
                "Jarvis: Call is ringing."
            )

            println(
                "Jarvis: Phone is stationary."
            )

            println(
                "Jarvis: Safe to answer call."
            )


            callManager.answerIncomingCall()

        } else {

            println(
                "Jarvis: Call is ringing."
            )

            println(
                "Jarvis: Phone is moving."
            )

            println(
                "Jarvis: I will not answer automatically."
            )
        }
    }


    // ==================================================
    // CLEANUP
    // ==================================================

    override fun onDestroy() {

        super.onDestroy()


        // Stop motion manager

        if (::motionManager.isInitialized) {

            motionManager.stop()
        }


        // Destroy speech recognizer

        if (
            ::speechRecognizer.isInitialized
        ) {

            speechRecognizer.destroy()
        }


        // Unregister call listener

        if (
            Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.S
        ) {

            telephonyCallback?.let {

                telephonyManager.unregisterTelephonyCallback(
                    it
                )
            }
        }


        // Stop Python server

        if (
            ::commandServer.isInitialized
        ) {

            commandServer.stop()
        }
    }
}