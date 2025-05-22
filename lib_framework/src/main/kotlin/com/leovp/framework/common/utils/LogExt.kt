@file:Suppress("unused")

package com.leovp.framework.common.utils

import com.leovp.framework.BuildConfig
import com.leovp.log.LogContext
import com.leovp.log.base.LogOutType

/**
 * Author: Michael Leo
 * Date: 2023/9/19 16:08
 */

interface ILogConfig {
    var throwable: Throwable?
    var fullOutput: Boolean
    var outputType: LogOutType
}

class LogConfig : ILogConfig {
    override var throwable: Throwable? = null
    override var fullOutput: Boolean = false
    override var outputType: LogOutType = LogOutType.COMMON
    lateinit var message: () -> String?
}

class LogConfig4Debug : ILogConfig {
    override var throwable: Throwable? = null
    override var fullOutput: Boolean = false
    override var outputType: LogOutType = LogOutType.COMMON
    lateinit var generateMsg: () -> Any?
}

inline fun d(tag: String = "", config: LogConfig4Debug.() -> Unit) {
    val logConfig = LogConfig4Debug().apply(config)
    @Suppress("SENSELESS_COMPARISON")
    if (BuildConfig.DEBUG_MODE) {
        val ret = logConfig.generateMsg()
        if (ret is String?) {
            LogContext.log.d(
                tag = tag,
                message = ret,
                fullOutput = logConfig.fullOutput,
                throwable = logConfig.throwable,
                outputType = logConfig.outputType
            )
        }
    }
}

inline fun i(tag: String = "", config: LogConfig.() -> Unit) {
    val logConfig = LogConfig().apply(config)
    LogContext.log.i(
        tag = tag,
        message = logConfig.message(),
        fullOutput = logConfig.fullOutput,
        throwable = logConfig.throwable,
        outputType = logConfig.outputType
    )
}

inline fun w(tag: String = "", config: LogConfig.() -> Unit) {
    val logConfig = LogConfig().apply(config)
    LogContext.log.w(
        tag = tag,
        message = logConfig.message(),
        fullOutput = logConfig.fullOutput,
        throwable = logConfig.throwable,
        outputType = logConfig.outputType
    )
}

inline fun e(tag: String = "", config: LogConfig.() -> Unit) {
    val logConfig = LogConfig().apply(config)
    LogContext.log.e(
        tag = tag,
        message = logConfig.message(),
        fullOutput = logConfig.fullOutput,
        throwable = logConfig.throwable,
        outputType = logConfig.outputType
    )
}
