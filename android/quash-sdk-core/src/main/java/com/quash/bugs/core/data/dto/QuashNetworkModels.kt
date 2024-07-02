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

package com.quash.bugs.core.data.dto

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize

/**
 * Represents the request body for reporting network logs.
 * @property networkLogs The list of network log data to be reported.
 */
@Keep
data class ReportNetworkLogRequestBody(
    val networkLogs: List<QuashNetworkData> = arrayListOf()
)

/**
 * Represents the response received after reporting network logs.
 * @property message The message associated with the response.
 * @property success Indicates whether the network logs were successfully reported.
 */
@Keep
data class ReportNetworkResponse(
    val message: String,
    val success: Boolean
)

/**
 * Represents the data of a single network request/response.
 * @property requestUrl The URL of the network request.
 * @property requestMethod The HTTP method of the network request.
 * @property requestHeaders The headers of the network request.
 * @property requestBody The body of the network request.
 * @property responseCode The HTTP response code of the network response.
 * @property responseHeaders The headers of the network response.
 * @property responseBody The body of the network response.
 * @property durationMs The duration of the network request/response in milliseconds.
 * @property errorMessage The error message associated with the network request/response, if any.
 * @property exceptionMessage The exception message associated with the network request/response, if any.
 * @property exceptionStackTrace The stack trace of the exception associated with the network request/response, if any.
 * @property timeStamp The timestamp of the network request/response.
 */
@Keep
@Parcelize
data class QuashNetworkData(
    val requestUrl: String?,
    val requestMethod: String?,
    val requestHeaders: Map<String, String>?,
    val requestBody: String?,
    var responseCode: Int?,
    var responseHeaders: Map<String, String>?,
    var responseBody: String?,
    var durationMs: Long?,
    var errorMessage: String? = null,
    val exceptionMessage: String? = null,
    val exceptionStackTrace: String? = null,
    val timeStamp: String? = null
) : Parcelable