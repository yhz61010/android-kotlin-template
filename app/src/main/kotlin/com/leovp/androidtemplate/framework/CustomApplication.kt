package com.leovp.androidtemplate.framework

import android.util.Log
import androidx.multidex.MultiDexApplication
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.disk.DiskCache
import coil3.disk.directory
import coil3.request.crossfade
import com.leovp.feature.base.GlobalConst
import com.leovp.feature.base.log.MarsXLog
import com.leovp.feature.base.pref.MMKVPref
import com.leovp.log.LogContext
import com.leovp.pref.PrefContext
import dagger.hilt.android.HiltAndroidApp

/**
 * Author: Michael Leo
 * Date: 2023/7/6 15:52
 */

@HiltAndroidApp
class CustomApplication :
    MultiDexApplication(),
    SingletonImageLoader.Factory {
    companion object {
        private const val TAG = "CA"
    }

    override fun onCreate() {
        super.onCreate()

        // Log must be initialized first.
        LogContext.setLogImpl(
            MarsXLog("AOS").apply {
                @Suppress("SENSELESS_COMPARISON")
                init(this@CustomApplication, GlobalConst.CONSOLE_LOG_OPEN)
            },
        )
        PrefContext.setPrefImpl(MMKVPref(this@CustomApplication))
    }

    override fun newImageLoader(context: PlatformContext): ImageLoader {
        return ImageLoader
            .Builder(context)
            .crossfade(true)
            // Disable `Cache-Control` header support in order to disable disk caching.
            // .respectCacheHeaders(false)
            // .memoryCache {
            //     MemoryCache.Builder(this)
            //         .maxSizePercent(0.25)
            //         .build()
            // }
            .diskCache {
                DiskCache
                    .Builder()
                    .directory(context.cacheDir.resolve("image_cache"))
                    // .maxSizePercent(0.02)
                    .build()
            }.build()
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
