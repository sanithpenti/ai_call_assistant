package com.example.jarvissensor

class TaskExecutor(
    private val onAnswerCall: () -> Unit
) {

    fun execute(intent: String) {

        println("Jarvis TaskExecutor: Received intent = $intent")

        when (intent) {

            "ANSWER_CALL" -> {
                executeAnswerCall()
            }

            "UNKNOWN" -> {
                executeUnknown()
            }

            else -> {
                executeUnknown()
            }
        }
    }

    private fun executeAnswerCall() {

        println("Jarvis TaskExecutor: Executing ANSWER_CALL")

        onAnswerCall()
    }

    private fun executeUnknown() {

        println("Jarvis TaskExecutor: Unknown task")
    }
}