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

import com.quash.bugs.core.data.dto.BugReportInfo
import com.quash.bugs.core.data.dto.QuashDeviceMetadata
import com.quash.bugs.core.data.dto.QuashNetworkData
import com.quash.bugs.core.data.dto.ReportBugResponse
import com.quash.bugs.core.data.dto.ReportNetworkLogRequestBody
import com.quash.bugs.core.data.dto.ReportNetworkResponse
import com.quash.bugs.core.data.dto.TypedByteArray
import com.quash.bugs.core.data.remote.QuashApiService
import com.quash.bugs.core.data.remote.QuashToastHandler
import com.quash.bugs.core.data.remote.RequestState
import com.quash.bugs.core.data.remote.protectedApiCall
import com.quash.bugs.core.data.remote.protectedApiCallWithToast
import kotlinx.coroutines.flow.Flow
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

// TODO: de-clutter this class 
/**
 * Repository class for managing bug reporting operations.
 *
 * @property apiService The API service used for making network requests.
 * @property toastHandler The toast handler used for displaying toast messages.
 */
class QuashBugReportRepository @Inject constructor(
    private val apiService: QuashApiService,
    private val toastHandler: QuashToastHandler
) {

    /**
     * Reports a bug with the provided bug report information, media byte arrays, and crash log byte array.
     *
     * @param bugReportInfo The bug report information.
     * @param mediaByteArrays The list of media byte arrays.
     * @param crashLogByteArray The crash log byte array.
     * @return A flow of [RequestState] representing the state of the bug report request.
     */
    fun reportBug(
        bugReportInfo: BugReportInfo,
        mediaByteArrays: List<TypedByteArray>,
        crashLogByteArray: TypedByteArray?
    ): Flow<RequestState<ReportBugResponse>> {
        return protectedApiCallWithToast(toastHandler = toastHandler, apiCall = {
            val parts = mediaByteArrays.mapIndexed(::createMediaPart).toMutableList()
            val crashLogPart = crashLogByteArray?.let { createCrashLogPart(it) }
            val quashDeviceMetadata = bugReportInfo.deviceMetadata as QuashDeviceMetadata
            val textData = mapOf(
                "title" to bugReportInfo.title,
                "description" to bugReportInfo.description,
                "type" to bugReportInfo.type,
                "priority" to bugReportInfo.priority,
                "source" to "ANDROID",
                "reporterId" to bugReportInfo.reporterId,
                "appId" to bugReportInfo.appId,
                "deviceMetadata.device" to quashDeviceMetadata.deviceName,
                "deviceMetadata.os" to quashDeviceMetadata.osVersion,
                "deviceMetadata.screenResolution" to quashDeviceMetadata.screenResolution,
                "deviceMetadata.batteryLevel" to quashDeviceMetadata.batteryLevel
            )

            val textParts = textData.map { (key, value) ->
                key to value?.toPlainTextRequestBody()
            }.toMap()

            apiService.reportBug(
                title = textParts["title"] ?: throw IllegalStateException("Title is missing"),
                description = textParts["description"]
                    ?: throw IllegalStateException("Description is missing"),
                type = textParts["type"] ?: throw IllegalStateException("Type is missing"),
                priority = textParts["priority"]
                    ?: throw IllegalStateException("Priority is missing"),
                source = textParts["source"] ?: throw IllegalStateException("Source is missing"),
                mediaFiles = parts,
                crashLog = crashLogPart,
                reporterId = textParts["reporterId"]
                    ?: throw IllegalStateException("Source is missing"),
                appId = textParts["appId"] ?: throw IllegalStateException("appId is missing"),
                device = textParts["deviceMetadata.device"]
                    ?: throw IllegalStateException("device is missing"),
                os = textParts["deviceMetadata.os"] ?: throw IllegalStateException("os is missing"),
                screenResolution = textParts["deviceMetadata.screenResolution"]
                    ?: throw IllegalStateException("screenResolution is missing"),
                batteryLevel = textParts["deviceMetadata.batteryLevel"]
                    ?: throw IllegalStateException("batterLevel is missing")
            )
        })
    }

    private fun String.toPlainTextRequestBody() = toRequestBody("text/plain".toMediaTypeOrNull())

    private fun createMediaPart(index: Int, typedByteArray: TypedByteArray): MultipartBody.Part? {
        if (!typedByteArray.mimeType.contains("/")) {
            return null
        }
        val (mainType, subType) = typedByteArray.mimeType.split("/")
        val fileName = when (mainType) {
            "image" -> "IMG_${index}.${subType}"
            "audio" -> "AUD_${index}.${subType}"
            "video" -> "VID_${index}.${subType}"
            else -> "FILE_${index}.${subType}"
        }

        return MultipartBody.Part.createFormData(
            "mediaFiles",
            fileName,
            typedByteArray.byteArray.toRequestBody(typedByteArray.mimeType.toMediaTypeOrNull())
        )
    }

    private fun createCrashLogPart(typedByteArray: TypedByteArray): MultipartBody.Part? {
        if (!typedByteArray.mimeType.contains("/")) {
            return null
        }
        val fileName = "crash-log.txt"
        return MultipartBody.Part.createFormData(
            "crashLog",
            fileName,
            typedByteArray.byteArray.toRequestBody(typedByteArray.mimeType.toMediaTypeOrNull())
        )
    }

    /**
     * Submits network logs for a specific report ID.
     *
     * @param reportId The ID of the report associated with the network logs.
     * @param networkLogs The list of network log data.
     * @return A flow of [RequestState] representing the state of the network log submission request.
     */
    suspend fun submitNetworkLogs(
        reportId: String,
        networkLogs: List<QuashNetworkData>
    ): Flow<RequestState<ReportNetworkResponse>> {
        return protectedApiCallWithToast(toastHandler = toastHandler, apiCall = {
            apiService.submitNetworkLogs(
                reportId,
                networkLogs = ReportNetworkLogRequestBody(networkLogs)
            )
        })
    }

    /**
     * Retrieves users for a specific organization key.
     *
     * @param orgKey The organization key.
     * @return A flow of [RequestState] representing the state of the user retrieval request.
     */
    fun getUsers(orgKey: String?) =
        protectedApiCallWithToast(toastHandler = toastHandler, apiCall = {
            apiService.getUsers(orgKey)
        })

    /**
     * Submits a screenshot for a specific report ID.
     *
     * @param reportId The ID of the report associated with the screenshot.
     * @param multipartList The list of multipart body parts representing the screenshot.
     * @return A flow of [RequestState] representing the state of the screenshot submission request.
     */
    fun submitScreenshot(
        reportId: String,
        multipartList: List<MultipartBody.Part>
    ): Flow<RequestState<ReportNetworkResponse>> {
        return protectedApiCall(apiCall = {
            apiService.uploadBitmaps(reportId, multipartList)
        })
    }
}