package com.leovp.feature.base.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.leovp.feature.base.GlobalConst
import com.leovp.feature.base.presentation.viewmodel.lifecycle.LifecycleAware
import com.leovp.log.base.w
import kotlin.properties.Delegates
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

abstract class BaseViewModel<State : BaseState, Action : BaseAction<State>>(initialState: State) :
    ViewModel(), LifecycleAware {

    companion object {
        private const val TAG = "BaseVM"
    }

    private val _uiStateFlow = MutableStateFlow(initialState)
    val uiStateFlow = _uiStateFlow.asStateFlow()

    private var stateTimeTravelDebugger: StateTimeTravelDebugger? = null

    init {
        @Suppress("SENSELESS_COMPARISON")
        if (GlobalConst.DEBUG) {
            stateTimeTravelDebugger = StateTimeTravelDebugger(this::class.java.simpleName)
        }
    }

    // Delegate handles state event deduplication (multiple states of the same type holding the same data
    // will not be emitted multiple times to UI)
    private var state by Delegates.observable(initialState) { _, old, new ->
        if (old != new) {
            viewModelScope.launch {
                _uiStateFlow.value = new
            }

            stateTimeTravelDebugger?.apply {
                addStateTransition(old, new)
                logLast()
            }
        }
    }

    protected fun sendAction(action: Action) {
        stateTimeTravelDebugger?.addAction(action)
        state = action.execute(state)
    }

    // ==============================

    override fun onResume() {
        super.onResume()
        w(TAG) { "onResume()" }
    }

    override fun onPause() {
        super.onPause()
        w(TAG) { "onPause()" }
    }

    override fun onStart() {
        super.onStart()
        w(TAG) { "onStart()" }
    }

    override fun onStop() {
        super.onStop()
        w(TAG) { "onStop()" }
    }

    override fun onDestroy() {
        super.onDestroy()
        w(TAG) { "onDestroy()" }
    }
}
