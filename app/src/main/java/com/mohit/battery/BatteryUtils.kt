package com.mohit.battery

import android.content.Intent
import android.os.BatteryManager

fun getBatteryPercentage(batteryManager:BatteryManager): Int {
    // Get battery capacity
    return batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
}

fun getBatteryStatus(batteryManager:BatteryManager):String{
    // Get battery status
    val status = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_STATUS)
    return when (status) {
        BatteryManager.BATTERY_STATUS_CHARGING -> "Charging"
        BatteryManager.BATTERY_STATUS_DISCHARGING -> "Discharging"
        BatteryManager.BATTERY_STATUS_FULL -> "Full"
        BatteryManager.BATTERY_STATUS_UNKNOWN -> "Unknown"
        else -> "Undefined"
    }
}

fun getAverageBatteryCurrent(batteryManager:BatteryManager):Int{
    // Get average battery current
    return batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_AVERAGE)
}

fun getCurrentBatteryCurrent(batteryManager:BatteryManager):Int{
    // Get current battery current
    return batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
}

fun getEnergyCounter(batteryManager:BatteryManager):Int{
    // Get energy counter
    val energyCounterMicroAh = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_ENERGY_COUNTER)
    return energyCounterMicroAh / 1000 // Convert to mAh
}

//fun getBatteryHealth(batteryManager: BatteryManager){
//    val x = Intent.getin
//
//    val health = (BatteryManager.EXTRA_HEALTH)
//    when(health){
//        is BatteryManager.BATTERY_HEALTH_COLD -> {}
//    }
//}





