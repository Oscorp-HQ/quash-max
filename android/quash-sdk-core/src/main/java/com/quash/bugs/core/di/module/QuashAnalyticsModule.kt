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

package com.quash.bugs.core.di.module

import android.content.Context
import com.amplitude.api.Amplitude
import com.amplitude.api.AmplitudeClient
import com.quash.bugs.core.BuildConfig
import com.quash.bugs.core.data.preference.QuashAppPreferences
import com.quash.bugs.core.data.preference.QuashUserPreferences
import com.quash.bugs.core.util.QuashAmplitudeLogger
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class QuashAnalyticsModule {

    @Provides
    @Singleton
    fun provideAmplitudeClient(context: Context): AmplitudeClient {
        return Amplitude.getInstance()
            .trackSessionEvents(true)
            .initialize(context, BuildConfig.AMPLITUDE_API_KEY)
    }

    @Provides
    @Singleton
    fun provideAmplitudeLogger(
        amplitudeClient: AmplitudeClient,
        quashAppPreferences: QuashAppPreferences,
        quashUserPreferences: QuashUserPreferences
    ): QuashAmplitudeLogger =
        QuashAmplitudeLogger(amplitudeClient, quashAppPreferences, quashUserPreferences)
}

