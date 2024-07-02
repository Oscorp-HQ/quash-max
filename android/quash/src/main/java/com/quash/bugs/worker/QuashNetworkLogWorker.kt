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

package com.quash.bugs.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.gson.Gson
import com.quash.bugs.Quash
import com.quash.bugs.core.data.dto.QuashNetworkData
import com.quash.bugs.core.data.local.QuashNetworkDao
import com.quash.bugs.core.data.remote.RequestState
import com.quash.bugs.core.data.repository.QuashBugReportRepository
import com.quash.bugs.core.di.module.QuashCoreModule
import com.quash.bugs.di.component.DaggerQuashComponent
import java.io.File
import javax.inject.Inject

/**
 * A worker that processes network logs stored in a file and submits them to a server.
 * It also handles the cleanup of the network logs after submission.
 */
class QuashNetworkLogWorker(appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    init {
        // Inject dependencies required by this worker.
        DaggerQuashComponent.builder()
            .quashCoreModule(QuashCoreModule(appContext))
            .build()
            .inject(this)
    }

    @Inject
    lateinit var networkDao: QuashNetworkDao

    @Inject
    lateinit var bugReportRepository: QuashBugReportRepository

    /**
     * Performs the work needed to read network logs from storage, submit them to the server,
     * and handle the cleanup of logs and files post-submission.
     */
    override suspend fun doWork(): Result {
        val reportId = inputData.getString("reportId") ?: return Result.failure()
        val filePath = inputData.getString("filePath") ?: return Result.failure()

        val file = File(filePath)
        if (!file.exists()) return Result.failure()

        val jsonString = file.readText()
        val listOfNetworkLogs =
            Gson().fromJson(jsonString, Array<QuashNetworkData>::class.java).toList()

        return try {
            submitNetworkLogs(reportId, listOfNetworkLogs)
            file.delete()  // Clean up the temporary file after processing.
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }

    /**
     * Submits a list of network logs to the server and handles the response to update local storage accordingly.
     * It deletes all network logs from local database if the submission is successful.
     *
     * @param reportId The identifier for the report to which these network logs belong.
     * @param networkLogs A list of network data logs to be submitted.
     */
    private suspend fun submitNetworkLogs(reportId: String, networkLogs: List<QuashNetworkData>) {
        try {
            bugReportRepository.submitNetworkLogs(reportId, networkLogs).collect {
                when (it) {
                    is RequestState.Successful -> {
                        networkDao.deleteAll()  // Clear the database of logs after successful submission.
                        Quash.getInstance().clearNetworkLogs()
                        Log.i("Report:", "Network Logs Published")
                    }

                    else -> Log.i("Report:", "Network Logs Failed")
                }
            }
        } catch (e: Exception) {
            // Handle exceptions such as network errors.
        }
    }
}
