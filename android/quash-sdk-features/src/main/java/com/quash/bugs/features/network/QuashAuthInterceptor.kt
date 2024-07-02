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

package com.quash.bugs.features.network

import com.quash.bugs.core.data.preference.QuashAppPreferences
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

/**
 * An interceptor for adding authorization headers to HTTP requests.
 *
 * This class intercepts outgoing requests and appends an authorization header along with some
 * default headers to ensure the requests are executed with the necessary credentials and formats.
 *
 * @property quashAppPreferences Provides access to the application's preference data where the token is stored.
 */
class QuashAuthInterceptor(private val quashAppPreferences: QuashAppPreferences) : Interceptor {

    /**
     * Intercepts an outgoing request and modifies it to include authorization and other necessary headers.
     *
     * @param chain The interceptor chain which provides access to the outgoing request.
     * @return The response after the request has been modified and processed.
     * @throws IOException if there is an issue executing the request.
     */
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // Retrieve the reporting token or use an empty string if none exists.
        val token = quashAppPreferences.getReportingToken() ?: ""

        // Modify the original request to include the authorization header and content type.
        val requestWithHeaders = originalRequest.newBuilder()
            .addHeader("Authorization", "Bearer $token")
            .addHeader("Accept", "*/*")
            .addHeader("Content-Type", "application/json")
            .build()

        // Proceed with the modified request in the chain.
        return chain.proceed(requestWithHeaders)
    }
}
