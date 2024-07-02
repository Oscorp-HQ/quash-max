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
 * Represents the response received when retrieving user and organization data.
 * @property success Indicates whether the request was successful.
 * @property message The message associated with the response.
 * @property data The data containing the organization information.
 */
@Keep
data class UserResponse(
    val success: Boolean,
    val message: String,
    val data: OrganisationData
)

/**
 * Represents the data of an organization.
 * @property organisationName The name of the organization.
 * @property organisationUsers The list of users associated with the organization.
 */
@Keep
data class OrganisationData(
    val organisationName: String,
    val organisationUsers: List<OrganisationUser>
)

/**
 * Represents a user within an organization.
 * @property id The unique identifier of the user.
 * @property name The name of the user.
 * @property email The email address of the user.
 * @property admin Indicates whether the user has admin privileges.
 */
@Keep
data class OrganisationUser(
    val id: String,
    val name: String?,
    val email: String,
    val admin: Boolean
)