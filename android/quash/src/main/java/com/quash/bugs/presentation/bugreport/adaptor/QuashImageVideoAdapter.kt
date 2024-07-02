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

package com.quash.bugs.presentation.bugreport.adaptor

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.quash.bugs.core.util.loadImageWithCoil
import com.quash.bugs.core.util.loadVideoThumbnail
import com.quash.bugs.databinding.QuashItemBugReportBinding
import com.quash.bugs.databinding.QuashItemImageBinding
import com.quash.bugs.databinding.QuashItemVideoBinding
import com.quash.bugs.presentation.helper.OnAttachmentOpened
import com.quash.bugs.presentation.helper.OnAttachmentRemoved
import java.util.Locale

/**
 * Adapter for displaying a list of image and video attachments in a RecyclerView.
 *
 * @property context The context for accessing resources and resolving URIs.
 * @property onAttachmentRemoved Listener for handling the removal of attachments.
 * @property onAttachmentOpened Listener for handling the opening of attachments.
 */
class QuashImageVideoAdapter(
    private val context: Context,
    private val onAttachmentRemoved: OnAttachmentRemoved,
    private val onAttachmentOpened: OnAttachmentOpened
) : ListAdapter<Uri, BaseViewHolder>(AttachmentComparator) {

    private var isFromCrashFlow = false

    /**
     * Sets the flag indicating if the adapter is used in crash flow.
     *
     * @param tf The flag value.
     */
    fun isFromCrashFlow(tf: Boolean = false) {
        isFromCrashFlow = tf
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder =
        LayoutInflater.from(parent.context).let { inflater ->
            when (viewType) {
                ITEM_TYPE_VIDEO -> QuashVideoViewHolder(
                    QuashItemVideoBinding.inflate(inflater, parent, false),
                    onAttachmentRemoved,
                    isFromCrashFlow,
                    onAttachmentOpened
                )

                ITEM_TYPE_SCREENSHOT -> QuashImageViewHolder(
                    QuashItemImageBinding.inflate(inflater, parent, false),
                    onAttachmentRemoved,
                    onAttachmentOpened,
                    isFromCrashFlow
                )

                ITEM_TYPE_BUG_REPORT -> QuashBugReportViewHolder(
                    QuashItemBugReportBinding.inflate(inflater, parent, false),
                    onAttachmentRemoved
                )
                else -> throw IllegalArgumentException(INVALID_TYPE)
            }
        }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun getItemViewType(position: Int): Int {
        val uri = getItem(position)
        return if (uri.scheme == CONTENT) {
            // For content URIs, get the MIME type
            context.contentResolver.getType(uri)?.substringBefore('/')?.let { type ->
                when (type) {
                    IMAGE -> ITEM_TYPE_SCREENSHOT
                    else -> ITEM_TYPE_BUG_REPORT
                }
            } ?: ITEM_TYPE_BUG_REPORT
        } else if (uri.toString() == PLACEHOLDER_ICON) {
            ITEM_TYPE_VIDEO
        } else {
            val lastPathSegment = uri.lastPathSegment ?: BLANK
            val fileExtension = lastPathSegment.substringAfterLast('.', BLANK)
            when (fileExtension.lowercase(Locale.ROOT)) {
                PNG, JPG, JPEG_IMAGE, GIF -> ITEM_TYPE_SCREENSHOT
                else -> ITEM_TYPE_BUG_REPORT
            }
        }
    }

    companion object {
        private const val ITEM_TYPE_SCREENSHOT = 1
        private const val ITEM_TYPE_VIDEO = 2
        private const val ITEM_TYPE_BUG_REPORT = 3
        private const val INVALID_TYPE = "Invalid view type"
        private const val CONTENT = "content"
        private const val IMAGE = "image"
        private const val PLACEHOLDER_ICON = "placeholder://icon"
        private const val BLANK = ""
        private const val PNG = "png"
        private const val JPG = "jpg"
        private const val JPEG_IMAGE = "jpeg"
        private const val GIF = "gif"
    }
}

/**
 * Abstract ViewHolder for attachment items.
 *
 * @property itemView The root view of the ViewHolder.
 * @property onAttachmentRemoved Listener for handling the removal of attachments.
 */
abstract class BaseViewHolder(
    itemView: androidx.viewbinding.ViewBinding,
    private val onAttachmentRemoved: OnAttachmentRemoved,
) : RecyclerView.ViewHolder(itemView.root) {

    /**
     * Binds the URI to the ViewHolder.
     *
     * @param uri The URI of the attachment.
     */
    abstract fun bind(uri: Uri)

    /**
     * Sets the click listener for the close button.
     *
     * @param view The view to set the listener on.
     * @param type The type of the attachment.
     */
    protected fun setOnCloseClickListener(view: View, type: String) {
        view.setOnClickListener { onAttachmentRemoved(bindingAdapterPosition, type) }
    }
}

/**
 * ViewHolder for video attachments.
 *
 * @property binding The view binding for the video item.
 * @property isFromCrashFlow Flag indicating if the ViewHolder is used in crash flow.
 * @property onAttachmentOpened Listener for handling the opening of attachments.
 */
class QuashVideoViewHolder(
    private val binding: QuashItemVideoBinding,
    onAttachmentRemoved: OnAttachmentRemoved,
    private val isFromCrashFlow: Boolean,
    private val onAttachmentOpened: OnAttachmentOpened
) : BaseViewHolder(binding, onAttachmentRemoved) {

    override fun bind(uri: Uri) {
        binding.apply {
            viewVideo.loadVideoThumbnail(uri)
            setOnCloseClickListener(ivCLose, "VIDEO")
            ivCLose.isVisible = !isFromCrashFlow
            itemView.setOnClickListener { onAttachmentOpened(uri) }
        }
    }
}

/**
 * ViewHolder for image attachments.
 *
 * @property binding The view binding for the image item.
 * @property isFromCrashFlow Flag indicating if the ViewHolder is used in crash flow.
 * @property onAttachmentOpened Listener for handling the opening of attachments.
 */
class QuashImageViewHolder(
    private val binding: QuashItemImageBinding,
    onAttachmentRemoved: OnAttachmentRemoved,
    private val onAttachmentOpened: OnAttachmentOpened,
    private val isFromCrashFlow: Boolean
) : BaseViewHolder(binding, onAttachmentRemoved) {

    override fun bind(uri: Uri) {
        binding.apply {
            iv.loadImageWithCoil(uri)
            ivCLose.isVisible = !isFromCrashFlow
            setOnCloseClickListener(ivCLose, "CONTENT")
            itemView.setOnClickListener { onAttachmentOpened(uri) }
        }
    }
}

/**
 * ViewHolder for bug report attachments.
 *
 * @property binding The view binding for the bug report item.
 */
class QuashBugReportViewHolder(
    private val binding: QuashItemBugReportBinding,
    onAttachmentRemoved: OnAttachmentRemoved
) : BaseViewHolder(binding, onAttachmentRemoved) {

    override fun bind(uri: Uri) {
        binding.iv.loadImageWithCoil(uri)
        binding.ivCLose.visibility = View.GONE
    }
}
