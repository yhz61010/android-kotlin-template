package com.leovp.feature.base.http.model

import androidx.annotation.Keep
import androidx.compose.runtime.Immutable
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Date: 2023/9/15 14:05
 * Author: Michael Leo
 */

@Keep
@Serializable
@OptIn(InternalSerializationApi::class)
@Immutable
open class ApiResponseResult(
    @SerialName("code") val code: Int = 0,
    @SerialName("message") val message: String? = null,
) {
    open fun resultSuccess(): Boolean {
        return code == 200
    }

    open fun isEmpty() = false
}
