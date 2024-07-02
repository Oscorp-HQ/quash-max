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
import com.quash.bugs.Quash
import com.quash.bugs.core.data.preference.QuashAppPreferences
import com.quash.bugs.core.data.repository.QuashAppRegistrationRepository
import com.quash.bugs.core.flowcontrol.QuashFlowManager
import com.quash.bugs.di.component.QuashRecorderComponent
import com.quash.bugs.features.network.IQuashNetworkLogger
import com.quash.bugs.features.shake.ShakeDetector
import com.quash.bugs.services.QuashFlowManagerImpl
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineScope
import javax.inject.Provider
import javax.inject.Singleton


@Module
class QuashModule {

    /**
     * Provides the main `Quash` instance that coordinates core functionalities within the SDK.
     * This method assembles all the major components needed by `Quash` such as preferences,
     * network logging, shake detection, and more.
     *
     * @param quashAppPreferences Provides application-specific preferences.
     * @param context The application context.
     * @param appRegistrationRepository Repository for registering the app with backend services.
     * @param coroutineScope Coroutine scope for async operations.
     * @param quashNetworkLogger Logger for network operations.
     * @param shakeDetectorProvider Provides a shake detector.
     * @param recorderFactory Factory to create recorder components.
     * @param quashFlowManager Manages navigational flows within the app.
     * @return An instance of `Quash`.
     */
    @Provides
    @Singleton
    fun provideQuash(
        quashAppPreferences: QuashAppPreferences,
        context: Context,
        appRegistrationRepository: QuashAppRegistrationRepository,
        coroutineScope: CoroutineScope,
        quashNetworkLogger: IQuashNetworkLogger,
        shakeDetectorProvider: Provider<ShakeDetector>,
        recorderFactory: QuashRecorderComponent.Factory,
        quashFlowManager: QuashFlowManager
    ): Quash {
        return Quash(
            context,
            quashAppPreferences,
            appRegistrationRepository,
            coroutineScope,
            quashNetworkLogger,
            shakeDetectorProvider,
            recorderFactory,
            quashFlowManager
        )
    }

    /**
     * Provides the implementation of `QuashFlowManager` that handles the operational flows within the app,
     * such as transitions and interactions across different components of the application.
     *
     * @param context Application context for accessing system services.
     * @param quashAppPreferences Preferences manager to manage app settings and configurations.
     * @return An instance of `QuashFlowManager`.
     */
    @Provides
    @Singleton
    fun providesFlowManager(
        context: Context,
        quashAppPreferences: QuashAppPreferences
    ): QuashFlowManager =
        QuashFlowManagerImpl(context, quashAppPreferences)
}
