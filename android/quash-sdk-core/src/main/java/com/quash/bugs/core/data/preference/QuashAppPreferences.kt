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
import com.quash.bugs.core.data.dto.AppData
import javax.inject.Inject

/**
 * Manages application-related preferences.
 *
 * @param sharedPreferences The SharedPreferences instance.
 */
class QuashAppPreferences @Inject constructor(private val sharedPreferences: SharedPreferences) {

    companion object {
        private const val APP_ID_KEY = "appId"
        private const val PACKAGE_NAME_KEY = "packageName"
        private const val APP_TYPE_KEY = "appType"
        private const val APP_NAME_KEY = "appName"
        private const val REPORTING_TOKEN_KEY = "reportingToken"
        private const val ORG_KEY = "orgKey"
        private const val SESSION_DURATION = "sessionDuration"
    }

    /**
     * Retrieves the session duration value from SharedPreferences.
     *
     * @return The session duration value, or 40 if not set.
     */
    fun getSessionDuration(): Int = sharedPreferences.getInt(SESSION_DURATION, 40)

    /**
     * Sets the session duration value in SharedPreferences.
     *
     * @param i The session duration value to be set.
     */
    fun setSessionDuration(i: Int) {
        sharedPreferences.edit().putInt(SESSION_DURATION, i).apply()
    }

    /**
     * Saves the basic app data to SharedPreferences.
     *
     * @param appData An AppData object containing the application's metadata.
     */
    fun saveAppData(appData: AppData) {
        with(sharedPreferences.edit()) {
            putString(APP_ID_KEY, appData.appId)
            putString(PACKAGE_NAME_KEY, appData.packageName)
            putString(APP_TYPE_KEY, appData.appType)
            putString(APP_NAME_KEY, appData.appName)
            putString(REPORTING_TOKEN_KEY, appData.reportingToken)
            putString(ORG_KEY, appData.orgUniqueKey)
            apply()
        }
    }

    /**
     * Marks the application as registered in SharedPreferences.
     * @param isRegistered True to mark as registered, false otherwise.
     */
    fun setAppRegistered(isRegistered: Boolean) {
        val editor = sharedPreferences.edit()
        editor.putBoolean("isAppRegistered", isRegistered)
        editor.apply()
    }

    /**
     * Retrieves the app's unique identifier from SharedPreferences.
     *
     * @return The app's unique identifier or null if it's not found.
     */
    fun getAppId(): String? = sharedPreferences.getString(APP_ID_KEY, null)

    /**
     * Retrieves the package name of the application from SharedPreferences.
     *
     * @return The package name, or null if not set.
     */
    fun getPackageName(): String? = sharedPreferences.getString(PACKAGE_NAME_KEY, null)

    /**
     * Retrieves the application type from SharedPreferences.
     *
     * @return The application type, or null if not set.
     */
    fun getAppType(): String? = sharedPreferences.getString(APP_TYPE_KEY, null)

    /**
     * Retrieves the application name from SharedPreferences.
     *
     * @return The application name, or null if not set.
     */
    fun getAppName(): String? = sharedPreferences.getString(APP_NAME_KEY, null)

    /**
     * Retrieves the reporting token for the application from SharedPreferences.
     *
     * @return The reporting token, or null if not set.
     */
    fun getReportingToken(): String? = sharedPreferences.getString(REPORTING_TOKEN_KEY, null)

    /**
     * Retrieves the organization key from SharedPreferences.
     *
     * @return The organization key, or null if not set.
     */
    fun getOrgKey(): String? = sharedPreferences.getString(ORG_KEY, null)
}
