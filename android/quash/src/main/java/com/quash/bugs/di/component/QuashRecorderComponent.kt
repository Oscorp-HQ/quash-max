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

package com.quash.bugs.di.component

import com.quash.bugs.di.module.QuashRecorderModule
import com.quash.bugs.features.recorder.QuashRecorder
import dagger.BindsInstance
import dagger.Subcomponent
import javax.inject.Named

/**
 * Defines a Dagger subcomponent to provide QuashRecorder dependencies.
 *
 * This subcomponent is associated with the QuashRecorderModule which provides the necessary
 * dependencies for QuashRecorder instances. The use of a subcomponent here allows for
 * scoping and provision of specific recorder configurations directly into the dependent components.
 */
@Subcomponent(modules = [QuashRecorderModule::class])
interface QuashRecorderComponent {
    /**
     * Provides an instance of QuashRecorder configured with injected dependencies.
     *
     * @return The QuashRecorder instance ready for use.
     */
    fun getRecorder(): QuashRecorder

    /**
     * Factory interface to create instances of the QuashRecorderComponent subcomponent.
     *
     * This factory allows for dynamic creation of the QuashRecorderComponent with specific
     * configurations passed at runtime. These configurations include screenshot quality,
     * capture frequency, and recording duration which can be adjusted per instance.
     */
    @Subcomponent.Factory
    interface Factory {
        fun create(
            @BindsInstance @Named("quality") quality: QuashRecorder.ScreenshotQuality,
            @BindsInstance @Named("frequency") frequency: QuashRecorder.CaptureFrequency,
            @BindsInstance @Named("duration") duration: Int
        ): QuashRecorderComponent
    }
}
