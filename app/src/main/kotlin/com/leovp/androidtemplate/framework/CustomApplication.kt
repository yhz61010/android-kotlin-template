package com.leovp.androidtemplate.framework

import android.util.Log
import androidx.multidex.MultiDexApplication
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.compose.AsyncImage
import coil.disk.DiskCache
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
    ImageLoaderFactory {
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

    /**
     * Create the singleton [ImageLoader].
     * This is used by [AsyncImage] to load images in the app.
     */
    override fun newImageLoader(): ImageLoader =
        ImageLoader
            .Builder(this)
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
                    .directory(this.cacheDir.resolve("image_cache"))
                    // .maxSizePercent(0.02)
                    .build()
            }.build()

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
