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

package com.quash.bugs.core.data.remote

import com.quash.bugs.core.data.dto.BugListResponse
import com.quash.bugs.core.data.dto.RegisterAppRequest
import com.quash.bugs.core.data.dto.RegisterAppResponse
import com.quash.bugs.core.data.dto.ReportBugResponse
import com.quash.bugs.core.data.dto.ReportNetworkLogRequestBody
import com.quash.bugs.core.data.dto.ReportNetworkResponse
import com.quash.bugs.core.data.dto.UserResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Quash API service for handling network operations related to bug reporting,
 * user management, and app registration.
 */
interface QuashApiService {

    /**
     * Reports a bug to the server.
     *
     * @param title The title of the bug.
     * @param description The description of the bug.
     * @param type The type of the bug.
     * @param priority The priority level of the bug.
     * @param source The source of the bug report.
     * @param mediaFiles The list of media files associated with the bug report.
     * @param crashLog The crash log associated with the bug report.
     * @param reporterId The ID of the reporter.
     * @param appId The ID of the app.
     * @param device The device metadata.
     * @param os The operating system metadata.
     * @param screenResolution The screen resolution metadata.
     * @param batteryLevel The battery level metadata.
     * @return The response from the server.
     */
    @Multipart
    @POST("api/report")
    suspend fun reportBug(
        @Part("title") title: RequestBody,
        @Part("description") description: RequestBody,
        @Part("type") type: RequestBody,
        @Part("priority") priority: RequestBody,
        @Part("source") source: RequestBody,
        @Part mediaFiles: MutableList<MultipartBody.Part?>,
        @Part crashLog: MultipartBody.Part?,
        @Part("reporterId") reporterId: RequestBody,
        @Part("appId") appId: RequestBody,
        @Part("deviceMetadata.device") device: RequestBody,
        @Part("deviceMetadata.os") os: RequestBody,
        @Part("deviceMetadata.screenResolution") screenResolution: RequestBody,
        @Part("deviceMetadata.batteryLevel") batteryLevel: RequestBody
    ): Response<ReportBugResponse>

    /**
     * Fetches the list of users associated with an organization.
     *
     * @param orgUniqueKey The unique key of the organization.
     * @return The response containing the user information.
     */
    @GET("api/app/users")
    suspend fun getUsers(
        @Query("orgUniqueKey") orgUniqueKey: String?
    ): Response<UserResponse>

    /**
     * Fetches the list of bugs for an app.
     *
     * @param appId The ID of the app.
     * @param page The page number for pagination.
     * @param size The number of items per page.
     * @return The response containing the list of bugs.
     */
    @GET("api/report")
    suspend fun getBugs(
        @Query("appId") appId: String,
        @Query("page") page: Int,
        @Query("size") size: Int
    ): Response<BugListResponse>

    /**
     * Deletes a bug report by ID.
     *
     * @param reportId The ID of the bug report to delete.
     * @return The response from the server.
     */
    @DELETE("api/report/{reportId}")
    suspend fun deleteBug(
        @Path("reportId") reportId: String
    ): Response<BugListResponse>

    /**
     * Registers an app with the server.
     *
     * @param request The registration request containing app details.
     * @return The response from the server.
     */
    @POST("api/app/register-app")
    suspend fun registerApp(
        @Body request: RegisterAppRequest
    ): Response<RegisterAppResponse>

    /**
     * Updates a bug report by ID.
     *
     * @param reportId The ID of the bug report to update.
     * @param title The updated title of the bug.
     * @param description The updated description of the bug.
     * @param type The updated type of the bug.
     * @param priority The updated priority level of the bug.
     * @param reporterId The updated ID of the reporter.
     * @param newMediaFiles The list of new media files associated with the bug report.
     * @param mediaToRemoveIds The IDs of media files to remove.
     * @return The response from the server.
     */
    @Multipart
    @PATCH("api/report/{reportId}")
    suspend fun updateBug(
        @Path("reportId") reportId: String,
        @Part("title") title: RequestBody,
        @Part("description") description: RequestBody,
        @Part("type") type: RequestBody,
        @Part("priority") priority: RequestBody,
        @Part("reporterId") reporterId: RequestBody,
        @Part newMediaFiles: List<MultipartBody.Part>,
        @Part("mediaToRemoveIds") vararg mediaToRemoveIds: RequestBody
    ): Response<ReportBugResponse>

    /**
     * Submits network logs for a specific bug report.
     *
     * @param reportId The ID of the bug report.
     * @param networkLogs The network logs to submit.
     * @return The response from the server.
     */
    @POST("api/report/{reportId}/network-logs")
    suspend fun submitNetworkLogs(
        @Path("reportId") reportId: String,
        @Body networkLogs: ReportNetworkLogRequestBody
    ): Response<ReportNetworkResponse>

    /**
     * Uploads bitmaps for a specific bug report.
     *
     * @param reportId The ID of the bug report.
     * @param bitmaps The list of bitmap files to upload.
     * @return The response from the server.
     */
    @Multipart
    @POST("api/report/{reportId}/bitmaps")
    suspend fun uploadBitmaps(
        @Path("reportId") reportId: String,
        @Part bitmaps: List<MultipartBody.Part>
    ): Response<ReportNetworkResponse>
}
