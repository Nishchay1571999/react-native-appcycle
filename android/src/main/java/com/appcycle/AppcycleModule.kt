package com.appcycle

import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.LifecycleEventListener
import com.facebook.react.modules.core.DeviceEventManagerModule
import android.util.Log
import android.content.Intent
import android.os.Handler
import android.os.Looper


class AppcycleModule(
  private val reactContext: ReactApplicationContext
) : ReactContextBaseJavaModule(reactContext),
    LifecycleEventListener {

  private var hasJSListeners = false

  private val mainHandler = Handler(Looper.getMainLooper())
  private val jsHeartbeatRunnable = object : Runnable {
    override fun run() {
      emit("onIsLive")
      mainHandler.postDelayed(this, 3000L)
    }
  }

  override fun getName(): String = "Appcycle"

  override fun initialize() {
    super.initialize()
    reactContext.addLifecycleEventListener(this)
    instance = this
  }

  override fun onCatalystInstanceDestroy() {
    instance = null
    mainHandler.removeCallbacks(jsHeartbeatRunnable)
    reactContext.removeLifecycleEventListener(this)
    super.onCatalystInstanceDestroy()
  }

  companion object {
    @Volatile
    private var instance: AppcycleModule? = null

    /** Called by AppcycleRuntimeService every 3s to emit "is live" to JS (console). */
    fun emitHeartbeat() {
      instance?.emit("onIsLive")
    }
  }

  // ---------- React lifecycle ----------

  override fun onHostResume() {
    Log.d("Appcycle", "onHostResume → foreground")

    if (!RuntimeState.isForeground()) {
      RuntimeState.setForeground(true)
      emit("onForeground")
    }
  }

  override fun onHostPause() {
    Log.d("Appcycle", "onHostPause → background")

    if (RuntimeState.isForeground()) {
      RuntimeState.setForeground(false)
      emit("onBackground")
    }
  }

  override fun onHostDestroy() {}

  // ---------- Events ----------

  private fun emit(event: String) {
    if (!hasJSListeners) return
    if (!reactContext.hasActiveReactInstance()) return

    reactContext
      .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
      .emit(event, null)
  }

  // ---------- JS API ----------

  @ReactMethod
  fun getCurrentState(promise: Promise) {
    promise.resolve(
      if (RuntimeState.isForeground()) "foreground" else "background"
    )
  }

  @ReactMethod
  fun addListener(eventName: String) {
    hasJSListeners = true
  }

  @ReactMethod
  fun removeListeners(count: Int) {
    hasJSListeners = false
  }
  @ReactMethod 
  fun init() {
    RuntimeState.setForeground(reactContext.hasActiveReactInstance())
  }

  @ReactMethod
  fun startRuntime() {
    val intent = Intent(reactContext, AppcycleRuntimeService::class.java)
    intent.action = AppcycleRuntimeService.ACTION_START
    reactContext.startForegroundService(intent)
    mainHandler.removeCallbacks(jsHeartbeatRunnable)
    mainHandler.postDelayed(jsHeartbeatRunnable, 3000L)
  }

  @ReactMethod
  fun stopRuntime() {
    mainHandler.removeCallbacks(jsHeartbeatRunnable)
    val intent = Intent(reactContext, AppcycleRuntimeService::class.java)
    intent.action = AppcycleRuntimeService.ACTION_STOP
    reactContext.startService(intent)
  }

  /**
   * For demo: starts the runtime service with ACTION_TRIGGER_OVERLAY so the service (in :runtime)
   * starts the main app with openOverlay=true. Simulates "background service receives message →
   * start app with openOverlay=true → app opens overlay".
   */
  @ReactMethod
  fun triggerShowOverlayFromBackground() {
    val intent = Intent(reactContext, AppcycleRuntimeService::class.java)
    intent.action = AppcycleRuntimeService.ACTION_TRIGGER_OVERLAY
    reactContext.startService(intent)
  }
}
