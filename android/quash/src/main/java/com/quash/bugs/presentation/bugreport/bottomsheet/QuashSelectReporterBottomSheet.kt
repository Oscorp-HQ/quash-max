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
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.quash.bugs.R
import com.quash.bugs.core.data.dto.ReporterInt
import com.quash.bugs.databinding.QuashFragmentSelectReporterBottomSheetBinding
import com.quash.bugs.presentation.bugreport.adaptor.QuashReporterAdapter
import com.quash.bugs.presentation.helper.DividerItemDecoration
import com.quash.bugs.presentation.helper.OnReporterNameSelected

/**
 * BottomSheetDialogFragment to select a reporter from a list.
 */
class QuashSelectReporterBottomSheet : BottomSheetDialogFragment() {

    private lateinit var binding: QuashFragmentSelectReporterBottomSheetBinding
    private var onReporterNameSelected: OnReporterNameSelected? = null
    private var name = ""
    private var reporterIntList: ArrayList<ReporterInt> = arrayListOf()
    private val quashReporterAdapter = QuashReporterAdapter({ name, id ->
        dismiss()
        onReporterNameSelected?.invoke(name, id)
    }, name)

    companion object {
        /**
         * Creates a new instance of QuashSelectReporterBottomSheet.
         *
         * @param listener Callback for when a reporter is selected.
         * @param str The name of the reporter.
         * @param list The list of reporters.
         * @return A new instance of QuashSelectReporterBottomSheet.
         */
        fun createNewInstance(
            listener: OnReporterNameSelected,
            str: String,
            list: ArrayList<ReporterInt>
        ) = QuashSelectReporterBottomSheet().apply {
            onReporterNameSelected = listener
            name = str
            reporterIntList.addAll(list)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.QuashBottomSheetDialogStyle)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = QuashFragmentSelectReporterBottomSheetBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        quashReporterAdapter.submitList(reporterIntList)

        val dividerItemDecoration = DividerItemDecoration(requireContext())
        binding.rvReporter.apply {
            layoutManager = LinearLayoutManager(requireContext())
            addItemDecoration(dividerItemDecoration)
            adapter = quashReporterAdapter
        }
        binding.ivClose.setOnClickListener { dismiss() }
    }
}
