package com.leovp.feature.base.http.interceptors

import com.leovp.framework.common.utils.d
import com.leovp.log.base.LogOutType
import kotlin.collections.iterator
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Author: Michael Leo
 * Date: 2023/9/6 09:19
 */
class HeaderInterceptor(private val headerMap: Map<String, String>? = null) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response = chain.request().let {
        val request = headerMap?.let { headers ->
            val builder = it.newBuilder()
            for ((k, v) in headers) {
                d(tag = "Interceptor") {
                    outputType = LogOutType.Companion.HTTP_HEADER
                    "Assign cookie: $k=$v"
                }
                builder.addHeader(k, v)
            }
            builder.build()
        } ?: it
        chain.proceed(request)
    }
}
