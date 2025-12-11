package com.piggylabs.piggyflow.data.local.db

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object AppEvents {
    // replay = 1 so late subscribers can get the last reload event if needed
    private val _dbRecreated = MutableSharedFlow<Unit>(replay = 1)
    val dbRecreated = _dbRecreated.asSharedFlow()

    // non-suspending emit (safe from places where you don't want to suspend)
    fun tryEmitDbRecreated() = _dbRecreated.tryEmit(Unit)
}
