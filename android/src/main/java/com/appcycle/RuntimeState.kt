package com.appcycle

import android.util.Log
import java.util.concurrent.atomic.AtomicBoolean

object RuntimeState {

  private const val TAG = "AppcycleState"

  private val isForeground = AtomicBoolean(false)
  private val isRuntimeRunning = AtomicBoolean(false)

  // ---------- Foreground ----------

  fun setForeground(value: Boolean) {
    isForeground.set(value)
    Log.d(TAG, "Foreground = $value")
  }

  fun isForeground(): Boolean {
    return isForeground.get()
  }

  // ---------- Runtime ----------

  fun setRuntimeRunning(value: Boolean) {
    isRuntimeRunning.set(value)
    Log.d(TAG, "Runtime running = $value")
  }

  fun isRuntimeRunning(): Boolean {
    return isRuntimeRunning.get()
  }
}
