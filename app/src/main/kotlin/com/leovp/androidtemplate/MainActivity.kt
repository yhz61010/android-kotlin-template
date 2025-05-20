package com.leovp.androidtemplate

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowCompat
import com.leovp.androidtemplate.framework.InitManager
import com.leovp.feature.base.ui.theme.ImmersiveTheme
import com.leovp.framework.common.utils.d
import dagger.hilt.android.AndroidEntryPoint

private const val TAG = "MA"

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        d(TAG) { "=====> Enter MainActivity <=====" }
        InitManager.init(application)
        // RequestUtil.initNetEngine(baseUrl = BuildConfig.DOMAIN_LINK)
        super.onCreate(savedInstanceState)
        // This app draws behind the system bars, so we want to handle fitting system windows
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            ImmersiveTheme(
                color = Color.Transparent,
                dynamicColor = false,
                lightStatusBar = false
            ) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    MainScreen()
                }
            }
        }
    }
}

@Composable
fun MainScreen(modifier: Modifier = Modifier) {
    Image(
        painterResource(R.drawable.app_beauty),
        contentDescription = null,
        modifier = modifier
            .fillMaxWidth()
            .clip(shape = MaterialTheme.shapes.medium),
        alignment = Alignment.TopStart,
        contentScale = ContentScale.FillWidth
    )
}

@Preview("Main Screen")
@Preview("Main Screen (dark)", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PreviewMainScreen() {
    ImmersiveTheme(
        color = Color.Transparent,
        dynamicColor = false,
        lightStatusBar = false
    ) {
        MainScreen()
    }
}
