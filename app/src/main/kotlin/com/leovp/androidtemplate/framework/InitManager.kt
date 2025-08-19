package com.leovp.androidtemplate.framework

import android.app.Application
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import com.leovp.android.exts.LeoToast
import com.leovp.android.exts.toast
import com.leovp.android.ui.ForegroundComponent
import com.leovp.androidbase.exts.android.closeAndroidPDialog
import com.leovp.androidbase.utils.CrashHandler
import com.leovp.androidtemplate.R
import com.leovp.feature.base.GlobalConst
import com.leovp.log.base.w
import io.karn.notify.Notify

/**
 * Author: Michael Leo
 * Date: 2021/10/11 11:02
 */

private const val TAG = "IM"

object InitManager {
    fun init(app: Application) {
        val st = SystemClock.elapsedRealtime()
        CrashHandler.initCrashHandler { th, err ->
            app.toast("Crash...", error = true)
        }

        @Suppress("SENSELESS_COMPARISON")
        LeoToast.getInstance(app).init(
            LeoToast.ToastConfig(
                GlobalConst.DEBUG,
                R.mipmap.app_ic_launcher_round,
            ),
        )

        closeAndroidPDialog()

        // Ignore netty start error cause by log4j
        // InternalLoggerFactory.setDefaultFactory(JdkLoggerFactory.INSTANCE)

        ForegroundComponent.init(app)

        Notify.defaultConfig {
            header {
                icon = R.mipmap.app_ic_launcher_round
                // color = Color(0xffff0000).toArgb()
            }
            alerting("default-notification") {
                channelName = app.getString(R.string.app_notification_channel_name)
                channelDescription =
                    app.getString(R.string.app_notification_channel_name_desc)
                lockScreenVisibility = NotificationCompat.VISIBILITY_PRIVATE
                channelImportance = Notify.IMPORTANCE_LOW
            }
        }
        val ed = SystemClock.elapsedRealtime()
        w(TAG) { "Initialize manager cost: ${ed - st}ms" }
    }
}
