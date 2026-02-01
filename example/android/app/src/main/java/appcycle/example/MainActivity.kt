package appcycle.example

import android.content.Intent
import com.facebook.react.ReactActivity
import com.facebook.react.ReactActivityDelegate
import com.facebook.react.defaults.DefaultNewArchitectureEntryPoint.fabricEnabled
import com.facebook.react.defaults.DefaultReactActivityDelegate

class MainActivity : ReactActivity() {

  /**
   * Returns the name of the main component registered from JavaScript. This is used to schedule
   * rendering of the component.
   */
  override fun getMainComponentName(): String = "AppcycleExample"

  /**
   * Returns the instance of the [ReactActivityDelegate]. We use [DefaultReactActivityDelegate]
   * which allows you to enable New Architecture with a single boolean flags [fabricEnabled]
   */
  override fun createReactActivityDelegate(): ReactActivityDelegate =
      DefaultReactActivityDelegate(this, mainComponentName, fabricEnabled)

  /**
   * When the app is already running and the runtime service starts the activity with
   * openOverlay=true, the system delivers the intent here. We set the intent so
   * getIntent() returns the latest one and the overlay module can read openOverlay.
   */
  override fun onNewIntent(intent: Intent?) {
    super.onNewIntent(intent)
    intent?.let { setIntent(it) }
  }
}
