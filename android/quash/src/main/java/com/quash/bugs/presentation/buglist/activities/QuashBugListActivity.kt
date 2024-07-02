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

package com.quash.bugs.presentation.buglist.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.quash.bugs.R
import com.quash.bugs.core.data.dto.QuashBugConstant
import com.quash.bugs.core.data.dto.Report
import com.quash.bugs.core.data.remote.RequestState
import com.quash.bugs.core.di.module.QuashCoreModule
import com.quash.bugs.core.util.QuashAmplitudeLogger
import com.quash.bugs.databinding.QuashActivityBugListBinding
import com.quash.bugs.databinding.QuashDialogCloseBinding
import com.quash.bugs.di.component.DaggerQuashComponent
import com.quash.bugs.di.module.ViewModelFactory
import com.quash.bugs.presentation.buglist.adapters.BugsListAdapter
import com.quash.bugs.presentation.buglist.bottomsheets.QuashEditBugBottomSheetFragment
import com.quash.bugs.presentation.buglist.fragments.QuashDetailDialogFragment
import com.quash.bugs.presentation.buglist.viewmodel.QuashBugListViewModel
import com.quash.bugs.presentation.common.QuashLoaderView
import com.quash.bugs.presentation.helper.DividerItemDecoration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Activity for displaying the list of bugs.
 */
class QuashBugListActivity : AppCompatActivity() {

    private lateinit var binding: QuashActivityBugListBinding
    private val bugsListAdapter = BugsListAdapter(::editBugClicked)
    private lateinit var quashLoaderView: QuashLoaderView
    private val viewModel by viewModels<QuashBugListViewModel> { viewModelFactory }

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    @Inject
    lateinit var amplitudeLogger: QuashAmplitudeLogger

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = QuashActivityBugListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
        quashLoaderView = QuashLoaderView(this)
        injectDependencies()
        setUpUi()
    }

    /**
     * Sets up the UI components.
     */
    @SuppressLint("SetTextI18n")
    private fun setUpUi() {
        val dividerItemDecoration = DividerItemDecoration(this)
        binding.rvBugList.apply {
            layoutManager = LinearLayoutManager(this@QuashBugListActivity)
            addItemDecoration(dividerItemDecoration)
            adapter = bugsListAdapter
        }

        lifecycleScope.launch {
            bugsListAdapter.loadStateFlow.collect { loadState ->
                binding.tvBugCount.text = "${bugsListAdapter.itemCount} Bugs"
                with(binding) {
                    val isLoadError =
                        loadState.refresh is LoadState.Error || loadState.append is LoadState.Error
                    clErrorView.visibility = if (isLoadError) View.VISIBLE else View.GONE
                    clEmptyView.visibility =
                        if (loadState.append.endOfPaginationReached && bugsListAdapter.itemCount == 0) View.VISIBLE else View.GONE
                    rvBugList.visibility = if (!isLoadError) View.VISIBLE else View.GONE
                    tvBugCount.visibility = if (!isLoadError) View.VISIBLE else View.GONE
                    tvShowing.visibility = if (!isLoadError) View.VISIBLE else View.GONE
                    tvBugCount.text = "${bugsListAdapter.itemCount} Bugs"
                }
            }
        }

        bugsListAdapter.addLoadStateListener { loadState ->
            when (loadState.refresh) {
                is LoadState.Loading -> {
                    binding.lottieLoader.visibility = View.VISIBLE
                }

                is LoadState.NotLoading -> {
                    binding.lottieLoader.visibility = View.GONE
                }

                is LoadState.Error -> {
                    binding.lottieLoader.visibility = View.GONE
                }

            }
        }

        binding.ivClose.setOnClickListener { finish() }

        viewModel.requestStateDeleteBug.observe(this) { state ->
            when (state) {
                is RequestState.InProgress -> {
                    // Show loading spinner
                    quashLoaderView.showLoading()
                }

                is RequestState.Successful -> {
                    // Handle success
                    quashLoaderView.hideLoading()
                    if (state.data.success)
                        getBugList()
                    reportEvent(DELETE_CLICKED)
                }

                is RequestState.Failed -> {
                    // Show error message
                    quashLoaderView.hideLoading()
                }
            }
        }
    }

    /**
     * Handles the edit bug click event.
     *
     * @param bug The selected bug.
     * @param boolean Indicates whether to show the edit bottom sheet or detail dialog.
     */
    private fun editBugClicked(bug: Report, boolean: Boolean) {
        quashLoaderView.showLoading()
        QuashBugConstant.report = bug
        if (boolean)
            QuashEditBugBottomSheetFragment.createNewInstance(::onActionClicked, bug.id)
                .show(supportFragmentManager, EDIT_BUG_FRAG)
        else {
            QuashDetailDialogFragment.createNewInstance()
                .show(supportFragmentManager, DETAIL_DIALOG_FRAG)
        }
        quashLoaderView.hideLoading()
    }

    /**
     * Handles the action clicked event from the edit bottom sheet.
     *
     * @param bugId The ID of the selected bug.
     * @param isEdit Indicates whether the action is edit or delete.
     */
    private fun onActionClicked(bugId: String, isEdit: Boolean) {
        if (isEdit) {
            reportEvent(EDIT_CLICKED)
            val intent = Intent(this@QuashBugListActivity, QuashEditBugActivity::class.java)
            startActivity(intent)
        } else {
            showDeleteDialog(bugId)
        }
    }

    /**
     * Retrieves the list of bugs.
     */
    private fun getBugList() {
        reportEvent(LIST_PAGE_VIEW)
        lifecycleScope.launch {
            viewModel.getBugList().collectLatest { pagingData ->
                bugsListAdapter.submitData(pagingData)
            }
        }
    }

    /**
     * Injects the dependencies using Dagger.
     */
    private fun injectDependencies() {
        DaggerQuashComponent.builder().quashCoreModule(QuashCoreModule(this)).build().inject(this)
    }

    /**
     * Shows the delete confirmation dialog.
     *
     * @param bugId The ID of the bug to delete.
     */
    private fun showDeleteDialog(bugId: String) {
        val binding = QuashDialogCloseBinding.inflate(layoutInflater)
        val dialog =
            MaterialAlertDialogBuilder(
                this,
                R.style.QuashMaterial3AlertDialog
            ).setView(binding.root)
                .setCancelable(false)
                .show()

        binding.apply {
            tvTitle.text = resources.getString(R.string.delete_title)
            tvSubTitle.text = resources.getString(R.string.delete_sub_title)
            btDiscard.text = resources.getString(R.string.cancel)
            btSave.text = resources.getString(R.string.delete)
            ivDialogClose.visibility = View.GONE
            btDiscard.setOnClickListener {
                dialog.dismiss()
            }
            btSave.setOnClickListener {
                lifecycleScope.launch { viewModel.deleteBug(bugId) }
                dialog.dismiss()
            }
        }
    }

    /**
     * Reports an event using the QuashAmplitudeLogger.
     *
     * @param string The event string.
     */
    private fun reportEvent(string: String) {
        CoroutineScope(Dispatchers.Main).launch {
            amplitudeLogger.logEvent(LIST_PAGE, string)
        }
    }

    override fun onResume() {
        super.onResume()
        getBugList()
    }

    companion object {
        const val LIST_PAGE = "List Page"
        const val LIST_PAGE_VIEW = "List Page Views"
        const val DELETE_CLICKED = "Delete Button Clicked"
        const val EDIT_CLICKED = "Edit Button Clicked"
        const val EDIT_BUG_FRAG = "EditBugBottomSheetFragment"
        const val DETAIL_DIALOG_FRAG = "DetailDialogFragment"
    }
}