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

package com.quash.bugs.di.component

import com.quash.bugs.Quash
import com.quash.bugs.core.di.module.QuashAnalyticsModule
import com.quash.bugs.core.di.module.QuashCoreModule
import com.quash.bugs.core.di.module.QuashDatabaseModule
import com.quash.bugs.core.di.module.QuashRepositoryModule
import com.quash.bugs.core.di.module.QuashViewModule
import com.quash.bugs.di.module.QuashModule
import com.quash.bugs.di.module.QuashNetworkModule
import com.quash.bugs.di.module.QuashSensorModule
import com.quash.bugs.di.module.QuashViewModelModule
import com.quash.bugs.features.crash.QuashCrashHandler
import com.quash.bugs.features.shake.ShakeDetector
import com.quash.bugs.presentation.buglist.activities.QuashBugListActivity
import com.quash.bugs.presentation.buglist.activities.QuashEditBugActivity
import com.quash.bugs.presentation.buglist.fragments.QuashDetailDialogFragment
import com.quash.bugs.presentation.bugreport.activity.QuashBugReportActivity
import com.quash.bugs.presentation.permission.PermissionRequestActivity
import com.quash.bugs.presentation.permission.QuashCrashNotifierActivity
import com.quash.bugs.service.QuashRecorderService
import com.quash.bugs.service.QuashWatchdogService
import com.quash.bugs.worker.QuashBitmapSyncWorker
import com.quash.bugs.worker.QuashNetworkLogWorker
import dagger.Component
import javax.inject.Singleton

/**
 * Defines the main dependency injection component for the Quash application.
 * This component is responsible for providing the instances of all the services,
 * repositories, and system components required across the application.
 */
@Singleton
@Component(
    modules = [
        QuashAnalyticsModule::class,
        QuashCoreModule::class,
        QuashDatabaseModule::class,
        QuashModule::class,
        QuashNetworkModule::class,
        QuashRepositoryModule::class,
        QuashSensorModule::class,
        QuashViewModelModule::class,
        QuashViewModule::class
    ]
)
interface QuashComponent {

    // Provides a singleton instance of the main Quash class.
    fun getQuash(): Quash

    // Provides access to the recorder component factory to create recorder instances.
    fun recorderComponent(): QuashRecorderComponent.Factory

    // Dependency injection methods to inject dependencies into various classes.
    fun inject(quash: Quash)
    fun inject(activity: QuashBugReportActivity)
    fun inject(activity: QuashBugListActivity)
    fun inject(activity: QuashEditBugActivity)
    fun inject(activity: PermissionRequestActivity)
    fun inject(crashNotifierActivity: QuashCrashNotifierActivity)
    fun inject(dialogFragment: QuashDetailDialogFragment)
    fun inject(screenshotWorker: QuashBitmapSyncWorker)
    fun inject(quashNetworkLogWorker: QuashNetworkLogWorker)
    fun inject(crashHandler: QuashCrashHandler)
    fun inject(shakeDetector: ShakeDetector)
    fun inject(quashRecorderService: QuashRecorderService)
    fun inject(quashWatchdogService: QuashWatchdogService)
}
