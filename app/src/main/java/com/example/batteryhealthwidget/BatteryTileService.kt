package com.example.batteryhealthwidget

import android.graphics.drawable.Icon
import android.provider.Settings
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.util.Log

class BatteryTileService : TileService() {

    private enum class BatteryMode {
        UNRESTRICTED,
        LIMIT_80,
        ADAPTIVE;

        fun next(): BatteryMode = when (this) {
            UNRESTRICTED -> LIMIT_80
            LIMIT_80 -> ADAPTIVE
            ADAPTIVE -> UNRESTRICTED
        }
    }

    override fun onStartListening() {
        super.onStartListening()
        updateTile()
    }

    override fun onClick() {
        super.onClick()

        val nextMode = getCurrentBatteryMode().next()
        setBatteryMode(nextMode)
        updateTile()
    }

    private fun setBatteryMode(mode: BatteryMode) {
        val resolver = contentResolver
        try {
            when (mode) {
                BatteryMode.UNRESTRICTED -> {
                    Settings.Secure.putInt(resolver, "charge_optimization_mode", 0)
                    Settings.Secure.putInt(resolver, "adaptive_charging_enabled", 0)
                }
                BatteryMode.LIMIT_80 -> {
                    Settings.Secure.putInt(resolver, "charge_optimization_mode", 1)
                    Settings.Secure.putInt(resolver, "adaptive_charging_enabled", 0)
                }
                BatteryMode.ADAPTIVE -> {
                    Settings.Secure.putInt(resolver, "charge_optimization_mode", 0)
                    Settings.Secure.putInt(resolver, "adaptive_charging_enabled", 1)
                }
            }
        } catch (e: SecurityException) {
            Log.e("BatteryTileService", "Permission WRITE_SECURE_SETTINGS not granted", e)
            showPermissionError()
        }
    }

    private fun getCurrentBatteryMode(): BatteryMode {
        val resolver = contentResolver
        val optimizationMode = Settings.Secure.getInt(resolver, "charge_optimization_mode", 0)
        val adaptiveMode = Settings.Secure.getInt(resolver, "adaptive_charging_enabled", 0)

        return when {
            optimizationMode == 1 -> BatteryMode.LIMIT_80
            adaptiveMode == 1 -> BatteryMode.ADAPTIVE
            else -> BatteryMode.UNRESTRICTED
        }
    }

    private fun updateTile() {
        val tile = qsTile ?: return
        val mode = getCurrentBatteryMode()

        when (mode) {
            BatteryMode.LIMIT_80 -> {
                tile.state = Tile.STATE_ACTIVE
                tile.label = getString(R.string.tile_label_80_percent)
                tile.icon = Icon.createWithResource(this, R.drawable.ic_battery_80)
            }
            BatteryMode.ADAPTIVE -> {
                tile.state = Tile.STATE_ACTIVE
                tile.label = getString(R.string.tile_label_adaptive)
                tile.icon = Icon.createWithResource(this, R.drawable.ic_battery_adaptive)
            }
            BatteryMode.UNRESTRICTED -> {
                tile.state = Tile.STATE_INACTIVE
                tile.label = getString(R.string.tile_label_unrestricted)
                tile.icon = Icon.createWithResource(this, R.drawable.ic_battery_full)
            }
        }
        tile.updateTile()
    }

    private fun showPermissionError() {
        val tile = qsTile ?: return
        tile.state = Tile.STATE_UNAVAILABLE
        tile.label = getString(R.string.tile_permission_error)
        tile.updateTile()
    }
}
