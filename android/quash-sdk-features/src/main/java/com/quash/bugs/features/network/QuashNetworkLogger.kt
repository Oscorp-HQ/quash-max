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

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.quash.bugs.core.data.dto.QuashNetworkData
import com.quash.bugs.core.data.local.QuashNetworkDao
import com.quash.bugs.core.data.local.QuashNetworkFilePath
import com.quash.bugs.core.data.preference.QuashCommonPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.Interceptor
import java.io.File
import javax.inject.Inject

/**
 * Implementation of [IQuashNetworkLogger] for logging network calls.
 *
 * @property sharedPreferencesUtil Utility for accessing shared preferences.
 * @property networkDao DAO for network-related database operations.
 * @property coroutineScope Coroutine scope for executing asynchronous tasks.
 * @property context Application context.
 */
class IQuashNetworkLoggerImpl @Inject constructor(
    private val sharedPreferencesUtil: QuashCommonPreferences,
    private val networkDao: QuashNetworkDao,
    private val coroutineScope: CoroutineScope,
    private val context: Context
) : IQuashNetworkLogger {

    private lateinit var networkInterceptor: QuashNetworkInterceptor
    private val _networkLogs = MutableStateFlow<List<QuashNetworkData>>(emptyList())

    /**
     * Initializes network logging based on the provided flag.
     *
     * @param enable Flag to enable or disable network logging.
     */
    override fun initializeLogging(enable: Boolean) {
        sharedPreferencesUtil.setNetworkLogs(enable)
        if (enable) {
            initializeInterceptor()
        }
    }

    /**
     * Initializes the network interceptor.
     */
    private fun initializeInterceptor() {
        networkInterceptor = QuashNetworkInterceptor(this::logNetworkCall, coroutineScope)
    }

    /**
     * Logs a network call.
     *
     * @param networkData The network data to log.
     */
    override fun logNetworkCall(networkData: QuashNetworkData) {
        coroutineScope.launch {
            _networkLogs.update { it + networkData }
        }
    }

    /**
     * Saves network logs to a file.
     */
    override fun saveNetworkLogsToFile() {
        coroutineScope.launch(Dispatchers.IO) {
            val path = saveNetworkLogsToFile(context, _networkLogs.value)
            path?.let {
                networkDao.insertQuashFilePath(QuashNetworkFilePath(0, it))
            }
        }
    }

    /**
     * private function to save network logs to a file.
     *
     * @param networkLogs List of network data to save.
     * @param context context to get files directory.
     * @return The file path where the logs were saved, or null if saving failed.
     */
    private fun saveNetworkLogsToFile(
        context: Context,
        networkLogs: List<QuashNetworkData>
    ): String? {
        return try {
            val gson = Gson()
            val jsonString = gson.toJson(networkLogs)
            val fileName = "networkLogs_${System.currentTimeMillis()}.json"
            val file = File(context.filesDir, fileName)
            file.writeText(jsonString)
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Retrieves the network interceptor.
     *
     * @return The network interceptor, or null if not initialized.
     */
    override fun getNetworkInterceptor(): Interceptor? {
        return if (this::networkInterceptor.isInitialized) {
            networkInterceptor
        } else {
            Log.d("NetworkLogger", "Network Interceptor is not initialized")
            null
        }
    }

    /**
     * Clears all network logs.
     */
    override fun clearNetworkLogs() {
        _networkLogs.value = emptyList()
    }
}
