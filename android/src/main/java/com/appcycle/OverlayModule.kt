package com.appcycle

import android.content.Intent
import android.util.Log
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.modules.core.DeviceEventManagerModule

/**
 * Native module for global overlay / BottomSheet: open/close overlay and read launch intent.
 * openOverlay() starts the app's main activity with openOverlay=true so the app can show the overlay.
 * The overlay UI is rendered in the main app's React tree (same bundle, app API access).
 */
class OverlayModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {

  override fun getName(): String = "AppcycleOverlay"

  /**
   * Starts the app's launcher activity (or the activity that handles SHOW_OVERLAY) with
   * openOverlay=true. If the app is in background, it comes to front; the app should read
   * the launch extra and show the overlay.
   */
  @ReactMethod
  fun openOverlay() {
    val ctx = reactApplicationContext
    val intent = Intent(OverlayModule.ACTION_SHOW_OVERLAY).apply {
      setPackage(ctx.packageName)
      addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      putExtra(EXTRA_OPEN_OVERLAY, true)
    }
    try {
      ctx.startActivity(intent)
    } catch (e: Exception) {
      Log.e(TAG, "openOverlay failed", e)
    }
  }

  /**
   * Opens the system Settings screen where the user can set the default digital assistant app.
   * After the user selects this app as the default assistant, long-press home (or the
   * system assistant shortcut) will invoke this app's VoiceInteractionService, which can
   * then open the overlay. Call this from a "Set as default assistant" button.
   */
  @ReactMethod
  fun openDefaultAssistantSettings() {
    val ctx = reactApplicationContext
    val intent = Intent(android.provider.Settings.ACTION_VOICE_INPUT_SETTINGS).apply {
      addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    try {
      ctx.startActivity(intent)
    } catch (e: Exception) {
      Log.e(TAG, "openDefaultAssistantSettings failed", e)
    }
  }

  /**
   * Finishes the current activity and starts the app's main launcher activity.
   * Use when the user is in overlay-only mode (e.g. OverlayOnlyActivity) and taps "Open full app".
   */
  @ReactMethod
  fun openFullApp() {
    val ctx = reactApplicationContext
    val activity = ctx.currentActivity ?: return
    val intent = Intent(Intent.ACTION_MAIN).apply {
      addCategory(Intent.CATEGORY_LAUNCHER)
      setPackage(ctx.packageName)
      addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    try {
      activity.finish()
      ctx.startActivity(intent)
    } catch (e: Exception) {
      Log.e(TAG, "openFullApp failed", e)
    }
  }

  /**
   * Finishes the current activity. Use when the overlay is closed in overlay-only mode
   * (OverlayOnlyActivity) so the activity is removed from the stack and the app does not
   * stay in background; the next long-press will start a fresh overlay activity.
   */
  @ReactMethod
  fun finishActivity() {
    reactApplicationContext.currentActivity?.finish()
  }

  /**
   * Emits an event so JS can hide the overlay. The overlay is controlled by React state.
   */
  @ReactMethod
  fun closeOverlay() {
    emit(EVENT_CLOSE_OVERLAY, null)
  }

  /**
   * Returns the value of the launch intent extra and marks it as consumed so subsequent
   * calls return false. Use this on app mount/resume to detect openOverlay=true (e.g.
   * when the app was started by the background service to show the overlay).
   */
  @ReactMethod
  fun getAndClearLaunchExtra(key: String, promise: Promise) {
    val activity = reactApplicationContext.currentActivity ?: run {
      promise.resolve(false)
      return
    }
    val intent = activity.intent ?: run {
      promise.resolve(false)
      return
    }
    synchronized(consumedKeys) {
      val id = System.identityHashCode(intent)
      if (consumedKeys.contains(id to key)) {
        promise.resolve(false)
        return
      }
      val value = intent.getBooleanExtra(key, false)
      consumedKeys.add(id to key)
      promise.resolve(value)
    }
  }

  private fun emit(event: String, data: Any?) {
    if (!reactApplicationContext.hasActiveReactInstance()) return
    reactApplicationContext
      .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
      .emit(event, data)
  }

  companion object {
    const val TAG = "AppcycleOverlay"
    /** Intent action the app's Activity should handle to receive openOverlay from background. */
    const val ACTION_SHOW_OVERLAY = "com.appcycle.SHOW_OVERLAY"
    const val EXTRA_OPEN_OVERLAY = "openOverlay"
    /** When true, the app should show only the overlay (no full app UI); used when launched from assistant. */
    const val EXTRA_OPEN_OVERLAY_ONLY = "openOverlayOnly"
    const val EVENT_CLOSE_OVERLAY = "onOverlayCloseRequested"

    private val consumedKeys = mutableSetOf<Pair<Int, String>>()
  }
}
