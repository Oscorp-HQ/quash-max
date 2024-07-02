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

package com.quash.bugs.features

import android.content.Context
import android.os.Build
import com.quash.bugs.core.data.dto.RegisterAppRequest
import com.quash.bugs.core.data.preference.QuashAppPreferences
import com.quash.bugs.core.data.remote.RequestState
import com.quash.bugs.core.data.repository.QuashAppRegistrationRepository
import com.quash.bugs.core.util.getAppName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * This class is responsible for initializing the Quash SDK by registering the app with the backend service.
 *
 * @property quashAppPreferences Manages the app's shared preferences.
 * @property appRegistrationRepository Handles app registration with the backend.
 * @property coroutineScope Scope for launching coroutines.
 * @property context Application context.
 */
class QuashAppRegistrar(
    private val quashAppPreferences: QuashAppPreferences,
    private val appRegistrationRepository: QuashAppRegistrationRepository,
    private val coroutineScope: CoroutineScope,
    private val context: Context
) {

    /**
     * Initializes the SDK by registering the app with the backend.
     *
     * @param orgKey The organization's unique key.
     * @param packageName The package name of the app.
     * @param onSuccess Callback function to be executed on successful registration.
     * @param onFailure Callback function to be executed on registration failure.
     */
    fun initializeSDK(
        orgKey: String,
        packageName: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val version = Build.VERSION.RELEASE
        // Create RegisterAppRequest
        val registerAppRequest = RegisterAppRequest(
            packageName = packageName,
            appType = "ANDROID",
            appName = context.getAppName(),
            version = version,
            orgUniqueKey = orgKey
        )
        // Call API to register the app
        registerApp(registerAppRequest, onSuccess, onFailure)
    }

    /**
     * Registers the app with the backend service.
     *
     * @param registerAppRequest The request data for app registration.
     * @param onSuccess Callback function to be executed on successful registration.
     * @param onFailure Callback function to be executed on registration failure.
     */
    private fun registerApp(
        registerAppRequest: RegisterAppRequest,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        coroutineScope.launch {
            try {
                appRegistrationRepository.registerApp(registerAppRequest).collect { state ->
                    when (state) {
                        is RequestState.Failed -> {
                            // Handle registration failure
                        }

                        RequestState.InProgress -> {
                            // Handle in-progress state
                        }
                        is RequestState.Successful -> {
                            quashAppPreferences.saveAppData(state.data.data.apply {
                                orgUniqueKey = registerAppRequest.orgUniqueKey
                            })
                            quashAppPreferences.setAppRegistered(true)
                            appRegistrationRepository.getUsers(registerAppRequest.orgUniqueKey)
                            onSuccess()
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                onFailure(e.localizedMessage ?: "Unknown error")
            }
        }
    }
}
