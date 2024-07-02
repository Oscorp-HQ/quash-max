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
import com.quash.bugs.core.data.dto.BugReportInfo
import javax.inject.Inject

/**
 * Manages preferences related to bug reports.
 *
 * @param sharedPreferences The SharedPreferences instance.
 */
class QuashBugReportPreferences @Inject constructor(sharedPreferences: SharedPreferences) :
    QuashPreferencesManager(sharedPreferences) {

    companion object {
        private const val REPORTER_ID_KEY = "reporterId"
        private const val TITLE_KEY = "title"
        private const val DESCRIPTION_KEY = "description"
        private const val TYPE_KEY = "type"
        private const val MEDIA_FILES_KEY = "mediaFiles"
        private const val AUDIO_FILES_KEY = "audioFiles"
        private const val PRIORITY = "priority"
    }

    /**
     * Saves details about a bug report to SharedPreferences.
     *
     * @param bugReportInfo BugReportInfo object containing details about the bug.
     */
    fun saveBugReportInfo(bugReportInfo: BugReportInfo) {
        with(sharedPreferences.edit()) {
            putString(REPORTER_ID_KEY, bugReportInfo.reporterId)
            putString(TITLE_KEY, bugReportInfo.title)
            putString(DESCRIPTION_KEY, bugReportInfo.description)
            putString(TYPE_KEY, bugReportInfo.type)
            putString(PRIORITY, bugReportInfo.priority)
            putString(MEDIA_FILES_KEY, bugReportInfo.mediaFiles.joinToString(",") { it.toString() })
            putString(AUDIO_FILES_KEY, bugReportInfo.audioFiles.joinToString(",") { it.toString() })
            apply()
        }
    }

    /**
     * Retrieves stored bug report details from SharedPreferences.
     *
     * @return BugReportInfo object populated with stored values.
     */
    fun loadBugReportInfo(): BugReportInfo {
        return BugReportInfo(
            retrieveString(REPORTER_ID_KEY),
            retrieveString(TITLE_KEY),
            retrieveString(DESCRIPTION_KEY),
            retrieveString(TYPE_KEY),
            retrieveString(PRIORITY),
            retrieveUriList(MEDIA_FILES_KEY),
            retrieveAudioFiles(AUDIO_FILES_KEY),
            crashLog = null
        )
    }
}
