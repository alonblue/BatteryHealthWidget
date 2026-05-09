package com.example.batteryhealthwidget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.*
import android.graphics.drawable.Icon
import android.os.BatteryManager
import android.provider.Settings
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.util.Log
import androidx.core.content.ContextCompat
import kotlinx.coroutines.*
import java.util.Locale
import kotlin.math.abs

class BatteryTileService : TileService() {

    private var updateJob: Job? = null
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private val powerReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Intent.ACTION_POWER_CONNECTED -> startWattageUpdates()
                Intent.ACTION_POWER_DISCONNECTED -> stopWattageUpdates()
            }
        }
    }

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
        
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_POWER_CONNECTED)
            addAction(Intent.ACTION_POWER_DISCONNECTED)
        }
        
        registerReceiver(powerReceiver, filter)

        if (isCharging()) {
            startWattageUpdates()
        } else {
            updateTile()
        }
    }

    override fun onStopListening() {
        super.onStopListening()
        try {
            unregisterReceiver(powerReceiver)
        } catch (e: Exception) {
            // Not registered
        }
        stopWattageUpdates()
    }

    override fun onClick() {
        super.onClick()
        val nextMode = getCurrentBatteryMode().next()
        setBatteryMode(nextMode)
        updateTile()
    }

    private fun isCharging(): Boolean {
        val batteryStatus: Intent? = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        return status == BatteryManager.BATTERY_STATUS_CHARGING || 
               status == BatteryManager.BATTERY_STATUS_FULL
    }

    private fun startWattageUpdates() {
        updateJob?.cancel()
        updateJob = serviceScope.launch {
            while (isActive) {
                updateTile()
                delay(2000)
            }
        }
    }

    private fun stopWattageUpdates() {
        updateJob?.cancel()
        updateJob = null
        updateTile()
    }

    private fun calculateWattage(): String {
        val batteryManager = getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        val currentMicroAmperes = abs(batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW))
        val batteryStatus = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val voltageMilliVolts = batteryStatus?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0) ?: 0
        val wattage = (currentMicroAmperes.toDouble() * voltageMilliVolts.toDouble()) / 1_000_000_000.0
        return String.format(Locale.US, "%.1f", wattage)
    }

    private fun createWattageIcon(baseIconResId: Int, wattageText: String): Icon {
        val size = 96
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        
        // 1. Draw mode icon at the top
        val drawable = ContextCompat.getDrawable(this, baseIconResId)
        drawable?.let {
            it.mutate()
            it.setTint(Color.WHITE)
            val iconSize = 56 // Slightly reduced from 60 to give text more horizontal and vertical breathing room
            val left = (size - iconSize) / 2
            val top = 0
            it.setBounds(left, top, left + iconSize, top + iconSize)
            it.draw(canvas)
        }
        
        // 2. Draw wattage text at the bottom
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 32f // Reduced from 38f to comfortably fit double digits (e.g. "25.5W")
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        
        val text = "${wattageText}W"
        val x = size / 2f
        val y = size - 6f // Moved up slightly to prevent bottom clipping
        canvas.drawText(text, x, y, paint)
        
        return Icon.createWithBitmap(bitmap)
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
                }
                BatteryMode.ADAPTIVE -> {
                    Settings.Secure.putInt(resolver, "charge_optimization_mode", 0)
                    Settings.Secure.putInt(resolver, "adaptive_charging_enabled", 1)
                }
            }
        } catch (e: SecurityException) {
            Log.e("BatteryTileService", "Permission error")
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
        val isCharging = isCharging()
        val wattage = if (isCharging) calculateWattage() else ""

        // Common setup
        tile.state = if (mode == BatteryMode.UNRESTRICTED) Tile.STATE_INACTIVE else Tile.STATE_ACTIVE
        
        val baseIconRes = when (mode) {
            BatteryMode.LIMIT_80 -> R.drawable.ic_battery_80
            BatteryMode.ADAPTIVE -> R.drawable.ic_battery_adaptive
            else -> R.drawable.ic_battery_full
        }

        // Apply dynamic icon: Combine mode icon + wattage if charging
        tile.icon = if (isCharging) {
            createWattageIcon(baseIconRes, wattage)
        } else {
            Icon.createWithResource(this, baseIconRes)
        }

        tile.label = when (mode) {
            BatteryMode.LIMIT_80 -> "Limit: 80%"
            BatteryMode.ADAPTIVE -> "Adaptive"
            else -> "Unrestricted"
        }

        tile.subtitle = if (isCharging) "${wattage}W" else ""

        tile.updateTile()
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}
