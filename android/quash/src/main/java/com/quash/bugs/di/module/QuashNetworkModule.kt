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

package com.quash.bugs.di.module

import android.content.Context
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.quash.bugs.BuildConfig
import com.quash.bugs.core.data.local.QuashNetworkDao
import com.quash.bugs.core.data.preference.QuashAppPreferences
import com.quash.bugs.core.data.preference.QuashCommonPreferences
import com.quash.bugs.core.data.remote.QuashApiService
import com.quash.bugs.features.network.IQuashNetworkLogger
import com.quash.bugs.features.network.IQuashNetworkLoggerImpl
import com.quash.bugs.features.network.QuashAuthInterceptor
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineScope
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * A Dagger module to provide network-related dependencies such as OkHttpClient, Retrofit, and other services.
 */
@Module
class QuashNetworkModule {

    /**
     * Provides an HttpLoggingInterceptor which logs HTTP request and response data.
     * The logging level is set to BODY in debug builds for comprehensive logging. It's turned off in release builds.
     */
    @Provides
    @Singleton
    fun providesHttpLoggingInterceptor() = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG)
            HttpLoggingInterceptor.Level.BODY
        else
            HttpLoggingInterceptor.Level.NONE
    }

    /**
     * Provides a QuashAuthInterceptor which adds authentication headers to every network request.
     * @param quashAppPreferences Provides access to the app's preferences where auth tokens are stored.
     */
    @Provides
    @Singleton
    fun provideQuashAuthenticationInterceptor(quashAppPreferences: QuashAppPreferences): QuashAuthInterceptor {
        return QuashAuthInterceptor(quashAppPreferences)
    }

    /**
     * Provides a customized OkHttpClient with set timeouts, retry configurations, and interceptors including
     * logging and authentication.
     */
    @Provides
    @Singleton
    fun providesQuashOkHttpClient(
        quashAuthInterceptor: QuashAuthInterceptor,
        okHttpLoggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .addInterceptor(okHttpLoggingInterceptor)
            .addInterceptor(quashAuthInterceptor)
            .build()
    }

    /**
     * Provides a Retrofit instance configured with the base URL and converters for handling JSON.
     * This setup includes a coroutine call adapter which enables Retrofit to return Kotlin coroutines types.
     */
    @Provides
    @Singleton
    fun providesQuashRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(CoroutineCallAdapterFactory())
            .client(okHttpClient)
            .build()
    }

    /**
     * Provides the API service for the Quash app, enabling network operations like fetching, posting, and updating data.
     */
    @Provides
    @Singleton
    fun provideQuashApiService(retrofit: Retrofit): QuashApiService {
        return retrofit.create(QuashApiService::class.java)
    }

    /**
     * Provides a logger for network requests that can log details to the local storage and manage them through a database.
     */
    @Provides
    @Singleton
    fun provideNetworkLogger(
        sharedPreferencesUtil: QuashCommonPreferences,
        networkDao: QuashNetworkDao,
        coroutineScope: CoroutineScope,
        context: Context,
    ): IQuashNetworkLogger =
        IQuashNetworkLoggerImpl(sharedPreferencesUtil, networkDao, coroutineScope, context)
}
