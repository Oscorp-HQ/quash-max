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

package com.quash.bugs.core.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File
import java.io.FileOutputStream

/**
 * Utility object for saving and loading bitmap images to and from files.
 */
object QuashBitmapUtils {

    /**
     * Saves a bitmap to a file in the application's files directory.
     *
     * @param context The context to access the application's file directory.
     * @param bitmap The bitmap to save.
     * @return The absolute path of the saved file.
     */
    fun saveBitmapToFile(context: Context, bitmap: Bitmap): String {
        val fileName =
            "bitmap_${System.currentTimeMillis()}.png"  // Generate a unique file name based on the current time
        val file =
            File(context.filesDir, fileName)  // Create a new file in the app's file directory
        FileOutputStream(file).use { out ->  // Use a FileOutputStream to write the bitmap to the file
            bitmap.compress(
                Bitmap.CompressFormat.PNG,
                100,
                out
            )  // Compress the bitmap into PNG format and write it to the file
        }
        return file.absolutePath  // Return the absolute path of the saved file
    }

    /**
     * Loads a bitmap from a file.
     *
     * @param filePath The path of the file to load the bitmap from.
     * @return The loaded bitmap.
     */
    fun loadBitmapFromFile(filePath: String): Bitmap {
        return BitmapFactory.decodeFile(filePath)  // Decode and return the bitmap from the file at the given path
    }
}
