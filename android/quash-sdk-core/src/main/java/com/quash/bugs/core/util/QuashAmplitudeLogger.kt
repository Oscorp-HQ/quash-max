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

package com.quash.bugs.core.util

import com.amplitude.api.AmplitudeClient
import com.quash.bugs.core.data.preference.QuashAppPreferences
import com.quash.bugs.core.data.preference.QuashUserPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

/**
 * Utility class for logging events to Amplitude.
 *
 * @param amplitudeClient The Amplitude client instance used for logging events.
 * @param quashAppPreferences The Quash app preferences used for retrieving app-related data.
 * @param quashUserPreferences The Quash user preferences used for retrieving user-related data.
 */
class QuashAmplitudeLogger(
    private val amplitudeClient: AmplitudeClient,
    private val quashAppPreferences: QuashAppPreferences,
    private val quashUserPreferences: QuashUserPreferences
) {

    /**
     * Logs an event to Amplitude with the specified event name and source.
     *
     * @param eventName The name of the event to log.
     * @param source The source of the event.
     */
    suspend fun logEvent(eventName: String, source: String) {
        val jsonObject = getCommonParameters()
        jsonObject.put(QuashEventConstant.Event.NAME, eventName)
        jsonObject.put(QuashEventConstant.Event.SOURCE, source)
        try {
            amplitudeClient.userId = quashUserPreferences.getUserId()
            withContext(Dispatchers.IO) {
                amplitudeClient.logEvent(eventName, jsonObject)
            }
        } catch (e: Exception) {
            handleException(e)
        }
    }

    /**
     * Logs an event to Amplitude with the specified event name and JSON object.
     *
     * @param eventName The name of the event to log.
     * @param jsonObject The JSON object containing additional event data.
     */
    suspend fun logEvent(eventName: String, jsonObject: JSONObject) {
        try {
            amplitudeClient.userId = quashUserPreferences.getUserId()
            withContext(Dispatchers.IO) {
                amplitudeClient.logEvent(eventName, jsonObject)
            }
        } catch (e: Exception) {
            handleException(e)
        }
    }

    /**
     * Retrieves the common parameters for logging events.
     *
     * @return A JSON object containing the common event parameters.
     */
    private fun getCommonParameters(): JSONObject {
        val jsonObject = JSONObject()
        jsonObject.put(QuashEventConstant.App.NAME, quashAppPreferences.getAppName())
        jsonObject.put(QuashEventConstant.App.ID, quashAppPreferences.getAppId())
        jsonObject.put(QuashEventConstant.App.TYPE, quashAppPreferences.getAppType())
        jsonObject.put(QuashEventConstant.App.PACKAGE_ID, quashAppPreferences.getPackageName())
        jsonObject.put(QuashEventConstant.Org.UNIQUE_KEY, quashAppPreferences.getOrgKey())
        return jsonObject
    }

    /**
     * Handles exceptions that occur during event logging.
     *
     * @param exception The exception to handle.
     */
    private fun handleException(exception: Exception) {
        // TODO: Implement exception handling logic
    }
}