package com.appcycle

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.accessibility.AccessibilityEvent

/**
 * Accessibility Service that opens the app's overlay when the user triggers the
 * accessibility button (nav bar button on Android 8+) or the accessibility shortcut
 * (e.g. hold both volume keys, if configured for this service).
 *
 * Enable in Settings → Accessibility → [App name] → turn on. On some devices you can
 * assign the accessibility shortcut (e.g. Volume up + Volume down) to this service so
 * that key combo opens the overlay.
 *
 * When the accessibility button/shortcut is used, this service starts the main Activity
 * with [OverlayModule.ACTION_SHOW_OVERLAY] and [OverlayModule.EXTRA_OPEN_OVERLAY]=true,
 * reusing the same flow as the runtime service's triggerShowOverlay().
 *
 * Note: Long-press home / system "assistant" cannot be intercepted by a normal app;
 * the system launches the default assistant (e.g. Google Assistant). This service
 * provides an alternative trigger (accessibility button/shortcut). For a reliable
 * trigger without enabling accessibility, use the Quick Settings tile or the
 * notification action "Open overlay".
 */
class AppcycleOverlayAccessibilityService : AccessibilityService() {

  override fun onServiceConnected() {
    super.onServiceConnected()
    // Accessibility button API is available from API 26 (O).
    if (android.os.Build.VERSION.SDK_INT >= 26) {
      val info = serviceInfo ?: return
      info.flags = info.flags or AccessibilityServiceInfo.FLAG_REQUEST_ACCESSIBILITY_BUTTON
      serviceInfo = info
      val controller = accessibilityButtonController ?: return
      if (!controller.isAccessibilityButtonAvailable) {
        Log.d(TAG, "Accessibility button not available on this device")
        return
      }
      controller.registerAccessibilityButtonCallback(accessibilityButtonCallback, Handler(Looper.getMainLooper()))
      Log.d(TAG, "Accessibility button callback registered")
    }
  }

  override fun onAccessibilityEvent(event: AccessibilityEvent?) {
    // We only react to the accessibility button; ignore other events.
  }

  override fun onInterrupt() {}

  private fun triggerShowOverlay() {
    val intent = Intent(OverlayModule.ACTION_SHOW_OVERLAY).apply {
      setPackage(packageName)
      addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      putExtra(OverlayModule.EXTRA_OPEN_OVERLAY, true)
    }
    try {
      startActivity(intent)
      Log.d(TAG, "Started main Activity with openOverlay=true")
    } catch (e: Exception) {
      Log.e(TAG, "triggerShowOverlay failed", e)
    }
  }

  private val accessibilityButtonCallback =
    object : android.accessibilityservice.AccessibilityButtonController.AccessibilityButtonCallback() {
      override fun onClicked(controller: android.accessibilityservice.AccessibilityButtonController) {
        Log.d(TAG, "Accessibility button clicked")
        triggerShowOverlay()
      }

      override fun onAvailabilityChanged(
        controller: android.accessibilityservice.AccessibilityButtonController,
        available: Boolean
      ) {
        Log.d(TAG, "Accessibility button available: $available")
      }
    }

  companion object {
    private const val TAG = "AppcycleOverlayA11y"
  }
}
