package appcycle.example

import android.os.Bundle
import android.service.voice.VoiceInteractionSession
import android.service.voice.VoiceInteractionSessionService

/**
 * Creates a [VoiceInteractionSession] when the user invokes the assistant (e.g. long-press home).
 * The session immediately starts MainActivity with openOverlay=true and finishes.
 */
class AppcycleVoiceInteractionSessionService : VoiceInteractionSessionService() {

  override fun onNewSession(args: Bundle?): VoiceInteractionSession {
    return AppcycleOverlayVoiceSession(this)
  }
}
