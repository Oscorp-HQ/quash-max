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

package com.quash.bugs.presentation.bugreport.viewmodel

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.BatteryManager
import android.webkit.MimeTypeMap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quash.bugs.Quash
import com.quash.bugs.core.data.dto.*
import com.quash.bugs.core.data.local.QuashBitmapDao
import com.quash.bugs.core.data.local.QuashNetworkDao
import com.quash.bugs.core.data.local.QuashNetworkFilePath
import com.quash.bugs.core.data.preference.*
import com.quash.bugs.core.data.remote.RequestState
import com.quash.bugs.core.data.repository.QuashBugReportRepository
import com.quash.bugs.di.component.QuashRecorderComponent
import com.quash.bugs.features.recorder.QuashRecorder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.util.UUID
import javax.inject.Inject

/**
 * ViewModel for managing bug report operations.
 *
 * @property application Application context for accessing resources.
 * @property sharedPreferencesUtil Common preferences for shared settings.
 * @property quashAppPreferences Preferences for app-specific settings.
 * @property quashUserPreferences Preferences for user-specific settings.
 * @property quashBugReportPreferences Preferences for bug report settings.
 * @property bugReportRepository Repository for bug report operations.
 * @property quashNetworkDao DAO for accessing network data.
 * @property quashBitmapDao DAO for accessing bitmap data.
 * @property recorderFactory Factory for creating QuashRecorder components.
 */
class QuashBugReportViewModel @Inject constructor(
    application: Context,
    private val sharedPreferencesUtil: QuashCommonPreferences,
    private val quashAppPreferences: QuashAppPreferences,
    private val quashUserPreferences: QuashUserPreferences,
    private val quashBugReportPreferences: QuashBugReportPreferences,
    private val bugReportRepository: QuashBugReportRepository,
    private val quashNetworkDao: QuashNetworkDao,
    private val quashBitmapDao: QuashBitmapDao,
    private val recorderFactory: QuashRecorderComponent.Factory
) : ViewModel() {

    // Lazy initialization of QuashRecorder
    private val quashRecorder: QuashRecorder by lazy {
        val recorderComponent = recorderFactory.create(
            quality = QuashRecorder.ScreenshotQuality.MEDIUM,
            frequency = QuashRecorder.CaptureFrequency.MEDIUM,
            duration = 40
        )
        recorderComponent.getRecorder()
    }

    // LiveData for network file path
    private val _networkFilePathLiveData = MutableLiveData<QuashNetworkFilePath>()
    val networkFilePathLiveData: LiveData<QuashNetworkFilePath>
        get() = _networkFilePathLiveData

    // Initial setup
    init {
        if (sharedPreferencesUtil.isNetworkLogEnabled()) {
            getNetworkLogFilePath()
        }
    }

    // StateFlow for holding and updating bug report information
    private val _bugReportInfo = MutableStateFlow(
        BugReportInfo(
            "", "", "", "", "", emptyList(), emptyList(), crashLog = null,
            deviceMetadata = QuashDeviceMetadata().copy(batteryLevel = getBatteryLevel())
        )
    )

    // Get current battery level
    private fun getBatteryLevel(): String {
        val batteryIntent =
            Quash.context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val batteryPct = (level / scale.toFloat() * 100).toInt()
        return "$batteryPct%"
    }

    val bugReport = _bugReportInfo.asStateFlow()
    val sessionID = UUID.randomUUID().toString()

    // LiveData for holding the request state of bug report
    private val _requestStateBugReport = MutableLiveData<RequestState<ReportBugResponse>>()
    val requestStateBugReport: LiveData<RequestState<ReportBugResponse>> get() = _requestStateBugReport

    // LiveData for holding the request state of users
    private val _requestStateUsers = MutableLiveData<RequestState<UserResponse>>()
    val requestStateUsers: LiveData<RequestState<UserResponse>> get() = _requestStateUsers

    // Initial setup to load bug report information and fetch users
    init {
        _bugReportInfo.value = quashBugReportPreferences.loadBugReportInfo()
        getUsers()
    }

    /**
     * Save the current bug report data to preferences.
     */
    fun saveData() {
        _bugReportInfo.value.let { bugReportData ->
            quashBugReportPreferences.saveBugReportInfo(bugReportData)
        }
    }

    /**
     * Update the bug report data and print current state.
     *
     * @param update Lambda function to update the BugReportInfo.
     */
    fun updateBugReport(update: BugReportInfo.() -> Unit) {
        _bugReportInfo.value.apply(update)
        printCurrentRequestState()
    }

    // Print the current request state for debugging
    private fun printCurrentRequestState() {
        viewModelScope.launch {
            bugReport.collect { bugReportInfo ->
                println("Bug Report Info: $bugReportInfo")
            }
        }
    }

    /**
     * Transform field values to a specific string format.
     *
     * @param toString The field value to transform.
     * @return Transformed string value.
     */
    fun transformFields(toString: String): String {
        return when (toString) {
            "UI Improvement" -> "UI"
            "Bug" -> "BUG"
            "Crash" -> "CRASH"
            else -> "BUG"
        }
    }

    /**
     * Report the bug using the current bug report information.
     */
    fun reportBug() {
        val currentBugReport = _bugReportInfo.value

        val bugReportInfo = BugReportInfo(
            title = currentBugReport.title,
            description = currentBugReport.description,
            type = currentBugReport.type,
            priority = currentBugReport.priority,
            mediaFiles = checkEmptyPlaceHolderUriAndReturn(
                currentBugReport.mediaFiles.distinct().map { normalizeUri(it) }),
            audioFiles = currentBugReport.audioFiles.distinct().map { normalizeUri(it) },
            reporterId = currentBugReport.reporterId,
            crashLog = currentBugReport.crashLog,
            appId = quashAppPreferences.getAppId()!!,
            deviceMetadata = QuashDeviceMetadata().copy(batteryLevel = getBatteryLevel())
        )

        viewModelScope.launch {
            reportBugWithInfo(bugReportInfo)
        }
    }

    // Filter out placeholder URIs
    private fun checkEmptyPlaceHolderUriAndReturn(map: List<Uri>): List<Uri> {
        val placeholderUriString = "placeholder://icon"
        return map.filterNot { it.toString() == placeholderUriString }
    }

    // Report bug with detailed information
    private suspend fun reportBugWithInfo(bugReportInfo: BugReportInfo) {
        val mediaTypedByteArrays = convertUrisToByteArrayCombine(
            bugReportInfo.mediaFiles,
            bugReportInfo.audioFiles,
            contentResolver
        )

        val crashLogTypedByteArray = convertCrashLogUriToTypedByteArray(
            bugReportInfo.crashLog,
            contentResolver
        )

        bugReportRepository.reportBug(bugReportInfo, mediaTypedByteArrays, crashLogTypedByteArray)
            .collect { response ->
                _requestStateBugReport.value = response
            }
    }

    // Convert crash log URI to TypedByteArray
    private fun convertCrashLogUriToTypedByteArray(
        crashLogUri: Uri?,
        contentResolver: ContentResolver
    ): TypedByteArray? {
        if (crashLogUri == null) return null
        val mimeType = "text/plain"
        return convertUriToTypedByteArray(crashLogUri, mimeType, contentResolver)
    }

    // Normalize a URI to handle "file://" prefix
    private fun normalizeUri(uri: Uri): Uri {
        val uriString = uri.toString()
        val normalizedPath = if (uriString.startsWith("file://")) {
            uriString.substring(7)
        } else {
            uriString
        }
        return Uri.parse(normalizedPath)
    }

    /**
     * Convert media and audio URIs to TypedByteArray.
     *
     * @param mediaUris List of media URIs.
     * @param audioUris List of audio URIs.
     * @param contentResolver Content resolver for accessing media files.
     * @return List of TypedByteArray.
     */
    private fun convertUrisToByteArrayCombine(
        mediaUris: List<Uri>,
        audioUris: List<Uri>,
        contentResolver: ContentResolver
    ): List<TypedByteArray> {
        val combinedByteArrays = mutableListOf<TypedByteArray>()

        // Convert media URIs (image/video) to TypedByteArray
        mediaUris.forEach { uri ->
            val mimeType = if (uri.scheme == "content") {
                contentResolver.getType(uri)
            } else {
                val fileExtension = File(uri.path ?: "").extension
                MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension)
            }
            val typedByteArray = convertUriToTypedByteArray(uri, mimeType ?: "", contentResolver)
            typedByteArray?.let { combinedByteArrays.add(it) }
        }

        // Convert audio URIs to TypedByteArray
        audioUris.forEach { uri ->
            val mimeType = "audio/mpeg"
            val typedByteArray = convertUriToTypedByteArray(uri, mimeType, contentResolver)
            typedByteArray?.let { combinedByteArrays.add(it) }
        }

        return combinedByteArrays
    }

    /**
     * Convert a single URI to TypedByteArray.
     *
     * @param uri The URI to convert.
     * @param mimeType The MIME type of the URI.
     * @param contentResolver Content resolver for accessing media files.
     * @return TypedByteArray or null if conversion fails.
     */
    private fun convertUriToTypedByteArray(
        uri: Uri,
        mimeType: String,
        contentResolver: ContentResolver
    ): TypedByteArray? {
        return if (uri.scheme == "content") {
            // Handle content URIs
            contentResolver.openInputStream(uri)?.use { inputStream ->
                val byteArray = inputStream.readBytes()
                TypedByteArray(byteArray, mimeType)
            }
        } else {
            // Handle file URIs
            val fileUri = uri.path ?: return null
            val byteArray = readFileToByteArray(fileUri) ?: return null
            TypedByteArray(byteArray, mimeType)
        }
    }

    /**
     * Helper function to read a file into a byte array.
     *
     * @param fileUri The URI of the file to read.
     * @return Byte array or null if reading fails.
     */
    private fun readFileToByteArray(fileUri: String): ByteArray? {
        return try {
            val file = File(fileUri)
            val fileInputStream = FileInputStream(file)
            val bytes = ByteArray(file.length().toInt())
            fileInputStream.read(bytes)
            fileInputStream.close()
            bytes
        } catch (e: IOException) {
            null
        }
    }

    // Content resolver for accessing media files
    private val contentResolver: ContentResolver = application.contentResolver

    // Fetch users from the repository or cache
    private fun getUsers() {
        getCachedUsers()?.let {
            _requestStateUsers.value = RequestState.Successful(it)
        } ?: run {
            viewModelScope.launch {
                bugReportRepository.getUsers(quashAppPreferences.getOrgKey())
                    .collect { response ->
                        _requestStateUsers.value = response
                    }
            }
        }
    }

    // Get cached users from preferences
    private fun getCachedUsers(): UserResponse? {
        return try {
            val users = quashUserPreferences.getOrganisationUsers()
            if (users.isEmpty()) throw Exception()
            val organisationData = OrganisationData("cached Organisation", users)
            UserResponse(true, "cached Response", organisationData)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Get network log file path from DAO
    private fun getNetworkLogFilePath() {
        viewModelScope.launch(Dispatchers.IO) {
            val data = quashNetworkDao.getQuashFilePath()
            data?.let { file ->
                withContext(Dispatchers.Main) {
                    _networkFilePathLiveData.postValue(file)
                }
            }
        }
    }

    /**
     * Clear GIFs from recorder and bitmap DAO.
     */
    fun clearGifs() {
        viewModelScope.launch {
            quashRecorder.clearRecorder()
            quashBitmapDao.clearAll()
        }
    }
}
