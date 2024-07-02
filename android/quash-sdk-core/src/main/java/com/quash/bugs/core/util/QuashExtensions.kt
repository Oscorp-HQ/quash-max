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

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.os.Parcelable
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.core.view.forEach
import coil.ImageLoader
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.google.gson.Gson
import com.quash.bugs.core.data.dto.QuashNetworkData
import com.quash.bugs.core.data.dto.QuashPriority
import java.io.File

/**
 * Extension function for EditText to listen for text changes after the text has been changed.
 * @param afterTextChanged lambda function to be invoked after the text has changed.
 */
fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        }

        override fun afterTextChanged(editable: Editable?) {
            afterTextChanged.invoke(editable.toString())
        }
    })
}

/**
 * Extension function for Context to get the application name.
 * @return the application name as a String.
 */
fun Context.getAppName(): String = applicationInfo.loadLabel(packageManager).toString()

/**
 * Extension function for String to get the initials of the string.
 * @param limit the maximum number of initials to return (default is 2).
 * @return the initials of the string as a String.
 */
fun String.asInitials(limit: Int = 2): String {
    val buffer = StringBuffer()
    trim().split(" ").filter {
        it.isNotEmpty()
    }.joinTo(
        buffer = buffer,
        limit = limit,
        separator = "",
        truncated = "",
    ) { s ->
        s.first().uppercase()
    }
    return buffer.toString()
}

/**
 * Function to load a video thumbnail into an ImageView.
 * @param imageView the ImageView to load the thumbnail into.
 * @param uri the Uri of the video.
 */
fun loadVideoThumbnail(imageView: ImageView, uri: String) {
    val retriever = MediaMetadataRetriever()
    retriever.setDataSource(uri, HashMap())
    val thumbnail: Bitmap? = retriever.getFrameAtTime(0)

    thumbnail?.let {
        imageView.setImageBitmap(it)
    }
    retriever.release()
}

/**
 * Extension function for ImageView to load a video thumbnail.
 * @param uri the Uri of the video.
 */
fun ImageView.loadVideoThumbnail(uri: Uri) {
    val retriever = MediaMetadataRetriever()
    try {
        retriever.setDataSource(context, uri)
        val thumbnail: Bitmap? = retriever.getFrameAtTime(0)
        thumbnail?.let {
            this.setImageBitmap(it)
        }
    } catch (e: Exception) {
        // Handle exceptions here
    } finally {
        retriever.release()
    }
}

/**
 * Extension function for ImageView to load an image using Coil library.
 * @param uri the Uri of the image.
 */
fun ImageView.loadImageWithCoil(uri: Uri) {
    // Initialize your ImageLoader here if you haven't
    val imageLoader = ImageLoader.Builder(context).build()

    val request = ImageRequest.Builder(context)
        .data(uri)
        .memoryCachePolicy(CachePolicy.ENABLED)  // Enable memory cache
        .diskCachePolicy(CachePolicy.ENABLED)
        .target(this)
        // any other configurations
        .build()

    imageLoader.enqueue(request)
}

/**
 * Extension function for ContentResolver to check if a file size is within a certain limit.
 * @param uri the Uri of the file.
 * @return true if the file size is within the limit, false otherwise.
 */
public fun ContentResolver.isFileSizeWithinLimit(uri: Uri): Boolean {
    return openFileDescriptor(uri, "r")?.use {
        it.statSize <= 15 * 1024 * 1024 // 15 MB in bytes
    } ?: false
}

/**
 * Extension function for ImageView to load an image using Coil library.
 * @param uri the String representation of the image Uri.
 */
fun ImageView.loadImageWithCoil(uri: String) {
    // Initialize your ImageLoader here if you haven't
    val imageLoader = ImageLoader.Builder(context).components {
        if (SDK_INT >= 28) {
            add(ImageDecoderDecoder.Factory())
        } else {
            add(GifDecoder.Factory())
        }
    }.build()

    val request = ImageRequest.Builder(context)
        .data(uri)
        .memoryCachePolicy(CachePolicy.ENABLED)  // Enable memory cache
        .diskCachePolicy(CachePolicy.ENABLED)
        .target(this)
        // any other configurations
        .build()

    imageLoader.enqueue(request)
}

/**
 * Extension function for List<QuashPriority> to get the API name for a given display name.
 * @param name the display name to search for.
 * @return the API name corresponding to the display name, or "NOT_DEFINED" if not found.
 */
fun List<QuashPriority>.getApiName(name: String): String {
    return this.find { it.displayName == name }?.serverField ?: "NOT_DEFINED"
}

/**
 * Extension function for List<QuashPriority> to get the API name for a given bug type display name.
 * @param name the display name to search for.
 * @return the API name corresponding to the display name, or "BUG" if not found.
 */
fun List<QuashPriority>.getTypeApiName(name: String): String {
    return this.find { it.displayName == name }?.serverField ?: "BUG"
}

/**
 * Extension function for RadioGroup to disable all radio buttons and select a specific one.
 * @param radioButtonId the ID of the radio button to select.
 */
fun RadioGroup.disableAndSelect(radioButtonId: Int) {
    isEnabled = false
    forEach { view ->
        view.isEnabled = false
    }
    findViewById<RadioButton>(radioButtonId)?.isChecked = true
}

/**
 * Extension function for String to capitalize each word in the string.
 * @return the string with each word capitalized.
 */
fun String.capitalizeWords(): String =
    split(" ").map { it.replaceFirstChar(Char::uppercaseChar) }.joinToString(" ")

/**
 * Extension function for Intent to retrieve a parcelable extra.
 * @param T the type of the parcelable extra.
 * @param key the key of the parcelable extra.
 * @return the parcelable extra of type T, or null if not found.
 */
inline fun <reified T : Parcelable> Intent.parcelable(key: String): T? = when {
    SDK_INT >= 33 -> getParcelableExtra(key, T::class.java)
    else -> @Suppress("DEPRECATION") getParcelableExtra(key) as? T
}

/**
 * Extension function for Bundle to retrieve a parcelable extra.
 * @param T the type of the parcelable extra.
 * @param key the key of the parcelable extra.
 * @return the parcelable extra of type T, or null if not found.
 */
inline fun <reified T : Parcelable> Bundle.parcelable(key: String): T? = when {
    SDK_INT >= 33 -> getParcelable(key, T::class.java)
    else -> @Suppress("DEPRECATION") getParcelable(key) as? T
}

/**
 * Extension function for Bundle to retrieve a parcelable array list extra.
 * @param T the type of the parcelable elements in the array list.
 * @param key the key of the parcelable array list extra.
 * @return the parcelable array list of type ArrayList<T>, or null if not found.
 */
inline fun <reified T : Parcelable> Bundle.parcelableArrayList(key: String): ArrayList<T>? = when {
    SDK_INT >= 33 -> getParcelableArrayList(key, T::class.java)
    else -> @Suppress("DEPRECATION") getParcelableArrayList(key)
}

/**
 * Extension function for Intent to retrieve a parcelable array list extra.
 * @param T the type of the parcelable elements in the array list.
 * @param key the key of the parcelable array list extra.
 * @return the parcelable array list of type ArrayList<T>, or null if not found.
 */
inline fun <reified T : Parcelable> Intent.parcelableArrayList(key: String): ArrayList<T>? = when {
    SDK_INT >= 33 -> getParcelableArrayListExtra(key, T::class.java)
    else -> @Suppress("DEPRECATION") getParcelableArrayListExtra(key)
}

/**
 * Extension function for Context to save network logs to a file.
 * @param networkLogs the list of QuashNetworkData objects to save.
 * @return the absolute path of the saved file, or null if an exception occurred.
 */
fun Context.saveNetworkLogsToFile(networkLogs: List<QuashNetworkData>): String? {
    return try {
        val gson = Gson()
        val jsonString = gson.toJson(networkLogs)
        val fileName = "networkLogs_${System.currentTimeMillis()}.json"
        val file = File(filesDir, fileName)
        file.writeText(jsonString)
        file.absolutePath
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}