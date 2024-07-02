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
 * Represents the response received when retrieving a list of bug reports.
 * @property success Indicates whether the request was successful.
 * @property message A message describing the response status.
 * @property data The list of bug reports and associated metadata.
 */
@Keep
data class BugListResponse(
    val success: Boolean,
    val message: String,
    val data: BugReportList
)

/**
 * Represents a list of bug reports and associated metadata.
 * @property reports The list of bug reports.
 * @property meta Metadata associated with the bug report list.
 */
@Keep
data class BugReportList(
    val reports: List<Report>,
    val meta: Meta
)

/**
 * Represents a single bug report.
 * @property id The unique identifier of the bug report.
 * @property title The title of the bug report.
 * @property description The description of the bug report.
 * @property reportedBy The user who reported the bug.
 * @property type The type of the bug report.
 * @property priority The priority of the bug report.
 * @property source The source of the bug report.
 * @property status The status of the bug report.
 * @property createdAt The timestamp when the bug report was created.
 * @property listOfMedia The list of media attachments associated with the bug report.
 * @property crashLog2 The crash log associated with the bug report.
 * @property appId The ID of the application associated with the bug report.
 * @property exportedOn The timestamp when the bug report was exported.
 * @property updatedAt The timestamp when the bug report was last updated.
 */
@Keep
@Parcelize
data class Report(
    val id: String,
    val title: String? = null,
    val description: String? = null,
    val reportedBy: Reporter,
    val type: String? = null,
    val priority: String? = null,
    val source: String? = null,
    val status: String? = null,
    val createdAt: String? = null,
    val listOfMedia: List<Media>?,
    val crashLog2: CrashLog?,
    val appId: String? = null,
    val exportedOn: String? = null,
    val updatedAt: String? = null
) : Parcelable

/**
 * Represents the user who reported a bug.
 * @property id The unique identifier of the user.
 * @property fullName The full name of the user.
 * @property workEmail The work email of the user.
 * @property password The password of the user.
 * @property profileImage The URL of the user's profile image.
 * @property coverImage The URL of the user's cover image.
 * @property emailVerified Indicates whether the user's email is verified.
 * @property verificationToken The verification token for the user.
 * @property tokenExpiration The timestamp when the user's token expires.
 * @property createdAt The timestamp when the user was created.
 * @property shouldNavigateToDashboard Indicates whether the user should navigate to the dashboard.
 * @property userOrganisationRole The role of the user in the organization.
 * @property signUpType The type of sign-up for the user.
 * @property enabled Indicates whether the user is enabled.
 * @property authorities The list of authorities associated with the user.
 * @property accountNonExpired Indicates whether the user's account is not expired.
 * @property accountNonLocked Indicates whether the user's account is not locked.
 * @property credentialsNonExpired Indicates whether the user's credentials are not expired.
 */
@Keep
@Parcelize
data class Reporter(
    val id: String? = null,
    val fullName: String? = null,
    val workEmail: String? = null,
    val password: String? = null,
    val profileImage: String? = null,
    val coverImage: String? = null,
    val emailVerified: Boolean,
    val verificationToken: String? = null,
    val tokenExpiration: String? = null,
    val createdAt: String? = null,
    val shouldNavigateToDashboard: Boolean,
    val userOrganisationRole: String? = null,
    val signUpType: String? = null,
    val enabled: Boolean,
    val authorities: List<String>?,
    val accountNonExpired: Boolean,
    val accountNonLocked: Boolean,
    val credentialsNonExpired: Boolean
) : Parcelable

/**
 * Represents a media attachment associated with a bug report.
 * @property id The unique identifier of the media attachment.
 * @property bugId The ID of the bug report associated with the media attachment.
 * @property mediaUrl The URL of the media attachment.
 * @property createdAt The timestamp when the media attachment was created.
 * @property mediaType The type of the media attachment.
 */
@Keep
@Parcelize
data class Media(
    val id: String,
    val bugId: String? = null,
    val mediaUrl: String,
    val createdAt: String? = null,
    val mediaType: String
) : Parcelable

/**
 * Represents a crash log associated with a bug report.
 * @property id The unique identifier of the crash log.
 * @property logUrl The URL of the crash log.
 * @property bugId The ID of the bug report associated with the crash log.
 * @property createdAt The timestamp when the crash log was created.
 */
@Keep
@Parcelize
data class CrashLog(
    val id: String,
    val logUrl: String,
    val bugId: String? = null,
    val createdAt: String? = null
) : Parcelable

/**
 * Represents metadata associated with a list of bug reports.
 * @property currentPage The current page number of the bug report list.
 * @property totalPages The total number of pages in the bug report list.
 * @property totalRecords The total number of records in the bug report list.
 * @property perPage The number of records per page in the bug report list.
 */
@Keep
@Parcelize
data class Meta(
    val currentPage: Int,
    val totalPages: Int,
    val totalRecords: Int,
    val perPage: Int
) : Parcelable

/**
 * An object that holds a reference to a single bug report.
 */
object QuashBugConstant {
    var report: Report? = null
}