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
import android.net.Uri
import javax.inject.Inject

/**
 * Manages interactions with SharedPreferences for the application.
 * This class provides utility methods to retrieve and store data in SharedPreferences.
 *
 * @param sharedPreferences The SharedPreferences instance to use for data storage.
 */
open class QuashPreferencesManager @Inject constructor(protected val sharedPreferences: SharedPreferences) {

    /**
     * Retrieves a string value from SharedPreferences.
     *
     * @param key The key of the value to retrieve.
     * @return The string value associated with the key, or an empty string if not found.
     */
    protected fun retrieveString(key: String): String = sharedPreferences.getString(key, "") ?: ""

    /**
     * Retrieves a list of URIs from a comma-separated string in SharedPreferences.
     *
     * @param key The key of the value to retrieve.
     * @return A list of URIs associated with the key, or an empty list if not found.
     */
    protected fun retrieveUriList(key: String): List<Uri> =
        sharedPreferences.getString(key, "")?.split(",")?.filter { it.isNotBlank() }
            ?.map { Uri.parse(it) } ?: emptyList()

    /**
     * Retrieves a list of audio file URIs from a comma-separated string in SharedPreferences.
     *
     * @param key The key of the value to retrieve.
     * @return A list of URIs for audio files associated with the key, or an empty list if not found.
     */
    protected fun retrieveAudioFiles(key: String): List<Uri> =
        sharedPreferences.getString(key, "")?.split(",")?.filter { it.isNotBlank() }
            ?.map { Uri.parse(it) } ?: emptyList()

    /**
     * Retrieves a list of strings (IDs of removed media files) from a comma-separated string in SharedPreferences.
     *
     * @param key The key of the value to retrieve.
     * @return A list of string IDs associated with the key, or an empty list if not found.
     */
    protected fun retrieveRemovedFiles(key: String): List<String> =
        sharedPreferences.getString(key, "")?.split(",")?.filter { it.isNotBlank() } ?: emptyList()

    /**
     * Stores a boolean value in SharedPreferences.
     *
     * @param key The key under which the value should be stored.
     * @param value The boolean value to store.
     */
    fun putBoolean(key: String, value: Boolean) {
        sharedPreferences.edit().putBoolean(key, value).apply()
    }

    /**
     * Retrieves a boolean value from SharedPreferences.
     *
     * @param key The key of the value to retrieve.
     * @param defaultValue The default value to return if the key isn't found.
     * @return The boolean value associated with the key, or the default value if not found.
     */
    fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return sharedPreferences.getBoolean(key, defaultValue)
    }
}
