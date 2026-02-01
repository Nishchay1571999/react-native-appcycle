package appcycle.example

import android.os.Bundle
import com.facebook.react.ReactActivity
import com.facebook.react.ReactActivityDelegate
import com.facebook.react.defaults.DefaultNewArchitectureEntryPoint.fabricEnabled
import com.facebook.react.defaults.DefaultReactActivityDelegate

/**
 * Separate activity that shows only the overlay (BottomSheet). Used when the app is invoked
 * from the assistant (long-press home)—we start this activity instead of MainActivity so
 * the user sees only the BottomSheet over the previous app, with a transparent background.
 * Uses the same React component and bundle so the overlay has full access to app API/data.
 * "Open full app" starts MainActivity and finishes this activity.
 */
class OverlayOnlyActivity : ReactActivity() {

  override fun getMainComponentName(): String = "AppcycleExample"

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    window.setBackgroundDrawableResource(android.R.color.transparent)
    window.decorView.setBackgroundColor(android.graphics.Color.TRANSPARENT)
  }

  override fun createReactActivityDelegate(): ReactActivityDelegate =
    DefaultReactActivityDelegate(this, mainComponentName, fabricEnabled)
}
