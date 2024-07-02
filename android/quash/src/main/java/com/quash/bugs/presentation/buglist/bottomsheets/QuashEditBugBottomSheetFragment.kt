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

package com.quash.bugs.presentation.buglist.bottomsheets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.quash.bugs.R
import com.quash.bugs.databinding.QuashFragmentEditBugBottomSheetBinding
import com.quash.bugs.presentation.helper.OnListAction

/**
 * A bottom sheet fragment for editing or deleting a bug.
 */
class QuashEditBugBottomSheetFragment : BottomSheetDialogFragment() {

    private lateinit var binding: QuashFragmentEditBugBottomSheetBinding
    private var bugId: String? = null
    private var listener: OnListAction? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.QuashBottomSheetDialogStyle)
    }

    companion object {
        /**
         * Creates a new instance of [QuashEditBugBottomSheetFragment].
         *
         * @param onListAction The action listener for the list.
         * @param bug The ID of the bug.
         * @return A new instance of [QuashEditBugBottomSheetFragment].
         */
        fun createNewInstance(onListAction: OnListAction, bug: String) =
            QuashEditBugBottomSheetFragment().apply {
                listener = onListAction
                bugId = bug
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = QuashFragmentEditBugBottomSheetBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
    }

    /**
     * Sets up the click listeners for the edit and delete actions.
     */
    private fun setupClickListeners() {
        with(binding) {
            tvEdit.setOnClickListener {
                listener?.let { it1 -> it1(bugId ?: "", true) }
                dismiss()
            }

            tvDelete.setOnClickListener {
                listener?.let { it1 -> it1(bugId ?: "", false) }
                dismiss()
            }
            ivClose.setOnClickListener { dismiss() }
        }
    }
}
