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

package com.quash.bugs.core.di.module

import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import com.quash.bugs.core.data.local.QuashBitmapDao
import com.quash.bugs.core.data.local.QuashDatabase
import com.quash.bugs.core.data.local.QuashNetworkDao
import com.quash.bugs.core.data.preference.QuashAppPreferences
import com.quash.bugs.core.data.preference.QuashBugReportPreferences
import com.quash.bugs.core.data.preference.QuashCommonPreferences
import com.quash.bugs.core.data.preference.QuashEditBugPreferences
import com.quash.bugs.core.data.preference.QuashUserPreferences
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class QuashDatabaseModule {

    @Provides
    @Singleton
    fun providesRoomDatabase(applicationContext: Context): QuashDatabase {
        return Room.databaseBuilder(
            applicationContext, QuashDatabase::class.java, "quash-database"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    @Singleton
    fun providesQuashDao(quashDatabase: QuashDatabase): QuashNetworkDao {
        return quashDatabase.quashNetworkDao()
    }

    @Provides
    @Singleton
    fun providesQuashBitmapDao(quashDatabase: QuashDatabase): QuashBitmapDao {
        return quashDatabase.bitmapDao()
    }


    @Provides
    @Singleton
    fun provideSharedPreferences(context: Context): SharedPreferences =
        context.getSharedPreferences("quashbugs", Context.MODE_PRIVATE)

    @Provides
    @Singleton
    fun provideSharedPreferencesUtil(sharedPreferences: SharedPreferences): QuashCommonPreferences =
        QuashCommonPreferences(sharedPreferences)

    @Provides
    @Singleton
    fun provideUserPreferences(sharedPreferences: SharedPreferences): QuashUserPreferences =
        QuashUserPreferences(sharedPreferences)

    @Provides
    @Singleton
    fun provideAppPreferences(sharedPreferences: SharedPreferences): QuashAppPreferences =
        QuashAppPreferences(sharedPreferences)

    @Provides
    @Singleton
    fun provideBugReportPreferences(sharedPreferences: SharedPreferences): QuashBugReportPreferences =
        QuashBugReportPreferences(sharedPreferences)

    @Provides
    @Singleton
    fun provideEditBugReportPreferences(sharedPreferences: SharedPreferences): QuashEditBugPreferences =
        QuashEditBugPreferences(sharedPreferences)
}