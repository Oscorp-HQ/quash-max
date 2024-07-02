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

package com.quash.bugs.features.crash

import android.content.Context
import android.net.Uri
import android.os.Process
import android.util.Log
import com.quash.bugs.core.data.preference.QuashAppPreferences
import com.quash.bugs.core.flowcontrol.QuashFlowManager
import kotlinx.coroutines.CoroutineScope
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.PrintWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlin.system.exitProcess

/**
 * Handles uncaught exceptions within the application, logging them to Firebase Crashlytics,
 * saving crash logs locally, and performing necessary clean-up operations.
 *
 * @property context The application context used for accessing system services.
 * @property quashFlowManager An interface for starting services related to crash handling.
 */
class QuashCrashHandler(
    private val context: Context,
    private val quashFlowManager: QuashFlowManager
) : Thread.UncaughtExceptionHandler {

    @Inject
    lateinit var coroutineScope: CoroutineScope

    @Inject
    lateinit var quashAppPreferences: QuashAppPreferences

    private val defaultUEH = Thread.getDefaultUncaughtExceptionHandler()

    /**
     * Invoked when an uncaught exception is thrown. Determines if the crash is SDK-related
     * and handles it accordingly.
     *
     * @param t The thread that has an uncaught exception.
     * @param e The uncaught exception.
     */
    override fun uncaughtException(t: Thread, e: Throwable) {
        try {
            handleUncaughtException(t, e)
        } catch (exception: Exception) {
            Log.e("QuashCrashHandler", "Error within uncaughtException: ", exception)
            defaultUEH?.uncaughtException(t, exception)
        }
    }

    /**
     * Handles uncaught exceptions, differentiating between SDK-related and other crashes.
     *
     * @param t The thread that has an uncaught exception.
     * @param e The uncaught exception.
     */
    private fun handleUncaughtException(t: Thread?, e: Throwable?) {
        e?.let {
            if (isSdkCrash(it)) {
                logToCrashlytics(t, it)
                passToDefaultHandler(t, it)
            } else {
                val logcatUri = saveCrashLogcatToFile(it)
                saveLogsAndShowBottomSheet(logcatUri)
                safelyTerminateApplication()
            }
        }
    }

    /**
     * Logs crash details to Firebase Crashlytics.
     *
     * @param thread The thread in which the crash occurred.
     * @param throwable The uncaught exception.
     */
    private fun logToCrashlytics(thread: Thread?, throwable: Throwable) {
        quashFlowManager.logException(throwable, thread?.name ?: "Unknown thread")
    }

    /**
     * Passes the uncaught exception to the default uncaught exception handler.
     *
     * @param t The thread that has an uncaught exception.
     * @param e The uncaught exception.
     */
    private fun passToDefaultHandler(t: Thread?, e: Throwable?) {
        defaultUEH?.uncaughtException(t, e)
    }

    /**
     * Determines if the crash is related to the SDK by checking the stack trace.
     *
     * @param e The uncaught exception.
     * @return True if the crash is SDK-related, false otherwise.
     */
    private fun isSdkCrash(e: Throwable): Boolean {
        var cause: Throwable? = e
        while (cause != null) {
            if (cause.stackTrace.any { it.className.startsWith("com.quash.bugs") }) {
                return true
            }
            cause = cause.cause
        }
        return false
    }

    /**
     * Saves crash logs and shows a bottom sheet with the log details.
     *
     * @param filePath The URI of the saved crash log file.
     */
    private fun saveLogsAndShowBottomSheet(filePath: Uri?) {
        filePath?.let {
            quashFlowManager.startService { intent ->
                intent.action = "com.quash.bugs.action.CRASH_LOG_URI"
                intent.putExtra("crashLogUri", it)
            }
        }
    }

    /**
     * Saves the logcat output to a file when a crash occurs.
     *
     * @param e The uncaught exception.
     * @return The URI of the saved crash log file.
     */
    private fun saveCrashLogcatToFile(e: Throwable): Uri {
        return try {
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val filename = "CrashLogcat_$timestamp.txt"
            val externalFilesDir = context.applicationContext.getExternalFilesDir(null)
                ?: return Uri.EMPTY
            val outputFile = File(externalFilesDir, filename)

            FileOutputStream(outputFile).use { fos ->
                PrintWriter(fos).use { writer ->
                    writer.println("Crash Timestamp: $timestamp")
                    writer.println("Exception Type: ${e::class.java.simpleName}")
                    writer.println("Message: ${e.localizedMessage}\n")
                    writer.println("Stack Trace:")
                    e.printStackTrace(writer)
                    captureLogcat(writer)
                }
            }
            Uri.fromFile(outputFile)
        } catch (ioe: IOException) {
            Log.e("QuashCrashHandler", "Error saving crash log", ioe)
            Uri.EMPTY
        }
    }

    /**
     * Captures the logcat output and writes it to the provided writer.
     *
     * @param writer The PrintWriter to write the logcat output to.
     */
    private fun captureLogcat(writer: PrintWriter) {
        try {
            val process = Runtime.getRuntime().exec("logcat -d --pid=${Process.myPid()} -b crash")
            process.inputStream.bufferedReader().useLines { lines ->
                writer.println("\nLogcat Details:")
                lines.forEach { line -> writer.println(line) }
            }
            Runtime.getRuntime().exec("logcat -c -b crash")
        } catch (ioe: IOException) {
            Log.e("QuashCrashHandler", "Error capturing logcat", ioe)
        }
    }

    /**
     * Terminates the application safely after a non-SDK crash.
     */
    private fun safelyTerminateApplication() {
        Process.killProcess(Process.myPid())
        exitProcess(10)
    }
}
