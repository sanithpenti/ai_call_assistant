package com.example.jarvissensor

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.telecom.TelecomManager
import androidx.core.app.ActivityCompat

class CallManager(
    private val context: Context
) {

    private val telecomManager =
        context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager

    fun answerIncomingCall() {

        if (
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ANSWER_PHONE_CALLS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            println("Jarvis CallManager: Answer call permission required.")
            return
        }

        try {

            telecomManager.acceptRingingCall()

            println(
                "Jarvis CallManager: Answering the incoming call."
            )

        } catch (e: SecurityException) {

            println(
                "Jarvis CallManager: Permission denied while answering call."
            )

        } catch (e: Exception) {

            println(
                "Jarvis CallManager: Could not answer the call."
            )

            println(
                "Jarvis CallManager Error: ${e.message}"
            )
        }
    }
}