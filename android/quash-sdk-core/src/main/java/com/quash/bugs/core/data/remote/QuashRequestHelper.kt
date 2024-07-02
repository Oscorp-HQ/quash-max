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

package com.quash.bugs.core.data.remote

import android.util.Log
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import retrofit2.Response
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLHandshakeException

const val DEBUG_MODE = false // Set this based on your app's build config or other logic

/**
 * Executes an API call with protection, emitting the result as a Flow.
 *
 * @param T The type of the response body.
 * @param apiCall The suspend function representing the API call.
 * @param toastHandler The handler for showing error toasts.
 * @return A Flow emitting the request state.
 */
fun <T> protectedApiCallWithToast(
    apiCall: suspend () -> Response<T>,
    toastHandler: QuashToastHandler
): Flow<RequestState<T>> = flow {
    try {
        if (DEBUG_MODE) Log.d("protectedApiCallWithToast", "Starting API call")
        emit(RequestState.InProgress)
        val response = apiCall.invoke()
        if (response.isSuccessful && response.body() != null) {
            if (DEBUG_MODE) Log.d(
                "protectedApiCallWithToast",
                "API call successful: ${response.body()}"
            )
            emit(RequestState.Successful(response.body()!!))
        } else {
            val exception = handleApiException(HttpException(response), toastHandler)
            if (DEBUG_MODE) Log.e("protectedApiCallWithToast", "API call failed: $exception")
            emit(RequestState.Failed(exception))
        }
    } catch (e: Exception) {
        val exception = handleApiException(e, toastHandler)
        if (DEBUG_MODE) Log.e("protectedApiCallWithToast", "API call exception: $exception", e)
        emit(RequestState.Failed(exception))
    }
}

/**
 * Executes an API call with protection and retry mechanism, emitting the result as a Flow.
 *
 * @param T The type of the response body.
 * @param apiCall The suspend function representing the API call.
 * @param maxAttempts The maximum number of attempts.
 * @param delayBetweenRetries The delay between retries in milliseconds.
 * @return A Flow emitting the request state.
 */
fun <T> protectedApiCallWithRetry(
    apiCall: suspend () -> Response<T>,
    maxAttempts: Int = 3,
    delayBetweenRetries: Long = 1000
): Flow<RequestState<T>> = flow {
    emit(RequestState.InProgress)

    var currentAttempt = 0
    while (currentAttempt < maxAttempts) {
        try {
            if (DEBUG_MODE) Log.d("protectedApiCallWithRetry", "Attempt $currentAttempt")
            val response = apiCall.invoke()
            if (response.isSuccessful && response.body() != null) {
                if (DEBUG_MODE) Log.d(
                    "protectedApiCallWithRetry",
                    "API call successful: ${response.body()}"
                )
                emit(RequestState.Successful(response.body()!!))
                return@flow // Exit the flow early if the request is successful
            } else {
                val exception = handleApiException(HttpException(response))
                if (DEBUG_MODE) Log.e("protectedApiCallWithRetry", "API call failed: $exception")
                emit(RequestState.Failed(exception))
            }
        } catch (e: Exception) {
            val exception = handleApiException(e)
            if (DEBUG_MODE) Log.e("protectedApiCallWithRetry", "API call exception: $exception", e)
            emit(RequestState.Failed(exception))
        }

        currentAttempt++
        if (currentAttempt < maxAttempts) {
            delay(delayBetweenRetries) // Delay the next attempt if it's not the last one
        }
    }
}

/**
 * Executes an API call with protection, emitting the result as a Flow.
 *
 * @param T The type of the response body.
 * @param apiCall The suspend function representing the API call.
 * @return A Flow emitting the request state.
 */
fun <T> protectedApiCall(
    apiCall: suspend () -> Response<T>
): Flow<RequestState<T>> = flow {
    try {
        if (DEBUG_MODE) Log.d("protectedApiCall", "Starting API call")
        emit(RequestState.InProgress)
        val response = apiCall.invoke()
        if (response.isSuccessful && response.body() != null) {
            if (DEBUG_MODE) Log.d("protectedApiCall", "API call successful: ${response.body()}")
            emit(RequestState.Successful(response.body()!!))
        } else {
            val exception = handleApiException(HttpException(response))
            if (DEBUG_MODE) Log.e("protectedApiCall", "API call failed: $exception")
            emit(RequestState.Failed(exception))
        }
    } catch (e: Exception) {
        val exception = handleApiException(e)
        if (DEBUG_MODE) Log.e("protectedApiCall", "API call exception: $exception", e)
        emit(RequestState.Failed(exception))
    }
}

/**
 * Handles exceptions thrown during API calls and converts them to ApiException.
 *
 * @param e The exception thrown during the API call.
 * @param toastHandler The handler for showing error toasts.
 * @return The corresponding ApiException.
 */
fun handleApiException(e: Exception, toastHandler: QuashToastHandler): ApiException {
    val exception = when (e) {
        is HttpException -> {
            when (e.code()) {
                400 -> ApiException.BadRequest()
                401 -> ApiException.Unauthorized()
                403 -> ApiException.Forbidden()
                404 -> ApiException.NotFound()
                408 -> ApiException.Timeout()
                429 -> ApiException.TooManyRequests()
                500 -> ApiException.InternalServerError()
                503 -> ApiException.ServiceUnavailable()
                else -> ApiException.UnknownError()
            }
        }

        is UnknownHostException -> ApiException.NoInternetConnection()
        is SocketTimeoutException -> ApiException.Timeout()
        is SSLHandshakeException -> ApiException.SSLHandshake()
        else -> ApiException.UnknownError()
    }

    toastHandler.showErrorToast(exception.message ?: "An error occurred")
    if (DEBUG_MODE) Log.e("handleApiException", "Handled exception: $exception", e)
    return exception
}

/**
 * Handles exceptions thrown during API calls and converts them to ApiException.
 *
 * @param e The exception thrown during the API call.
 * @return The corresponding ApiException.
 */
fun handleApiException(e: Exception): ApiException {
    val exception = when (e) {
        is HttpException -> {
            when (e.code()) {
                400 -> ApiException.BadRequest()
                401 -> ApiException.Unauthorized()
                403 -> ApiException.Forbidden()
                404 -> ApiException.NotFound()
                408 -> ApiException.Timeout()
                429 -> ApiException.TooManyRequests()
                500 -> ApiException.InternalServerError()
                503 -> ApiException.ServiceUnavailable()
                else -> ApiException.UnknownError()
            }
        }

        is UnknownHostException -> ApiException.NoInternetConnection()
        is SocketTimeoutException -> ApiException.Timeout()
        is SSLHandshakeException -> ApiException.SSLHandshake()
        else -> ApiException.UnknownError()
    }

    if (DEBUG_MODE) Log.e("handleApiException", "Handled exception: $exception", e)
    return exception
}
