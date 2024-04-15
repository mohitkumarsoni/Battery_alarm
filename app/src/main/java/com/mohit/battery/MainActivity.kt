package com.mohit.battery

import android.content.Context
import android.os.BatteryManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.slider.Slider
import com.mohit.battery.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    private lateinit var batteryManager:BatteryManager
    private var isRegistered = false
    var valueHistory:Int = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
         batteryManager = getSystemService(Context.BATTERY_SERVICE) as BatteryManager

//        BatteryStatsReceiver.register(this@MainActivity,BatteryStatsReceiver(batteryManager,binding))

        binding.maxChargeSlider.addOnSliderTouchListener(object: Slider.OnSliderTouchListener{
            override fun onStartTrackingTouch(p0: Slider) {

            }

            override fun onStopTrackingTouch(slider: Slider) {
                val currentBatteryLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val value = slider.value.toInt()


                if (value > currentBatteryLevel){
//                    BatteryStatsReceiver.register(this@MainActivity,BatteryStatsReceiver(batteryManager,binding,value))
                    registerBattery(value)
                    valueHistory = value
                }else{
                    slider.value = valueHistory.toFloat()
                    Log.d("TAG", "onStopTrackingTouch: $valueHistory")
                }
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isRegistered) {
            unregisterBattery()
        }
    }

    private fun registerBattery(maxCharge:Int?=90){
        BatteryStatsReceiver.register(this@MainActivity,BatteryStatsReceiver(batteryManager,binding,maxCharge))
        isRegistered = true
        Log.d("TAG", "registerBattery: $maxCharge")
    }

    private fun unregisterBattery(){
        BatteryStatsReceiver.unregister(this, BatteryStatsReceiver(batteryManager, binding))
        isRegistered = false
        Log.d("TAG", "unregisterBattery:")
    }

}







