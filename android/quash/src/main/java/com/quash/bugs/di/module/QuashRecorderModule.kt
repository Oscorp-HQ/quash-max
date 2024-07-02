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

import com.quash.bugs.core.flowcontrol.QuashFlowManager
import com.quash.bugs.features.recorder.QuashRecorder
import com.quash.bugs.features.recorder.QuashRecorderImpl
import dagger.Module
import dagger.Provides
import javax.inject.Named

/**
 * A Dagger module to provide dependencies for the QuashRecorder.
 *
 * This module provides an implementation of QuashRecorder configured with custom parameters
 * such as quality, frequency, and duration. It injects necessary dependencies to the recorder
 * like QuashActivityProvider and QuashServiceStarter which facilitate interaction with the
 * application's activity lifecycle and services, respectively.
 */
@Module
class QuashRecorderModule {

    /**
     * Provides a QuashRecorder instance.
     *
     * This method constructs a QuashRecorderImpl with specific configurations for screenshot
     * quality, capture frequency, and session duration. It also provides the necessary
     * activity and service starters needed for the recorder to function within the application's context.
     *
     * @param quality The quality of the screenshots captured by the recorder.
     * @param frequency The frequency at which screenshots are captured.
     * @param duration The duration of the recording session.
     * @param quashFlowManager Provides communication with base module.
     * @return An instance of QuashRecorder configured with the provided parameters.
     */
    @Provides
    fun provideRecorder(
        @Named("quality") quality: QuashRecorder.ScreenshotQuality,
        @Named("frequency") frequency: QuashRecorder.CaptureFrequency,
        @Named("duration") duration: Int,
        quashFlowManager: QuashFlowManager
    ): QuashRecorder =
        QuashRecorderImpl(quality, frequency, duration, quashFlowManager)
}
