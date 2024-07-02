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

package com.quash.bugs.core.data.remote

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.quash.bugs.core.R
import javax.inject.Inject

/**
 * Handles the display of custom toasts in the application.
 *
 * @param context The application context used to create and display toasts.
 */
class QuashToastHandler @Inject constructor(private val context: Context) {

    /**
     * Displays a custom error toast with a specified message.
     *
     * @param message The message to display in the toast.
     */
    fun showErrorToast(message: String) {
        val layout = LayoutInflater.from(context).inflate(R.layout.quash_custom_toast, null)
        val textView = layout.findViewById<TextView>(R.id.toast_text)
        val icon = layout.findViewById<ImageView>(R.id.toast_icon)
        val close = layout.findViewById<ImageButton>(R.id.toast_close)

        textView.text = message
        icon.setImageResource(R.drawable.quash_ic_media_close) // Replace with your error icon
        close.setOnClickListener { /* Dismiss the toast here */ }

        val density = context.resources.displayMetrics.density
        val horizontalMargin = (32 * density).toInt()

        val params = ViewGroup.MarginLayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(horizontalMargin, 0, horizontalMargin, 0)
        layout.layoutParams = params

        with(Toast(context)) {
            setGravity(Gravity.BOTTOM or Gravity.FILL_HORIZONTAL, 0, 0)
            duration = Toast.LENGTH_LONG
            view = layout
            show()
        }
    }

    /**
     * Displays a custom success toast with a specified message.
     *
     * @param message The message to display in the toast.
     */
    fun showSuccessToast(message: String) {
        val layout = LayoutInflater.from(context).inflate(R.layout.quash_custom_toast, null)
        val textView = layout.findViewById<TextView>(R.id.toast_text)
        val icon = layout.findViewById<ImageView>(R.id.toast_icon)
        val close = layout.findViewById<ImageButton>(R.id.toast_close)

        textView.text = message
        icon.setImageResource(R.drawable.quash_ic_correct) // Replace with your success icon
        close.setOnClickListener { /* Dismiss the toast here */ }

        val density = context.resources.displayMetrics.density
        val horizontalMargin = (32 * density).toInt()

        val params = ViewGroup.MarginLayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(horizontalMargin, 0, horizontalMargin, 0)
        layout.layoutParams = params

        with(Toast(context)) {
            setGravity(Gravity.BOTTOM or Gravity.FILL_HORIZONTAL, 0, 0)
            duration = Toast.LENGTH_LONG
            view = layout
            show()
        }
    }
}
