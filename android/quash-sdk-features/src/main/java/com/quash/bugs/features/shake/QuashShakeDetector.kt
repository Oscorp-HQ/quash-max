/*
 * Copyright (c) 2024 Quash.
 *
 * Licensed under the MIT License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://opensource.org/licenses/MIT
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.quash.bugs.features.shake

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import javax.inject.Inject
import javax.inject.Provider
import kotlin.math.sqrt

/**
 * Interface defining the functionality of a shake detector.
 */
interface ShakeDetector {
    fun startListening()
    fun stopListening()
    fun setSensitivity(threshold: Float, damping: Float)
}

/**
 * Implementation of the ShakeDetector interface.
 *
 * @param sensorManager Manages the device's sensors.
 * @param onShake Provider for the function to call when a shake is detected.
 */
class QuashShakeDetector @Inject constructor(
    private val sensorManager: SensorManager,
    private val onShake: Provider<() -> Unit>
) : ShakeDetector, SensorEventListener {

    private var lastAcceleration = SensorManager.GRAVITY_EARTH
    private var currentAcceleration = SensorManager.GRAVITY_EARTH
    private var acceleration = 12f // Default threshold for detecting shake
    private var damping = 0.9f // Damping factor
    private var accelerometerSensor: Sensor? = null

    init {
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        if (accelerometerSensor == null) {
            // Log if no accelerometer is found
        }
    }

    /**
     * Starts listening for shake events.
     */
    override fun startListening() {
        accelerometerSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    /**
     * Stops listening for shake events.
     */
    override fun stopListening() {
        sensorManager.unregisterListener(this)
    }

    /**
     * Responds to changes in sensor data, detecting shake events based on acceleration thresholds.
     */
    override fun onSensorChanged(event: SensorEvent) {
        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        lastAcceleration = currentAcceleration
        currentAcceleration = sqrt((x * x + y * y + z * z).toDouble()).toFloat()

        val delta = currentAcceleration - lastAcceleration
        acceleration = acceleration * damping + delta

        if (acceleration > 12) {
            onShake.get().invoke()
        }
    }

    /**
     * Sets the sensitivity of the shake detector.
     *
     * @param threshold The acceleration threshold above which a shake is detected.
     * @param damping The damping factor to apply to acceleration changes.
     */
    override fun setSensitivity(threshold: Float, damping: Float) {
        this.acceleration = threshold
        this.damping = damping
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No operation needed
    }
}
