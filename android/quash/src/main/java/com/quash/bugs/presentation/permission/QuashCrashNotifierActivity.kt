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

package com.quash.bugs.presentation.permission

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.quash.bugs.Quash
import com.quash.bugs.R
import com.quash.bugs.core.data.local.QuashNetworkDao
import com.quash.bugs.core.di.module.QuashCoreModule
import com.quash.bugs.databinding.QuashLayoutCrashNotifierBinding
import com.quash.bugs.di.component.DaggerQuashComponent
import com.quash.bugs.presentation.bugreport.activity.QuashBugReportActivity
import javax.inject.Inject

/**
 * Activity to notify the user about a crash and provide options to cancel or report the crash.
 */
class QuashCrashNotifierActivity : AppCompatActivity(), View.OnClickListener {

    private var binding: QuashLayoutCrashNotifierBinding? = null
    private var uriListString: String? = null

    @Inject
    lateinit var quashNetworkDao: QuashNetworkDao

    /**
     * Called when the activity is starting.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = QuashLayoutCrashNotifierBinding.inflate(layoutInflater)
        injectDependencies()
        setContentView(binding?.root)
        fetchDataFromPreviousScreen()
        renderUI()
    }

    /**
     * Injects the necessary dependencies using Dagger.
     */
    private fun injectDependencies() {
        DaggerQuashComponent.builder().quashCoreModule(QuashCoreModule(this)).build().inject(this)
    }

    /**
     * Fetches data passed from the previous screen.
     */
    private fun fetchDataFromPreviousScreen() {
        uriListString = intent.getStringExtra("uriList")
    }

    /**
     * Renders the UI and sets up click listeners.
     */
    private fun renderUI() {
        setupClickListeners()
    }

    /**
     * Sets up click listeners for the buttons in the layout.
     */
    private fun setupClickListeners() {
        binding?.apply {
            btnCancel.setOnClickListener(this@QuashCrashNotifierActivity)
            btnReport.setOnClickListener(this@QuashCrashNotifierActivity)
        }
    }

    /**
     * Handles click events for the buttons.
     *
     * @param v The view that was clicked.
     */
    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnCancel -> {
                finish()
            }

            R.id.btnReport -> {
                openReportActivity()
            }
        }
    }

    /**
     * Opens the report activity to allow the user to report the crash.
     */
    private fun openReportActivity() {
        Quash.getInstance().saveFilePathForNetwork()
        val intent = Intent(this, QuashBugReportActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("uriList", uriListString)
            putExtra("isComingFromRecordingService", true)
        }
        val options = ActivityOptions.makeCustomAnimation(this, 0, 0)
        startActivity(intent, options.toBundle())
        finish()
    }
}
