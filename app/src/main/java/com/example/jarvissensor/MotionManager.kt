package com.example.jarvissensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.pow
import kotlin.math.sqrt

class MotionManager(
    context: Context,
    private val onMotionChanged: (String, Double) -> Unit
) : SensorEventListener {

    private val sensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private val accelerometer =
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private val magnitudes = mutableListOf<Double>()

    private val windowSize = 20

    fun start() {

        if (accelerometer == null) {

            println("Jarvis MotionManager: Accelerometer not available.")

            return
        }

        sensorManager.registerListener(
            this,
            accelerometer,
            SensorManager.SENSOR_DELAY_NORMAL
        )

        println("Jarvis MotionManager: Started.")
    }

    fun stop() {

        sensorManager.unregisterListener(this)

        println("Jarvis MotionManager: Stopped.")
    }

    override fun onSensorChanged(event: SensorEvent?) {

        if (event?.sensor?.type != Sensor.TYPE_ACCELEROMETER) {
            return
        }

        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        val magnitude =
            sqrt(
                (x * x) +
                        (y * y) +
                        (z * z)
            ).toDouble()

        magnitudes.add(magnitude)

        if (magnitudes.size >= windowSize) {

            val mean = magnitudes.average()

            val variance =
                magnitudes
                    .map { (it - mean).pow(2) }
                    .average()

            val variation = sqrt(variance)

            val motionStatus =
                if (variation < 0.5) {
                    "STATIONARY"
                } else {
                    "MOVING"
                }

            println(
                "Jarvis MotionManager: " +
                        "Variation = $variation, " +
                        "Status = $motionStatus"
            )

            onMotionChanged(
                motionStatus,
                variation
            )

            magnitudes.clear()
        }
    }

    override fun onAccuracyChanged(
        sensor: Sensor?,
        accuracy: Int
    ) {
        // Not required for this project.
    }
}