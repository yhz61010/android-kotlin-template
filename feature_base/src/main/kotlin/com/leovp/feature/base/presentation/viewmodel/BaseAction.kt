package com.leovp.feature.base.presentation.viewmodel

interface BaseAction<State> {
    fun execute(state: State): State
}
