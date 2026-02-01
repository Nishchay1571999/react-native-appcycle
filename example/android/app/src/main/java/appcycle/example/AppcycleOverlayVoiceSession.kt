package appcycle.example

import android.content.Intent
import android.service.voice.VoiceInteractionSession
import android.util.Log
import com.appcycle.OverlayModule

/**
 * When the user invokes the assistant (long-press home) and this app is the default assistant,
 * the system shows this session. We immediately start MainActivity with openOverlay=true so
 * the overlay opens, then finish the session so the user sees the app's BottomSheet.
 */
class AppcycleOverlayVoiceSession(service: AppcycleVoiceInteractionSessionService) :
  VoiceInteractionSession(service) {

  override fun onCreate() {
    super.onCreate()
    triggerShowOverlay()
    finish()
  }

  private fun triggerShowOverlay() {
    val ctx = getContext()
    val intent = Intent(ctx, OverlayOnlyActivity::class.java).apply {
      addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      putExtra(OverlayModule.EXTRA_OPEN_OVERLAY, true)
      putExtra(OverlayModule.EXTRA_OPEN_OVERLAY_ONLY, true)
    }
    try {
      ctx.startActivity(intent)
      Log.d(TAG, "Started OverlayOnlyActivity (from assistant)")
    } catch (e: Exception) {
      Log.e(TAG, "triggerShowOverlay failed", e)
    }
  }

  companion object {
    private const val TAG = "AppcycleOverlayVoice"
  }
}
