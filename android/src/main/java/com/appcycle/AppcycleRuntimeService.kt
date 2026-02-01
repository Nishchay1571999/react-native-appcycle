package com.appcycle

import android.app.*
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat

class AppcycleRuntimeService : Service() {

  companion object {
    const val TAG = "Appcycle"
    const val CHANNEL_ID = "appcycle_runtime"
    const val NOTIFICATION_ID = 1001
    private const val HEARTBEAT_INTERVAL_MS = 3000L

    const val ACTION_START = "APP_CYCLE_START"
    const val ACTION_STOP = "APP_CYCLE_STOP"
    /** Start main app with openOverlay=true so the app opens the overlay on launch. */
    const val ACTION_TRIGGER_OVERLAY = "APP_CYCLE_TRIGGER_OVERLAY"
  }

  /**
   * Starts the main app's Activity with openOverlay=true. Call this from the :runtime process
   * when the background service receives a "show overlay" signal (e.g. from a broadcast or
   * later from long-press home / accessibility). The app must declare an intent-filter for
   * [OverlayModule.ACTION_SHOW_OVERLAY] on its main/launcher Activity.
   */
  fun triggerShowOverlay() {
    val intent = Intent(OverlayModule.ACTION_SHOW_OVERLAY).apply {
      setPackage(applicationContext.packageName)
      addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      putExtra(OverlayModule.EXTRA_OPEN_OVERLAY, true)
    }
    try {
      applicationContext.startActivity(intent)
    } catch (e: Exception) {
      Log.e(TAG, "triggerShowOverlay failed", e)
    }
  }

  private val handler = Handler(Looper.getMainLooper())
  private val heartbeatRunnable = object : Runnable {
    override fun run() {
      if (isMainProcess()) {
        AppcycleModule.emitHeartbeat()
      } else {
        Log.d(TAG, "is live")
      }
      handler.postDelayed(this, HEARTBEAT_INTERVAL_MS)
    }
  }

  /** Service runs in :runtime process; only main process has React/JS context. */
  private fun isMainProcess(): Boolean {
    val processName = applicationContext.applicationInfo.processName
    return !processName.contains(":runtime")
  }

  override fun onCreate() {
    super.onCreate()
    RuntimeState.setRuntimeRunning(true)
    Log.d(TAG, "Runtime service created")
    createNotificationChannel()
    if (!startForegroundSafe()) {
      Log.w(TAG, "Cannot start as foreground service (e.g. app in background on Android 12+). Start runtime from the app when it is in foreground.")
      stopSelf()
      return
    }
    handler.postDelayed(heartbeatRunnable, HEARTBEAT_INTERVAL_MS)
  }

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    when (intent?.action) {
      ACTION_START -> {
        Log.d(TAG, "Runtime started")
        if (!startForegroundSafe()) {
          Log.w(TAG, "Cannot start as foreground (app in background?). Start runtime from the app when in foreground.")
          stopSelf()
          return START_NOT_STICKY
        }
        RuntimeState.setRuntimeRunning(true)
        handler.removeCallbacks(heartbeatRunnable)
        handler.postDelayed(heartbeatRunnable, HEARTBEAT_INTERVAL_MS)
      }
      ACTION_STOP -> {
        handler.removeCallbacks(heartbeatRunnable)
        stopSelf()
      }
      ACTION_TRIGGER_OVERLAY -> {
        triggerShowOverlay()
      }
    }
    return START_STICKY
  }
  

  override fun onDestroy() {
    handler.removeCallbacks(heartbeatRunnable)
    Log.d(TAG, "Runtime service destroyed")
    RuntimeState.setRuntimeRunning(false)
    super.onDestroy()
  }

  override fun onBind(intent: Intent?): IBinder? = null

  /**
   * Calls startForeground() and returns true if allowed. On Android 12+ (API 31),
   * foreground service start is not allowed when the app is in background; this
   * catches ForegroundServiceStartNotAllowedException and returns false so we can
   * stop without crashing.
   */
  private fun startForegroundSafe(): Boolean {
    return try {
      startForeground(NOTIFICATION_ID, createNotification())
      true
    } catch (e: Exception) {
      if (android.os.Build.VERSION.SDK_INT >= 31 && e is ForegroundServiceStartNotAllowedException) {
        Log.w(TAG, "ForegroundServiceStartNotAllowedException: start the runtime from the app when it is in foreground.", e)
      } else {
        Log.e(TAG, "startForeground failed", e)
      }
      false
    }
  }

  private fun createNotification(): Notification {
    val openOverlayIntent = Intent(OverlayModule.ACTION_SHOW_OVERLAY).apply {
      setPackage(applicationContext.packageName)
      addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      putExtra(OverlayModule.EXTRA_OPEN_OVERLAY, true)
    }
    val openOverlayPending = PendingIntent.getActivity(
      this,
      0,
      openOverlayIntent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    return NotificationCompat.Builder(this, CHANNEL_ID)
      .setContentTitle("Assistant running")
      .setContentText("Listening for triggers")
      .setSmallIcon(android.R.drawable.ic_btn_speak_now)
      .setOngoing(true)
      .addAction(
        android.R.drawable.ic_btn_speak_now,
        getString(R.string.appcycle_notification_action_open_overlay),
        openOverlayPending
      )
      .build()
  }

  private fun createNotificationChannel() {
    val channel = NotificationChannel(
      CHANNEL_ID,
      "Appcycle Runtime",
      NotificationManager.IMPORTANCE_LOW
    )
    val manager = getSystemService(NotificationManager::class.java)
    manager.createNotificationChannel(channel)
  }
}
