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
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.quash.bugs.R
import com.quash.bugs.databinding.QuashFragmentTypeBottomSheetBinding
import com.quash.bugs.presentation.helper.PriorityListener

/**
 * A BottomSheetDialogFragment for selecting the type of issue.
 * This dialog allows the user to choose between Bug, UI issue, or Crash.
 */
class QuashTypeBottomSheet : BottomSheetDialogFragment() {

    private lateinit var binding: QuashFragmentTypeBottomSheetBinding
    private var listener: PriorityListener? = null

    /**
     * Sets the dialog style to the custom style defined in styles.xml.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.QuashBottomSheetDialogStyle)
    }

    /**
     * Inflates the layout for this fragment.
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = QuashFragmentTypeBottomSheetBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    /**
     * Sets up the click listeners for the type options and close button.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            tvBug.setOnClickListener {
                listener?.let { it1 -> it1(R.drawable.quash_ic_bug, tvBug.text.toString()) }
                dismiss()
            }
            tvUi.setOnClickListener {
                listener?.let { it1 -> it1(R.drawable.quash_ic_ui, tvUi.text.toString()) }
                dismiss()
            }
            tvCrash.setOnClickListener {
                listener?.let { it1 -> it1(R.drawable.quash_ic_crash, tvCrash.text.toString()) }
                dismiss()
            }
            ivClose.setOnClickListener { dismiss() }
        }
    }

    companion object {
        /**
         * Creates a new instance of the QuashTypeBottomSheet with a specified priority listener.
         *
         * @param priorityListener A callback interface to handle type selection.
         * @return A new instance of QuashTypeBottomSheet.
         */
        @JvmStatic
        fun newInstance(priorityListener: PriorityListener) =
            QuashTypeBottomSheet().apply {
                listener = priorityListener
            }
    }
}
