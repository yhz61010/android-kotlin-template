@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package com.leovp.feature.base

/**
 * Author: Michael Leo
 * Date: 2023/7/6 15:54
 */
@Suppress("SENSELESS_COMPARISON")
object GlobalConst {
    private const val TAG = "GlobalConst"

    const val DEBUG = BuildConfig.DEBUG_MODE
    const val CONSOLE_LOG_OPEN = BuildConfig.CONSOLE_LOG_OPEN

    const val VERSION_NAME = BuildConfig.VERSION_NAME
}
