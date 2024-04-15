package com.mohit.battery

import android.app.usage.UsageStatsManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.BatteryManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.view.isVisible
import com.mohit.battery.databinding.ActivityMainBinding

class BatteryStatsReceiver(
    private val batteryManager: BatteryManager,
    private val binding: ActivityMainBinding,
    private val maxCharge: Int? = 90
) : BroadcastReceiver() {
    val TAG = "BatteryReceiver"
    private var previousBatteryLevel = -1
    private var maxChargeAllowed: Int? = maxCharge

    override fun onReceive(context: Context?, intent: Intent?) {
        val action = intent?.action

        when (action) {
            Intent.ACTION_BATTERY_CHANGED -> {
                infoUpdate(context,intent)
            }

            Intent.ACTION_POWER_CONNECTED -> {
                Toast.makeText(context, "Charger connected...", Toast.LENGTH_SHORT).show()
                infoUpdate(context,intent)
            }
            Intent.ACTION_POWER_DISCONNECTED -> {
                Toast.makeText(context, "Charger disconnected...", Toast.LENGTH_SHORT).show()
                infoUpdate(context,intent)
            }
        }

    }

    companion object {
        val TAG = "BatteryReceiver"
        fun register(context: Context, receiver: BatteryStatsReceiver) {
            context.registerReceiver(receiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            Log.d(TAG, "registered: ")
        }

        fun unregister(context: Context, receiver: BatteryStatsReceiver) {
            context.unregisterReceiver(receiver)
        }
    }

    //watt/volt convert
    private fun microampereToWatt(microampere: Double) = microampere / 100000

    //battery mAh
    private val POWER_PROFILE_CLASS = "com.android.internal.os.PowerProfile"
    private fun getBatteryTotalCapacity(context: Context): Double {
        var batteryCapacity = 0.0
        try {
            val powerProfile = Class.forName(POWER_PROFILE_CLASS)
                .getConstructor(Context::class.java)
                .newInstance(context)
            batteryCapacity = Class
                .forName(POWER_PROFILE_CLASS)
                .getMethod("getBatteryCapacity")
                .invoke(powerProfile) as Double
        } catch (e: Exception) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val batteryManager =
                    context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
                val chargeCounter =
                    batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER)
                val capacity =
                    batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
                return if (chargeCounter == Int.MIN_VALUE || capacity == Int.MIN_VALUE) 0.0 else (chargeCounter / capacity / 10.0)
            }
        }
        return batteryCapacity
    }

    //charging limit
    private fun checkMaxChargingReached(currentBatteryLevel: Int, context: Context) {
        if (currentBatteryLevel > previousBatteryLevel) {
            previousBatteryLevel = currentBatteryLevel
            if (currentBatteryLevel >= maxChargeAllowed!!) {

                var mediaPlayer: MediaPlayer ?= null
//                mediaPlayer?.stop()
                val ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
                mediaPlayer = MediaPlayer.create(context, ringtoneUri)
//                mediaPlayer.stop()
                mediaPlayer.start()

            }
        }
    }

    private fun infoUpdate(context: Context?, intent: Intent?){
        intent?.let {

            val currentBatteryLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val currentBatteryHealth = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, 0)
            val batteryTemperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0)
            val batteryPlugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0)
            val currentStatus = intent.getIntExtra(BatteryManager.EXTRA_STATUS, 0)
            val batteryTechnology = intent.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                binding.cycleCountLayout.isVisible = true
                val chargingCycleCount = intent.getIntExtra(BatteryManager.EXTRA_CYCLE_COUNT, 0)
                binding.batteryChargingCycleCountTv.text = chargingCycleCount.toString()
            }

            context?.let { checkMaxChargingReached(currentBatteryLevel, it) }
            binding.alarmPercentTv.text = "$maxChargeAllowed %"

            when (currentBatteryHealth) {
                BatteryManager.BATTERY_HEALTH_GOOD -> binding.batteryHealthTv.text = "Good"
                BatteryManager.BATTERY_HEALTH_COLD -> binding.batteryHealthTv.text = "Cold"
                BatteryManager.BATTERY_HEALTH_DEAD -> binding.batteryHealthTv.text = "Dead"
                BatteryManager.BATTERY_HEALTH_OVERHEAT -> binding.batteryHealthTv.text =
                    "OverHeat"

                BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> binding.batteryHealthTv.text =
                    "Over Voltage"

                BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> binding.batteryHealthTv.text =
                    "Unspecified Failure"

                BatteryManager.BATTERY_HEALTH_UNKNOWN -> binding.batteryHealthTv.text =
                    "Health Unknown"
            }
            when (currentStatus) {
                BatteryManager.BATTERY_STATUS_CHARGING -> binding.batteryStatusTv.text =
                    "Charging"

                BatteryManager.BATTERY_STATUS_DISCHARGING -> binding.batteryStatusTv.text =
                    "Discharging"

                BatteryManager.BATTERY_STATUS_FULL -> binding.batteryStatusTv.text = "Full"
                BatteryManager.BATTERY_STATUS_NOT_CHARGING -> binding.batteryStatusTv.text =
                    "Not Charging"

                BatteryManager.BATTERY_STATUS_UNKNOWN -> binding.batteryStatusTv.text =
                    "Unknown"
            }
            when (batteryPlugged) {
                BatteryManager.BATTERY_PLUGGED_AC -> binding.chargingSourceTv.text =
                    "Plugged AC"

                BatteryManager.BATTERY_PLUGGED_DOCK -> binding.chargingSourceTv.text =
                    "Plugged Dock"

                BatteryManager.BATTERY_PLUGGED_USB -> binding.chargingSourceTv.text =
                    "Plugged USB"

                BatteryManager.BATTERY_PLUGGED_WIRELESS -> binding.chargingSourceTv.text =
                    "Plugged Wireless"
            }

            binding.batteryProgressBar.progress = currentBatteryLevel
            binding.currentBatteryPercentageTv.text = currentBatteryLevel.toString()
            val currentTemperature: Float = (batteryTemperature.toFloat()) / 10
            binding.batteryTempTv.text = "$currentTemperature °C"
            binding.chargingVoltageTv.text = microampereToWatt(
                batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
                    .toDouble()
            ).toString()
            binding.batteryCapacityTv.text =
                context?.let { "${getBatteryTotalCapacity(it)} mAh" }
            binding.batteryTechnologyTv.text = batteryTechnology
        }
    }

}