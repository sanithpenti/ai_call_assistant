package com.example.jarvissensor

import java.net.ServerSocket
import kotlin.concurrent.thread

class CommandServer(
    private val onCommandReceived: (String) -> Unit
) {

    private var serverSocket: ServerSocket? = null

    fun start() {

        thread {

            try {

                serverSocket = ServerSocket(5000)

                println("Jarvis: Command server started.")
                println("Jarvis: Waiting for Python command...")

                while (true) {

                    val client =
                        serverSocket!!.accept()

                    println(
                        "Jarvis: Python connected."
                    )

                    val input =
                        client.getInputStream()
                            .bufferedReader()

                    val command =
                        input.readLine()

                    println(
                        "Jarvis: Command received: $command"
                    )

                    if (command != null) {

                        onCommandReceived(command)
                    }

                    client.close()
                }

            } catch (e: Exception) {

                println(
                    "Jarvis server error: ${e.message}"
                )
            }
        }
    }

    fun stop() {

        try {

            serverSocket?.close()

        } catch (e: Exception) {

            e.printStackTrace()
        }
    }
}