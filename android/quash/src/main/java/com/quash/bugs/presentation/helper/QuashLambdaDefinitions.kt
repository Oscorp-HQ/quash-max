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

package com.quash.bugs.presentation.helper

import android.net.Uri
import com.quash.bugs.core.data.dto.Media
import com.quash.bugs.core.data.dto.Report

// Define lambda type for media option selection
typealias OnMediaOptionSelected = (Int) -> Unit

// Define lambda type for reporter name selection
typealias OnReporterNameSelected = (String, String) -> Unit

// Define lambda type for adding an audio recording
typealias onAudioRecordingAdded = (Uri) -> Unit

// Define lambda type for attachment removal
typealias OnAttachmentRemoved = (Int, String) -> Unit

// Define lambda type for opening an attachment
typealias OnAttachmentOpened = (Uri) -> Unit

// Define lambda type for list action in editing bugs
typealias OnListAction = (String, Boolean) -> Unit

// Define lambda type for editing a bug
typealias EditBugListener = (Report, Boolean) -> Unit

// Define lambda type for editing audio
typealias EditAudioListener = (Media) -> Unit

// Define lambda type for closing audio
typealias OnAudioCloseListener = (Uri) -> Unit

// Define lambda type for viewing media
typealias ViewMediaListener = (String, Boolean) -> Unit

// Define lambda type for selecting priority
typealias PriorityListener = (Int, String) -> Unit

// Define lambda type for actions after permissions are granted
typealias AfterPermissionGranted = () -> Unit
