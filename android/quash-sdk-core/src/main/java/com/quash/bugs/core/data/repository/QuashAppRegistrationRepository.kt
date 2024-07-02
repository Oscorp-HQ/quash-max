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

package com.quash.bugs.core.data.repository

import com.quash.bugs.core.data.dto.RegisterAppRequest
import com.quash.bugs.core.data.preference.QuashUserPreferences
import com.quash.bugs.core.data.remote.QuashApiService
import com.quash.bugs.core.data.remote.QuashToastHandler
import com.quash.bugs.core.data.remote.RequestState
import com.quash.bugs.core.data.remote.protectedApiCallWithRetry
import com.quash.bugs.core.data.remote.protectedApiCallWithToast
import javax.inject.Inject

/**
 * Repository for handling app registration and user data retrieval.
 *
 * @param apiService The QuashApiService instance used to make API calls.
 * @param toastHandler The QuashToastHandler instance used to display toasts.
 * @param quashUserPreferences The QuashUserPreferences instance used to manage user preferences.
 */
class QuashAppRegistrationRepository @Inject constructor(
    private val apiService: QuashApiService,
    private val toastHandler: QuashToastHandler,
    private val quashUserPreferences: QuashUserPreferences
) {

    /**
     * Registers the app using the provided RegisterAppRequest.
     *
     * @param registerAppRequest The request object containing app registration details.
     * @return A Flow emitting the request state.
     */
    fun registerApp(registerAppRequest: RegisterAppRequest) =
        protectedApiCallWithRetry(apiCall = {
            apiService.registerApp(registerAppRequest)
        })

    /**
     * Retrieves users associated with the specified organization key and updates user preferences.
     *
     * @param orgKey The organization key to retrieve users for.
     */
    suspend fun getUsers(orgKey: String?) {
        protectedApiCallWithToast(toastHandler = toastHandler, apiCall = {
            apiService.getUsers(orgKey)
        }).collect {
            when (it) {
                is RequestState.Failed -> {
                    // Handle failure
                }
                RequestState.InProgress -> {
                    // Handle in-progress state
                }
                is RequestState.Successful -> {
                    try {
                        quashUserPreferences.saveOrganisationUsers(it.data.data.organisationUsers)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }
}
