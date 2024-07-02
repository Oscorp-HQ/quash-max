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
import com.quash.bugs.core.data.dto.EditBugInfo
import javax.inject.Inject

/**
 * Manages preferences related to editing bug reports.
 *
 * @param sharedPreferences The SharedPreferences instance.
 */
class QuashEditBugPreferences @Inject constructor(sharedPreferences: SharedPreferences) :
    QuashPreferencesManager(sharedPreferences) {

    companion object {
        private const val ED_REPORTER_ID_KEY = "edReporterId"
        private const val ED_TITLE_KEY = "edTitle"
        private const val ED_BUG_ID_KEY = "edBugId"
        private const val ED_DESCRIPTION_KEY = "edDescription"
        private const val ED_TYPE_KEY = "edType"
        private const val ED_MEDIA_FILES_KEY = "edMediaFiles"
        private const val ED_AUDIO_FILES_KEY = "edAudioFiles"
        private const val ED_REMOVED_MEDIA_ID = "edRemovedMedia"
        private const val ORIGINAL_MEDIA_ID = "originalMedia"
        private const val ED_USER = "edUser"
        private const val ED_PRIORITY = "edPriority"
    }

    /**
     * Saves information about an edited bug report to SharedPreferences.
     *
     * @param editBugInfo EditBugInfo object containing updated details about the bug.
     */
    fun saveEditBugInfo(editBugInfo: EditBugInfo) {
        with(sharedPreferences.edit()) {
            putString(ED_REPORTER_ID_KEY, editBugInfo.edReporterId)
            putString(ED_TITLE_KEY, editBugInfo.edTitle)
            putString(ED_BUG_ID_KEY, editBugInfo.bugId)
            putString(ED_DESCRIPTION_KEY, editBugInfo.edDescription)
            putString(ED_TYPE_KEY, editBugInfo.edType)
            putString(
                ED_MEDIA_FILES_KEY,
                editBugInfo.edMediaFiles.joinToString(",") { it.toString() })
            putString(
                ED_AUDIO_FILES_KEY,
                editBugInfo.edAudioFiles.joinToString(",") { it.toString() })
            putString(ED_REMOVED_MEDIA_ID, editBugInfo.removedMedia.joinToString(",") { it })
            putString(ORIGINAL_MEDIA_ID, editBugInfo.originalMediaJson)
            putString(ED_USER, editBugInfo.user)
            putString(ED_PRIORITY, editBugInfo.edpriority)
            apply()
        }
    }

    /**
     * Loads information about an edited bug report from SharedPreferences.
     *
     * @return EditBugInfo object populated with stored values.
     */
    fun loadEditBugInfo(): EditBugInfo {
        return EditBugInfo(
            retrieveString(ED_BUG_ID_KEY),
            retrieveString(ED_TITLE_KEY),
            retrieveString(ED_DESCRIPTION_KEY),
            retrieveString(ED_TYPE_KEY),
            retrieveUriList(ED_MEDIA_FILES_KEY),
            retrieveAudioFiles(ED_AUDIO_FILES_KEY),
            retrieveString(ED_REPORTER_ID_KEY),
            retrieveRemovedFiles(ED_REMOVED_MEDIA_ID),
            retrieveString(ORIGINAL_MEDIA_ID),
            retrieveString(ED_USER),
            retrieveString(ED_PRIORITY)
        )
    }
}
