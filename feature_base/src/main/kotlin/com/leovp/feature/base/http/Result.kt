@file:Suppress("unused")

package com.leovp.feature.base.http

import com.drake.net.exception.HttpResponseException
import com.drake.net.exception.RequestParamsException
import com.leovp.feature.base.http.model.ApiResponseResult
import com.leovp.framework.common.exception.ApiException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json

/**
 * A generic class that holds a value or an exception
 */
sealed interface Result<out R> {
    data class Success<out T>(val data: T) : Result<T>
    data class Failure(val exception: ApiException) : Result<Nothing>

    val isSuccess: Boolean get() = this is Success
    val isFailure: Boolean get() = this is Failure

    companion object {
        @OptIn(ExperimentalSerializationApi::class)
        val jsonDecoder = Json {
            explicitNulls = false
            ignoreUnknownKeys = true
            coerceInputValues = false
        }
    }
}

fun <R, T : R> Result<T>.get(): R = (this as Result.Success<T>).data

fun <R, T : R> Result<T>.getOrDefault(defaultValue: T): R = when {
    isFailure -> defaultValue
    else -> (this as Result.Success<T>).data
}

fun <T> Result<T>.getOrNull(): T? = when {
    isFailure -> null
    else -> (this as Result.Success<T>).data
}

fun <T> Result<T>.getOrThrow(): T = when {
    isFailure -> throw (this as Result.Failure).exception
    else -> (this as Result.Success<T>).data
}

inline fun <R, T : R> Result<T>.getOrElse(onFailure: (exception: ApiException) -> R): R {
    return when (val exception = exceptionOrNull()) {
        null -> (this as Result.Success<T>).data
        else -> onFailure(exception)
    }
}

inline fun <T, R> Result<T>.map(transform: (value: T) -> R): Result<R> {
    return when (val exception = exceptionOrNull()) {
        null -> Result.Success(transform((this as Result.Success<T>).data))
        else -> Result.Failure(exception)
    }
}

fun <T> Result<T>.exceptionOrNull(): ApiException? =
    when {
        isFailure -> (this as Result.Failure).exception
        else -> null
    }

inline fun <T> Result<T>.onSuccess(action: (value: T) -> Unit): Result<T> {
    if (isSuccess) action((this as Result.Success<T>).data)
    return this
}

inline fun <T> Result<T>.onFailure(action: (exception: ApiException) -> Unit): Result<T> {
    exceptionOrNull()?.let { action(it) }
    return this
}

inline fun <T, R> Result<T>.fold(
    onSuccess: (value: T) -> R,
    onFailure: (exception: ApiException) -> R
): R {
    return when (val exception = exceptionOrNull()) {
        null -> onSuccess((this as Result.Success<T>).data)
        else -> onFailure(exception)
    }
}

// ----------

/**
 * Wrap the [block] result into [Result].
 *
 * Note that, this function is very useful when you wrap your http request block.
 * In this project, it wraps http request which is implemented by _Net_.
 *
 * If the [Result] is failure, the [ApiException] will be used as [Result.Failure] parameter.
 *
 * @param dispatcher The dispatcher for suspend [block] function. [Dispatchers.Main] by default.
 */
suspend inline fun <reified R> result(
    dispatcher: CoroutineDispatcher = Dispatchers.Main,
    crossinline block: suspend CoroutineScope.() -> R
): Result<R> = supervisorScope {
    runCatching {
        Result.Success(withContext(dispatcher) { block() })
    }.getOrElse { err ->
        var code = -65535
        var message: String? = err.message
        var exception: Throwable = err

        // err can be one of the following exception:
        // - RequestParamsException
        // - ServerResponseException
        // - ConvertException
        // All above exceptions are inherited from HttpResponseException.

        when (err) {
            is RequestParamsException -> {
                err.response.body?.string()?.let { bodyString ->
                    runCatching {
                        val errorData =
                            Result.jsonDecoder.decodeFromString<ApiResponseResult>(bodyString)
                        code = errorData.code
                        message = errorData.message
                    }.onFailure {
                        // SerializationException
                        // IllegalArgumentException
                        message = it.message
                    }
                }
            }

            is HttpResponseException -> {
                val cause = err.cause
                if (cause is ApiException) {
                    code = cause.code
                    message = cause.message
                } else {
                    code = err.response.code
                    message = err.response.message
                }
            }
        }

        Result.Failure(ApiException(code, message, exception))
    }
}
