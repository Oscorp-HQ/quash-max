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

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder

object QuashPermissionUtil {

    const val AUDIO_PERMISSION_REQUEST_CODE = 1
    const val SCREEN_RECORD_PERMISSION_REQUEST_CODE = 3
    const val SCREENSHOT_PERMISSION_REQUEST_CODE = 4

    /**
     * Enum class for various permission types.
     */
    enum class PermissionType(val permission: String) {
        RECORD_AUDIO(Manifest.permission.RECORD_AUDIO)
    }

    /**
     * Requests the specified permissions.
     */
    fun requestPermissions(
        activity: AppCompatActivity,
        vararg permissions: PermissionType,
        requestCode: Int
    ) {
        val permissionArray = permissions.map { it.permission }.toTypedArray()
        ActivityCompat.requestPermissions(activity, permissionArray, requestCode)
    }

    /**
     * Checks if the specified permissions are granted.
     */
    fun hasPermissions(context: Context, vararg permissions: PermissionType): Boolean {
        return permissions.all { permissionType ->
            ContextCompat.checkSelfPermission(
                context,
                permissionType.permission
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    /**
     * Shows a dialog explaining why the permission is needed and directs to app settings if permanently denied.
     */
    private fun showPermissionDialog(
        context: Context,
        permanentlyDenied: Boolean,
        permissions: Array<String>,
        requestCode: Int
    ) {
        val builder = MaterialAlertDialogBuilder(context).apply {
            setTitle("Permission Required")
            setMessage(
                if (permanentlyDenied) {
                    "This permission is required for the app to function properly. Please enable it from app settings."
                } else {
                    "This permission is required for the app to function properly."
                }
            )
            setPositiveButton(
                if (permanentlyDenied) "Go to Settings" else "Grant Permission"
            ) { _, _ ->
                if (permanentlyDenied) {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                    context.startActivity(intent)
                } else {
                    ActivityCompat.requestPermissions(context as Activity, permissions, requestCode)
                }
            }
            setNegativeButton("Cancel", null)
        }
        builder.show()
    }

    /**
     * Handles the permission result by showing a dialog if needed.
     */
    fun handlePermissionResult(
        context: Context,
        requestCode: Int,
        permanentlyDenied: Boolean,
        permissions: Array<String>
    ) {
        showPermissionDialog(context, permanentlyDenied, permissions, requestCode)
    }
}
