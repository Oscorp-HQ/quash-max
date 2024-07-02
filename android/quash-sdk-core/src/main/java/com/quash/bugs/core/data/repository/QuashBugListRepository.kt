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

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.quash.bugs.core.data.dto.Report
import com.quash.bugs.core.data.dto.ReportBugResponse
import com.quash.bugs.core.data.dto.TypedByteArray
import com.quash.bugs.core.data.paging.QuashBugListDataSource
import com.quash.bugs.core.data.preference.QuashAppPreferences
import com.quash.bugs.core.data.remote.QuashApiService
import com.quash.bugs.core.data.remote.QuashToastHandler
import com.quash.bugs.core.data.remote.RequestState
import com.quash.bugs.core.data.remote.protectedApiCallWithToast
import kotlinx.coroutines.flow.Flow
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

/**
 * Repository for handling bug list related operations.
 *
 * @param apiService The QuashApiService instance used to make API calls.
 * @param toastHandler The QuashToastHandler instance used to display toasts.
 * @param quashAppPreferences The QuashAppPreferences instance used to manage app preferences.
 */
class QuashBugListRepository @Inject constructor(
    private val apiService: QuashApiService,
    private val toastHandler: QuashToastHandler,
    private val quashAppPreferences: QuashAppPreferences
) {

    /**
     * Retrieves a paginated list of bug reports.
     *
     * @return A Flow emitting PagingData of Report.
     */
    fun getBugList(): Flow<PagingData<Report>> {
        return Pager(PagingConfig(pageSize = 10, enablePlaceholders = true)) {
            QuashBugListDataSource(apiService, quashAppPreferences.getAppId()!!)
        }.flow
    }

    /**
     * Deletes a bug report.
     *
     * @param reportId The ID of the bug report to delete.
     * @return A Flow emitting the request state.
     */
    fun deleteBug(reportId: String) =
        protectedApiCallWithToast(toastHandler = toastHandler, apiCall = {
            apiService.deleteBug(reportId)
        })

    /**
     * Updates a bug report with the given details.
     *
     * @param reportId The ID of the bug report to update.
     * @param title The new title of the bug report.
     * @param description The new description of the bug report.
     * @param type The new type of the bug report.
     * @param priority The new priority of the bug report.
     * @param reporterId The ID of the reporter.
     * @param removedId The list of IDs of media files to remove.
     * @param mediaByteArrays The list of TypedByteArray objects representing new media files.
     * @return A Flow emitting the request state.
     */
    fun updateBug(
        reportId: String,
        title: String,
        description: String,
        type: String,
        priority: String,
        reporterId: String,
        removedId: List<String>,
        mediaByteArrays: List<TypedByteArray>
    ): Flow<RequestState<ReportBugResponse>> {
        return protectedApiCallWithToast(toastHandler = toastHandler, apiCall = {
            val parts = mediaByteArrays.mapIndexed(::createMediaPart).toMutableList()

            val textData = mapOf(
                "title" to title,
                "description" to description,
                "type" to type,
                "reporterId" to reporterId,
                "priority" to priority
            )
            val mediaToRemoveIdsParts = removedId.map { it.toPlainTextRequestBody() }.toTypedArray()

            val textParts = textData.map { (key, value) ->
                key to value.toPlainTextRequestBody()
            }.toMap()
            apiService.updateBug(
                reportId,
                title = textParts["title"] ?: throw IllegalStateException("Title is missing"),
                description = textParts["description"]
                    ?: throw IllegalStateException("Description is missing"),
                type = textParts["type"] ?: throw IllegalStateException("Type is missing"),
                priority = textParts["priority"]
                    ?: throw IllegalStateException("Priority is missing"),
                reporterId = textParts["reporterId"]
                    ?: throw IllegalStateException("Reporter Id is missing"),
                newMediaFiles = parts.filterNotNull(),
                mediaToRemoveIds = mediaToRemoveIdsParts
            )
        })
    }

    /**
     * Converts a string to a plain text RequestBody.
     *
     * @return A plain text RequestBody.
     */
    private fun String.toPlainTextRequestBody() = toRequestBody("text/plain".toMediaTypeOrNull())

    /**
     * Creates a MultipartBody.Part from a TypedByteArray.
     *
     * @param index The index of the media file.
     * @param typedByteArray The TypedByteArray object containing the media file data.
     * @return A MultipartBody.Part representing the media file, or null if the mime type is invalid.
     */
    private fun createMediaPart(index: Int, typedByteArray: TypedByteArray): MultipartBody.Part? {
        if (!typedByteArray.mimeType.contains("/")) {
            return null
        }
        val (mainType, subType) = typedByteArray.mimeType.split("/")
        val fileName = when (mainType) {
            "image" -> "IMG_$index.$subType"
            "audio" -> "AUD_$index.$subType"
            "video" -> "VID_$index.$subType"
            else -> "FILE_$index.$subType"
        }

        return MultipartBody.Part.createFormData(
            "newMediaFiles[$index]",
            fileName,
            typedByteArray.byteArray.toRequestBody(typedByteArray.mimeType.toMediaTypeOrNull())
        )
    }
}
