package com.example.hotspotconnect
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.WifiManager
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.hotspotconnect.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    private var tvHotspotStatus: TextView? = null
    private var wifiManager: WifiManager? = null
    private val handler = android.os.Handler()
    private val statusChecker: Runnable = object : Runnable {
        override fun run() {
            updateHotspotStatus()
            handler.postDelayed(this, 500) // Check every 5 seconds
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        wifiManager = applicationContext.getSystemService(WIFI_SERVICE) as WifiManager

        binding.btClick.setOnClickListener { v ->
            val intent = Intent(Intent.ACTION_MAIN)
            intent.setClassName("com.android.settings", "com.android.settings.TetherSettings")
            startActivity(intent)
        }

        // Register receiver to listen for Wi-Fi state changes
        registerReceiver(networkStateReceiver, IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION))
        registerReceiver(networkStateReceiver, IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED))

        // Start periodic checking
        handler.post(statusChecker)
    }

    override fun onResume() {
        super.onResume()
        updateHotspotStatus() // Check status when resuming
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(networkStateReceiver)
        handler.removeCallbacks(statusChecker)
    }

    private val networkStateReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            updateHotspotStatus()
        }
    }

    private fun updateHotspotStatus() {
        val isHotspotOn = isWifiApEnabled
        if (isHotspotOn) {
            binding.tvView.text = "Wi-Fi Hotspot is ON"
        } else {
            binding.tvView.text = "Wi-Fi Hotspot is OFF"
        }
    }

    private val isWifiApEnabled: Boolean
        private get() = try {
            val method = wifiManager!!.javaClass.getDeclaredMethod("isWifiApEnabled")
            method.isAccessible = true
            method.invoke(wifiManager) as Boolean
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
}