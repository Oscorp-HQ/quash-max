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

package com.quash.bugs.presentation.bugreport.bottomsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.quash.bugs.R
import com.quash.bugs.databinding.QuashFragmentAttachmentBottomSheetBinding
import com.quash.bugs.presentation.helper.OnMediaOptionSelected
import com.quash.bugs.presentation.helper.QuashMediaOption

/**
 * Bottom sheet dialog fragment for selecting media options.
 */
class QuashAttachmentBottomSheet : BottomSheetDialogFragment() {

    private lateinit var binding: QuashFragmentAttachmentBottomSheetBinding
    private var onMediaOptionSelected: OnMediaOptionSelected? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.QuashBottomSheetDialogStyle)
    }

    companion object {
        /**
         * Factory method to create a new instance of this fragment.
         *
         * @param selected Callback to handle media option selection.
         * @return A new instance of QuashAttachmentBottomSheet.
         */
        fun createNewInstance(selected: OnMediaOptionSelected) =
            QuashAttachmentBottomSheet().apply {
                onMediaOptionSelected = selected
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = QuashFragmentAttachmentBottomSheetBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            setMediaClickListener(tvScreenShot, QuashMediaOption.SCREENSHOT)
            setMediaClickListener(tvVideo, QuashMediaOption.SCREEN_RECORD)
            setMediaClickListener(tvGallery, QuashMediaOption.OPEN_GALLERY)
            ivClose.setOnClickListener { dismiss() }
        }
    }

    /**
     * Sets the click listener for media options and invokes the callback.
     *
     * @param view The view to attach the click listener to.
     * @param quashMediaOption The media option associated with the view.
     */
    private fun setMediaClickListener(view: View, quashMediaOption: QuashMediaOption) {
        view.setOnClickListener {
            onMediaOptionSelected?.invoke(quashMediaOption.id)
            dismiss()
        }
    }
}
