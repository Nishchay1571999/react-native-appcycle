package appcycle.example

import android.content.Intent
import android.os.RemoteException
import android.speech.RecognitionService
import android.speech.RecognitionService.Callback
import android.speech.SpeechRecognizer
import android.util.Log

/**
 * Minimal RecognitionService required for this app to appear in the default digital
 * assistant list (Settings → Voice input). Android requires both VoiceInteractionService
 * and RecognitionService for an app to be selectable as the default assistant.
 *
 * This implementation does not perform speech recognition; it immediately reports
 * ERROR_CLIENT so callers (e.g. other apps using SpeechRecognizer) get a clear failure
 * and can fall back. The app's main purpose is to open the overlay on long-press home,
 * not to provide voice recognition.
 */
class AppcycleRecognitionService : RecognitionService() {

  override fun onStartListening(recognizerIntent: Intent, listener: Callback) {
    try {
      listener.error(SpeechRecognizer.ERROR_CLIENT)
    } catch (e: RemoteException) {
      Log.e(TAG, "RecognitionService callback error", e)
    }
  }

  override fun onStopListening(listener: Callback) {
    // No-op: we don't maintain an active listening session.
  }

  override fun onCancel(listener: Callback) {
    // No-op.
  }

  companion object {
    private const val TAG = "AppcycleRecognition"
  }
}
