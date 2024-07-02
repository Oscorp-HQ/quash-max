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

import android.content.res.Resources
import android.os.Build
import androidx.annotation.Keep

/**
 * Data class representing the metadata about the device.
 *
 * @property deviceName The name of the device.
 * @property osVersion The version of the operating system.
 * @property screenResolution The screen resolution of the device.
 * @property batteryLevel The battery level of the device.
 */
@Keep
data class QuashDeviceMetadata(
    val deviceName: String = "${Build.MANUFACTURER} ${Build.MODEL}",
    val osVersion: String = Build.VERSION.RELEASE,
    val screenResolution: String = "${Resources.getSystem().displayMetrics.widthPixels}x${Resources.getSystem().displayMetrics.heightPixels}",
    val batteryLevel: String? = null
)


