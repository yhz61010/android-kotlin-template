package com.leovp.framework.common.http.converters

import com.drake.net.convert.NetConverter
import com.drake.net.exception.ConvertException
import com.drake.net.exception.RequestParamsException
import com.drake.net.exception.ServerResponseException
import com.drake.net.request.kType
import com.leovp.framework.common.utils.e
import java.lang.reflect.Type
import kotlin.reflect.KType
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import okhttp3.Response

/**
 * Author: Michael Leo
 * Date: 2023/9/6 10:32
 */
class SerializationConverter : NetConverter {

    companion object {
        @OptIn(ExperimentalSerializationApi::class)
        val jsonDecoder = Json {
            explicitNulls = false
            ignoreUnknownKeys = true
            coerceInputValues = false
        }
    }

    override fun <R> onConvert(succeed: Type, response: Response): R? {
        try {
            return NetConverter.onConvert<R>(succeed, response)
        } catch (e: ConvertException) {
            val code = response.code
            if (e.cause != null) {
                e("onConvert") {
                    throwable = e.cause
                    "onConvert exception. Code: $code"
                }
            }
            when {
                code in 200..299 -> {
                    // multiCatch(
                    //     runBlock = {
                    //         // Business error.
                    //         val errorRes: ApiResponseResult = jsonDecoder.decodeFromString(bodyString)
                    //         throw ApiException(errorRes.code, errorRes.message, e)
                    //     },
                    //     exceptions = arrayOf(
                    //         SerializationException::class,
                    //         IllegalArgumentException::class
                    //     )
                    // )

                    return response.body?.string()?.let { bodyString ->
                        val kType = response.request.kType ?: throw ConvertException(
                            response, "Request does not contain KType"
                        )
                        bodyString.parseBody<R>(kType)
                    }
                }

                code in 400..499 -> throw RequestParamsException(response, code.toString(), e)

                code >= 500 -> throw ServerResponseException(response, code.toString(), e)
                else -> throw ConvertException(response = response, cause = e)
            }
        }
    }

    @Suppress("UNCHECKED_CAST", "MemberVisibilityCanBePrivate")
    fun <R> String.parseBody(succeed: KType): R? {
        return jsonDecoder.decodeFromString(Json.serializersModule.serializer(succeed), this) as? R
    }
}
