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

/**
 * Represents the state of a request.
 */
sealed class RequestState<out T> {
    /**
     * Indicates that the request is in progress.
     */
    data object InProgress : RequestState<Nothing>()

    /**
     * Indicates that the request was successful.
     *
     * @param T The type of the successful response data.
     * @property data The successful response data.
     */
    data class Successful<out T>(val data: T) : RequestState<T>()

    /**
     * Indicates that the request failed.
     *
     * @property apiException The exception that caused the failure.
     */
    data class Failed(val apiException: ApiException) : RequestState<Nothing>()
}

/**
 * Represents an exception that occurred during an API call.
 *
 * @param message The detail message string of the exception.
 */
sealed class ApiException(message: String) : Exception(message) {
    // Client-side Errors

    /**
     * Represents a 400 Bad Request error.
     */
    class BadRequest : ApiException("Bad Request")

    /**
     * Represents a 401 Unauthorized error.
     */
    class Unauthorized : ApiException("Unauthorized")

    /**
     * Represents a 403 Forbidden error.
     */
    class Forbidden : ApiException("Forbidden")

    /**
     * Represents a 404 Not Found error.
     */
    class NotFound : ApiException("Not Found")

    /**
     * Represents a 408 Request Timeout error.
     */
    class Timeout : ApiException("Request Timeout")

    /**
     * Represents a 429 Too Many Requests error.
     */
    class TooManyRequests : ApiException("Too Many Requests")

    // Server-side Errors

    /**
     * Represents a 500 Internal Server Error.
     */
    class InternalServerError : ApiException("Internal Server Error")

    /**
     * Represents a 503 Service Unavailable error.
     */
    class ServiceUnavailable : ApiException("Service Unavailable")

    // Network Errors

    /**
     * Represents a no internet connection error.
     */
    class NoInternetConnection : ApiException("No internet connection")

    /**
     * Represents a generic network error.
     */
    class NetworkError : ApiException("Network error")

    /**
     * Represents an SSL handshake failure.
     */
    class SSLHandshake : ApiException("SSL Handshake failed")

    // Other

    /**
     * Represents an unknown error.
     */
    class UnknownError : ApiException("An unknown error occurred")
}
