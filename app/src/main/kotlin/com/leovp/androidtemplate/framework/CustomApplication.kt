package com.leovp.androidtemplate.framework

import android.util.Log
import androidx.multidex.MultiDexApplication

/**
 * Author: Michael Leo
 * Date: 2023/7/6 15:52
 */
class CustomApplication : MultiDexApplication() {
    companion object {
        private const val TAG = "CA"
    }

    override fun onCreate() {
        super.onCreate()
        InitManager.init(this)
    }

    // override fun attachBaseContext(base: Context) {
    //     // super.attachBaseContext(LangUtil.getInstance(base).setAppLanguage(base))
    //     super.attachBaseContext(this)
    //     // Reflection.unseal(base)
    //     // MultiDex.install(this) // already called in super
    // }

    // override fun onConfigurationChanged(newConfig: Configuration) {
    //     super.onConfigurationChanged(newConfig)
    //     LangUtil.getInstance(this).setAppLanguage(this)
    // }

    override fun onLowMemory() {
        Log.w(TAG, "=====> onLowMemory() <=====")
        super.onLowMemory()
    }

    override fun onTrimMemory(level: Int) {
        Log.w(TAG, "=====> onTrimMemory($level) <=====")
        super.onTrimMemory(level)
    }
}
