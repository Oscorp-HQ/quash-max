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

package com.quash.bugs.di.module

import android.content.Context
import android.hardware.SensorManager
import com.quash.bugs.Quash
import com.quash.bugs.features.shake.QuashShakeDetector
import com.quash.bugs.features.shake.ShakeDetector
import dagger.Module
import dagger.Provides
import javax.inject.Provider
import javax.inject.Singleton

/**
 * This module is responsible for providing dependencies related to sensor management within the Quash SDK.
 * It ensures that the necessary sensor services, particularly for shake detection, are available and properly
 * configured throughout the application. By abstracting the setup and provision of these services into a
 * Dagger module, the SDK maintains clean architecture and separation of concerns, allowing for easier
 * maintenance and scalability.
 *
 * Key services provided by this module include:
 * - SensorManager: To access the device's physical sensors.
 * - ShakeDetector: To detect and handle shake gestures, crucial for user interactions or triggering specific
 *   features dynamically.
 * - Callback Function: A functional interface provided to respond to shake events, linking sensor input to
 *   actionable responses within the application logic.
 *
 * The module uses the Singleton scope for its providers to ensure that instances are shared and remain alive
 * throughout the app lifecycle, promoting efficient resource use and consistent behavior.
 */

@Module
class QuashSensorModule {
    @Provides
    @Singleton
    fun provideSensorManager(context: Context): SensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    @Provides
    @Singleton
    fun provideShakeDetector(
        sensorManager: SensorManager,
        onShake: Provider<Function0<Unit>>
    ): ShakeDetector {
        return QuashShakeDetector(sensorManager) { onShake.get() }
    }

    /**
     * Provides a singleton callback function that gets executed when a shake gesture is detected.
     *
     * This method leverages the Singleton scope to ensure that the same instance of the callback is used throughout
     * the application's lifecycle, maintaining consistency across various components that handle shake detection.
     *
     * The callback is designed to interact directly with the Quash class, which centralizes the handling of various
     * actions and events within the application. By invoking 'quash.onShakeDetected()', this callback triggers any
     * operations or behaviors predefined in the Quash class to respond to shake gestures.
     *
     * The use of a Provider for the callback ensures lazy fetching of the actual implementation, optimizing performance
     * and resource utilization by initializing the callback only when it's actually needed. This approach also adds
     * flexibility, allowing for the callback's dependencies to be fully resolved before it is instantiated.
     *
     * @param quash An instance of the Quash class, which contains the method 'onShakeDetected' that defines the
     *              actions to take when a shake is detected.
     * @return A function that encapsulates the shake detection response logic, maintaining a clean separation of
     *         concerns and promoting modularity in the application architecture.
     */
    @Provides
    @Singleton
    fun provideOnShakeCallback(quash: Quash): Function0<Unit> = {
        quash.onShakeDetected()
    }
}