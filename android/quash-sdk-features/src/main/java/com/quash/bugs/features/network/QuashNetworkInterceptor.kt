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

import androidx.annotation.Keep
import com.quash.bugs.core.data.dto.QuashNetworkData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import okio.Buffer
import okio.GzipSource
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

/**
 * Interceptor for logging detailed information about HTTP requests and responses.
 *
 * This interceptor captures various details about HTTP requests and responses, including headers,
 * bodies, status codes, and the duration of the requests. This information is then logged using a
 * provided logging function.
 *
 * @property logger A suspend function for logging [QuashNetworkData].
 * @property coroutineScope The coroutine scope in which logging operations will be executed.
 */
@Keep
class QuashNetworkInterceptor(
    private val logger: suspend (QuashNetworkData) -> Unit,
    private val coroutineScope: CoroutineScope
) : Interceptor {

    /**
     * Intercepts and logs details about HTTP requests and responses.
     *
     * @param chain The chain of interceptors to process the request.
     * @return The HTTP response.
     */
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val startNs = System.nanoTime()

        val networkLog = QuashNetworkData(
            requestUrl = request.url.toString(),
            requestMethod = request.method,
            requestHeaders = request.headers.toMap(),
            requestBody = request.bodyToString(),
            durationMs = null,
            errorMessage = null,
            exceptionMessage = null,
            exceptionStackTrace = null,
            responseHeaders = null,
            responseBody = null,
            responseCode = null,
            timeStamp = getCurrentUtcTime()
        )

        val response: Response
        try {
            response = chain.proceed(request)
            val tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs)

            networkLog.apply {
                responseCode = response.code
                responseHeaders = response.headers.toMap()
                durationMs = tookMs
                responseBody = response.peekBody()
            }
        } catch (e: Exception) {
            networkLog.apply {
                durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs)
                errorMessage = e.message
            }
            throw e
        }

        coroutineScope.launch(Dispatchers.IO) {
            logger(networkLog)
        }

        return response
    }

    /**
     * Gets the current UTC time as a formatted string.
     *
     * @return The current UTC time in ISO 8601 format.
     */
    private fun getCurrentUtcTime(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(Date())
    }

    /**
     * Converts the request body to a string.
     *
     * @return The request body as a string, or null if the
     * Converts the request body to a string.
     *
     * @return The request body as a string, or null if the request has no body.
     */
    private fun Request.bodyToString(): String? {
        val buffer = Buffer()
        this.body?.writeTo(buffer)
        val charset =
            this.body?.contentType()?.charset(StandardCharsets.UTF_8) ?: StandardCharsets.UTF_8
        val bufferSize = 2_000_000L
        return if (buffer.size > bufferSize) {
            buffer.readString(bufferSize, charset) + "\n[Content truncated]"
        } else {
            buffer.readString(charset)
        }
    }

    /**
     * Reads and returns the response body as a string.
     *
     * @return The response body as a string, or an error message if reading fails.
     */
    private fun Response.peekBody(): String? {
        val maxSize = 2_000_000L
        return try {
            val isGzip = this.headers["Content-Encoding"]?.contains("gzip") == true
            val source = if (isGzip) {
                // Decompress GZIP content
                val gzipSource = GzipSource(this.body!!.source())
                Buffer().apply { writeAll(gzipSource) }
            } else {
                this.peekBody(maxSize).source()
            }
            source.request(maxSize)
            val buffer = source.buffer
            val contentLength = this.body?.contentLength() ?: -1
            if (contentLength > maxSize) {
                buffer.clone().readString(maxSize, StandardCharsets.UTF_8) + "\n[Content truncated]"
            } else {
                buffer.clone().readString(StandardCharsets.UTF_8)
            }
        } catch (e: IOException) {
            "Error reading response body: ${e.message}"
        }
    }
}
