package appcycle.example

import android.service.voice.VoiceInteractionService

/**
 * Lightweight voice interaction service. When the user sets this app as the default
 * assistant (Settings → Voice input / Digital assistant app), the system keeps this
 * service bound. When the user invokes the assistant (e.g. long-press home), the
 * system starts [AppcycleVoiceInteractionSessionService] which creates
 * [AppcycleOverlayVoiceSession]; that session starts MainActivity with openOverlay=true.
 */
class AppcycleVoiceInteractionService : VoiceInteractionService()
