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

package com.quash.bugs.presentation.common

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Window
import android.widget.ProgressBar
import com.quash.bugs.R

/**
 * LoaderView is a custom loading dialog used to show a progress indicator.
 *
 * @property context the context in which the dialog should be displayed.
 */
class QuashLoaderView(private val context: Context) {

    private var dialog: Dialog? = null
    private var progressBar: ProgressBar? = null

    init {
        setupDialog()
    }

    /**
     * Initializes and sets up the dialog properties.
     */
    private fun setupDialog() {
        dialog = Dialog(context).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(R.layout.quash_lottie_loader_view)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            progressBar = findViewById(R.id.progressBar)
            setCancelable(false)
        }
    }

    /**
     * Shows the loading dialog.
     */
    fun showLoading() {
        dialog?.show()
    }

    /**
     * Hides the loading dialog.
     */
    fun hideLoading() {
        dialog?.dismiss()
    }
}
