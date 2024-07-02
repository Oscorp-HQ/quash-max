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

package com.quash.bugs.core.data.preference

import android.content.SharedPreferences
import javax.inject.Inject

/**
 * Manages common preferences used across different functionalities.
 *
 * @param sharedPreferences The SharedPreferences instance.
 */
class QuashCommonPreferences @Inject constructor(private val sharedPreferences: SharedPreferences) {

    companion object {
        private const val SHOULD_ENABLE_NETWORK_LOGS = "shouldEnableNetworkLogs"
        private const val ON_CRASH = "onCrashReporting"
    }

    /**
     * Checks if network logging is enabled in SharedPreferences.
     *
     * @return True if enabled, false otherwise.
     */
    fun isNetworkLogEnabled(): Boolean {
        return sharedPreferences.getBoolean(SHOULD_ENABLE_NETWORK_LOGS, false)
    }

    /**
     * Enables or disables network logging.
     *
     * @param shouldEnable True to enable, false to disable.
     */
    fun setNetworkLogs(shouldEnable: Boolean) {
        val editor = sharedPreferences.edit()
        editor.putBoolean(SHOULD_ENABLE_NETWORK_LOGS, shouldEnable)
        editor.apply()
    }

    /**
     * Marks the application as registered in SharedPreferences.
     *
     * @param isRegistered True to mark as registered, false otherwise.
     */
    fun setAppRegistered(isRegistered: Boolean) {
        val editor = sharedPreferences.edit()
        editor.putBoolean("isAppRegistered", isRegistered)
        editor.apply()
    }

    /**
     * Updates the ongoing status of a crash report.
     *
     * @param isOngoing True if a crash report is ongoing, false otherwise.
     */
    fun setIsCrashReportOngoing(isOngoing: Boolean) {
        val editor = sharedPreferences.edit()
        editor.putBoolean(ON_CRASH, isOngoing)
        editor.apply()
    }

    /**
     * General purpose method to save a boolean value in SharedPreferences.
     *
     * @param key The key under which the value should be saved.
     * @param value The boolean value to save.
     */
    fun putBoolean(key: String, value: Boolean) {
        sharedPreferences.edit().putBoolean(key, value).apply()
    }

    /**
     * General purpose method to retrieve a boolean value from SharedPreferences.
     *
     * @param key The key under which the value is stored.
     * @param defaultValue The default value to return if the key isn't found.
     *
     * @return The boolean value retrieved or the default value.
     */
    fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return sharedPreferences.getBoolean(key, defaultValue)
    }
}
