package com.leovp.feature.base

import android.app.Application
import com.leovp.android.exts.toast
import com.leovp.log.LogContext

/**
 * Author: Michael Leo
 * Date: 2023/7/6 16:06
 */
object CrashHandler : Thread.UncaughtExceptionHandler {
    private const val TAG = "crash"

    private var defaultExceptionHandler: Thread.UncaughtExceptionHandler? = null

    private lateinit var appContext: Application
    fun initCrashHandler(application: Application) {
        appContext = application
        defaultExceptionHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler(this)
    }

    override fun uncaughtException(thread: Thread, ex: Throwable) {
        if (!handleException(ex)) {
            LogContext.log.e(TAG, "Not handler exception. Thread=${thread.name}", ex)
            // If user do not process this exception, handle it by system.
            defaultExceptionHandler?.uncaughtException(thread, ex)
        } else {
            // Handler exception by yourself
            LogContext.log.e(TAG, "Exception handled by handleException", ex)
        }
    }

    /**
     * Process your exception.
     *
     * @param ex The exception
     * @return true: If this exception has been consumed, return true. Otherwise return false.
     */
    private fun handleException(ex: Throwable): Boolean {
        LogContext.log.e(TAG, "=====> Crash <=====", ex)
        if (CrashHandler::appContext.isInitialized) {
            appContext.toast(
                ex.message,
                error = true,
                debug = true,
                longDuration = true
            )
        }
        return true
    }
}
