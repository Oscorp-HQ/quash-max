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

package com.quash.bugs.presentation.permission

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.quash.bugs.R
import com.quash.bugs.core.data.preference.QuashCommonPreferences
import com.quash.bugs.core.di.module.QuashCoreModule
import com.quash.bugs.di.component.DaggerQuashComponent
import com.quash.bugs.service.QuashWatchdogService
import javax.inject.Inject

/**
 * Activity to request and manage notification permissions required by the app.
 * Displays dialogs to request the user to grant notification permissions.
 */
class PermissionRequestActivity : AppCompatActivity() {

    private val NEVER_SHOW_PERMISSION_DIALOG: String = "key"

    @Inject
    lateinit var sharedPreferencesUtil: QuashCommonPreferences

    private var permissionDialog: Dialog? = null

    /**
     * Registers a launcher for the notification permission request.
     */
    @SuppressLint("ObsoleteSdkInt")
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startQuashService()
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !shouldShowRequestPermissionRationale(
                    Manifest.permission.POST_NOTIFICATIONS
                )
            ) {
                showPermissionDeniedDialog(true)
            } else {
                showPermissionDeniedDialog(false)
            }
        }
    }

    /**
     * Called when the activity is starting.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
        injectDependencies()
        checkPermissionsAndProceed()
        Log.e("QuashSDK", "PermissionRequestActivity onCreate")
    }

    /**
     * Injects dependencies using Dagger.
     */
    private fun injectDependencies() {
        DaggerQuashComponent.builder().quashCoreModule(QuashCoreModule(this)).build().inject(this)
    }

    /**
     * Checks if the permission dialog should be shown.
     */
    private fun shouldShowDialog(): Boolean {
        return sharedPreferencesUtil.getBoolean(NEVER_SHOW_PERMISSION_DIALOG, true)
    }

    /**
     * Displays the permission dialog to request notification permission.
     */
    @SuppressLint("InlinedApi")
    private fun showPermissionDialog() {
        val builder = AlertDialog.Builder(this, R.style.MaterialAlertDialog)

        val dialogView = layoutInflater.inflate(R.layout.quash_dialog_permission, null)
        builder.setView(dialogView)

        val notificationButton = dialogView.findViewById<Button>(R.id.buttonNotification)
        val notificationDescription =
            dialogView.findViewById<TextView>(R.id.textNotificationDescription)
        val neverShowButton = dialogView.findViewById<Button>(R.id.buttonNeverShow)

        val notificationPermissionGranted = hasPermission(Manifest.permission.POST_NOTIFICATIONS)

        notificationDescription.isVisible = !notificationPermissionGranted
        notificationDescription.text =
            "Notification permission is needed to show a persistent taskbar menu for quick access to reported bugs."

        notificationButton.isVisible = !notificationPermissionGranted
        notificationButton.setOnClickListener {
            permissionDialog?.dismiss()
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        neverShowButton.isVisible = !notificationPermissionGranted
        neverShowButton.setOnClickListener {
            sharedPreferencesUtil.putBoolean(NEVER_SHOW_PERMISSION_DIALOG, false)
            permissionDialog?.dismiss()
            startQuashService()
        }

        permissionDialog = builder.create()
        permissionDialog?.setOnDismissListener {
            permissionDialog = null
        }
        permissionDialog?.setCancelable(false)
        permissionDialog?.show()
    }

    /**
     * Displays a dialog when permission is denied.
     *
     * @param shouldOpenSettings Whether the dialog should provide an option to open app settings.
     */
    private fun showPermissionDeniedDialog(shouldOpenSettings: Boolean) {
        val builder = AlertDialog.Builder(this, R.style.MaterialAlertDialogSimple)
        builder.setTitle("Permission Denied")

        if (shouldOpenSettings) {
            builder.setMessage("You have denied the required permissions. Please go to the app settings and grant the permissions.")
            builder.setPositiveButton("Open Settings") { dialog, _ ->
                dialog.dismiss()
                openAppSettings()
            }
        } else {
            builder.setMessage("Some features may not work properly without the required permissions.")
            builder.setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
                checkPermissionsAndProceed()
            }
        }

        builder.show()
    }

    /**
     * Registers a launcher for opening app settings.
     */
    private val appSettingsLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            startQuashService()
        }

    /**
     * Opens the app settings screen.
     */
    private fun openAppSettings() {
        val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        startActivity(intent)
        appSettingsLauncher.launch(intent)
    }

    /**
     * Checks if a specific permission is granted.
     *
     * @param permission The permission to check.
     * @return True if the permission is granted, false otherwise.
     */
    private fun hasPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Checks the required permissions and proceeds accordingly.
     */
    private fun checkPermissionsAndProceed() {
        if (permissionsGranted()) {
            startQuashService()
        } else if (shouldShowDialog()) {
            showPermissionDialog()
        } else {
            startQuashService()
        }
    }

    /**
     * Checks if the required permissions are granted.
     *
     * @return True if permissions are granted, false otherwise.
     */
    @SuppressLint("InlinedApi")
    private fun permissionsGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Starts the QuashWatchdogService.
     */
    private fun startQuashService() {
        try {
            Log.d("QuashSDK", "Attempting to start Quash Service")
            Intent(this, QuashWatchdogService::class.java).also { intent ->
                // You can consider adding intent flags if needed
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    Log.d("QuashSDK", "Starting foreground service")
                    startForegroundService(intent)
                } else {
                    Log.d("QuashSDK", "Starting service")
                    startService(intent)
                }
            }
            Log.d("QuashSDK", "Quash Service started successfully")
        } catch (e: Exception) {
            Log.e("QuashSDK", "Error starting Quash Service", e)
        }
        finish()
    }
}
