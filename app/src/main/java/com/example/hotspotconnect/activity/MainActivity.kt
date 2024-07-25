package com.example.hotspotconnect.activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.WifiManager
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.hotspotconnect.databinding.ActivityMainBinding
import com.example.hotspotconnect.utils.visible

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

    private var countDownTimer: CountDownTimer? = null

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

        binding.bt1Min.setOnClickListener {
            startCountdownTimer(1 * 60 * 1000)
        }

        binding.bt2Min.setOnClickListener {
            startCountdownTimer(2 * 60 * 1000)
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
        countDownTimer?.cancel()
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

    private fun startCountdownTimer(duration: Long) {
        countDownTimer?.cancel()
        countDownTimer = object : CountDownTimer(duration, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val minutes = (millisUntilFinished / 1000) / 60
                val seconds = (millisUntilFinished / 1000) % 60
                binding.tvCountdown.text = String.format("%02d:%02d", minutes, seconds)
            }

            override fun onFinish() {
                binding.tvCountdown.text = "00:00"
                turnOffHotspot()
            }
        }.start()
    }

    private fun turnOffHotspot() {
        try {
            val intent = Intent(Intent.ACTION_MAIN)
            intent.setClassName("com.android.settings", "com.android.settings.TetherSettings")
            startActivity(intent)
        } catch (e: Exception) {
            Log.e("MainActivity", "Error turning off hotspot", e)
        }
        updateHotspotStatus()
    }


}