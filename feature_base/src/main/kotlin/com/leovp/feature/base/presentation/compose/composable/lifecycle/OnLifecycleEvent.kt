package com.leovp.feature.base.presentation.compose.composable.lifecycle

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

/**
 * Author: Michael Leo
 * Date: 2025/4/7 10:55
 */

/**
 * Usage:
 * ```kotlin
 * @Composable
 * fun MyScreen(viewModel: MyViewModel = viewModel()) {
 *     OnResume {
 *         viewModel.onResumeLogic()
 *     }
 *
 *     // UI content here...
 * }
 * ```
 */
@Composable
fun OnLifecycleEvent(
    lifecycleEvent: Lifecycle.Event,
    onEvent: () -> Unit
) {
    val currentOnEvent by rememberUpdatedState(onEvent)

    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == lifecycleEvent) {
                currentOnEvent()
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
}

@Composable
fun OnResume(onEvent: () -> Unit) = OnLifecycleEvent(Lifecycle.Event.ON_RESUME, onEvent)

@Composable
fun OnPause(onEvent: () -> Unit) = OnLifecycleEvent(Lifecycle.Event.ON_PAUSE, onEvent)

@Composable
fun OnStart(onEvent: () -> Unit) = OnLifecycleEvent(Lifecycle.Event.ON_START, onEvent)

@Composable
fun OnStop(onEvent: () -> Unit) = OnLifecycleEvent(Lifecycle.Event.ON_STOP, onEvent)

@Composable
fun OnCreate(onEvent: () -> Unit) = OnLifecycleEvent(Lifecycle.Event.ON_CREATE, onEvent)

@Composable
fun OnDestroy(onEvent: () -> Unit) = OnLifecycleEvent(Lifecycle.Event.ON_DESTROY, onEvent)
