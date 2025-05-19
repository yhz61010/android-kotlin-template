package com.leovp.feature.base.http

import android.content.Context
import com.drake.net.NetConfig
import com.drake.net.interceptor.RetryInterceptor
import com.drake.net.okhttp.setConverter
import java.util.concurrent.TimeUnit
import com.leovp.feature.base.http.interceptors.HeaderInterceptor
import com.leovp.framework.common.http.converters.SerializationConverter
import com.leovp.feature.base.http.interceptors.HttpLoggingInterceptor
import com.leovp.feature.base.http.interceptors.UserAgentInterceptor

/**
 * Author: Michael Leo
 * Date: 2023/9/5 17:18
 */
object RequestUtil {
    fun initNetEngine(
        baseUrl: String,
        context: Context? = null,
        headerMap: Map<String, String>? = null,
    ) {
        NetConfig.initialize(baseUrl, context) {
            connectTimeout(20, TimeUnit.SECONDS)
            readTimeout(20, TimeUnit.SECONDS)
            writeTimeout(20, TimeUnit.SECONDS)
            // addInterceptor(AuthenticationInterceptor(GlobalConst.API_TOKEN))
            addInterceptor(UserAgentInterceptor())
            addInterceptor(
                HeaderInterceptor(
                    headerMap
                )
            )
            addInterceptor(
                HttpLoggingInterceptor()
                    .apply { level = HttpLoggingInterceptor.Level.BODY })
            addInterceptor(RetryInterceptor(3))
            // setConverter(GsonConverter())
            setConverter(SerializationConverter())
        }
    }
}
