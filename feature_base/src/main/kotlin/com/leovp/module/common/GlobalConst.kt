@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package com.leovp.module.common

import com.leovp.framework.BuildConfig

/**
 * Author: Michael Leo
 * Date: 2023/7/6 15:54
 */
object GlobalConst {
    private const val TAG = "GlobalConst"

    @Suppress("SENSELESS_COMPARISON")
    const val DEBUG = BuildConfig.DEBUG_MODE
}
