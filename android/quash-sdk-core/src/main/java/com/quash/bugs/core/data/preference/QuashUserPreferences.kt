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

package com.quash.bugs.core.data.preference

import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.quash.bugs.core.data.dto.OrganisationUser
import javax.inject.Inject

class QuashUserPreferences @Inject constructor(private val sharedPreferences: SharedPreferences) {
    /**
     * Saves a list of OrganisationUser objects to SharedPreferences.
     * @param users List of users to save.
     */
    fun saveOrganisationUsers(users: List<OrganisationUser>) {
        val usersJson = Gson().toJson(users)
        val editor = sharedPreferences.edit()
        editor.putString("organisationUsers", usersJson)
        editor.apply()
    }

    /**
     * Retrieves a list of OrganisationUser objects from SharedPreferences.
     * @return List of OrganisationUser or empty list if no data found.
     */
    fun getOrganisationUsers(): List<OrganisationUser> {
        val usersJson = sharedPreferences.getString("organisationUsers", null) ?: return emptyList()
        val type = object : TypeToken<List<OrganisationUser>>() {}.type
        return Gson().fromJson(usersJson, type)
    }

    /**
     * Sets the session user ID in SharedPreferences.
     * @param userId The user ID to set.
     */
    fun setUserId(userId: String) {
        sharedPreferences.edit().putString("sessionUserId", userId).apply()
    }

    /**
     * Retrieves the session user ID from SharedPreferences.
     * @return The user ID, or an empty string if not found.
     */
    fun getUserId(): String = sharedPreferences.getString("sessionUserId", "") ?: ""
}
