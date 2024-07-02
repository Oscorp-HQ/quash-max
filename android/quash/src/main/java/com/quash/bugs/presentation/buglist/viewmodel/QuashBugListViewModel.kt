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

package com.quash.bugs.presentation.buglist.viewmodel

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.quash.bugs.core.data.dto.BugListResponse
import com.quash.bugs.core.data.dto.EditBugInfo
import com.quash.bugs.core.data.dto.ReportBugResponse
import com.quash.bugs.core.data.dto.TypedByteArray
import com.quash.bugs.core.data.preference.QuashEditBugPreferences
import com.quash.bugs.core.data.remote.RequestState
import com.quash.bugs.core.data.repository.QuashBugListRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import javax.inject.Inject

/**
 * ViewModel for managing bug list and bug report operations.
 *
 * @property repository Repository for bug list operations.
 * @property quashEditBugPreferences Preferences for saving and loading bug edit information.
 * @property contentResolver Content resolver for accessing media files.
 */
class QuashBugListViewModel @Inject constructor(
    application: Context,
    private val repository: QuashBugListRepository,
    private val quashEditBugPreferences: QuashEditBugPreferences
) : ViewModel() {

    // StateFlow for holding and updating bug edit information
    private val _editBugInfo =
        MutableStateFlow(EditBugInfo("", "", "", "", emptyList(), emptyList(), "", emptyList(), ""))
    val editBug = _editBugInfo.asStateFlow()

    // LiveData for holding the request state of editing a bug
    private val _requestStateEditBug = MutableLiveData<RequestState<ReportBugResponse>>()
    val requestStateEditBug: LiveData<RequestState<ReportBugResponse>> get() = _requestStateEditBug

    // Initialize the ViewModel by loading saved edit bug information
    init {
        _editBugInfo.value = quashEditBugPreferences.loadEditBugInfo()
    }

    // LiveData for holding the request state of deleting a bug
    private val _requestStateDeleteBug = MutableLiveData<RequestState<BugListResponse>>()
    val requestStateDeleteBug: LiveData<RequestState<BugListResponse>> get() = _requestStateDeleteBug

    /**
     * Get the bug list with caching.
     */
    fun getBugList() = repository.getBugList().cachedIn(viewModelScope)

    /**
     * Delete a bug by its report ID.
     *
     * @param reportId The ID of the bug report to delete.
     */
    fun deleteBug(reportId: String) {
        viewModelScope.launch {
            repository.deleteBug(reportId).collect { response ->
                _requestStateDeleteBug.value = response
            }
        }
    }

    /**
     * Save the current bug report data to preferences.
     */
    private fun saveData() {
        _editBugInfo.value.let { bugReportData ->
            quashEditBugPreferences.saveEditBugInfo(bugReportData)
        }
    }

    /**
     * Update the bug report data and save it.
     *
     * @param update Lambda function to update the EditBugInfo.
     */
    fun updateBugReport(update: EditBugInfo.() -> Unit) {
        _editBugInfo.value.apply(update)
        saveData()
    }

    /**
     * Update a bug with detailed information.
     *
     * @param reportId The ID of the bug report.
     * @param title The title of the bug.
     * @param description The description of the bug.
     * @param type The type of the bug.
     * @param priority The priority of the bug.
     * @param reporterId The ID of the reporter.
     * @param removedId List of IDs to be removed.
     * @param media List of media URIs.
     * @param audio List of audio URIs.
     */
    fun updateBug(
        reportId: String,
        title: String,
        description: String,
        type: String,
        priority: String,
        reporterId: String,
        removedId: List<String>,
        media: List<Uri>,
        audio: List<Uri>
    ) {
        val mediaTypedByteArrays = convertUrisToByteArrayCombine(media.distinct().map { normalizeUri(it) }, audio)
        viewModelScope.launch {
            repository.updateBug(
                reportId,
                title,
                description,
                type,
                priority,
                reporterId,
                removedId,
                mediaTypedByteArrays
            ).collect { response ->
                _requestStateEditBug.value = response
            }
        }
    }

    /**
     * Convert media and audio URIs to TypedByteArray.
     *
     * @param mediaUris List of media URIs.
     * @param audioUris List of audio URIs.
     * @return List of TypedByteArray.
     */
    private fun convertUrisToByteArrayCombine(
        mediaUris: List<Uri>,
        audioUris: List<Uri>
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

    /**
     * Normalize a URI to handle "file://" prefix.
     *
     * @param uri The URI to normalize.
     * @return Normalized URI.
     */
    private fun normalizeUri(uri: Uri): Uri {
        val uriString = uri.toString()
        val normalizedPath = if (uriString.startsWith("file://")) {
            uriString.substring(7)
        } else {
            uriString
        }
        return Uri.parse(normalizedPath)
    }

    // Content resolver for accessing media files
    private val contentResolver: ContentResolver = application.contentResolver
}
