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

package com.quash.bugs.features.network

import com.quash.bugs.core.data.dto.QuashNetworkData
import okhttp3.Interceptor

/**
 * Interface for logging network calls and managing network logging.
 */
interface IQuashNetworkLogger {
    /**
     * Initializes network logging based on the provided flag.
     *
     * @param enable Flag to enable or disable network logging.
     */
    fun initializeLogging(enable: Boolean)

    /**
     * Logs a network call.
     *
     * @param networkData The network data to log.
     */
    fun logNetworkCall(networkData: QuashNetworkData)

    /**
     * Saves network logs to a file.
     */
    fun saveNetworkLogsToFile()

    /**
     * Retrieves the network interceptor.
     *
     * @return The network interceptor, or null if not initialized.
     */
    fun getNetworkInterceptor(): Interceptor?

    /**
     * Clears all network logs.
     */
    fun clearNetworkLogs()
}
