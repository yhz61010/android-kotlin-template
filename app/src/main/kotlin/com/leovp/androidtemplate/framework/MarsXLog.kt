@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package com.leovp.androidtemplate.framework

import android.content.Context
import com.leovp.log.base.ILog
import com.leovp.module.common.GlobalConstants
import com.tencent.mars.xlog.Log
import com.tencent.mars.xlog.Xlog
import java.io.File

/**
 * Author: Michael Leo
 * Date: 2023/7/6 15:54
 */
class MarsXLog(private val prefix: String) : ILog {

    override var enableLog = true

    override fun getTagName(tag: String) = "$prefix-$tag"

    private val defaultLevel = if (GlobalConstants.DEBUG) Xlog.LEVEL_DEBUG else Xlog.LEVEL_INFO

    fun init(context: Context) {
        val logDir = getLogDir(context, "xlog").absolutePath
        val cacheDir = getLogDir(context, "x-cache-dir").absolutePath

        System.loadLibrary("c++_shared")
        System.loadLibrary("marsxlog")
        Log.setLogImp(Xlog())
        Log.setConsoleLogOpen(GlobalConstants.DEBUG)
        Log.appenderOpen(defaultLevel, Xlog.AppednerModeAsync, cacheDir, logDir, "main", 5)
    }

    private fun getLogDir(ctx: Context, baseFolderName: String): File {
        val builder = getBaseDirString(ctx, baseFolderName) + File.separator + "log"
        val dir = File(builder)
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    @Suppress("WeakerAccess")
    private fun getBaseDirString(ctx: Context, baseFolderName: String): String {
        return ctx.getExternalFilesDir(null)?.let { it.absolutePath + File.separator + baseFolderName } ?: ""
    }

    // ==================================================
    override fun printVerbLog(tag: String, message: String, outputType: Int) {
        Log.v(tag, message)
    }

    override fun printDebugLog(tag: String, message: String, outputType: Int) {
        Log.d(tag, message)
    }

    override fun printInfoLog(tag: String, message: String, outputType: Int) {
        Log.i(tag, message)
    }

    override fun printWarnLog(tag: String, message: String, outputType: Int) {
        Log.w(tag, message)
    }

    override fun printErrorLog(tag: String, message: String, outputType: Int) {
        Log.e(tag, message)
    }

    override fun printFatalLog(tag: String, message: String, outputType: Int) {
        Log.f(tag, message)
    }

    // ==================================================

    fun flushLog(isSync: Boolean = false) = Log.appenderFlushSync(isSync)

    fun closeLog() = Log.appenderClose()
}
