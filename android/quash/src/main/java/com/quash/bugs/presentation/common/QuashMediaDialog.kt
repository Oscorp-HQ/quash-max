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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.DialogFragment
import com.quash.bugs.R
import com.quash.bugs.core.util.loadImageWithCoil

/**
 * DialogFragment to display media (image or video).
 * Currently, it only handles images.
 */
class QuashMediaDialog : DialogFragment() {

    private lateinit var imageView: ImageView

    /**
     * Inflates the layout for this fragment.
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.quash_fragment_media_dialog, container, false)
    }

    /**
     * Initializes the dialog and loads the image if provided.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.setCanceledOnTouchOutside(false)

        imageView = view.findViewById(R.id.zoomableImageView)

        val uri = arguments?.getString("uri")
        val isVideo = arguments?.getBoolean("isVideo") ?: false

        if (isVideo) {
            // Currently, no operation for video
        } else {
            loadImage(uri)
        }
    }

    /**
     * Returns the theme for the dialog.
     */
    override fun getTheme(): Int = R.style.QuashDialogTheme

    /**
     * Loads an image into the ImageView using Coil.
     *
     * @param uri The URI of the image to load.
     */
    private fun loadImage(uri: String?) {
        imageView.visibility = View.VISIBLE
        uri?.let {
            imageView.loadImageWithCoil(it)
        }
    }

    companion object {
        /**
         * Creates a new instance of QuashMediaDialog with the provided URI and media type.
         *
         * @param uri The URI of the media.
         * @param isVideo Boolean indicating if the media is a video.
         * @return A new instance of QuashMediaDialog.
         */
        fun newInstance(uri: String, isVideo: Boolean) = QuashMediaDialog().apply {
            arguments = Bundle().apply {
                putString("uri", uri)
                putBoolean("isVideo", isVideo)
            }
        }
    }
}
