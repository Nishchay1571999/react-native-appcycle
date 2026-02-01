package com.appcycle

import android.content.Intent
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.util.Log

/**
 * Quick Settings tile that opens the overlay when tapped.
 *
 * User adds the tile from Quick Settings (edit / add tile). When tapped, starts
 * the main Activity with [OverlayModule.ACTION_SHOW_OVERLAY] and
 * [OverlayModule.EXTRA_OPEN_OVERLAY]=true (same flow as the runtime service's
 * triggerShowOverlay()), so the overlay opens even when the runtime service is
 * not running.
 */
class AppcycleOverlayTileService : TileService() {

  override fun onClick() {
    super.onClick()
    triggerShowOverlay()
  }

  private fun triggerShowOverlay() {
    val intent = Intent(OverlayModule.ACTION_SHOW_OVERLAY).apply {
      setPackage(packageName)
      addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      putExtra(OverlayModule.EXTRA_OPEN_OVERLAY, true)
    }
    try {
      startActivityAndCollapse(intent)
      Log.d(TAG, "Started main Activity with openOverlay=true (from tile)")
    } catch (e: Exception) {
      Log.e(TAG, "triggerShowOverlay failed", e)
    }
  }

  override fun onStartListening() {
    super.onStartListening()
    qsTile?.state = Tile.STATE_ACTIVE
    qsTile?.updateTile()
  }

  companion object {
    private const val TAG = "AppcycleOverlayTile"
  }
}
