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

import android.net.Uri
import androidx.annotation.Keep

/**
 * Represents the information needed to report a bug.
 * @property reportingToken The reporting token for the bug report.
 * @property title The title of the bug report.
 * @property description The description of the bug report.
 * @property type The type of the bug report.
 * @property priority The priority of the bug report.
 * @property mediaFiles The list of media files associated with the bug report.
 * @property audioFiles The list of audio files associated with the bug report.
 * @property crashLog The crash log associated with the bug report.
 * @property reporterId The ID of the reporter who submitted the bug report.
 * @property appId The ID of the application associated with the bug report.
 * @property deviceMetadata Additional device metadata associated with the bug report.
 */
@Keep
data class BugReportInfo(
    var reportingToken: String = "",
    var title: String = "",
    var description: String = "",
    var type: String = "",
    var priority: String = "",
    var mediaFiles: List<Uri> = mutableListOf(),
    var audioFiles: List<Uri> = mutableListOf(),
    var crashLog: Uri?,
    var reporterId: String = "",
    var appId: String = "",
    var deviceMetadata: Any? = null
)

/**
 * Represents the information needed to edit a bug report.
 * @property bugId The ID of the bug report to edit.
 * @property edTitle The updated title of the bug report.
 * @property edDescription The updated description of the bug report.
 * @property edType The updated type of the bug report.
 * @property edMediaFiles The updated list of media files associated with the bug report.
 * @property edAudioFiles The updated list of audio files associated with the bug report.
 * @property edReporterId The updated ID of the reporter who submitted the bug report.
 * @property removedMedia The list of media files to be removed from the bug report.
 * @property originalMediaJson The original JSON representation of the media files.
 * @property user The user associated with the bug report.
 * @property edpriority The updated priority of the bug report.
 */
@Keep
data class EditBugInfo(
    var bugId: String = "",
    var edTitle: String = "",
    var edDescription: String = "",
    var edType: String = "",
    var edMediaFiles: List<Uri> = mutableListOf(),
    var edAudioFiles: List<Uri> = mutableListOf(),
    var edReporterId: String = "",
    var removedMedia: List<String> = mutableListOf(),
    var originalMediaJson: String = "",
    var user: String = "",
    var edpriority: String = ""
)

/**
 * Represents a priority level for a bug report.
 * @property id The ID of the priority level.
 * @property displayName The display name of the priority level.
 * @property serverField The server field associated with the priority level.
 */
@Keep
data class QuashPriority(
    val id: Int,
    val displayName: String,
    val serverField: String
)

/**
 * Represents a reporter with a name and ID.
 * @property name The name of the reporter.
 * @property id The ID of the reporter.
 */
@Keep
data class ReporterInt(
    val name: String,
    val id: String
)

/**
 * Represents a byte array with an associated MIME type.
 * @property byteArray The byte array.
 * @property mimeType The MIME type associated with the byte array.
 */
@Keep
data class TypedByteArray(
    val byteArray: ByteArray,
    val mimeType: String,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TypedByteArray

        if (!byteArray.contentEquals(other.byteArray)) return false
        return mimeType == other.mimeType
    }

    override fun hashCode(): Int {
        var result = byteArray.contentHashCode()
        result = 31 * result + mimeType.hashCode()
        return result
    }
}

/**
 * Represents the response received after reporting a bug.
 * @property data The data associated with the bug report response.
 * @property message The message associated with the bug report response.
 * @property success Indicates whether the bug report was successfully submitted.
 */
@Keep
data class ReportBugResponse(
    val `data`: Data,
    val message: String,
    val success: Boolean
)

/**
 * Represents the data associated with a bug report response.
 * @property appId The ID of the application associated with the bug report.
 * @property crashLog The crash log associated with the bug report.
 * @property createdAt The timestamp when the bug report was created.
 * @property description The description of the bug report.
 * @property exportedOn The timestamp when the bug report was exported.
 * @property id The ID of the bug report.
 * @property listOfMedia The list of media files associated with the bug report.
 * @property reportedBy The user who reported the bug.
 * @property source The source of the bug report.
 * @property status The status of the bug report.
 * @property title The title of the bug report.
 * @property type The type of the bug report.
 * @property updatedAt The timestamp when the bug report was last updated.
 */
@Keep
data class Data(
    val appId: String,
    val crashLog: Any,
    val createdAt: String,
    val description: String,
    val exportedOn: Any,
    val id: String,
    val listOfMedia: List<OfMedia>,
    val reportedBy: ReportedBy,
    val source: String,
    val status: String,
    val title: String,
    val type: String,
    val updatedAt: Any
)

/**
 * Represents a media file associated with a bug report.
 * @property bugId The ID of the bug report associated with the media file.
 * @property createdAt The timestamp when the media file was created.
 * @property id The ID of the media file.
 * @property mediaType The type of the media file.
 * @property mediaUrl The URL of the media file.
 */
@Keep
data class OfMedia(
    val bugId: Any,
    val createdAt: String,
    val id: String,
    val mediaType: String,
    val mediaUrl: String
)

/**
 * Represents the user who reported a bug.
 * @property accountNonExpired Indicates whether the user's account is not expired.
 * @property accountNonLocked Indicates whether the user's account is not locked.
 * @property authorities The authorities associated with the user.
 * @property coverImage The cover image of the user.
 * @property createdAt The timestamp when the user was created.
 * @property credentialsNonExpired Indicates whether the user's credentials are not expired.
 * @property emailVerified Indicates whether the user's email is verified.
 * @property enabled Indicates whether the user is enabled.
 * @property fullName The full name of the user.
 * @property id The ID of the user.
 * @property password The password of the user.
 * @property profileImage The profile image of the user.
 * @property shouldNavigateToDashboard Indicates whether the user should navigate to the dashboard.
 * @property signUpType The type of sign-up for the user.
 * @property tokenExpiration The timestamp when the user's token expires.
 * @property userOrganisationRole The role of the user in the organization.
 * @property username The username of the user.
 * @property verificationToken The verification token for the user.
 * @property workEmail The work email of the user.
 */
@Keep
data class ReportedBy(
    val accountNonExpired: Boolean,
    val accountNonLocked: Boolean,
    val authorities: Any,
    val coverImage: Any,
    val createdAt: String,
    val credentialsNonExpired: Boolean,
    val emailVerified: Boolean,
    val enabled: Boolean,
    val fullName: Any,
    val id: String,
    val password: Any,
    val profileImage: Any,
    val shouldNavigateToDashboard: Boolean,
    val signUpType: Any,
    val tokenExpiration: Any,
    val userOrganisationRole: Any,
    val username: Any,
    val verificationToken: Any,
    val workEmail: String
)