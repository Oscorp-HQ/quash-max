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

import androidx.annotation.Keep

/**
 * Represents the response received after registering an app.
 * @property success Indicates whether the app registration was successful.
 * @property message The message associated with the app registration response.
 * @property data The data associated with the registered app.
 */
@Keep
data class RegisterAppResponse(
    val success: Boolean,
    val message: String,
    val data: AppData
)

/**
 * Represents the data of a registered app.
 * @property appId The unique identifier of the app.
 * @property packageName The package name of the app.
 * @property appType The type of the app.
 * @property appName The name of the app.
 * @property reportingToken The reporting token associated with the app.
 * @property orgUniqueKey The unique key of the organization associated with the app.
 */
@Keep
data class AppData(
    val appId: String,
    val packageName: String,
    val appType: String,
    val appName: String,
    val reportingToken: String,
    @Transient var orgUniqueKey: String
)

/**
 * Represents the request body for registering an app.
 * @property packageName The package name of the app.
 * @property appType The type of the app.
 * @property appName The name of the app.
 * @property version The version of the app.
 * @property orgUniqueKey The unique key of the organization associated with the app.
 */
@Keep
data class RegisterAppRequest(
    val packageName: String,
    val appType: String,
    val appName: String,
    val version: String,
    val orgUniqueKey: String
)