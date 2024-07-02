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

package com.quash.bugs.service

import android.app.ActivityOptions
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.quash.bugs.Quash
import com.quash.bugs.R
import com.quash.bugs.core.data.local.QuashBitmapDao
import com.quash.bugs.core.data.local.QuashBufferItem
import com.quash.bugs.core.di.module.QuashCoreModule
import com.quash.bugs.di.component.DaggerQuashComponent
import com.quash.bugs.presentation.buglist.activities.QuashBugListActivity
import com.quash.bugs.presentation.permission.QuashCrashNotifierActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Service for handling background operations related to screen recording and crash reporting.
 * This service is responsible for managing and storing file paths and launching the crash notifier activity.
 */
class QuashWatchdogService : Service() {

    @Inject
    lateinit var quashBitmapDao: QuashBitmapDao

    private lateinit var ioScope: CoroutineScope

    init {
        DaggerQuashComponent.builder().quashCoreModule(QuashCoreModule(Quash.context)).build()
            .inject(this)
    }

    /**
     * Initializes the service by creating the notification channel and foreground notification.
     */
    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate() {
        super.onCreate()
        ioScope = CoroutineScope(Dispatchers.IO)
        createNotificationChannel()
        val notification = createNotification()
        startForeground(NOTIFICATION_ID, notification)
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    /**
     * Responds to service start commands by processing intent actions to save file paths or display crash dialogs.
     * This method directs the service's response to intents based on their actions, ensuring appropriate handling
     * of file paths and crash information.
     *
     * @param intent The Intent supplied to `startService`, containing the action and potentially extra data.
     * @param flags Additional data about this start request.
     * @param startId A unique integer representing this specific request to start.
     * @return The redelivery preference. Either START_NOT_STICKY, START_STICKY, or START_REDELIVER_INTENT.
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.action?.let { action ->
            when (action) {
                "com.quash.bugs.action.SAVE_FILE_PATH" -> {
                    intent.getStringExtra("filePath")?.let(this::saveFilePathToDatabase)
                }

                "com.quash.bugs.action.CRASH_LOG_URI" -> {
                    intent.getParcelableExtra<Uri>("crashLogUri")?.let(this::showCrashDialog)
                }

                else -> {}
            }
        }
        return START_STICKY
    }


    /**
     * Saves file paths to the database using the QuashBitmapDao.
     */
    private fun saveFilePathToDatabase(filePath: String) {
        ioScope.launch {
            try {
                val timestamp = System.currentTimeMillis()
                quashBitmapDao.insert(QuashBufferItem(filePath = filePath, timestamp = timestamp))
            } catch (e: Exception) {
                Log.e("QuashWatchdogService", "Failed to save file path", e)
            }
        }
    }


    /**
     * Shows a dialog for crashes by launching QuashCrashNotifierActivity.
     */
    private fun showCrashDialog(crashLogUri: Uri) {
        val intent = Intent(this, QuashCrashNotifierActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("crashLogUri", crashLogUri.toString())
        }
        startActivity(intent, ActivityOptions.makeCustomAnimation(this, 0, 0).toBundle())
    }

    /**
     * Creates a notification channel for the service running in the foreground.
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
        }
    }

    /**
     * Creates a notification for running the service in the foreground.
     */
    private fun createNotification(): Notification {
        val intent = Intent(this, QuashBugListActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent =
            PendingIntent.getActivity(
                this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.quash_ic_quash_notification)
            .setContentIntent(pendingIntent)
            .setContentTitle("Quash Service")
            .setContentText("Tap to inspect the reported issues")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)
            .build()
    }

    /**
     * Cancels all ongoing operations and cleans up resources when the service is destroyed.
     */
    override fun onDestroy() {
        super.onDestroy()
        ioScope.cancel()
    }

    /**
     * Stops the service when the task is removed from the recent apps list.
     */
    override fun onTaskRemoved(rootIntent: Intent?) {
        stopSelf()
        super.onTaskRemoved(rootIntent)
    }

    companion object {
        private const val NOTIFICATION_ID = 2203
        private const val CHANNEL_ID = "quash_channel_01"
        private const val CHANNEL_NAME = "Quash Reporting Channel"
    }
}
